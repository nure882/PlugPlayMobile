namespace PlugPlay.Domain.Entities;

public class UserAddress
{
    public int Id { get; set; }

    public int UserId { get; set; }

    public string Apartments { get; set; }

    public string House { get; set; }

    public string Street { get; set; }

    public string City { get; set; }

    public User User { get; set; }
}
