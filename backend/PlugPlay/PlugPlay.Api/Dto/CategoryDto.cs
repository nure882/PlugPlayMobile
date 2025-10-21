namespace PlugPlay.Api.Dto;

public record CategoryDto(int Id, string Name, CategoryDto? Parent = null);
