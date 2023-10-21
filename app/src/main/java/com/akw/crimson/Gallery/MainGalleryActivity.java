package com.akw.crimson.Gallery;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.akw.crimson.Backend.AppObjects.FolderFacer;
import com.akw.crimson.Backend.AppObjects.User;
import com.akw.crimson.Backend.Communications.Communicator;
import com.akw.crimson.Backend.Constants;
import com.akw.crimson.Backend.Database.TheViewModel;
import com.akw.crimson.Backend.UsefulFunctions;
import com.akw.crimson.R;
import com.google.android.gms.common.util.ArrayUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainGalleryActivity extends AppCompatActivity {

    public static Cursor allImageCursor;
    static ArrayList<FolderFacer> imageFolders = new ArrayList<>();
    static ArrayList<FolderFacer> crimsonFolders = new ArrayList<>();
    static ArrayList<String> mediaURI = new ArrayList<>();

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gallery_main_activity);


        if (ContextCompat.checkSelfPermission(
                getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED) {
            // You can use the API that requires the permission.
            imageFolders = getAllFoldersWithImagesAndVideos();
            Collections.sort(imageFolders, new Comparator<FolderFacer>() {
                @Override
                public int compare(FolderFacer lhs, FolderFacer rhs) {
                    return lhs.getFolderName().compareTo(rhs.getFolderName());
                }
            });
            crimsonFolders=getCrimsonFolders();

            getVideoPaths();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
        }

        BottomNavigationView bottomNav = findViewById(R.id.galleryMainActivity_BottomNavigationBar);
        bottomNav.setOnItemSelectedListener(navListener);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.galleryMainActivity_FrameLayout,
                    new AlbumsList_Fragment()).commit();
        }

        allImageCursor = UsefulFunctions.getImagePaths(this);

        setTitle("Gallery");
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
                    case R.id.nav_crimson:
                        selectedFragment = new CrimsonAlbumsList_Fragment();
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
        int i = -1;
        Uri allImagesuri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Images.ImageColumns.DATA, MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.Images.Media.BUCKET_ID, MediaStore.Images.Media._ID, MediaStore.Images.Media.SIZE};
        Cursor cursor = getContentResolver().query(allImagesuri, projection, null, null, MediaStore.Images.Media.DATE_ADDED + " DESC");

        try {
            cursor.moveToFirst();
            do {


                String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME));
                String folder = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                String datapath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                long size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE));

                String folderpaths = datapath.replace(name, "");
                if (!imagePaths.contains(folderpaths)) {
                    FolderFacer folds = new FolderFacer();
                    imagePaths.add(folderpaths);
                    int id = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);

                    folds.setPath(folderpaths);
                    folds.setFolderName(folder);
                    folds.setSize(size);

                    int imageId = cursor.getInt(id);
                    // folds.setIcon(imageId);
                    imageFolders.add(folds);
                } else {
                    FolderFacer f = imageFolders.get(i);
                    f.incSize(size);
                    imageFolders.remove(i);
                    imageFolders.add(f);
                }

            } while (cursor.moveToNext());

            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

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

        return videoFolders;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public ArrayList<FolderFacer> getAllFoldersWithImagesAndVideos() {
        ArrayList<FolderFacer> folders = new ArrayList<>();

        // Define the columns to query
        String[] projection = new String[]{
                MediaStore.Files.FileColumns.BUCKET_ID,
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME,
                MediaStore.Files.FileColumns.MEDIA_TYPE,
                MediaStore.Files.FileColumns.PARENT,
                MediaStore.Files.FileColumns.SIZE,
                MediaStore.Files.FileColumns.DATE_ADDED,
                MediaStore.Files.FileColumns.DISPLAY_NAME
        };

        // Define the selection criteria for images and videos
        String selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                + " OR "
                + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

        // Define the sorting order
        String sortOrder = MediaStore.Files.FileColumns.DATE_ADDED + " DESC";

        // Query the MediaStore
        Cursor cursor = getContentResolver().query(MediaStore.Files.getContentUri("external"), projection, selection, null, sortOrder);

        if (cursor != null) {
            // Create a map to store folder information
            Map<Integer, FolderFacer> folderMap = new HashMap<>();

            while (cursor.moveToNext()) {
                // Get the folder ID
                int cId = cursor.getColumnIndex(MediaStore.Files.FileColumns.PARENT);
                int folderId = cursor.getInt(cId);

                FolderFacer folder;
                if (folderMap.containsKey(folderId)) {
                    // If we've already seen this folder, get it from the map
                    folder = folderMap.get(folderId);
                } else {
                    // If this is a new folder, create a new Folder object
                    folder = new FolderFacer();
                    folderMap.put(folderId, folder);
                    folders.add(folder);
                    cId = cursor.getColumnIndex(MediaStore.Files.FileColumns._ID);
                    folder.setID(cursor.getString(cId));
                    cId = cursor.getColumnIndex(MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME);
                    String name = cursor.getString(cId);
                    folder.setFolderName(name);
                    String datapath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA));
                    File file = new File(datapath);
                    folder.setPath(file.getParent());
                    long mediaId = 0;
                    cId = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE);
                    if (cursor.getInt(cId) == MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE) {

                        String[] mediaProjection = {
                                MediaStore.Images.Media._ID, MediaStore.Images.Media.DISPLAY_NAME,
                                MediaStore.Images.Media.DATE_ADDED, MediaStore.Images.Media.MIME_TYPE,
                                MediaStore.Images.Media.SIZE, MediaStore.Images.Media.DATA
                        };

                        String mediaSelection = MediaStore.Images.Media.DATA + " = ?";
                        String[] selectionArgs = {cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA))};
