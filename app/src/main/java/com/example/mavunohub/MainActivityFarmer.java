package com.example.mavunohub;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivityFarmer extends AppCompatActivity {
    private TextView username;
    private ImageView profilePicture, logoutIcon;
    private CardView cardAddProduct, cardManageProducts, cardViewOrders, cardGenerateReport;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String farmerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farmers);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        username = findViewById(R.id.username);
        profilePicture = findViewById(R.id.profilePicture);
        logoutIcon = findViewById(R.id.logoutIcon);
        cardAddProduct = findViewById(R.id.cardAddProduct);
        cardManageProducts = findViewById(R.id.cardManageProducts);
        cardViewOrders = findViewById(R.id.cardViewOrders);
        cardGenerateReport = findViewById(R.id.cardGenerateReport);

        loadUserData();
        logoutIcon.setOnClickListener(v -> showLogoutDialog());

        cardAddProduct.setOnClickListener(v -> navigateTo(AddProductActivity.class));
        cardManageProducts.setOnClickListener(v -> navigateTo(ManageProductsActivity.class));
        cardViewOrders.setOnClickListener(v -> navigateTo(ViewOrdersActivity.class));
        cardGenerateReport.setOnClickListener(v -> generateReport());
    }

    private void loadUserData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            farmerId = user.getUid();
            DocumentReference userRef = db.collection("users").document(farmerId);
            userRef.get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");
                            String profileImageUrl = documentSnapshot.getString("profileImageUrl");

                            username.setText(name != null ? name : "Farmer");

                            if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                                Glide.with(this)
                                        .load(profileImageUrl)
                                        .placeholder(R.drawable.ic_profile_placeholder)
                                        .error(R.drawable.ic_profile_error)
                                        .into(profilePicture);
                            }
                        }
                    })
                    .addOnFailureListener(e -> showToast("Failed to load user data"));
        } else {
            showToast("User not logged in");
        }
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Sign Out")
                .setMessage("Are you sure you want to sign out?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    mAuth.signOut();
                    showToast("Logging out...");
                    navigateTo(LoginActivity.class);
                    finish();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void generateReport() {
        if (farmerId != null) {
            Intent intent = new Intent(MainActivityFarmer.this, GenerateReportActivity.class);
            intent.putExtra("farmerId", farmerId);
            startActivity(intent);
        } else {
            showToast("Farmer ID not found.");
        }
    }

    private void navigateTo(Class<?> targetActivity) {
        Intent intent = new Intent(MainActivityFarmer.this, targetActivity);
        startActivity(intent);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}