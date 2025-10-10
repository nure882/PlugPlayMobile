using PlugPlay.Domain.Entities;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Identity;
using Microsoft.Extensions.Configuration;
using PlugPlay.Domain.Common;
using PlugPlay.Domain.Enums;
using PlugPlay.Infrastructure;
using PlugPlay.Services.Interfaces;

namespace PlugPlay.Services.Interfaces
{
    public interface IProductService
    {
        Task<IEnumerable<Product>> GetAllProductsAsync();
    }
}
