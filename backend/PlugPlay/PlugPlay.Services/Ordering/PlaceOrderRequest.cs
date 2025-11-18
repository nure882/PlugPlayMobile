using PlugPlay.Domain.Enums;

namespace PlugPlay.Services.Ordering;

public class PlaceOrderRequest
{
    public int UserId { get; set; }

    public DeliveryMethod DeliveryMethod { get; set; }

    public PaymentMethod PaymentMethod { get; set; }

    public int? DeliveryAddressId { get; set; }

    public IEnumerable<OrderItemDto> OrderItems { get; set; }
}
