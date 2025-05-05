package com.example.mavunohub;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.OrderViewHolder> {

    private List<OrderItem> orders;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public OrdersAdapter(List<OrderItem> orders) {
        this.orders = orders;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_item, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        OrderItem order = orders.get(position);

        holder.buyerId.setText("Buyer ID: " + order.getBuyerId());
        holder.orderStatus.setText("Status: " + order.getStatus());


        double totalPrice = (order.getTotalPrice() != null) ? order.getTotalPrice() : 0.0;
        holder.totalPrice.setText("Total: KES " + String.format("%.2f", totalPrice));

        if (order.getProducts() != null && !order.getProducts().isEmpty()) {
            Map<String, Object> productDetails = order.getProducts().get(0);
            holder.productDetails.setText("Product: " + productDetails.get("name"));


            String imageUrl = (String) productDetails.get("imageUrl");
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(holder.itemView.getContext())
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_imageplaceholder)
                        .into(holder.productImage);
            } else {
                holder.productImage.setImageResource(R.drawable.ic_imageplaceholder);
            }
        } else {
            holder.productDetails.setText("Product: N/A");
            holder.productImage.setImageResource(R.drawable.ic_imageplaceholder);
        }

        holder.markAsSoldButton.setOnClickListener(v -> markOrderAsSold(order, holder));
    }

    private void markOrderAsSold(OrderItem order, OrderViewHolder holder) {
        db.collection("orders").document(order.getId()).update("status", "Sold")
                .addOnSuccessListener(aVoid -> {
                    order.setStatus("Sold");
                    holder.orderStatus.setText("Status: Sold");
                    updateProductStock(order);
                    storeSoldProduct(order);
                })
                .addOnFailureListener(e -> holder.orderStatus.setText("Error updating status"));
    }

    private void updateProductStock(OrderItem order) {
        if (order.getProducts() != null && !order.getProducts().isEmpty()) {
            for (Map<String, Object> productDetails : order.getProducts()) {
                String productId = (String) productDetails.get("productId");

                db.collection("products").document(productId).get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {

                                Long currentStockLong = documentSnapshot.getLong("quantity");
                                int currentStock = (currentStockLong != null) ? currentStockLong.intValue() : 0;

                                Long soldQuantityLong = (Long) productDetails.get("quantity");
                                int soldQuantity = (soldQuantityLong != null) ? soldQuantityLong.intValue() : 0;

                                Double pricePerUnitDouble = (Double) productDetails.get("pricePerUnit");
                                double pricePerUnit = (pricePerUnitDouble != null) ? pricePerUnitDouble : 0.0;

                                Double currentTotalPriceDouble = documentSnapshot.getDouble("totalPrice");
                                double currentTotalPrice = (currentTotalPriceDouble != null) ? currentTotalPriceDouble : 0.0;

                                int newStock = Math.max(0, currentStock - soldQuantity);
                                double newTotalPrice = Math.max(0, currentTotalPrice - (soldQuantity * pricePerUnit));

                                db.collection("products").document(productId)
                                        .update("quantity", newStock, "totalPrice", newTotalPrice)
                                        .addOnSuccessListener(aVoid -> System.out.println("✅ Updated product stock & total price"))
                                        .addOnFailureListener(e -> System.err.println("❌ Error updating product stock: " + e.getMessage()));
                            }
                        });
            }
        }
    }

    private void storeSoldProduct(OrderItem order) {
        if (order.getProducts() != null && !order.getProducts().isEmpty()) {
            for (Map<String, Object> productDetails : order.getProducts()) {
                Map<String, Object> soldProductData = new HashMap<>();
                soldProductData.put("buyerId", order.getBuyerId());
                soldProductData.put("farmerId", order.getFarmerId());
                soldProductData.put("productId", productDetails.get("productId"));
                soldProductData.put("name", productDetails.get("name"));


                String imageUrl = (String) productDetails.get("imageUrl");
                soldProductData.put("imageUrl", (imageUrl != null) ? imageUrl : "N/A");


                Long quantityLong = (Long) productDetails.get("quantity");
                int quantity = (quantityLong != null) ? quantityLong.intValue() : 0;
                soldProductData.put("quantity", quantity);

                Double pricePerUnitDouble = (Double) productDetails.get("pricePerUnit");
                double pricePerUnit = (pricePerUnitDouble != null) ? pricePerUnitDouble : 0.0;
                soldProductData.put("pricePerUnit", pricePerUnit);

                soldProductData.put("totalPrice", quantity * pricePerUnit);
                soldProductData.put("soldDate", System.currentTimeMillis());

                db.collection("sold_products")
                        .add(soldProductData)
                        .addOnSuccessListener(documentReference -> {
                            System.out.println("✅ Sold product stored in Firestore: " + documentReference.getId());
                        })
                        .addOnFailureListener(e -> {
                            System.err.println("❌ Error saving sold product: " + e.getMessage());
                        });
            }
        } else {
            System.err.println("❌ No products found in order!");
        }
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView buyerId, productDetails, orderStatus, totalPrice;
        Button markAsSoldButton;
        ImageView productImage;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            buyerId = itemView.findViewById(R.id.buyerId);
            productDetails = itemView.findViewById(R.id.productDetails);
            orderStatus = itemView.findViewById(R.id.orderStatus);
            totalPrice = itemView.findViewById(R.id.totalPrice);
            markAsSoldButton = itemView.findViewById(R.id.markAsSoldButton);
            productImage = itemView.findViewById(R.id.productImage);
        }
    }
}