using System.Runtime.CompilerServices;
using LinqKit;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Logging;
using PlugPlay.Domain.Common;
using PlugPlay.Domain.Entities;
using PlugPlay.Infrastructure;
using PlugPlay.Services.Interfaces;
using Attribute = PlugPlay.Domain.Entities.Attribute;

namespace PlugPlay.Services.Products;

public class ProductsService : IProductsService
{
    private readonly PlugPlayDbContext _context;

    private readonly ILogger<ProductsService> _logger;

    public ProductsService(PlugPlayDbContext context, ILogger<ProductsService> logger)
    {
        _context = context;
        _logger = logger;
    }

    public async Task<IEnumerable<Product>> GetAllProductsAsync()
    {
        _logger.LogInformation("Fetching all products");

        var query = _context.Products
            .Include(p => p.ProductAttributes)
            .ThenInclude(pa => pa.Attribute)
            .Include(p => p.ProductImages)
            .Include(p => p.Category)
            .Include(p => p.Reviews)
            .AsQueryable();

        var products = await query.ToListAsync();
        _logger.LogInformation("Successfully retrieved {Count} products", products.Count);

        return products;
    }

    public async Task<Result<IEnumerable<Product>>> GetAvailableProductsAsync()
    {
        _logger.LogInformation("Fetching available products");

        var products = _context.Products
            .Where(p => p.StockQuantity != 0)
            .Include(p => p.ProductAttributes)
            .ThenInclude(pa => pa.Attribute)
            .Include(p => p.ProductImages)
            .Include(p => p.Category)
            .Include(p => p.Reviews);

        _logger.LogInformation("Successfully retrieved {Count} products", products.Count());

        return Result.Success<IEnumerable<Product>>(products);
    }

    public async Task<Result> AddImageAsync(int productId, string uploadResultUrl)
    {
        _logger.LogInformation("Adding image for product {ProductId}", productId);
        var product = await _context.Products.FindAsync(productId);
        if (product is null)
        {
            _logger.LogWarning("Product with ID {ProductId} not found", productId);

            return Result.Fail("No such product");
        }

        try
        {
            var image = new ProductImage
            {
                ImageUrl = uploadResultUrl,
                ProductId = productId
            };
            _context.ProductImages.Add(image);
            await _context.SaveChangesAsync();
            _logger.LogInformation("Successfully added image for product {ProductId}", productId);
        }
        catch (Exception e)
        {
            _logger.LogError(e, "Failed to add image for product {ProductId}", productId);

            return Result.Fail($"{e.Message}");
        }

        return Result.Success();
    }

    public async Task<Result<IEnumerable<Product>>> FilterProductsAsync(FilterProductsRequest request)
    {
        try
        {
            var query = _context.Products.AsQueryable().AsExpandable();

            if (request.Predicate != null)
            {
                query = query.Where(request.Predicate);
            }

            if (request.Includes != null)
            {
                foreach (var include in request.Includes)
                {
                    query = include(query);
                }
            }

            query = request.OrderBy != null ? request.OrderBy(query) : query.OrderBy(p => p.Price);

            query = query.Skip(request.SkipCount).Take(request.TakeCount ?? 25);

            var products = await query.AsNoTracking().ToListAsync();

            return Result.Success<IEnumerable<Product>>(products);
        }
        catch (Exception e)
        {
            _logger.LogError(e, "Error filtering products");
            return Result.Fail<IEnumerable<Product>>($"Problem filtering products: {e.Message}");
        }
    }

