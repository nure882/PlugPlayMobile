using Microsoft.AspNetCore.Identity;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Logging;
using PlugPlay.Domain.Common;
using PlugPlay.Domain.Entities;
using PlugPlay.Infrastructure;
using PlugPlay.Services.Dto;
using PlugPlay.Services.Interfaces;

namespace PlugPlay.Services.Profile
{
    public class UserInfoService : IUserInfoService
    {
        private readonly UserManager<User> _userManager;

        private readonly PlugPlayDbContext _context;

        private readonly ILogger<UserInfoService> _logger;

        public UserInfoService(UserManager<User> userManager, PlugPlayDbContext context, ILogger<UserInfoService> logger)
        {
            _userManager = userManager;
            _context = context;
            _logger = logger;
        }

        public async Task<Result<User>> GetUserInfoByIdAsync(int id)
        {
            _logger.LogInformation("Fetching user info for user ID: {UserId}", id);
            
            var userInfo = await _userManager.Users
                .Include(u => u.UserAddresses)
                .FirstOrDefaultAsync(u => u.Id == id);

            if (userInfo == null)
            {
                _logger.LogWarning("User with ID {UserId} not found", id);

                return Result.Fail<User>("User not found"); 
            }

            _logger.LogInformation("Successfully retrieved user info for user ID: {UserId}", id);
            
            return Result.Success(userInfo);
        }

        public async Task<bool> UpdateUserAsync(int id, UserInfoDto dto)
        {
            _logger.LogInformation("Updating user with ID: {UserId}", id);
            
            var user = await _userManager.Users
                .Include(u => u.UserAddresses)
                .FirstOrDefaultAsync(u => u.Id == id);

            if (user == null || dto == null)
            {
                _logger.LogWarning("Update failed: User with ID {UserId} not found or DTO is null", id);
                
                return false;
            }

            if (!string.IsNullOrEmpty(dto.Email) && dto.Email != user.Email)
            {
                _logger.LogInformation("Updating email for user ID: {UserId} from {OldEmail} to {NewEmail}", id, user.Email, dto.Email);
                var setEmailResult = await _userManager.SetEmailAsync(user, dto.Email);

                if (!setEmailResult.Succeeded)
                {
                    _logger.LogError("Failed to update email for user ID: {UserId}. Errors: {Errors}", 
                        id, string.Join(", ", setEmailResult.Errors.Select(e => e.Description)));

                    return false;
                }
            }

            user.FirstName = dto.FirstName ?? user.FirstName;
            user.LastName = dto.LastName ?? user.LastName;
            user.PhoneNumber = dto.PhoneNumber ?? user.PhoneNumber;

            var dtoAddressIds = dto.Addresses.Where(a => a.Id.HasValue).Select(a => a.Id.Value).ToList();
            var addressesToRemove = user.UserAddresses.Where(a => !dtoAddressIds.Contains(a.Id)).ToList();
            
            _logger.LogInformation("Removing {Count} addresses for user ID: {UserId}", addressesToRemove.Count, id);
            foreach (var addr in addressesToRemove)
            {
                _context.UserAddresses.Remove(addr);
            }

            foreach (var addrDto in dto.Addresses)
            {
                if (addrDto.Id.HasValue)
                {
                    var existing = user.UserAddresses.FirstOrDefault(a => a.Id == addrDto.Id);

                    if (existing != null)
                    {
                        _logger.LogDebug("Updating address ID: {AddressId} for user ID: {UserId}", addrDto.Id, id);
                        existing.House = addrDto.House;
                        existing.Apartments = addrDto.Apartments;
                        existing.Street = addrDto.Street;
                        existing.City = addrDto.City;
                        _context.UserAddresses.Update(existing);
                    }
                    else
                    {
                        _logger.LogDebug("Adding new address for user ID: {UserId}", id);
                        user.UserAddresses.Add(new UserAddress
                        {
                            House = addrDto.House,
                            Apartments = addrDto.Apartments,
                            Street = addrDto.Street,
                            City = addrDto.City
                        });
                    }
                }
                else
                {
                    _logger.LogDebug("Adding new address for user ID: {UserId}", id);
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
            {
                _logger.LogError("Failed to update user ID: {UserId}. Errors: {Errors}", 
                    id, string.Join(", ", identityResult.Errors.Select(e => e.Description)));

                return false;
            }

            await _context.SaveChangesAsync();
            _logger.LogInformation("Successfully updated user ID: {UserId}", id);

            return true;
        }

        public async Task<Result<User>> GetUserByTokenAsync(string token)
        {
            _logger.LogInformation("Attempting to get user from token");
            
            if (string.IsNullOrWhiteSpace(token))
            {
                _logger.LogWarning("Token is empty or null");
               
                return Result.Fail<User>("Token is required.");
            }

            try
            {
                var handler = new System.IdentityModel.Tokens.Jwt.JwtSecurityTokenHandler();
                var jwt = handler.ReadJwtToken(token);
                var idClaim = jwt.Claims.FirstOrDefault(c =>
                    c.Type == System.Security.Claims.ClaimTypes.NameIdentifier ||
                    c.Type == "id" ||
                    c.Type == "sub" ||
                    c.Type == "userId");

                if (idClaim == null || !int.TryParse(idClaim.Value, out var userId))
                {
                    _logger.LogWarning("Invalid token claims or user ID not found in token");
                    
                    return Result.Fail<User>("Invalid token claims.");
                }

                _logger.LogInformation("Fetching user with ID: {UserId} from token", userId);
                var user = await _userManager.Users
                    .Include(u => u.UserAddresses)
                    .FirstOrDefaultAsync(u => u.Id == userId);

                if (user == null)
                {
                    _logger.LogWarning("User with ID {UserId} not found in database", userId);
                 
                    return Result.Fail<User>($"User with ID {userId} not found.");
                }

                _logger.LogInformation("Successfully retrieved user ID: {UserId} from token", userId);
              
                return Result.Success(user);
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error occurred while getting user from token");
               
                return Result.Fail<User>($"Failed to get user from token: {ex.Message}");
            }
        }
    }
}
