using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;
using PlugPlay.Domain.Entities;

namespace PlugPlay.Infrastructure.EntityConfigs;

public class ReviewConfiguration : IEntityTypeConfiguration<Review>
{
    public void Configure(EntityTypeBuilder<Review> builder)
    {
        builder.ToTable("review");

        builder.HasKey(r => r.Id);

        builder.Property(r => r.Rating)
            .IsRequired();

        builder.Property(r => r.Comment)
            .HasMaxLength(1000);

        builder.Property(r => r.CreatedAt)
            .IsRequired()
            .HasColumnType("timestamptz");

        builder.Property(r => r.UpdatedAt)
            .HasColumnType("timestamptz");

        builder.HasOne(r => r.User)
            .WithMany(u => u.Reviews)
            .HasForeignKey(r => r.UserId)
            .OnDelete(DeleteBehavior.Cascade);

        builder.HasOne(r => r.Product)
            .WithMany(p => p.Reviews)
            .HasForeignKey(r => r.ProductId)
            .OnDelete(DeleteBehavior.Cascade);
    }
}