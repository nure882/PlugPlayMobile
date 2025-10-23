# Build stage
FROM mcr.microsoft.com/dotnet/sdk:8.0 AS build
WORKDIR /src

# Copy project files for restore (layer caching optimization)
COPY backend/PlugPlay/PlugPlay.Api/PlugPlay.Api.csproj backend/PlugPlay/PlugPlay.Api/
COPY backend/PlugPlay/PlugPlay.Services/PlugPlay.Services.csproj backend/PlugPlay/PlugPlay.Services/
COPY backend/PlugPlay/PlugPlay.Infrastructure/PlugPlay.Infrastructure.csproj backend/PlugPlay/PlugPlay.Infrastructure/
COPY backend/PlugPlay/PlugPlay.Domain/PlugPlay.Domain.csproj backend/PlugPlay/PlugPlay.Domain/

# Restore dependencies
RUN dotnet restore backend/PlugPlay/PlugPlay.Api/PlugPlay.Api.csproj

# Copy all source code
COPY backend/PlugPlay/PlugPlay.Api/. backend/PlugPlay/PlugPlay.Api/
COPY backend/PlugPlay/PlugPlay.Services/. backend/PlugPlay/PlugPlay.Services/
COPY backend/PlugPlay/PlugPlay.Infrastructure/. backend/PlugPlay/PlugPlay.Infrastructure/
COPY backend/PlugPlay/PlugPlay.Domain/. backend/PlugPlay/PlugPlay.Domain/

# Build and publish
WORKDIR /src/backend/PlugPlay/PlugPlay.Api
RUN dotnet publish PlugPlay.Api.csproj -c Release -o /app/publish /p:UseAppHost=false

# Runtime stage
FROM mcr.microsoft.com/dotnet/aspnet:8.0 AS runtime
WORKDIR /app

# Copy published output
COPY --from=build /app/publish .

# Configure ASP.NET Core
ENV ASPNETCORE_URLS=http://+:80 \
    ASPNETCORE_FORWARDEDHEADERS_ENABLED=true

EXPOSE 80

ENTRYPOINT ["dotnet", "PlugPlay.Api.dll"]
