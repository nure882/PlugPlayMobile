using System.Net;
using CloudinaryDotNet;
using CloudinaryDotNet.Actions;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Query;
using PlugPlay.Api.Dto;
using PlugPlay.Api.Dto.Product;
using PlugPlay.Domain.Entities;
using PlugPlay.Domain.Extensions;
using PlugPlay.Services.Interfaces;
using PlugPlay.Services.Products;
using Attribute = PlugPlay.Domain.Entities.Attribute;

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

    [HttpGet("attribute/{categoryId:int}")]
    public async Task<IActionResult> GetAttributes(int categoryId)
    {
        if (categoryId < 1)
        {
            return BadRequest("Invalid categroyId");
        }

        var result = await _productsService.GetCategoryAttributesAsync(categoryId);
        result.OnFailure(() =>
        {
            var failed = LoggerMessage.Define<int, string>(LogLevel.Error,
                new EventId(2001, $"{nameof(GetAttributes)}Failed"),
                @"Failed to retrieve attributes for category {CategoryId}. Error: {Error}");
            failed(_logger, categoryId, result.Error, null);
        });
        result.OnSuccess(() =>
        {
            var success = LoggerMessage.Define<int>(LogLevel.Information,
                new EventId(2002, $"{nameof(GetAttributes)}Success"),
                @"Successfully retrieved attributes for category {CategoryId}");
            success(_logger, categoryId, null);
        });

        if (result.Failure)
        {
            return BadRequest($"Error: {result.Error}");
        }

        var attributeDtos = result.Value.Select(MapAttribute);

        return Ok(attributeDtos);
    }

    [HttpGet("filter/{categoryId:int}")]
    public async Task<IActionResult> FilterProducts(
        int categoryId,
        [FromQuery] decimal? minPrice = null,
        [FromQuery] decimal? maxPrice = null,
        [FromQuery] string filter = null,
        [FromQuery] string sort = null,
        [FromQuery] int page = 1,
        [FromQuery] int pageSize = 20)
    {
        var categoryResult = await _productsService.GetCategoryAsync(categoryId);
        if (categoryResult.Failure)
        {
            return BadRequest("Invalid categoryId");
        }

        var predicate = await AttributeHelper.BuildPredicate(filter, categoryResult.Value, minPrice, maxPrice);
        var includes = new List<Func<IQueryable<Product>, IIncludableQueryable<Product, object>>>
        {
            q => q
                .Include(p => p.ProductAttributes).ThenInclude(av => av.Attribute)
                .Include(p => p.Category).ThenInclude(c => c.ParentCategory)
        };
        var orderBy = AttributeHelper.BuildOrderByDelegate(sort);
        var skipCount = (page - 1) * pageSize;
        int? takeCount = pageSize;

        var result = await _productsService.FilterProductsAsync(new FilterProductsRequest
        {
            Predicate = predicate,
            Includes = includes,
            OrderBy = orderBy,
            SkipCount = skipCount,
            TakeCount = takeCount
        });
        result.OnFailure(() =>
        {
            var failed = LoggerMessage.Define<int, string, string, int, int, string>(LogLevel.Error,
                new EventId(2001, "FilterProductsFailed"),
                @"Failed to filter products for category {CategoryId} with filter '{Filter}', 
                            sort '{Sort}', page {Page}, pageSize {PageSize}. Error: {Error}");

            failed(_logger, categoryId, filter, sort, page, pageSize, result.Error, null);
        });
        result.OnSuccess(() =>
        {
            var success = LoggerMessage.Define<int, string, string, int, int>(LogLevel.Information,
                new EventId(2002, "FilterProductsSuccess"),
                @"Successfully filtered products for category {CategoryId} with filter '{Filter}', 
                            sort '{Sort}', page {Page}, pageSize {PageSize}");

            success(_logger, categoryId, filter, sort, page, pageSize, null);
        });
        if (result.Failure)
        {
            return BadRequest(result.Error);
        }

        var productDtos = result.Value.Select(MapProduct);
        var total = result.Value.Count();
        var totalPages = (int)Math.Ceiling(total / (double)pageSize);

        return Ok(new FilterProductsResponse
            { Products = productDtos, Total = total, TotalPages = totalPages, Page = page, PageSize = pageSize });
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

    [Authorize(Roles = "Admin")]
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

        if (uploadResult.StatusCode != HttpStatusCode.OK)
        {
            return BadRequest(new ProblemDetails() { Title = "Upload failed." });
        }

        var result = await _productsService.AddImageAsync(
            productId, uploadResult.Url.AbsoluteUri);
        result.OnFailure(() =>
                _logger.LogError("Failed to add image for product {ProductId}", productId))
            .OnSuccess(() =>
                _logger.LogInformation("Added image for product {ProductId}", productId));

        return Ok();
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
            product.ProductAttributes.Select(pa => pa.Attribute),
            product.ProductAttributes.Select(MapProductAttribute)
        );
    }

    private ProductAttributeDto MapProductAttribute(ProductAttribute pa)
    {
        var dto = new ProductAttributeDto
        {
            Id = pa.Id,
            AttributeId = pa.AttributeId,
            ProductId = pa.ProductId
        };
        var value = pa.GetTypedValue();
        if (value.Item2 == typeof(string))
        {
            dto.StrValue = value.Item1;
        }
        else
        {
            dto.NumValue = value.Item1;
        }

        return dto;
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

    private AttributeDto MapAttribute(Attribute attribute)
    {
        var pas = new List<ProductAttributeDto>();
        foreach (var pa in attribute.ProductAttributes)
        {
            var paDto = new ProductAttributeDto
            {
                Id = pa.Id,
                AttributeId = pa.AttributeId,
                ProductId = pa.ProductId,
            };
            var value = pa.GetTypedValue();
            if (value.Item2 == typeof(string))
            {
                paDto.StrValue = value.Item1;
            }
            else
            {
                paDto.NumValue = value.Item1;
            }
            pas.Add(paDto);
        }

        return new AttributeDto(
            attribute.Id,
            attribute.Name,
            attribute.Unit,
            attribute.DataType,
            pas
            );
    }

    #endregion
}
