package com.example.appsocialver2.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.appsocialver2.Models.Post;
import com.example.appsocialver2.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private List<Post> postList;
    private Context context;
    private FirebaseFirestore db;

    public PostAdapter(List<Post> postList, Context context) {
        this.postList = postList;
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_posts, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = postList.get(position);
        String location = post.getLocationName();
        //Mặc định
        if (location == null || location.isEmpty()) {
            location = "Hà Nội, Việt Nam";
        }
        holder.tvDescription.setText(post.getDescription());
        holder.tvLocation.setText(post.getLocationName());
        Glide.with(context)
                .load(post.getImageUrl())
                .placeholder(R.drawable.bg_photo)
                .into(holder.imgPost);

        db.collection("users").document(post.getOwnerUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("tendn");
                        String avatar = documentSnapshot.getString("avatar");

                        holder.tvUserName.setText(name);
                        Glide.with(context)
                                .load(avatar)
                                .placeholder(R.drawable.account)
                                .into(holder.imgAvatar);
                    }
                });

        // Xử lý nút Like/Comment đơn giản
        holder.btnLike.setOnClickListener(v -> {
            // Xử lý tăng giảm lượt like trên Firestore
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatar, imgPost, btnLike, btnComment, btnMore;
        TextView tvUserName, tvLocation, tvLikeCount, tvCommentCount, tvDescription;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            imgPost = itemView.findViewById(R.id.imgPost);
            btnLike = itemView.findViewById(R.id.btnLike);
            btnComment = itemView.findViewById(R.id.btnComment);
            btnMore = itemView.findViewById(R.id.btnMore);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvLikeCount = itemView.findViewById(R.id.tvLikeCount);
            tvCommentCount = itemView.findViewById(R.id.tvCommentCount);
            tvDescription = itemView.findViewById(R.id.tvDescription);
        }
    }
}
