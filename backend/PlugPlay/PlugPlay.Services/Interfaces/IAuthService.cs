using PlugPlay.Domain.Common;
using PlugPlay.Domain.Entities;

namespace PlugPlay.Services.Interfaces;

public interface IAuthService
{
    Task<Result> RegisterAsync(User user, string password, string phoneNumber, string firstName, string lastName);
}
