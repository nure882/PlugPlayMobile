using Microsoft.Extensions.Logging;
using PlugPlay.Domain.Common;
using PlugPlay.Domain.Entities;
using PlugPlay.Infrastructure;
using PlugPlay.Services.Auth;
using PlugPlay.Services.Interfaces;

namespace PlugPlay.Services.Cart;

public class CartService : ICartService
{
    private readonly PlugPlayDbContext _context;

    private readonly ILogger<CartService> _logger;

    public CartService(PlugPlayDbContext context, ILogger<CartService> logger)
    {
        _context = context;
        _logger = logger;
    }

    public Task<Result<int>> AddItemToCartAsync(CartItem item)
    {
        throw new NotImplementedException();
    }

    public Task<Result<IEnumerable<CartItem>>> GetUserCartAsync(int userId)
    {
        throw new NotImplementedException();
    }

    public Task<Result<CartItem>> GetCartItemByIdAsync(int itemId)
    {
        throw new NotImplementedException();
    }

    public Task<Result<int>> UpdateQuantityAsync(int itemId, int newQuantity)
    {
        throw new NotImplementedException();
    }

    public Task<Result> DeleteItemFromCartAsync(int itemId)
    {
        throw new NotImplementedException();
    }
}
