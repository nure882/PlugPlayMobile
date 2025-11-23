using System.Text.Json.Serialization;

namespace PlugPlay.Services.Payment;

public class LiqPayRefundResponse
{
    [JsonPropertyName("result")]
    public string Result { get; set; }

    [JsonPropertyName("action")]
    public string Action { get; set; }

    [JsonPropertyName("status")]
    public string Status { get; set; }

    [JsonPropertyName("order_id")]
    public string OrderId { get; set; }
}
