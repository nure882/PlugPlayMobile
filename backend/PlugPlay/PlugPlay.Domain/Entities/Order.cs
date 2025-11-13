using PlugPlay.Domain.Enums;

namespace PlugPlay.Domain.Entities;

public class Order
{
    public int Id { get; set; }

    public int? UserId { get; set; }

    public DateTime OrderDate { get; set; }

    public OrderStatus Status { get; set; }

    public decimal TotalAmount { get; set; }

    public decimal? DiscountAmount { get; set; }

    public PaymentMethod PaymentMethod { get; set; }

    public string DeliveryAddress { get; set; }

    public PaymentStatus PaymentStatus { get; set; }

    public long TransactionId { get; set; }

    public DateTime? PaymentCreated { get; set; }

    public DateTime? PaymentProcessed { get; set; }

    public string PaymentFailureReason { get; set; }

    public User User { get; set; }

    public ICollection<OrderItem> OrderItems { get; set; } = new List<OrderItem>();
}
