using LinqKit;
using Microsoft.EntityFrameworkCore.Query;
using PlugPlay.Domain.Entities;

namespace PlugPlay.Services.Products;

public record FilterProductsRequest
{
    public ExpressionStarter<Product> Predicate { get; set; }

    public List<Func<IQueryable<Product>, IIncludableQueryable<Product, object>>> Includes { get; set; }

    public Func<IQueryable<Product>, IOrderedQueryable<Product>> OrderBy { get; set; }

    public int SkipCount { get; set; }

    public int? TakeCount { get; set; }
}
