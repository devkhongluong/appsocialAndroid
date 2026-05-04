package com.example.appsocialver2.activity;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.appsocialver2.Models.Post;
import com.example.appsocialver2.R;
import com.example.appsocialver2.adapters.PostAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;
public class SinglePostActivity extends AppCompatActivity {
    private RecyclerView rvSinglePost;
    private ImageButton btnBack;

    private PostAdapter postAdapter;
    private List<Post> singlePostList;
    private FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_post);
        // Nhận ID bài viết từ Intent do GridPostAdapter truyền sang
        String postId = getIntent().getStringExtra("POST_ID");
        rvSinglePost = findViewById(R.id.rvSinglePost);
        btnBack = findViewById(R.id.btnBackFromSinglePost);
        db = FirebaseFirestore.getInstance();
        // Setup nút Back (tắt Activity này, trở về màn hình trước đó)
        btnBack.setOnClickListener(v -> finish());
        // Setup RecyclerView dùng chung PostAdapter
        singlePostList = new ArrayList<>();
        postAdapter = new PostAdapter(singlePostList, this);
        rvSinglePost.setLayoutManager(new LinearLayoutManager(this));
        rvSinglePost.setAdapter(postAdapter);
        // Truy vấn lấy dữ liệu 1 bài duy nhất
        loadSinglePost(postId);
    }
    private void loadSinglePost(String postId) {
        if (postId == null) return;
        db.collection("Posts").document(postId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Post post = documentSnapshot.toObject(Post.class);
                        if (post != null) {
                            post.setPostId(documentSnapshot.getId());
                            singlePostList.add(post);
                            postAdapter.notifyDataSetChanged();
                        }
                    } else {
                        Toast.makeText(this, "Bài viết không còn tồn tại", Toast.LENGTH_SHORT).show();
                        finish(); // đóng màn hình nếu lỗi
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi tải bài viết", Toast.LENGTH_SHORT).show();
                });
    }
}