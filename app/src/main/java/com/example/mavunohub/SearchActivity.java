package com.example.mavunohub;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import android.util.Log;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private RecyclerView searchRecyclerView;
    private ProductAdapterBuyer productAdapter;
    private List<Product> productList;
    private FirebaseFirestore db;
    private EditText searchInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Initialize Firebase and RecyclerView
        db = FirebaseFirestore.getInstance();
        searchRecyclerView = findViewById(R.id.searchRecyclerView);
        searchInput = findViewById(R.id.searchInput);

        // Initialize product list and adapter
        productList = new ArrayList<>();
        productAdapter = new ProductAdapterBuyer(this, productList, new ProductAdapterBuyer.OnProductClickListener() {
            @Override
            public void onProductClick(Product product) {
                Toast.makeText(SearchActivity.this, "Clicked: " + product.getName(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAddToCartClick(Product product) {
                addToCartWithDynamicFields(product);
            }
        });

        searchRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        searchRecyclerView.setAdapter(productAdapter);

        // Load all products
        loadProducts();

        // Add text change listener for search functionality
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProducts(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadProducts() {
        db.collection("products").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    productList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            Product product = document.toObject(Product.class);
                            productList.add(product);
                        } catch (Exception e) {
                            Toast.makeText(this, "Error mapping product data.", Toast.LENGTH_SHORT).show();
                        }
                    }
                    productAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load products from Firebase.", Toast.LENGTH_SHORT).show();
                });
    }

    private void filterProducts(String query) {
        List<Product> filteredList = new ArrayList<>();
        for (Product product : productList) {
            if (product.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(product);
            }
        }
        productAdapter.updateList(filteredList);
    }

    private void addToCartWithDynamicFields(Product product) {
        if (product == null || product.getId() == null) {
            Toast.makeText(this, "Failed to Add to Cart: Invalid Product.", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("CartDebug", "Attempting to add to cart: " +
                "ID = " + product.getId() +
                ", Name = " + product.getName() +
                ", Price = " + product.getPricePerUnit() +
                ", Quantity = " + product.getQuantity() +
                ", Phone = " + product.getPhone() +
                ", County = " + product.getCounty() +
                ", SubCounty = " + product.getSubCounty());

        db.collection("cart")
                .add(product)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Product added to cart!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to Add to Cart: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("CartError", "Failed to add to cart: ", e);
                });
    }
}