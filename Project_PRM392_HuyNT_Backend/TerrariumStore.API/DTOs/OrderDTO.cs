using System;
using System.Collections.Generic;

namespace TerrariumStore.API.DTOs
{
    public class OrderDTO
    {
        public int Id { get; set; }
        public int UserId { get; set; }
        public string UserName { get; set; } // Thêm UserName vào đây
        public DateTime OrderDate { get; set; } = DateTime.UtcNow;
        public string Status { get; set; } = "Pending";
        public decimal TotalPrice { get; set; }
        public string ShippingAddress { get; set; }
        public List<OrderDetailDTO> OrderDetails { get; set; } = new List<OrderDetailDTO>();
    }

    public class CreateOrderDTO
    {
        public int UserId { get; set; }
        public string ShippingAddress { get; set; }
        public List<CreateOrderDetailDTO> OrderDetails { get; set; } = new List<CreateOrderDetailDTO>();
    }

    public class CreateOrderDetailDTO
    {
        public int ProductId { get; set; }
        public int Quantity { get; set; }
        public decimal Price { get; set; }
    }
}
