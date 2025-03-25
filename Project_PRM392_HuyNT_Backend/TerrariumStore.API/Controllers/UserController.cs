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

            // Kiểm tra xem người dùng đã đăng nhập chưa
            var userIdClaim = User.FindFirst(ClaimTypes.NameIdentifier);
            var userRoleClaim = User.FindFirst(ClaimTypes.Role);
            
            // Nếu không có claims (chưa đăng nhập hoặc token không hợp lệ)
            if (userIdClaim == null)
            {
                return Unauthorized("Không có quyền truy cập, vui lòng đăng nhập.");
            }

            int userId;
            if (!int.TryParse(userIdClaim.Value, out userId))
            {
                return BadRequest("Token không hợp lệ.");
            }
            
            var userRole = userRoleClaim?.Value;

            // Kiểm tra quyền truy cập: chỉ Admin hoặc chính chủ mới được xem
            if (userId != id && userRole != "Admin")
            {
                return Forbid("Bạn không có quyền xem thông tin của người dùng khác.");
            }

            return Ok(_mapper.Map<UserDTO>(user));
        }

        // Chỉnh sửa thông tin người dùng (Chỉ admin hoặc chính chủ)
        [HttpPut("{id}")]
        public async Task<IActionResult> UpdateUser(int id, UserUpdateDTO userUpdate)
        {
            var user = await _context.Users.FindAsync(id);
            if (user == null) return NotFound("Người dùng không tồn tại.");

            // Kiểm tra xem người dùng đã đăng nhập chưa
            var userIdClaim = User.FindFirst(ClaimTypes.NameIdentifier);
            var userRoleClaim = User.FindFirst(ClaimTypes.Role);
            
            // Nếu không có claims (chưa đăng nhập hoặc token không hợp lệ)
            if (userIdClaim == null)
            {
                return Unauthorized("Không có quyền truy cập, vui lòng đăng nhập.");
            }

            int userId;
            if (!int.TryParse(userIdClaim.Value, out userId))
            {
                return BadRequest("Token không hợp lệ.");
            }
            
            var userRole = userRoleClaim?.Value;

            // Kiểm tra quyền truy cập: chỉ Admin hoặc chính chủ mới được chỉnh sửa
            if (userId != id && userRole != "Admin")
            {
                return Forbid("Bạn không có quyền chỉnh sửa thông tin của người dùng khác.");
            }

            user.FullName = userUpdate.FullName;
            user.Email = userUpdate.Email;
            user.Phone = userUpdate.Phone;
            user.Address = userUpdate.Address;

            if (!string.IsNullOrEmpty(userUpdate.NewPassword))
            {
                if (!string.IsNullOrEmpty(userUpdate.CurrentPassword) && !BCrypt.Net.BCrypt.Verify(userUpdate.CurrentPassword, user.PasswordHash))
                    return BadRequest("Mật khẩu cũ không đúng.");

                user.PasswordHash = BCrypt.Net.BCrypt.HashPassword(userUpdate.NewPassword);
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
