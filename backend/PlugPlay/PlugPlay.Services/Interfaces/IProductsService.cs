using PlugPlay.Domain.Common;
using PlugPlay.Domain.Entities;
using PlugPlay.Services.Products;

namespace PlugPlay.Services.Interfaces
{
    public interface IProductsService
    {
        Task<IEnumerable<Product>> GetAllProductsAsync();

        Task<Product> GetProductByIdAsync(int id);

        Task<Result<IEnumerable<Product>>> GetAvailableProductsAsync();

        Task<Result<IEnumerable<Product>>> SearchProductsAsync(ProductSearchRequest searchRequest);

        Task<Result> AddImageAsync(int productId, string uploadResultUrl);
    }
}
