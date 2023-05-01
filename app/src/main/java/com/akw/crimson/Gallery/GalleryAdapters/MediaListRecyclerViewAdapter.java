package com.akw.crimson.Gallery.GalleryAdapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.akw.crimson.R;

public class MediaListRecyclerViewAdapter extends RecyclerView.Adapter<MediaListRecyclerViewAdapter.ViewHolder> {

    CursorAdapter mCursorAdapter;
    OnImageClickListener mOnImageClickListener;
    Context mContext;



    public MediaListRecyclerViewAdapter(Context context, Cursor c, OnImageClickListener onImageClickListener) {
        mOnImageClickListener=onImageClickListener;
        mContext = context;
        Log.i("ALL MEDIA::::", "Constructor2");

        mCursorAdapter = new CursorAdapter(mContext, c, 0) {

            @Override
            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                // Inflate the view here
                View view = LayoutInflater.from(context).inflate(R.layout.media_list_item, parent, false);
                return view;
            }

            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                // Binding operations
//                    ImageView img=view.findViewById(R.id.MediaListItem_ImageLayout);
            }
        };
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
            return mCursorAdapter.getCount();
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            // Passing the binding operation to cursor loader
            mCursorAdapter.getCursor().moveToPosition(position);
            mCursorAdapter.bindView(holder.itemView, mContext, mCursorAdapter.getCursor());
            int i=mCursorAdapter.getCursor().getColumnIndex(MediaStore.Images.Media._ID);
            int imageId = mCursorAdapter.getCursor().getInt(i);
            Bitmap thumbnail = MediaStore.Images.Thumbnails.getThumbnail(mContext.getContentResolver(), imageId, MediaStore.Images.Thumbnails.MINI_KIND, null);

            //Log.i("ALL ::::", mCursorAdapter.getCursor().getString(mCursorAdapter.getCursor().getColumnIndexOrThrow(MediaStore.Images.Media.DATA)));
//            holder.v1.setImageURI(Uri.parse(mCursorAdapter.getCursor().getString(mCursorAdapter.getCursor().getColumnIndexOrThrow(MediaStore.Images.Media.DATA))));

            holder.v1.setImageBitmap(thumbnail);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // Passing the inflater job to the cursor-adapter
            View v = mCursorAdapter.newView(mContext, mCursorAdapter.getCursor(), parent);
            return new ViewHolder(v, mOnImageClickListener);
        }
    public interface OnImageClickListener {
        void onImageClick(int position);
    }
    }