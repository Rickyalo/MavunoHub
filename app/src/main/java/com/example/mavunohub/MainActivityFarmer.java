package com.example.mavunohub;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivityFarmer extends AppCompatActivity {

    private TextView username;
    private ImageView logoutIcon;
    private Button btnAddProduct, btnManageProducts, btnViewOrders, btnGenerateReport;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farmers);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        username = findViewById(R.id.username);
        logoutIcon = findViewById(R.id.logoutIcon);
        btnAddProduct = findViewById(R.id.btnAddProduct);
        btnManageProducts = findViewById(R.id.btnManageProducts);
        btnViewOrders = findViewById(R.id.btnViewOrders);
        btnGenerateReport = findViewById(R.id.btnGenerateReport);

        // Load farmer's name
        loadUserData();

        // Logout functionality
        logoutIcon.setOnClickListener(v -> showLogoutDialog());

        // Button click listeners
        btnAddProduct.setOnClickListener(v -> startActivity(new Intent(MainActivityFarmer.this, AddProductActivity.class)));
        btnManageProducts.setOnClickListener(v -> startActivity(new Intent(MainActivityFarmer.this, ManageProductsActivity.class)));
        btnViewOrders.setOnClickListener(v -> startActivity(new Intent(MainActivityFarmer.this, ViewOrdersActivity.class)));
        btnGenerateReport.setOnClickListener(v -> startActivity(new Intent(MainActivityFarmer.this, GenerateReportActivity.class)));
    }

    private void loadUserData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            DocumentReference userRef = db.collection("users").document(userId);
            userRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String name = documentSnapshot.getString("name");
                    username.setText(name != null ? name : "Farmer");
                }
            }).addOnFailureListener(e -> Toast.makeText(MainActivityFarmer.this, "Failed to load user data", Toast.LENGTH_SHORT).show());
        }
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Sign Out")
                .setMessage("Are you sure you want to sign out?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    mAuth.signOut();
                    Toast.makeText(MainActivityFarmer.this, "Logging out...", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(MainActivityFarmer.this, LoginActivity.class));
                    finish();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
