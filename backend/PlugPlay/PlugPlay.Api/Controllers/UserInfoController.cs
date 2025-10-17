using Microsoft.AspNetCore.Mvc;
using PlugPlay.Services.Interfaces;
using PlugPlay.Shared.Dto;

namespace PlugPlay.Api.Controllers;

[Route("api/[controller]")]
[ApiController]
public class UserInfoController : ControllerBase
{
    private readonly IUserInfoService _userInfoService;

    public UserInfoController(IUserInfoService userInfoService)
    {
        _userInfoService = userInfoService;
    }

    [HttpGet]
    public async Task<IActionResult> GetUserInfoById(int id)
    {
        var user = await _userInfoService.GetUserInfoByIdAsync(id);

        if (user == null)
            throw new KeyNotFoundException($"User with ID {id} not found.");

        UserInfoDto userInfo = new UserInfoDto
        {
            Id = user.Id,
            Email = user.Email,
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

        return Ok(userInfo);
    }

    [HttpPut("{id:int}")]
    public async Task<IActionResult> UpdateUser(int id, [FromBody] UserInfoDto dto)
    {
        var result = await _userInfoService.UpdateUserAsync(id, dto);

        if (!result)
            throw new KeyNotFoundException($"User with ID {id} not found.");

        return Ok("User updated successfully.");
    }
}