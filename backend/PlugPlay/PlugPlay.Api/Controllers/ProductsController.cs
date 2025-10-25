using System.Net;
using CloudinaryDotNet;
using CloudinaryDotNet.Actions;
using Microsoft.AspNetCore.Mvc;
using PlugPlay.Api.Dto;
using PlugPlay.Domain.Entities;
using PlugPlay.Domain.Extensions;
using PlugPlay.Services.Interfaces;

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
        var productDtos = products.Select(pd => new ProductDto(
            pd.Id,
            pd.Name,
            pd.Description,
            pd.Price,
            pd.StockQuantity,
            pd.CreatedAt,
            MapCategory(pd.Category),
            pd.ProductImages.FirstOrDefault()?.ImageUrl
        ));

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

        var productDtos = result.Value.Select(pd => new ProductDto(
            pd.Id,
            pd.Name,
            pd.Description,
            pd.Price,
            pd.StockQuantity,
            pd.CreatedAt,
            MapCategory(pd.Category),
            pd.ProductImages.FirstOrDefault()?.ImageUrl
        ));

        return Ok(productDtos);
    }

    [HttpGet("{id:int}")]
    public async Task<IActionResult> GetProductById(int id)
    {
        _logger.LogInformation("Getting product by ID: {ProductId}", id);

        try
        {
            var product = await _productsService.GetProductByIdAsync(id);
            var productDto = new ProductDto(
                product.Id,
                product.Name,
                product.Description,
                product.Price,
                product.StockQuantity,
                product.CreatedAt,
                MapCategory(product.Category),
                product.ProductImages.FirstOrDefault()?.ImageUrl
            );

            _logger.LogInformation("Successfully retrieved product with ID: {ProductId}", id);

            return Ok(productDto);
        }
        catch (KeyNotFoundException ex)
        {
            _logger.LogWarning(ex, "Product with ID {ProductId} not found", id);

            return NotFound(new { message = ex.Message });
        }
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

    #endregion
}