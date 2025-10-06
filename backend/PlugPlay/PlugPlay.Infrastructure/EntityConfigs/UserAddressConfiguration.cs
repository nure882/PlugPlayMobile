using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;
using PlugPlay.Domain.Entities;

namespace PlugPlay.Infrastructure.EntityConfigs;

public class UserAddressConfiguration : IEntityTypeConfiguration<UserAddress>
{
    public void Configure(EntityTypeBuilder<UserAddress> builder)
    {
        builder.ToTable("user_address");

        builder.HasKey(a => a.Id);

        builder.Property(a => a.Apartments)
            .HasMaxLength(50);

        builder.Property(a => a.House)
            .HasMaxLength(50);

        builder.Property(a => a.Street)
            .HasMaxLength(200);

        builder.Property(a => a.City)
            .HasMaxLength(100);

        builder.HasOne(a => a.User)
            .WithMany(u => u.UserAddresses)
            .HasForeignKey(a => a.UserId)
            .OnDelete(DeleteBehavior.Cascade);
    }
}