using Microsoft.AspNetCore.Mvc;
using PlugPlay.Api.Dto.Ordering;
using PlugPlay.Domain.Extensions;
using PlugPlay.Services.Interfaces;
using PlugPlay.Services.Ordering;

namespace PlugPlay.Api.Controllers;

[Route("api/[controller]")]
[ApiController]
public class OrderController : ControllerBase
{
    private static readonly EventId PlaceOrderEvent = new(2002, nameof(PlaceOrder));

    private static readonly EventId GetUserOrdersEvent = new(2003, nameof(GetUserOrders));

    private static readonly EventId GetOrderItemsEvent = new(2004, nameof(GetOrderItems));

    private static readonly EventId GetOrderByIdEvent = new(2005, nameof(GetOrderById));

    private static readonly EventId CancelOrderEvent = new(2006, nameof(CancelOrderAsync));

    private readonly IOrderService _orderService;

    private readonly ILogger<OrderController> _logger;


    public OrderController(IOrderService orderService, ILogger<OrderController> logger)
    {
        _orderService = orderService;
        _logger = logger;
    }

    [HttpPost("")]
    public async Task<IActionResult> PlaceOrder(PlaceOrderRequest orderRequest)
    {
        _logger.Log(LogLevel.Information, "Start order placement");

        var placeOrderRes = await _orderService.PlaceOrderAsync(orderRequest);

        placeOrderRes
            .OnSuccess(() =>
            {
                var paymentRequired = placeOrderRes.Value?.PaymentData != null;
                var infoOrderPlaced = LoggerMessage.Define<int, bool>(
                    LogLevel.Information,
                    PlaceOrderEvent,
                    "Order placed by user with id {userId}. PaymentRequired = {paymentRequired}");
                infoOrderPlaced(_logger, orderRequest.UserId, paymentRequired, null);
            })
            .OnFailure(() =>
            {
                var failurePlacingOrder = LoggerMessage.Define<int, string>(
                    LogLevel.Error,
                    PlaceOrderEvent,
                    "Error placing order for user with id {userId}. Error: {error}");
                failurePlacingOrder(_logger, orderRequest.UserId, placeOrderRes.Error, null);
            });

        return placeOrderRes.Failure
            ? StatusCode(StatusCodes.Status400BadRequest, placeOrderRes.Error)
            : StatusCode(201, placeOrderRes.Value);
    }

    [HttpGet("user/{userId:int}")]
    public async Task<IActionResult> GetUserOrders(int userId)
    {
        if (userId < 1) return StatusCode(StatusCodes.Status400BadRequest, "Id is less than 1");

        var infoStartGettingUserOrders = LoggerMessage.Define<int>(
            LogLevel.Information,
            GetUserOrdersEvent,
            "Retrieving orders of user with id {userId}.");
        infoStartGettingUserOrders(_logger, userId, null);

        var userOrdersRes = await _orderService.GetUserOrdersAsync(userId);

        userOrdersRes
            .OnSuccess(() =>
            {
                var orderCount = userOrdersRes.Value.Count();
                var infoRetrievedUserOrders = LoggerMessage.Define<int, int>(
                    LogLevel.Information,
                    GetUserOrdersEvent,
                    "Retrieved orders of user with id {userId}. Count = {orderCount}");
                infoRetrievedUserOrders(_logger, userId, orderCount, null);
            })
            .OnFailure(() =>
            {
                var failureRetrievingUserOrders = LoggerMessage.Define<int, string>(
                    LogLevel.Error,
                    GetUserOrdersEvent,
                    "Error retrieving orders of user with id {userId}. Error: {error}");
                failureRetrievingUserOrders(_logger, userId, userOrdersRes.Error, null);
            });

        if (userOrdersRes.Failure)
        {
            return StatusCode(404, userOrdersRes.Error);
        }

        List<OrderDto> orderDtos = [];
        foreach (var order in userOrdersRes.Value)
        {
            orderDtos.Add(OrderDto.MapOrder(order));
        }

        return StatusCode(200, orderDtos);
    }

