package com.example.mavunohub;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText registerEmail, registerPassword, confirmPassword;
    private Button registerButton;
    private TextView loginRedirectText;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        registerEmail = findViewById(R.id.signup_email);
        registerPassword = findViewById(R.id.signup_password);
        confirmPassword = findViewById(R.id.signup_confirm);
        registerButton = findViewById(R.id.signup_button);
        loginRedirectText = findViewById(R.id.loginRedirectText);

        registerButton.setOnClickListener(v -> createUser());
        loginRedirectText.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });


        registerPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String password = s.toString();
                if (!isValidPassword(password)) {
                    registerPassword.setError("Weak password! Use uppercase, lowercase, number, and special character.");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void createUser() {
        String email = registerEmail.getText().toString().trim();
        String password = registerPassword.getText().toString().trim();
        String confirmPass = confirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            registerEmail.setError("Email is required");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            registerPassword.setError("Password is required");
            return;
        }
        if (!isValidPassword(password)) {
            registerPassword.setError("Password must contain at least 6 characters, including an uppercase, lowercase, digit, and special character.");
            return;
        }
        if (!password.equals(confirmPass)) {
            confirmPassword.setError("Passwords do not match");
            return;
        }

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            saveUserToFirestore(user.getUid(), email);
                        }
                    } else {
                        Toast.makeText(RegisterActivity.this, "Registration Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean isValidPassword(String password) {
        String passwordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{6,}$";
        return password.matches(passwordPattern);
    }

    private void saveUserToFirestore(String userId, String email) {
        Map<String, Object> user = new HashMap<>();
        user.put("email", email);
        user.put("name", "");
        user.put("phone", "");
        user.put("county", "");
        user.put("subCounty", "");
        user.put("role", "");

        db.collection("users").document(userId)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(RegisterActivity.this, "Registration Successful! Complete your profile.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(RegisterActivity.this, UserDetailsActivity.class);
                    intent.putExtra("userId", userId);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(RegisterActivity.this, "Failed to save user details", Toast.LENGTH_SHORT).show());
    }
}