using Microsoft.AspNetCore.Identity;
using PlugPlay.Domain.Common;
using PlugPlay.Domain.Entities;
using PlugPlay.Services.Interfaces;

namespace PlugPlay.Services.Auth;

public class AuthService : IAuthService
{
    private readonly UserManager<User> _userManager;

    public AuthService(UserManager<User> userManager)
    {
        _userManager = userManager;
    }

    public async Task<Result> RegisterAsync(User user, string password,
        string phoneNumber, string firstName, string lastName)
    {
        user.PhoneNumber = phoneNumber ?? "";
        user.UserName = user.Email;
        user.FirstName = firstName;
        user.LastName = lastName;

        var result = await _userManager.CreateAsync(user, password);
        if (!result.Succeeded)
        {
            return Result.Fail($"Failed to create a user: {string.Join(", ",
                result.Errors.Select(e => e.Description))}");
        }

        var roleResult = await _userManager.AddToRoleAsync(user, user.Role.ToString());
        if (!roleResult.Succeeded)
        {
            return Result.Fail(
                $"Failed to assign role: {string.Join(", ",
                    roleResult.Errors.Select(e => e.Description))}");
        }

        return Result.Success();
    }
}
