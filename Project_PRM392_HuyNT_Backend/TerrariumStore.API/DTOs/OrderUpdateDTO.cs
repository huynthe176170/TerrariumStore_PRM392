public class OrderUpdateDTO
{
    public int UserId { get; set; }
    public DateTime OrderDate { get; set; }
    public decimal TotalPrice { get; set; }
    public string Status { get; set; }
    public string ShippingAddress { get; set; }
}
