using PlugPlay.Domain.Entities;

namespace PlugPlay.Api.Dto;

public class UserDto
{
    public  int Id { get; set; }
    
    public string Email { get; set; }
    
    public string FirstName { get; set; }
    
    public string LastName { get; set; }

    public static UserDto MapUser(User user)
    {
        return new UserDto
        {
            Id = user.Id,
            FirstName = user.FirstName,
            LastName = user.LastName,
        };
    }
}
