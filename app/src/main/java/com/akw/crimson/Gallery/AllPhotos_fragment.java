package com.akw.crimson.Gallery;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.akw.crimson.Gallery.GalleryAdapters.MediaCursorRecyclerViewAdapter;
import com.akw.crimson.Gallery.GalleryAdapters.MediaListRecyclerViewAdapter;
import com.akw.crimson.R;

public class AllPhotos_fragment extends Fragment implements MediaListRecyclerViewAdapter.OnImageClickListener {
    RecyclerView rv_allMedia;
    Cursor mediaCursor;
    MediaListRecyclerViewAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_all_photos_fragment, container, false);

        Uri allImagesuri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Images.ImageColumns._ID, MediaStore.Images.ImageColumns.DATA, MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.Images.Media.BUCKET_ID};
        mediaCursor = getContext().getContentResolver().query(allImagesuri, projection, null, null, MediaStore.Images.Media.DATE_ADDED + " DESC");

        rv_allMedia = v.findViewById(R.id.AllPhotosFragment_RecyclerView);
        rv_allMedia.setHasFixedSize(true);
        rv_allMedia.setItemViewCacheSize(20);
        rv_allMedia.setDrawingCacheEnabled(true);
        rv_allMedia.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        rv_allMedia.setLayoutManager(new GridLayoutManager(getContext(), 4));
        adapter = new MediaListRecyclerViewAdapter(getContext(), mediaCursor, this,4);
        rv_allMedia.setAdapter(adapter);

        generateList(mediaCursor);
        return v;
    }

    private void generateList(Cursor mediaCursor) {
        MainGalleryActivity.mediaURI.clear();
        while (mediaCursor.moveToNext()) {
            MainGalleryActivity.mediaURI.add(mediaCursor.getString(mediaCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)));
        }
        MediaCursorRecyclerViewAdapter cursorAdapter = new MediaCursorRecyclerViewAdapter(MainGalleryActivity.mediaURI, new MediaCursorRecyclerViewAdapter.OnImageClickListener() {
            @Override
            public void onImageClick(int position) {
                Intent intent = new Intent(getContext(), MediaView.class);
                intent.putExtra("POSITION", String.valueOf(position));
//        intent.putStringArrayListExtra("LIST", mediaUriList);
                Log.i("IMAGE VIEW:::", "STARTED");
                startActivity(intent);
            }
        }, getContext());
        Log.i("IMG:::::", "DONE");

    }

    @Override
    public void onImageClick(int position) {
        Intent intent = new Intent(getContext(), MediaView.class);
        intent.putExtra("POSITION", String.valueOf(position));
//        intent.putStringArrayListExtra("LIST", mediaUriList);
        Log.i("IMAGE VIEW:::", "STARTED");
        startActivity(intent);
    }
}