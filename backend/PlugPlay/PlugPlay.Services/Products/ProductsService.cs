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

    public async Task<Result<IEnumerable<Attribute>>> GetCategoryAttributesAsync(int categoryId)
    {
        var category = await _context.Categories.FindAsync(categoryId);
        if (category is null)
        {
            return Result.Fail<IEnumerable<Attribute>>($"No category with id {categoryId}");
        }

        var products = _context.Products.AsQueryable();
        if (await products.Where(p => p.CategoryId == categoryId && p.Category.ParentCategoryId != null)
                .CountAsync() != 0)
        {
            while (await _context.Categories
                       .Where(c => c.SubCategories.Contains(category)).CountAsync() > 1)
            {
                category = await _context.Categories
                    .Where(c => c.SubCategories.Contains(category))
                    .FirstOrDefaultAsync();
            }
        }

        var attributeIds = new List<int>();

        foreach (var p in products.Include(product => product.ProductAttributes))
        {
            foreach (var pa in p.ProductAttributes)
            {
                if (!attributeIds.Contains(pa.AttributeId))
                {
                    attributeIds.Add(pa.AttributeId);
                }
            }
        }

        var attributes = await _context.Attributes.FromSqlInterpolated(@"SELECT * FROM attribute a
                                                                WHERE a.id IN {attributeIds");

        return Result.Success(attributes);
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
