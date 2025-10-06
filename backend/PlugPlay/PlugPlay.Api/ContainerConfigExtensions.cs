namespace PlugPlay.Api;

public static class ContainerConfigExtensions
{
    public static void RegisterAutomapper(this IServiceCollection services)
    {
        services.AddAutoMapper(AppDomain.CurrentDomain.GetAssemblies());
        services.AddAutoMapper(typeof(MappingProfile));
    }
}
