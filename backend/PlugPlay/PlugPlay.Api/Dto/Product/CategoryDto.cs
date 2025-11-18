using PlugPlay.Domain.Entities;

namespace PlugPlay.Api.Dto.Product;

public record CategoryDto
{
    public CategoryDto(int id, string name, CategoryDto categoryDto = null)
    {
        Id = id;
        Name = name;
        Parent = categoryDto;
    }

    public int Id { get; set; }

    public string Name { get; set; }

    public CategoryDto Parent { get; set; } = null;

    public static CategoryDto MapCategory(Category category, int maxDepth = 16)
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
