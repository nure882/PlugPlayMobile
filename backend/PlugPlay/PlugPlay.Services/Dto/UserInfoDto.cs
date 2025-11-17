namespace PlugPlay.Services.Dto;

public class UserInfoDto
{
    public int Id { get; set; }

    public string Email { get; set; }

    public string PhoneNumber { get; set; }

    public string FirstName { get; set; }

    public string LastName { get; set; }

    public ICollection<UserAddressDto> Addresses { get; set; } = new List<UserAddressDto>();

    public static UserInfoDto MapUser(Domain.Entities.User user)
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
}
