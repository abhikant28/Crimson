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

    public static Bitmap compressImage(Bitmap bitmap) {

        // Initialize a ByteArrayOutputStream
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // Compress the input Bitmap into the output stream
        bitmap.compress(Bitmap.CompressFormat.PNG, 20, baos);

        // Iteratively compress the Bitmap until its size is less than 100KB
//        int options = 100;
//        while (baos.size() > 100000) {
//            // Reset the output stream and compress the Bitmap again
//            baos.reset();
//            options -= 10;
//            bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos);
//        }

        // Convert the output stream to a byte array and create a new Bitmap from the array
        byte[] imageData = baos.toByteArray();
        return BitmapFactory.decodeByteArray(imageData, 0, imageData.length);

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static Bitmap resizeAndCompressImage(Context context, Uri uri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            Bitmap image = BitmapFactory.decodeStream(inputStream);
            Bitmap.Config conf = image.getConfig();

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

            Bitmap resized = Bitmap.createScaledBitmap(image, width, height, true);
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

    private static int getImgOrientation(Uri imageUri) {
        ExifInterface exif;
        try {
            exif = new ExifInterface(imageUri.getPath());
        } catch (IOException e) {
            return ExifInterface.ORIENTATION_UNDEFINED;
        }
        return exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
    }
    private static Bitmap setImgOrientation(Bitmap bitmap, int orientation) {
        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_NORMAL:
                break;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(-90);
                break;
            default:
                break;
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }


    public static boolean saveBitmapAsJpeg(Context context, Bitmap bitmap) {
        File pictureFile = getOutputMediaFile(context);
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
            Log.d(TAG + "::::", "File not found: " + e.getMessage());
            return false;
        } catch (IOException e) {
            Log.d(TAG + "::::", "Error accessing file: " + e.getMessage());
            return false;
        }

        Log.i(TAG + "::::", pictureFile.getAbsolutePath());
        return true;
    }

    private static File getOutputMediaFile(Context context) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
                + "/Android/media" +
                "" +
                "/"
                + context.getApplicationContext().getPackageName()
                + "/Image");

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


    public static class LZWCompressor {

        public static String compress(String input) {
            // Initialize the dictionary
            Map<String, Integer> dictionary = new HashMap<>();
            for (int i = 0; i < 256; i++) {
                dictionary.put((char) i + "", i);
            }
            String phrase = "";
            StringBuilder output = new StringBuilder();
            for (char c : input.toCharArray()) {
                String currChar = c + "";
                String newPhrase = phrase + currChar;
                if (dictionary.containsKey(newPhrase)) {
                    phrase = newPhrase;
                } else {
                    output.append(dictionary.get(phrase));
                    dictionary.put(newPhrase, dictionary.size());
                    phrase = currChar;
                }
            }
            // Add the remaining phrase to the output
            if (!phrase.equals("")) {
                output.append(dictionary.get(phrase));
            }
            return output.toString();
        }

        public static String decompress(String input) {
            // Initialize the dictionary
            Map<Integer, String> dictionary = new HashMap<>();
            for (int i = 0; i < 256; i++) {
                dictionary.put(i, (char) i + "");
            }
            String phrase = "";
            int prevCode = input.charAt(0);
            StringBuilder output = new StringBuilder((char) prevCode + "");
            for (int i = 1; i < input.length(); i++) {
                int currCode = input.charAt(i);
                String currChar;
                if (dictionary.containsKey(currCode)) {
                    currChar = dictionary.get(currCode);
                } else {
                    currChar = phrase + phrase.charAt(0);
                }
                output.append(currChar);
                dictionary.put(dictionary.size(), phrase + currChar.charAt(0));
                phrase = currChar;
            }
            return output.toString();
        }

    }

}
