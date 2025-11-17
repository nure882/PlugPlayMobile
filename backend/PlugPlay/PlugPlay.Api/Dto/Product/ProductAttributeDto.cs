using PlugPlay.Domain.Entities;

namespace PlugPlay.Api.Dto.Product;

public class ProductAttributeDto
{
    public int Id { get; set; }

    public int AttributeId { get; set; }

    public int ProductId { get; set; }

    public string StrValue { get; set; }

    public double? NumValue { get; set; }

    public static ProductAttributeDto MapProductAttribute(ProductAttribute pa)
    {
        var dto = new ProductAttributeDto
        {
            Id = pa.Id,
            AttributeId = pa.AttributeId,
            ProductId = pa.ProductId
        };
        var value = pa.GetTypedValue();
        if (value.Item2 == typeof(string))
        {
            dto.StrValue = value.Item1;
        }
        else
        {
            dto.NumValue = value.Item1;
        }

        return dto;
    }
}
