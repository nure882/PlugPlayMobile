using System.Reflection;
using Microsoft.AspNetCore.Identity;
using Microsoft.AspNetCore.Identity.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore;
using PlugPlay.Domain.Entities;
using PlugPlay.Infrastructure.Extensions;
using Attribute = PlugPlay.Domain.Entities.Attribute;

namespace PlugPlay.Infrastructure;

public class PlugPlayDbContext : IdentityDbContext<User, IdentityRole<int>, int>
{
    public PlugPlayDbContext(DbContextOptions<PlugPlayDbContext> options) : base(options)
    {
    }

    public DbSet<Attribute> Attributes { get; set; }

    public DbSet<CartItem> CartItems { get; set; }

    public DbSet<Category> Categories { get; set; }

    public DbSet<Order> Orders { get; set; }

    public DbSet<OrderItem> OrderItems { get; set; }

    public DbSet<Product> Products { get; set; }

    public DbSet<ProductAttribute> ProductAttributes { get; set; }

    public DbSet<ProductImage> ProductImages { get; set; }

    public DbSet<Review> Reviews { get; set; }

    public DbSet<WishList> Wishlists { get; set; }

    public DbSet<UserAddress> UserAddresses { get; set; }

    public DbSet<User> Users { get; set; }

    public DbSet<UserRefreshToken> UserRefreshTokens { get; set; }

    public DbSet<WishList> WishLists { get; set; }

    protected override void OnModelCreating(ModelBuilder builder)
    {
        base.OnModelCreating(builder);

        builder.UseSnakeCaseNamingConvention();
        builder.ApplyConfigurationsFromAssembly(Assembly.GetAssembly(typeof(PlugPlayDbContext)));
    }

    protected override void OnConfiguring(DbContextOptionsBuilder optionsBuilder)
    {
        base.OnConfiguring(optionsBuilder);

        if (!optionsBuilder.IsConfigured)
        {
            optionsBuilder.UseNpgsql("Server=localhost;Port=5432;Database=plugplay;Username=myuser;Password=mypassword;");
        }
    }
}