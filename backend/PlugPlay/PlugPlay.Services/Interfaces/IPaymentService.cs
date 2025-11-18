using PlugPlay.Domain.Common;
using PlugPlay.Services.Payment;

namespace PlugPlay.Services.Interfaces;

public interface IPaymentService
{
    Task<Result> UpdatePaymentStatus(int orderId, long transactionId, string status);

    Task<Result<LiqPayPaymentData>> CreatePayment(int orderId, decimal totalWithDelivery);

    Task<Result> RefundPayment(int orderId);
}
