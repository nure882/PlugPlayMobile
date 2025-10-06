using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;
using PlugPlay.Domain.Entities;

namespace PlugPlay.Infrastructure.EntityConfigs;

public class UserConfiguration : IEntityTypeConfiguration<User>
{
    public void Configure(EntityTypeBuilder<User> builder)
    {
        builder.ToTable("user");

        builder.HasKey(u => u.Id);

        builder.Property(u => u.Id)
            .HasColumnName("user_id");

        builder.Property(u => u.FirstName)
            .HasColumnName("first_name")
            .IsRequired()
            .HasMaxLength(100);

        builder.Property(u => u.LastName)
            .HasColumnName("last_name")
            .IsRequired()
            .HasMaxLength(120);

        builder.Property(u => u.Email)
            .HasColumnName("email")
            .IsRequired()
            .HasMaxLength(150);

        builder.Property(u => u.PhoneNumber)
            .HasColumnName("phone_number")
            .IsRequired()
            .HasMaxLength(30);

        builder.Property(u => u.Role)
            .HasColumnName("role")
            .IsRequired();

        builder.HasMany(u => u.Orders)
            .WithOne(p => p.User)
            .HasForeignKey(p => p.UserId)
            .OnDelete(DeleteBehavior.SetNull);

        builder.HasMany(u => u.UserAddresses)
            .WithOne(ua => ua.User)
            .HasForeignKey(ua => ua.UserId)
            .OnDelete(DeleteBehavior.Cascade);

        builder.HasMany(u => u.CartItems)
            .WithOne(ci => ci.User)
            .HasForeignKey(ci => ci.UserId)
            .OnDelete(DeleteBehavior.Cascade);

        builder.HasMany(u => u.Reviews)
            .WithOne(r => r.User)
            .HasForeignKey(r => r.UserId)
            .OnDelete(DeleteBehavior.SetNull);

        builder.HasMany(u => u.WishLists)
            .WithOne(w => w.User)
            .HasForeignKey(w => w.UserId)
            .OnDelete(DeleteBehavior.Cascade);
    }
}