namespace TerrariumStore.API.DTOs
{
    public class OrderDetailDTO
    {
        public int Id { get; set; }
        public int ProductId { get; set; }
        public string? ProductName { get; set; } // Thêm tên sản phẩm
        public int Quantity { get; set; }
        public decimal Price { get; set; } // Giá tại thời điểm mua
        public decimal Total => Price * Quantity; // Tính tổng tiền của sản phẩm trong đơn
    }
}
