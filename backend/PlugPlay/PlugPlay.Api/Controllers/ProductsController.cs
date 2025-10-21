using Microsoft.AspNetCore.Mvc;
using PlugPlay.Api.Dto;
using PlugPlay.Domain.Entities;
using PlugPlay.Services.Interfaces;

namespace PlugPlay.Api.Controllers;

[Route("api/[controller]")]
[ApiController]
public class ProductsController : ControllerBase
{
    private readonly IProductsService _productsService;

    public ProductsController(IProductsService productsService)
    {
        _productsService = productsService;
    }

    [HttpGet("all")]
    public async Task<IActionResult> GetAllProducts()
    {
        var products = await _productsService.GetAllProductsAsync();
        var productDtos = products.Select(p => new ProductDto(
            p.Id,
            p.Name,
            p.Description,
            p.Price,
            p.StockQuantity,
            p.CreatedAt,
            MapCategory(p.Category)
        ));


        return Ok(productDtos);
    }

    [HttpGet("{id:int}")]
    public async Task<IActionResult> GetProductById(int id)
    {
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
                MapCategory(product.Category)
            );

            return Ok(productDto);
        }
        catch (KeyNotFoundException ex)
        {
            return NotFound(new { message = ex.Message });
        }
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
}
