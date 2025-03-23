using AutoMapper;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using System.Linq;
using TerrariumStore.API.Data;
using TerrariumStore.API.DTOs;
using TerrariumStore.API.Models;

//[Authorize]
[ApiController]
[Route("api/[controller]")]
public class ProductController : ControllerBase
{
    private readonly AppDbContext _context;
    private readonly IMapper _mapper;

    public ProductController(AppDbContext context, IMapper mapper)
    {
        _context = context;
        _mapper = mapper;
    }

    // ✅ GET: Lấy danh sách sản phẩm với filter, search và sort
    [HttpGet]
    public async Task<ActionResult<IEnumerable<ProductDTO>>> GetProducts(
        [FromQuery] int? categoryId = null,
        [FromQuery] string? searchTerm = null,
        [FromQuery] string? sortBy = null,
        [FromQuery] bool ascending = true)
    {
        var query = _context.Products.Include(p => p.Category).AsQueryable();

        // Filter by category
        if (categoryId.HasValue && categoryId > 0)
        {
            query = query.Where(p => p.CategoryId == categoryId);
        }

        // Search by name
        if (!string.IsNullOrWhiteSpace(searchTerm))
        {
            query = query.Where(p => p.Name.Contains(searchTerm) || 
                                    (p.Description != null && p.Description.Contains(searchTerm)));
        }

        // Sort by specified field
        if (!string.IsNullOrWhiteSpace(sortBy))
        {
            switch (sortBy.ToLower())
            {
                case "name":
                    query = ascending ? query.OrderBy(p => p.Name) : query.OrderByDescending(p => p.Name);
                    break;
                case "price":
                    query = ascending ? query.OrderBy(p => p.Price) : query.OrderByDescending(p => p.Price);
                    break;
                case "stock":
                    query = ascending ? query.OrderBy(p => p.StockQuantity) : query.OrderByDescending(p => p.StockQuantity);
                    break;
                case "category":
                    query = ascending ? query.OrderBy(p => p.Category.Name) : query.OrderByDescending(p => p.Category.Name);
                    break;
                default:
                    query = ascending ? query.OrderBy(p => p.Id) : query.OrderByDescending(p => p.Id);
                    break;
            }
        }
        else
        {
            // Default sort by Id
            query = query.OrderBy(p => p.Id);
        }

        var products = await query.ToListAsync();
        return Ok(_mapper.Map<IEnumerable<ProductDTO>>(products));
    }

    // ✅ GET: Lấy chi tiết sản phẩm
    [HttpGet("{id}")]
    public async Task<ActionResult<ProductDTO>> GetProduct(int id)
    {
        var product = await _context.Products.Include(p => p.Category)
                                             .FirstOrDefaultAsync(p => p.Id == id);
        if (product == null) return NotFound();

        return Ok(_mapper.Map<ProductDTO>(product));
    }

    // ✅ POST: Thêm sản phẩm mới
    [HttpPost]
    public async Task<ActionResult<ProductDTO>> CreateProduct(ProductDTO productDto)
    {
        var product = _mapper.Map<Product>(productDto);
        _context.Products.Add(product);
        await _context.SaveChangesAsync();

        return CreatedAtAction(nameof(GetProduct), new { id = product.Id }, _mapper.Map<ProductDTO>(product));
    }

    // ✅ PUT: Cập nhật sản phẩm
    [HttpPut("{id}")]
    public async Task<IActionResult> UpdateProduct(int id, ProductDTO productDto)
    {
        if (id != productDto.Id) return BadRequest("ID không khớp!");

        var product = await _context.Products.FindAsync(id);
        if (product == null) return NotFound();

        _mapper.Map(productDto, product);
        await _context.SaveChangesAsync();

        return NoContent();
    }

    // ✅ DELETE: Xóa sản phẩm
    [HttpDelete("{id}")]
    public async Task<IActionResult> DeleteProduct(int id)
    {
        var product = await _context.Products.FindAsync(id);
        if (product == null) return NotFound();

        _context.Products.Remove(product);
        await _context.SaveChangesAsync();

        return NoContent();
    }
}
