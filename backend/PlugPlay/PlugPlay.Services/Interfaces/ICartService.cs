using PlugPlay.Domain.Common;
using PlugPlay.Domain.Entities;

namespace PlugPlay.Services.Interfaces;

public interface ICartService
{
    Task<Result<int>> AddItemToCartAsync(CartItem item);

    Task<Result<IEnumerable<CartItem>>> GetUserCartAsync(int userId);
    
    Task<Result<CartItem>> GetCartItemByIdAsync(int itemId);
    
    Task<Result> UpdateQuantityAsync(int itemId, int newQuantity);

    Task<Result> DeleteItemFromCartAsync(int itemId);
}
