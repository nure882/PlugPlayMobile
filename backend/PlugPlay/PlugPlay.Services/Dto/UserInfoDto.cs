namespace PlugPlay.Services.Dto;

public class UserInfoDto
{
    public int Id { get; set; }

    public string Email { get; set; }

    public string FirstName { get; set; }

    public string LastName { get; set; }

    public ICollection<UserAddressDto> Addresses { get; set; } = new List<UserAddressDto>();
}
