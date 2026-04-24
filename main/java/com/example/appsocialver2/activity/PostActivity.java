package com.example.appsocialver2.activity;
import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.appsocialver2.R;
import com.example.appsocialver2.Models.Post;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class PostActivity extends BaseSensorActivity {

    private EditText editDescription;
    private TextView txtLocation;
    private Button btnPost;
    private View privacyOverlay;

    private Sensor lightSensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        // Khởi tạo UI
        editDescription = findViewById(R.id.editDescription);
        txtLocation = findViewById(R.id.txtLocation);
        btnPost = findViewById(R.id.btnPost);
        privacyOverlay = findViewById(R.id.privacyOverlay);

        // Khởi tạo cảm biến ánh sáng (Light Sensor) [cite: 136, 172]
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        // Giả lập lấy vị trí GPS [cite: 97, 281]
        fetchLocation();

        btnPost.setOnClickListener(v -> uploadPost());
    }

    private void fetchLocation() {
        // Trong thực tế, bạn sẽ dùng FusedLocationProviderClient ở đây [cite: 97, 281]
        txtLocation.setText("Đang lấy vị trí...");
        // Ví dụ kết quả:
        txtLocation.setText("Hà Nội, Việt Nam");
    }

    /**
     * Xử lý Cảm biến ánh sáng: Tự động tăng độ sáng màn hình nếu môi trường tối [cite: 136, 167, 255]
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        super.onSensorChanged(event); // Gọi để xử lý cảm biến tiệm cận ở lớp cha

        if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            float lux = event.values[0];
            WindowManager.LayoutParams layout = getWindow().getAttributes();

            if (lux < 100) { // Nếu độ sáng môi trường < 100 lux [cite: 255]
                layout.screenBrightness = 0.9f; // Tăng độ sáng màn hình lên ít nhất 80% [cite: 255]
            } else {
                layout.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
            }
            getWindow().setAttributes(layout);
        }
    }

    /**
     * Triển khai từ BaseSensorActivity: Bảo vệ quyền riêng tư [cite: 108, 221, 285]
     */
    @Override
    protected void onPrivacyTriggered(boolean isCovered) {
        if (isCovered) {
            privacyOverlay.setVisibility(View.VISIBLE); // Phủ màn hình đen [cite: 160, 221]
            btnPost.setEnabled(false); // Khóa nút đăng bài để chống "cấn máy" [cite: 161, 285]
            Toast.makeText(this, "Vui lòng bỏ vật cản để tiếp tục", Toast.LENGTH_SHORT).show();
        } else {
            privacyOverlay.setVisibility(View.GONE);
            btnPost.setEnabled(true); // Khôi phục tương tác [cite: 224, 286]
        }
    }

    private void uploadPost() {
        String desc = editDescription.getText().toString();
        String loc = txtLocation.getText().toString();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Logic đẩy dữ liệu lên Firestore [cite: 99, 180, 280]
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Post newPost = new Post(null, uid, "url_anh_mac_dinh", desc, loc);

        db.collection("Posts").add(newPost)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Đã đăng bài thành công!", Toast.LENGTH_SHORT).show();
                    finish(); // Quay lại màn hình chính [cite: 207, 305]
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (lightSensor != null) {
            sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_UI);
        }
    }
}