using System.Text.Json.Serialization;

namespace PlugPlay.Domain.Entities;

public class CartItem
{
    public int Id { get; set; }

    public decimal Total { get; set; }

    public int Quantity { get; set; }

    public int ProductId { get; set; }

    public int UserId { get; set; }

    [JsonIgnore]
    public Product Product { get; set; }

    [JsonIgnore]
    public User User { get; set; }
}
