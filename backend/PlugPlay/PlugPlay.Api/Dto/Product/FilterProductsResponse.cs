namespace PlugPlay.Api.Dto.Product;

public class FilterProductsResponse
{
    public IEnumerable<ProductDto> Products { get; set; }

    public int Total { get; set; }

    public int TotalPages { get; set; }

    public int Page { get; set; }

    public int PageSize { get; set; }
}
