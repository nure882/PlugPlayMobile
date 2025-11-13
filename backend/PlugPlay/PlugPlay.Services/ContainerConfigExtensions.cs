using Microsoft.Extensions.DependencyInjection;
using PlugPlay.Services.Auth;
using PlugPlay.Services.Cart;
using PlugPlay.Services.Interfaces;
using PlugPlay.Services.Ordering;
using PlugPlay.Services.Products;
using PlugPlay.Services.Profile;
using PlugPlay.Services.Payment;

namespace PlugPlay.Services;

public static class ContainerConfigExtensions
{
    public static void RegisterServices(this IServiceCollection services)
    {
        services.AddScoped<IAuthService, AuthService>();
        services.AddScoped<ICartService, CartService>();
        services.AddScoped<IJwtService, JwtService>();
        services.AddScoped<IOrderService, OrderService>();
        services.AddScoped<IPaymentService, PaymentService>();
        services.AddScoped<IProductsService, ProductsService>();
        services.AddScoped<IUserInfoService, UserInfoService>();
    }
}