//                        Log.i("DATA URI:::::",cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)));

                        Cursor mediaCursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                mediaProjection, mediaSelection,
                                selectionArgs,
                                null);
                        if (mediaCursor != null && mediaCursor.moveToFirst()) {
                            cId = mediaCursor.getColumnIndex(MediaStore.Images.Media._ID);
                            mediaId = mediaCursor.getLong(cId);
                        }

                    } else {
                        String[] mediaProjection = {
                                MediaStore.Video.Media._ID,
                                MediaStore.Video.Media.DISPLAY_NAME,
                                MediaStore.Video.Media.MIME_TYPE,
                                MediaStore.Video.Media.SIZE
                        };
                        String mediaSelection = MediaStore.Video.Media.DATA + " = ?";
//                        Log.e("URI::::", cursor.getString(cId));
                        cId = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                        String[] selectionArgs = {cursor.getString(cId)};

                        Cursor mediaCursor = getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                                mediaProjection,
                                mediaSelection,
                                selectionArgs,
                                null);
                        if (mediaCursor != null && mediaCursor.moveToFirst()) {
                            cId = mediaCursor.getColumnIndex(MediaStore.Video.Media._ID);
                            mediaId = mediaCursor.getLong(cId);
                        }
                    }
                    folder.setIcon(mediaId);
                    folder.setIconType(cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE)));
                }

                // Update the folder information
                cId = cursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE);
                folder.incSize(cursor.getLong(cId));
                cId = cursor.getColumnIndex(MediaStore.Files.FileColumns.MEDIA_TYPE);
                if (cursor.getInt(cId) == MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE) {
                    folder.incImgCount();
                } else {
//                    Log.i("FOLDER::::::",folder.getFolderName()+"_"+folder.getCount() );
                    folder.incVidCount();
                }
            }

            cursor.close();
        }
        return folders;
    }

    public ArrayList<FolderFacer> getCrimsonFolders() {
        List<User> users = null;
        String[] projection = {MediaStore.Images.ImageColumns._ID, MediaStore.Images.ImageColumns.DATA, MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.Images.Media.BUCKET_ID};
        String path = Environment.getExternalStorageDirectory()
                + "/Android/media" +
                "" +
                "/"
                + this.getApplicationContext().getPackageName()
                + "/Media/Crimson ";
//        Cursor crimsonMediaCursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, MediaStore.Images.Media.DATA + " like ? ", new String[]{path + "Images" + "%", path + "Videos" + "%", path + "Documents" + "%"}, MediaStore.Images.Media.DATE_ADDED + " DESC");
        if (Communicator.localDB == null) {
            TheViewModel db = new TheViewModel(getApplication());
            users = db.getChatListUsers().getValue();
        } else {
            users = Communicator.localDB.getChatListUsers().getValue();
        }
        ArrayList<FolderFacer> userFolders = new ArrayList<>();
        Cursor cursor = null;
        FolderFacer fol = new FolderFacer();
        String[] selectionArgsFolder1 = new String[] {path+ "Images"};
        String[] selectionArgsFolder1s = new String[] {path+ "Images/Sent"};
        String[] selectionArgsFolder2 = new String[] {path+ "Videos/Sent"};
        String[] selectionArgsFolder2s = new String[] {path+ "Videos"};
        String[] selectionArgsFolder3 = new String[] {path+ "Documents/Sent"};
        String[] selectionArgsFolder3s = new String[] {path+ "Documents"};
        String[] selectionArgs = ArrayUtils.concat(selectionArgsFolder1,selectionArgsFolder1s,selectionArgsFolder2s, selectionArgsFolder2, selectionArgsFolder3,selectionArgsFolder3s);

        long sizeSum=0,imgCount=0,vidCount=0;

        if(users!=null){
            for (User user : users) {
                if (user.getMedias().size() > 0) {
                    sizeSum = +user.getMediaSize();
                    imgCount = +user.getImgCount();
                    vidCount = +user.getVidCount();
                    fol = new FolderFacer();
                    fol.setSize(user.getMediaSize());
                    fol.setFolderName(user.getDisplayName());
                    int type = (user.getMedias().get(0).startsWith("IMG_") ? Constants.Media.KEY_MESSAGE_MEDIA_TYPE_IMAGE : Constants.Media.KEY_MESSAGE_MEDIA_TYPE_VIDEO);
                    File file = UsefulFunctions.FileUtil.getFile(this, user.getMedias().get(0), type);
                    cursor = this.getContentResolver().query(Uri.parse(file.getPath()), projection, Arrays.deepToString(selectionArgs), null, null);
                    if (cursor != null && cursor.moveToFirst()) {
                        int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
                        fol.setIcon(cursor.getLong(columnIndex));
                        fol.setIconType(type);
                    }
                    fol.setPath(path + "Images");
                    fol.setVidCount(user.getVidCount());
                    fol.setImgCount(user.getImgCount());

                    userFolders.add(fol);
                }
            }
        }        fol = new FolderFacer();
        fol.setPath(Arrays.deepToString(selectionArgs));
        fol.setIconType(Constants.Media.KEY_MESSAGE_MEDIA_TYPE_IMAGE);
        fol.setFolderName("All Medias");
        fol.setSize(sizeSum);
        fol.setImgCount((int)imgCount);
        fol.setVidCount((int)vidCount);
        userFolders.add(0,fol);
        if (cursor != null) {
            cursor.close();
        }
        return userFolders;
    }


}