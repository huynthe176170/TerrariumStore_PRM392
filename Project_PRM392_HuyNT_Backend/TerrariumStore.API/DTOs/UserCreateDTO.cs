﻿namespace TerrariumStore.API.DTOs
{
    public class UserCreateDTO
    {
        public string FullName { get; set; }
        public string Email { get; set; }
        public string Phone { get; set; }
        public string? Address { get; set; }
        public string Username { get; set; }
        public string Password { get; set; }
    }
}
