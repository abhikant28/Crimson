package com.akw.crimson;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;

import com.akw.crimson.Backend.AppObjects.Message;
import com.akw.crimson.Backend.AppObjects.User;
import com.akw.crimson.Backend.Communications.Communicator;
import com.akw.crimson.Backend.Constants;
import com.akw.crimson.Backend.ScrollableImageView;
import com.akw.crimson.Backend.UsefulFunctions;
import com.akw.crimson.databinding.ActivityAdjustWallpaperBinding;

import java.io.File;

public class AdjustWallpaper extends BaseActivity {

    ActivityAdjustWallpaperBinding layoutBinding;
    File file;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        layoutBinding = ActivityAdjustWallpaperBinding.inflate(getLayoutInflater());
        setContentView(layoutBinding.getRoot());

        String userName = getIntent().getStringExtra(Constants.Intent.KEY_INTENT_USERNAME);
        String userID=getIntent().getStringExtra(Constants.Intent.KEY_INTENT_USERID);
        String path = getIntent().getStringExtra(Constants.Intent.KEY_INTENT_FILE_PATH);

        Log.i("PATH::::", path);
        baseActionBar.setTitle(userName);

        layoutBinding.adjustWallpaperBtnSubmit.setOnClickListener(view->{

            Bitmap b=cropVisibleBitmap(layoutBinding.adjustWallpaperIvBackground);
            file= UsefulFunctions.FileUtil.makeOutputMediaFile(this,false, Constants.Media.KEY_MESSAGE_MEDIA_TYPE_WALLPAPER);
            UsefulFunctions.FileUtil.saveImage(b,false,file);
            User user= Communicator.localDB.getUser(userID);
            user.setWallpaper(file.getName());
            Communicator.localDB.updateUser(user);
            shareWallpaper(user.getDisplayName(),userID);
        });

        scaleImage(path);
    }

    private void shareWallpaper(String userName, String userID) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("Do you want to share this wallpaper with "+userName+" ?")
                .setTitle("Share this wallpaper");

        builder.setPositiveButton("Share", (dialog, id) -> {
            Message msg= new Message(Communicator.thisUserID,userID,"Some message",true,null,true
                    ,Constants.Message.MESSAGE_STATUS_BULK_MEDIA_UPLOAD_PENDING,Constants.Message.MESSAGE_TYPE_INTERNAL
                    ,Constants.Media.KEY_MESSAGE_MEDIA_TYPE_WALLPAPER,file.getName(),null);
            Communicator.localDB.insertMessage(msg);
            finish();
        });
        builder.setNegativeButton("No", (dialog, id) -> {
            // User cancelled the dialog
            finish();
        });
        AlertDialog alertDialog = builder.create();
        // Show the Alert Dialog box
        alertDialog.show();
    }

    private void scaleImage(String path) {

        Uri imageUri = Uri.parse(path);

        Bitmap bitmap = null;
        try {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                bitmap=UsefulFunctions.FileUtil.getImageFromUri(this,imageUri);
            }
             // Use the bitmap as needed
        } catch (Exception e) {
            e.printStackTrace();
        }


        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;

// Calculate the aspect ratio of the image
        float imageAspectRatio = (float) bitmap.getWidth() / bitmap.getHeight();

// Calculate the target dimensions based on the screen size and aspect ratio
        int targetWidth;
        int targetHeight;

        if (imageAspectRatio > 1.0f) {
            // Image is wider than tall, adjust height to fit the screen
            targetHeight = screenHeight;
            targetWidth = (int) (targetHeight * imageAspectRatio);
        } else {
            // Image is taller than wide, adjust width to fit the screen
            targetWidth = screenWidth;
            targetHeight = (int) (targetWidth / imageAspectRatio);
        }

// Create a scaled bitmap with the target dimensions
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true);

        // Set the scaled bitmap to the ImageView
        layoutBinding.adjustWallpaperIvBackground.setImageBitmap(scaledBitmap);
        layoutBinding.adjustWallpaperIvBackground.setMinimumHeight(screenHeight);
        layoutBinding.adjustWallpaperIvBackground.setMinimumWidth(screenWidth);

    }

    public Bitmap cropVisibleBitmap(ScrollableImageView imageView) {
        // Get the current dimensions of the ImageView
        int viewWidth = imageView.getWidth();
        int viewHeight = imageView.getHeight();

        // Create a new bitmap for the visible portion
        Bitmap croppedBitmap = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.ARGB_8888);

        // Create a canvas for drawing on the cropped bitmap
        Canvas canvas = new Canvas(croppedBitmap);

        // Calculate the scale factors to map the ImageView dimensions to the underlying drawable's intrinsic dimensions
        Drawable drawable = imageView.getDrawable();
        int drawableWidth = drawable.getIntrinsicWidth();
        int drawableHeight = drawable.getIntrinsicHeight();
        float scaleX = (float) drawableWidth / viewWidth;
        float scaleY = (float) drawableHeight / viewHeight;

        // Calculate the visible bounds of the drawable on the ImageView
        int scrollX = imageView.getScrollX();
        int scrollY = imageView.getScrollY();
