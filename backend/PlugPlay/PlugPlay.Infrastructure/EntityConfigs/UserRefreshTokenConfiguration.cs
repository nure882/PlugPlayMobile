using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;
using PlugPlay.Domain.Entities;

namespace PlugPlay.Infrastructure.EntityConfigs;

public class UserRefreshTokenConfiguration : IEntityTypeConfiguration<UserRefreshToken>
{
    public void Configure(EntityTypeBuilder<UserRefreshToken> builder)
    {
        builder.ToTable("user_refresh_token");

        builder.HasKey(rt => rt.Id);

        builder.HasIndex(rt => rt.Token)
            .IsUnique();

        builder.Property(rt => rt.Token)
            .HasColumnName("token")
            .IsRequired();

        builder.Property(rt => rt.Expires)
            .HasColumnName("expires")
            .HasColumnType("timestamp with time zone")
            .IsRequired();

        builder.Property(rt => rt.CreatedAt)
            .HasColumnName("created_at")
            .HasColumnType("timestamp with time zone")
            .HasDefaultValueSql("CURRENT_TIMESTAMP")
            .IsRequired();

        builder.Property(rt => rt.CreatedByIp)
            .HasColumnName("created_by_ip")
            .IsRequired();

        builder.Property(rt => rt.Revoked)
            .HasColumnName("revoked");

        builder.Property(rt => rt.RevokedByIp)
            .HasColumnName("revoked_by_ip");

        builder.Property(rt => rt.ReplacedByToken)
            .HasColumnName("replaced_by_token");

        builder.Property(rt => rt.UserId)
            .HasColumnName("user_id")
            .IsRequired();

        builder.HasOne(rt => rt.User)
            .WithMany(u => u.RefreshTokens)
            .HasForeignKey(rt => rt.UserId);
    }
}