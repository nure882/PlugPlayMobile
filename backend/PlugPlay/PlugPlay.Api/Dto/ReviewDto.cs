namespace PlugPlay.Api.Dto;

public record ReviewDto(
    int Id, 
    int ProductId,
    int? UserId,
    int Rating,
    string Comment,
    DateTime CreatedAt,
    DateTime? UpdatedAt);
