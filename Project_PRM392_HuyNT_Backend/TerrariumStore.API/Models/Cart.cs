using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace TerrariumStore.API.Models
{
    public class Cart
    {
        [Key]
        public int Id { get; set; }

        [Required]
        public int UserId { get; set; }

        [ForeignKey("UserId")]
        public User User { get; set; }

        // Một giỏ hàng có nhiều sản phẩm (CartItems)
        public ICollection<CartItem> CartItems { get; set; } = new List<CartItem>();
    }
}
