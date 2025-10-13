using Microsoft.AspNetCore.Mvc;
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

    [HttpGet]
    public async Task<IActionResult> GetAllProducts()
    {
        var products = await _productsService.GetAllProductsAsync();

        return Ok(products);
    }

    [HttpGet]
    public async Task<IActionResult> GetProductById(int id)
    {
        var product = await _productsService.GetProductByIdAsync(id);

        if (product == null)
            throw new KeyNotFoundException($"Product with ID {id} not found.");

        return Ok(product);
    }
}