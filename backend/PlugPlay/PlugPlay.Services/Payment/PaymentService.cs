using PlugPlay.Domain.Common;
using PlugPlay.Domain.Enums;
using PlugPlay.Infrastructure;
using PlugPlay.Services.Interfaces;

namespace PlugPlay.Services.Payment;

public class PaymentService : IPaymentService
{
    private readonly PlugPlayDbContext _context;

    public PaymentService(PlugPlayDbContext context)
    {
        _context = context;
    }

    public async Task<Result> UpdatePaymentStatus(int orderId, long transactionId, string status)
    {
        throw new NotImplementedException();

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
