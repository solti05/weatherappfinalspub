package com.example.weatherappfinals;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AnnouncementAdapter extends RecyclerView.Adapter<AnnouncementAdapter.ViewHolder> {

    private final List<Article> articles;
    private final Context context;

    public AnnouncementAdapter(Context context, List<Article> articles) {
        this.context = context;
        this.articles = articles;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_announcement_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Article article = articles.get(position);

        // --- Title & Description ---
        holder.tvTitle.setText(article.title != null ? article.title : "Untitled Announcement");
        holder.tvDescription.setText(article.description != null ? article.description : "No description available");

        // --- Date Formatting (CurrentsAPI uses 'published') ---
        try {
            if (article.published != null) {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM dd, yyyy, h:mm a", Locale.getDefault());
                Date date = inputFormat.parse(article.published);
                holder.tvDate.setText(outputFormat.format(date));
            } else {
                holder.tvDate.setText("—");
            }
        } catch (Exception e) {
            holder.tvDate.setText("—");
        }

        // --- Image Loading ---
        String imageUrl = article.image != null ? article.image : null;
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(imageUrl)
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.ic_announcements)
                    .error(R.drawable.ic_announcements)
                    .into(holder.imgNews);
        } else {
            holder.imgNews.setImageResource(R.drawable.ic_announcements);
        }

        // --- Open in Browser ---
        holder.itemView.setOnClickListener(v -> {
            if (article.url != null && !article.url.isEmpty()) {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(article.url));
                    context.startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        // --- Share Button ---
        holder.btnShare.setOnClickListener(v -> {
            if (article.url != null && !article.url.isEmpty()) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, article.title);
                shareIntent.putExtra(Intent.EXTRA_TEXT, article.title + "\n" + article.url);
                context.startActivity(Intent.createChooser(shareIntent, "Share Announcement"));
            }
        });
    }

    @Override
    public int getItemCount() {
        return articles.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDescription, tvDate;
        ImageView imgNews, btnShare;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvDate = itemView.findViewById(R.id.tvDate);
            imgNews = itemView.findViewById(R.id.imgNews);
            btnShare = itemView.findViewById(R.id.btnShare);
        }
    }
}
