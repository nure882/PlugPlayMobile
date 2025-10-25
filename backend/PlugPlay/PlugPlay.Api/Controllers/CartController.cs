using Microsoft.AspNetCore.Mvc;
using PlugPlay.Api.Dto;
using PlugPlay.Domain.Entities;
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
        _logger.LogInformation("Getting cart for user {UserId}", userId);

        if (userId < 1)
        {
            _logger.LogWarning("Invalid userId: {UserId}", userId);
            
            return BadRequest(new ProblemDetails() { Title = "Invalid userId" });
        }

        var result = await _cartService.GetUserCartAsync(userId);
        result.OnFailure(() =>
                _logger.LogWarning("Problem retrieving a cart: {result.Error}", result.Error))
            .OnSuccess(() => 
                _logger.LogInformation("Retrieved cart for user {UserId}", userId));

        if (result.Failure)
        {
            return BadRequest(new ProblemDetails() { Title = $"Problem retrieving a cart: {result.Error}" });
        }
        
        return Ok(result.Value);
    }

    [HttpGet("item/{itemId:int}")]
    public async Task<IActionResult> GetCartItemAsync(int itemId)
    {
        _logger.LogInformation("Getting cart item {ItemId}", itemId);

        if (itemId < 1)
        {
            _logger.LogWarning("Invalid cart item id: {ItemId}", itemId);
            
            return BadRequest(new ProblemDetails() { Title = "Invalid cart item id" });
        }

        var result = await _cartService.GetCartItemByIdAsync(itemId);
        result.OnFailure(() => 
                _logger.LogWarning("Problem retrieving a cart item: {result.Error}", result.Error))
            .OnSuccess(() => 
                _logger.LogInformation("Retrieved cart item {ItemId}", itemId));
        if (result.Failure)
        {
            return BadRequest(new ProblemDetails() { Title = $"Problem retrieving a cart item: {result.Error}" });
        }
        
        return Ok(result.Value);
    }

    [HttpPost]
    public async Task<IActionResult> AddToCartAsync(CreateCartItemDto dto)
    {
        _logger.LogInformation("Adding item to cart for user {UserId}", dto?.UserId);

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

        var cartItem = new CartItem
        {
            ProductId = dto.ProductId,
            Quantity = dto.Quantity,
            UserId = dto.UserId
        };
        var result = await _cartService.AddItemToCartAsync(cartItem);
        result.OnFailure(() => 
                _logger.LogWarning("Problem adding a cart item: {result.Error}", result.Error))
            .OnSuccess(() => 
                _logger.LogInformation("Added cart item {CartItemId} for user {UserId}", result.Value, dto.UserId));
        if (result.Failure)
        {
            return BadRequest(new ProblemDetails() { Title = $"Problem retrieving a cart item: {result.Error}" });
        }
        
        return StatusCode(StatusCodes.Status201Created, new { CartItemId = result.Value });
    }

    [HttpPut("quantity")]
    public async Task<IActionResult> UpdateQuantityAsync(UpdateCartItemQuantityDto dto)
    {
        _logger.LogInformation("Updating quantity for cart item {CartItemId}", dto?.CartItemId);

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
                _logger.LogInformation("Updated quantity for cart item {CartItemId}", dto.CartItemId));
        if (result.Failure)
        {
            return BadRequest(new ProblemDetails() { Title = $"Problem updating a cart item: {result.Error}" });
        }

        return Ok(result);
    }

    [HttpDelete("{itemId:int}")]
    public async Task<IActionResult> DeleteCartItemAsync(int itemId)
    {
        _logger.LogInformation("Deleting cart item {ItemId}", itemId);

        if (itemId < 1)
        {
            _logger.LogWarning("Invalid itemId: {ItemId}", itemId);

            return BadRequest(new ProblemDetails() { Title = "Invalid userId" });
        }

        var result = await _cartService.DeleteItemFromCartAsync(itemId);
        result.OnFailure(() =>
                _logger.LogWarning("Problem deleting a cart item: {result.Error}", result.Error))
            .OnSuccess(() =>
                _logger.LogInformation("Deleted cart item {ItemId}", itemId));
        if (result.Failure)
        {
            return BadRequest(new ProblemDetails() { Title = $"Problem deleting a cart item: {result.Error}" });
        }

        return Ok();
    }
}
