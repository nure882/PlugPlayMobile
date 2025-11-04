using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Logging;
using PlugPlay.Domain.Common;
using PlugPlay.Domain.Entities;
using PlugPlay.Domain.Extensions;
using PlugPlay.Infrastructure;
using PlugPlay.Services.Interfaces;

namespace PlugPlay.Services.Products
{
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

        public async Task<Result<IEnumerable<Product>>> SearchProductsAsync(ProductSearchRequest req)
        {
            _logger.LogInformation("Fetching available products");

            try
            {
                var query = _context.Products.AsNoTracking().AsQueryable();

                if (!string.IsNullOrWhiteSpace(req.Query))
                {
                    var pattern = $"%{req.Query}%";
                    query = query.Where(p =>
                        EF.Functions.ILike(p.Name, pattern) ||
                        EF.Functions.ILike(p.Description, pattern))
                        .Include(p => p.ProductImages);
                }

                var pageSize = Math.Clamp(req.PageSize, 1, 100);
                var page = Math.Max(1, req.Page);
                var skip = (page - 1) * pageSize;

                var products = await query
                    .Skip(skip)
                    .Take(pageSize)
                    .ToListAsync();

                _logger.LogInformation("Successfully retrieved {Count} products", products.Count);

                return Result.Success<IEnumerable<Product>>(products);
            }
            catch (Exception e)
            {
                return Result.Fail<IEnumerable<Product>>($"Error searching products: {e.Message}");
            }
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
    }
}