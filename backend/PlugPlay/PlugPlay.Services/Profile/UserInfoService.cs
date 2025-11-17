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
            var fetchingUserInfo = LoggerMessage.Define<int>(
                LogLevel.Information,
                new EventId(3000, "FetchingUserInfo"),
                "Fetching user info for user ID: {UserId}");

            fetchingUserInfo(_logger, id, null);

            var userInfo = await _userManager.Users
                .Include(u => u.UserAddresses)
                .FirstOrDefaultAsync(u => u.Id == id);

            if (userInfo == null)
            {
                var userNotFoundWarningById = LoggerMessage.Define<int>(
                    LogLevel.Warning,
                    new EventId(3001, "UserNotFoundWarningById"),
                    "User with ID {UserId} not found");

                userNotFoundWarningById(_logger, id, null);

                return Result.Fail<User>("User not found"); 
            }

            var userInfoRetrievedSuccess = LoggerMessage.Define<int>(
                LogLevel.Information,
                new EventId(3002, "UserInfoRetrievedSuccess"),
                "Successfully retrieved user info for user ID: {UserId}");

            userInfoRetrievedSuccess(_logger, id, null);

            return Result.Success(userInfo);
        }

        public async Task<bool> UpdateUserAsync(int id, UserInfoDto dto)
        {
            var updatingUserInfo = LoggerMessage.Define<int>(
                LogLevel.Information,
                new EventId(3000, "UpdatingUserInfo"),
                "Updating user with ID: {UserId}");

            updatingUserInfo(_logger, id, null);

            var user = await _userManager.Users
                .Include(u => u.UserAddresses)
                .FirstOrDefaultAsync(u => u.Id == id);

            if (user == null || dto == null)
            {
                var updateUserFailed = LoggerMessage.Define<int>(
                    LogLevel.Warning,
                    new EventId(3001, "UpdateUserFailed"),
                    "Update failed: User with ID {UserId} not found or DTO is null");

                updateUserFailed(_logger, id, null);

                return false;
            }

            if (!string.IsNullOrEmpty(dto.Email) && dto.Email != user.Email)
            {
                var updatingUserEmail = LoggerMessage.Define<int, string, string>(
                    LogLevel.Information,
                    new EventId(3000, "UpdatingUserEmail"),
                    "Updating email for user ID: {UserId} from {OldEmail} to {NewEmail}");

                updatingUserEmail(_logger, id, user.Email, dto.Email, null);
                var setEmailResult = await _userManager.SetEmailAsync(user, dto.Email);

                if (!setEmailResult.Succeeded)
                {
                    var failedToUpdateUserEmail = LoggerMessage.Define<int, string>(
                        LogLevel.Error,
                        new EventId(3001, "FailedToUpdateUserEmail"),
                        "Failed to update email for user ID: {UserId}. Errors: {Errors}");

                    failedToUpdateUserEmail(
                        _logger,
                        id,
                        string.Join(", ", setEmailResult.Errors.Select(e => e.Description)),
                        null
                    );

                    return false;
                }
            }

            user.FirstName = dto.FirstName ?? user.FirstName;
            user.LastName = dto.LastName ?? user.LastName;
            user.PhoneNumber = dto.PhoneNumber ?? user.PhoneNumber;

            var dtoAddressIds = dto.Addresses.Where(a => a.Id.HasValue).Select(a => a.Id.Value).ToList();
            var addressesToRemove = user.UserAddresses.Where(a => !dtoAddressIds.Contains(a.Id)).ToList();

            var removingUserAddresses = LoggerMessage.Define<int, int>(
                LogLevel.Information,
                new EventId(3000, "RemovingUserAddresses"),
                "Removing {Count} addresses for user ID: {UserId}");

            removingUserAddresses(_logger, addressesToRemove.Count, id, null);
            foreach (var addr in addressesToRemove)
            {
                _context.UserAddresses.Remove(addr);
            }

            foreach (var addrDto in dto.Addresses)
            {
                var addingUserAddress = LoggerMessage.Define<int>(
                      LogLevel.Debug,
                      new EventId(3000, "AddingUserAddress"),
                      "Adding new address for user ID: {UserId}");

                if (addrDto.Id.HasValue)
                {
                    var existing = user.UserAddresses.FirstOrDefault(a => a.Id == addrDto.Id);

                    if (existing != null)
                    {
                        var updatingUserAddress = LoggerMessage.Define<int, int>(
                            LogLevel.Debug,
                            new EventId(3000, "UpdatingUserAddress"),
                            "Updating address ID: {AddressId} for user ID: {UserId}");

                        updatingUserAddress(_logger, addrDto.Id ?? 0, id, null);

                        existing.House = addrDto.House;
                        existing.Apartments = addrDto.Apartments;
                        existing.Street = addrDto.Street;
                        existing.City = addrDto.City;
                        _context.UserAddresses.Update(existing);
                    }
                    else
                    {
                        addingUserAddress(_logger, id, null);

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
                  

                    addingUserAddress(_logger, id, null);

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
                var failedToUpdateUser = LoggerMessage.Define<int, string>(
                    LogLevel.Error,
                    new EventId(3001, "FailedToUpdateUser"),
                    "Failed to update user ID: {UserId}. Errors: {Errors}");

                failedToUpdateUser(
                    _logger,
                    id,
                    string.Join(", ", identityResult.Errors.Select(e => e.Description)),
                    null
                );

                return false;
            }

            await _context.SaveChangesAsync();

            var userUpdateSuccess = LoggerMessage.Define<int>(
                LogLevel.Information,
                new EventId(3002, "UserUpdateSuccess"),
                "Successfully updated user ID: {UserId}");

            userUpdateSuccess(_logger, id, null);

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

                var fetchingUserFromToken = LoggerMessage.Define<int>(
                    LogLevel.Information,
                    new EventId(3000, "FetchingUserFromToken"),
                    "Fetching user with ID: {UserId} from token");

                fetchingUserFromToken(_logger, userId, null);

                var user = await _userManager.Users
                    .Include(u => u.UserAddresses)
                    .FirstOrDefaultAsync(u => u.Id == userId);

                if (user == null)
                {
                    var userNotFoundInDb = LoggerMessage.Define<int>(
                        LogLevel.Warning,
                        new EventId(3001, "UserNotFoundInDb"),
                        "User with ID {UserId} not found in database");

                    userNotFoundInDb(_logger, userId, null);

                    return Result.Fail<User>($"User with ID {userId} not found.");
                }

                var userRetrievedFromToken = LoggerMessage.Define<int>(
                    LogLevel.Information,
                    new EventId(3002, "UserRetrievedFromToken"),
                    "Successfully retrieved user ID: {UserId} from token");

                userRetrievedFromToken(_logger, userId, null);

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
