using Microsoft.Extensions.Options;
using TerrariumStore.API.Models;

public class AuthService
{
    private readonly JwtSettings _jwtSettings;

    public AuthService(IOptions<JwtSettings> jwtSettings)
    {
        _jwtSettings = jwtSettings.Value;
    }

    public void PrintJwtInfo()
    {
        Console.WriteLine($"Key: {_jwtSettings.Key}");
        Console.WriteLine($"Issuer: {_jwtSettings.Issuer}");
        Console.WriteLine($"Audience: {_jwtSettings.Audience}");
    }
}
