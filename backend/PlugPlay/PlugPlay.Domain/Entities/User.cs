using System.ComponentModel.DataAnnotations;
using Microsoft.AspNetCore.Identity;
using PlugPlay.Domain.Enums;

namespace PlugPlay.Domain.Entities;

public class User : IdentityUser<int>
{
    [Required]
    public string FirstName { get; set; }
        
    [Required]
    public string LastName { get; set; }
        
    [Required]
    public Role Role { get; set; } = Role.User;

    public string GoogleId { get; set; }

    public string PictureUrl { get; set; }

    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
        
    public DateTime UpdatedAt { get; set; } = DateTime.UtcNow;

    public ICollection<CartItem> CartItems { get; set; } = new List<CartItem>();

    public ICollection<Order> Orders { get; set; } = new List<Order>();

    public ICollection<UserRefreshToken> RefreshTokens { get; set; } = new List<UserRefreshToken>();

    public ICollection<Review> Reviews { get; set; } = new List<Review>();

    public ICollection<WishList> WishLists { get; set; } = new List<WishList>();

    public ICollection<UserAddress> UserAddresses { get; set; } = new List<UserAddress>();
}
