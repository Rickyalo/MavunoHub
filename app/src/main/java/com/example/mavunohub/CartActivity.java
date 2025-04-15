package com.example.mavunohub;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        cartRecyclerView = findViewById(R.id.cartRecyclerView);
        totalPriceTextView = findViewById(R.id.totalPriceTextView);
        Button purchaseButton = findViewById(R.id.purchaseButton);

        db = FirebaseFirestore.getInstance();
        cartItemList = new ArrayList<>();
        cartAdapter = new CartAdapter(this, cartItemList, new CartAdapter.OnCartActionListener() {
            @Override
            public void onQuantityChanged() {
                calculateTotalPrice();
            }

            @Override
            public void onItemRemoved() {
                calculateTotalPrice();
            }
        });

        cartRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        cartRecyclerView.setAdapter(cartAdapter);

        purchaseButton.setOnClickListener(v -> {
            if (!cartItemList.isEmpty()) {
                createOrder(); // Create the order and then open WhatsApp
            } else {
                Toast.makeText(this, "Your cart is empty.", Toast.LENGTH_SHORT).show();
            }
        });

        loadCartItems();
    }

    private void loadCartItems() {
        String userId = FirebaseAuth.getInstance().getUid();

        db.collection("cart")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    cartItemList.clear();
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        CartItem cartItem = document.toObject(CartItem.class);
                        cartItem.setId(document.getId());
                        cartItemList.add(cartItem);
                    }
                    cartAdapter.notifyDataSetChanged();
                    calculateTotalPrice();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load cart items.", Toast.LENGTH_SHORT).show());
    }

    private void calculateTotalPrice() {
        double total = calculateTotalPriceAsDouble();
        totalPriceTextView.setText("Total Price: KES " + String.format("%.2f", total));
    }

    private double calculateTotalPriceAsDouble() {
        double total = 0.0;
        for (CartItem item : cartItemList) {
            total += item.getPricePerUnit() * item.getQuantity();
        }
        return total;
    }

    private void createOrder() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String userId = mAuth.getUid();

        if (userId == null) {
            Toast.makeText(this, "Please log in first.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> orderData = new HashMap<>();
        orderData.put("userId", userId);
        orderData.put("cartItems", cartItemList); // Send the entire cart items
        orderData.put("totalPrice", calculateTotalPriceAsDouble()); // Total price
        orderData.put("status", "Pending"); // Initial status

        db.collection("orders")
                .add(orderData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Order created successfully!", Toast.LENGTH_SHORT).show();
                    clearCart(); // Clear the cart after order creation
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to create order: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void clearCart() {
        String userId = FirebaseAuth.getInstance().getUid();
        db.collection("cart")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        db.collection("cart").document(document.getId()).delete();
                    }
                    cartItemList.clear();
                    cartAdapter.notifyDataSetChanged();
                    calculateTotalPrice();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to clear cart: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    public void openWhatsApp(String phone) {
        if (phone == null || phone.isEmpty()) {
            Toast.makeText(this, "Phone number is missing or invalid.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (phone.startsWith("0")) {
            phone = "+254" + phone.substring(1);
        }

        String message = "Hello, I would like to discuss my purchase.";
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://wa.me/" + phone + "?text=" + Uri.encode(message)));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Failed to open WhatsApp: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}