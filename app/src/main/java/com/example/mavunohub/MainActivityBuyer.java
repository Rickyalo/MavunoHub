package com.example.mavunohub;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivityBuyer extends AppCompatActivity {

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    // UI Components
    private RecyclerView recyclerView;
    private ProductAdapterBuyer productAdapter;
    private TextView userNameTextView, cartTextView;
    private ImageView profileImageView;
    private EditText searchField;

    // Data
    private List<Product> productList;
    private String userName, profileImageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buyer_main);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize Views
        userNameTextView = findViewById(R.id.username);
        profileImageView = findViewById(R.id.profilePicture);
        cartTextView = findViewById(R.id.cart_tv);
        searchField = findViewById(R.id.searchField);
        recyclerView = findViewById(R.id.productRecyclerView);

        // RecyclerView Setup
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        productList = new ArrayList<>();
        productAdapter = new ProductAdapterBuyer(this, productList, new ProductAdapterBuyer.OnProductClickListener() {
            @Override
            public void onProductClick(Product product) {
                Toast.makeText(MainActivityBuyer.this, "Clicked: " + product.getName(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAddToCartClick(Product product) {
                addToCart(product);
            }
        });
        recyclerView.setAdapter(productAdapter);

        // Load Data
        loadUserData();
        loadProducts();

        // Cart Click
        cartTextView.setOnClickListener(v -> startActivity(new Intent(MainActivityBuyer.this, CartActivity.class)));

        // Search Field Listener
        searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProducts(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadUserData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            db.collection("users").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot != null && documentSnapshot.exists()) {
                            userName = documentSnapshot.getString("name");
                            profileImageUrl = documentSnapshot.getString("profileImageUrl");

                            userNameTextView.setText(userName != null ? userName : "User");
                            if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                                Glide.with(this).load(profileImageUrl).into(profileImageView);
                            }
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to load user data", Toast.LENGTH_SHORT).show());
        }
    }

    private void loadProducts() {
        db.collection("products").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    productList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Product product = doc.toObject(Product.class);
                        productList.add(product);
                    }
                    productAdapter.updateList(productList);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load products", Toast.LENGTH_SHORT).show());
    }

    private void addToCart(Product product) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show();
            return;
        }

        String cartItemId = db.collection("cart").document().getId();
        CartItem cartItem = new CartItem(
                cartItemId,
                product.getName(),
                product.getPricePerUnit(),
                1, // Default quantity
                product.getImageUrl(),
                product.getCounty(),
                product.getSubCounty(),
                product.getPhone(),
                user.getUid() // Add userId
        );

        db.collection("cart")
                .document(cartItemId)
                .set(cartItem)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Product added to cart!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to add to cart: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }


    private void filterProducts(String query) {
        List<Product> filteredList = new ArrayList<>();
        for (Product product : productList) {
            if ((product.getName() != null && product.getName().toLowerCase().contains(query.toLowerCase())) ||
                    (product.getCounty() != null && product.getCounty().toLowerCase().contains(query.toLowerCase())) ||
                    (product.getSubCounty() != null && product.getSubCounty().toLowerCase().contains(query.toLowerCase()))) {
                filteredList.add(product);
            }
        }
        productAdapter.updateList(filteredList);

        if (filteredList.isEmpty()) {
            Toast.makeText(this, "No products match your search", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_buyer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.signOut) {
            showSignOutDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showSignOutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Sign Out")
                .setMessage("Are you sure you want to sign out?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    mAuth.signOut();
                    startActivity(new Intent(MainActivityBuyer.this, LoginActivity.class));
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
    }
}