namespace PlugPlay.Api.Dto.Order;

public class CreatePaymentRequest
{
    public decimal Amount { get; set; }

    public string Currency { get; set; }

    public string Description { get; set; }

    public int PaymentId { get; set; }
}
