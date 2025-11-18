using Attribute = PlugPlay.Domain.Entities.Attribute;

namespace PlugPlay.Api.Dto.Product;

public record AttributeDto
{
    public int Id { get; set; }

    public string Name { get; set; }

    public string Unit { get; set; }

    public string DataType { get; set; }

    public IEnumerable<ProductAttributeDto> ProductAttributeDtos { get; set; }

    public static AttributeDto MapAttribute(Attribute attribute)
    {
        var pas = new List<ProductAttributeDto>();
        foreach (var pa in attribute.ProductAttributes)
        {
            var paDto = new ProductAttributeDto
            {
                Id = pa.Id,
                AttributeId = pa.AttributeId,
                ProductId = pa.ProductId,
            };
            var value = pa.GetTypedValue();
            if (value.Item2 == typeof(string))
            {
                paDto.StrValue = value.Item1;
            }
            else
            {
                paDto.NumValue = value.Item1;
            }

            pas.Add(paDto);
        }

        return new AttributeDto
        {
            Id = attribute.Id,
            Name = attribute.Name,
            Unit = attribute.Unit,
            DataType = attribute.DataType,
            ProductAttributeDtos = pas
        };
    }
}
