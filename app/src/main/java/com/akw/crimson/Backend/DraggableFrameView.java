package com.akw.crimson.Backend;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.Nullable;

public class DraggableFrameView extends androidx.appcompat.widget.AppCompatImageView {
        private float lastTouchX;
        private float lastTouchY;

        public DraggableFrameView(Context context) {
            super(context);
        }

        public DraggableFrameView(Context context, @Nullable AttributeSet attrs) {
            super(context, attrs);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float currentX = event.getX();
            float currentY = event.getY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    lastTouchX = currentX;
                    lastTouchY = currentY;
                    break;

                case MotionEvent.ACTION_MOVE:
                    float offsetX = currentX - lastTouchX;
                    float offsetY = currentY - lastTouchY;

                    float newTranslationX = getTranslationX() + offsetX;
                    float newTranslationY = getTranslationY() + offsetY;

                    setTranslationX(newTranslationX);
                    setTranslationY(newTranslationY);

                    lastTouchX = currentX;
                    lastTouchY = currentY;
                    break;

                case MotionEvent.ACTION_UP:
                    // Additional logic if needed
                    break;
            }

            return true;
        }

}
