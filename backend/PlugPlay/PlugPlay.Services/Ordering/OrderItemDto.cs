using PlugPlay.Domain.Entities;

namespace PlugPlay.Services.Ordering;

public class OrderItemDto
{
    public int Id { get; set; }

    public int ProductId { get; set; }

    public int Quantity { get; set; }

    public static OrderItemDto MapOrderItem(OrderItem item)
    {
        return new OrderItemDto
        {
            Id = item.Id,
            ProductId = item.ProductId,
            Quantity = item.Quantity,
            // Product = item.Product != null ? ProductDto.MapProduct(item.Product) : null
        };
    }
}
