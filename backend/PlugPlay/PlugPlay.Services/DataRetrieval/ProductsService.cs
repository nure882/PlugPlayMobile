using Microsoft.EntityFrameworkCore;
using PlugPlay.Domain.Entities;
using PlugPlay.Infrastructure;
using PlugPlay.Services.Interfaces;

namespace PlugPlay.Services.DataRetrieval
{
    public class ProductsService : IProductsService
    {
        private readonly PlugPlayDbContext _context;

        public ProductsService(PlugPlayDbContext context)
        {
            _context = context;
        }

        public async Task<IEnumerable<Product>> GetAllProductsAsync()
        {
            var query = _context.Products
                .Include(p => p.ProductAttributes)
                    .ThenInclude(pa => pa.Attribute)
                .Include(p => p.ProductImages)
                .Include(p => p.Category)
                .AsQueryable();

            return await query.ToListAsync();
        }

        public async Task<Product> GetProductByIdAsync(int id)
        {
            var product = await _context.Products
                .Include(p => p.ProductAttributes)
                    .ThenInclude(pa => pa.Attribute)
                .Include(p => p.ProductImages)
                .Include(p => p.Category)
                .Include(p => p.Reviews)
                .FirstOrDefaultAsync(p => p.Id == id);

            if (product == null)
            {
                throw new KeyNotFoundException($"Product with ID {id} not found.");
            }

            return product;
        }
    }
}
