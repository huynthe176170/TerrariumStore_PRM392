using Microsoft.AspNetCore.Mvc;
using TerrariumStore.Web.Models;

namespace TerrariumStore.Web.Controllers
{
    public class HomeController : Controller
    {
        public IActionResult Index()
        {
            return View();
        }

        public IActionResult Login()
        {
            return View();
        }

        public IActionResult Register()
        {
            return View();
        }

        public IActionResult Logout()
        {
            // Xóa token sẽ được xử lý ở phía client bằng JavaScript
            return RedirectToAction("Login");
        }

        public IActionResult Cart()
        {
            return View();
        }
    }
}