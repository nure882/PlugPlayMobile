using Microsoft.Extensions.DependencyInjection;
using PlugPlay.Services.Auth;
using PlugPlay.Services.Interfaces;

namespace PlugPlay.Services;

public static class ContainerConfigExtensions
{
    public static void RegisterServices(this IServiceCollection services)
    {
        services.AddScoped<IAuthService, AuthService>();
        // services.AddScoped<IPaymentService, PaymentService>();
        services.AddScoped<IJwtService, JwtService>();
    }
}
