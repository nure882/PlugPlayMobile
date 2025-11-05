namespace PlugPlay.Api.Dto.Product;

public record CategoryDto(int Id, string Name, CategoryDto? Parent = null);
