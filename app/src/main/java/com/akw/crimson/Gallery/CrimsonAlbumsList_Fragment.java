package com.akw.crimson.Gallery;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.akw.crimson.Gallery.GalleryAdapters.AlbumsRecyclerViewAdapter;
import com.akw.crimson.R;

public class CrimsonAlbumsList_Fragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v= inflater.inflate(R.layout.crimson_albums_list_fragment, container, false);

        RecyclerView rv_albums= v.findViewById(R.id.crimsonAlbum_recyclerView);
        rv_albums.setLayoutManager(new GridLayoutManager(getContext(),2));
        AlbumsRecyclerViewAdapter adapter = new AlbumsRecyclerViewAdapter(getContext(), MainGalleryActivity.crimsonFolders);

        rv_albums.setAdapter(adapter);
        return v;
    }



}