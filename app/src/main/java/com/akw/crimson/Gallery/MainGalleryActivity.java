package com.akw.crimson.Gallery;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.akw.crimson.Backend.AppObjects.FolderFacer;
import com.akw.crimson.Backend.UsefulFunctions;
import com.akw.crimson.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;

public class MainGalleryActivity extends AppCompatActivity {
    public static Cursor allImageCursor;
    static ArrayList<FolderFacer> imageFolders = new ArrayList<>();
    static ArrayList<String> mediaURI = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gallery_main_activity);


        if (ContextCompat.checkSelfPermission(
                getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED) {
            // You can use the API that requires the permission.
            getImagePaths();
            getVideoPaths();
        }
//        else if (shouldShowRequestPermissionRationale()) {
//            // In an educational UI, explain to the user why your app requires this
//            // permission for a specific feature to behave as expected. In this UI,
//            // include a "cancel" or "no thanks" button that allows the user to
//            // continue using your app without granting the permission.
//            showInContextUI(...);
//        }
        else {
            // You can directly ask for the permission.
            // The registered ActivityResultCallback gets the result of this request.
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
        }

        BottomNavigationView bottomNav = findViewById(R.id.galleryMainActivity_BottomNavigationBar);
        bottomNav.setOnItemSelectedListener(navListener);

        //I added this if statement to keep the selected fragment when rotating the device
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.galleryMainActivity_FrameLayout,
                    new AlbumsList_Fragment()).commit();
        }
        Log.i("IMAGE FUNCTIONS::::::", "");

        imageFolders = UsefulFunctions.getImageFolders(this);

        allImageCursor = UsefulFunctions.getImagePaths(this);

    }

    private NavigationBarView.OnItemSelectedListener navListener =
            item -> {
                Fragment selectedFragment = null;

                switch (item.getItemId()) {
                    case R.id.nav_albums:
                        selectedFragment = new AlbumsList_Fragment();
                        break;
                    case R.id.nav_photos:
                        selectedFragment = new AllPhotos_fragment();
                        break;
                }

                getSupportFragmentManager().beginTransaction().replace(R.id.galleryMainActivity_FrameLayout,
                        selectedFragment).commit();

                return true;
            };

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    getImagePaths();
                    getVideoPaths();
                } else {
                    // Explain to the user that the feature is unavailable because the
                    // features requires a permission that the user has denied. At the
                    // same time, respect the user's decision. Don't link to system
                    // settings in an effort to convince the user to change their
                    // decision.
                }
            });


    public ArrayList<FolderFacer> getImagePaths() {
        ArrayList<FolderFacer> imageFolders = new ArrayList<>();
        ArrayList<String> imagePaths = new ArrayList<>();
        Uri allImagesuri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Images.ImageColumns.DATA, MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.Images.Media.BUCKET_ID};
        Cursor cursor = getContentResolver().query(allImagesuri, projection, null, null, null);

        try {
            cursor.moveToFirst();
            do {

                FolderFacer folds = new FolderFacer();

                String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME));
                String folder = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                String datapath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));

                String folderpaths = datapath.replace(name, "");
                if (!imagePaths.contains(folderpaths)) {
                    imagePaths.add(folderpaths);

                    folds.setPath(folderpaths);
                    folds.setFolderName(folder);
                    imageFolders.add(folds);
                }

            } while (cursor.moveToNext());

            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

//        for(int i = 0;i < imageFolders.size();i++){
//
//            Log.d("Image folders",imageFolders.get(i).getFolderName()+" and path = "+imageFolders.get(i).getPath());
//
//        }

        return imageFolders;
    }

    private ArrayList<FolderFacer> getVideoPaths() {
        ArrayList<FolderFacer> videoFolders = new ArrayList<>();
        ArrayList<String> videoPaths = new ArrayList<>();
        Uri allVideosuri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Video.VideoColumns.DATA, MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.BUCKET_DISPLAY_NAME, MediaStore.Video.Media.BUCKET_ID};
        Cursor cursor = getContentResolver().query(allVideosuri, projection, null, null, null);

        try {
            cursor.moveToFirst();
            do {

                FolderFacer folds = new FolderFacer();

                String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME));
                String folder = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME));
                String datapath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));

                String folderpaths = datapath.replace(name, "");
                if (!videoPaths.contains(folderpaths)) {
                    videoPaths.add(folderpaths);

                    folds.setPath(folderpaths);
                    folds.setFolderName(folder);
                    videoFolders.add(folds);
                }

            } while (cursor.moveToNext());

            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

//        for(int i = 0;i < videoFolders.size();i++){
//
//            Log.d("video folders",videoFolders.get(i).getFolderName()+" and path = "+videoFolders.get(i).getPath());
//
//        }

        return videoFolders;
    }

}