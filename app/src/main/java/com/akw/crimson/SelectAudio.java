package com.akw.crimson;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.akw.crimson.Backend.Adapters.AudioList_RecyclerAdapter;
import com.akw.crimson.Backend.AppObjects.AudioFile;
import com.akw.crimson.Backend.Constants;

import java.util.ArrayList;

public class SelectAudio extends AppCompatActivity {

    private AudioFile currAudio;
    private MediaPlayer mediaPlayer;
    private ImageButton playStopButton;
    private ProgressBar progressBar;
    private Handler handler;
    private RecyclerView rv_audioList;
    private AudioList_RecyclerAdapter audioList_recyclerAdapter;

    private boolean isPlaying = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_audio);

        // Initialize views
        initialize();

        // Initialize MediaPlayer

//        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//            @Override
//            public void onCompletion(MediaPlayer mediaPlayer) {
//                stopMediaPlayer();
//            }
//        });
//        mediaPlayer = new MediaPlayer();

        // Initialize handler
    }


//    private void startMediaPlayer(String filePath) {
//        try {
//            mediaPlayer.setDataSource(filePath);
//            mediaPlayer.prepare();
//        } catch (IOException e) {
//            Toast.makeText(this, "Audio Not Found", Toast.LENGTH_SHORT).show();
//            e.printStackTrace();
//            return;
//        }
//        mediaPlayer.start();
//        isPlaying = true;
//        playStopButton.setImageResource(R.drawable.ic_baseline_stop_24);
//
//        // Update progress bar
//        updateProgressBar();
//    }

//    private void stopMediaPlayer() {
//        mediaPlayer.pause();
////        mediaPlayer.seekTo(0);
//        isPlaying = false;
//        playStopButton.setImageResource(R.drawable.ic_baseline_play_arrow_24);
////        progressBar.setProgress(0);
//    }

//    private void updateProgressBar() {
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                if (mediaPlayer != null && isPlaying) {
//                    int progress = mediaPlayer.getCurrentPosition();
//                    progressBar.setProgress(progress);
//                    handler.postDelayed(this, 100);
//                }
//            }
//        }, 100);
//    }

//    public void onPlayButtonClick(AudioFile audioFile) {
//        if (isPlaying) {
//            stopMediaPlayer();
//        } else {
//            startMediaPlayer(audioFile.getName());
//        }
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
//        handler.removeCallbacksAndMessages(null);
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
        playStopButton = findViewById(R.id.listItem_audioSelect_ib_playStop);
        progressBar = findViewById(R.id.listItem_audioSelect_pb_playProgress);
        rv_audioList= findViewById(R.id.AudioSelect_rv_audioList);

        rv_audioList.setLayoutManager(new LinearLayoutManager(this));

        audioList_recyclerAdapter = new AudioList_RecyclerAdapter(getAudioFiles(), new AudioList_RecyclerAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(AudioFile audio) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SelectAudio.this);
                builder.setTitle("Confirmation")
                        .setMessage("Are you sure you want to select this item?")
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
        setResult(Activity.RESULT_OK, intent);
        finish();
    }
}

