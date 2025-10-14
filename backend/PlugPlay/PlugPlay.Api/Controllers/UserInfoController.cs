using Microsoft.AspNetCore.Mvc;
using PlugPlay.Services.Interfaces;

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
        var userInfo = await _userInfoService.GetUserInfoByIdAsync(id);

        if (userInfo == null)
            throw new KeyNotFoundException($"User with ID {id} not found.");

        return Ok(userInfo);
    }
}