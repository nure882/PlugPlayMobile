using Google.Apis.Auth;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Identity;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.Logging;
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

    private readonly ILogger<AuthService> _logger;

    public AuthService(PlugPlayDbContext context,
        UserManager<User> userManager,
        IJwtService jwtService,
        IHttpContextAccessor httpContextAccessor,
        IConfiguration configuration,
        ILogger<AuthService> logger)
    {
        _context = context;
        _userManager = userManager;
        _jwtService = jwtService;
        _httpContextAccessor = httpContextAccessor;
        _configuration = configuration;
        _logger = logger;
    }

    public async Task<Result> RegisterAsync(User user, string password,
        string phoneNumber, string firstName, string lastName)
    {
        _logger.LogInformation("Registering new user with email: {Email}", user.Email);
        
        user.PhoneNumber = phoneNumber ?? "";
        user.UserName = user.Email;
        user.FirstName = firstName;
        user.LastName = lastName;

        var result = await _userManager.CreateAsync(user, password);
        if (!result.Succeeded)
        {
            _logger.LogError("Failed to create user {Email}. Errors: {Errors}", 
                user.Email, string.Join(", ", result.Errors.Select(e => e.Description)));
            
            return Result.Fail($"Failed to create a user: {string.Join(", ",
                result.Errors.Select(e => e.Description))}");
        }

        var roleResult = await _userManager.AddToRoleAsync(user, user.Role.ToString());
        if (!roleResult.Succeeded)
        {
            _logger.LogError("Failed to assign role {Role} to user {Email}. Errors: {Errors}", 
                user.Role, user.Email, string.Join(", ", roleResult.Errors.Select(e => e.Description)));
           
            return Result.Fail(
                $"Failed to assign role: {string.Join(", ",
                    roleResult.Errors.Select(e => e.Description))}");
        }

        _logger.LogInformation("Successfully registered user with email: {Email}", user.Email);
       
        return Result.Success();
    }

    public async Task<Result<User>> ValidateUserCredentials(string email, string password)
    {
        _logger.LogInformation("Validating credentials for user: {Email}", email);
        
        var user = await _context.Users
            .FirstOrDefaultAsync(u => u.Email == email);

        if (user == null || !(await VerifyPasswordAsync(user, password)))
        {
            _logger.LogWarning("Invalid credentials for user: {Email}", email);
            
            return Result.Fail<User>("Invalid email or password");
        }

        _logger.LogInformation("Successfully validated credentials for user: {Email}", email);
       
        return Result.Success(user);
    }

    public async Task<(string token, string refreshToken)> GenerateTokens(User user)
    {
        _logger.LogInformation("Generating tokens for user ID: {UserId}", user.Id);
        
        var token = _jwtService.GenerateToken(user);
        var refreshToken = _jwtService.GenerateRefreshToken();

        var activeTokens = await _context.UserRefreshTokens
            .Where(rt => rt.UserId == user.Id && rt.Expires > DateTime.UtcNow && rt.Revoked == null)
            .OrderBy(rt => rt.CreatedAt)
            .ToListAsync();
            
        if (activeTokens.Count >= 5)
        {
            _logger.LogInformation("Removing oldest refresh token for user ID: {UserId}", user.Id);
            var oldestToken = activeTokens.First();
            _context.UserRefreshTokens.Remove(oldestToken);
        }

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
        _logger.LogInformation("Successfully generated and saved tokens for user ID: {UserId}", user.Id);

        return (token, refreshToken);
    }

    public async Task<Result<GoogleJsonWebSignature.Payload>> ValidateGoogleSignInRequestAsync(string idToken)
    {
        _logger.LogInformation("Validating Google sign-in request");
        
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
            _logger.LogError(ex, "Google token validation failed");
           
            return Result.Fail<GoogleJsonWebSignature.Payload>("Validation error");
        }

        if (payload == null)
        {
            _logger.LogWarning("Google token payload is null");
            
            return Result.Fail<GoogleJsonWebSignature.Payload>("Invalid Google token payload");
        }

        var issuer = payload.Issuer;
        if (issuer != "accounts.google.com" && issuer != "https://accounts.google.com")
        {
            _logger.LogWarning("Invalid Google token issuer: {Issuer}", issuer);
          
            return Result.Fail<GoogleJsonWebSignature.Payload>("Invalid token issuer");
        }

        if (payload.EmailVerified == null || payload.EmailVerified == false)
        {
            _logger.LogWarning("Email not verified by Google for user: {Email}", payload.Email);
           
            return Result.Fail<GoogleJsonWebSignature.Payload>("Email not verified by Google");
        }

        _logger.LogInformation("Successfully validated Google sign-in request for email: {Email}", payload.Email);
       
        return Result.Success(payload);
    }

    public async Task<Result<User>> GetOrCreateUser(string payloadEmail, string payloadName, string payloadSubject)
    {
        _logger.LogInformation("Getting or creating user for Google ID: {GoogleId}", payloadSubject);
        
        if (string.IsNullOrEmpty(payloadSubject) || string.IsNullOrEmpty(payloadEmail))
        {
            _logger.LogWarning("Invalid Google payload: empty subject or email");
           
            return Result.Fail<User>("Invalid Google payload");
        }

        var user = await _userManager.FindByLoginAsync("Google", payloadSubject);
        if (user != null)
        {
            _logger.LogInformation("Found existing user with Google login: {Email}", user.Email);
          
            return Result.Success(user);
        }

        user = await _userManager.FindByEmailAsync(payloadEmail);
        if (user != null)
        {
            _logger.LogInformation("Found existing user by email, fixing Google login: {Email}", payloadEmail);
            var fixResult = await FixLoginInUserManager(payloadSubject, user);
          
            return fixResult.Failure ? Result.Fail<User>(fixResult.Error) : Result.Success(user);
        }

        try
        {
            _logger.LogInformation("Creating new user for Google email: {Email}", payloadEmail);
            var userCreateResult = await CreateNewUser(payloadEmail, payloadName, payloadSubject);
            if (userCreateResult.Failure)
            {
                return Result.Fail<User>(userCreateResult.Error);
            }

            var login = new UserLoginInfo("Google", payloadSubject, "Google");
            var addLogin = await _userManager.AddLoginAsync(userCreateResult.Value, login);
            if (!addLogin.Succeeded)
            {
                _logger.LogError("Failed to add Google login for user: {Email}. Errors: {Errors}", 
                    payloadEmail, string.Join(", ", addLogin.Errors.Select(e => e.Description)));
             
                return Result.Fail<User>(
                    string.Join(", ", addLogin.Errors.Select(e => e.Description)));
            }

            _logger.LogInformation("Successfully created user with Google login: {Email}", payloadEmail);
          
            return Result.Success(userCreateResult.Value);
        }
        catch (DbUpdateException ex)
        {
            _logger.LogError(ex, "Database error while creating user for Google email: {Email}", payloadEmail);
            user = await _userManager.FindByEmailAsync(payloadEmail);
            if (user != null)
            {
                var fixResult = await FixLoginInUserManager(payloadSubject, user);
              
                return fixResult.Failure ? Result.Fail<User>(fixResult.Error) : Result.Success(user);
            }

            return Result.Fail<User>("Failed to create user: " + ex.Message);
        }
    }

    public async Task<Result<RefreshTokenResponse>> RefreshTokenAsync(string token)
    {
        _logger.LogInformation("Refreshing token");
        
        await using var tx = await _context.Database.BeginTransactionAsync();

        var storedRefreshToken = await _context.UserRefreshTokens
            .Include(rt => rt.User)
            .FirstOrDefaultAsync(rt => rt.Token == token && rt.Expires > DateTime.UtcNow);

        if (storedRefreshToken == null)
        {
            _logger.LogWarning("Invalid or expired refresh token provided");
           
            return Result.Fail<RefreshTokenResponse>("Invalid refresh token");
        }

        if (storedRefreshToken.Revoked != null)
        {
            _logger.LogWarning("Token theft detected for user ID: {UserId}", storedRefreshToken.UserId);
            await RevokeTokenChainAsync(storedRefreshToken);
            await tx.CommitAsync();
         
            return Result.Fail<RefreshTokenResponse>("Token theft detected");
        }

        var requestIp = GetIpAddress();
        var newToken = _jwtService.GenerateToken(storedRefreshToken.User);
        var newRefreshToken = _jwtService.GenerateRefreshToken();

        storedRefreshToken.Revoked = DateTime.UtcNow;
        storedRefreshToken.RevokedByIp = requestIp;
        storedRefreshToken.ReplacedByToken = newRefreshToken;

        var userRefreshToken = new UserRefreshToken
        {
            UserId = storedRefreshToken.UserId,
            Token = newRefreshToken,
            Expires = DateTime.UtcNow.AddDays(Convert.ToDouble(_configuration["Jwt:RefreshTokenExpirationDays"])),
            CreatedAt = DateTime.UtcNow,
            CreatedByIp = requestIp
        };
        _context.UserRefreshTokens.Add(userRefreshToken);

        await _context.SaveChangesAsync();
        await tx.CommitAsync();
        
        _logger.LogInformation("Successfully refreshed token for user ID: {UserId}", storedRefreshToken.UserId);

        return Result.Success(new RefreshTokenResponse
        {
            Token = newToken,
            RefreshToken = newRefreshToken,
            Id = storedRefreshToken.User.Id,
            Email = storedRefreshToken.User.Email,
            FirstName = storedRefreshToken.User.FirstName,
            LastName = storedRefreshToken.User.LastName
        });
    }

    private async Task RevokeTokenChainAsync(UserRefreshToken startToken)
    {
        _logger.LogInformation("Revoking token chain starting from token for user ID: {UserId}", startToken.UserId);
        
        if (startToken.Revoked == null)
        {
            startToken.Revoked = DateTime.UtcNow;
            startToken.RevokedByIp = GetIpAddress();
        }

        var nextTokenValue = startToken.ReplacedByToken;
        while (!string.IsNullOrEmpty(nextTokenValue))
        {
            var next = await _context.UserRefreshTokens
                .FirstOrDefaultAsync(rt => rt.Token == nextTokenValue);

            if (next == null)
            {
                break;
            }

            if (next.Revoked == null)
            {
                next.Revoked = DateTime.UtcNow;
                next.RevokedByIp = GetIpAddress();
            }

            nextTokenValue = next.ReplacedByToken;
        }

        await _context.SaveChangesAsync();
        _logger.LogInformation("Successfully revoked token chain for user ID: {UserId}", startToken.UserId);
    }

    public async Task<Result> LogoutAsync(string token)
    {
        _logger.LogInformation("Logging out user with refresh token");
        
        var refreshToken = await _context.UserRefreshTokens
            .FirstOrDefaultAsync(rt => rt.Token == token);

        if (refreshToken != null)
        {
            refreshToken.Revoked = DateTime.UtcNow;
            refreshToken.RevokedByIp = GetIpAddress();
            await _context.SaveChangesAsync();
            _logger.LogInformation("Successfully logged out user ID: {UserId}", refreshToken.UserId);
        }
        else
        {
            _logger.LogWarning("Refresh token not found during logout");
       
            return Result.Fail("Refresh token wasn't found");
        }

        return Result.Success();
    }

    public async Task<Result<User>> GetUserAsync(int userId)
    {
        _logger.LogInformation("Fetching user with ID: {UserId}", userId);
        
        var user = await _context.Users
            .Where(u => u.Id == userId)
            .FirstOrDefaultAsync();
            
        if (user == null)
        {
            _logger.LogWarning("User with ID {UserId} not found", userId);
            
            return Result.Fail<User>("User is not found");
        }

        _logger.LogInformation("Successfully fetched user with ID: {UserId}", userId);
       
        return Result.Success(user);
    }

    private async Task<bool> VerifyPasswordAsync(User user, string password)
        => await _userManager.CheckPasswordAsync(user, password);

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

        return context.Connection.RemoteIpAddress?.MapToIPv4().ToString();
    }

    private async Task<Result<User>> CreateNewUser(string payloadEmail, string payloadName, string payloadSubject)
    {
        _logger.LogInformation("Creating new user with email: {Email}", payloadEmail);
        
        string firstName = "", lastName;
        int i = 0;
        for (;i < payloadName.Length; i++)
        {
            if (payloadName[i] == ' ')
            {
                firstName = payloadName.Substring(0, i + 1);
                break;
            }
        }

        if (i == payloadName.Length)
        {
            firstName = payloadName;
            lastName = "";
        }
        else
        {
            lastName = payloadName.Substring(i + 1);
        }

        var newUser1 = new User
        {
            GoogleId = payloadSubject,
            Email = payloadEmail,
            UserName = payloadEmail,
            FirstName = firstName,
            LastName = lastName,
            PhoneNumber = "",
            EmailConfirmed = true,
            CreatedAt = DateTime.UtcNow,
            Role = Role.User
        };

        var createResult = await _userManager.CreateAsync(newUser1);
        if (!createResult.Succeeded)
        {
            _logger.LogError("Failed to create user {Email}. Errors: {Errors}", 
                payloadEmail, string.Join(", ", createResult.Errors.Select(e => e.Description)));
           
            return (Result.Fail<User>(
                string.Join(", ", createResult.Errors.Select(e => e.Description))));
        }

        _logger.LogInformation("Successfully created user with email: {Email}", payloadEmail);
        
        return Result.Success(newUser1);
    }

    private async Task<Result> FixLoginInUserManager(string payloadSubject, User user)
    {
        _logger.LogInformation("Fixing Google login for user: {Email}", user.Email);
        
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
                _logger.LogError("Failed to add Google login for user {Email}. Errors: {Errors}", 
                    user.Email, string.Join(", ", nonDuplicateErrors.Select(e => e.Description)));
               
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
        
        _logger.LogInformation("Successfully fixed Google login for user: {Email}", user.Email);
       
        return Result.Success();
    }
}