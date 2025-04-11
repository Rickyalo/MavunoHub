package com.example.mavunohub;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ViewOrdersActivity extends AppCompatActivity {

    private RecyclerView ordersRecyclerView;
    private OrdersAdapter ordersAdapter;
    private List<OrderItem> orderList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_orders);

        ordersRecyclerView = findViewById(R.id.ordersRecyclerView);

        db = FirebaseFirestore.getInstance();
        orderList = new ArrayList<>();
        ordersAdapter = new OrdersAdapter(this, orderList, this::markAsSold);

        ordersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        ordersRecyclerView.setAdapter(ordersAdapter);

        loadOrders();
    }

    private void loadOrders() {
        String farmerId = FirebaseAuth.getInstance().getUid();

        db.collection("orders")
                .whereEqualTo("farmerId", farmerId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    orderList.clear();
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        OrderItem order = document.toObject(OrderItem.class);
                        order.setId(document.getId());
                        orderList.add(order);
                    }
                    ordersAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load orders: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void markAsSold(OrderItem order) {
        db.collection("orders")
                .document(order.getId())
                .update("status", "Sold")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Order marked as sold!", Toast.LENGTH_SHORT).show();
                    loadOrders();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to mark order as sold: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}