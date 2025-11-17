using PlugPlay.Domain.Common;
using PlugPlay.Domain.Enums;
using PlugPlay.Infrastructure;
using PlugPlay.Services.Interfaces;

namespace PlugPlay.Services.Payment;

public class PaymentService : IPaymentService
{
    private readonly PlugPlayDbContext _context;

    private readonly LiqPayHelper _liqPayHelper;

    public PaymentService(PlugPlayDbContext context, LiqPayHelper liqPayHelper)
    {
        _context = context;
        _liqPayHelper = liqPayHelper;
    }

    public async Task<Result<LiqPayPaymentData>> CreatePayment(int orderId, decimal total)
    {
        try
        {
            var paymentData = _liqPayHelper.GeneratePaymentData(
                total,
                "UAH",
                "",
                orderId
            );

            return Result.Success(paymentData);
        }
        catch (Exception ex)
        {
            return Result.Fail<LiqPayPaymentData>($"Failure creating payment: {ex.Message}");
        }
    }

    public Task<Result> RefundPayment(int orderId)
    {
        throw new NotImplementedException();
    }

    public async Task<Result> UpdatePaymentStatus(int orderId, long transactionId, string status)
    {
        var order = await _context.Orders.FindAsync(orderId);
        if (order is not null)
        {
            switch (status)
            {
                case "TestPaid":
                    order.PaymentStatus = PaymentStatus.TestPaid;
                    break;
                case "Paid":
                    order.PaymentStatus = PaymentStatus.Paid;
                    break;
                case "Failed":
                    order.PaymentStatus = PaymentStatus.Failed;
                    break;
                default:
                    order.PaymentStatus = order.PaymentStatus;
                    break;
            }

            order.TransactionId = transactionId;
            order.PaymentProcessed = DateTime.UtcNow;

            await _context.SaveChangesAsync();

            return Result.Success();
        }

        return Result.Fail($"No payment for {orderId} is in db");
    }
}
