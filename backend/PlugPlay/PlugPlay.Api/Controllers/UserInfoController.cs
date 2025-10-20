using Microsoft.AspNetCore.Mvc;
using PlugPlay.Services.Interfaces;
using PlugPlay.Services.Dto;

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

    [HttpGet("{id:int}")]
    public async Task<IActionResult> GetUserInfoById(int id)
    {
        try
        {
            var user = await _userInfoService.GetUserInfoByIdAsync(id);

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
        catch (KeyNotFoundException ex) 
        {
            return NotFound(new { message = ex.Message });
        }
    }

    [HttpPut("{id:int}")]
    public async Task<IActionResult> UpdateUser(int id, [FromBody] UserInfoDto dto)
    {
        try
        {
            var result = await _userInfoService.UpdateUserAsync(id, dto);

            return Ok("User updated successfully.");
        }
        catch(Exception ex)
        {
            return BadRequest(new { message = ex.Message });
        }
    }
}