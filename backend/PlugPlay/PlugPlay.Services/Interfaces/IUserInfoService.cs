using PlugPlay.Domain.Entities;
using PlugPlay.Services.Dto;

namespace PlugPlay.Services.Interfaces
{
    public interface IUserInfoService
    {
       Task<User> GetUserInfoByIdAsync(int id);

       Task<bool> UpdateUserAsync(int userId, UserInfoDto dto); 
    }
}
