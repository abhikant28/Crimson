package com.akw.crimson.Backend;

import static android.content.ContentValues.TAG;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class UsefulFunctions {
    public static String encodeImage(Bitmap bitmap) {
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    public static Bitmap decodeImage(String input) {
        byte[] decoded = Base64.decode(input, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
    }

    public static String encodeText(String input) {
        return Base64.encodeToString(input.getBytes(), Base64.DEFAULT);
    }

    public static String decodeText(String input) {
        return new String(Base64.decode(input, Base64.DEFAULT));
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


    public static String saveImage(Context context, Bitmap bitmap, boolean sent) {
        File pictureFile = getOutputMediaFile(context, sent, Constants.KEY_MESSAGE_MEDIA_TYPE_IMAGE);
        return saveImage(context, bitmap, sent, pictureFile);
    }

    public static String saveImage(Context context, Bitmap bitmap, boolean sent, File file) {
        File pictureFile=file;
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
        return pictureFile.getName();
    }

    public static File getOutputMediaFile(Context context, boolean sent, int type) {
        return getOutputMediaFile(context, sent, type, null);
    }

    public static File getOutputMediaFile(Context context, boolean sent, int type, String docName) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        String folder = "";
        String init = "";
        String format = "";
        switch (type) {
            case Constants.KEY_MESSAGE_MEDIA_TYPE_IMAGE:
                folder = "Images";
                init = "IMG";
                format = ".jpg";
                break;
            case Constants.KEY_MESSAGE_MEDIA_TYPE_VIDEO:
                folder = "Videos";
                init = "VID";
                format = ".mp4";
                break;
            case Constants.KEY_MESSAGE_MEDIA_TYPE_DOCUMENT:
                folder = "Documents";
                break;
            case Constants.KEY_MESSAGE_MEDIA_TYPE_AUDIO:
                folder = "Audios";
                init = "AUD";
                format = ".mp3";
                break;
        }
        String fol = sent ? "/Sent" : "";
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
                + "/Android/media" +
                "" +
                "/"
                + context.getApplicationContext().getPackageName()
                + "/Crimson " + folder + fol);

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
        String mImageName = docName == null ? init + "_" + timeStamp + format : docName;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);
        int i = 1;
        while (mediaFile.exists()) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName + " (" + (i++) + ")");
        }
        Log.i(TAG, mImageName);
        return mediaFile;
    }

    public static File getFile(Context context, String id, int type, boolean sent) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        Log.i("TYPE:::::::",type+"");

        String folder = "";
        switch (type) {
            case Constants.KEY_MESSAGE_MEDIA_TYPE_IMAGE:
                folder = "Images";
                break;
            case Constants.KEY_MESSAGE_MEDIA_TYPE_VIDEO:
                folder = "Videos";
                break;
            case Constants.KEY_MESSAGE_MEDIA_TYPE_DOCUMENT:
                folder = "Documents";
                break;
            case Constants.KEY_MESSAGE_MEDIA_TYPE_AUDIO:
                folder = "Audios";
                break;
        }
        String subFolder = sent ? "/Sent" : "";
        String mediaStorageDir = Environment.getExternalStorageDirectory()
                + "/Android/media" +
                "" +
                "/"
                + context.getApplicationContext().getPackageName()
                + "/Crimson " + folder + subFolder;

        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        Log.i("FILE::::",mediaStorageDir);
        return new File(mediaStorageDir, id);
    }

    public static String saveFile(byte[] bytes, File outFile) throws IOException {
        OutputStream os = new FileOutputStream(outFile);
        os.write(bytes);
        os.close();
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
        return outputFile.getName();
    }

    public static String getFileName(Context context, Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            int c=cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
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
        return result;
    }

}
