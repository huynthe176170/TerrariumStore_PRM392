using AutoMapper;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using System;
using System.Linq;
using TerrariumStore.API.Data;
using TerrariumStore.API.DTOs;
using TerrariumStore.API.Models;

namespace TerrariumStore.API.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class OrderController : ControllerBase
    {
        private readonly AppDbContext _context;
        private readonly IMapper _mapper;

        public OrderController(AppDbContext context, IMapper mapper)
        {
            _context = context;
            _mapper = mapper;
        }

        // 1. Lấy danh sách đơn hàng với filter, search và sort
        [HttpGet]
        public async Task<ActionResult<IEnumerable<OrderDTO>>> GetOrders(
            [FromQuery] string? status = null,
            [FromQuery] DateTime? fromDate = null,
            [FromQuery] DateTime? toDate = null,
            [FromQuery] string? searchTerm = null,
            [FromQuery] string? sortBy = null,
            [FromQuery] bool ascending = true)
        {
            var query = _context.Orders
                .Include(o => o.User)
                .Include(o => o.OrderDetails)
                .ThenInclude(od => od.Product)
                .AsQueryable();

            // Filter by status
            if (!string.IsNullOrWhiteSpace(status) && status.ToLower() != "all")
            {
                query = query.Where(o => o.Status == status);
            }

            // Filter by date range
            if (fromDate.HasValue)
            {
                query = query.Where(o => o.OrderDate >= fromDate.Value);
            }

            if (toDate.HasValue)
            {
                // Add one day to include orders from the end date
                var nextDay = toDate.Value.AddDays(1);
                query = query.Where(o => o.OrderDate < nextDay);
            }

            // Search by customer name or order ID
            if (!string.IsNullOrWhiteSpace(searchTerm))
            {
                // Try to parse as order ID
                if (int.TryParse(searchTerm, out int orderId))
                {
                    query = query.Where(o => o.Id == orderId);
                }
                else
                {
                    // Search by customer name
                    query = query.Where(o => o.User.FullName.Contains(searchTerm) || 
                                           o.User.Username.Contains(searchTerm));
                }
            }

            // Sort by specified field
            if (!string.IsNullOrWhiteSpace(sortBy))
            {
                switch (sortBy.ToLower())
                {
                    case "date":
                        query = ascending ? query.OrderBy(o => o.OrderDate) : query.OrderByDescending(o => o.OrderDate);
                        break;
                    case "status":
                        query = ascending ? query.OrderBy(o => o.Status) : query.OrderByDescending(o => o.Status);
                        break;
                    case "customer":
                        query = ascending ? query.OrderBy(o => o.User.FullName) : query.OrderByDescending(o => o.User.FullName);
                        break;
                    case "total":
                        query = ascending ? query.OrderBy(o => o.TotalPrice) : query.OrderByDescending(o => o.TotalPrice);
                        break;
                    default:
                        query = ascending ? query.OrderBy(o => o.Id) : query.OrderByDescending(o => o.Id);
                        break;
                }
            }
            else
            {
                // Default sort by order date (newest first)
                query = query.OrderByDescending(o => o.OrderDate);
            }

            var orders = await query.ToListAsync();
            return Ok(_mapper.Map<IEnumerable<OrderDTO>>(orders));
        }

        // 2. Lấy chi tiết đơn hàng theo ID
        [HttpGet("{id}")]
        public async Task<ActionResult<OrderDTO>> GetOrder(int id)
        {
            var order = await _context.Orders
                .Include(o => o.User)
                .Include(o => o.OrderDetails)
                .ThenInclude(od => od.Product)
                .FirstOrDefaultAsync(o => o.Id == id);

            if (order == null)
                return NotFound("Đơn hàng không tồn tại.");

            return Ok(_mapper.Map<OrderDTO>(order));
        }

        // 3. Tạo đơn hàng mới
        [HttpPost]
        public async Task<ActionResult<OrderDTO>> CreateOrder(CreateOrderDTO createOrderDto)
        {
            // Kiểm tra user có tồn tại không
            var user = await _context.Users.FindAsync(createOrderDto.UserId);
            if (user == null)
                return BadRequest("Người dùng không tồn tại.");

            // Tạo đơn hàng mới
            var order = new Order
            {
                UserId = createOrderDto.UserId,
                OrderDate = DateTime.UtcNow,
                Status = "Pending",
                ShippingAddress = createOrderDto.ShippingAddress,
                TotalPrice = 0 // Sẽ tính tổng sau
            };

            // Thêm chi tiết đơn hàng
            foreach (var item in createOrderDto.OrderDetails)
            {
                var product = await _context.Products.FindAsync(item.ProductId);
                if (product == null)
                    return BadRequest($"Sản phẩm với ID {item.ProductId} không tồn tại.");

                order.OrderDetails.Add(new OrderDetail
                {
                    ProductId = item.ProductId,
                    Quantity = item.Quantity,
                    Price = product.Price
                });

                // Cập nhật tổng tiền
                order.TotalPrice += product.Price * item.Quantity;
            }

            _context.Orders.Add(order);
            await _context.SaveChangesAsync();

            return CreatedAtAction(nameof(GetOrder), new { id = order.Id }, _mapper.Map<OrderDTO>(order));
        }

        // 4. Cập nhật trạng thái đơn hàng
        [HttpPut("{id}/status")]
        public async Task<ActionResult<OrderDTO>> UpdateOrderStatus(int id, [FromBody] string status)
        {
            var order = await _context.Orders.FindAsync(id);
            if (order == null)
                return NotFound("Đơn hàng không tồn tại.");

            // Cập nhật trạng thái
            order.Status = status;
            await _context.SaveChangesAsync();

            // Lấy đơn hàng đã cập nhật
            var updatedOrder = await _context.Orders
                .Include(o => o.User)
                .Include(o => o.OrderDetails)
                .ThenInclude(od => od.Product)
                .FirstOrDefaultAsync(o => o.Id == id);

            return Ok(_mapper.Map<OrderDTO>(updatedOrder));
        }

        // 5. Xóa đơn hàng
        [HttpDelete("{id}")]
        public async Task<IActionResult> DeleteOrder(int id)
        {
            var order = await _context.Orders
                .Include(o => o.OrderDetails)
                .FirstOrDefaultAsync(o => o.Id == id);

            if (order == null)
                return NotFound("Đơn hàng không tồn tại.");

            _context.OrderDetails.RemoveRange(order.OrderDetails);
            _context.Orders.Remove(order);
            await _context.SaveChangesAsync();

            return NoContent();
        }

        // 6. Cập nhật đơn hàng
        [HttpPut("{id}")]
        public async Task<IActionResult> UpdateOrder(int id, [FromBody] OrderUpdateDTO updateDto)
        {
            var order = await _context.Orders.FindAsync(id);
            if (order == null)
                return NotFound("Đơn hàng không tồn tại.");

            // Cập nhật thông tin đơn hàng
            order.Status = updateDto.Status;
            order.ShippingAddress = updateDto.ShippingAddress;

            await _context.SaveChangesAsync();
            return NoContent();
        }
    }
}
