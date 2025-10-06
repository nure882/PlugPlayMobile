using AutoMapper;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using PlugPlay.Api.Dto;
using PlugPlay.Domain.Entities;
using PlugPlay.Domain.Enums;
using PlugPlay.Services.Interfaces;

namespace PlugPlay.Api.Controllers;

[Route("api/[controller]")]
[ApiController]
public class AuthController : ControllerBase
{
    private readonly IAuthService _authService;

    private readonly IMapper _mapper;

    private readonly IConfiguration _configuration;

    public AuthController(IAuthService authService, IMapper mapper, IConfiguration configuration)
    {
        _authService = authService;
        _mapper = mapper;
        _configuration = configuration;
    }

    [HttpPost("register")]
    public async Task<IActionResult> RegisterAsync(ReqisterRequest reqisterRequest)
    {
        if (reqisterRequest == null)
        {
            return BadRequest(new ProblemDetails() { Title = "Invalid register data" });
        }

        if (!ModelState.IsValid)
        {
            return BadRequest(ModelState);
        }
        var user = _mapper.Map<User>(reqisterRequest);
        user.Role = Role.User;
        var result = await _authService.RegisterAsync(user, reqisterRequest.Password,
            reqisterRequest.PhoneNumber, reqisterRequest.FirstName, reqisterRequest.LastName);
        if (result.Failure)
        {
            return StatusCode(500, result.Error);
        }

        return Ok();
    }

    [HttpPost("create_admin")]
    [Authorize(Roles = "Admin")]
    public async Task<IActionResult> CreateAdminAsync(ReqisterRequest reqisterRequest)
    {
        if (reqisterRequest == null)
        {
            return BadRequest(new ProblemDetails() { Title = "Invalid register data" });
        }

        var user = _mapper.Map<User>(reqisterRequest);
        user.Role = Role.Admin;
        var result = await _authService.RegisterAsync(user, reqisterRequest.Password,
            reqisterRequest.PhoneNumber, reqisterRequest.FirstName, reqisterRequest.LastName);
        if (result.Failure)
        {
            return StatusCode(500, result.Error);
        }

        return Ok();
    }

    [HttpPost("google")]
    public async Task<ActionResult<LoginResponse>> GoogleSignIn([FromBody] GoogleSignInRequest request)
    {
        var validationResult = await _authService.ValidateGoogleSignInRequestAsync(request.IdToken);
        if (validationResult.Failure)
        {
            return Unauthorized(new { message = "Invalid Google token" });
        }

        var payload = validationResult.Value;

        var userResult = await _authService.GetOrCreateUser(payload.Email, payload.Name, payload.Subject);
        if (userResult.Failure)
        {
            return Unauthorized(new { message = userResult.Error });
        }

        (string, string) tokens = await _authService.GenerateTokens(userResult.Value);
        var tokenExpiration = DateTime.UtcNow.AddMinutes(
            Convert.ToDouble(_configuration["Jwt:TokenExpirationMinutes"]));
        var response = new LoginResponse
        {
            Token = tokens.Item1,
            RefreshToken = tokens.Item2,
            Expiration = tokenExpiration,
            User = new UserDto
            {
                Id = userResult.Value.Id,
                Email = userResult.Value.Email,
                FirstName = userResult.Value.FirstName,
                LastName = userResult.Value.LastName
            }
        };

        return Ok(response);
    }
}