package com.akw.crimson.Gallery.GalleryAdapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.akw.crimson.Backend.AppObjects.FolderFacer;
import com.akw.crimson.Gallery.AlbumView;
import com.akw.crimson.R;

import java.util.ArrayList;

public class AlbumsRecyclerViewAdapter extends RecyclerView.Adapter<AlbumsRecyclerViewAdapter.ViewHolder> implements View.OnClickListener{

        private ArrayList<FolderFacer> mData;
        private LayoutInflater mInflater;
        private Context context;


        // data is passed into the constructor
        public AlbumsRecyclerViewAdapter(Context context, ArrayList<FolderFacer> data) {
            this.mInflater = LayoutInflater.from(context);
            this.mData = data;
            this.context=context;
            Log.i("ALBUMS:::", "CONSTRUCTOR");
        }

        // inflates the cell layout from xml when needed
        @Override
        @NonNull
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = mInflater.inflate(R.layout.albums_list_items, parent, false);
            ViewHolder viewHolder=new ViewHolder(view);

            return viewHolder ;
        }

        // binds the data to the TextView in each cell
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Log.i("LIST:::", mData.get(position).getFolderName());
            holder.folderName.setText(mData.get(position).getFolderName());
            holder.folderIcon.setImageURI(Uri.parse(mData.get(position).getIcon()));
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
            TextView folderName;
            ImageView folderIcon;

            ViewHolder(View itemView) {
                super(itemView);
                folderIcon=itemView.findViewById(R.id.AlbumListItem_ImageView);
                folderName = itemView.findViewById(R.id.AlbumListItem_TextView);
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
                FolderFacer f=getItem(this.getAdapterPosition());
                Intent intent= new Intent(context, AlbumView.class);
                intent.putExtra("FOLDERPATH", f.getPath());
                context.startActivity(intent);
            }


        }

        // convenience method for getting data at click position
        FolderFacer getItem(int id) {
            return mData.get(id);
        }

    }