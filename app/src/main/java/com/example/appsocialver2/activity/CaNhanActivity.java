package com.example.appsocialver2.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.appsocialver2.Models.Post;
import com.example.appsocialver2.R;
import com.example.appsocialver2.adapters.MyPostAdapter;
import com.example.appsocialver2.adapters.PostAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class CaNhanActivity extends AppCompatActivity {

    private ImageView imgAvatarProfile;
    private TextView tvProfileName, tvCountPosts, tvCountFriends, tvCountLikes;
    private RecyclerView rvMyPosts;
    private MyPostAdapter postAdapter;
    private List<Post> myPostList;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        initUI();
        initFirebase();
        setupRecyclerView();
        loadUserInfo();
        loadMyPosts();
        setupNavigation();
    }

    private void initUI() {
        imgAvatarProfile = findViewById(R.id.imgAvatarProfile);
        tvProfileName = findViewById(R.id.tvProfileName);
        tvCountPosts = findViewById(R.id.tvCountPosts);
        tvCountFriends = findViewById(R.id.tvCountFriends);
        tvCountLikes = findViewById(R.id.tvCountLikes);
        rvMyPosts = findViewById(R.id.rvMyPosts);

        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(CaNhanActivity.this, DangNhapActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void initFirebase() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            currentUserId = user.getUid();
        } else {
            // Chuyển về màn hình đăng nhập nếu chưa login
            startActivity(new Intent(this, DangNhapActivity.class));
            finish();
        }
    }

    private void setupRecyclerView() {
        myPostList = new ArrayList<>();
        postAdapter = new MyPostAdapter(myPostList, this);
        rvMyPosts.setLayoutManager(new GridLayoutManager(this, 3));
        rvMyPosts.setAdapter(postAdapter);
    }

    private void loadUserInfo() {
        if (currentUserId == null) return;

        db.collection("users").document(currentUserId)
                .addSnapshotListener((documentSnapshot, e) -> {
                    if (e != null) return;
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("tendn");
                        String avatarUrl = documentSnapshot.getString("avatar");

                        tvProfileName.setText(name != null ? name : "Người dùng");
                        if (avatarUrl != null && !avatarUrl.isEmpty()) {
                            Glide.with(this).load(avatarUrl).placeholder(R.drawable.account).into(imgAvatarProfile);
                        }
                    }
                });
        
        // Có thể load số lượng bạn bè từ collection "friends" ở đây
        db.collection("friends").whereEqualTo("uid1", currentUserId).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    tvCountFriends.setText(String.valueOf(queryDocumentSnapshots.size()));
                });
    }

    private void loadMyPosts() {
        if (currentUserId == null) return;

        db.collection("Posts")
                .whereEqualTo("ownerUid", currentUserId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("Firestore", "Lỗi: ", error);
                    }
                    if (value != null) {
                        myPostList.clear();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            Post post = doc.toObject(Post.class);
                            if (post != null) {
                                post.setPostId(doc.getId());
                                myPostList.add(post);
                            }
                        }
                        postAdapter.notifyDataSetChanged();
                        tvCountPosts.setText(String.valueOf(myPostList.size()));
                    }
                });
    }

    private void setupNavigation() {
        findViewById(R.id.home).setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
        findViewById(R.id.friend).setOnClickListener(v -> {
            startActivity(new Intent(this, BanBe.class));
            finish();
        });
        findViewById(R.id.camera).setOnClickListener(v -> {
            startActivity(new Intent(this, PostActivity.class));
        });
        // "my" là activity hiện tại nên không cần chuyển
        findViewById(R.id.chat).setOnClickListener(v -> {
            // startActivity(new Intent(this, MessengerActivity.class));
        });
    }
}
