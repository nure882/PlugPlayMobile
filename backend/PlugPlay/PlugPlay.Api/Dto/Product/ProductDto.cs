using Attribute = PlugPlay.Domain.Entities.Attribute;

namespace PlugPlay.Api.Dto.Product;

public record ProductDto(
    int Id,
    string Name,
    string Description,
    decimal Price,
    int StockQuantity,
    DateTime CreatedAt,
    CategoryDto? Category,
    IEnumerable<string> PictureUrls,
    IEnumerable<ReviewDto> Reviews,
    IEnumerable<Attribute> Attributes,
    IEnumerable<ProductAttributeDto> ProductAttributeDtos);