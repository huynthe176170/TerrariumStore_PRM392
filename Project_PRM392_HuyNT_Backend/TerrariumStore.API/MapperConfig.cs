using AutoMapper;
using TerrariumStore.API.DTOs;
using TerrariumStore.API.Models;

namespace TerrariumStore.API.Profiles
{
    public class MapperConfig : Profile
    {
        public MapperConfig()
        {
            // Map Product
            CreateMap<Product, ProductDTO>()
                .ForMember(dest => dest.CategoryName, opt => opt.MapFrom(src => src.Category.Name));
            CreateMap<ProductDTO, Product>().ForMember(dest => dest.Id, opt => opt.Ignore());

            // Map Order & OrderDetail
            CreateMap<Order, OrderDTO>()
               .ForMember(dest => dest.UserName, opt => opt.MapFrom(src => src.User.FullName))
               .ReverseMap();

            CreateMap<CreateOrderDTO, Order>();
            CreateMap<CreateOrderDetailDTO, OrderDetail>();

            CreateMap<OrderDetail, OrderDetailDTO>()
               .ForMember(dest => dest.ProductName, opt => opt.MapFrom(src => src.Product.Name))
               .ForMember(dest => dest.Total, opt => opt.MapFrom(src => src.Quantity * src.Price))
               .ReverseMap();

            //User
            // Mapping từ User -> UserDTO (Dùng để trả dữ liệu ra ngoài)
            CreateMap<User, UserDTO>();

            // Mapping từ UserCreateDTO -> User (Dùng khi đăng ký)
            CreateMap<UserCreateDTO, User>()
                .ForMember(dest => dest.PasswordHash, opt => opt.Ignore()); // Không map trực tiếp password

            // Mapping từ UserUpdateDTO -> User (Dùng khi cập nhật)
            CreateMap<UserUpdateDTO, User>()
                .ForMember(dest => dest.PasswordHash, opt => opt.Ignore()); // Không map mật khẩu trực tiếp


            // Ánh xạ giữa Cart và CartDTO
            CreateMap<Cart, CartDTO>();

            // Ánh xạ giữa CartItem và CartItemDTO (bao gồm thông tin Product)
            CreateMap<CartItem, CartItemDTO>()
                .ForMember(dest => dest.Product, opt => opt.MapFrom(src => src.Product));

            // Ánh xạ từ AddCartItemDTO sang CartItem để thêm vào DB
            CreateMap<AddCartItemDTO, CartItem>();

            //category
            CreateMap<Category, CategoryDTO>().ReverseMap();
            CreateMap<CreateCategoryDTO, Category>();
            CreateMap<UpdateCategoryDTO, Category>();

            //payment
            CreateMap<Payment, PaymentDTO>().ReverseMap();
            
            //transaction
            CreateMap<Transaction, TransactionDTO>()
                .ForMember(dest => dest.PaymentMethod, opt => opt.MapFrom(src => src.Payment.PaymentMethod))
                .ForMember(dest => dest.OrderId, opt => opt.MapFrom(src => src.Payment.OrderId))
                .ForMember(dest => dest.IsPaid, opt => opt.MapFrom(src => src.Payment.IsPaid));
        }
    }
}
