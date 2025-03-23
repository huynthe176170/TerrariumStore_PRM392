namespace TerrariumStore.API.DTOs
{
    public class UserDTO
    {
        public int Id { get; set; }
        public string FullName { get; set; }
        public string Email { get; set; }
        public string Phone { get; set; }
        public string? Address { get; set; }
        public string Username { get; set; }
        public string Role { get; set; }
    }
}
