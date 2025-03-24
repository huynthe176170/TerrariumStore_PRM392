package com.example.project_terrarium_prm392.ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.project_terrarium_prm392.R;
import com.example.project_terrarium_prm392.models.CartItem;
import com.example.project_terrarium_prm392.models.Product;
import com.example.project_terrarium_prm392.repository.CartRepository;
import com.example.project_terrarium_prm392.ui.auth.LoginActivity;
import com.example.project_terrarium_prm392.utils.TokenManager;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<Product> products;
    private Context context;
    private OnProductClickListener onProductClickListener;

    public ProductAdapter(Context context) {
        this.context = context;
        this.products = new ArrayList<>();
    }

    public void setProducts(List<Product> products) {
        this.products = products;
        notifyDataSetChanged();
    }

    public void setOnProductClickListener(OnProductClickListener listener) {
        this.onProductClickListener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = products.get(position);
        holder.bind(product);
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageViewProduct;
        private final TextView textViewProductName;
        private final TextView textViewPrice;
        private final Button buttonAddToCart;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewProduct = itemView.findViewById(R.id.imageViewProduct);
            textViewProductName = itemView.findViewById(R.id.textViewProductName);
            textViewPrice = itemView.findViewById(R.id.textViewPrice);
            buttonAddToCart = itemView.findViewById(R.id.buttonAddToCart);
        }

        public void bind(Product product) {
            textViewProductName.setText(product.getName());
            textViewPrice.setText(formatPrice(product.getPrice()));

            // Load product image using Glide
            if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                Picasso.get()
                        .load(product.getImageUrl())
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .error(R.drawable.ic_launcher_foreground)
                        .into(imageViewProduct);
            } else {
                imageViewProduct.setImageResource(R.drawable.ic_launcher_foreground);
            }

            // Set click listener for the whole item
            itemView.setOnClickListener(v -> {
                if (onProductClickListener != null) {
                    onProductClickListener.onProductClick(product);
                }
            });

            // Set click listener for Add to Cart button
            buttonAddToCart.setOnClickListener(v -> {
                Context context = itemView.getContext();
                TokenManager tokenManager = new TokenManager(context);
                
                if (!tokenManager.isLoggedIn()) {
                    Toast.makeText(context, "Vui lòng đăng nhập để thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                    context.startActivity(new Intent(context, LoginActivity.class));
                    return;
                }

                CartRepository cartRepository = new CartRepository(context);
                CartItem cartItem = new CartItem();
                cartItem.setProductId(product.getId());
                cartItem.setQuantity(1); // Default quantity is 1
                cartItem.setPrice(product.getPrice());
                cartItem.setProduct(product);

                cartRepository.addItemToCart(cartItem, new CartRepository.CartItemCallback() {
                    @Override
                    public void onSuccess(CartItem cartItem) {
                        Toast.makeText(context, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }

        private String formatPrice(double price) {
            NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
            return formatter.format(price) + " đ";
        }
    }

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }
} 