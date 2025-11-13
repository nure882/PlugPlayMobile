namespace PlugPlay.Api.Dto.Auth;

public class LoginResponse
{
    public string Token { get; set; }

    public string RefreshToken { get; set; }

    public DateTime Expiration { get; set; }

    public UserDto User { get; set; }
}
