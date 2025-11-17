using System.Diagnostics;
using System.Text;
using System.Text.Json;
using Microsoft.Extensions.Configuration;
using SHA3.Net;

namespace PlugPlay.Services.Payment;

public class LiqPayHelper
{
    private readonly string _publicKey;

    private readonly string _privateKey;

    public LiqPayHelper(IConfiguration configuration)
    {
        _publicKey = configuration["LiqPay:PublicKey"];
        _privateKey = configuration["LiqPay:PrivateKey"];
    }

    public LiqPayPaymentData GeneratePaymentData(decimal amount, string currency, string description, int orderId)
    {
        var paymentParams = new
        {
            public_key = _publicKey,
            version = 7,
            action = "pay",
            amount = amount.ToString(),
            currency,
            description,
            order_id = orderId.ToString(),
        };

        var jsonParams = JsonSerializer.Serialize(paymentParams);
        Debug.WriteLine("JSON:");
        Debug.WriteLine(jsonParams);
        Debug.WriteLine("---------------------------------");
        var data = Convert.ToBase64String(Encoding.UTF8.GetBytes(jsonParams));
        var signature = GenerateSignature(data);

        return new LiqPayPaymentData
        {
            Data = data,
            Signature = signature
        };
    }

    public bool VerifyCallback(string data, string signature)
    {
        var expectedSignature = GenerateSignature(data);

        return signature == expectedSignature;
    }

    public T DecodeData<T>(string data)
    {
        var json = Encoding.UTF8.GetString(Convert.FromBase64String(data));

        return JsonSerializer.Deserialize<T>(json);
    }

    private string GenerateSignature(string data)
    {
        var signString = _privateKey + data + _privateKey;
        using var sha3 = Sha3.Sha3256();
        var hash = sha3.ComputeHash(Encoding.UTF8.GetBytes(signString));
        
        return Convert.ToBase64String(hash);
    }
}
