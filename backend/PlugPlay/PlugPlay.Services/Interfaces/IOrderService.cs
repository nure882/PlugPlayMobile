using PlugPlay.Domain.Common;
using PlugPlay.Domain.Entities;
using PlugPlay.Services.Ordering;

namespace PlugPlay.Services.Interfaces;

public interface IOrderService
{
    Task<Result<OrderResponse>> PlaceOrderAsync(PlaceOrderRequest orderReq);

    Task<Result<IEnumerable<Order>>> GetUserOrdersAsync(int userId);

    Task<Result<Order>> GetOrderAsync(int orderId);

    Task<Result<IEnumerable<OrderItem>>> GetOrderItemsAsync(int orderId);

    Task<Result> UpdateOrderAsync(Order order);

    Task<Result> CancelOrderAsync(int orderId);
}
