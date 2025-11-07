namespace PlugPlay.Services.Products;

public record ProductSearchRequest
{
    public string Query { get; set; }

    public int Page { get; set; }

    public int PageSize { get; set; }
}