package com.akw.crimson.Gallery.GalleryAdapters;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.akw.crimson.Gallery.AllPhotos_fragment;
import com.akw.crimson.R;

import java.util.ArrayList;
import java.util.List;

public class MediaCursorRecyclerViewAdapter extends RecyclerView.Adapter<MediaCursorRecyclerViewAdapter.ViewHolder> {

    ArrayList<String>  mCursorAdapter;
    OnImageClickListener mOnImageClickListener;
    Context mContext;


    public MediaCursorRecyclerViewAdapter(ArrayList<String> mCursorAdapter, OnImageClickListener mOnImageClickListener, Context mContext) {
        this.mCursorAdapter = mCursorAdapter;
        this.mOnImageClickListener = mOnImageClickListener;
        this.mContext = mContext;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
            ImageView v1;
            OnImageClickListener onImageClickListener;

            public ViewHolder(View itemView, OnImageClickListener onImageClickListener) {
                super(itemView);
                v1 = itemView.findViewById(R.id.mediaListItem_iv_media);
                this.onImageClickListener=onImageClickListener;
                itemView.setOnClickListener(this);
            }

            @Override
                public void onClick(View view) {
                    onImageClickListener.onImageClick(getAdapterPosition());
                }
            }


        @Override
        public int getItemCount() {
            return mCursorAdapter.size();
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            // Passing the binding operation to cursor loader
            mCursorAdapter.get(position);

            //Log.i("ALL ::::", mCursorAdapter.getCursor().getString(mCursorAdapter.getCursor().getColumnIndexOrThrow(MediaStore.Images.Media.DATA)));
            holder.v1.setImageURI(Uri.parse(mCursorAdapter.get(position)));

        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // Passing the inflater job to the cursor-adapter
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.main_chatlist_item, parent, false);
            return new ViewHolder(itemView, mOnImageClickListener);
        }
    public interface OnImageClickListener {
        void onImageClick(int position);
    }
    }