    [HttpGet("{orderId:int}/order_items")]
    public async Task<IActionResult> GetOrderItems(int orderId)
    {
        if (orderId < 1) return StatusCode(StatusCodes.Status400BadRequest, "Id is less than 1");

        var infoStartGettingOrderItems = LoggerMessage.Define<int>(
            LogLevel.Information,
            GetOrderItemsEvent,
            "Retrieving order items for order with id {orderId}.");
        infoStartGettingOrderItems(_logger, orderId, null);

        var orderItemsRes = await _orderService.GetOrderItemsAsync(orderId);

        orderItemsRes
            .OnSuccess(() =>
            {
                var orderItemsCount = orderItemsRes.Value.Count();
                var infoRetrievedOrderItems = LoggerMessage.Define<int, int>(
                    LogLevel.Information,
                    GetOrderItemsEvent,
                    "Retrieved order items of order with id {orderId}. Count = {orderItemsCount}");
                infoRetrievedOrderItems(_logger, orderId, orderItemsCount, null);
            })
            .OnFailure(() =>
            {
                var failureRetrievingOrderItems = LoggerMessage.Define<int, string>(
                    LogLevel.Error,
                    GetOrderItemsEvent,
                    "Error retrieving order items for order with id {orderId}. Error: {error}");
                failureRetrievingOrderItems(_logger, orderId, orderItemsRes.Error, null);
            });


        if (orderItemsRes.Failure)
        {
            return StatusCode(404, orderItemsRes.Error);
        }

        List<OrderItemDto> orderItemDtos = [];
        foreach (var order in orderItemsRes.Value)
        {
            orderItemDtos.Add(OrderItemDto.MapOrderItem(order));
        }

        return StatusCode(200, orderItemDtos);
    }

    [HttpGet("{orderId:int}")]
    public async Task<IActionResult> GetOrderById(int orderId)
    {
        if (orderId < 1) return StatusCode(StatusCodes.Status400BadRequest, "Id is less than 1");

        var infoStartGettingOrder = LoggerMessage.Define<int>(
            LogLevel.Information,
            GetOrderByIdEvent,
            "Retrieving order with id {orderId}.");
        infoStartGettingOrder(_logger, orderId, null);

        var orderRes = await _orderService.GetOrderAsync(orderId);

        orderRes
            .OnSuccess(() =>
            {
                var infoRetrievedOrder = LoggerMessage.Define<int>(
                    LogLevel.Information,
                    GetOrderByIdEvent,
                    "Retrieved order with id {orderId}.");
                infoRetrievedOrder(_logger, orderId, null);
            })
            .OnFailure(() =>
            {
                var failureRetrievingOrder = LoggerMessage.Define<int, string>(
                    LogLevel.Error,
                    GetOrderByIdEvent,
                    "Error retrieving order with id {orderId}. Error: {error}");
                failureRetrievingOrder(_logger, orderId, orderRes.Error, null);
            });

        if (orderRes.Failure)
        {
            return StatusCode(404, orderRes.Error);
        }

        var orderDto = OrderDto.MapOrder(orderRes.Value);

        return StatusCode(200, orderDto);
    }

    [HttpPut("cancel/{orderId:int}")]
    public async Task<IActionResult> CancelOrderAsync(int orderId)
    {
        if (orderId < 1)
        {
            return StatusCode(StatusCodes.Status400BadRequest, "Id is less than 1");
        }

        var infoStartOrderCancellation = LoggerMessage.Define<int>(
            LogLevel.Information,
            CancelOrderEvent,
            "Init cancellation for order with id {orderId}.");
        infoStartOrderCancellation(_logger, orderId, null);

        var cancelRes = await _orderService.CancelOrderAsync(orderId);

        cancelRes
            .OnSuccess(() =>
            {
                var infoCancelledSuccessfully = LoggerMessage.Define<int>(
                    LogLevel.Information,
                    CancelOrderEvent,
                    "Cancelled order with id {orderId}");
                infoCancelledSuccessfully(_logger, orderId, null);
            })
            .OnFailure(() =>
            {
                var failureCancellingOrder = LoggerMessage.Define<int, string>(
                    LogLevel.Error,
                    CancelOrderEvent,
                    "Error cancelling order with id {orderId}. Error: {error}");
                failureCancellingOrder(_logger, orderId, cancelRes.Error, null);
            });

        return cancelRes.Failure ? StatusCode(500, cancelRes.Error) : StatusCode(200);
    }
}
