using Microsoft.AspNetCore.Mvc;
using PlugPlay.Api.Dto.Cart;
using PlugPlay.Domain.Extensions;
using PlugPlay.Services.Interfaces;

namespace PlugPlay.Api.Controllers;

[ApiController]
[Route("api/[controller]")]
public class CartController : ControllerBase
{
    private readonly ICartService _cartService;

    private readonly ILogger<CartController> _logger;

    public CartController(ICartService cartService, ILogger<CartController> logger)
    {
        _cartService = cartService;
        _logger = logger;
    }

    [HttpGet("{userId:int}")]
    public async Task<IActionResult> GetCartAsync(int userId)
    {
        var gettingCart = LoggerMessage.Define<int>(
            LogLevel.Information,
            new EventId(4000, "GettingCart"),
            "Getting cart for user {UserId}");

        gettingCart(_logger, userId, null);

        if (userId < 1)
        {
            var invalidUserId = LoggerMessage.Define<int>(
                LogLevel.Warning,
                new EventId(4001, "InvalidUserId"),
                "Invalid userId: {UserId}");

            invalidUserId(_logger, userId, null);

            return BadRequest(new ProblemDetails() { Title = "Invalid userId" });
        }

        var result = await _cartService.GetUserCartAsync(userId);
        result.OnFailure(() =>
                _logger.LogWarning("Problem retrieving a cart: {result.Error}", result.Error))
            .OnSuccess(() =>
            {
                var cartRetrieved = LoggerMessage.Define<int>(
                    LogLevel.Information,
                    new EventId(4002, "CartRetrieved"),
                    "Retrieved cart for user {UserId}");

                cartRetrieved(_logger, userId, null);
            });
               
        if (result.Failure)
        {
            return BadRequest(new ProblemDetails() { Title = $"Problem retrieving a cart: {result.Error}" });
        }
        
        return Ok(result.Value);
    }

    [HttpGet("item/{itemId:int}")]
    public async Task<IActionResult> GetCartItemAsync(int itemId)
    {
        var gettingCartItem = LoggerMessage.Define<int>(
            LogLevel.Information,
            new EventId(4000, "GettingCartItem"),
            "Getting cart item {ItemId}");

        gettingCartItem(_logger, itemId, null);

        if (itemId < 1)
        {
            var invalidCartItemId = LoggerMessage.Define<int>(
                LogLevel.Warning,
                new EventId(4001, "InvalidCartItemId"),
                "Invalid cart item id: {ItemId}");

            invalidCartItemId(_logger, itemId, null);

            return BadRequest(new ProblemDetails() { Title = "Invalid cart item id" });
        }

        var result = await _cartService.GetCartItemByIdAsync(itemId);
        result.OnFailure(() =>
                _logger.LogWarning("Problem retrieving a cart item: {result.Error}", result.Error))
            .OnSuccess(() =>
            {
                var cartItemRetrieved = LoggerMessage.Define<int>(
                    LogLevel.Information,
                    new EventId(4002, "CartItemRetrieved"),
                    "Retrieved cart item {ItemId}");

                cartItemRetrieved(_logger, itemId, null);
            });
                
        if (result.Failure)
        {
            return BadRequest(new ProblemDetails() { Title = $"Problem retrieving a cart item: {result.Error}" });
        }
        
        return Ok(result.Value);
    }

