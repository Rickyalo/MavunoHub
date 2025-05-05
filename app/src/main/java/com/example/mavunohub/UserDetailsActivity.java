package com.example.mavunohub;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UserDetailsActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView ivProfilePicture;
    private Uri imageUri;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    private EditText etName, etPhone, etCounty, etSubCounty;
    private RadioGroup loginTypeGroup;
    private Button btnUpdate, btnUploadPicture;

    private String profileImageUrl = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        ivProfilePicture = findViewById(R.id.ivProfilePicture);
        btnUploadPicture = findViewById(R.id.btnUploadPicture);
        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        etCounty = findViewById(R.id.etCounty);
        etSubCounty = findViewById(R.id.etSubCounty);
        loginTypeGroup = findViewById(R.id.loginTypeGroup);
        btnUpdate = findViewById(R.id.btnUpdate);

        btnUploadPicture.setOnClickListener(v -> chooseImage());
        btnUpdate.setOnClickListener(v -> {
            if (imageUri != null) {
                uploadImageAndSaveData();
            } else {
                saveUserData(profileImageUrl);
            }
        });
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                ivProfilePicture.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadImageAndSaveData() {
        StorageReference ref = storageReference.child("profile_pictures/" + UUID.randomUUID().toString());
        ref.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl().addOnSuccessListener(uri -> {
                    profileImageUrl = uri.toString();
                    saveUserData(profileImageUrl);
                }))
                .addOnFailureListener(e -> Toast.makeText(UserDetailsActivity.this, "Image Upload Failed!", Toast.LENGTH_SHORT).show());
    }

    private void saveUserData(String imageUrl) {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String county = etCounty.getText().toString().trim();
        String subCounty = etSubCounty.getText().toString().trim();
        int selectedLoginType = loginTypeGroup.getCheckedRadioButtonId();

        if (name.isEmpty() || phone.isEmpty() || county.isEmpty() || subCounty.isEmpty() || selectedLoginType == -1) {
            Toast.makeText(this, "Please fill all fields!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!phone.matches("\\d{10}")) {
            Toast.makeText(this, "Enter a valid 10-digit phone number!", Toast.LENGTH_SHORT).show();
            return;
        }

        String userType = ((RadioButton) findViewById(selectedLoginType)).getText().toString();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            Map<String, Object> userData = new HashMap<>();
            userData.put("name", name);
            userData.put("phone", phone);
            userData.put("county", county);
            userData.put("subCounty", subCounty);
            userData.put("userType", userType);
            userData.put("profileImageUrl", imageUrl);

            if (userType.equals("Farmer")) {
                userData.put("farmerId", userId);
            }

            DocumentReference userRef = db.collection("users").document(userId);
            userRef.set(userData)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(UserDetailsActivity.this, "Profile Updated!", Toast.LENGTH_SHORT).show();
                            redirectToMainActivity(name, userType);
                        } else {
                            Toast.makeText(UserDetailsActivity.this, "Failed to Save Data!", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void redirectToMainActivity(String name, String userType) {
        Intent intent;
        if (userType.equals("Farmer")) {
            intent = new Intent(UserDetailsActivity.this, MainActivityFarmer.class);
        } else {
            intent = new Intent(UserDetailsActivity.this, MainActivityBuyer.class);
        }
        intent.putExtra("user_name", name);
        startActivity(intent);
        finish();
    }
}