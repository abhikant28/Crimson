package com.akw.crimson.Gallery;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ImageView;

import androidx.viewpager.widget.ViewPager;

import com.akw.crimson.Gallery.GalleryAdapters.MediaViewAdapter;
import com.akw.crimson.R;

public class MediaView extends Activity {

    ScaleGestureDetector mScaleGestureDetector;
    String imgPath;
    private ImageView imageView;
    private ScaleGestureDetector scaleGestureDetector;
    // on below line we are defining our scale factor.
    private float mScaleFactor = 1.0f;

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        // inside on touch event method we are calling on
        // touch event method and passing our motion event to it.
        scaleGestureDetector.onTouchEvent(motionEvent);
        return true;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_view);
        //Log.i("VIEW INTENT:::", getIntent().getStringExtra("POSITION"));
        int position = Integer.parseInt(getIntent().getStringExtra("POSITION"));
        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());
        ViewPager viewPager = findViewById(R.id.MediaView_ViewPager);
        MediaViewAdapter adapter = new MediaViewAdapter(getApplicationContext(), MainGalleryActivity.mediaURI);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(position);

    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        // on below line we are creating a class for our scale
        // listener and  extending it with gesture listener.
        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector) {

            // inside on scale method we are setting scale
            // for our image in our image view.
            mScaleFactor *= scaleGestureDetector.getScaleFactor();
            mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 10.0f));

            // on below line we are setting
            // scale x and scale y to our image view.
            imageView.setScaleX(mScaleFactor);
            imageView.setScaleY(mScaleFactor);
            return true;
        }
    }
}
