package com.example.chatapp;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Message> messageList;
    private String currentUserId;

    public MessageAdapter(List<Message> messageList, String currentUserId) {
        this.messageList = messageList;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messageList.get(position);

        // Reset all views visibility
        holder.senderMessageText.setVisibility(View.GONE);
        holder.receiverMessageText.setVisibility(View.GONE);
        holder.senderVideoThumbnail.setVisibility(View.GONE);
        holder.receiverVideoThumbnail.setVisibility(View.GONE);

        if (message.getSender().equals(currentUserId)) {
            // Sender message
            if (message.getVideoUrl() != null) {
                // Sender video message
                holder.senderVideoThumbnail.setVisibility(View.VISIBLE);

                // Load and display the thumbnail using Glide
                Glide.with(holder.itemView.getContext())
                        .load(message.getVideoUrl())
                        .into(holder.senderVideoThumbnail);

                holder.senderVideoThumbnail.setOnClickListener(v -> {
                    Intent intent = new Intent(holder.itemView.getContext(), VideoPlayerActivity.class);
                    intent.putExtra("videoUrl", message.getVideoUrl());
                    holder.itemView.getContext().startActivity(intent);
                });
            } else if (message.getText().startsWith("https://maps.google.com")) {
                // Sender location message
                holder.senderMessageText.setVisibility(View.VISIBLE);
                holder.senderMessageText.setText("Shared Location");
                holder.senderMessageText.setOnClickListener(v -> {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(message.getText()));
                    holder.itemView.getContext().startActivity(intent);
                });
            } else {
                // Sender regular text message
                holder.senderMessageText.setVisibility(View.VISIBLE);
                holder.senderMessageText.setText(message.getText());
            }
        } else {
            // Receiver message
            if (message.getVideoUrl() != null) {
                // Receiver video message
                holder.receiverVideoThumbnail.setVisibility(View.VISIBLE);

                // Load and display the thumbnail using Glide
                Glide.with(holder.itemView.getContext())
                        .load(message.getVideoUrl())
                        .into(holder.receiverVideoThumbnail);

                holder.receiverVideoThumbnail.setOnClickListener(v -> {
                    Intent intent = new Intent(holder.itemView.getContext(), VideoPlayerActivity.class);
                    intent.putExtra("videoUrl", message.getVideoUrl());
                    holder.itemView.getContext().startActivity(intent);
                });
            } else if (message.getText().startsWith("https://maps.google.com")) {
                // Receiver location message
                holder.receiverMessageText.setVisibility(View.VISIBLE);
                holder.receiverMessageText.setText("Shared Location");
                holder.receiverMessageText.setOnClickListener(v -> {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(message.getText()));
                    holder.itemView.getContext().startActivity(intent);
                });
            } else {
                // Receiver regular text message
                holder.receiverMessageText.setVisibility(View.VISIBLE);
                holder.receiverMessageText.setText(message.getText());
            }
        }
    }
    /**
     * Generate and display a video thumbnail in an ImageView.
     *
     * @param imageView ImageView to display the thumbnail.
     * @param videoUrl  URL of the video.
     */
    private void generateVideoThumbnail(ImageView imageView, String videoUrl) {
        Glide.with(imageView.getContext())
                .load(Uri.parse(videoUrl))
                .into(imageView);
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView senderMessageText, receiverMessageText;
        ImageView senderVideoThumbnail, receiverVideoThumbnail;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            senderMessageText = itemView.findViewById(R.id.senderMessageText);
            receiverMessageText = itemView.findViewById(R.id.receiverMessageText);
            // Initialize VideoView for video messages
            senderVideoThumbnail = itemView.findViewById(R.id.senderVideoThumbnail);
            receiverVideoThumbnail = itemView.findViewById(R.id.receiverVideoThumbnail);

        }
    }
}
