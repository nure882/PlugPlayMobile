using Microsoft.AspNetCore.Mvc;
using PlugPlay.Domain.Entities;
using PlugPlay.Domain.Extensions;
using PlugPlay.Services.Dto;
using PlugPlay.Services.Interfaces;

namespace PlugPlay.Api.Controllers;

[Route("api/[controller]")]
[ApiController]
public class UserInfoController : ControllerBase
{
    private readonly IUserInfoService _userInfoService;

    private readonly ILogger<UserInfoController> _logger;

    public UserInfoController(IUserInfoService userInfoService, ILogger<UserInfoController> logger)
    {
        _userInfoService = userInfoService;
        _logger = logger;
    }

    [HttpGet("{token}")]
    public async Task<IActionResult> GetUserByToken(string token)
    {
        _logger.LogInformation("Getting user by token");
        
        var userResult = await _userInfoService.GetUserByTokenAsync(token);
        userResult.OnFailure(() =>
                _logger.LogWarning("Failed to get user by token: {Error}", userResult.Error))
            .OnSuccess(() =>
            {
                var userRetrievedByToken = LoggerMessage.Define<int>(
                    LogLevel.Information,
                    new EventId(3001, "UserRetrievedByToken"),
                    "Successfully retrieved user ID: {UserId} by token");

                userRetrievedByToken(_logger, userResult.Value.Id, null);
            });
        if (userResult.Failure)
        {
            return BadRequest("No such user");
        }

        var user = userResult.Value;
        UserInfoDto userInfo = MapUser(user); 

        return Ok(userInfo);
    }

    [HttpGet("{id:int}")]
    public async Task<IActionResult> GetUserInfoById(int id)
    {
        var gettingUserInfo = LoggerMessage.Define<int>(
            LogLevel.Information,
            new EventId(3000, "GettingUserInfo"),
            "Getting user info for user ID: {UserId}");

        gettingUserInfo(_logger, id, null);

        var result = await _userInfoService.GetUserInfoByIdAsync(id);

        result.OnSuccess(() =>
        {
            var userInfoRetrieved = LoggerMessage.Define<int>(
                LogLevel.Information,
                new EventId(3002, "UserInfoRetrieved"),
                "Successfully retrieved user info for user ID: {UserId}");

            userInfoRetrieved(_logger, id, null);
        });

        result.OnFailure(() =>
        {
            var userNotFound = LoggerMessage.Define<int>(
                LogLevel.Warning,
                new EventId(3001, "UserNotFound"),
                "User with ID {UserId} not found");

            userNotFound(_logger, id, null);
        });

        if (result.Failure)
        {
            return NotFound(new { message = result.Error });
        }

        User user = result.Value;
        UserInfoDto userInfo = MapUser(user);

        return Ok(userInfo);
    }

    [HttpPut("{id:int}")]
    public async Task<IActionResult> UpdateUserById(int id, [FromBody] UserInfoDto dto)
    {
        var updatingUser = LoggerMessage.Define<int>(
            LogLevel.Information,
            new EventId(3000, "UpdatingUser"),
            "Updating user with ID: {UserId}");

        updatingUser(_logger, id, null);

        bool success = await _userInfoService.UpdateUserAsync(id, dto);
        if(success)
        {
            var userUpdated = LoggerMessage.Define<int>(
                LogLevel.Information,
                new EventId(3002, "UserUpdated"),
                "Successfully updated user with ID: {UserId}");

            userUpdated(_logger, id, null);

            return Ok("User updated successfully.");
        }

        var errorUpdatingUser = LoggerMessage.Define<int>(
            LogLevel.Error,
            new EventId(3001, "ErrorUpdatingUser"),
            "Error updating user with ID: {UserId}");

        errorUpdatingUser(_logger, id, null);

        return BadRequest(new { message = "Failed to update user" });
    }

    [HttpPut("{token}")]
    public async Task<IActionResult> UpdateUserByToken(string token, [FromBody] UserInfoDto dto)
    {
        _logger.LogInformation("Received update for token {Token} with data: {@Dto}", token, dto);
        _logger.LogInformation("Getting user by token");

        var userResult = await _userInfoService.GetUserByTokenAsync(token);
        userResult.OnFailure(() =>
                _logger.LogWarning("Failed to get user by token: {Error}", userResult.Error))
            .OnSuccess(() =>
            {
                var userRetrievedByToken = LoggerMessage.Define<int>(
                    LogLevel.Information,
                    new EventId(3002, "UserRetrievedByToken"),
                    "Successfully retrieved user ID: {UserId} by token");

                userRetrievedByToken(_logger, userResult.Value.Id, null);
            });

        if (userResult.Failure)
        {
            return BadRequest("No such user");
        }

        int id = userResult.Value.Id;

        var updatingUserByToken = LoggerMessage.Define<int>(
                LogLevel.Information,
                new EventId(3000, "UpdatingUserByToken"),
                "Updating user with ID: {UserId}");

        updatingUserByToken(_logger, id, null);

        var success = await _userInfoService.UpdateUserAsync(id, dto);

        if(success)
        {
            var userUpdatedByToken = LoggerMessage.Define<int>(
                LogLevel.Information,
                new EventId(3002, "UserUpdatedByToken"),
                "Successfully updated user with ID: {UserId}");

            userUpdatedByToken(_logger, id, null);

            return Ok("User updated successfully.");
        }
        
        
        var errorUpdatingUserByToken = LoggerMessage.Define<int>(
            LogLevel.Error,
            new EventId(3001, "ErrorUpdatingUserByToken"),
            "Error updating user with ID: {UserId}");

        errorUpdatingUserByToken(_logger, id, null);

        return BadRequest(new { message = "Failed to retrieve user by token" });
        
    }

    #region Helpers

    private UserInfoDto MapUser(Domain.Entities.User user)
    {
        return new UserInfoDto
        {
            Id = user.Id,
            Email = user.Email,
            PhoneNumber = user.PhoneNumber,
            FirstName = user.FirstName,
            LastName = user.LastName,
            Addresses = user.UserAddresses
                .Select(a => new UserAddressDto
                {
                    Id = a.Id,
                    House = a.House,
                    Apartments = a.Apartments,
                    Street = a.Street,
                    City = a.City
                })
                .ToList()
        };
    }

    #endregion
}
