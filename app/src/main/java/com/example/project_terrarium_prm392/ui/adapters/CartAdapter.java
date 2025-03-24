package com.example.project_terrarium_prm392.ui.adapters;

import android.content.Context;
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
    private final Context context;
    private final CartItemListener listener;
    private List<CartItem> cartItems;

    public CartAdapter(Context context, CartItemListener listener) {
        this.context = context;
        this.listener = listener;
        this.cartItems = new ArrayList<>();
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItems.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public void setCartItems(List<CartItem> items) {
        this.cartItems = items;
        notifyDataSetChanged();
    }

    public void updateItem(CartItem updatedItem) {
        for (int i = 0; i < cartItems.size(); i++) {
            if (cartItems.get(i).getId() == updatedItem.getId()) {
                cartItems.set(i, updatedItem);
                notifyItemChanged(i);
                break;
            }
        }
    }

    public void removeItem(CartItem item) {
        int position = cartItems.indexOf(item);
        if (position != -1) {
            cartItems.remove(position);
            notifyItemRemoved(position);
        }
    }

    public class CartViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageProduct;
        private final TextView textProductName;
        private final TextView textPrice;
        private final TextView textQuantity;
        private final TextView textTotal;
        private final ImageButton buttonIncrease;
        private final ImageButton buttonDecrease;
        private final ImageButton buttonRemove;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            imageProduct = itemView.findViewById(R.id.imageProduct);
            textProductName = itemView.findViewById(R.id.textProductName);
            textPrice = itemView.findViewById(R.id.textPrice);
            textQuantity = itemView.findViewById(R.id.textQuantity);
            textTotal = itemView.findViewById(R.id.textTotal);
            buttonIncrease = itemView.findViewById(R.id.buttonIncrease);
            buttonDecrease = itemView.findViewById(R.id.buttonDecrease);
            buttonRemove = itemView.findViewById(R.id.buttonRemove);
        }

        public void bind(CartItem item) {
            // Load product image
            if (item.getProduct() != null && item.getProduct().getImageUrl() != null) {
                Glide.with(context)
                    .load(item.getProduct().getImageUrl())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_dialog_alert)
                    .into(imageProduct);
            } else {
                imageProduct.setImageResource(android.R.drawable.ic_menu_gallery);
            }

            // Set product details
            textProductName.setText(item.getProduct() != null ? item.getProduct().getName() : "Unknown Product");
            textPrice.setText(formatCurrency(item.getPrice()));
            textQuantity.setText(String.valueOf(item.getQuantity()));
            textTotal.setText(formatCurrency(item.getPrice() * item.getQuantity()));

            // Set click listeners
            buttonIncrease.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onIncreaseQuantity(item);
                }
            });

            buttonDecrease.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDecreaseQuantity(item);
                }
            });

            buttonRemove.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRemoveItem(item);
                }
            });
        }

        private String formatCurrency(double amount) {
            NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
            return formatter.format(amount) + " đ";
        }
    }

    public interface CartItemListener {
        void onIncreaseQuantity(CartItem cartItem);
        void onDecreaseQuantity(CartItem cartItem);
        void onRemoveItem(CartItem cartItem);
    }
} 