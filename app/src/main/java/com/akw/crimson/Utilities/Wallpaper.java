package com.akw.crimson.Utilities;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.Nullable;

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
    User user;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode==RESULT_OK){

            if(requestCode==Constants.Intent.KEY_INTENT_REQUEST_CODE_MEDIA){
                Intent intent = new Intent(this,AdjustWallpaper.class);
                String path= String.valueOf(data.getData());
                intent.putExtra(Constants.Intent.KEY_INTENT_FILE_PATH,path);
                intent.putExtra(Constants.Intent.KEY_INTENT_USERNAME,user.getDisplayName());
                intent.putExtra(Constants.Intent.KEY_INTENT_USERID,user.getUser_id());
                startActivity(intent);

            }

        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        String id = getIntent().getStringExtra(Constants.Intent.KEY_INTENT_USERID);

        user=Communicator.localDB.getUser(id);

        setViews();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        layoutBinding = ActivityWallpaperBinding.inflate(getLayoutInflater());
        setContentView(layoutBinding.getRoot());

    }

    private void setViews() {
        baseActionBar.setTitle("Custom Wallpaper");

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
            intent.putExtra( Constants.Intent.KEY_INTENT_FILE_PATH,wallpaperFile.getAbsolutePath());
            intent.putExtra(Constants.Intent.KEY_INTENT_USERNAME, user.getDisplayName());
            startActivityForResult(intent,709);

        });

    }
}