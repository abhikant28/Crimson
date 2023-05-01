package com.akw.crimson.Gallery.GalleryAdapters;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.viewpager.widget.PagerAdapter;

import java.util.ArrayList;

public class MediaViewAdapter extends PagerAdapter {
    private final Context mContext;
    private ArrayList<String> mediaUri=new ArrayList<>();

    public MediaViewAdapter(Context applicationContext, ArrayList<String> mediaUri) {
        mContext=applicationContext;
        this.mediaUri=mediaUri;
    }

    @Override
    public int getCount() {
        return mediaUri.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        ImageView imageView = new ImageView(mContext);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageURI(Uri.parse(mediaUri.get(position)));
        container.addView(imageView, 0);
        return imageView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((ImageView) object);
    }


}