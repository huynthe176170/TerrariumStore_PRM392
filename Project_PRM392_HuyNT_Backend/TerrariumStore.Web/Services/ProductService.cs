using System.Net.Http;
using System.Text.Json;
using System.Collections.Generic;
using System.Threading.Tasks;
using Microsoft.Extensions.Configuration;
using TerrariumStore.Web.Models;

namespace TerrariumStore.Web.Services
{
    public class ProductService
    {
        private readonly HttpClient _httpClient;
        private readonly string _apiBaseUrl;

        public ProductService(HttpClient httpClient, IConfiguration config)
        {
            _httpClient = httpClient;
            _apiBaseUrl = config["ApiBaseUrl"];
        }

        public async Task<List<Product>> GetProductsAsync()
        {
            var response = await _httpClient.GetStringAsync($"{_apiBaseUrl}/product");
            return JsonSerializer.Deserialize<List<Product>>(response,
                new JsonSerializerOptions { PropertyNameCaseInsensitive = true }) ?? new List<Product>();
        }
    }
}
