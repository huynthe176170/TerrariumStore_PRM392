package com.example.project_terrarium_prm392.ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_terrarium_prm392.R;
import com.example.project_terrarium_prm392.models.Order;
import com.example.project_terrarium_prm392.ui.OrderDetailActivity;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private final List<Order> orderList;
    private final Context context;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    public OrderAdapter(Context context, List<Order> orderList) {
        this.context = context;
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);
        
        holder.textOrderId.setText(String.valueOf(order.getId()));
        holder.textOrderDate.setText(dateFormat.format(order.getOrderDate()));
        holder.textOrderTotal.setText(currencyFormat.format(order.getTotalPrice()));
        
        // Cập nhật trạng thái và màu sắc tương ứng
        holder.textOrderStatus.setText(getStatusText(order.getStatus()));
        holder.textOrderStatus.setBackgroundResource(getStatusBackground(order.getStatus()));
        
        holder.buttonViewDetail.setOnClickListener(v -> {
            Intent intent = new Intent(context, OrderDetailActivity.class);
            intent.putExtra("ORDER_ID", order.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    private String getStatusText(String status) {
        switch (status.toLowerCase()) {
            case "pending":
                return "Chờ xác nhận";
            case "confirmed":
                return "Đã xác nhận";
            case "shipping":
                return "Đang giao hàng";
            case "delivered":
                return "Đã giao hàng";
            case "cancelled":
                return "Đã hủy";
            default:
                return status;
        }
    }

    private int getStatusBackground(String status) {
        // Note: you should create different background drawables for different statuses
        // For now we'll use the same background
        return R.drawable.status_background;
    }

    public void updateData(List<Order> newOrderList) {
        this.orderList.clear();
        this.orderList.addAll(newOrderList);
        notifyDataSetChanged();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView textOrderId;
        TextView textOrderDate;
        TextView textOrderTotal;
        TextView textOrderStatus;
        Button buttonViewDetail;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            textOrderId = itemView.findViewById(R.id.textOrderId);
            textOrderDate = itemView.findViewById(R.id.textOrderDate);
            textOrderTotal = itemView.findViewById(R.id.textOrderTotal);
            textOrderStatus = itemView.findViewById(R.id.textOrderStatus);
            buttonViewDetail = itemView.findViewById(R.id.buttonViewDetail);
        }
    }
} 