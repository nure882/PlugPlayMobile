using PlugPlay.Domain.Entities;

namespace PlugPlay.Api.Dto.Ordering;

public class UserAddressDto
{
    public int Id { get; set; }

    public int UserId { get; set; }

    public string Apartments { get; set; }

    public string House { get; set; }

    public string Street { get; set; }

    public string City { get; set; }

    public static UserAddressDto MapAddress(UserAddress address)
    {
        return new UserAddressDto
        {
            Id = address.Id,
            UserId = address.UserId,
            Apartments = address.Apartments,
            House = address.House,
            Street = address.Street,
            City = address.City,
        };
    }
}
