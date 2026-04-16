package com.example.appsocialver2.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.appsocialver2.activity.MainActivity;
import com.example.appsocialver2.R;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends BaseSensorActivity {

    private EditText editEmail, editPassword;
    private FirebaseAuth mAuth;
    private View privacyOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login2);

        mAuth = FirebaseAuth.getInstance();

        // Kiểm tra nếu đã đăng nhập rồi thì vào thẳng Main
        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }

        editEmail = findViewById(R.id.editLoginEmail);
        editPassword = findViewById(R.id.editLoginPassword);
        privacyOverlay = findViewById(R.id.privacyOverlay);

        findViewById(R.id.btnLogin).setOnClickListener(v -> performLogin());

        findViewById(R.id.txtGoToRegister).setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    private void performLogin() {
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Vui lòng nhập Email và Mật khẩu", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Đăng nhập thất bại: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    /**
     * Triển khai từ BaseSensorActivity: Ẩn thông tin khi bị che cảm biến
     */
    @Override
    protected void onPrivacyTriggered(boolean isCovered) {
        if (privacyOverlay != null) {
            if (isCovered) {
                privacyOverlay.setVisibility(View.VISIBLE);
            } else {
                privacyOverlay.setVisibility(View.GONE);
            }
        }
    }
}