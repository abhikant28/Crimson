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
    private Context mContext;
    private ArrayList<String> mediaUri=new ArrayList<>();

    public MediaViewAdapter(Context applicationContext, ArrayList<String> mediaUri) {
        mContext=applicationContext;
        this.mediaUri=mediaUri;
    }
//    private CursorAdapter mediaAdapter;

    void ImageAdapter(Context context,ArrayList<String> uris) {
        mContext = context;
        mediaUri=uris;
//        mediaAdapter = new CursorAdapter(mContext, c, 0) {
//            @Override
//            public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
//                View view = LayoutInflater.from(context).inflate(R.layout.activity_media_view, (ViewGroup) viewGroup.getParent(), false);
//                return view;
//
//            }
//
//            @Override
//            public void bindView(View view, Context context, Cursor cursor) {
//
//            }
//
//
//        };
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
//        mediaAdapter.getCursor().moveToPosition(position);
//        mediaAdapter.bindView(container..itemView, mContext, mediaAdapter.getCursor());

        ImageView imageView = new ImageView(mContext);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
//        imageView.setImageURI(Uri.parse(mediaAdapter.getCursor().getString(mediaAdapter.getCursor().getColumnIndexOrThrow(MediaStore.Images.Media.DATA))));
        Log.i("IMAGE VIEW URI:::", mediaUri.get(position));
        imageView.setImageURI(Uri.parse(mediaUri.get(position)));
        container.addView(imageView, 0);
        return imageView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((ImageView) object);
    }
}