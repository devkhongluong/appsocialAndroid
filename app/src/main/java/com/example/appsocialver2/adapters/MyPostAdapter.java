package com.example.appsocialver2.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.appsocialver2.Models.Post;
import com.example.appsocialver2.R;

import java.util.List;

public class MyPostAdapter extends RecyclerView.Adapter<MyPostAdapter.MyPostViewHolder> {

    private List<Post> postList;
    private Context context;

    public MyPostAdapter(List<Post> postList, Context context) {
        this.postList = postList;
        this.context = context;
    }

    @NonNull
    @Override
    public MyPostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_canhan, parent, false);
        return new MyPostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyPostViewHolder holder, int position) {
        Post post = postList.get(position);
        Glide.with(context)
                .load(post.getImageUrl())
                .placeholder(R.drawable.bg_photo)
                .centerCrop()
                .into(holder.imgPostCaNhan);
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public static class MyPostViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPostCaNhan;

        public MyPostViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPostCaNhan = itemView.findViewById(R.id.imgPostCaNhan);
        }
    }
}
