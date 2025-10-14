using PlugPlay.Domain.Entities;

namespace PlugPlay.Services.Interfaces
{
    public interface IUserInfoService
    {
       Task<User> GetUserInfoByIdAsync(int id);
    }
}
