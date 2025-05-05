package com.example.mavunohub;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CartActivity extends AppCompatActivity {

    private RecyclerView cartRecyclerView;
    private CartAdapter cartAdapter;
    private List<CartItem> cartItemList;
    private FirebaseFirestore db;
    private TextView totalPriceTextView;
    private String userId;
    private Button purchaseButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        cartRecyclerView = findViewById(R.id.cartRecyclerView);
        totalPriceTextView = findViewById(R.id.totalPriceTextView);
        Button chatButton = findViewById(R.id.chatButton);
        purchaseButton = findViewById(R.id.purchaseButton);

        db = FirebaseFirestore.getInstance();
        cartItemList = new ArrayList<>();
        cartAdapter = new CartAdapter(this, cartItemList, new CartAdapter.OnCartActionListener() {
            @Override
            public void onQuantityChanged() { calculateTotalPrice(); }
            @Override
            public void onItemRemoved() { calculateTotalPrice(); }
        });

        cartRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        cartRecyclerView.setAdapter(cartAdapter);

        userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) {
            Toast.makeText(this, "User not authenticated!", Toast.LENGTH_SHORT).show();
            return;
        }

        chatButton.setOnClickListener(v -> startWhatsAppChat());
        purchaseButton.setOnClickListener(v -> showPurchaseConfirmation());

        loadCartItems();
    }

    private void loadCartItems() {
        db.collection("cart").whereEqualTo("userId", userId).get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        cartItemList.clear();
                        for (QueryDocumentSnapshot document : querySnapshot) {
                            CartItem cartItem = document.toObject(CartItem.class);
                            cartItem.setId(document.getId());
                            cartItemList.add(cartItem);
                        }
                        cartAdapter.notifyDataSetChanged();
                        calculateTotalPrice();
                    }
                })
                .addOnFailureListener(e -> Log.e("CartActivity", "Error loading cart items", e));
    }

    private double calculateTotalPrice() {
        double total = 0.0;
        for (CartItem item : cartItemList) {
            total += item.getPricePerUnit() * item.getQuantity();
        }
        totalPriceTextView.setText("Total Price: KES " + String.format("%.2f", total));
        return total;
    }

    private void showPurchaseConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Purchase")
                .setMessage("Are you sure you want to place this order?")
                .setPositiveButton("Yes", (dialog, which) -> purchaseItems())
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void purchaseItems() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show();
            return;
        }

        if (cartItemList.isEmpty()) {
            Toast.makeText(this, "Your cart is empty!", Toast.LENGTH_SHORT).show();
            return;
        }

        List<Map<String, Object>> orderedProducts = new ArrayList<>();
        for (CartItem item : cartItemList) {
            Map<String, Object> productDetails = new HashMap<>();
            productDetails.put("productId", item.getProductId());
            productDetails.put("name", item.getName());
            productDetails.put("imageUrl", item.getImageUrl());
            productDetails.put("quantity", item.getQuantity());
            orderedProducts.add(productDetails);
        }

        Map<String, Object> orderData = new HashMap<>();
        orderData.put("phone", cartItemList.get(0).getPhone());
        orderData.put("buyerId", user.getUid());
        orderData.put("farmerId", cartItemList.get(0).getSellerId());
        orderData.put("products", orderedProducts);
        orderData.put("status", "Pending");
        orderData.put("totalPrice", calculateTotalPrice());

        db.collection("orders")
                .add(orderData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Order placed successfully!", Toast.LENGTH_SHORT).show();
                    clearCart();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to place order: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void clearCart() {
        for (CartItem item : cartItemList) {
            db.collection("cart").document(item.getId())
                    .delete()
                    .addOnSuccessListener(aVoid -> Log.d("CartActivity", "Item removed from cart"))
                    .addOnFailureListener(e -> Log.e("CartActivity", "Error clearing cart", e));
        }
        cartItemList.clear();
        cartAdapter.notifyDataSetChanged();
        calculateTotalPrice();
    }

    private void startWhatsAppChat() {
        if (cartItemList.isEmpty()) {
            Toast.makeText(this, "No farmer associated with an empty cart.", Toast.LENGTH_SHORT).show();
            return;
        }

        String productId = cartItemList.get(0).getProductId();
        if (productId == null || productId.isEmpty()) {
            Toast.makeText(this, "Product ID is missing. Cannot start chat.", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("products").document(productId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.contains("phone")) {
                        String phoneNumber = documentSnapshot.getString("phone");

                        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
                            Toast.makeText(CartActivity.this, "Farmer's phone number is empty.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (!phoneNumber.matches("^\\+?[0-9]{10,13}$")) {
                            Toast.makeText(CartActivity.this, "Invalid phone number format.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String message = "Hello! I'd like to discuss the products you’ve updated in MavunoHub.";
                        String encodedMessage = Uri.encode(message);

                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse("https://wa.me/" + phoneNumber + "?text=" + encodedMessage));
                        startActivity(intent);
                    } else {
                        Toast.makeText(CartActivity.this, "Farmer's phone number not found in product details.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Log.e("WhatsAppChat", "Error fetching farmer's number", e));
    }
}