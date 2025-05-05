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
import java.util.Map;

public class ViewOrdersActivity extends AppCompatActivity {

    private RecyclerView ordersRecyclerView;
    private OrdersAdapter ordersAdapter;
    private List<OrderItem> orderList;
    private FirebaseFirestore db;
    private String farmerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_orders);

        ordersRecyclerView = findViewById(R.id.ordersRecyclerView);
        db = FirebaseFirestore.getInstance();
        orderList = new ArrayList<>();
        ordersAdapter = new OrdersAdapter(orderList);

        ordersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        ordersRecyclerView.setAdapter(ordersAdapter);

        fetchFarmerIdAndLoadOrders();
    }

    private void fetchFarmerIdAndLoadOrders() {
        String userId = FirebaseAuth.getInstance().getUid();

        if (userId == null) {
            Toast.makeText(this, "User not authenticated!", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        farmerId = documentSnapshot.getString("farmerId");
                        if (farmerId != null && !farmerId.isEmpty()) {
                            loadOrders();
                        } else {
                            Toast.makeText(this, "Farmer ID missing!", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to retrieve farmer data.", Toast.LENGTH_SHORT).show());
    }

    private void loadOrders() {
        db.collection("orders")
                .whereEqualTo("farmerId", farmerId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    orderList.clear();
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        OrderItem order = document.toObject(OrderItem.class);
                        order.setId(document.getId());

                        List<Map<String, Object>> products = (List<Map<String, Object>>) document.get("products");
                        order.setProducts(products != null ? products : new ArrayList<>());

                        if (document.contains("totalPrice")) {
                            order.setTotalPrice(document.getDouble("totalPrice"));
                        }

                        orderList.add(order);
                    }
                    ordersAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load orders: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}