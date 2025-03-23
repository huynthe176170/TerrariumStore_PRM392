using AutoMapper;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using System.Security.Claims;
using TerrariumStore.API.Data;
using TerrariumStore.API.DTOs;
using TerrariumStore.API.Models;

namespace TerrariumStore.API.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    //[Authorize]
    public class UserController : ControllerBase
    {
        private readonly AppDbContext _context;
        private readonly IMapper _mapper;

        public UserController(AppDbContext context, IMapper mapper)
        {
            _context = context;
            _mapper = mapper;
        }

        // Lấy danh sách user (Chỉ Admin được xem)
        [HttpGet]
        [Authorize(Roles = "Admin")]
        public async Task<ActionResult<IEnumerable<UserDTO>>> GetUsers()
        {
            var users = await _context.Users.ToListAsync();
            return Ok(_mapper.Map<IEnumerable<UserDTO>>(users));
        }

        // Lấy thông tin user theo ID (Chỉ Admin hoặc chính chủ)
        [HttpGet("{id}")]
        public async Task<ActionResult<UserDTO>> GetUser(int id)
        {
            var user = await _context.Users.FindAsync(id);
            if (user == null) return NotFound("Người dùng không tồn tại.");

            var userId = int.Parse(User.FindFirst(ClaimTypes.NameIdentifier)?.Value);
            var userRole = User.FindFirst(ClaimTypes.Role)?.Value;

            if (userId != id && userRole != "Admin")
                return Forbid();

            return Ok(_mapper.Map<UserDTO>(user));
        }

        // Cập nhật thông tin user (Chỉ user tự cập nhật hoặc admin)
        [HttpPut("{id}")]
        public async Task<IActionResult> UpdateUser(int id, UserUpdateDTO updateDto)
        {
            var user = await _context.Users.FindAsync(id);
            if (user == null) return NotFound("Người dùng không tồn tại.");

            var userId = int.Parse(User.FindFirst(ClaimTypes.NameIdentifier)?.Value);
            var userRole = User.FindFirst(ClaimTypes.Role)?.Value;

            if (userId != id && userRole != "Admin")
                return Forbid();

            user.FullName = updateDto.FullName;
            user.Email = updateDto.Email;
            user.Phone = updateDto.Phone;
            user.Address = updateDto.Address;

            if (!string.IsNullOrEmpty(updateDto.NewPassword))
            {
                if (!string.IsNullOrEmpty(updateDto.CurrentPassword) && !BCrypt.Net.BCrypt.Verify(updateDto.CurrentPassword, user.PasswordHash))
                    return BadRequest("Mật khẩu cũ không đúng.");

                user.PasswordHash = BCrypt.Net.BCrypt.HashPassword(updateDto.NewPassword);
            }

            await _context.SaveChangesAsync();
            return NoContent();
        }

        // Xóa user (Chỉ Admin)
        [HttpDelete("{id}")]
        [Authorize(Roles = "Admin")]
        public async Task<IActionResult> DeleteUser(int id)
        {
            var user = await _context.Users.FindAsync(id);
            if (user == null) return NotFound("Người dùng không tồn tại.");

            _context.Users.Remove(user);
            await _context.SaveChangesAsync();
            return NoContent();
        }
    }
}