    public async Task<Result<IEnumerable<Attribute>>> GetCategoryAttributesAsync(
        int categoryId,
        int[] productIds = null)
    {
        List<int> categoryIds;
        int[] targetProductIds = null;

        // all available
        if (categoryId == int.MaxValue)
        {
            var categoryIdsWithProducts = await _context.Products
                .Select(p => p.CategoryId)
                .Distinct()
                .ToListAsync();

            categoryIds = await _context.Categories
                .Where(c => categoryIdsWithProducts.Contains(c.Id))
                .Select(c => c.Id)
                .ToListAsync();

            if (productIds != null && productIds.Length > 0)
            {
                targetProductIds = productIds;
            }
            else
            {
                targetProductIds = await _context.Products
                    .Where(p => p.StockQuantity != 0)
                    .Select(p => p.Id)
                    .ToArrayAsync();
            }
        }
        // category specified
        else
        {
            const string descendantsSql = """
                                              WITH RECURSIVE descendants AS (
                                                  SELECT * FROM "category" 
                                                  WHERE "id" = {0}
                                                  UNION ALL
                                                  SELECT c.* FROM "category" c 
                                                  INNER JOIN descendants d ON c."parent_category_id" = d."id"
                                              )
                                              SELECT * FROM descendants
                                          """;
            var descendantCategories = await _context.Categories
                .FromSqlInterpolated(FormattableStringFactory.Create(descendantsSql, categoryId))
                .AsNoTracking()
                .ToListAsync();

            if (descendantCategories.Count == 0)
            {
                return Result.Fail<IEnumerable<Attribute>>($"No category with id {categoryId}");
            }

            categoryIds = descendantCategories.Select(c => c.Id).ToList();

            if (productIds != null && productIds.Length > 0)
            {
                targetProductIds = productIds;
            }
        }

        IQueryable<int> productsQuery;

        if (targetProductIds != null && targetProductIds.Length > 0)
        {
            productsQuery = _context.Products
                .AsNoTracking()
                .Where(p => categoryIds.Contains(p.CategoryId) && targetProductIds.Contains(p.Id))
                .Select(p => p.Id);
        }
        else
        {
            productsQuery = _context.Products
                .AsNoTracking()
                .Where(p => categoryIds.Contains(p.CategoryId))
                .Select(p => p.Id);
        }

        var attributeCounts = await _context.ProductAttributes
            .AsNoTracking()
            .Where(pa => productsQuery.Contains(pa.ProductId))
            .GroupBy(pa => pa.AttributeId)
            .Select(g => new { AttributeId = g.Key })
            .ToListAsync();

        if (attributeCounts.Count == 0)
        {
            return Result.Success<IEnumerable<Attribute>>(new List<Attribute>());
        }

        var attributeIds = attributeCounts.Select(ac => ac.AttributeId).ToList();

        var attributes = await _context.Attributes
            .AsNoTracking()
            .Where(a => attributeIds.Contains(a.Id))
            .Include(a => a.ProductAttributes.Where(pa =>
                targetProductIds == null || targetProductIds.Contains(pa.ProductId)))
            .ToListAsync();

        return Result.Success<IEnumerable<Attribute>>(attributes);
    }

    public async Task<Result<Category>> GetCategoryAsync(int categoryId)
    {
        var category = await _context.Categories
            .Include(c => c.ParentCategory)
            .Include(c => c.SubCategories)
            .FirstOrDefaultAsync(c => c.Id == categoryId);

        if (category is null)
        {
            return Result.Fail<Category>($"No category {categoryId}");
        }

        return Result.Success(category);
    }

    public async Task<Product> GetProductByIdAsync(int id)
    {
        _logger.LogInformation("Fetching product with ID: {ProductId}", id);

        var product = await _context.Products
            .Include(p => p.ProductAttributes)
            .ThenInclude(pa => pa.Attribute)
            .Include(p => p.ProductImages)
            .Include(p => p.Category)
            .Include(p => p.Reviews)
            .FirstOrDefaultAsync(p => p.Id == id);

        if (product == null)
        {
            _logger.LogWarning("Product with ID {ProductId} not found", id);
            throw new KeyNotFoundException($"Product with ID {id} not found.");
        }

        _logger.LogInformation("Successfully retrieved product with ID: {ProductId}", id);

        return product;
    }
}