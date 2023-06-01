package com.akw.crimson.Gallery;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.akw.crimson.Gallery.GalleryAdapters.MediaListRecyclerViewAdapter;
import com.akw.crimson.R;

import java.util.ArrayList;

public class AlbumView extends AppCompatActivity implements MediaListRecyclerViewAdapter.OnImageClickListener {

    RecyclerView rv_media;
    Cursor mediaCursor;
    ArrayList<String> mediaUriList= new ArrayList<>();
    private MediaListRecyclerViewAdapter.OnImageClickListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_view);
        String[] projection = { MediaStore.Images.ImageColumns._ID,MediaStore.Images.ImageColumns.DATA ,MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME,MediaStore.Images.Media.BUCKET_ID};

        if(getIntent()!=null) {
            mediaCursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, MediaStore.Images.Media.DATA + " like ? ", new String[]{getIntent().getStringExtra("FOLDERPATH")+"%"}, MediaStore.Images.Media.DATE_ADDED + " DESC");
        }

        rv_media=findViewById(R.id.AlbumMedia_RecyclerView);
        LinearLayoutManager layoutManager = new GridLayoutManager(this,3);
        rv_media.setLayoutManager(layoutManager);
        layoutManager.setAutoMeasureEnabled(true);
        MediaListRecyclerViewAdapter adapter = new MediaListRecyclerViewAdapter(getApplicationContext(), mediaCursor, this,3);
        rv_media.setAdapter(adapter);
        generateList(mediaCursor);
    }

    private void generateList(Cursor mediaCursor) {
        try {
            MainGalleryActivity.mediaURI.clear();
            while (mediaCursor.moveToNext()) {
                //Log.i("ALBUM MEDIA URI:::", mediaCursor.getString(mediaCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)));
                MainGalleryActivity.mediaURI.add(mediaCursor.getString(mediaCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)));
            }
        } finally {
            return;
        }
    }

    @Override
    public void onImageClick(int position) {
        Intent intent= new Intent(getApplicationContext(), MediaView.class);
        intent.putExtra("POSITION", String.valueOf(position));
        intent.putStringArrayListExtra("LIST", mediaUriList);
        //Log.i("IMAGE VIEW:::", "STARTED");
        startActivity(intent);
    }
}