using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;
using PlugPlay.Domain.Entities;

namespace PlugPlay.Infrastructure.EntityConfigs;

public class OrderConfiguration : IEntityTypeConfiguration<Order>
{
    public void Configure(EntityTypeBuilder<Order> builder)
    {
        builder.ToTable("order");

        builder.HasKey(o => o.Id);

        builder.Property(o => o.OrderDate)
            .IsRequired();

        builder.Property(o => o.Status)
            .IsRequired();

        builder.Property(o => o.TotalAmount)
            .HasColumnType("decimal(18,2)")
            .IsRequired();

        builder.Property(o => o.DiscountAmount)
            .HasColumnType("decimal(18,2)");

        builder.Property(o => o.PaymentMethod)
            .IsRequired();

        builder.Property(o => o.DeliveryAddress)
            .HasMaxLength(500);

        builder.Property(o => o.TransactionId)
            .IsRequired();

        builder.Property(o => o.PaymentCreated);

        builder.Property(o => o.PaymentProcessed);

        builder.Property(o => o.PaymentFailureReason)
            .HasMaxLength(500);

        builder.HasOne(o => o.User)
            .WithMany(u => u.Orders)
            .HasForeignKey(o => o.UserId)
            .OnDelete(DeleteBehavior.SetNull);

        builder.HasMany(o => o.OrderItems)
            .WithOne(oi => oi.Order)
            .HasForeignKey(oi => oi.OrderId)
            .OnDelete(DeleteBehavior.Cascade);
    }
}