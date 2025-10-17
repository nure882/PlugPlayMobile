using PlugPlay.Domain.Entities;
using PlugPlay.Shared.Dto;

namespace PlugPlay.Services.Interfaces
{
    public interface IUserInfoService
    {
       Task<User> GetUserInfoByIdAsync(int id);

       Task<bool> UpdateUserAsync(int userId, UserInfoDto dto); 
    }
}
