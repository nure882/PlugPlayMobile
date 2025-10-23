using PlugPlay.Domain.Common;
using PlugPlay.Domain.Entities;
using PlugPlay.Services.Dto;

namespace PlugPlay.Services.Interfaces
{
    public interface IUserInfoService
    {
        Task<User> GetUserInfoByIdAsync(int id);

        Task<bool> UpdateUserAsync(int userId, UserInfoDto dto);

        Task<Result<User>> GetUserByTokenAsync(string token);
    }
}
