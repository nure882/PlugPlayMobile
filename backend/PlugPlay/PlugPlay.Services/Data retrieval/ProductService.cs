using System;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Identity;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Configuration;
using PlugPlay.Domain.Common;
using PlugPlay.Domain.Entities;
using PlugPlay.Domain.Enums;
using PlugPlay.Infrastructure;
using PlugPlay.Services.Interfaces;

namespace PlugPlay.Services.Data_retrieval
{
    public class ProductService : IProductService
    {
        private readonly PlugPlayDbContext _context;

        public ProductService(PlugPlayDbContext context) 
        {
            _context = context;
        }

        public async Task<IEnumerable<Product>> GetAllProductsAsync()
        {
            return await _context.Products
             .Include(p => p.ProductAttributes)
             .ThenInclude(pa => pa.Attribute)
             .Include(p => p.ProductImages)
             .Include(p => p.Category)
             .Include(p => p.Reviews)
             .ToListAsync();
        }
    }
}
