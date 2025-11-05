namespace PlugPlay.Api.Dto.Product;

public class ProductAttributeDto
{
    public int Id { get; set; }

    public int AttributeId { get; set; }

    public int ProductId { get; set; }

    public string StrValue { get; set; }

    public double? NumValue { get; set; }
};