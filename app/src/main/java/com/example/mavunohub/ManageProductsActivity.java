package com.example.mavunohub;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class ManageProductsActivity extends AppCompatActivity implements ProductAdapterFarmer.OnProductClickListener {

    private RecyclerView rvProducts;
    private FloatingActionButton fabAddProduct;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private ProductAdapterFarmer productAdapterFarmer;
    private ArrayList<Product> productList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_products);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Initialize UI components
        rvProducts = findViewById(R.id.rvProducts);
        fabAddProduct = findViewById(R.id.fabAddProduct);
        rvProducts.setLayoutManager(new LinearLayoutManager(this));
        productList = new ArrayList<>();

        // Initialize adapter with all 3 arguments (this activity implements OnProductClickListener)
        productAdapterFarmer = new ProductAdapterFarmer(this, productList, this);
        rvProducts.setAdapter(productAdapterFarmer);

        // Load products
        loadProducts();

        // Floating button to add a new product
        fabAddProduct.setOnClickListener(v -> {
            startActivity(new Intent(ManageProductsActivity.this, AddProductActivity.class));
        });
    }

    private void loadProducts() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        // Reference to products uploaded by the logged-in farmer
        CollectionReference productsRef = db.collection("products");
        productsRef.whereEqualTo("sellerId", user.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        productList.clear();
                        for (DocumentSnapshot document : task.getResult()) {
                            Product product = document.toObject(Product.class);
                            if (product != null) {
                                product.setId(document.getId()); // Store document ID for edits/deletes
                                productList.add(product);
                            }
                        }
                        productAdapterFarmer.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "Failed to load products", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Handle Edit Product Click
    @Override
    public void onEditClick(Product product) {
        Intent editIntent = new Intent(this, EditProductActivity.class);
        editIntent.putExtra("productId", product.getId()); // Pass product ID for editing
        startActivity(editIntent);
    }

    // Handle Delete Product Click
    @Override
    public void onDeleteClick(Product product) {
        // Delete product from Firestore
        db.collection("products").document(product.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Product deleted successfully", Toast.LENGTH_SHORT).show();
                    loadProducts(); // Refresh the list
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to delete product", Toast.LENGTH_SHORT).show();
                });
    }

    // Refresh products when returning from Add/Edit
    @Override
    protected void onResume() {
        super.onResume();
        loadProducts();
    }
}