using PlugPlay.Domain.Entities;

namespace PlugPlay.Services.Interfaces
{
    public interface IProductsService
    {
        Task<IEnumerable<Product>> GetAllProductsAsync();

        Task<Product> GetProductByIdAsync(int id);
    }
}
