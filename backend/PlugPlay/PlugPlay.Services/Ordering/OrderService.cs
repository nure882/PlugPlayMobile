using System.Transactions;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Logging;
using PlugPlay.Domain.Common;
using PlugPlay.Domain.Entities;
using PlugPlay.Domain.Enums;
using PlugPlay.Infrastructure;
using PlugPlay.Services.Interfaces;

namespace PlugPlay.Services.Ordering;

public class OrderService : IOrderService
{
    public async Task<Result<Order>> PlaceOrderAsync(PlaceOrderRequest order)
    {
        throw new NotImplementedException();
    private static readonly EventId GetUserOrdersEvent = new(2007, nameof(GetUserOrdersAsync));

    private static readonly EventId GetOrderItemsEvent = new(2008, nameof(GetOrderItemsAsync));

    private static readonly EventId GetOrderByIdEvent = new(2009, nameof(GetOrderAsync));

    private readonly PlugPlayDbContext _context;

    private readonly IPaymentService _paymentService;

    private readonly ILogger<OrderService> _logger;

    public OrderService(PlugPlayDbContext context, IPaymentService paymentService, ILogger<OrderService> logger)
    {
        _context = context;
        _paymentService = paymentService;
        _logger = logger;
    }
    }

    public async Task<Result<IEnumerable<Order>>> GetUserOrdersAsync(int userId)
    {
        try
        {
            var user = await _context.Users.FindAsync(userId);
            if (user is null)
            {
                return Result.Fail<IEnumerable<Order>>($"No user with id {userId}");
            }

            var orders = await _context.Orders
                .Where(o => o.UserId == userId)
                .Include(o => o.User)
                .Include(o => o.DeliveryAddress)
                .Include(o => o.OrderItems)
                .ThenInclude(oi => oi.Product)
                .ThenInclude(p => p.ProductAttributes)
                .Include(o => o.OrderItems)
                .ThenInclude(oi => oi.Product)
                .ThenInclude(p => p.ProductImages)
                .Include(o => o.OrderItems)
                .ThenInclude(oi => oi.Product)
                .ThenInclude(p => p.Category)
                .Include(o => o.OrderItems)
                .ThenInclude(oi => oi.Product)
                // .ThenInclude(p => p.Reviews)
                // .ThenInclude(r => r.User)
                .ToListAsync();

            return Result.Success<IEnumerable<Order>>(orders);
        }
        catch (Exception e)
        {
            return Result.Fail<IEnumerable<Order>>(
                $"Exception thrown retrieving orders of user {userId}. Message: {e.Message}");
        }
    }

    public async Task<Result<Order>> GetOrderAsync(int orderId)
    {
        try
        {
            var order = await _context.Orders
                .Include(o => o.User)
                .Include(o => o.DeliveryAddress)
                .Include(o => o.OrderItems)
                .ThenInclude(oi => oi.Product)
                .ThenInclude(p => p.ProductAttributes)
                .Include(o => o.OrderItems)
                .ThenInclude(oi => oi.Product)
                .ThenInclude(p => p.ProductImages)
                .Include(o => o.OrderItems)
                .ThenInclude(oi => oi.Product)
                .ThenInclude(p => p.Category)
                .Include(o => o.OrderItems)
                .ThenInclude(oi => oi.Product)
                // .ThenInclude(p => p.Reviews)
                // .ThenInclude(r => r.User)
                .FirstOrDefaultAsync(o => o.Id == orderId);

            if (order == null)
            {
                var warnOrderNotFound = LoggerMessage.Define<int>(
                    LogLevel.Warning,
                    GetOrderByIdEvent,
                    "Order with ID {orderId} not found.");
                warnOrderNotFound(_logger, orderId, null);

                return Result.Fail<Order>($"Order with ID {orderId} not found.");
            }

            return Result.Success(order);
        }
        catch (Exception e)
        {
            return Result.Fail<Order>(
                $"Exception thrown retrieving order with ID {orderId}. Message: {e.Message}");
        }
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
        try
        {
            var order = await _context.Orders.FindAsync(orderId);
            if (order == null)
            {
                return Result.Fail<IEnumerable<OrderItem>>($"No order with id {orderId}");
            }

            var items = await _context.OrderItems
                .Where(oi => oi.OrderId == orderId)
                .Include(oi => oi.Product)
                .ThenInclude(p => p.ProductAttributes)
                .Include(oi => oi.Product)
                .ThenInclude(p => p.ProductImages)
                .Include(oi => oi.Product)
                .ThenInclude(p => p.Category)
                // .Include(oi => oi.Product)
                // .ThenInclude(p => p.Reviews)
                // .ThenInclude(r => r.User)
                .ToListAsync();

            return Result.Success<IEnumerable<OrderItem>>(items);
        }
        catch (Exception e)
        {
            return Result.Fail<IEnumerable<OrderItem>>(
                $"Exception thrown retrieving order items for order with id {orderId}. Message: {e.Message}");
        }
    }
    }
}
