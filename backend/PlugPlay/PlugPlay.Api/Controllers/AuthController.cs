using System.Security.Claims;
using AutoMapper;
using Google.Apis.Auth.OAuth2.Requests;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using PlugPlay.Api.Dto;
using PlugPlay.Domain.Entities;
using PlugPlay.Domain.Enums;
using PlugPlay.Services.Interfaces;

namespace PlugPlay.Api.Controllers;

[ApiController]
[Route("api/[controller]")]
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

    [HttpPost("register")]
    public async Task<IActionResult> Register(RegisterRequest registerRequest)
    {
        if (registerRequest == null)
        {
            return BadRequest(new ProblemDetails() { Title = "Invalid register data" });
        }

        if (!ModelState.IsValid)
        {
            return BadRequest(ModelState);
        }

        var user = _mapper.Map<User>(registerRequest);
        user.Role = Role.User;
        var result = await _authService.RegisterAsync(user, registerRequest.Password,
            registerRequest.PhoneNumber, registerRequest.FirstName, registerRequest.LastName);
        if (result.Failure)
        {
            return StatusCode(500, result.Error);
        }

        return Ok();
    }

    [HttpPost("login")]
    public async Task<ActionResult<LoginResponse>> Login([FromBody] LoginRequest request)
    {
        if (request == null)
        {
            return BadRequest(new ProblemDetails() { Title = $"Invalid user data" });
        }

        var validationResult = await _authService.ValidateUserCredentials(request.Email, request.Password);
        if (validationResult.Failure)
        {
            return Unauthorized(new { Message = validationResult.Error });
        }

        (string, string) tokens = await _authService.GenerateTokens(validationResult.Value);
        var tokenExpiration = DateTime.UtcNow.AddMinutes(
            Convert.ToDouble(_configuration["Jwt:TokenExpirationMinutes"]));
        var response = new LoginResponse
        {
            Token = tokens.Item1,
            RefreshToken = tokens.Item2,
            Expiration = tokenExpiration,
            User = new UserDto
            {
                Id = validationResult.Value.Id,
                Email = validationResult.Value.Email,
                FirstName = validationResult.Value.FirstName,
                LastName = validationResult.Value.LastName
            }
        };

        return Ok(response);
    }

    [HttpPost("refresh")]
    public async Task<ActionResult<LoginResponse>> Refresh([FromBody] RefreshTokenRequest request)
    {
        if (string.IsNullOrEmpty(request.RefreshToken))
        {
            return BadRequest(new { message = "Refresh token is required" });
        }

        var result = await _authService.RefreshTokenAsync(request.RefreshToken);
        if (result.Failure)
        {
            return Unauthorized(new { message = result.Error });
        }

        LoginResponse response = new LoginResponse
        {
            Token = result.Value.Token,
            RefreshToken = result.Value.RefreshToken,
            Expiration = DateTime.UtcNow.AddMinutes(
                Convert.ToDouble(_configuration["Jwt:TokenExpirationMinutes"])),
            User = new UserDto
            {
                Id = result.Value.Id,
                Email = result.Value.Email,
                FirstName = result.Value.FirstName,
                LastName = result.Value.LastName
            }
        };

        return Ok(response);
    }

    [HttpPost("logout")]
    public async Task<IActionResult> Logout([FromBody] LogoutDto request)
    {
        if (string.IsNullOrEmpty(request.RefreshToken))
        {
            return BadRequest(new { message = "Refresh token is required" });
        }

        var result = await _authService.LogoutAsync(request.RefreshToken);
        if (result.Failure)
        {
            return BadRequest(new { message = result.Error });
        }

        return Ok(new { message = "Logged out successfully" });
    }

    [HttpPost("verify")]
    [Authorize(Roles = "User, Admin")]
    public async Task<ActionResult<UserDto>> VerifyToken()
    {
        var userId = User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
        if (string.IsNullOrEmpty(userId))
        {
            return Unauthorized();
        }

        var isParsed = int.TryParse(userId, out var id);
        if (isParsed)
        {
            var result = await _authService.GetUserAsync(id);
            if (result.Failure)
            {
                return Unauthorized();
            }

            var userDto = new UserDto
            {
                Id = result.Value.Id,
                Email = result.Value.Email,
                FirstName = result.Value.FirstName,
                LastName = result.Value.LastName,
            };

            return Ok(userDto);
        }

        return Unauthorized();
    }

    [HttpPost("create_admin")]
    [Authorize(Roles = "Admin")]
    public async Task<IActionResult> CreateAdminAsync(RegisterRequest registerRequest)
    {
        if (registerRequest == null)
        {
            return BadRequest(new ProblemDetails() { Title = "Invalid register data" });
        }

        var user = _mapper.Map<User>(registerRequest);
        user.Role = Role.Admin;
        var result = await _authService.RegisterAsync(user, registerRequest.Password,
            registerRequest.PhoneNumber, registerRequest.FirstName, registerRequest.LastName);
        if (result.Failure)
        {
            return StatusCode(500, result.Error);
        }

        return Ok();
    }
}
