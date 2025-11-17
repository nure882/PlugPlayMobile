using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;
using PlugPlay.Domain.Entities;

namespace PlugPlay.Infrastructure.EntityConfigs;

public class OrderConfiguration : IEntityTypeConfiguration<Order>
{
    public void Configure(EntityTypeBuilder<Order> builder)
    {
        builder.HasKey(o => o.Id);
        builder.Property(o => o.Id);

        builder.Property(o => o.UserId);

        builder.Property(o => o.OrderDate)
            .IsRequired();

        builder.Property(o => o.Status)
            .IsRequired();

        builder.Property(o => o.TotalAmount)
            .HasColumnType("decimal(18,2)")
            .IsRequired();

        builder.Property(o => o.DiscountAmount)
            .HasColumnType("decimal(18,2)");

        builder.Property(o => o.DeliveryMethod)
            .IsRequired();

        builder.Property(o => o.PaymentMethod)
            .IsRequired();

        builder.Property(o => o.DeliveryAddressId);

        builder.Property(o => o.PaymentStatus)
            .IsRequired();

        builder.Property(o => o.TransactionId)
            .IsRequired();

        builder.Property(o => o.PaymentCreated);

        builder.Property(o => o.PaymentProcessed);

        builder.Property(o => o.PaymentFailureReason)
            .HasMaxLength(500);

        builder.Property(o => o.UpdatedAt)
            .IsRequired();

        builder.HasOne(o => o.User)
            .WithMany(u => u.Orders)
            .HasForeignKey(o => o.UserId)
            .OnDelete(DeleteBehavior.SetNull);

        builder.HasMany(o => o.OrderItems)
            .WithOne(oi => oi.Order)
            .HasForeignKey(oi => oi.OrderId)
            .OnDelete(DeleteBehavior.Cascade);

        builder.HasOne(o => o.DeliveryAddress)
            .WithMany(ua => ua.Orders)
            .HasForeignKey(o => o.DeliveryAddressId)
            .OnDelete(DeleteBehavior.SetNull);
    }
}
