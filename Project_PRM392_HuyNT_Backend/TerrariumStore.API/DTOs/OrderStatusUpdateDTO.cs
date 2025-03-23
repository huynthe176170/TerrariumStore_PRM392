using System.ComponentModel.DataAnnotations;

namespace TerrariumStore.API.DTOs
{
    public class OrderStatusUpdateDTO
    {
        [Required]
        public string Status { get; set; }
    }
} 