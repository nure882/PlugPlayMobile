namespace PlugPlay.Domain.Entities;

public class ProductAttribute
{
    public int AttributeId { get; set; }

    public int ProductId { get; set; }

    public string Value { get; set; }

    public Product Product { get; set; }

    public Attribute Attribute { get; set; }
}
