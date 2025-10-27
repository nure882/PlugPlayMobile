namespace PlugPlay.Api.Dto;

public class UpdateCartItemQuantityDto
{
    public int CartItemId { get; init; }
    
    public int NewQuantity { get; init; }
}
