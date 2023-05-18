package com.akw.crimson.Backend.Adapters;

import android.graphics.Color;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import com.akw.crimson.Backend.AppObjects.Message;
import com.akw.crimson.Backend.AppObjects.User;
import com.akw.crimson.Backend.Constants;
import com.akw.crimson.Backend.UsefulFunctions;
import com.akw.crimson.R;

import java.io.File;
import java.util.List;

public class MediaListAdapter extends RecyclerView.Adapter<MediaListAdapter.ViewHolder> {
    User user;
    List<Message> mediaList;

    public MediaListAdapter(User user, List<Message> mediaList) {
        this.user = user;
        this.mediaList = mediaList;
    }

    @NonNull
    @Override
    public MediaListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.media_list_item, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MediaListAdapter.ViewHolder holder, int position) {
        Message msg = mediaList.get(position);
        File file = UsefulFunctions.getFile(holder.itemView.getContext(), msg.getMediaID()
                , msg.getMediaType(), msg.isSelf());
        if (msg.getMediaType() == Constants.KEY_MESSAGE_MEDIA_TYPE_IMAGE) {
            holder.imageView.setImageURI(Uri.parse(file.toURI().toString()));
        } else if (msg.getMediaType() == Constants.KEY_MESSAGE_MEDIA_TYPE_DOCUMENT) {
            holder.imageView.setImageDrawable(AppCompatResources.getDrawable(holder.itemView.getContext(), R.drawable.ic_twotone_insert_drive_file_24));
        } else if (msg.getMediaType() == Constants.KEY_MESSAGE_MEDIA_TYPE_AUDIO) {
            holder.imageView.setBackgroundColor(Color.MAGENTA);
            holder.imageView.setImageDrawable(AppCompatResources.getDrawable(holder.itemView.getContext(), R.drawable.ic_baseline_headphones_24));
        } else if (msg.getMediaType() == Constants.KEY_MESSAGE_MEDIA_TYPE_VIDEO) {
            holder.imageView.setImageBitmap(ThumbnailUtils.createVideoThumbnail(file.getAbsolutePath(), MediaStore.Video.Thumbnails.MINI_KIND));
        }
    }

    @Override
    public int getItemCount() {
        return Math.min(10, mediaList.size());
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.mediaListItem_iv_media);
        }
    }
}
