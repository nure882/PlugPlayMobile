namespace PlugPlay.Api.Dto.Cart;

public class UpdateCartItemQuantityDto
{
    public int CartItemId { get; init; }
    
    public int NewQuantity { get; init; }
}
