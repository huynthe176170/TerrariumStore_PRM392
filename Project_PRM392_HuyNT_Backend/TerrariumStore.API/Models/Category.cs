using System.ComponentModel.DataAnnotations;

namespace TerrariumStore.API.Models
{
    public class Category
    {
        [Key]
        public int Id { get; set; }

        [Required]
        [MaxLength(100)]
        public string Name { get; set; }

        public string? Description { get; set; }

        // Một danh mục có nhiều sản phẩm
        public ICollection<Product> Products { get; set; } = new List<Product>();
    }
}
