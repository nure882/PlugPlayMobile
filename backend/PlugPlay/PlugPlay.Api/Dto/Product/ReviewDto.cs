namespace PlugPlay.Api.Dto.Product;

public record ReviewDto(
    int Id, 
    int ProductId,
    int? UserId,
    int Rating,
    string Comment,
    DateTime CreatedAt,
    DateTime? UpdatedAt);
