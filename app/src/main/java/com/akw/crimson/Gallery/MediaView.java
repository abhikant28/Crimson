package com.akw.crimson.Gallery;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import androidx.viewpager.widget.ViewPager;

import com.akw.crimson.Gallery.GalleryAdapters.MediaViewAdapter;
import com.akw.crimson.R;

public class MediaView extends Activity {

    ScaleGestureDetector mScaleGestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_view);
        //Log.i("VIEW INTENT:::", getIntent().getStringExtra("POSITION"));
        int position = Integer.parseInt(getIntent().getStringExtra("POSITION"));
        //mediaUriList =getIntent().getStringArrayListExtra("LIST");
        ViewPager viewPager = findViewById(R.id.MediaView_ViewPager);
        MediaViewAdapter adapter = new MediaViewAdapter(getApplicationContext(), MainGalleryActivity.mediaURI);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(position);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mScaleGestureDetector.onTouchEvent(event);
        return true;
    }


}