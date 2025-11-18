namespace PlugPlay.Services.Payment;

public class LiqPayResponse
{
    public string status { get; set; }

    public string order_id { get; set; }
    
    public decimal amount { get; set; }
    
    public string currency { get; set; }
    
    public long transaction_id { get; set; }

    public override string ToString()
    {
        return $"Status: {status}, OrderId: {order_id}, Amount: {amount}, Currency: {currency}, TransactionId: {transaction_id}";
    }
}
