var builder = WebApplication.CreateBuilder(args);



// Thêm hỗ trợ Controllers + Views
builder.Services.AddControllersWithViews();

// Xóa hoặc comment các dịch vụ không cần thiết
// builder.Services.AddScoped<ProductService>();

var app = builder.Build();

// Middleware cấu hình
if (!app.Environment.IsDevelopment())
{
    app.UseExceptionHandler("/Home/Error");
    app.UseHsts();
}

app.UseHttpsRedirection();
app.UseStaticFiles();

app.UseRouting();
// app.UseAuthorization(); // Xóa nếu không cần xác thực ở server-side

// Định nghĩa route mặc định để điều hướng về /Home/Login
app.MapControllerRoute(
    name: "default",
    pattern: "{controller=Home}/{action=Login}/{id?}");

app.Run();