using PlugPlay.Domain.Common;
using PlugPlay.Domain.Entities;

namespace PlugPlay.Services.Interfaces;

public interface ICartService
{
    Task<Result<int>> AddItemToCartAsync(int productId, int quantity, int userId);

    Task<Result<IEnumerable<CartItem>>> GetUserCartAsync(int userId);
    
    Task<Result<CartItem>> GetCartItemByIdAsync(int itemId);
    
    Task<Result> UpdateQuantityAsync(int itemId, int newQuantity);

    Task<Result> DeleteItemFromCartAsync(int itemId);

    Task<Result> ClearCartAsync(int userId);

    Task<Result<int>> GetCartItemsTotalAsync(int userId);

    Task<Result<bool>> IsInCartAsync(int productId, int userId);
}
