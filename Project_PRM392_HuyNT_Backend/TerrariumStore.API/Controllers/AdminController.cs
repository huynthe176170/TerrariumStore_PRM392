using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using TerrariumStore.API.Data;
using TerrariumStore.API.DTOs;
using TerrariumStore.API.Models;

namespace TerrariumStore.API.Controllers
{
    [Authorize(Roles = "Admin")]
    [ApiController]
    [Route("api/[controller]")]
    public class AdminController : ControllerBase
    {
        private readonly AppDbContext _context;

        public AdminController(AppDbContext context)
        {
            _context = context;
        }

        [HttpGet("dashboard/stats")]
        public async Task<IActionResult> GetDashboardStats()
        {
            var totalOrders = await _context.Orders.CountAsync();
            var totalRevenue = await _context.Orders.SumAsync(o => o.TotalPrice);
            var totalCustomers = await _context.Users.CountAsync(u => u.Role == "Customer");
            var totalProducts = await _context.Products.CountAsync();

            return Ok(new
            {
                TotalOrders = totalOrders,
                TotalRevenue = totalRevenue,
                TotalCustomers = totalCustomers,
                TotalProducts = totalProducts
            });
        }

        [HttpGet("dashboard/revenue")]
        public async Task<IActionResult> GetRevenueData()
        {
            // Lấy dữ liệu doanh thu theo tháng trong năm hiện tại
            var currentYear = DateTime.Now.Year;
            var revenueData = await _context.Orders
                .Where(o => o.OrderDate.Year == currentYear)
                .GroupBy(o => o.OrderDate.Month)
                .Select(g => new
                {
                    Month = g.Key,
                    Revenue = g.Sum(o => o.TotalPrice)
                })
                .OrderBy(x => x.Month)
                .ToListAsync();

            // Chuyển đổi số tháng thành tên tháng
            var result = revenueData.Select(d => new
            {
                Month = GetMonthName(d.Month),
                Revenue = d.Revenue
            });

            return Ok(result);
        }

        [HttpGet("dashboard/order-status")]
        public async Task<IActionResult> GetOrderStatusData()
        {
            var orderStatusData = await _context.Orders
                .GroupBy(o => o.Status)
                .Select(g => new
                {
                    Status = g.Key,
                    Count = g.Count()
                })
                .ToListAsync();

            return Ok(orderStatusData);
        }

        [HttpGet("dashboard/recent-orders")]
        public async Task<IActionResult> GetRecentOrders()
        {
            var recentOrders = await _context.Orders
                .Include(o => o.User)
                .OrderByDescending(o => o.OrderDate)
                .Take(10)
                .Select(o => new
                {
                    Id = o.Id,
                    CustomerName = o.User.FullName,
                    TotalPrice = o.TotalPrice,
                    Status = o.Status,
                    OrderDate = o.OrderDate
                })
                .ToListAsync();

            return Ok(recentOrders);
        }

        [HttpGet("orders/{id}")]
        public async Task<IActionResult> GetOrderDetails(int id)
        {
            var order = await _context.Orders
                .Include(o => o.User)
                .Include(o => o.OrderDetails)
                .ThenInclude(od => od.Product)
                .FirstOrDefaultAsync(o => o.Id == id);

            if (order == null)
                return NotFound();

            var orderDetails = new
            {
                Id = order.Id,
                CustomerName = order.User.FullName,
                CustomerEmail = order.User.Email,
                TotalPrice = order.TotalPrice,
                Status = order.Status,
                OrderDate = order.OrderDate,
                ShippingAddress = order.ShippingAddress ?? "Không có thông tin",
                Items = order.OrderDetails.Select(od => new
                {
                    ProductId = od.ProductId,
                    ProductName = od.Product.Name,
                    Price = od.Price,
                    Quantity = od.Quantity,
                    Subtotal = od.Price * od.Quantity
                })
            };

            return Ok(orderDetails);
        }

        [HttpPut("orders/{id}/status")]
        public async Task<IActionResult> UpdateOrderStatus(int id, [FromBody] OrderStatusUpdateDTO model)
        {
            var order = await _context.Orders.FindAsync(id);
            if (order == null)
                return NotFound();

            order.Status = model.Status;
            await _context.SaveChangesAsync();

            return NoContent();
        }

        private string GetMonthName(int month)
        {
            return new DateTime(2022, month, 1).ToString("MMMM");
        }
    }
} 