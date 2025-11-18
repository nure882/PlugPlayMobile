using PlugPlay.Domain.Entities;
using PlugPlay.Domain.Enums;
using PlugPlay.Services.Ordering;

namespace PlugPlay.Api.Dto.Ordering;

public class OrderDto
{
    public int Id { get; set; }

    public int? UserId { get; set; }

    public DateTime OrderDate { get; set; }

    public OrderStatus Status { get; set; }

    public decimal TotalAmount { get; set; }

    public decimal? DiscountAmount { get; set; }

    public DeliveryMethod DeliveryMethod { get; set; }

    public PaymentMethod PaymentMethod { get; set; }

    public int? DeliveryAddressId { get; set; }

    public PaymentStatus PaymentStatus { get; set; }

    public long TransactionId { get; set; }

    public DateTime? PaymentCreated { get; set; }

    public DateTime? PaymentProcessed { get; set; }

    public string PaymentFailureReason { get; set; }

    public DateTime UpdatedAt { get; set; }

    // public UserDto User { get; set; }

    public UserAddressDto DeliveryAddress { get; set; }

    public List<OrderItemDto> OrderItems { get; set; }

    public static OrderDto MapOrder(Order order)
    {
        return new OrderDto
        {
            Id = order.Id,
            UserId = order.UserId,
            OrderDate = order.OrderDate,
            Status = order.Status,
            TotalAmount = order.TotalAmount,
            DiscountAmount = order.DiscountAmount,
            DeliveryMethod = order.DeliveryMethod,
            PaymentMethod = order.PaymentMethod,
            DeliveryAddressId = order.DeliveryAddressId,
            PaymentStatus = order.PaymentStatus,
            TransactionId = order.TransactionId,
            PaymentCreated = order.PaymentCreated,
            PaymentProcessed = order.PaymentProcessed,
            PaymentFailureReason = order.PaymentFailureReason,
            UpdatedAt = order.UpdatedAt,
            // User = order.User != null ? UserDto.MapUser(order.User) : null,
            DeliveryAddress = order.DeliveryAddress != null ? UserAddressDto.MapAddress(order.DeliveryAddress) : null,
            OrderItems = order.OrderItems?.Select(OrderItemDto.MapOrderItem).ToList() ?? new List<OrderItemDto>()
        };
    }
}
