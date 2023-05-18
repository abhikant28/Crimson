package com.akw.crimson.Backend.Adapters;


import android.media.MediaPlayer;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.akw.crimson.Backend.AppObjects.AudioFile;
import com.akw.crimson.R;
import com.akw.crimson.databinding.ListItemSelectAudioBinding;

import java.io.IOException;
import java.util.ArrayList;


public class AudioList_RecyclerAdapter extends RecyclerView.Adapter {

    MediaPlayer mediaPlayer;
    private ProgressBar currPb;
    private ImageButton currButton;
    private AudioFile audio;
    private boolean isPlaying;
    private final Handler handler;

    private final OnItemClickListener mListener; // Reference to the callback interface
    private final ArrayList<AudioFile> audioList;

    private void startMediaPlayer(String filePath) {
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.prepare();
        } catch (IOException e) {
//            Toast.makeText(, "Audio Not Found", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            return;
        }
        currPb.setVisibility(View.VISIBLE);
        mediaPlayer.start();
        isPlaying = true;
        currButton.setImageResource(R.drawable.ic_baseline_stop_24);

        // Update progress bar
        updateProgressBar();
    }

    private void stopMediaPlayer() {
        mediaPlayer.pause();
        mediaPlayer.seekTo(0);
        isPlaying = false;
        currButton.setImageResource(R.drawable.ic_baseline_audio_play_24);
        currPb.setProgress(0);
        currPb.setVisibility(View.GONE);
        mediaPlayer.release();
    }

    private void updateProgressBar() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && isPlaying) {
                    int progress = mediaPlayer.getCurrentPosition();
                    currPb.setProgress(progress);
                    handler.postDelayed(this, 100);
                }
            }
        }, 100);
    }

    public AudioList_RecyclerAdapter(ArrayList<AudioFile> audioList, OnItemClickListener listener) {
        mListener = listener;
        handler = new Handler();
        this.audioList = audioList;
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(mediaPlayer -> stopMediaPlayer());
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ListItemSelectAudioBinding sentBinding = ListItemSelectAudioBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ListItemView(sentBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ListItemView binding = (ListItemView) holder;

        int p = position;
        binding.binding.listItemAudioSelectLl.setOnClickListener(view -> {
            mListener.OnItemClick(audioList.get(p)); // Call the callback method
        });

        binding.binding.listItemAudioSelectTvAudioName.setText(audioList.get(position).getName());
        binding.binding.listItemAudioSelectTvAudioSize.setText(audioList.get(position).getSizeValue());
        binding.binding.listItemAudioSelectTvAudioLength.setText(audioList.get(position).getLengthMmSs());
        if (audioList.get(position).getImage() != null)
            binding.binding.listItemAudioSelectIvAudioImage.setImageBitmap(audioList.get(position).getImage());
        binding.binding.listItemAudioSelectIbPlayStop.setOnClickListener(view -> {
            if (audio != null && audio.equals(audioList.get(position))) {
                stopMediaPlayer();
            } else {
                mediaPlayer.release();
                audio = audioList.get(p);
                if (currPb != null)
                    currButton.setImageResource(R.drawable.ic_baseline_audio_play_24);
                if (currPb != null)
                    currPb.setVisibility(View.GONE);
                currButton = binding.binding.listItemAudioSelectIbPlayStop;
                currPb = binding.binding.listItemAudioSelectPbPlayProgress;
                startMediaPlayer(audio.getPath());
            }
        });
    }

    @Override
    public int getItemCount() {
        return audioList.size();
    }


    public interface OnItemClickListener {
        void OnItemClick(AudioFile audio);
    }

    class ListItemView extends RecyclerView.ViewHolder {
        ListItemSelectAudioBinding binding;
        boolean isPlaying;

        public ListItemView(@NonNull ListItemSelectAudioBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            isPlaying = false;
        }
    }
}