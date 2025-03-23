namespace TerrariumStore.API.DTOs
{
    public class TransactionDTO
    {
        public int Id { get; set; }
        public int PaymentId { get; set; }
        public string TransactionId { get; set; }
        public decimal Amount { get; set; }
        public DateTime CreatedAt { get; set; }
        
        // Thông tin bổ sung
        public string PaymentMethod { get; set; }
        public int OrderId { get; set; }
        public bool IsPaid { get; set; }
    }

    public class CreateTransactionDTO
    {
        public int PaymentId { get; set; }
        public string TransactionId { get; set; }
        public decimal Amount { get; set; }
    }
} 