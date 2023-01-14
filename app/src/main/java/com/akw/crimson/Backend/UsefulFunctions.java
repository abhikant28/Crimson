package com.akw.crimson.Backend;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
            resized=Bitmap.createBitmap(resized, 0, 0, resized.getWidth(), resized.getHeight(), matrix, true);
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


    public static boolean saveBitmapAsJpeg(Context context, Bitmap bitmap, boolean sent) {
        File pictureFile = getOutputMediaFile(context,sent);

        if (pictureFile == null) {
            Log.d(TAG + "::::",
                    "Error creating media file, check storage permissions: ");// e.getMessage());
            return false;
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d("ERROR ::::", "File not found: " + e.getMessage());
            return false;
        } catch (IOException e) {
            Log.d("ERROR ::::", "Error accessing file: " + e.getMessage());
            return false;
        }

        Log.i(TAG + "::::", pictureFile.getAbsolutePath());
        return true;
    }

    private static File getOutputMediaFile(Context context, boolean sent) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        String fol=sent?"/Sent":"";
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
                + "/Android/media" +
                "" +
                "/"
                + context.getApplicationContext().getPackageName()
                + "/Crimson Images"+fol);

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
        String mImageName = "IMG_" + timeStamp + ".jpg";
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);
        Log.i(TAG, mImageName);
        return mediaFile;
    }


//    public static String compressText(String input) {
//        byte[] decoded = Base64.decode(input, Base64.DEFAULT);
//        return BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
//    }
//
//    public static String decompressText(String input) {
//        int previewWidth = 150;
//        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
//        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
//        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
//        byte[] bytes = byteArrayOutputStream.toByteArray();
//        return Base64.encodeToString(bytes, Base64.DEFAULT);
//    }

}
