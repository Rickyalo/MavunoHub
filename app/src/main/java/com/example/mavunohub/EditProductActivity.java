package com.example.mavunohub;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.HashMap;
import java.util.Map;

public class EditProductActivity extends AppCompatActivity {

    private ImageView editProductImage;
    private EditText etProductName, etPricePerUnit, etQuantity;
    private Button btnChangeImage, btnSaveChanges, btnCancelEdit;
    private String productId, imageUrl;
    private Uri imageUri;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private FirebaseAuth auth;
    private static final int IMAGE_PICK_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_product);


        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        auth = FirebaseAuth.getInstance();


        editProductImage = findViewById(R.id.edit_product_image);
        etProductName = findViewById(R.id.et_product_name);
        etPricePerUnit = findViewById(R.id.et_price_per_unit);
        etQuantity = findViewById(R.id.et_quantity);
        btnChangeImage = findViewById(R.id.btn_change_image);
        btnSaveChanges = findViewById(R.id.btn_save_changes);
        btnCancelEdit = findViewById(R.id.btn_cancel_edit);


        productId = getIntent().getStringExtra("productId");
        if (productId == null) {
            Toast.makeText(this, "No product found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }


        loadProductDetails();


        btnChangeImage.setOnClickListener(v -> selectImage());


        btnSaveChanges.setOnClickListener(v -> saveChanges());


        btnCancelEdit.setOnClickListener(v -> finish());
    }

    private void loadProductDetails() {
        DocumentReference productRef = db.collection("products").document(productId);
        productRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                etProductName.setText(documentSnapshot.getString("name"));
                etPricePerUnit.setText(String.valueOf(documentSnapshot.getDouble("pricePerUnit")));
                etQuantity.setText(String.valueOf(documentSnapshot.getLong("quantity")));
                imageUrl = documentSnapshot.getString("imageUrl");
                Glide.with(this).load(imageUrl).into(editProductImage);
            }
        }).addOnFailureListener(e ->
                Toast.makeText(EditProductActivity.this, "Failed to load product", Toast.LENGTH_SHORT).show()
        );
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.getData();
            editProductImage.setImageURI(imageUri);
        }
    }

    private void saveChanges() {
        String name = etProductName.getText().toString().trim();
        String pricePerUnit = etPricePerUnit.getText().toString().trim();
        String quantity = etQuantity.getText().toString().trim();

        if (name.isEmpty() || pricePerUnit.isEmpty() || quantity.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("pricePerUnit", Double.parseDouble(pricePerUnit));
        updates.put("quantity", Integer.parseInt(quantity));

        DocumentReference productRef = db.collection("products").document(productId);

        if (imageUri != null) {
            StorageReference imageRef = storage.getReference("product_images/").child(productId + ".jpg");
            imageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        updates.put("imageUrl", uri.toString());
                        updateProduct(productRef, updates);
                    }))
                    .addOnFailureListener(e -> Toast.makeText(EditProductActivity.this, "Image upload failed", Toast.LENGTH_SHORT).show());
        } else {
            updateProduct(productRef, updates);
        }
    }

    private void updateProduct(DocumentReference productRef, Map<String, Object> updates) {
        productRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(EditProductActivity.this, "Product updated successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(EditProductActivity.this, "Failed to update product", Toast.LENGTH_SHORT).show());
    }
}
