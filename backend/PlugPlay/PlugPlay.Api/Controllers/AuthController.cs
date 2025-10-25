using System.Security.Claims;
using AutoMapper;
using Google.Apis.Auth.OAuth2.Requests;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using PlugPlay.Api.Dto;
using PlugPlay.Domain.Entities;
using PlugPlay.Domain.Enums;
using PlugPlay.Domain.Extensions;
using PlugPlay.Services.Interfaces;

namespace PlugPlay.Api.Controllers;

[ApiController]
[Route("api/[controller]")]
public class AuthController : ControllerBase
{
    private readonly IAuthService _authService;

    private readonly IMapper _mapper;

    private readonly IConfiguration _configuration;

    private readonly ILogger<AuthController> _logger;

    public AuthController(IAuthService authService, IMapper mapper, IConfiguration configuration,
        ILogger<AuthController> logger)
    {
        _authService = authService;
        _mapper = mapper;
        _configuration = configuration;
        _logger = logger;
    }

    [HttpPost("google")]
    public async Task<ActionResult<LoginResponse>> GoogleSignIn([FromBody] GoogleSignInRequest request)
    {
        _logger.LogInformation("Google sign-in attempt");

        var validationResult = await _authService.ValidateGoogleSignInRequestAsync(request.IdToken);
        validationResult.OnFailure(() => _logger.LogWarning("Invalid Google token"));
        if (validationResult.Failure)
        {
            return Unauthorized(new { message = "Invalid Google token" });
        }

        var payload = validationResult.Value;

        var userResult = await _authService.GetOrCreateUser(payload.Email, payload.Name, payload.Subject);
        userResult.OnFailure(() =>
            _logger.LogWarning("Failed to get or create user for Google email: {Email}. " +
                               "Error: {Error}", payload.Email, userResult.Error));
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

        _logger.LogInformation("Successfully signed in user via Google: {Email}", payload.Email);

        return Ok(response);
    }

    [HttpPost("register")]
    public async Task<IActionResult> Register(RegisterRequest registerRequest)
    {
        _logger.LogInformation("Registration attempt for email: {Email}", registerRequest?.Email);

        if (registerRequest == null)
        {
            _logger.LogWarning("Invalid register data: request is null");

            return BadRequest(new ProblemDetails() { Title = "Invalid register data" });
        }

        if (!ModelState.IsValid)
        {
            _logger.LogWarning("Invalid register data: model state invalid for email: {Email}", registerRequest.Email);

            return BadRequest(ModelState);
        }

        var user = _mapper.Map<User>(registerRequest);
        user.Role = Role.User;
        var result = await _authService.RegisterAsync(user, registerRequest.Password,
            registerRequest.PhoneNumber, registerRequest.FirstName, registerRequest.LastName);
        result.OnFailure(() =>
                _logger.LogError("Registration failed for email: {Email}. Error: {Error}",
                registerRequest.Email, result.Error))
            .OnSuccess(() =>
                _logger.LogInformation("Successfully registered user with email: {Email}", registerRequest.Email));
        if (result.Failure)
        {
            return StatusCode(500, result.Error);
        }

        return Ok();
    }

    [HttpPost("login")]
    public async Task<ActionResult<LoginResponse>> Login([FromBody] LoginRequest request)
    {
        _logger.LogInformation("Login attempt for email: {Email}", request?.Email);

        if (request == null)
        {
            _logger.LogWarning("Invalid login data: request is null");

            return BadRequest(new ProblemDetails() { Title = $"Invalid user data" });
        }

        var validationResult = await _authService.ValidateUserCredentials(request.Email, request.Password);
        validationResult.OnFailure(() => _logger.LogWarning("Login failed for email: {Email}. Error: {Error}",
            request.Email,
            validationResult.Error));
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

        _logger.LogInformation("Successfully logged in user: {Email}", request.Email);

        return Ok(response);
    }

    [HttpPost("refresh")]
    public async Task<ActionResult<LoginResponse>> Refresh([FromBody] RefreshTokenRequest request)
    {
        _logger.LogInformation("Token refresh attempt");

        if (string.IsNullOrEmpty(request.RefreshToken))
        {
            _logger.LogWarning("Refresh token is empty");

            return BadRequest(new { message = "Refresh token is required" });
        }

        var result = await _authService.RefreshTokenAsync(request.RefreshToken);
        result.OnFailure(() => _logger.LogWarning("Token refresh failed: {Error}", result.Error));
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

        _logger.LogInformation("Successfully refreshed token for user ID: {UserId}", result.Value.Id);

        return Ok(response);
    }

    [HttpPost("logout")]
    public async Task<IActionResult> Logout([FromBody] LogoutDto request)
    {
        _logger.LogInformation("Logout attempt");

        if (string.IsNullOrEmpty(request.RefreshToken))
        {
            _logger.LogWarning("Logout failed: refresh token is empty");

            return BadRequest(new { message = "Refresh token is required" });
        }

        var result = await _authService.LogoutAsync(request.RefreshToken);
        result.OnFailure(() => _logger.LogWarning("Logout failed: {Error}", result.Error))
            .OnSuccess(() => _logger.LogInformation("Successfully logged out user"));
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
        _logger.LogInformation("Token verification attempt for user ID: {UserId}", userId);

        if (string.IsNullOrEmpty(userId))
        {
            _logger.LogWarning("Token verification failed: user ID not found in claims");

            return Unauthorized();
        }

        var isParsed = int.TryParse(userId, out var id);
        if (isParsed)
        {
            var result = await _authService.GetUserAsync(id);
            result.OnFailure(() =>
                    _logger.LogWarning("Token verification failed for user ID: {UserId}. Error: {Error}", id,
                        result.Error))
                .OnSuccess(() =>
                    _logger.LogInformation("Successfully verified token for user ID: {UserId}", id));
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

        _logger.LogWarning("Token verification failed: could not parse user ID");

        return Unauthorized();
    }

    [HttpPost("create_admin")]
    [Authorize(Roles = "Admin")]
    public async Task<IActionResult> CreateAdminAsync(RegisterRequest registerRequest)
    {
        _logger.LogInformation("Admin creation attempt for email: {Email}", registerRequest?.Email);

        if (registerRequest == null)
        {
            _logger.LogWarning("Invalid admin creation data: request is null");

            return BadRequest(new ProblemDetails() { Title = "Invalid register data" });
        }

        var user = _mapper.Map<User>(registerRequest);
        user.Role = Role.Admin;
        var result = await _authService.RegisterAsync(user, registerRequest.Password,
            registerRequest.PhoneNumber, registerRequest.FirstName, registerRequest.LastName);
        result.OnFailure(() =>
                _logger.LogError("Admin creation failed for email: {Email}. Error: {Error}",
                registerRequest.Email, result.Error))
            .OnSuccess(() =>
                _logger.LogInformation("Successfully created admin with email: {Email}", registerRequest.Email));
        if (result.Failure)
        {
            return StatusCode(500, result.Error);
        }

        return Ok();
    }
}
