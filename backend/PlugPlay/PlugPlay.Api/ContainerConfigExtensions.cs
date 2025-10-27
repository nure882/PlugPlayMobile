using CloudinaryDotNet;

namespace PlugPlay.Api;

public static class ContainerConfigExtensions
{
    public static void RegisterAutomapper(this IServiceCollection services)
    {
        services.AddAutoMapper(AppDomain.CurrentDomain.GetAssemblies());
        services.AddAutoMapper(typeof(MappingProfile));
    }

    public static void RegisterCloudinary(
        this IServiceCollection services, string cloud, string apiKey, string apiSecret)
        => services.AddSingleton(new Cloudinary(new Account(cloud, apiKey, apiSecret)));
}
