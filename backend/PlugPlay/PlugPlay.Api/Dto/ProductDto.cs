namespace PlugPlay.Api.Dto;

public record ProductDto(
    int Id,
    string Name,
    string Description,
    decimal Price,
    int StockQuantity,
    DateTime CreatedAt,
    CategoryDto? Category);