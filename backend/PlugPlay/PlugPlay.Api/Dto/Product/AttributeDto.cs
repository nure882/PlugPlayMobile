namespace PlugPlay.Api.Dto.Product;

public record AttributeDto(
    int Id,
    string Name,
    string Unit,
    string DataType,
    IEnumerable<ProductAttributeDto> ProductAttributeDtos);
