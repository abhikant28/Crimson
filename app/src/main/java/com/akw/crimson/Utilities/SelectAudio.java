package com.akw.crimson.Utilities;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.akw.crimson.Backend.Adapters.AudioList_RecyclerAdapter;
import com.akw.crimson.Backend.AppObjects.AudioFile;
import com.akw.crimson.Backend.Constants;
import com.akw.crimson.R;

import java.util.ArrayList;

public class SelectAudio extends AppCompatActivity {

    private MediaPlayer mediaPlayer;
    private RecyclerView rv_audioList;
    private AudioList_RecyclerAdapter audioList_recyclerAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_audio);

        // Initialize views
        initialize();

    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }

    }

    private ArrayList<AudioFile> getAudioFiles() {
        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DATA
        };

        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";

        Cursor cursor = getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                null
        );

        ArrayList<AudioFile> audioFiles = new ArrayList<>();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int a = cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME), b = cursor.getColumnIndex(MediaStore.Audio.Media.SIZE), c = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION), d = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID), e = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
                String name = cursor.getString(a);
                long size = cursor.getLong(b);
                int length = cursor.getInt(c);
                int albumId = cursor.getInt(d);
                String filePath = cursor.getString(e);

                // Get the album art bitmap for the current audio file
                Bitmap image = getAlbumArtBitmap(albumId);

                AudioFile audioFile = new AudioFile(name, filePath, size, albumId, length, image);
                audioFiles.add(audioFile);
            } while (cursor.moveToNext());
        }

        if (cursor != null) {
            cursor.close();
        }

        return audioFiles;
    }

    private Bitmap getAlbumArtBitmap(int albumId) {
        Bitmap bitmap = null;

        String[] projection = {
                MediaStore.Images.Media.DATA
        };

        String selection = MediaStore.Images.Media._ID + "=?";

        String[] selectionArgs = new String[]{
                String.valueOf(albumId)
        };

        Cursor cursor = getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            int a = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
            String filePath = cursor.getString(a);
            bitmap = BitmapFactory.decodeFile(filePath);
        }

        if (cursor != null) {
            cursor.close();
        }

        return bitmap;
    }

    private void initialize() {
        rv_audioList= findViewById(R.id.AudioSelect_rv_audioList);

        rv_audioList.setLayoutManager(new LinearLayoutManager(this));

        audioList_recyclerAdapter = new AudioList_RecyclerAdapter(getAudioFiles(), new AudioList_RecyclerAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(AudioFile audio) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SelectAudio.this);
                builder.setTitle("Confirmation")
                        .setMessage("Are you sure you want to share "+audio.getName()+" ?")
                        .setPositiveButton("Yes", (dialog, id) -> {
                            // User confirmed selection
                            sendResultAndFinish(audio);

                        })
                        .setNegativeButton("Cancel", (dialog, id) -> {
                            // User cancelled selection
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
        rv_audioList.setAdapter(audioList_recyclerAdapter);
    }


    private void sendResultAndFinish(AudioFile audioFile) {
        Intent intent = new Intent();
        intent.putExtra(Constants.KEY_INTENT_RESULT_AUDIO_PATH, audioFile.getPath());
        Log.i("RESULT SENT::::::", audioFile.getPath());
        setResult(Activity.RESULT_OK, intent);
        finish();
    }
}

