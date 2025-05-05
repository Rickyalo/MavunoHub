package com.example.mavunohub;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private final Context context;
    private final List<CartItem> cartItemList;
    private final OnCartActionListener listener;

    public interface OnCartActionListener {
        void onQuantityChanged();
        void onItemRemoved();
    }

    public CartAdapter(Context context, List<CartItem> cartItemList, OnCartActionListener listener) {
        this.context = context;
        this.cartItemList = cartItemList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cart_item, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem cartItem = cartItemList.get(position);

        // Bind product details
        holder.productName.setText(cartItem.getName());
        holder.productPrice.setText("KES " + String.format("%.2f", cartItem.getPricePerUnit()));
        holder.productQuantity.setText("Qty: " + cartItem.getQuantity());

        // Load product image or use placeholder
        if (cartItem.getImageUrl() != null && !cartItem.getImageUrl().isEmpty()) {
            Glide.with(context).load(cartItem.getImageUrl()).into(holder.productImageView);
        } else {
            holder.productImageView.setImageResource(R.drawable.ic_imageplaceholder);
        }

        // Increase quantity
        holder.increaseQuantity.setOnClickListener(v -> {
            cartItem.setQuantity(cartItem.getQuantity() + 1);
            updateCartItem(cartItem);
            notifyItemChanged(position);
            listener.onQuantityChanged();
        });

        // Reduce quantity
        holder.decreaseQuantity.setOnClickListener(v -> {
            if (cartItem.getQuantity() > 1) {
                cartItem.setQuantity(cartItem.getQuantity() - 1);
                updateCartItem(cartItem);
                notifyItemChanged(position);
                listener.onQuantityChanged();
            } else {
                // Quantity is 1, remove the item
                removeCartItem(cartItem, position);
            }
        });

        // Delete item
        holder.removeItem.setOnClickListener(v -> removeCartItem(cartItem, position));
    }

    @Override
    public int getItemCount() {
        return cartItemList.size();
    }

    private void updateCartItem(CartItem cartItem) {
        FirebaseFirestore.getInstance()
                .collection("cart")
                .document(cartItem.getId())
                .update("quantity", cartItem.getQuantity())
                .addOnSuccessListener(aVoid -> {})
                .addOnFailureListener(e -> Toast.makeText(context, "Failed to update quantity: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void removeCartItem(CartItem cartItem, int position) {
        String currentUserId = FirebaseAuth.getInstance().getUid(); // Get logged-in user's UID

        if (cartItem.getUserId().equals(currentUserId)) { // Check if userId matches
            FirebaseFirestore.getInstance()
                    .collection("cart")
                    .document(cartItem.getId())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        cartItemList.remove(position);
                        notifyItemRemoved(position);
                        listener.onItemRemoved();
                        Toast.makeText(context, "Item removed successfully!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> Toast.makeText(context, "Failed to remove item: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(context, "Permission Denied: You cannot remove this item.", Toast.LENGTH_SHORT).show();
        }
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView productImageView, increaseQuantity, decreaseQuantity, removeItem;
        TextView productName, productPrice, productQuantity;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            productImageView = itemView.findViewById(R.id.productImageView);
            productName = itemView.findViewById(R.id.productName);
            productPrice = itemView.findViewById(R.id.productPrice);
            productQuantity = itemView.findViewById(R.id.productQuantity);
            increaseQuantity = itemView.findViewById(R.id.increaseQuantity);
            decreaseQuantity = itemView.findViewById(R.id.decreaseQuantity);
            removeItem = itemView.findViewById(R.id.removeItem);
        }
    }
}