package com.akw.crimson.Utilities;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;

import com.akw.crimson.BaseActivity;
import com.akw.crimson.R;
import com.akw.crimson.databinding.ActivityCropImageBinding;

public class CropImage extends BaseActivity {

    ActivityCropImageBinding layoutBinding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        layoutBinding=ActivityCropImageBinding.inflate(getLayoutInflater());
        setContentView(layoutBinding.getRoot());




    }


    private void draggableImageView() {

        layoutBinding.draggableFrame.setOnTouchListener(new View.OnTouchListener() {
            private static final int EDGE_THRESHOLD = 50;
            private float initialTouchX, initialTouchY;
            private int initialWidth, initialHeight;
            private boolean edgeTouched = false;
            private Rect parentBoundaries = new Rect();


            @Override
            public boolean onTouch(View view, MotionEvent event) {
                float touchX = event.getX();
                float touchY = event.getY();
                float deltaX = touchX - initialTouchX;
                float deltaY = touchY - initialTouchY;
                int newWidth = 0;
                int newHeight =0 ;
                ((View) layoutBinding.imageView).getGlobalVisibleRect(parentBoundaries);
                boolean leftEdgeTouched = touchX <= EDGE_THRESHOLD;
                boolean rightEdgeTouched = touchX >= view.getWidth() - EDGE_THRESHOLD;
                boolean topEdgeTouched = touchY <= EDGE_THRESHOLD;
                boolean bottomEdgeTouched = touchY >= view.getHeight() - EDGE_THRESHOLD;


                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialTouchX = touchX;
                        initialTouchY = touchY;
                        initialWidth = view.getWidth();
                        initialHeight = view.getHeight();
                        edgeTouched = touchX <= EDGE_THRESHOLD || touchY <= EDGE_THRESHOLD || touchX >= view.getWidth() - EDGE_THRESHOLD || touchY >= view.getHeight() - EDGE_THRESHOLD;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (edgeTouched) {

                            if (leftEdgeTouched || rightEdgeTouched || topEdgeTouched || bottomEdgeTouched) {
                                ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();

                                if (leftEdgeTouched) {
                                    newWidth = (int) (view.getWidth() - deltaX);
                                    if (newWidth >= view.getMinimumWidth()) {
                                        layoutParams.width = newWidth;
                                        layoutParams.leftMargin += deltaX;
                                    }
                                } else if (rightEdgeTouched) {
                                    newWidth = (int) (view.getWidth() + deltaX);
                                    if (newWidth >= view.getMinimumWidth()) {
                                        layoutParams.width = newWidth;
                                        layoutParams.rightMargin -= deltaX;
                                    }
                                }
                                if (topEdgeTouched) {
                                    // Adjust the top edge
                                    newHeight = (int) (view.getHeight() - deltaY);
                                    if (newHeight >= view.getMinimumHeight()) {
                                        layoutParams.height = newHeight;
                                        layoutParams.topMargin += deltaY;
                                    }
                                } else if (bottomEdgeTouched) {
                                    // Adjust the bottom edge
                                    newHeight = (int) (initialHeight + deltaY);
                                    layoutParams.height = Math.max(newHeight, view.getMinimumHeight());
                                    layoutParams.bottomMargin += newHeight - initialHeight;
                                }


                                // Update layout params and request layout
                                view.setLayoutParams(layoutParams);
                                view.requestLayout();
                            }

                        } else {
                            // Handle dragging the view
                            float newX = view.getX() + deltaX;
                            float newY = view.getY() + deltaY;

                            // Apply boundary restrictions
                            if (newX >= parentBoundaries.left && newX + view.getWidth() <= parentBoundaries.right) {
                                view.setX(newX);
                            }
                            if (newY >= parentBoundaries.top && newY + view.getHeight() <= parentBoundaries.bottom) {
                                view.setY(newY);
                            }
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        // Reset variables and flags
                        initialTouchX = 0;
                        initialTouchY = 0;
                        initialWidth = 0;
                        initialHeight = 0;
                        edgeTouched = false;
                        break;
                }
                return true;
            }
        });

    }


}