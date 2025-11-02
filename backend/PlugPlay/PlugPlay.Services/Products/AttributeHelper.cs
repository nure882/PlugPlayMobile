using System.Linq.Expressions;
using LinqKit;
using PlugPlay.Domain.Entities;

namespace PlugPlay.Services.Products;

public static class AttributeHelper
{
    public static Func<IQueryable<Product>, IOrderedQueryable<Product>> BuildOrderByDelegate(string sort)
    {
        if (string.IsNullOrEmpty(sort))
        {
            return q => q.OrderBy(p => p.Id);
        }

        Func<IQueryable<Product>, IOrderedQueryable<Product>> orderBy = sort switch
        {
            "price_asc" => q => q.OrderBy(p => p.Price),
            "price_desc" => q => q.OrderByDescending(p => p.Price),
            "newest" => q => q.OrderByDescending(p => p.CreatedAt),
            _ => q => q.OrderBy(p => p.Id)
        };

        return orderBy;
    }

    public async static Task<ExpressionStarter<Product>> BuildPredicate(string filter, Category category, decimal? minPrice = null,
        decimal? maxPrice = null)
    {
        var predicate = PredicateBuilder.New<Product>(true);
        if (category.ParentCategoryId.HasValue)
        {
            predicate = predicate.And(p => p.CategoryId == category.Id);
        }
        else
        {
            foreach (var sc in category.SubCategories)
            {
                predicate = predicate.Or(p => p.CategoryId == sc.Id);
            }
        }

        if (minPrice.HasValue)
        {
            predicate = predicate.And(p => p.Price >= minPrice.Value);
        }

        if (maxPrice.HasValue)
        {
            predicate = predicate.And(p => p.Price <= maxPrice.Value);
        }

        if (string.IsNullOrEmpty(filter))
        {
            return predicate;
        }

        var decodedFilter = System.Net.WebUtility.UrlDecode(filter)?.Trim();
        if (string.IsNullOrEmpty(decodedFilter))
        {
            return predicate;
        }

        var filterParts = decodedFilter.Split(';');
        foreach (var part in filterParts)
        {
            var kvp = part.Split(':');
            if (kvp.Length != 2)
            {
                continue;
            }

            var key = kvp[0].Trim();
            var valueStr = kvp[1].Trim();

            if (!int.TryParse(key, out int attrId))
            {
                continue;
            }

            var values = valueStr
                .Split(',')
                .Select(v => v.Trim())
                .Where(v => !string.IsNullOrEmpty(v))
                .ToList();
            if (values.Count != 0)
            {
                var allNumeric = values.All(v => int.TryParse(v, out _));
                var attrFilter = AddAttribute(attrId, values, allNumeric);
                predicate = predicate.And(attrFilter);
            }
        }

        return predicate;
    }

    private static Expression<Func<Product, bool>> AddAttribute(int attrId, List<string> values, bool allNumeric)
    {
        var pParam = Expression.Parameter(typeof(Product), "p");
        var avParam = Expression.Parameter(typeof(ProductAttribute), "av");

        // av.AttributeId == attrId
        var attrProp = Expression.Property(avParam, nameof(ProductAttribute.AttributeId));
        var attrConst = Expression.Constant(attrId);
        var attrEqual = Expression.Equal(attrProp, attrConst);

        Expression innerCondition;
        if (allNumeric)
        {
            // ids.Contains(av.Id)
            var ids = values.Select(int.Parse).ToList();
            var idProp = Expression.Property(avParam, nameof(ProductAttribute.Id));
            var idsConst = Expression.Constant(ids);
            var containsMethod = typeof(Enumerable).GetMethods()
                .First(m => m.Name == "Contains" && m.GetParameters().Length == 2)
                .MakeGenericMethod(typeof(int));
            innerCondition = Expression.Call(containsMethod, idsConst, idProp);
        }
        else
        {
            // av.Value == val1 || av.Value == val2 || ...
            var valueProp = Expression.Property(avParam, nameof(ProductAttribute.Value));
            Expression orChain = null;
            foreach (var val in values)
            {
                var valConst = Expression.Constant(val);
                var eq = Expression.Equal(valueProp, valConst);
                orChain = orChain == null ? eq : Expression.OrElse(orChain, eq);
            }

            innerCondition = orChain ?? Expression.Constant(false);
        }

        var innerAnd = Expression.AndAlso(attrEqual, innerCondition);
        var innerLambda = Expression.Lambda<Func<ProductAttribute, bool>>(innerAnd, avParam);

        // p.ProductAttributes.Any(innerLambda)
        var avProp = Expression.Property(pParam, nameof(Product.ProductAttributes));
        var anyMethod = typeof(Enumerable).GetMethods()
            .First(m => m.Name == "Any" && m.GetParameters().Length == 2)
            .MakeGenericMethod(typeof(ProductAttribute));
        var anyCall = Expression.Call(anyMethod, avProp, innerLambda);

        return Expression.Lambda<Func<Product, bool>>(anyCall, pParam);
    }
}