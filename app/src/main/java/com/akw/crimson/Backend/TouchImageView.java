package com.akw.crimson.Backend;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

public class TouchImageView extends androidx.appcompat.widget.AppCompatImageView implements View.OnTouchListener {

    private ScaleGestureDetector mScaleGestureDetector;

    public TouchImageView(Context context) {
        super(context);
        init();
    }

    public TouchImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TouchImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mScaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleListener());
        setOnTouchListener(this);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        mScaleGestureDetector.onTouchEvent(event);
        return true;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scaleFactor = detector.getScaleFactor();
            float currentScale = getScaleX();
            currentScale *= scaleFactor;
            setScaleX(currentScale);
            setScaleY(currentScale);
            return true;
        }
    }
}
