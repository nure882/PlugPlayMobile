using Attribute = PlugPlay.Domain.Entities.Attribute;

namespace PlugPlay.Api.Dto.Product;

public record ProductDto
{
    public int Id { get; set; }

    public string Name { get; set; }

    public string Description { get; set; }

    public decimal Price { get; set; }

    public int StockQuantity { get; set; }

    public DateTime CreatedAt { get; set; }

    public CategoryDto? Category { get; set; }

    public IEnumerable<string> PictureUrls { get; set; }

    public IEnumerable<ReviewDto> Reviews { get; set; }

    public IEnumerable<Attribute> Attributes { get; set; }

    public IEnumerable<ProductAttributeDto> ProductAttributeDtos { get; set; }

    public static ProductDto MapProduct(Domain.Entities.Product product)
    {
        return new ProductDto
        {
            Id = product.Id,
            Name = product.Name,
            Description = product.Description,
            Price = product.Price,
            StockQuantity = product.StockQuantity,
            CreatedAt = product.CreatedAt,
            Category = CategoryDto.MapCategory(product.Category),
            PictureUrls = product.ProductImages.Select(pi => pi.ImageUrl),
            Reviews = product.Reviews.Select(ReviewDto.MapReview),
            Attributes = product.ProductAttributes.Select(pa => pa.Attribute),
            ProductAttributeDtos = product.ProductAttributes.Select(ProductAttributeDto.MapProductAttribute)
        };
    }
}
