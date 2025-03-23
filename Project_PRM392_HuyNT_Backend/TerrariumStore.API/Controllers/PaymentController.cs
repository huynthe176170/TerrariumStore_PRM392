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
    public class PaymentController : ControllerBase
    {
        private readonly AppDbContext _context;
        private readonly IMapper _mapper;

        public PaymentController(AppDbContext context, IMapper mapper)
        {
            _context = context;
            _mapper = mapper;
        }

        // 1. Lấy danh sách Payment
        [HttpGet]
        public async Task<ActionResult<IEnumerable<PaymentDTO>>> GetPayments()
        {
            var payments = await _context.Payments.Include(p => p.Order).ToListAsync();
            return Ok(_mapper.Map<IEnumerable<PaymentDTO>>(payments));
        }

        // 2. Lấy Payment theo ID
        [HttpGet("{id}")]
        public async Task<ActionResult<PaymentDTO>> GetPayment(int id)
        {
            var payment = await _context.Payments.Include(p => p.Order).FirstOrDefaultAsync(p => p.Id == id);
            if (payment == null)
                return NotFound("Payment không tồn tại.");

            return Ok(_mapper.Map<PaymentDTO>(payment));
        }

        // 3. Tạo Payment mới
        [HttpPost]
        public async Task<ActionResult<PaymentDTO>> CreatePayment(PaymentDTO paymentDto)
        {
            var order = await _context.Orders.FindAsync(paymentDto.OrderId);
            if (order == null)
                return BadRequest("Đơn hàng không tồn tại.");

            var payment = _mapper.Map<Payment>(paymentDto);
            _context.Payments.Add(payment);
            await _context.SaveChangesAsync();

            return CreatedAtAction(nameof(GetPayment), new { id = payment.Id }, _mapper.Map<PaymentDTO>(payment));
        }

        // 4. Cập nhật trạng thái thanh toán
        [HttpPut("{id}/status")]
        public async Task<IActionResult> UpdatePaymentStatus(int id, [FromBody] bool isPaid)
        {
            var payment = await _context.Payments.FindAsync(id);
            if (payment == null)
                return NotFound("Payment không tồn tại.");

            payment.IsPaid = isPaid;
            payment.PaymentDate = isPaid ? DateTime.UtcNow : null;

            await _context.SaveChangesAsync();
            return NoContent();
        }

        // 5. Xóa Payment
        [HttpDelete("{id}")]
        public async Task<IActionResult> DeletePayment(int id)
        {
            var payment = await _context.Payments.FindAsync(id);
            if (payment == null)
                return NotFound("Payment không tồn tại.");

            _context.Payments.Remove(payment);
            await _context.SaveChangesAsync();

            return NoContent();
        }
    }
}
