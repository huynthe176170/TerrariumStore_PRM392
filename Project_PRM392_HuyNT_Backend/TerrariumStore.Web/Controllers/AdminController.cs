using Microsoft.AspNetCore.Mvc;
using TerrariumStore.Web.Services;

namespace TerrariumStore.Web.Controllers
{
    public class AdminController : Controller
    {
        public IActionResult Dashboard()
        {
            return View();
        }

        public IActionResult Products()
        {
            return View();
        }

        public IActionResult Orders()
        {
            return View();
        }

        public IActionResult Users()
        {
            return View();
        }

        public IActionResult Categories()
        {
            return View();
        }

        public IActionResult Payments()
        {
            return View();
        }

        public IActionResult Transactions()
        {
            return View();
        }

        public IActionResult OrderDetails(int id)
        {
            ViewBag.OrderId = id;
            return View();
        }

        public IActionResult EditProduct(int? id)
        {
            ViewBag.ProductId = id;
            return View();
        }
    }
}