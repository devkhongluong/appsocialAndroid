package com.example.appsocialver2.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appsocialver2.Models.Post;
import com.example.appsocialver2.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;
public class MainActivity extends BaseSensorActivity {

    private RecyclerView rvPosts;
    private List<Post> postList;
    private FirebaseFirestore db;
    private View privacyOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Khởi tạo UI và Firebase [cite: 18, 109]
        db = FirebaseFirestore.getInstance();
        rvPosts = findViewById(R.id.rvPosts);
        privacyOverlay = findViewById(R.id.privacyOverlay);

        postList = new ArrayList<>();
        rvPosts.setLayoutManager(new LinearLayoutManager(this));

        // Load bài viết từ Firestore theo thời gian thực [cite: 36, 190]
        loadPosts();

        // Nút Camera điều hướng sang PostActivity [cite: 133, 224]
        findViewById(R.id.btnNavCamera).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, PostActivity.class));
        });
        findViewById(R.id.home).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, MainActivity.class));
        });
        findViewById(R.id.friend).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, BanBe.class));
        });
        findViewById(R.id.btnNavChat).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, ListFriendsChatActivity.class));
        });
        findViewById(R.id.btnNavProfile).setOnClickListener(v -> {
            //startActivity(new Intent(MainActivity.this, Profile.class));
        });
    }

    private void loadPosts() {
        db.collection("Posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (value != null) {
                        postList.clear();
                        // Logic cập nhật List và Adapter ở đây [cite: 164, 165]
                    }
                });
    }

    @Override
    protected void onPrivacyTriggered(boolean isCovered) {
        if (isCovered) {
            privacyOverlay.setVisibility(View.VISIBLE); // Phủ màn hình đen [cite: 91, 151]
            rvPosts.setAlpha(0.0f); // Ẩn hoàn toàn nội dung bài viết
        } else {
            privacyOverlay.setVisibility(View.GONE);
            rvPosts.setAlpha(1.0f);
        }
    }
}