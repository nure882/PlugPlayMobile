using System.Security.Claims;
using PlugPlay.Domain.Entities;

namespace PlugPlay.Services.Interfaces;

public interface IJwtService
{
    string GenerateToken(User user);

    string GenerateRefreshToken();

    ClaimsPrincipal GetPrincipalFromExpiredToken(string token);
}
