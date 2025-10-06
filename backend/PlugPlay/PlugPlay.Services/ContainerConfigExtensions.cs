using Microsoft.Extensions.DependencyInjection;

namespace PlugPlay.Services;

public static class ContainerConfigExtensions
{
    public static void RegisterServices(this IServiceCollection services)
    {
        // services.AddScoped<IAuthService, AuthService>();
        // services.AddScoped<IPaymentService, PaymentService>();
        // services.AddScoped<IJwtService, JwtService>();
    }
}
