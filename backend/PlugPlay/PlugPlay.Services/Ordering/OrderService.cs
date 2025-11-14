using PlugPlay.Domain.Common;
using PlugPlay.Domain.Entities;
using PlugPlay.Services.Interfaces;

namespace PlugPlay.Services.Ordering;

public class OrderService : IOrderService
{
    public async Task<Result<Order>> PlaceOrderAsync(PlaceOrderRequest order)
    {
        throw new NotImplementedException();
    }

    public async Task<Result<IEnumerable<Order>>> GetUserOrdersAsync(int userId)
    {
        throw new NotImplementedException();
    }

    public async Task<Result<Order>> GetOrderAsync(int orderId)
    {
        throw new NotImplementedException();
    }

    public async Task<Result> UpdateOrderAsync(Order order)
    {
        throw new NotImplementedException();
    }

    public async Task<Result> CancelOrderAsync(int orderId)
    {
        throw new NotImplementedException();
    }

    public async Task<Result<IEnumerable<OrderItem>>> GetOrderItemsAsync(int orderId)
    {
        throw new NotImplementedException();
    }
}
