using Microsoft.AspNetCore.Identity;
using PlugPlay.Services.Interfaces;
using PlugPlay.Infrastructure;
using PlugPlay.Domain.Entities;
using Microsoft.EntityFrameworkCore;

namespace PlugPlay.Services.DataRetrieval
{
    public class UserInfoService :IUserInfoService
    {
        private readonly UserManager<User> _userManager;

        public UserInfoService(UserManager<User> userManager)
        {
            _userManager = userManager;
        }

        public async Task<User> GetUserInfoByIdAsync(int id)
        {
            var user = await _userManager.Users
                .Include(u => u.UserAddresses)
                .FirstOrDefaultAsync(u => u.Id == id);

            return user;
        }
    }
}
