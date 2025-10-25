using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Logging;
using PlugPlay.Domain.Common;
using PlugPlay.Domain.Entities;
using PlugPlay.Infrastructure;
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

    public async Task<Result<int>> AddItemToCartAsync(CartItem item)
    {
        if (item == null)
        {
            return Result.Fail<int>("Invalid cart item");
        }

        if (item.Quantity <= 0)
        {
            return Result.Fail<int>("Quantity must be greater than zero");
        }

        item.Id = 0;
        try
        {
            var product = await _context.Products.FindAsync(item.ProductId);
            if (product == null)
            {
                return Result.Fail<int>($"Product with id {item.ProductId} not found");
            }

            item.Total = product.Price * item.Quantity;

            var entityEntry = await _context.CartItems.AddAsync(item);
            await _context.SaveChangesAsync();

            return Result.Success(entityEntry.Entity.Id);
        }
        catch (Exception e)
        {
            _logger.LogError(e, "Error adding item to cart");

            return Result.Fail<int>($"Problem adding an item: {e.Message}");
        }
    }

    public async Task<Result<IEnumerable<CartItem>>> GetUserCartAsync(int userId)
    {
        if (userId < 1)
        {
            return Result.Fail<IEnumerable<CartItem>>("Invalid userId");
        }

        var cart = await _context.CartItems
            .Where(ci => ci.UserId == userId)
            .AsNoTracking()
            .ToListAsync();

        return Result.Success<IEnumerable<CartItem>>(cart);
    }

    public async Task<Result<CartItem>> GetCartItemByIdAsync(int itemId)
    {
        if (itemId < 1)
        {
            return Result.Fail<CartItem>("Invalid item id");
        }

        var cartItem = await _context.CartItems
            .AsNoTracking()
            .FirstOrDefaultAsync(ci => ci.Id == itemId);

        if (cartItem is null)
        {
            return Result.Fail<CartItem>($"No cart item with id {itemId}");
        }

        return Result.Success(cartItem);
    }

    public async Task<Result> UpdateQuantityAsync(int itemId, int newQuantity)
    {
        if (itemId < 1)
        {
            return Result.Fail("Invalid item id");
        }

        if (newQuantity < 1)
        {
            return Result.Fail("Quantity must be greater than zero");
        }

        try
        {
            var cartItem = await _context.CartItems
                .Include(ci => ci.Product)
                .FirstOrDefaultAsync(ci => ci.Id == itemId);
            if (cartItem is null)
            {
                return Result.Fail($"No cart item {itemId}");
            }

            if (cartItem.Product is null)
            {
                return Result.Fail($"No product related to cart item {itemId}");
            }

            cartItem.Quantity = newQuantity;
            cartItem.Total = cartItem.Product.Price * newQuantity;

            await _context.SaveChangesAsync();
        }
        catch (Exception e)
        {
            _logger.LogError(e, "Error updating cart item {ItemId}", itemId);

            return Result.Fail($"Problem updating cart item {itemId}");
        }

        return Result.Success();
    }

    public async Task<Result> DeleteItemFromCartAsync(int itemId)
    {
        if (itemId < 1)
        {
            return Result.Fail("Invalid item id");
        }

        try
        {
            var entity = await _context.CartItems.FindAsync(itemId);

            if (entity == null)
            {
                return Result.Fail($"No cart item {itemId}");
            }

            _context.Remove(entity);
            await _context.SaveChangesAsync();
        }
        catch (Exception e)
        {
            _logger.LogError(e, "Error deleting cart item {ItemId}", itemId);

            return Result.Fail($"Problem deleting cart item {itemId}");
        }

        return Result.Success();
    }
}
