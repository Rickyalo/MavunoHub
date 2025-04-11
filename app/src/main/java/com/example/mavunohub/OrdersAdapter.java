package com.example.mavunohub;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.OrdersViewHolder> {

    private final Context context;
    private final List<OrderItem> orderList;
    private final OnOrderActionListener listener;

    public interface OnOrderActionListener {
        void onMarkAsSold(OrderItem order);
    }

    public OrdersAdapter(Context context, List<OrderItem> orderList, OnOrderActionListener listener) {
        this.context = context;
        this.orderList = orderList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public OrdersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.order_item, parent, false);
        return new OrdersViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrdersViewHolder holder, int position) {
        OrderItem order = orderList.get(position);

        holder.buyerName.setText("Buyer ID: " + order.getBuyerId());
        holder.productDetails.setText("Products:\n" + order.getProductsDetails());
        holder.orderStatus.setText("Status: " + order.getStatus());
        holder.totalPrice.setText("Total: KES " + String.format("%.2f", order.getTotalPrice()));

        holder.markAsSoldButton.setOnClickListener(v -> listener.onMarkAsSold(order));
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class OrdersViewHolder extends RecyclerView.ViewHolder {
        TextView buyerName, productDetails, orderStatus, totalPrice;
        Button markAsSoldButton;

        public OrdersViewHolder(@NonNull View itemView) {
            super(itemView);
            buyerName = itemView.findViewById(R.id.buyerName);
            productDetails = itemView.findViewById(R.id.productDetails);
            orderStatus = itemView.findViewById(R.id.orderStatus);
            totalPrice = itemView.findViewById(R.id.totalPrice);
            markAsSoldButton = itemView.findViewById(R.id.markAsSoldButton);
        }
    }
}