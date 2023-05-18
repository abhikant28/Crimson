package com.akw.crimson.Backend.Adapters;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.akw.crimson.Backend.Constants;
import com.akw.crimson.Chat.MessageAttachment;
import com.akw.crimson.R;

public class ChatAttachment_MediaListAdapter extends RecyclerView.Adapter<ChatAttachment_MediaListAdapter.ViewHolder> {
    public int prevPos = 0;
    ImageView iv_image;
    VideoView vv_video;
    EditText et_msg;
    Context cxt;
    View prevView;

    public ChatAttachment_MediaListAdapter(ImageView iv_image, VideoView vv_video, EditText et_msg, Context cxt) {
        this.iv_image = iv_image;
        this.vv_video = vv_video;
        this.et_msg = et_msg;
        this.cxt = cxt;
    }

    @NonNull
    @Override
    public ChatAttachment_MediaListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.media_list_item, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatAttachment_MediaListAdapter.ViewHolder holder, int position) {
        final int p = position;
        ContentResolver cR = cxt.getContentResolver();
        if (MessageAttachment.requestCodes.get(position) == Constants.KEY_INTENT_REQUEST_CODE_MEDIA) {
            if (cR.getType(Uri.parse(MessageAttachment.mediaUris.get(position))).startsWith("video/")) {
                holder.imageView.setVisibility(View.GONE);
                holder.videoView.setVideoURI(Uri.parse(MessageAttachment.mediaUris.get(position)));
            } else {
                holder.imageView.setImageURI(Uri.parse(MessageAttachment.mediaUris.get(position)));
                holder.videoView.setVisibility(View.GONE);
            }
        }
        holder.itemView.setOnClickListener(view -> {

            MessageAttachment.msgText.put(prevPos, et_msg.getText().toString());
            et_msg.setText("");
            if (MessageAttachment.msgText.containsKey(prevPos))
                et_msg.setText(MessageAttachment.msgText.get(position));
            if (prevView != null) prevView.setVisibility(View.GONE);
            holder.selectedFrame.setVisibility(View.VISIBLE);
            if (cR.getType(Uri.parse(MessageAttachment.mediaUris.get(position))).startsWith("video/")) {
                iv_image.setVisibility(View.GONE);
                vv_video.setVideoURI(Uri.parse(MessageAttachment.mediaUris.get(position)));
            } else {
                iv_image.setImageURI(Uri.parse(MessageAttachment.mediaUris.get(position)));
                vv_video.setVisibility(View.GONE);
            }
            prevView = holder.selectedFrame;
            prevPos = p;
        });
    }

    @Override
    public int getItemCount() {
        return MessageAttachment.mediaUris.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        VideoView videoView;
        View selectedFrame;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.mediaListItem_iv_media);
            videoView = itemView.findViewById(R.id.mediaListItem_vv_media);
            selectedFrame = itemView.findViewById(R.id.mediaListItem_v_selectedFrame);
        }
    }
}
