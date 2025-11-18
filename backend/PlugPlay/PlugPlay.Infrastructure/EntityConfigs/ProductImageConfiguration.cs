using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;
using PlugPlay.Domain.Entities;

namespace PlugPlay.Infrastructure.EntityConfigs;

public class ProductImageConfiguration : IEntityTypeConfiguration<ProductImage>
{
    public void Configure(EntityTypeBuilder<ProductImage> builder)
    {
        builder.HasKey(pi => pi.Id);
        builder.Property(pi => pi.Id);

        builder.Property(pi => pi.ImageUrl)
            .IsRequired()
            .HasMaxLength(1000);

        builder.HasOne(pi => pi.Product)
            .WithMany(p => p.ProductImages)
            .HasForeignKey(pi => pi.ProductId)
            .OnDelete(DeleteBehavior.Cascade);
    }
}
