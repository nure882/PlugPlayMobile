using System.Net;
using CloudinaryDotNet;
using CloudinaryDotNet.Actions;
using Microsoft.AspNetCore.Mvc;
using PlugPlay.Api.Dto;
using PlugPlay.Domain.Entities;
using PlugPlay.Domain.Extensions;
using PlugPlay.Services.Interfaces;
using PlugPlay.Services.Products;

namespace PlugPlay.Api.Controllers;

[Route("api/[controller]")]
[ApiController]
public class ProductsController : ControllerBase
{
    private readonly IProductsService _productsService;

    private readonly ILogger<ProductsController> _logger;

    private readonly Cloudinary _cloudinary;

    public ProductsController(IProductsService productsService, ILogger<ProductsController> logger,
        Cloudinary cloudinary)
    {
        _productsService = productsService;
        _logger = logger;
        _cloudinary = cloudinary;
    }

    [HttpGet("all")]
    public async Task<IActionResult> GetAllProducts()
    {
        _logger.LogInformation("Getting all products");

        var products = await _productsService.GetAllProductsAsync();
        var productDtos = products.Select(MapProduct);

        _logger.LogInformation("Successfully retrieved {Count} products", products.Count());

        return Ok(productDtos);
    }

    [HttpGet("available")]
    public async Task<IActionResult> GetAllAvailableProducts()
    {
        _logger.LogInformation("Getting all products");

        var result = await _productsService.GetAvailableProductsAsync();
        result.OnSuccess(() =>
            _logger.LogInformation("Successfully retrieved {Count} products", result.Value.Count()));

        var productDtos = result.Value.Select(MapProduct);

        return Ok(productDtos);
    }

    [HttpGet("{id:int}")]
    public async Task<IActionResult> GetProductById(int id)
    {
        _logger.LogInformation("Getting product by ID: {ProductId}", id);

        try
        {
            var product = await _productsService.GetProductByIdAsync(id);
            var productDto = MapProduct(product);

            _logger.LogInformation("Successfully retrieved product with ID: {ProductId}", id);

            return Ok(productDto);
        }
        catch (KeyNotFoundException ex)
        {
            _logger.LogWarning(ex, "Product with ID {ProductId} not found", id);

            return NotFound(new { message = ex.Message });
        }
    }

    [HttpGet("search/{query}")]
    public async Task<IActionResult> SearchProducts(
        string query,
        [FromQuery] int page = 1,
        [FromQuery] int pageSize = 20)
    {
        if (string.IsNullOrWhiteSpace(query))
        {
            _logger.LogWarning("No search query provided");

            return StatusCode(400, "No search query provided");
        }

        if (page <= 0 || pageSize <= 0)
        {
            _logger.LogWarning("Invalid paging parameters: page={Page}, pageSize={PageSize}", page, pageSize);

            return StatusCode(400, "page and pageSize must be positive integers");
        }

        var result = await _productsService.SearchProductsAsync(new ProductSearchRequest
            { Query = query, Page = page, PageSize = pageSize });
        result.OnSuccess(()
                => _logger.LogInformation("Successfully found {Count} products", result.Value.Count()))
            .OnFailure(()
                => _logger.LogError($"{result.Error}"));

        if (result.Failure)
        {
            return StatusCode(500, new ProblemDetails { Detail = "Search failed" });
        }

        var productsDtos = result.Value.Select(MapProduct);

        return Ok(productsDtos);
    }

    // todo: [Authorize(Roles = "Admin")]
    [HttpPost("image/{productId:int}")]
    public async Task<IActionResult> UploadImage(int productId, IFormFile file)
    {
        if (file == null || file.Length == 0)
        {
            return BadRequest("No file uploaded.");
        }

        var uploadParams = new ImageUploadParams()
        {
            File = new FileDescription(file.FileName, file.OpenReadStream()),
            PublicId = Guid.NewGuid().ToString(),
            Overwrite = true,
            Folder = "uploads/"
        };

        var uploadResult = await _cloudinary.UploadAsync(uploadParams);

        if (uploadResult.StatusCode == HttpStatusCode.OK)
        {
            var result = await _productsService.AddImageAsync(productId, uploadResult.Url.AbsoluteUri);
            result.OnFailure(() =>
                    _logger.LogError("Failed to add image for product {ProductId}", productId))
                .OnSuccess(() =>
                    _logger.LogInformation("Successfully added image for product {ProductId}", productId));

            return Ok();
        }

        return BadRequest(new ProblemDetails() { Title = "Upload failed." });
    }

    #region Helpers

    private ProductDto MapProduct(Product product)
    {
        return new ProductDto(
            product.Id,
            product.Name,
            product.Description,
            product.Price,
            product.StockQuantity,
            product.CreatedAt,
            MapCategory(product.Category),
            product.ProductImages.Select(pi => pi.ImageUrl),
            product.Reviews.Select(MapReview),
            product.ProductAttributes.Select(pa => MapAttribute(pa.Attribute)));
    }

    private CategoryDto? MapCategory(Category category, int maxDepth = 16)
    {
        return category == null ? null : MapCategoryInternal(category, 0, maxDepth, new HashSet<int>());

        CategoryDto MapCategoryInternal(Category category, int depth, int maxDepth, HashSet<int> seen)
        {
            if (depth >= maxDepth || !seen.Add(category.Id))
                return new CategoryDto(category.Id, category.Name);

            var parent = category.ParentCategory == null
                ? null
                : MapCategoryInternal(category.ParentCategory, depth + 1, maxDepth, seen);

            return new CategoryDto(category.Id, category.Name, parent);
        }
    }

    private ReviewDto MapReview(Review review)
    {
        return new ReviewDto(
            review.Id,
            review.ProductId,
            review.UserId,
            review.Rating,
            review.Comment,
            review.CreatedAt,
            review.UpdatedAt);
    }

    private AttributeDto MapAttribute(Domain.Entities.Attribute attribute)
    {
        return new AttributeDto(
            attribute.Id,
            attribute.Name,
            attribute.Unit,
            attribute.DataType);
    }

    #endregion
}
