using Microsoft.AspNetCore.Identity;
using PlugPlay.Services.Interfaces;
using PlugPlay.Domain.Entities;
using Microsoft.EntityFrameworkCore;
using PlugPlay.Shared.Dto;
using PlugPlay.Infrastructure;

namespace PlugPlay.Services.DataRetrieval
{
    public class UserInfoService :IUserInfoService
    {
        private readonly UserManager<User> _userManager;

        private readonly PlugPlayDbContext _context;

        public UserInfoService(UserManager<User> userManager, PlugPlayDbContext context)
        {
            _userManager = userManager;
            _context = context;
        }

        public async Task<User> GetUserInfoByIdAsync(int id)
        {
            var userInfo = await _userManager.Users
                .Include(u => u.UserAddresses)
                .FirstOrDefaultAsync(u => u.Id == id);

            return userInfo;
        }

        public async Task<bool> UpdateUserAsync(int id, UserInfoDto dto)
        {
            var user = await _userManager.Users
                .Include(u => u.UserAddresses)
                .FirstOrDefaultAsync(u => u.Id == id);

            if (user == null)
                return false;

            user.FirstName = dto.FirstName ?? user.FirstName;
            user.LastName = dto.LastName ?? user.LastName;
            user.Email = dto.Email ?? user.Email;

            var dtoAddressIds = dto.Addresses.Where(a => a.Id.HasValue).Select(a => a.Id!.Value).ToList();
            var addressesToRemove = user.UserAddresses.Where(a => !dtoAddressIds.Contains(a.Id)).ToList();
            foreach (var addr in addressesToRemove)
            {
                _context.UserAddresses.Remove(addr);
            }

            foreach (var addrDto in dto.Addresses)
            {
                if (addrDto.Id.HasValue)
                {
                    var existing = user.UserAddresses.First(a => a.Id == addrDto.Id);

                    existing.House = addrDto.House;
                    existing.Apartments = addrDto.Apartments;
                    existing.Street = addrDto.Street;
                    existing.City = addrDto.City;
                }
                else
                {
                    user.UserAddresses.Add(new UserAddress
                    {
                        House = addrDto.House,
                        Apartments = addrDto.Apartments,
                        Street = addrDto.Street,
                        City = addrDto.City
                    });
                }
            }

            var identityResult = await _userManager.UpdateAsync(user);

            if (!identityResult.Succeeded)
                throw new Exception("Failed to update user info.");

            await _context.SaveChangesAsync();

            return true;
        }
    }
}