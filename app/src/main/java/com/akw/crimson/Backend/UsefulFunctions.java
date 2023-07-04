package com.akw.crimson.Backend;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.akw.crimson.Backend.AppObjects.FolderFacer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class UsefulFunctions {

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String formatDate(String timeStamp) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd, HH:mm:ss");
        LocalDate currentDate = LocalDate.now();
        LocalDate inputDate = LocalDate.parse(timeStamp, formatter);

        if (inputDate.isEqual(currentDate)) {
            return "Today";
        } else if (inputDate.isEqual(currentDate.minusDays(1))) {
            return "Yesterday";
        } else if (inputDate.isAfter(currentDate.minusDays(7))) {
            return inputDate.getDayOfWeek().toString();
        } else if (inputDate.getYear() == currentDate.getYear()) {
            return inputDate.format(DateTimeFormatter.ofPattern("dd/MMMM"));
        } else {
            return inputDate.format(formatter);
        }
    }


    public static String getCurrentMmTimeStamp(){
        return String.valueOf(System.currentTimeMillis());
    }

    public static String getGreeting() {
        LocalTime t = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            t = LocalTime.now();
        }
        int hour = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            hour = t.getHour();
        }

        if (hour >= 6 && hour < 12) {
            return "Good Morning";
        } else if (hour >= 12 && hour < 18) {
            return "Good Afternoon";
        } else {
            return "Good Evening";
        }
    }


    public static String getCurrentTimestamp() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd, HH:mm:ss", Locale.getDefault());
        return dateFormat.format(new Date());
    }

    public static String convertTimestampFormat(String time) {

        SimpleDateFormat inputFormatter = new SimpleDateFormat("dd/MM/yyyy, HH:mm:ss");
        SimpleDateFormat outputFormatter = new SimpleDateFormat("yyyy/MM/dd, HH:mm:ss");

        Date date = null;
        try {
            date = inputFormatter.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String outputDate = outputFormatter.format(date);
        return outputDate;
    }

    public static String getTime(String time) {
        Calendar calendar = Calendar.getInstance();
        try {
        calendar.setTime(new SimpleDateFormat("dd/MM/yyyy, HH:mm:ss").parse(time));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        String formattedTime = String.format("%02d:%02d:%02d", hour, minute, second);

        return formattedTime;
    }

    public static String getTimeHhMm(String time) {
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(new SimpleDateFormat("dd/MM/yyyy, HH:mm:ss").parse(time));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        String formattedTime = String.format("%02d:%02d", hour, minute);

        return formattedTime;
    }

    public static String getDate(String time) {
        return time.substring(0, time.lastIndexOf(","));
    }

    public static String getTime() {
        String time = getCurrentTimestamp();
        return time.substring(time.lastIndexOf(",") + 2);
    }

    public static String getDate() {
        String time = getCurrentTimestamp();
        return time.substring(0, time.lastIndexOf(","));
    }

    public static class FileUtil {

        public static String fileExt(String url) {
            if (url.indexOf("?") > -1) {
                url = url.substring(0, url.indexOf("?"));
            }
            if (url.lastIndexOf(".") == -1) {
                return null;
            } else {
                String ext = url.substring(url.lastIndexOf(".") + 1);
                if (ext.indexOf("%") > -1) {
                    ext = ext.substring(0, ext.indexOf("%"));
                }
                if (ext.indexOf("/") > -1) {
                    ext = ext.substring(0, ext.indexOf("/"));
                }
                return ext.toLowerCase();

            }
        }

        public static File saveImageInInternalStorage(Context cxt, Bitmap bitmap) {
            Log.i("CAMERA.saveImage:::::::", "try");
            FileOutputStream outputStream;
            File imageFile = null;
            try {
                File storageDir = cxt.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                String imageFileName = "IMG_" + timeStamp + ".jpg";
                imageFile = new File(storageDir, imageFileName);

                outputStream = new FileOutputStream(imageFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                outputStream.flush();
                outputStream.close();

                // Image saved successfully
                // You can do further processing with the saved image file here

            } catch (IOException e) {
                e.printStackTrace();
                // Handle image saving error
            }
            return imageFile;
        }

        public static File createInternalFile(Context context, String fileName) {
            File file = new File(context.getFilesDir(), fileName);

            try {
                if (file.createNewFile()) {
                    // File created successfully
                } else {
                    // File already exists or failed to create the file
                }
            } catch (IOException e) {
                e.printStackTrace();
                // Handle the exception
            }

            return file;
        }

        public static File saveFileInternalStorage(Context context, Uri uri, String destinationFileName) {
            InputStream inputStream = null;
            OutputStream outputStream = null;
            File destinationFile = null;
            try {
                destinationFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), destinationFileName);

                inputStream = context.getContentResolver().openInputStream(uri);
                outputStream = new FileOutputStream(destinationFile);

                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                // File copied successfully
            } catch (IOException e) {
                e.printStackTrace();
                // Handle the exception
            } finally {
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    if (outputStream != null) {
                        outputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return destinationFile;
        }


        @RequiresApi(api = Build.VERSION_CODES.N)
        public static Bitmap getImageFromUri(Context context, Uri uri) {
            try {
                InputStream inputStream = context.getContentResolver().openInputStream(uri);
                Bitmap image = BitmapFactory.decodeStream(inputStream);
                ExifInterface exifIn = new ExifInterface(context.getContentResolver().openInputStream(uri));
                int orientation = exifIn.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                int width = image.getWidth();
                int height = image.getHeight();
                int side = Math.min(width, height);
                float ratio = (float) width / height;

                Matrix matrix = new Matrix();
                switch (orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        matrix.setRotate(90);
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        matrix.setRotate(180);
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        matrix.setRotate(270);
                        break;
                    default:
                        break;
                }
                Bitmap resized = image;
                resized = Bitmap.createBitmap(resized, 0, 0, width, height, matrix, true);
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                resized.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                byte[] imageData = outputStream.toByteArray();
                return BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }


        public static String saveImage(Bitmap bitmap, boolean sent, File file) {
            File pictureFile = file;
            if (pictureFile == null) {
                Log.d(TAG + "::::",
                        "Error creating media file, check storage permissions: ");// e.getMessage());
                return null;
            }
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.close();
            } catch (FileNotFoundException e) {
                Log.d("ERROR ::::", "File not found: " + e.getMessage());
                return null;
            } catch (IOException e) {
                Log.d("ERROR ::::", "Error accessing file: " + e.getMessage());
                return null;
            }
            Log.i(TAG + "::::", pictureFile.getAbsolutePath());
            return pictureFile.getName().substring(0, pictureFile.getName().lastIndexOf('.'));
        }

        public static File makeOutputMediaFile(Context context, boolean sent, int type) {
            return makeOutputMediaFile(context, sent, type, null);
        }

        public static File makeOutputMediaFile(Context context, boolean sent, int type, String docName) {
            // To be safe, you should check that the SDCard is mounted
            // using Environment.getExternalStorageState() before doing this.
            String folder = "";
            String init = "";
            String format = "";
            switch (type) {
                case Constants.Media.KEY_MESSAGE_MEDIA_TYPE_IMAGE:
                    folder = "Images";
                    init = "IMG";
                    format = ".jpg";
                    break;
                case Constants.Media.KEY_MESSAGE_MEDIA_TYPE_VIDEO:
                    folder = "Videos";
                    init = "VID";
                    format = ".mp4";
                    break;
                case Constants.Media.KEY_MESSAGE_MEDIA_TYPE_DOCUMENT:
                    folder = "Documents";
                    break;
                case Constants.Media.KEY_MESSAGE_MEDIA_TYPE_AUDIO:
                    folder = "Audios";
                    init = "AUD";
                    format = ".mp3";
                    break;
                case Constants.Media.KEY_MESSAGE_MEDIA_TYPE_WALLPAPER:
                    folder = "Wallpapers";
                    init = "IMG";
                    format = ".jpg";
                    break;
                case Constants.Media.KEY_MESSAGE_MEDIA_TYPE_CAMERA_IMAGE:
                    folder = "Camera";
                    init = "IMG";
                    format = ".jpg";
                    break;
                case Constants.Media.KEY_MESSAGE_MEDIA_TYPE_CAMERA_VIDEO:
                    folder = "Camera";
                    init = "VID";
                    format = ".mp4";
                    break;
                case Constants.Media.KEY_MESSAGE_MEDIA_TYPE_PROFILE:
                    folder = "";
                    init = docName;
                    format = ".jpg";
                    break;
            }
            String fol = sent ? (type == Constants.Media.KEY_MESSAGE_MEDIA_TYPE_CAMERA_IMAGE || type == Constants.Media.KEY_MESSAGE_MEDIA_TYPE_CAMERA_VIDEO
                    || type==Constants.Media.KEY_MESSAGE_MEDIA_TYPE_WALLPAPER ? "" : "/Sent") : "";
            File mediaStorageDir = null;
            if (type != Constants.Media.KEY_MESSAGE_MEDIA_TYPE_PROFILE) {
                mediaStorageDir = new File(Environment.getExternalStorageDirectory()
                        + "/Android/media" +
                        "" +
                        "/"
                        + context.getApplicationContext().getPackageName()
                        + "/Media/Crimson " + folder + fol);
            } else {
                return new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),docName+format);
            }
            // This location works best if you want the created images to be shared
            // between applications and persist after your app has been uninstalled.

            // Create the storage directory if it does not exist
            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    Log.i("mediaStorageDir" + "::::", "NULL");

                    return null;
                }
            }
            // Create a media file name
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmSS").format(new Date());
            File mediaFile;
            //        Log.i("UsefulFunction.makeOutputMediaFile::::::", "docName="+docName);
            String mImageName = docName == null ? init + "_" + timeStamp : docName.substring(0, docName.lastIndexOf('.'));
            format = docName == null ? format : docName.substring(docName.lastIndexOf('.'));
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName + format);
            int i = 1;
            while (mediaFile.exists()) {
                mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName + " (" + (i++) + ")" + format);
            }
            Log.i(TAG, mImageName);
            return mediaFile;
        }

        public static File getFile(Context context, String id, int type) {
            File file = getFile(context, id, type, true);
            if (!file.exists()) {
                return getFile(context, id, type, false);
            }
            return file;
        }

        public static File getFile(Context context, String id, int type, boolean sent) {
            // To be safe, you should check that the SDCard is mounted
            // using Environment.getExternalStorageState() before doing this.

            String folder = "";
            String subFolder = sent ? "/Sent" : "";


            switch (type) {
                case Constants.Media.KEY_MESSAGE_MEDIA_TYPE_IMAGE:
                    folder = "Images";
                    break;
                case Constants.Media.KEY_MESSAGE_MEDIA_TYPE_VIDEO:
                    folder = "Videos";
                    break;
                case Constants.Media.KEY_MESSAGE_MEDIA_TYPE_DOCUMENT:
                    folder = "Documents";
                    break;
                case Constants.Media.KEY_MESSAGE_MEDIA_TYPE_AUDIO:
                    folder = "Audios";
                    break;
                case Constants.Media.KEY_MESSAGE_MEDIA_TYPE_CAMERA_IMAGE:
                case Constants.Media.KEY_MESSAGE_MEDIA_TYPE_CAMERA_VIDEO:
                    folder = "Camera";
                    break;
                case Constants.Media.KEY_MESSAGE_MEDIA_TYPE_PROFILE:

            }
            String mediaStorageDir = Environment.getExternalStorageDirectory()
                    + "/Android/media" +
                    "" +
                    "/"
                    + context.getApplicationContext().getPackageName()
                    + "/Media/Crimson " + folder + subFolder;
            if(type==Constants.Media.KEY_MESSAGE_MEDIA_TYPE_PROFILE){
                mediaStorageDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) + "";
            }


            // This location works best if you want the created images to be shared
            // between applications and persist after your app has been uninstalled.

            Log.i("FILE::::", mediaStorageDir);
            return new File(mediaStorageDir, id);
        }

        public static String saveFile(byte[] bytes, File outFile) throws IOException {
            OutputStream os = new FileOutputStream(outFile);
            os.write(bytes);
            os.close();
            Log.i("UsefulFunctions.saveFile", outFile.getName());
            return outFile.getName();
        }

        public static String saveFile(Context cxt, Uri originalUri, File outputFile) {
            try {
                InputStream inputStream = cxt.getContentResolver().openInputStream(originalUri);
                OutputStream outputStream = new FileOutputStream(outputFile);
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
                inputStream.close();
                outputStream.flush();
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
            Log.i("UsefulFunctions.saveFile", outputFile.getName());
            return outputFile.getName().substring(0, outputFile.getName().lastIndexOf('.'));
        }


        public static String getFileName(Context context, Uri uri, boolean withExtension) {
            String result = null;
            Log.i("TEMP TEST::::::", uri.getPath() + "__" + (uri.getScheme() == null));
            if (uri.getScheme().equals("content")) {
                Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
                int c = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                try {
                    if (cursor.moveToFirst()) {
                        result = cursor.getString(c);
                    }
                } finally {
                    cursor.close();
                }
            }
            if (result == null) {
                result = uri.getPath();
                int cut = result.lastIndexOf('/');
                if (cut != -1) {
                    result = result.substring(cut + 1);
                }
            }
            if (withExtension)
                return result;
            return result.substring(0, result.lastIndexOf('.'));
        }

        public static String getFileNameFromPath(String path) {
            String fileNameWithExtension = new File(path).getName();
            int dotIndex = fileNameWithExtension.lastIndexOf(".");
            if (dotIndex == -1) {
                return fileNameWithExtension;
            } else {
                return fileNameWithExtension.substring(0, dotIndex);
            }
        }
    }

    public static boolean isInternetConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            if (activeNetwork != null && activeNetwork.isConnectedOrConnecting()) {
                return true; // Device is connected to the internet
            }
        }
        return false; // Device is not connected to the internet
    }

    public static String encodeImage(Bitmap bitmap) {
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.WEBP, 100, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    public static Bitmap decodeImage(String input) {
        byte[] decoded = Base64.decode(input, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
    }

    public static Bitmap fileToBitmap(File file) {
        Bitmap bitmap = null;
        try {
            // Decode the file path into a Bitmap
            bitmap = BitmapFactory.decodeFile(file.getPath());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public static String encodeText(String input) {
        return Base64.encodeToString(input.getBytes(), Base64.DEFAULT);
    }

    public static String decodeText(String input) {
        return new String(Base64.decode(input, Base64.DEFAULT));
    }

    public static Bitmap getCircularBitmap(Bitmap bitmap) {
        Bitmap output;

        if (bitmap.getWidth() > bitmap.getHeight()) {
            output = Bitmap.createBitmap(bitmap.getHeight(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        } else {
            output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getWidth(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        float r;

        if (bitmap.getWidth() > bitmap.getHeight()) {
            r = bitmap.getHeight() / 2;
        } else {
            r = bitmap.getWidth() / 2;
        }

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawCircle(r, r, r, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }

    public static String getSizeValue(long size) {
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

    public static String getAudioLength(String path) {
        String sentTime = null;
        try {
            MediaPlayer mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();
            sentTime = getStringMmSsTimeVale(mediaPlayer.getDuration());
            mediaPlayer.release();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return sentTime;
    }

    public static String getStringMmSsTimeVale(int milliSec) {
        int minutes = (milliSec / 1000) / 60;
        int seconds = (milliSec / 1000) % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    public static Bitmap resizeAndCompressImage(Context context, Uri uri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            Bitmap image = BitmapFactory.decodeStream(inputStream);
            ExifInterface exifIn = new ExifInterface(context.getContentResolver().openInputStream(uri));
            int orientation = exifIn.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            int maxDimension = 1500;
            int width = image.getWidth();
            int height = image.getHeight();
            float ratio = (float) width / height;

            if (ratio > 1) {
                // landscape
                width = maxDimension;
                height = (int) (width / ratio);
            } else {
                // portrait
                height = maxDimension;
                width = (int) (height * ratio);
            }

            Matrix matrix = new Matrix();
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.setRotate(90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.setRotate(180);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.setRotate(270);
                    break;
                default:
                    break;
            }
            Bitmap resized = Bitmap.createScaledBitmap(image, width, height, true);
            resized = Bitmap.createBitmap(resized, 0, 0, resized.getWidth(), resized.getHeight(), matrix, true);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            resized.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            byte[] imageData = outputStream.toByteArray();
            long size = imageData.length / 1024;
            // size in kb

            while (size > 450) {
                outputStream.reset();
                resized.compress(Bitmap.CompressFormat.JPEG, 70, outputStream);
                imageData = outputStream.toByteArray();
                size = imageData.length / 1024;
            }
            return BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public static Uri getAudioContentUri(Context context, String filePath) {
        String[] projection = {MediaStore.Audio.Media._ID};
        String selection = MediaStore.Audio.Media.DATA + "=?";
        String[] selectionArgs = {filePath};
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null
        );
        if (cursor != null && cursor.moveToFirst()) {
            int a = cursor.getColumnIndex(MediaStore.Audio.Media._ID);
            int id = cursor.getInt(a);
            cursor.close();
            return Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, Integer.toString(id));
        }
        return null;
    }


    //Gallery Functions

    public static ArrayList<FolderFacer> getImageFolders(Context context) {
        Log.i("CURSOR:::::", "getImageFolders");
        ArrayList<FolderFacer> imageFolders = new ArrayList<>();
        ArrayList<String> imagePaths = new ArrayList<>();
        Uri allImagesuri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        //Log.i("ALL IMAGE URIs :::", allImagesuri.toString());
        String[] projection = {MediaStore.Images.ImageColumns.DATA, MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.Images.Media.BUCKET_ID};
        Cursor cursor = context.getContentResolver().query(allImagesuri, projection, null, null, MediaStore.Images.Media.DATE_ADDED + " DESC");
        try {
            cursor.moveToFirst();
            do {
                FolderFacer folds = new FolderFacer();
                String folder = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME));
                String datapath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                String folderPaths = datapath.replace(name, "");

                if (!imagePaths.contains(folderPaths)) {
                    imagePaths.add(folderPaths);
                    Log.i("CURSOR:::::", "getImageFolders___FOUND");

                    folds.setPath(folderPaths);
                    folds.setFolderName(folder);
                    //Log.i("IMAGE DATA_PATH:::", datapath);

                    int i = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
                    Log.i("IMG FUNCTION:::::", "COLUMN__" + i);
                    if (i >= 0) {
                        int imageId = cursor.getInt(i);
                        //folds.setIcon(imageId);
                        imageFolders.add(folds);
                    }

                }

                Log.i("IMG FUNCTION:::::", imageFolders.size() + "");
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

    public static Cursor getImagePaths(Context context) {
        ArrayList<FolderFacer> imageFolders = new ArrayList<>();
        ArrayList<String> imagePaths = new ArrayList<>();
        Uri allImagesuri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Images.ImageColumns.DATA, MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.Images.Media.BUCKET_ID};
        Cursor cursor = context.getContentResolver().query(allImagesuri, projection, null, null, MediaStore.Images.Media.DATE_ADDED + " DESC");

        try {
            cursor.moveToFirst();
            do {

                FolderFacer folds = new FolderFacer();

                String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME));
                String folder = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                String dataPath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));

                String folderpaths = dataPath.replace(name, "");
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
//            Log.d("Image folders",imageFolders.get(i).getFolderName()+" and path = "+imageFolders.get(i).getPath());
//        }

        return cursor;
    }

    public static Bitmap getResizedBitmapFromUri(Context cxt, Uri uri, int targetWidth, int targetHeight) {
        Bitmap bitmap = null;
        try {
            InputStream inputStream = cxt.getContentResolver().openInputStream(uri);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(inputStream, null, options);
            inputStream.close();

            int imageWidth = options.outWidth;
            int imageHeight = options.outHeight;

            int scaleFactor = Math.min(imageWidth / targetWidth, imageHeight / targetHeight);
            options.inJustDecodeBounds = false;
            options.inSampleSize = scaleFactor;
            inputStream = cxt.getContentResolver().openInputStream(uri);
            bitmap = BitmapFactory.decodeStream(inputStream, null, options);
            inputStream.close();
        } catch (IOException e) {
            return null;
        }
        return bitmap;
    }

    public static Bitmap getThumbnail(Uri uri) {
        Bitmap thumbnail;
        try {
            thumbnail = ThumbnailUtils.createVideoThumbnail(uri.getPath(), MediaStore.Images.Thumbnails.MINI_KIND);
            if (thumbnail == null) {
                thumbnail = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(uri.getPath()), 720, 1080);
            }
        } catch (Exception e) {
            return null;
        }
        return thumbnail;
    }

    public ArrayList<FolderFacer> getVideoFolders(Context context) {
        ArrayList<FolderFacer> videoFolders = new ArrayList<>();
        ArrayList<String> videoPaths = new ArrayList<>();
        Uri allVideosUri = android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Video.VideoColumns.DATA, MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.BUCKET_DISPLAY_NAME, MediaStore.Video.Media.BUCKET_ID};
        Cursor cursor = context.getContentResolver().query(allVideosUri, projection, null, null, MediaStore.Images.Media.DATE_ADDED + " DESC");

        try {
            cursor.moveToFirst();
            do {
                FolderFacer folds = new FolderFacer();
                String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME));
                String folder = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME));
                String datapath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));

                String folderPaths = datapath.replace(name, "");
                if (!videoPaths.contains(folderPaths)) {
                    videoPaths.add(folderPaths);

                    folds.setPath(folderPaths);
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
