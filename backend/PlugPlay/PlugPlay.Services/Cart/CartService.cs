using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Logging;
using PlugPlay.Domain.Common;
using PlugPlay.Domain.Entities;
using PlugPlay.Domain.Extensions;
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

    public async Task<Result<int>> AddItemToCartAsync(int productId, int quantity, int userId)
    {
        if (userId < 1)
        {
            return Result.Fail<int>("Invalid user id");
        }

        if (quantity < 1)
        {
            return Result.Fail<int>("Quantity must be greater than zero");
        }

        var product = await _context.Products.FindAsync(productId);
        if (product is null)
        {
            return Result.Fail<int>($"Product with id {productId} not found");
        }

        var item = await _context.CartItems
            .FirstOrDefaultAsync(ci => ci.UserId == userId && ci.ProductId == productId);
        var existed = true;
        if (item is null)
        {
            item = new CartItem
            {
                ProductId = productId,
                UserId = userId
            };
            existed = false;
        }

        item.Quantity = existed ? item.Quantity + quantity : quantity;
        item.Total = product.Price * item.Quantity;
        try
        {
            var entityEntry = existed ? _context.CartItems.Update(item) : await _context.CartItems.AddAsync(item);
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
            var errorUpdatingCartItem = LoggerMessage.Define<int>(
                LogLevel.Error,
                new EventId(4002, "ErrorUpdatingCartItem"),
                "Error updating cart item {ItemId}");

            errorUpdatingCartItem(_logger, itemId, e);

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
            var errorDeletingCartItem = LoggerMessage.Define<int>(
                LogLevel.Error,
                new EventId(4001, "ErrorDeletingCartItem"),
                "Error deleting cart item {ItemId}");

            errorDeletingCartItem(_logger, itemId, e);

            return Result.Fail($"Problem deleting cart item {itemId}");
        }

        return Result.Success();
    }

    public async Task<Result> ClearCartAsync(int userId)
    {
        if (userId < 1)
        {
            return Result.Fail("Invalid user id");
        }

        try
        {
            await _context.CartItems.Where(ci => ci.UserId == userId).ExecuteDeleteAsync();
        }
        catch (Exception e)
        {
            var errorDeletingCart = LoggerMessage.Define<int>(
                LogLevel.Error,
                new EventId(4001, "ErrorDeletingCart"),
                "Error deleting cart of user {UserId}");

            errorDeletingCart(_logger, userId, e);

            return Result.Fail($"Problem deleting cart of user {userId}");
        }

        return Result.Success();
    }

    public async Task<Result<int>> GetCartItemsTotalAsync(int userId)
        => userId < 1 || await _context.Users.FindAsync(userId) == null
            ? Result.Fail<int>($"Problem getting cart total for user {userId}")
            : Result.Success(await _context.CartItems.Where(ci => ci.UserId == userId).CountAsync());

    public async Task<Result<bool>> IsInCartAsync(int productId, int userId)
        => userId < 1 || productId < 1 || await _context.Users.FindAsync(userId) == null
            ? Result.Fail<bool>($"Error checking cart item presence for user {userId}, product {productId}")
            : Result.Success(
                await _context.CartItems.
                    FirstOrDefaultAsync(ci => ci.ProductId == productId && ci.UserId == userId) != null);
}
