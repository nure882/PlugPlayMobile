using System.Text.Json.Serialization;

namespace PlugPlay.Domain.Entities;

public class Attribute
{
    public int Id { get; set; }

    public string Name { get; set; }

    public string Unit { get; set; }

    public string DataType { get; set; }

    [JsonIgnore]
    public ICollection<ProductAttribute> ProductAttributes { get; set; } = new List<ProductAttribute>();
}
