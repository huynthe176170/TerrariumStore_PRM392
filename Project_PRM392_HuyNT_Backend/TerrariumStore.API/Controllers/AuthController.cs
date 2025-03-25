using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using Microsoft.IdentityModel.Tokens;
using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using System.Text;
using TerrariumStore.API.Data;
using TerrariumStore.API.DTOs;
using TerrariumStore.API.Models;
using Microsoft.AspNetCore.Authorization;
using AutoMapper;

namespace TerrariumStore.API.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class AuthController : ControllerBase
    {
        private readonly AppDbContext _context;
        private readonly IConfiguration _config;
        private readonly IMapper _mapper;

        public AuthController(AppDbContext context, IConfiguration config, IMapper mapper)
        {
            _context = context;
            _config = config;
            _mapper = mapper;
        }

        // 1. Đăng ký
        [HttpPost("register")]
        public async Task<IActionResult> Register(UserCreateDTO userDto)
        {
            if (await _context.Users.AnyAsync(u => u.Username == userDto.Username))
                return BadRequest("Tên đăng nhập đã tồn tại.");

            var user = new User
            {
                FullName = userDto.FullName,
                Email = userDto.Email,
                Phone = userDto.Phone,
                Address = userDto.Address,
                Username = userDto.Username,
                PasswordHash = BCrypt.Net.BCrypt.HashPassword(userDto.Password),
                Role = "Customer"
            };

            _context.Users.Add(user);
            await _context.SaveChangesAsync();

            return CreatedAtAction(nameof(Register), new { id = user.Id }, new { user.Id, user.FullName, user.Email, user.Username });
        }

        // 2. Đăng nhập
        [HttpPost("login")]
        public async Task<IActionResult> Login(UserLoginDTO loginDto)
        {
            var user = await _context.Users.SingleOrDefaultAsync(u => u.Username == loginDto.Username);
            if (user == null || !BCrypt.Net.BCrypt.Verify(loginDto.Password, user.PasswordHash))
                return Unauthorized("Sai tên đăng nhập hoặc mật khẩu.");

            string token = GenerateJwtToken(user);
            return Ok(new { token, user.Id, user.Username, user.Role });
        }

        // Lấy thông tin profile của người dùng đã đăng nhập
        [HttpGet("profile")]
        [Authorize]
        public async Task<ActionResult<UserDTO>> GetProfile()
        {
            var userIdClaim = User.FindFirst(ClaimTypes.NameIdentifier);
            if (userIdClaim == null)
            {
                return Unauthorized("Bạn cần đăng nhập để xem thông tin profile.");
            }

            int userId;
            if (!int.TryParse(userIdClaim.Value, out userId))
            {
                return BadRequest("Token không hợp lệ.");
            }

            var user = await _context.Users.FindAsync(userId);
            if (user == null)
            {
                return NotFound("Không tìm thấy thông tin người dùng.");
            }

            return Ok(_mapper.Map<UserDTO>(user));
        }

        private string GenerateJwtToken(User user)
        {
            var claims = new List<Claim>
    {
        new Claim(ClaimTypes.NameIdentifier, user.Id.ToString()),
        new Claim(ClaimTypes.Name, user.Username),
        new Claim(ClaimTypes.Role, user.Role) // 👈 Thêm role vào token
    };

            var key = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(_config["Jwt:Key"])); 
            var creds = new SigningCredentials(key, SecurityAlgorithms.HmacSha256);

            var token = new JwtSecurityToken(
                issuer: _config["Jwt:Issuer"], 
                audience: _config["Jwt:Audience"], 
                claims: claims,
                expires: DateTime.UtcNow.AddHours(1),
                signingCredentials: creds
            );

            return new JwtSecurityTokenHandler().WriteToken(token);
        }


    }
}
