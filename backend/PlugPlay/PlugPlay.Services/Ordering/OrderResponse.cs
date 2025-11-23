using PlugPlay.Services.Payment;

namespace PlugPlay.Services.Ordering;

public class OrderResponse
{
    public int OrderId { get; set; }

    public LiqPayPaymentData PaymentData { get; set; }
}
