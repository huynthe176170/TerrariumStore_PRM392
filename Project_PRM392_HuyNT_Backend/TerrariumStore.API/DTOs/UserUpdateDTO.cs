namespace TerrariumStore.API.DTOs
{
    public class UserUpdateDTO
    {
        public string FullName { get; set; }
        public string Email { get; set; }
        public string Phone { get; set; }
        public string? Address { get; set; }

        public string? CurrentPassword { get; set; } // Dùng để xác nhận nếu đổi mật khẩu
        public string? NewPassword { get; set; } // Mật khẩu mới (nếu có)
    }
}
