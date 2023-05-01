package com.akw.crimson.Gallery.GalleryAdapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.akw.crimson.Backend.AppObjects.FolderFacer;
import com.akw.crimson.Gallery.AlbumView;
import com.akw.crimson.R;

import java.util.ArrayList;

public class AlbumsRecyclerViewAdapter extends RecyclerView.Adapter<AlbumsRecyclerViewAdapter.ViewHolder> implements View.OnClickListener {

    private ArrayList<FolderFacer> mData;
    private LayoutInflater mInflater;
    private Context context;


    // data is passed into the constructor
    public AlbumsRecyclerViewAdapter(Context context, ArrayList<FolderFacer> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.context = context;
//        Log.i("ALBUMS:::", "CONSTRUCTOR_" + data.size());
    }

    // inflates the cell layout from xml when needed
    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.albums_list_items, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }

    // binds the data to the TextView in each cell
    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
//        Log.i("ICON LIST::::", mData.get(position).getIcon() + "");
        holder.folderName.setText(mData.get(position).getFolderName());
        Bitmap thumbnail = null;
        if (mData.get(position).getIconType() == MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE) {
            thumbnail = MediaStore.Images.Thumbnails.getThumbnail(context.getContentResolver(), mData.get(position).getIcon(), MediaStore.Images.Thumbnails.MINI_KIND, null);
        } else {
            thumbnail = MediaStore.Video.Thumbnails.getThumbnail(
                    context.getContentResolver(),
                    mData.get(position).getIcon(),
                    MediaStore.Video.Thumbnails.MINI_KIND,
                    null
            );
        }

        holder.folderIcon.setImageBitmap(thumbnail);
        holder.folderSize.setText(mData.get(position).getSizeValue());
        holder.folderCount.setText(mData.get(position).getCount());
    }

    // total number of cells
    @Override
    public int getItemCount() {
        return mData.size();
    }

    @Override
    public void onClick(View view) {

    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView folderName, folderCount, folderSize;
        ImageView folderIcon;

        ViewHolder(View itemView) {
            super(itemView);
            folderIcon = itemView.findViewById(R.id.AlbumListItem_ImageView);
            folderName = itemView.findViewById(R.id.AlbumListItem_TextView);
            folderCount = itemView.findViewById(R.id.AlbumListItem_TextView_count);
            folderSize = itemView.findViewById(R.id.AlbumListItem_TextView_size);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            FolderFacer f = getItem(getAbsoluteAdapterPosition());
            Intent intent = new Intent(context, AlbumView.class);
            intent.putExtra("FOLDERPATH", f.getPath());
            context.startActivity(intent);
        }


    }

    // convenience method for getting data at click position
    FolderFacer getItem(int id) {
        return mData.get(id);
    }


    private Bitmap loadThumbnail(String imagePath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, options);

        int sampleSize = calculateSampleSize(options);

        options.inJustDecodeBounds = false;
        options.inSampleSize = sampleSize;

        return BitmapFactory.decodeFile(imagePath, options);
    }

    private int calculateSampleSize(BitmapFactory.Options options) {
        int maxSize = 1100; // the desired maximum size of the bitmap

        int width = options.outWidth;
        int height = options.outHeight;

        int sampleSize = 1;
        while (width / sampleSize > maxSize || height / sampleSize > maxSize) {
            sampleSize *= 4;
        }

        return sampleSize;
    }

}