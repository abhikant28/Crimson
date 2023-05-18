package com.akw.crimson.Backend.Adapters;

import android.content.Context;
import android.net.Uri;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.akw.crimson.Backend.AppObjects.Message;
import com.akw.crimson.Backend.Constants;
import com.akw.crimson.Backend.Database.TheViewModel;
import com.akw.crimson.Backend.UsefulFunctions;
import com.akw.crimson.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MediaViewAdapter extends RecyclerView.Adapter<MediaViewAdapter.MediaViewHolder> {

    private final Context mContext;
    private List<Message> medias = new ArrayList<>();
    private LayoutInflater inflater;
    private TheViewModel db;
    private Toolbar toolbar;


    public MediaViewAdapter(Context applicationContext, List<Message> mediaUri) {
        mContext = applicationContext;
        this.medias = mediaUri;
        this.db = null;
    }

    public MediaViewAdapter(Context applicationContext, List<Message> mediaUri, TheViewModel db, Toolbar toolBar) {
        mContext = applicationContext;
        this.medias = mediaUri;
        this.db = db;
        this.toolbar = toolBar;
    }

    @NonNull
    @Override
    public MediaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View itemView = LayoutInflater.from(context).inflate(R.layout.item_media, parent, false);

        return new MediaViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MediaViewHolder holder, int position) {
        // Bind data to the view holder

        holder.linearLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        holder.linearLayout.setGravity(Gravity.CENTER);
        Message msg = medias.get(position);
        File file = UsefulFunctions.getFile(mContext, msg.getMediaID()
                , msg.getMediaType(), msg.isSelf());
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        if (db != null) {
            if (msg.isSelf()) {
                toolbar.setTitle("You");
            } else {
                toolbar.setTitle(db.getUser(msg.getUser_id()).getDisplayName());
            }
            toolbar.setSubtitle(msg.getDate() + ", " + msg.getTime());
        }
        if (msg.getMediaType() == Constants.KEY_MESSAGE_MEDIA_TYPE_IMAGE || msg.getMediaType() == Constants.KEY_MESSAGE_MEDIA_TYPE_CAMERA_IMAGE) {
            ImageView imageView = new ImageView(mContext);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageView.setImageURI(Uri.parse(file.toURI().toString()));
            imageView.setLayoutParams(params);
            holder.linearLayout.addView(imageView, 0);
        } else if (msg.getMediaType() == Constants.KEY_MESSAGE_MEDIA_TYPE_VIDEO || msg.getMediaType() == Constants.KEY_MESSAGE_MEDIA_TYPE_CAMERA_VIDEO) {

            // Video item
            VideoView videoView = new VideoView(mContext);
            holder.linearLayout.addView(videoView, 0);
            holder.controller = new MediaController(mContext);
            holder.controller.setAnchorView(videoView);

            videoView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
            videoView.setMediaController(holder.controller);
            videoView.setVideoURI(Uri.fromFile(file));
            videoView.seekTo(10);
        }
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
    }

    @Override
    public void onViewRecycled(@NonNull MediaViewHolder holder) {
        super.onViewRecycled(holder);
        holder.linearLayout.removeAllViews();
    }

    @Override
    public int getItemCount() {
        return medias.size();
    }

    public class MediaViewHolder extends RecyclerView.ViewHolder {
        private final LinearLayout linearLayout;
        private MediaController controller;

        public MediaViewHolder(@NonNull View itemView) {
            super(itemView);
            // Initialize views based on the layout type
            linearLayout = itemView.findViewById(R.id.mediaView_linearLayout);
        }
    }

}
