namespace PlugPlay.Domain.Entities;

public class Product
{
    public int Id { get; set; }

    public int CategoryId { get; set; }

    public string Name { get; set; }

    public string Description { get; set; }

    public decimal Price { get; set; }

    public int StockQuantity { get; set; }

    public DateTime CreatedAt { get; set; }

    public Category Category { get; set; }

    public ICollection<ProductAttribute> ProductAttributes { get; set; } = new List<ProductAttribute>();

    public ICollection<ProductImage> ProductImages { get; set; } = new List<ProductImage>();

    public ICollection<Review> Reviews { get; set; } = new List<Review>();

    public ICollection<OrderItem> OrderItems { get; set; } = new List<OrderItem>();
}