    [HttpPost]
    public async Task<IActionResult> AddToCartAsync(CreateCartItemDto dto)
    {
        var addingItemToCart = LoggerMessage.Define<int>(
            LogLevel.Information,
            new EventId(4000, "AddingItemToCart"),
            "Adding item to cart for user {UserId}");

        addingItemToCart(_logger, dto?.UserId ?? 0, null);

        if (dto is null)
        {
            _logger.LogWarning("Invalid cart item data provided");
            
            return BadRequest(new ProblemDetails { Title = "Invalid cart item data" });
        }

        if (dto.UserId < 1 || dto.ProductId < 1 || dto.Quantity < 1)
        {
            _logger.LogWarning("Invalid cart item fields provided");

            return BadRequest(new ProblemDetails { Title = "Invalid cart item fields" });
        }

        var result = await _cartService.AddItemToCartAsync(dto.ProductId, dto.Quantity, dto.UserId);
        result.OnFailure(() =>
                _logger.LogWarning("Problem adding a cart item: {result.Error}", result.Error))
            .OnSuccess(() =>
            {
                var cartItemAdded = LoggerMessage.Define<int, int>(
                    LogLevel.Information,
                    new EventId(4002, "CartItemAdded"),
                    "Added cart item {CartItemId} for user {UserId}");

                cartItemAdded(_logger, result.Value, dto.UserId, null);
            });
               
        if (result.Failure)
        {
            return BadRequest(new ProblemDetails() { Title = $"Problem retrieving a cart item: {result.Error}" });
        }
        
        return StatusCode(StatusCodes.Status201Created, new { CartItemId = result.Value });
    }

    [HttpPut("quantity")]
    public async Task<IActionResult> UpdateQuantityAsync(UpdateCartItemQuantityDto dto)
    {
        var updatingCartItemQuantity = LoggerMessage.Define<int>(
            LogLevel.Information,
            new EventId(4000, "UpdatingCartItemQuantity"),
            "Updating quantity for cart item {CartItemId}");

        updatingCartItemQuantity(_logger, dto?.CartItemId ?? 0, null);

        if (dto == null)
        {
            _logger.LogWarning("Invalid quantity data provided");
            
            return BadRequest(new ProblemDetails() { Title = "Invalid quantity data" });
        }

        if (dto.CartItemId < 1 || dto.NewQuantity < 1)
        {
            _logger.LogWarning("Invalid quantity fields provided");

            return BadRequest(new ProblemDetails() { Title = "Invalid quantity fields" });
        }

        var result = await _cartService.UpdateQuantityAsync(dto.CartItemId, dto.NewQuantity);
        result.OnFailure(() =>
                _logger.LogWarning("Problem updating a cart item: {result.Error}", result.Error))
            .OnSuccess(() =>
            {
                var cartItemQuantityUpdated = LoggerMessage.Define<int>(
                    LogLevel.Information,
                    new EventId(4001, "CartItemQuantityUpdated"),
                    "Updated quantity for cart item {CartItemId}");

                cartItemQuantityUpdated(_logger, dto.CartItemId, null);
            });
               
        if (result.Failure)
        {
            return BadRequest(new ProblemDetails() { Title = $"Problem updating a cart item: {result.Error}" });
        }

        return Ok();
    }

    [HttpDelete("{itemId:int}")]
    public async Task<IActionResult> DeleteCartItemAsync(int itemId)
    {
        var deletingCartItem = LoggerMessage.Define<int>(
            LogLevel.Information,
            new EventId(4000, "DeletingCartItem"),
            "Deleting cart item {ItemId}");

        deletingCartItem(_logger, itemId, null);

        if (itemId < 1)
        {
            var invalidItemId = LoggerMessage.Define<int>(
                LogLevel.Warning,
                new EventId(4001, "InvalidItemId"),
                "Invalid itemId: {ItemId}");

            invalidItemId(_logger, itemId, null);

            return BadRequest(new ProblemDetails() { Title = "Invalid userId" });
        }

        var result = await _cartService.DeleteItemFromCartAsync(itemId);
        result.OnFailure(() =>
                _logger.LogWarning("Problem deleting a cart item: {result.Error}", result.Error))
            .OnSuccess(() =>
            {
                var cartItemDeleted = LoggerMessage.Define<int>(
                    LogLevel.Information,
                    new EventId(4002, "CartItemDeleted"),
                    "Deleted cart item {ItemId}");

                cartItemDeleted(_logger, itemId, null);
            });
        if (result.Failure)
        {
            return BadRequest(new ProblemDetails() { Title = $"Problem deleting a cart item: {result.Error}" });
        }

        return Ok();
    }

