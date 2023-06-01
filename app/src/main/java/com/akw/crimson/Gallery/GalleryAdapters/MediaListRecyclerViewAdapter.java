package com.akw.crimson.Gallery.GalleryAdapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CursorAdapter;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.akw.crimson.R;

public class MediaListRecyclerViewAdapter extends RecyclerView.Adapter<MediaListRecyclerViewAdapter.ViewHolder> {

    CursorAdapter mCursorAdapter;
    OnImageClickListener mOnImageClickListener;
    Context mContext;
    static int width;


    public MediaListRecyclerViewAdapter(Context context, Cursor c, OnImageClickListener onImageClickListener, int colCount) {
        mOnImageClickListener = onImageClickListener;
        mContext = context;
        Log.i("ALL MEDIA::::", "Constructor3");
        width = getScreenWidth(context) / colCount;

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

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView v1;
        OnImageClickListener onImageClickListener;

        public ViewHolder(View itemView, OnImageClickListener onImageClickListener) {
            super(itemView);
            v1 = itemView.findViewById(R.id.mediaListItem_iv_media);
            this.onImageClickListener = onImageClickListener;
            itemView.setOnClickListener(this);
                ViewGroup.LayoutParams layoutParams = itemView.getLayoutParams();
                Log.i("ViewHolder.HEIGHT::::::", width+"");
                layoutParams.height = width; // Set height equal to width
                itemView.setLayoutParams(layoutParams);
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
        int i = mCursorAdapter.getCursor().getColumnIndex(MediaStore.Images.Media._ID);
        int imageId = mCursorAdapter.getCursor().getInt(i);
        Bitmap thumbnail = MediaStore.Images.Thumbnails.getThumbnail(mContext.getContentResolver(), imageId, MediaStore.Images.Thumbnails.MINI_KIND, null);

        //Log.i("ALL ::::", mCursorAdapter.getCursor().getString(mCursorAdapter.getCursor().getColumnIndexOrThrow(MediaStore.Images.Media.DATA)));
//            holder.v1.setImageURI(Uri.parse(mCursorAdapter.getCursor().getString(mCursorAdapter.getCursor().getColumnIndexOrThrow(MediaStore.Images.Media.DATA))));
//            int itemHeight = getRecyclerViewWidth(holder.itemView)/getColumnCount(holder.itemView);
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

//    private int getRecyclerViewWidth(View itemView) {
//        RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) itemView.getLayoutParams();
//        int width = layoutParams.width;
//        if (width <= 0) {
//            // If width is not specified, fallback to using the measured width of the itemView
//            width = itemView.getWidth();
//        }
//        if (width <= 0 && itemView.getParent() instanceof View) {
//            // If width is still not available, fallback to using the measured width of the parent view
//            width = ((View) itemView.getParent()).getWidth();
//        }
//        return width;
//    }
//
//    private int getColumnCount(View itemView) {
//        RecyclerView.LayoutManager layoutManager = getRecyclerViewLayoutManager(itemView);
//        if (layoutManager instanceof GridLayoutManager) {
//            GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
//            return gridLayoutManager.getSpanCount();
//        }
//        return 1; // Default value if not using a GridLayoutManager
//    }

    private int getScreenWidth(Context context) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager != null) {
            windowManager.getDefaultDisplay().getMetrics(displayMetrics);
            return displayMetrics.widthPixels;
        }
        return 0;
    }
}