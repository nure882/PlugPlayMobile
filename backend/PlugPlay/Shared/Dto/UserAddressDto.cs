namespace PlugPlay.Shared.Dto;

public class UserAddressDto
{
    public int? Id { get; set; } // null for new addresses

    public string Apartments { get; set; }

    public string House { get; set; }

    public string Street { get; set; }

    public string City { get; set; }
}
