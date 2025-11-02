using Microsoft.AspNetCore.Mvc;
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
                _logger.LogInformation("Successfully retrieved user ID: {UserId} by token", userResult.Value.Id));
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
        _logger.LogInformation("Getting user info for user ID: {UserId}", id);
        
        try
        {
            var user = await _userInfoService.GetUserInfoByIdAsync(id);
            UserInfoDto userInfo = MapUser(user);

            _logger.LogInformation("Successfully retrieved user info for user ID: {UserId}", id);
            
            return Ok(userInfo);
        }
        catch (KeyNotFoundException ex)
        {
            _logger.LogWarning(ex, "User with ID {UserId} not found", id);
            
            return NotFound(new { message = ex.Message });
        }
    }

    [HttpPut("{id:int}")]
    public async Task<IActionResult> UpdateUserById(int id, [FromBody] UserInfoDto dto)
    {
        _logger.LogInformation("Updating user with ID: {UserId}", id);

        try
        {
            var result = await _userInfoService.UpdateUserAsync(id, dto);
            
            _logger.LogInformation("Successfully updated user with ID: {UserId}", id);

            return Ok("User updated successfully.");
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error updating user with ID: {UserId}", id);
           
            return BadRequest(new { message = ex.Message });
        }
    }

    [HttpPut("{token}")]
    public async Task<IActionResult> UpdateUserByToken(string token, [FromBody] UserInfoDto dto)
    {
        _logger.LogInformation("Getting user by token");

        var userResult = await _userInfoService.GetUserByTokenAsync(token);
        userResult.OnFailure(() =>
                _logger.LogWarning("Failed to get user by token: {Error}", userResult.Error))
            .OnSuccess(() =>
                _logger.LogInformation("Successfully retrieved user ID: {UserId} by token", userResult.Value.Id));

        if (userResult.Failure)
        {
            return BadRequest("No such user");
        }

        int id = userResult.Value.Id;
        try
        {
            _logger.LogInformation("Updating user with ID: {UserId}", id);
            var result = await _userInfoService.UpdateUserAsync(id, dto);

            _logger.LogInformation("Successfully updated user with ID: {UserId}", id);

            return Ok("User updated successfully.");
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error updating user with ID: {UserId}", id);

            return BadRequest(new { message = ex.Message });
        }
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
