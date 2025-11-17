using System.Net;
using CloudinaryDotNet;
using CloudinaryDotNet.Actions;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Query;
using PlugPlay.Api.Dto;
using PlugPlay.Api.Dto.Product;
using PlugPlay.Domain.Common;
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

        var productsRetrieved = LoggerMessage.Define<int>(
            LogLevel.Information,
            new EventId(2001, "ProductsRetrieved"),
            "Successfully retrieved {Count} products");

        productsRetrieved(_logger, products.Count(), null);

        return Ok(productDtos);
    }

    [HttpGet("available")]
    public async Task<IActionResult> GetAllAvailableProducts()
    {
        _logger.LogInformation("Getting all products");

        var result = await _productsService.GetAvailableProductsAsync();
        result.OnSuccess(() =>
        {
            var productsRetrievedResult = LoggerMessage.Define<int>(
                LogLevel.Information,
                new EventId(2002, "ProductsRetrievedResult"),
                "Successfully retrieved {Count} products");

            productsRetrievedResult(_logger, result.Value.Count(), null);
        });

        var productDtos = result.Value.Select(MapProduct).ToList();

        return Ok(productDtos);
    }

    [HttpPost("attribute/{categoryId:int}")]
    public async Task<IActionResult> GetAttributes(int categoryId, [FromBody] int[] productIds = null)
    {
        if (categoryId < 1)
        {
            return BadRequest("Invalid categroyId");
        }

        var result = await _productsService.GetCategoryAttributesAsync(categoryId, productIds ?? new int[] {});
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
        Result<Category> categoryResult = Result.Success(new Category() {Id = categoryId});
        if (categoryId != int.MaxValue)  //  2147483647
        {
            categoryResult = await _productsService.GetCategoryAsync(categoryId);
            if (categoryResult.Failure)
            {
                return BadRequest("Invalid categoryId");
            }
        }

        var predicate = AttributeHelper.BuildPredicate(
            filter, categoryResult.Value, minPrice, maxPrice);
        var includes = new List<Func<IQueryable<Product>, IIncludableQueryable<Product, object>>>
        {
            q => q
                .Include(p => p.ProductAttributes).ThenInclude(av => av.Attribute)
                .Include(p => p.Category).ThenInclude(c => c.ParentCategory)
                .Include(p => p.ProductImages)
                .Include(p => p.Reviews)
                .ThenInclude(r => r.User)
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
        var gettingProductById = LoggerMessage.Define<int>(
            LogLevel.Information,
            new EventId(2000, "GettingProductById"),
            "Getting product by ID: {ProductId}");

        gettingProductById(_logger, id, null);

        var result = await _productsService.GetProductByIdAsync(id);

        result.OnSuccess(() =>
        {
            var productRetrieved = LoggerMessage.Define<int>(
                LogLevel.Information,
                new EventId(2001, "ProductRetrieved"),
                "Successfully retrieved product with ID: {ProductId}");

            productRetrieved(_logger, id, null);
        });

        result.OnFailure(() =>
        {
            var productNotFound = LoggerMessage.Define<int>(
                LogLevel.Warning,
                new EventId(2002, "ProductNotFound"),
                "Product with ID {ProductId} not found");

            productNotFound(_logger, id, null);
        });

        if(result.Failure)
        {
            return NotFound(result.Error);
        }

        var product = result.Value;
        var productDto = MapProduct(product);

        return Ok(productDto);
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
            var invalidPagingParameters = LoggerMessage.Define<int, int>(
                LogLevel.Warning,
                new EventId(200, "InvalidPagingParameters"),
                "Invalid paging parameters: page={Page}, pageSize={PageSize}");

            invalidPagingParameters(_logger, page, pageSize, null);

            return StatusCode(400, "page and pageSize must be positive integers");
        }

        var result = await _productsService.SearchProductsAsync(new ProductSearchRequest
            { Query = query, Page = page, PageSize = pageSize });
        result.OnSuccess(() =>
        {
            var productsFound = LoggerMessage.Define<int>(
                LogLevel.Information,
                new EventId(2001, "ProductsFound"),
                "Successfully found {Count} products");

            productsFound(_logger, result.Value.Count(), null);
        });
       
        result.OnFailure(() =>
        _logger.LogError($"{result.Error}"));

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

        if (uploadResult.StatusCode != HttpStatusCode.OK)
        {
            return BadRequest(new ProblemDetails() { Title = "Upload failed." });
        }

        var result = await _productsService.AddImageAsync(
            productId, uploadResult.Url.AbsoluteUri);
        result.OnFailure(() =>
        {
            var failedToAddProductImage = LoggerMessage.Define<int>(
                LogLevel.Error,
                new EventId(2001, "FailedToAddProductImage"),
                "Failed to add image for product {ProductId}");

            failedToAddProductImage(_logger, productId, null);
        });

        result.OnSuccess(() =>
        {
            var productImageAdded = LoggerMessage.Define<int>(
                LogLevel.Information,
                new EventId(2002, "ProductImageAdded"),
                "Added image for product {ProductId}");

            productImageAdded(_logger, productId, null);
        });

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
            MapUser(review.User),
            review.CreatedAt,
            review.UpdatedAt);
    }

    private UserDto MapUser(Domain.Entities.User user)
    {
        return new UserDto
        {
            Id = user.Id,
            FirstName = user.FirstName,
            LastName = user.LastName,
        };
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
