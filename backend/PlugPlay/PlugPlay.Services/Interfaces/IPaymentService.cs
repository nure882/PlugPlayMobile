using PlugPlay.Domain.Common;

namespace PlugPlay.Services.Interfaces;

public interface IPaymentService
{
    Task<Result> UpdatePaymentStatus(int orderId, long transactionId, string status);
}
