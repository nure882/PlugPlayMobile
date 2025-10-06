using Microsoft.AspNetCore.Identity;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.DependencyInjection;
using PlugPlay.Domain.Entities;
using PlugPlay.Domain.Enums;

namespace PlugPlay.Infrastructure;

public static class DataSeed
{
    public static void Seed(IServiceProvider serviceProvider)
    {
        Task.Run(async () =>
        {
            using var scope = serviceProvider.CreateScope();
            var provider = scope.ServiceProvider;
            var context = provider.GetRequiredService<PlugPlayDbContext>();
            var userManager = provider.GetRequiredService<UserManager<User>>();
            var roleManager = provider.GetRequiredService<RoleManager<IdentityRole<int>>>();

            // Roles
            var roles = new[] { "Admin", "User" };
            foreach (var roleName in roles)
            {
                if (!await roleManager.RoleExistsAsync(roleName))
                {
                    await roleManager.CreateAsync(new IdentityRole<int>(roleName));
                }
            }

            // Users (Identity)
            if (!await userManager.Users.AnyAsync())
            {
                var users = new[]
                {
                    new User
                    {
                        UserName = "admin", Email = "admin@cowork.com", PhoneNumber = "+10000000001",
                        FirstName = "Admin", LastName = "User", Role = Role.Admin, EmailConfirmed = true,
                        CreatedAt = DateTime.UtcNow, UpdatedAt = DateTime.UtcNow
                    },
                    new User
                    {
                        UserName = "user1", Email = "user1@cowork.com", PhoneNumber = "+10000000002",
                        FirstName = "John", LastName = "Doe", Role = Role.User, EmailConfirmed = true,
                        CreatedAt = DateTime.UtcNow, UpdatedAt = DateTime.UtcNow
                    },
                    new User
                    {
                        UserName = "user2", Email = "user2@cowork.com", PhoneNumber = "+10000000003",
                        FirstName = "Jane", LastName = "Smith", Role = Role.User, EmailConfirmed = true,
                        CreatedAt = DateTime.UtcNow, UpdatedAt = DateTime.UtcNow
                    },
                    new User
                    {
                        UserName = "user3", Email = "user3@cowork.com", PhoneNumber = "+10000000004",
                        FirstName = "Alex", LastName = "Johnson", Role = Role.User, EmailConfirmed = true,
                        CreatedAt = DateTime.UtcNow, UpdatedAt = DateTime.UtcNow
                    }
                };

                await userManager.CreateAsync(users[0], "Admin123!");
                await userManager.CreateAsync(users[1], "User123!");
                await userManager.CreateAsync(users[2], "User123!");
                await userManager.CreateAsync(users[3], "User123!");

                // assign Identity roles based on the User.Role enum
                foreach (var u in users)
                {
                    var created = await userManager.FindByNameAsync(u.UserName);
                    if (created != null)
                    {
                        var roleName = u.Role == Role.Admin ? "Admin" : "User";
                        if (!await userManager.IsInRoleAsync(created, roleName))
                        {
                            await userManager.AddToRoleAsync(created, roleName);
                        }
                    }
                }
            }

            var existingUsers = await userManager.Users.ToListAsync();
            var firstUserId = existingUsers.FirstOrDefault()?.Id ?? 0;

            // Categories
            if (!await context.Categories.AnyAsync())
            {
                var categories = new[]
                {
                    new Category { Id = 1, Name = "Electronics", ParentCategoryId = null },
                    new Category { Id = 2, Name = "Computers", ParentCategoryId = 1 },
                    new Category { Id = 3, Name = "Home", ParentCategoryId = null }
                };
                await context.Categories.AddRangeAsync(categories);
                await context.SaveChangesAsync();
            }

            // Attributes
            if (!await context.Attributes.AnyAsync())
            {
                var attributes = new[]
                {
                    new Domain.Entities.Attribute { Id = 1, Name = "Color", Unit = "", DataType = "string" },
                    new Domain.Entities.Attribute { Id = 2, Name = "Weight", Unit = "kg", DataType = "decimal" },
                    new Domain.Entities.Attribute { Id = 3, Name = "Dimensions", Unit = "cm", DataType = "string" }
                };
                await context.Attributes.AddRangeAsync(attributes);
                await context.SaveChangesAsync();
            }

            // Products
            if (!await context.Products.AnyAsync())
            {
                var products = new[]
                {
                    new Product
                    {
                        Id = 1,
                        CategoryId = 2,
                        Name = "Laptop Pro",
                        Description = "Powerful laptop",
                        Price = 1299.99m,
                        StockQuantity = 10,
                        CreatedAt = DateTime.UtcNow
                    },
                    new Product
                    {
                        Id = 2,
                        CategoryId = 1,
                        Name = "Wireless Headphones",
                        Description = "Noise cancelling",
                        Price = 199.99m,
                        StockQuantity = 50,
                        CreatedAt = DateTime.UtcNow
                    },
                    new Product
                    {
                        Id = 3,
                        CategoryId = 3,
                        Name = "Coffee Maker",
                        Description = "Automatic coffee maker",
                        Price = 79.99m,
                        StockQuantity = 25,
                        CreatedAt = DateTime.UtcNow
                    }
                };
                await context.Products.AddRangeAsync(products);
                await context.SaveChangesAsync();
            }

            // ProductAttributes
            if (!await context.Set<ProductAttribute>().AnyAsync())
            {
                var productAttributes = new[]
                {
                    new ProductAttribute { ProductId = 1, AttributeId = 2, Value = "1.5" }, // weight
                    new ProductAttribute { ProductId = 1, AttributeId = 3, Value = "35x24x2" }, // dimensions
                    new ProductAttribute { ProductId = 2, AttributeId = 1, Value = "Black" } // color
                };
                await context.AddRangeAsync(productAttributes);
                await context.SaveChangesAsync();
            }

            // UserAddresses
            if (firstUserId != 0 && !await context.UserAddresses.AnyAsync())
            {
                var addresses = new[]
                {
                    new UserAddress
                    {
                        Id = 1, UserId = firstUserId, Apartments = "12A", House = "10", Street = "Main St",
                        City = "Metropolis"
                    },
                    new UserAddress
                    {
                        Id = 2, UserId = firstUserId, Apartments = "5B", House = "22", Street = "2nd Ave",
                        City = "Metropolis"
                    }
                };
                await context.UserAddresses.AddRangeAsync(addresses);
                await context.SaveChangesAsync();
            }

            // Reviews
            if (!await context.Reviews.AnyAsync())
            {
                var reviews = new List<Review>
                {
                    new Review
                    {
                        Id = 1, ProductId = 1, UserId = firstUserId == 0 ? null : firstUserId, Rating = 5,
                        Comment = "Excellent laptop", CreatedAt = DateTime.UtcNow, UpdatedAt = DateTime.UtcNow
                    },
                    new Review
                    {
                        Id = 2, ProductId = 2, UserId = firstUserId == 0 ? null : firstUserId, Rating = 4,
                        Comment = "Great sound", CreatedAt = DateTime.UtcNow, UpdatedAt = DateTime.UtcNow
                    }
                };
                await context.Reviews.AddRangeAsync(reviews);
                await context.SaveChangesAsync();
            }

            // CartItems
            if (firstUserId != 0 && !await context.CartItems.AnyAsync())
            {
                var carts = new[]
                {
                    new CartItem { Id = 1, ProductId = 2, UserId = firstUserId, Quantity = 1, Total = 199.99m },
                    new CartItem { Id = 2, ProductId = 3, UserId = firstUserId, Quantity = 2, Total = 159.98m }
                };
                await context.CartItems.AddRangeAsync(carts);
                await context.SaveChangesAsync();
            }

            // WishLists
            if (firstUserId != 0 && !await context.WishLists.AnyAsync())
            {
                var wishes = new[]
                {
                    new WishList { Id = 1, UserId = firstUserId, ProductId = 1 }
                };
                await context.WishLists.AddRangeAsync(wishes);
                await context.SaveChangesAsync();
            }

            // Orders + OrderItems
            if (!await context.Orders.AnyAsync())
            {
                var order = new Order
                {
                    Id = 1,
                    UserId = firstUserId == 0 ? null : firstUserId,
                    OrderDate = DateTime.UtcNow,
                    Status = OrderStatus.Delivered,
                    TotalAmount = 1299.99m,
                    DiscountAmount = 0m,
                    PaymentMethod = PaymentMethod.Card,
                    DeliveryAddress = "123 Delivery Lane",
                    TransactionId = 1000001,
                    PaymentCreated = DateTime.UtcNow,
                    PaymentProcessed = DateTime.UtcNow,
                    PaymentFailureReason = string.Empty
                };
                await context.Orders.AddAsync(order);
                await context.SaveChangesAsync();

                var orderItems = new[]
                {
                    new OrderItem { Id = 1, OrderId = order.Id, ProductId = 1, Quantity = 1, UnitPrice = 1299.99m }
                };
                await context.OrderItems.AddRangeAsync(orderItems);
                await context.SaveChangesAsync();
            }

            context.ChangeTracker.Clear();
        }).GetAwaiter().GetResult();
    }
}
