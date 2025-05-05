package com.example.mavunohub;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivityBuyer extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private RecyclerView recyclerView;
    private ProductAdapterBuyer productAdapter;
    private TextView userNameTextView, cartTextView, signOutTextView;
    private ImageView profileImageView;
    private EditText searchField;
    private List<Product> productList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_buyer_main);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        userNameTextView = findViewById(R.id.username);
        profileImageView = findViewById(R.id.profilePicture);
        cartTextView = findViewById(R.id.cart_tv);
        signOutTextView = findViewById(R.id.signOut);
        searchField = findViewById(R.id.searchField);
        recyclerView = findViewById(R.id.productRecyclerView);
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
        loadUserData();
        loadProducts();

        cartTextView.setOnClickListener(v -> startActivity(new Intent(MainActivityBuyer.this, CartActivity.class)));

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

        signOutTextView.setOnClickListener(v -> showSignOutConfirmation());
    }

    private void loadUserData() {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId != null) {
            db.collection("users").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            userNameTextView.setText(documentSnapshot.getString("name"));
                            String profileImageUrl = documentSnapshot.getString("profileImageUrl");
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
                    List<Product> inStockList = new ArrayList<>();
                    List<Product> outOfStockList = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Product product = doc.toObject(Product.class);
                        if (product.getQuantity() > 0) {
                            inStockList.add(product);
                        } else {
                            outOfStockList.add(product);
                        }
                    }

                    productList.addAll(inStockList);
                    productList.addAll(outOfStockList);
                    productAdapter.updateList(productList);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load products", Toast.LENGTH_SHORT).show());
    }

    private void addToCart(Product product) {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) {
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show();
            return;
        }

        String cartItemId = db.collection("cart").document().getId();
        CartItem cartItem = new CartItem(
                cartItemId,
                product.getProductId(),
                product.getName(),
                product.getPricePerUnit(),
                1,
                product.getImageUrl(),
                product.getCounty(),
                product.getSubCounty(),
                product.getPhone(),
                userId,
                product.getSellerId()
        );

        db.collection("cart").document(cartItemId)
                .set(cartItem)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Product added to cart!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to add to cart: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void filterProducts(String query) {
        List<Product> filteredList = new ArrayList<>();
        for (Product product : productList) {
            if (product.getName().toLowerCase().contains(query.toLowerCase()) ||
                    product.getCounty().toLowerCase().contains(query.toLowerCase()) ||
                    product.getSubCounty().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(product);
            }
        }
        productAdapter.updateList(filteredList);

        if (filteredList.isEmpty()) {
            Toast.makeText(this, "No products match your search", Toast.LENGTH_SHORT).show();
        }
    }

    private void showSignOutConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Sign Out")
                .setMessage("Are you sure you want to sign out?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    mAuth.signOut();
                    Toast.makeText(MainActivityBuyer.this, "Signed out successfully", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(MainActivityBuyer.this, LoginActivity.class));
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
    }
}