    [HttpDelete("clear/{userId:int}")]
    public async Task<IActionResult> ClearCartAsync(int userId)
    {
        var clearingCart = LoggerMessage.Define<int>(
            LogLevel.Information,
            new EventId(4000, "ClearingCart"),
            "Clearing cart for user {UserId}");

        clearingCart(_logger, userId, null);

        if (userId < 1)
        {
            var invalidUserIdWarning = LoggerMessage.Define<int>(
                LogLevel.Warning,
                new EventId(4001, "InvalidUserIdWarning"),
                "Invalid userId: {UserId}");

            invalidUserIdWarning(_logger, userId, null);

            return BadRequest(new ProblemDetails { Title = "Invalid userId" });
        }

        var result = await _cartService.ClearCartAsync(userId);
        result.OnFailure(() => _logger.LogWarning("Problem clearing cart: {Error}", result.Error))
              .OnSuccess(() =>
              {
                  var cartCleared = LoggerMessage.Define<int>(
                      LogLevel.Information,
                      new EventId(4002, "CartCleared"),
                      "Cleared cart for user {UserId}");

                  cartCleared(_logger, userId, null);
              });

        if (result.Failure)
        {
            return BadRequest(new ProblemDetails { Title = $"Problem clearing cart: {result.Error}" });
        }

        return Ok();
    }

    [HttpGet("total/{userId:int}")]
    public async Task<ActionResult<int>> GetCartItemsTotalAsync(int userId)
    {
        var gettingCartTotal = LoggerMessage.Define<int>(
            LogLevel.Information,
            new EventId(4000, "GettingCartTotal"),
            "Getting cart items total for user {UserId}");

        gettingCartTotal(_logger, userId, null);

        if (userId < 1)
        {
            var invalidUserIdCartTotal = LoggerMessage.Define<int>(
                LogLevel.Warning,
                new EventId(4001, "InvalidUserIdCartTotal"),
                "Invalid userId: {UserId}");

            invalidUserIdCartTotal(_logger, userId, null);

            return BadRequest(new ProblemDetails { Title = "Invalid userId" });
        }

        var result = await _cartService.GetCartItemsTotalAsync(userId);
        result.OnFailure(() =>
                _logger.LogWarning("Problem getting cart items total: {Error}", result.Error))
              .OnSuccess(() =>
              {
                  var cartTotalRetrieved = LoggerMessage.Define<int, decimal>(
                      LogLevel.Information,
                      new EventId(4002, "CartTotalRetrieved"),
                      "Cart items total for user {UserId}: {Total}");

                  cartTotalRetrieved(_logger, userId, result.Value, null);
              });

        if (result.Failure)
        {
            return BadRequest(new ProblemDetails { Title = $"Problem getting cart total: {result.Error}" });
        }

        return result.Value;
    }

    [HttpGet("isincart/{productId:int}/{userId:int}")]
    public async Task<ActionResult<bool>> IsInCartAsync(int productId, int userId)
    {
        var checkingProductInCart = LoggerMessage.Define<int, int>(
            LogLevel.Information,
            new EventId(4000, "CheckingProductInCart"),
            "Checking if product {ProductId} is in cart for user {UserId}");

        checkingProductInCart(_logger, productId, userId, null);

        if (productId < 1 || userId < 1)
        {
            var invalidProductOrUser = LoggerMessage.Define<int, int>(
                LogLevel.Warning,
                new EventId(4001, "InvalidProductOrUser"),
                "Invalid productId or userId: {ProductId}, {UserId}");

            invalidProductOrUser(_logger, productId, userId, null);

            return BadRequest(new ProblemDetails { Title = "Invalid productId or userId" });
        }

        var result = await _cartService.IsInCartAsync(productId, userId);
        result.OnFailure(() =>
                _logger.LogWarning("Problem checking cart item presence: {Error}", result.Error))
              .OnSuccess(() =>
              {
                  var productPresenceChecked = LoggerMessage.Define<int, int, bool>(
                      LogLevel.Information,
                      new EventId(4002, "ProductPresenceChecked"),
                      "Product {ProductId} presence for user {UserId}: {Exists}");

                  productPresenceChecked(_logger, productId, userId, result.Value, null);
              });

        if (result.Failure)
        {
            return BadRequest(new ProblemDetails { Title = $"Problem checking cart item: {result.Error}" });
        }

        return result.Value;
    }
}
