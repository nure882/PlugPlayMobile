using System.Diagnostics;
using Microsoft.AspNetCore.Mvc;
using PlugPlay.Api.Dto.Order;
using PlugPlay.Services.Interfaces;
using PlugPlay.Services.Payment;

namespace PlugPlay.Api.Controllers;

[ApiController]
[Microsoft.AspNetCore.Components.Route("api/[controller]")]
public class PaymentController : ControllerBase
{
    private readonly LiqPayHelper _liqPayHelper;

    private readonly IPaymentService _paymentService;

    public PaymentController(IConfiguration configuration, IPaymentService paymentService)
    {
        var publicKey = configuration["LiqPay:PublicKey"];
        var privateKey = configuration["LiqPay:PrivateKey"];
        _liqPayHelper = new LiqPayHelper(publicKey, privateKey);
        _paymentService = paymentService;
    }

    [HttpPost("create")]
    public IActionResult CreatePayment([FromBody] CreatePaymentRequest request)
    {
        try
        {
            var paymentData = _liqPayHelper.GeneratePaymentData(
                request.Amount,
                request.Currency ?? "UAH",
                request.Description,
                request.PaymentId
            );

            return Ok(new
            {
                data = paymentData.Data,
                signature = paymentData.Signature
            });
        }
        catch (Exception ex)
        {
            return StatusCode(500, "Error creating payment");
        }
    }

    [HttpPost("callback")]
    [Consumes("application/x-www-form-urlencoded")]
    public async Task<IActionResult> PaymentCallback([FromForm] LiqPayCallback callback)
    {
        try
        {
            Debug.WriteLine("---------------------------------");
            Debug.WriteLine(callback.data);
            Debug.WriteLine(callback.signature);
            if (!_liqPayHelper.VerifyCallback(callback.data, callback.signature))
            {
                return BadRequest("Invalid signature");
            }

            var response = _liqPayHelper.DecodeData<LiqPayResponse>(callback.data);
            Debug.WriteLine(response.ToString());
            var parseResult = int.TryParse(response.order_id, out var paymentId);
            if (parseResult)
            {
                switch (response.status)
                {
                    case "success":
                        await _paymentService.UpdatePaymentStatus(paymentId, response.transaction_id, "Paid");
                        break;
                    case "failure":
                    case "error":
                        await _paymentService.UpdatePaymentStatus(paymentId, response.transaction_id, "Failed");
                        break;
                    case "sandbox":
                        await _paymentService.UpdatePaymentStatus(paymentId, response.transaction_id, "TestPaid");
                        break;
                }
            }
            else
            {
                return StatusCode(500);
            }

            return Ok();
        }
        catch (Exception)
        {
            return StatusCode(500);
        }
    }
}
