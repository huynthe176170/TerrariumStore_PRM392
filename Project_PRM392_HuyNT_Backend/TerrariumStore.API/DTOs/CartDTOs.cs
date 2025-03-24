using System.Collections.Generic;

namespace TerrariumStore.API.DTOs
{
    public class CartDTO
    {
        public int Id { get; set; }
        public int UserId { get; set; }
        public List<CartItemDTO> CartItems { get; set; } = new();
    }

    public class CartItemDTO
    {
        public int Id { get; set; }
        public int ProductId { get; set; }
        public ProductDTO Product { get; set; }
        public int Quantity { get; set; }
        public decimal Price { get; set; }
    }

    public class AddCartItemDTO
    {
        public int UserId { get; set; }
        public int ProductId { get; set; }
        public int Quantity { get; set; }
        public decimal Price { get; set; }
    }

    public class CheckoutDTO
    {
        public int UserId { get; set; }
        public string ShippingAddress { get; set; }
    }
}