//        int left = Math.max((int) (scrollX * scaleX), 0);
//        int top = Math.max((int) (scrollY * scaleY), 0);
//        int right = Math.min((int) ((scrollX + viewWidth) * scaleX), drawableWidth);
//        int bottom = Math.min((int) ((scrollY + viewHeight) * scaleY), drawableHeight);

        // Set the bounds for drawing on the canvas
        drawable.setBounds(-scrollX, -scrollY, -scrollX + drawableWidth, -scrollY + drawableHeight);

        // Save the current matrix and clip bounds of the canvas
        canvas.save();
        canvas.clipRect(0, 0, viewWidth, viewHeight);

        // Draw the drawable onto the canvas with the updated bounds
        drawable.draw(canvas);

        // Restore the saved matrix and clip bounds of the canvas
        canvas.restore();

        // Return the cropped bitmap
        return croppedBitmap;
    }



//    private void draggableImageView() {
//
//        layoutBinding.draggableFrame.setOnTouchListener(new View.OnTouchListener() {
//            private static final int EDGE_THRESHOLD = 50;
//            private float initialTouchX, initialTouchY;
//            private int initialWidth, initialHeight;
//            private boolean edgeTouched = false;
//            private Rect parentBoundaries = new Rect();
//
//
//            @Override
//            public boolean onTouch(View view, MotionEvent event) {
//                float touchX = event.getX();
//                float touchY = event.getY();
//                float deltaX = touchX - initialTouchX;
//                float deltaY = touchY - initialTouchY;
//                int newWidth = 0;
//                int newHeight =0 ;
//                ((View) layoutBinding.imageView).getGlobalVisibleRect(parentBoundaries);
//                boolean leftEdgeTouched = touchX <= EDGE_THRESHOLD;
//                boolean rightEdgeTouched = touchX >= view.getWidth() - EDGE_THRESHOLD;
//                boolean topEdgeTouched = touchY <= EDGE_THRESHOLD;
//                boolean bottomEdgeTouched = touchY >= view.getHeight() - EDGE_THRESHOLD;
//
//
//                switch (event.getAction()) {
//                    case MotionEvent.ACTION_DOWN:
//                        initialTouchX = touchX;
//                        initialTouchY = touchY;
//                        initialWidth = view.getWidth();
//                        initialHeight = view.getHeight();
//                        edgeTouched = touchX <= EDGE_THRESHOLD || touchY <= EDGE_THRESHOLD || touchX >= view.getWidth() - EDGE_THRESHOLD || touchY >= view.getHeight() - EDGE_THRESHOLD;
//                        break;
//                    case MotionEvent.ACTION_MOVE:
//                        if (edgeTouched) {
//
//                            if (leftEdgeTouched || rightEdgeTouched || topEdgeTouched || bottomEdgeTouched) {
//                                ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
//
//                                if (leftEdgeTouched) {
//                                    newWidth = (int) (view.getWidth() - deltaX);
//                                    if (newWidth >= view.getMinimumWidth()) {
//                                        layoutParams.width = newWidth;
//                                        layoutParams.leftMargin += deltaX;
//                                    }
//                                } else if (rightEdgeTouched) {
//                                     newWidth = (int) (view.getWidth() + deltaX);
//                                    if (newWidth >= view.getMinimumWidth()) {
//                                        layoutParams.width = newWidth;
//                                        layoutParams.rightMargin -= deltaX;
//                                    }
//                                }
//                                if (topEdgeTouched) {
//                                    // Adjust the top edge
//                                     newHeight = (int) (view.getHeight() - deltaY);
//                                    if (newHeight >= view.getMinimumHeight()) {
//                                        layoutParams.height = newHeight;
//                                        layoutParams.topMargin += deltaY;
//                                    }
//                                } else if (bottomEdgeTouched) {
//                                    // Adjust the bottom edge
//                                    newHeight = (int) (initialHeight + deltaY);
//                                    layoutParams.height = Math.max(newHeight, view.getMinimumHeight());
//                                    layoutParams.bottomMargin += newHeight - initialHeight;
//                                }
//
//
//                                // Update layout params and request layout
//                                view.setLayoutParams(layoutParams);
//                                view.requestLayout();
//                            }
//
//                        } else {
//                            // Handle dragging the view
//                            float newX = view.getX() + deltaX;
//                            float newY = view.getY() + deltaY;
//
//                            // Apply boundary restrictions
//                            if (newX >= parentBoundaries.left && newX + view.getWidth() <= parentBoundaries.right) {
//                                view.setX(newX);
//                            }
//                            if (newY >= parentBoundaries.top && newY + view.getHeight() <= parentBoundaries.bottom) {
//                                view.setY(newY);
//                            }
//                        }
//                        break;
//                    case MotionEvent.ACTION_UP:
//                        // Reset variables and flags
//                        initialTouchX = 0;
//                        initialTouchY = 0;
//                        initialWidth = 0;
//                        initialHeight = 0;
//                        edgeTouched = false;
//                        break;
//                }
//                return true;
//            }
//        });
//
//    }
}