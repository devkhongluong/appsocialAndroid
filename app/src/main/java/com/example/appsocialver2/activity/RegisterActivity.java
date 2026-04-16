package com.example.appsocialver2.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.appsocialver2.R;
import com.example.appsocialver2.Models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegisterActivity extends BaseSensorActivity {

    private EditText editUsername, editEmail, editPassword;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private View privacyOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        editUsername = findViewById(R.id.editUsername);
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        privacyOverlay = findViewById(R.id.privacyOverlay);

        findViewById(R.id.btnRegister).setOnClickListener(v -> performRegister());
    }

    private void performRegister() {
        String username = editUsername.getText().toString();
        String email = editEmail.getText().toString();
        String password = editPassword.getText().toString();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Tạo User trên Firebase Auth
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String uid = mAuth.getCurrentUser().getUid();

                        // 2. Tạo đối tượng User để lưu vào Firestore
                        User newUser = new User(uid, username, email, "url_mac_dinh", "Hi, I'm new here!");

                        db.collection("Users").document(uid).set(newUser)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(RegisterActivity.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                                    finish();
                                });
                    } else {
                        Toast.makeText(RegisterActivity.this, "Lỗi: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    protected void onPrivacyTriggered(boolean isCovered) {
        if (isCovered) privacyOverlay.setVisibility(View.VISIBLE);
        else privacyOverlay.setVisibility(View.GONE);
    }
}