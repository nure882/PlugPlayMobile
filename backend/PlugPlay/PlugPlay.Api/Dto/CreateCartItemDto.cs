namespace PlugPlay.Api.Dto;

public record CreateCartItemDto
{
    public int ProductId { get; init; }

    public int UserId { get; init; }

    public int Quantity { get; init; }

    public int Total { get; init; }
}
