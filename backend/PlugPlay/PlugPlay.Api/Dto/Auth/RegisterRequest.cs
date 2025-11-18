using PlugPlay.Domain.Entities;
using System.ComponentModel.DataAnnotations;

namespace PlugPlay.Api.Dto.Auth;

public class RegisterRequest
{
    [Required]
    public string Email { get; set; }

    [Required]
    public string Password { get; set; }

    [Required]
    public string FirstName { get; set; }

    [Required]
    public string LastName { get; set; }

    public string PhoneNumber { get; set; }

    public static User MapUser(RegisterRequest request)
    {
        return new User
        {
            Email = request.Email,
            FirstName = request.FirstName,
            LastName = request.LastName,
            PhoneNumber = request.PhoneNumber
        };
    }
}
