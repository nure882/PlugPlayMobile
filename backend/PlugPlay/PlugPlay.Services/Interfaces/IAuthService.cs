using Google.Apis.Auth;
using PlugPlay.Domain.Common;
using PlugPlay.Domain.Entities;
using PlugPlay.Services.Auth;

namespace PlugPlay.Services.Interfaces;

public interface IAuthService
{
    Task<Result<User>> ValidateUserCredentials(string email, string password);

    Task<(string token, string refreshToken)> GenerateTokens(User user);

    Task<Result<GoogleJsonWebSignature.Payload>> ValidateGoogleSignInRequestAsync(string idToken);

    Task<Result> RegisterAsync(User user, string password, string phoneNumber, string firstName, string lastName);

    Task<Result<RefreshTokenResponse>> RefreshTokenAsync(string token);

    Task<Result> LogoutAsync(string token);

    Task<Result<User>> GetUserAsync(int userId);

    Task<Result<User>> GetOrCreateUser(
        string payloadEmail, string payloadGivenName, string familyName, string payloadSubject);
}
