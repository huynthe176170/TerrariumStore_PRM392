using AutoMapper;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using TerrariumStore.API.Data;
using TerrariumStore.API.DTOs;
using TerrariumStore.API.Models;

namespace TerrariumStore.API.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class CartController : ControllerBase
    {
        private readonly AppDbContext _context;
        private readonly IMapper _mapper;

        public CartController(AppDbContext context, IMapper mapper)
        {
            _context = context;
            _mapper = mapper;
        }

        // API lấy giỏ hàng của user
        [HttpGet("{userId}")]
        public async Task<ActionResult<CartDTO>> GetCart(int userId)
        {
            var cart = await _context.Carts
                .Include(c => c.CartItems)
                    .ThenInclude(ci => ci.Product)
                        .ThenInclude(p => p.Category)
                .FirstOrDefaultAsync(c => c.UserId == userId);

            if (cart == null)
                return NotFound("Giỏ hàng không tồn tại.");

            var cartDto = _mapper.Map<CartDTO>(cart);
            return Ok(cartDto);
        }

        // API thêm sản phẩm vào giỏ hàng
        [HttpPost("add")] // /api/cart/add
        public async Task<IActionResult> AddToCart(AddCartItemDTO addToCartDto)

        {
            var cart = await _context.Carts
                .Include(c => c.CartItems)
                .FirstOrDefaultAsync(c => c.UserId == addToCartDto.UserId);

            if (cart == null)
            {
                cart = new Cart { UserId = addToCartDto.UserId, CartItems = new List<CartItem>() };
                _context.Carts.Add(cart);
            }

            var product = await _context.Products.FindAsync(addToCartDto.ProductId);
            if (product == null)
                return BadRequest("Sản phẩm không tồn tại.");

            var cartItem = cart.CartItems.FirstOrDefault(ci => ci.ProductId == addToCartDto.ProductId);
            if (cartItem != null)
            {
                cartItem.Quantity += addToCartDto.Quantity;
            }
            else
            {
                cart.CartItems.Add(new CartItem
                {
                    ProductId = addToCartDto.ProductId,
                    Quantity = addToCartDto.Quantity,
                    Price = product.Price
                });
            }

            await _context.SaveChangesAsync();
            return Ok("Thêm sản phẩm vào giỏ hàng thành công.");
        }

        // API thanh toán giỏ hàng với địa chỉ giao hàng tùy chỉnh
        [HttpPost("checkout")]
        public async Task<IActionResult> CheckoutCartWithAddress(CheckoutDTO checkoutDto)
        {
            if (string.IsNullOrEmpty(checkoutDto.ShippingAddress))
                return BadRequest("Vui lòng cung cấp địa chỉ giao hàng.");

            var cart = await _context.Carts
                .Include(c => c.CartItems)
                .ThenInclude(ci => ci.Product)
                .FirstOrDefaultAsync(c => c.UserId == checkoutDto.UserId);

            if (cart == null || !cart.CartItems.Any())
                return BadRequest("Giỏ hàng trống.");

            // Kiểm tra số lượng sản phẩm trong kho
            foreach (var item in cart.CartItems)
            {
                if (item.Quantity > item.Product.StockQuantity)
                {
                    return BadRequest($"Sản phẩm {item.Product.Name} không đủ hàng. Chỉ còn {item.Product.StockQuantity} sản phẩm.");
                }
            }

            // Tạo đơn hàng
            var order = new Order
            {
                UserId = checkoutDto.UserId,
                OrderDate = DateTime.UtcNow,
                Status = "Pending",
                ShippingAddress = checkoutDto.ShippingAddress, // Sử dụng địa chỉ giao hàng từ request
                OrderDetails = cart.CartItems.Select(ci => new OrderDetail
                {
                    ProductId = ci.ProductId,
                    Quantity = ci.Quantity,
                    Price = ci.Price
                }).ToList(),
                TotalPrice = cart.CartItems.Sum(ci => ci.Price * ci.Quantity)
            };

            // Trừ số lượng sản phẩm trong kho
            foreach (var item in cart.CartItems)
            {
                item.Product.StockQuantity -= item.Quantity;
            }

            _context.Orders.Add(order);
            _context.Carts.Remove(cart);
            await _context.SaveChangesAsync();

            return Ok("Thanh toán thành công, đơn hàng đã được tạo.");
        }

        // API thanh toán giỏ hàng (sử dụng địa chỉ từ thông tin người dùng)
        [HttpPost("checkout/{userId}")]
        public async Task<IActionResult> CheckoutCart(int userId)
        {
            var cart = await _context.Carts
                .Include(c => c.CartItems)
                .ThenInclude(ci => ci.Product)
                .FirstOrDefaultAsync(c => c.UserId == userId);

            if (cart == null || !cart.CartItems.Any())
                return BadRequest("Giỏ hàng trống.");

            // Lấy thông tin người dùng để lấy địa chỉ giao hàng
            var user = await _context.Users.FindAsync(userId);
            if (user == null)
                return BadRequest("Không tìm thấy thông tin người dùng.");

            // Kiểm tra nếu người dùng không có địa chỉ
            if (string.IsNullOrEmpty(user.Address))
                return BadRequest("Vui lòng cập nhật địa chỉ giao hàng trong thông tin cá nhân trước khi thanh toán.");

            // Kiểm tra số lượng sản phẩm trong kho
            foreach (var item in cart.CartItems)
            {
                if (item.Quantity > item.Product.StockQuantity)
                {
                    return BadRequest($"Sản phẩm {item.Product.Name} không đủ hàng. Chỉ còn {item.Product.StockQuantity} sản phẩm.");
                }
            }

            // Tạo đơn hàng
            var order = new Order
            {
                UserId = userId,
                OrderDate = DateTime.UtcNow,
                Status = "Pending",
                ShippingAddress = user.Address, // Sử dụng địa chỉ của người dùng làm địa chỉ giao hàng
                OrderDetails = cart.CartItems.Select(ci => new OrderDetail
                {
                    ProductId = ci.ProductId,
                    Quantity = ci.Quantity,
                    Price = ci.Price
                }).ToList(),
                TotalPrice = cart.CartItems.Sum(ci => ci.Price * ci.Quantity)
            };

            // Trừ số lượng sản phẩm trong kho
            foreach (var item in cart.CartItems)
            {
                item.Product.StockQuantity -= item.Quantity;
            }

            _context.Orders.Add(order);
            _context.Carts.Remove(cart);
            await _context.SaveChangesAsync();

            return Ok("Thanh toán thành công, đơn hàng đã được tạo.");
        }
    }
}
