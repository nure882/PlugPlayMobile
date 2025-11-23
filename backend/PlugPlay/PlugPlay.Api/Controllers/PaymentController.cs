using Microsoft.AspNetCore.Mvc;
using PlugPlay.Services.Interfaces;
using PlugPlay.Services.Payment;

namespace PlugPlay.Api.Controllers;

[Route("api/[controller]")]
[ApiController]
public class PaymentController : ControllerBase
{
    private readonly LiqPayHelper _liqPayHelper;

    private readonly IPaymentService _paymentService;

    public PaymentController(IPaymentService paymentService, LiqPayHelper liqPayHelper)
    {
        _paymentService = paymentService;
        _liqPayHelper = liqPayHelper;
    }

    [HttpPost("callback")]
    [Consumes("application/x-www-form-urlencoded")]
    public async Task<IActionResult> PaymentCallback([FromForm] LiqPayCallback callback)
    {
        try
        {
            if (!_liqPayHelper.VerifyCallback(callback.data, callback.signature))
            {
                return BadRequest("Invalid signature");
            }

            var response = _liqPayHelper.DecodeData<LiqPayResponse>(callback.data);
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
