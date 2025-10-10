using Google.Apis.Auth;
using PlugPlay.Domain.Common;
using PlugPlay.Domain.Entities;

namespace PlugPlay.Services.Interfaces;

public interface IAuthService
{
    Task<Result> RegisterAsync(User user, string password, string phoneNumber, string firstName, string lastName);

    Task<(string token, string refreshToken)> GenerateTokens(User user);

    Task<Result<GoogleJsonWebSignature.Payload>> ValidateGoogleSignInRequestAsync(string idToken);

    Task<Result<User>> ValidateUserCredentials(string email, string password);

    Task<Result<User>> GetOrCreateUser(string payloadEmail, string payloadName, string payloadSubject);

    Task<Result> LogoutAsync(string token);
}
