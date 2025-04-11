package com.example.mavunohub;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AddProductActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private EditText etProductName, etProductPrice, etProductQuantity, etPhoneNumber, etCounty, etSubCounty;
    private Spinner spinnerUnit;
    private Button btnUploadImage, btnSubmitProduct;
    private ImageView ivProductImage;
    private Uri imageUri;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        // Initialize Firebase components
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference("product_images");

        // Bind XML views
        etProductName = findViewById(R.id.etProductName);
        etProductPrice = findViewById(R.id.etProductPrice);
        etProductQuantity = findViewById(R.id.etProductQuantity);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etCounty = findViewById(R.id.etCounty);
        etSubCounty = findViewById(R.id.etSubCounty);
        btnUploadImage = findViewById(R.id.btnUploadImage);
        btnSubmitProduct = findViewById(R.id.btnSubmitProduct);
        ivProductImage = findViewById(R.id.ivProductImage);
        spinnerUnit = findViewById(R.id.spinnerUnit);

        // Spinner options for units
        String[] units = {"Kgs", "Litres", "Crates","Animals"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, units);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUnit.setAdapter(adapter);

        // Progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading product...");
        progressDialog.setCancelable(false);

        // Set listeners
        btnUploadImage.setOnClickListener(v -> openImageChooser());
        btnSubmitProduct.setOnClickListener(v -> submitProduct());
    }

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Product Image"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            ivProductImage.setImageURI(imageUri);
        } else {
            Toast.makeText(this, "No image selected!", Toast.LENGTH_SHORT).show();
        }
    }

    private void submitProduct() {
        String productName = etProductName.getText().toString().trim();
        String productPrice = etProductPrice.getText().toString().trim();
        String productQuantity = etProductQuantity.getText().toString().trim();
        String productUnit = spinnerUnit.getSelectedItem().toString();
        String phoneNumber = etPhoneNumber.getText().toString().trim();
        String county = etCounty.getText().toString().trim();
        String subCounty = etSubCounty.getText().toString().trim();

        // Validate inputs
        if (productName.isEmpty() || productPrice.isEmpty() || productQuantity.isEmpty() ||
                phoneNumber.isEmpty() || county.isEmpty() || subCounty.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double pricePerUnit;
        int quantity;
        try {
            pricePerUnit = Double.parseDouble(productPrice);
            quantity = Integer.parseInt(productQuantity);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Enter valid numeric values", Toast.LENGTH_SHORT).show();
            return;
        }

        if (imageUri == null) {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.show();

        StorageReference fileRef = storageReference.child(UUID.randomUUID().toString());
        fileRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
            String imageUrl = uri.toString();
            saveProductToFirestore(productName, pricePerUnit, quantity, productUnit, phoneNumber, county, subCounty, imageUrl);
        })).addOnFailureListener(e -> {
            progressDialog.dismiss();
            Toast.makeText(AddProductActivity.this, "Image upload failed!", Toast.LENGTH_SHORT).show();
        });
    }

    private void saveProductToFirestore(String name, double pricePerUnit, int quantity, String unit,
                                        String phone, String county, String subCounty, String imageUrl) {
        String userId = mAuth.getCurrentUser().getUid();
        double totalPrice = pricePerUnit * quantity;

        Map<String, Object> product = new HashMap<>();
        product.put("name", name);
        product.put("pricePerUnit", pricePerUnit);
        product.put("quantity", quantity);
        product.put("totalPrice", totalPrice);
        product.put("unit", unit);
        product.put("phone", phone);
        product.put("county", county);
        product.put("subCounty", subCounty);
        product.put("imageUrl", imageUrl);
        product.put("sellerId", userId);

        db.collection("products").add(product).addOnSuccessListener(documentReference -> {
            progressDialog.dismiss();
            Toast.makeText(AddProductActivity.this, "Product added successfully!", Toast.LENGTH_SHORT).show();
            finish();
        }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            Toast.makeText(AddProductActivity.this, "Failed to add product!", Toast.LENGTH_SHORT).show();
        });
    }
}