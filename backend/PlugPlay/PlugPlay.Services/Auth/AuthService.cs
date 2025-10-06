using Google.Apis.Auth;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Identity;
using Microsoft.Extensions.Configuration;
using PlugPlay.Domain.Common;
using PlugPlay.Domain.Entities;
using PlugPlay.Domain.Enums;
using PlugPlay.Infrastructure;
using PlugPlay.Services.Interfaces;

namespace PlugPlay.Services.Auth;

public class AuthService : IAuthService
{
    private readonly PlugPlayDbContext _context;

    private readonly IJwtService _jwtService;

    private readonly UserManager<User> _userManager;

    private readonly IConfiguration _configuration;

    private readonly IHttpContextAccessor _httpContextAccessor;

    public AuthService(PlugPlayDbContext context,
        UserManager<User> userManager,
        IJwtService jwtService,
        IHttpContextAccessor httpContextAccessor,
        IConfiguration configuration)
    {
        _context = context;
        _userManager = userManager;
        _jwtService = jwtService;
        _httpContextAccessor = httpContextAccessor;
        _configuration = configuration;
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

    public async Task<(string token, string refreshToken)> GenerateTokens(User user)
    {
        var token = _jwtService.GenerateToken(user);
        var refreshToken = _jwtService.GenerateRefreshToken();

        var userRefreshToken = new UserRefreshToken
        {
            UserId = user.Id,
            Token = refreshToken,
            Expires = DateTime.UtcNow.AddDays(Convert.ToDouble(_configuration["Jwt:RefreshTokenExpirationDays"])),
            CreatedAt = DateTime.UtcNow,
            CreatedByIp = GetIpAddress()
        };

        _context.UserRefreshTokens.Add(userRefreshToken);

        var oldTokens = _context.UserRefreshTokens
            .Where(rt => rt.UserId == user.Id && rt.Expires < DateTime.UtcNow);
        _context.UserRefreshTokens.RemoveRange(oldTokens);

        await _context.SaveChangesAsync();

        return new ValueTuple<string, string>(token, refreshToken);
    }

   public async Task<Result<GoogleJsonWebSignature.Payload>> ValidateGoogleSignInRequestAsync(string idToken)
    {
        GoogleJsonWebSignature.Payload payload;
        try
        {
            payload = await GoogleJsonWebSignature.ValidateAsync(idToken,
                new GoogleJsonWebSignature.ValidationSettings
                {
                    Audience = new[] { _configuration["GoogleAuth:ClientId"] }
                });
        }
        catch (Exception ex)
        {
            return Result.Fail<GoogleJsonWebSignature.Payload>("Validation error");
        }

        if (payload == null)
        {
            return Result.Fail<GoogleJsonWebSignature.Payload>("Invalid Google token payload");
        }

        var issuer = payload.Issuer;
        if (issuer != "accounts.google.com" && issuer != "https://accounts.google.com")
        {
            return Result.Fail<GoogleJsonWebSignature.Payload>("Invalid token issuer");
        }

        if (payload.EmailVerified == null || payload.EmailVerified == false)
        {
            return Result.Fail<GoogleJsonWebSignature.Payload>("Email not verified by Google");
        }

        return Result.Success(payload);
    }

    public async Task<Result<User>> GetOrCreateUser(string payloadEmail, string payloadName, string payloadSubject)
    {
        if (string.IsNullOrEmpty(payloadSubject) || string.IsNullOrEmpty(payloadEmail))
        {
            return Result.Fail<User>("Invalid Google payload");
        }

        var user = await _userManager.FindByLoginAsync("Google", payloadSubject);
        if (user != null)
        {
            return Result.Success(user);
        }

        user = await _userManager.FindByEmailAsync(payloadEmail);
        if (user != null)
        {
            var fixResult = await FixLoginInUserManager(payloadSubject, user);

            return fixResult.Failure ? Result.Fail<User>(fixResult.Error) : Result.Success(user);
        }

        var userCreateResult = await CreateNewUser(payloadEmail, payloadName, payloadSubject);
        if (userCreateResult.Failure)
        {
            return Result.Fail<User>(userCreateResult.Error);
        }

        var login = new UserLoginInfo("Google", payloadSubject, "Google");
        var addLogin = await _userManager.AddLoginAsync(userCreateResult.Value, login);
        if (!addLogin.Succeeded)
        {
            return Result.Fail<User>(
                string.Join(", ", addLogin.Errors.Select(e => e.Description)));
        }

        return Result.Success(userCreateResult.Value);
    }


    private async Task<Result<User>> CreateNewUser(string payloadEmail, string payloadName, string payloadSubject)
    {
        var newUser1 = new User
        {
            GoogleId = payloadSubject,
            Email = payloadEmail,
            UserName = payloadEmail,
            FirstName = payloadName,
            LastName = "",
            PhoneNumber = "",
            EmailConfirmed = true,
            CreatedAt = DateTime.UtcNow,
            Role = Role.User
        };

        var createResult = await _userManager.CreateAsync(newUser1);
        if (!createResult.Succeeded)
        {
            return (Result.Fail<User>(
                string.Join(", ", createResult.Errors.Select(e => e.Description))));
        }

        return Result.Success(newUser1);
    }

    private async Task<Result> FixLoginInUserManager(string payloadSubject, User user)
    {
        var loginInfo = new UserLoginInfo("Google", payloadSubject, "Google");
        var addLoginResult = await _userManager.AddLoginAsync(user, loginInfo);
        if (!addLoginResult.Succeeded && addLoginResult.Errors.Any())
        {
            var nonDuplicateErrors = addLoginResult.Errors
                .Where(e =>
                {
                    var desc = e.Description ?? string.Empty;
                    return !(desc.Contains("already", StringComparison.OrdinalIgnoreCase)
                             || desc.Contains("exists", StringComparison.OrdinalIgnoreCase)
                             || desc.Contains("associated", StringComparison.OrdinalIgnoreCase));
                })
                .ToList();

            if (nonDuplicateErrors.Any())
            {
                return Result.Fail(string.Join(", ", nonDuplicateErrors.Select(e => e.Description)));
            }
        }

        if (!user.EmailConfirmed)
        {
            user.EmailConfirmed = true;
            await _userManager.UpdateAsync(user);
        }

        user.GoogleId ??= payloadSubject;
        await _userManager.UpdateAsync(user);

        return Result.Success();
    }

    private string GetIpAddress()
    {
        var context = _httpContextAccessor.HttpContext;
        if (context == null)
        {
            return null;
        }

        if (context.Request.Headers.ContainsKey("X-Forwarded-For"))
        {
            return context.Request.Headers["X-Forwarded-For"].ToString();
        }

        return context.Connection.RemoteIpAddress?.MapToIPv4().ToString() ?? "0";
    }
}
