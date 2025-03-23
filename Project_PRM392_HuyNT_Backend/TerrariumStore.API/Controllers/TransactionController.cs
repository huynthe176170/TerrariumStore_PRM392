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
    public class TransactionController : ControllerBase
    {
        private readonly AppDbContext _context;
        private readonly IMapper _mapper;

        public TransactionController(AppDbContext context, IMapper mapper)
        {
            _context = context;
            _mapper = mapper;
        }

        // 1. Lấy danh sách Transaction
        [HttpGet]
        public async Task<ActionResult<IEnumerable<TransactionDTO>>> GetTransactions()
        {
            var transactions = await _context.Transactions
                .Include(t => t.Payment)
                .ThenInclude(p => p.Order)
                .ToListAsync();
            return Ok(_mapper.Map<IEnumerable<TransactionDTO>>(transactions));
        }

        // 2. Lấy Transaction theo ID
        [HttpGet("{id}")]
        public async Task<ActionResult<TransactionDTO>> GetTransaction(int id)
        {
            var transaction = await _context.Transactions
                .Include(t => t.Payment)
                .ThenInclude(p => p.Order)
                .FirstOrDefaultAsync(t => t.Id == id);

            if (transaction == null)
                return NotFound("Giao dịch không tồn tại.");

            return Ok(_mapper.Map<TransactionDTO>(transaction));
        }

        // 3. Lấy Transaction theo PaymentId
        [HttpGet("payment/{paymentId}")]
        public async Task<ActionResult<IEnumerable<TransactionDTO>>> GetTransactionsByPayment(int paymentId)
        {
            var payment = await _context.Payments.FindAsync(paymentId);
            if (payment == null)
                return NotFound("Thanh toán không tồn tại.");

            var transactions = await _context.Transactions
                .Include(t => t.Payment)
                .Where(t => t.PaymentId == paymentId)
                .ToListAsync();

            return Ok(_mapper.Map<IEnumerable<TransactionDTO>>(transactions));
        }

        // 4. Tạo Transaction mới
        [HttpPost]
        public async Task<ActionResult<TransactionDTO>> CreateTransaction(CreateTransactionDTO transactionDto)
        {
            var payment = await _context.Payments.FindAsync(transactionDto.PaymentId);
            if (payment == null)
                return BadRequest("Thanh toán không tồn tại.");

            var transaction = new Transaction
            {
                PaymentId = transactionDto.PaymentId,
                TransactionId = transactionDto.TransactionId,
                Amount = transactionDto.Amount,
                CreatedAt = DateTime.UtcNow
            };

            _context.Transactions.Add(transaction);
            await _context.SaveChangesAsync();

            // Cập nhật trạng thái thanh toán
            payment.IsPaid = true;
            payment.PaymentDate = DateTime.UtcNow;
            await _context.SaveChangesAsync();

            return CreatedAtAction(nameof(GetTransaction), new { id = transaction.Id }, _mapper.Map<TransactionDTO>(transaction));
        }

        // 5. Xóa Transaction
        [HttpDelete("{id}")]
        public async Task<IActionResult> DeleteTransaction(int id)
        {
            var transaction = await _context.Transactions.FindAsync(id);
            if (transaction == null)
                return NotFound("Giao dịch không tồn tại.");

            _context.Transactions.Remove(transaction);
            await _context.SaveChangesAsync();

            return NoContent();
        }
    }
} 