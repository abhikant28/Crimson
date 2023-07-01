package com.akw.crimson.Utilities;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.akw.crimson.AdjustWallpaper;
import com.akw.crimson.Backend.AppObjects.User;
import com.akw.crimson.Backend.Communications.Communicator;
import com.akw.crimson.Backend.Constants;
import com.akw.crimson.Backend.UsefulFunctions;
import com.akw.crimson.BaseActivity;
import com.akw.crimson.databinding.ActivityWallpaperBinding;

import java.io.File;

public class Wallpaper extends BaseActivity {


    ActivityWallpaperBinding layoutBinding;
    private File wallpaperFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        layoutBinding = ActivityWallpaperBinding.inflate(getLayoutInflater());
        setContentView(layoutBinding.getRoot());


        baseActionBar.setTitle("Custom Wallpaper");

        String id = getIntent().getStringExtra(Constants.Intent.KEY_INTENT_USERID);

        User user = Communicator.localDB.getUser(id);

        if (user.getWallpaper() != null) {
            wallpaperFile = UsefulFunctions.FileUtil.getFile(this, user.getWallpaper(), Constants.Media.KEY_MESSAGE_MEDIA_TYPE_WALLPAPER);
            if (wallpaperFile.exists()) {
                Drawable drawable = Drawable.createFromPath(wallpaperFile.getAbsolutePath());
                layoutBinding.wallpaperLlChatLayoutBg.setBackground(drawable);
            }
        }

        layoutBinding.wallpaperBtnChange.setOnClickListener(view->{

            Intent intentOther = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intentOther.addCategory(Intent.CATEGORY_OPENABLE);
            intentOther.setType("*/*");
            String[] mimeTypes = {"image/*", "video/*"};
            intentOther.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);

            startActivityForResult(intentOther, Constants.Intent.KEY_INTENT_REQUEST_CODE_MEDIA);

        });

        layoutBinding.wallpaperLlChatLayoutBg.setOnClickListener(view -> {
            Intent intent = new Intent(this, AdjustWallpaper.class);
//            intent.putExtra( Constants.Intent.KEY_INTENT_FILE_PATH,wallpaperFile.getAbsolutePath());
            intent.putExtra(Constants.Intent.KEY_INTENT_USERNAME, user.getDisplayName());
            startActivityForResult(intent,709);

        });

    }
}