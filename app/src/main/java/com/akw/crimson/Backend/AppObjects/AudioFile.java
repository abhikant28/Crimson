package com.akw.crimson.Backend.AppObjects;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import java.util.concurrent.TimeUnit;

public class AudioFile {
    private final String name;
    private String path;
    private final long size;
    private int id;
    private final int length;
    private final Bitmap image;


    public AudioFile(String name, String path, long size, int id, int length, Bitmap image) {
        this.name = name;
        this.path = path;
        this.size = size;
        this.id = id;
        this.length = length;
        this.image = image;
    }

    public String getSizeValue() {
        if (size >= 1073741824) {
            return String.format("%.2f GB", size / 1073741824.0);
        } else if (size >= 1048576) {
            return String.format("%.2f MB", size / 1048576.0);
        } else if (size >= 1024) {
            return String.format("%.2f KB", size / 1024.0);
        } else {
            return size + " B";
        }
    }

    public String getLengthMmSs() {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(length);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(length)
                - TimeUnit.MINUTES.toSeconds(minutes);
        return String.format("%02d:%02d", minutes, seconds);
    }

    @Override
    public boolean equals(@NonNull Object obj) {
        return getId() == ((AudioFile) obj).getId();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setURI(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public long getSize() {
        return size;
    }

    public int getLength() {
        return length;
    }

    public Bitmap getImage() {
        return image;
    }
}