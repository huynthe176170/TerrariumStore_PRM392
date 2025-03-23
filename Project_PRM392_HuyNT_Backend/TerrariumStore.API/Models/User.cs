using System.ComponentModel.DataAnnotations;

namespace TerrariumStore.API.Models
{
    public class User
    {
        [Key]
        public int Id { get; set; }

        [Required]
        [MaxLength(100)]
        public string FullName { get; set; }

        [Required]
        [EmailAddress]
        public string Email { get; set; }

        [Required]
        [MaxLength(15)]
        public string Phone { get; set; }

        public string? Address { get; set; }

        [Required]
        public string Username { get; set; }

        [Required]
        public string PasswordHash { get; set; }

        public string Role { get; set; } = "Customer"; // "Customer", "Admin"

        // Liên kết với Orders (Một User có thể có nhiều đơn hàng)
        public ICollection<Order> Orders { get; set; } = new List<Order>();
    }
}
