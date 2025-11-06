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
            "price-asc" => q => q.OrderBy(p => p.Price),
            "price-desc" => q => q.OrderByDescending(p => p.Price),
            "newest" => q => q.OrderByDescending(p => p.CreatedAt),
            _ => q =>
            {
                if (sort == "price_asc")
                {
                    return q.OrderBy(p => p.Price);
                }

                if (sort == "price_desc")
                {
                    return q.OrderByDescending(p => p.Price);
                }

                return q.OrderBy(p => p.Id);
            }
        };

        return orderBy;
    }

    public async static Task<ExpressionStarter<Product>> BuildPredicate(
        string filter, Category category, decimal? minPrice = null, decimal? maxPrice = null)
    {
        var predicate = PredicateBuilder.New<Product>(true);
        predicate = AddCategories();
        predicate = AddPriceRange();

        if (string.IsNullOrEmpty(filter))
        {
            return predicate;
        }

        var decodedFilter = System.Net.WebUtility.UrlDecode(filter)?.Trim();
        if (string.IsNullOrEmpty(decodedFilter))
        {
            return predicate;
        }

        predicate = AddParsedProductAttributesFilters();

        return predicate;

        ExpressionStarter<Product> AddCategories()
        {
            if (category.Id == int.MaxValue)
            {
            }
            else if (category.ParentCategoryId.HasValue)
            {
                predicate = predicate.And(p => p.CategoryId == category.Id);
            }
            else
            {
                if (category.SubCategories.Any())
                {
                    var catPred = PredicateBuilder.New<Product>(false);
                    foreach (var sc in category.SubCategories)
                    {
                        catPred = catPred.Or(p => p.CategoryId == sc.Id);
                    }
                    predicate = predicate.And(catPred);
                }
            }

            return predicate;
        }

        ExpressionStarter<Product> AddPriceRange()
        {
            if (minPrice.HasValue)
            {
                predicate = predicate.And(p => p.Price >= minPrice.Value);
            }

            if (maxPrice.HasValue)
            {
                predicate = predicate.And(p => p.Price <= maxPrice.Value);
            }

            return predicate;
        }

        ExpressionStarter<Product> AddParsedProductAttributesFilters()
        {
            var filterParts = decodedFilter.Split(',');
            foreach (var part in filterParts)
            {
                var kvp = part.Split(':');
                if (kvp.Length != 2)
                {
                    continue;
                }

                var key = kvp[0].Trim();
                var valueStr = kvp[1].Trim();
                int attrId;
                if (!int.TryParse(key, out attrId))
                {
                    continue;
                }

                var values = valueStr
                    .Split(',')
                    .Select(v => v.Trim())
                    .Where(v => !string.IsNullOrEmpty(v))
                    .ToList();

                if (values.Count == 0)
                {
                    continue;
                }

                var allNumeric = values.All(v => double.TryParse(v,
                    System.Globalization.NumberStyles.Any,
                    System.Globalization.CultureInfo.InvariantCulture,
                    out _));

                var attrFilter = allNumeric
                    ? AddNumericAttribute(attrId, values)
                    : AddStringAttribute(attrId, values);
                predicate = predicate.And(attrFilter);
            }

            return predicate;
        }
    }

    private static Expression<Func<Product, bool>> BuildAttributePredicate(int attrId, IEnumerable<string> values)
    {
        var pParam = Expression.Parameter(typeof(Product), "p");
        var avParam = Expression.Parameter(typeof(ProductAttribute), "av");

        var attrProp = Expression.Property(avParam, nameof(ProductAttribute.AttributeId));
        var attrConst = Expression.Constant(attrId);
        var attrEqual = Expression.Equal(attrProp, attrConst);

        var valueProp = Expression.Property(avParam, nameof(ProductAttribute.Value));
        Expression orChain = null;
        foreach (var val in values)
        {
            var valConst = Expression.Constant(val);
            var eq = Expression.Equal(valueProp, valConst);
            orChain = orChain == null ? eq : Expression.OrElse(orChain, eq);
        }

        var innerCondition = orChain ?? Expression.Constant(false);
        var innerAnd = Expression.AndAlso(attrEqual, innerCondition);
        var innerLambda = Expression.Lambda<Func<ProductAttribute, bool>>(innerAnd, avParam);

        var avProp = Expression.Property(pParam, nameof(Product.ProductAttributes));
        var anyMethod = typeof(Enumerable).GetMethods()
            .First(m => m.Name == "Any" && m.GetParameters().Length == 2)
            .MakeGenericMethod(typeof(ProductAttribute));
        var anyCall = Expression.Call(anyMethod, avProp, innerLambda);

        return Expression.Lambda<Func<Product, bool>>(anyCall, pParam);
    }

    private static Expression<Func<Product, bool>> AddNumericAttribute(int attrId, List<string> values)
    {
        var normalizedValues = new HashSet<string>();
        foreach (var val in values)
        {
            if (double.TryParse(val, System.Globalization.NumberStyles.Any,
                    System.Globalization.CultureInfo.InvariantCulture, out double numericVal))
            {
                normalizedValues.Add(val);
                normalizedValues.Add(numericVal.ToString(System.Globalization.CultureInfo.InvariantCulture));
                if (numericVal == Math.Floor(numericVal))
                {
                    normalizedValues.Add(((int)numericVal).ToString());
                }
            }
            else
            {
                normalizedValues.Add(val);
            }
        }

        return BuildAttributePredicate(attrId, normalizedValues);
    }

    private static Expression<Func<Product, bool>> AddStringAttribute(int attrId, List<string> values)
    {
        return BuildAttributePredicate(attrId, values);
    }
}
