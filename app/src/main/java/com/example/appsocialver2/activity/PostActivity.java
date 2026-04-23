package com.example.appsocialver2.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.core.resolutionselector.ResolutionSelector;
import androidx.camera.core.resolutionselector.ResolutionStrategy;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import android.util.Size;
import androidx.core.content.ContextCompat;

import com.example.appsocialver2.R;
import com.example.appsocialver2.Models.Post;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.util.concurrent.ExecutionException;

public class PostActivity extends BaseSensorActivity {

    // UI
    private PreviewView viewFinder;
    private ImageView imgDemo;
    private EditText editDescription;
    private TextView txtLocation;
    private View privacyOverlay, overlayHeader;
    private ImageButton btnCapture, btnGallery, btnCancel, btnPickLocation;
    private ExtendedFloatingActionButton btnPost;

    // Camera
    private ImageCapture imageCapture;

    // State
    private Uri finalImageUri;
    private boolean isFromGallery = false;
    private Sensor lightSensor;

    // ── Runtime Permission ──────────────────────────────────────────────────
    private final ActivityResultLauncher<String> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    startCamera();
                } else {
                    Toast.makeText(this, "Cần quyền Camera để sử dụng tính năng này", Toast.LENGTH_LONG).show();
                    finish();
                }
            });

    // ── Gallery Picker ──────────────────────────────────────────────────────
    private final ActivityResultLauncher<String> mGetContent =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    isFromGallery = true;
                    showPreview(uri);
                }
            });

    // ───────────────────────────────────────────────────────────────────────
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        initUI();
        setupNavigation();
        setupButtons();

        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        // Kiểm tra quyền camera tại runtime (bắt buộc Android 6+)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void initUI() {
        viewFinder      = findViewById(R.id.viewFinder);
        imgDemo         = findViewById(R.id.imgDemo);
        editDescription = findViewById(R.id.editDescription);
        txtLocation     = findViewById(R.id.txtLocation);
        privacyOverlay  = findViewById(R.id.privacyOverlay);
        overlayHeader   = findViewById(R.id.overlayHeader);
        btnCapture      = findViewById(R.id.btnCapture);
        btnGallery      = findViewById(R.id.btnGallery);
        btnCancel       = findViewById(R.id.btnCancel);
        btnPost         = findViewById(R.id.btnPost);
        btnPickLocation = findViewById(R.id.btnPickLocation);
    }

    private void setupButtons() {
        btnCapture.setOnClickListener(v -> takePhoto());
        btnGallery.setOnClickListener(v -> mGetContent.launch("image/*"));
        btnCancel.setOnClickListener(v -> resetToCapture());
        btnPost.setOnClickListener(v -> uploadPost());
        btnPickLocation.setOnClickListener(v -> pickLocationManually());
    }

    /** Bottom nav điều hướng giống MainActivity */
    private void setupNavigation() {
        findViewById(R.id.home).setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
        findViewById(R.id.friend).setOnClickListener(v -> {
            startActivity(new Intent(this, BanBe.class));
            finish();
        });
        // btnNavCamera → trang hiện tại, không cần làm gì
        findViewById(R.id.btnNavChat).setOnClickListener(v -> {
            startActivity(new Intent(this, ListFriendsChatActivity.class));
            finish();
        });
        // btnNavProfile: chưa implement
    }

    // ── State: Camera Mode → Preview Mode ──────────────────────────────────
    private void showPreview(Uri uri) {
        finalImageUri = uri;

        // Ẩn camera view + nút camera/gallery
        viewFinder.setVisibility(View.GONE);
        btnCapture.setVisibility(View.GONE);
        btnGallery.setVisibility(View.GONE);

        // Hiện ảnh + các thành phần preview
        imgDemo.setVisibility(View.VISIBLE);
        imgDemo.setImageURI(uri);
        btnPost.setVisibility(View.VISIBLE);
        editDescription.setVisibility(View.VISIBLE);
        overlayHeader.setVisibility(View.VISIBLE);
        btnCancel.setVisibility(View.VISIBLE);

        // btnPickLocation chỉ hiện khi chọn từ thư viện
        btnPickLocation.setVisibility(isFromGallery ? View.VISIBLE : View.GONE);

        fetchLocation();
    }

    // ── State: Preview Mode → Camera Mode ──────────────────────────────────
    private void resetToCapture() {
        viewFinder.setVisibility(View.VISIBLE);
        btnCapture.setVisibility(View.VISIBLE);
        btnGallery.setVisibility(View.VISIBLE);

        imgDemo.setVisibility(View.GONE);
        btnPost.setVisibility(View.GONE);
        editDescription.setVisibility(View.GONE);
        overlayHeader.setVisibility(View.GONE);
        btnCancel.setVisibility(View.GONE);
        btnPickLocation.setVisibility(View.GONE);

        editDescription.setText("");
        isFromGallery = false;
        finalImageUri = null;
    }

    // ── Camera ──────────────────────────────────────────────────────────────
    private void startCamera() {
        // Giới hạn preview 720p — giảm CPU ~30-40% so với full resolution
        ResolutionSelector resolutionSelector = new ResolutionSelector.Builder()
                .setResolutionStrategy(new ResolutionStrategy(
                        new Size(1280, 720),
                        ResolutionStrategy.FALLBACK_RULE_CLOSEST_LOWER_THEN_HIGHER))
                .build();

        ListenableFuture<ProcessCameraProvider> future = ProcessCameraProvider.getInstance(this);
        future.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = future.get();

                Preview preview = new Preview.Builder()
                        .setResolutionSelector(resolutionSelector)
                        .build();
                preview.setSurfaceProvider(viewFinder.getSurfaceProvider());

                // MINIMIZE_LATENCY: ưu tiên tốc độ chụp thay vì chất lượng tối đa
                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .setResolutionSelector(resolutionSelector)
                        .build();

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void takePhoto() {
        if (imageCapture == null) return;
        File photoFile = new File(getExternalCacheDir(), System.currentTimeMillis() + ".jpg");
        ImageCapture.OutputFileOptions options = new ImageCapture.OutputFileOptions.Builder(photoFile).build();
        imageCapture.takePicture(options, ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults r) {
                        isFromGallery = false;
                        showPreview(Uri.fromFile(photoFile));
                    }
                    @Override
                    public void onError(@NonNull ImageCaptureException e) {
                        Toast.makeText(PostActivity.this, "Lỗi chụp ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ── Location ─────────────────────────────────────────────────────────
    private void fetchLocation() {
        // TODO: Thay bằng FusedLocationProviderClient thực tế
        txtLocation.setText("Hà Nội, Việt Nam");
    }

    /** Chọn vị trí thủ công qua Google Maps — sẽ tích hợp sau */
    private void pickLocationManually() {
        Toast.makeText(this, "Tích hợp Google Maps đang được phát triển", Toast.LENGTH_SHORT).show();
    }

    // ── Upload Post ──────────────────────────────────────────────────────
    private void uploadPost() {
        if (finalImageUri == null) return;
        String desc = editDescription.getText().toString().trim();
        String loc  = txtLocation.getText().toString().trim();
        String uid  = FirebaseAuth.getInstance().getUid();

        // TODO: Upload ảnh lên Firebase Storage để lấy downloadUrl thực sự
        Post newPost = new Post(null, uid, finalImageUri.toString(), desc, loc);
        FirebaseFirestore.getInstance().collection("Posts").add(newPost)
                .addOnSuccessListener(ref -> {
                    Toast.makeText(this, "Đã đăng bài thành công!", Toast.LENGTH_SHORT).show();
                    resetToCapture();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi đăng bài: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // ── Sensor ───────────────────────────────────────────────────────────
    @Override
    public void onSensorChanged(SensorEvent event) {
        super.onSensorChanged(event);
        if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            float lux = event.values[0];
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.screenBrightness = (lux < 100)
                    ? 0.9f
                    : WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
            getWindow().setAttributes(lp);
        }
    }

    @Override
    protected void onPrivacyTriggered(boolean isCovered) {
        privacyOverlay.setVisibility(isCovered ? View.VISIBLE : View.GONE);
        btnPost.setEnabled(!isCovered);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (lightSensor != null) {
            // SENSOR_DELAY_NORMAL (~200ms) thay vì SENSOR_DELAY_UI (~16ms) — giảm tải CPU
            sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause(); // BaseSensorActivity unregister proximitySensor
        if (lightSensor != null) {
            sensorManager.unregisterListener(this, lightSensor); // Fix: unregister lightSensor
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // CameraX tự giải phóng khi lifecycle kết thúc — không cần xử lý thêm
    }
}