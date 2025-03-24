package com.example.project_terrarium_prm392.ui.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.project_terrarium_prm392.R;
import com.example.project_terrarium_prm392.models.CartItem;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
    private static final String TAG = "CartAdapter";

    private final Context context;
    private List<CartItem> cartItems;
    private final CartItemListener listener;

    public CartAdapter(Context context, CartItemListener listener) {
        this.context = context;
        this.cartItems = new ArrayList<>();
        this.listener = listener;
    }

    public void setCartItems(List<CartItem> cartItems) {
        this.cartItems = cartItems;
        notifyDataSetChanged();
    }

    public List<CartItem> getCartItems() {
        return cartItems;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem cartItem = cartItems.get(position);
        
        if (cartItem.getProduct() != null) {
            // Set product name
            holder.textViewProductName.setText(cartItem.getProduct().getName());
            
            // Set product price
            String formattedPrice = formatCurrency(cartItem.getProduct().getPrice());
            holder.textViewPrice.setText(formattedPrice);
            
            // Set quantity
            holder.textViewQuantity.setText(String.valueOf(cartItem.getQuantity()));
            
            // Calculate and set subtotal
            double subtotal = cartItem.getProduct().getPrice() * cartItem.getQuantity();
            String formattedSubtotal = formatCurrency(subtotal);
            holder.textViewSubtotal.setText("Tổng: " + formattedSubtotal);
            
            // Load product image using Glide
            if (cartItem.getProduct().getImageUrl() != null && !cartItem.getProduct().getImageUrl().isEmpty()) {
                Glide.with(context)
                        .load(cartItem.getProduct().getImageUrl())
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .error(R.drawable.ic_launcher_foreground)
                        .centerCrop()
                        .into(holder.imageViewProduct);
            } else {
                holder.imageViewProduct.setImageResource(R.drawable.ic_launcher_foreground);
            }

            // Set click listeners
            holder.buttonIncreaseQuantity.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onIncreaseQuantity(cartItem);
                }
            });

            holder.buttonDecreaseQuantity.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDecreaseQuantity(cartItem);
                }
            });

            holder.imageButtonDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRemoveItem(cartItem);
                }
            });
        } else {
            // Handle case where product is null
            holder.textViewProductName.setText("Sản phẩm không tồn tại");
            holder.textViewPrice.setText("N/A");
            holder.textViewQuantity.setText("0");
            holder.textViewSubtotal.setText("Tổng: 0 đ");
            holder.imageViewProduct.setImageResource(R.drawable.ic_launcher_foreground);
            Log.e(TAG, "Product is null for CartItem id: " + cartItem.getId());
        }
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public double getTotalPrice() {
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getSubtotal();
        }
        return total;
    }

    private String formatCurrency(double amount) {
        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        return formatter.format(amount) + " đ";
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewProduct;
        TextView textViewProductName, textViewPrice, textViewQuantity, textViewSubtotal;
        ImageButton buttonIncreaseQuantity, buttonDecreaseQuantity, imageButtonDelete;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewProduct = itemView.findViewById(R.id.imageViewProduct);
            textViewProductName = itemView.findViewById(R.id.textViewProductName);
            textViewPrice = itemView.findViewById(R.id.textViewPrice);
            textViewQuantity = itemView.findViewById(R.id.textViewQuantity);
            textViewSubtotal = itemView.findViewById(R.id.textViewSubtotal);
            buttonIncreaseQuantity = itemView.findViewById(R.id.buttonIncreaseQuantity);
            buttonDecreaseQuantity = itemView.findViewById(R.id.buttonDecreaseQuantity);
            imageButtonDelete = itemView.findViewById(R.id.imageButtonDelete);
        }
    }

    public interface CartItemListener {
        void onIncreaseQuantity(CartItem cartItem);
        void onDecreaseQuantity(CartItem cartItem);
        void onRemoveItem(CartItem cartItem);
    }
} 