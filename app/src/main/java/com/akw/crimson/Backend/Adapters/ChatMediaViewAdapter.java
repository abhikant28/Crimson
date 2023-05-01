package com.akw.crimson.Backend.Adapters;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.PagerAdapter;

import com.akw.crimson.Backend.AppObjects.Message;
import com.akw.crimson.Backend.UsefulFunctions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ChatMediaViewAdapter extends PagerAdapter {
    private final Context mContext;
    private List<Message> medias=new ArrayList<>();

    public ChatMediaViewAdapter(Context applicationContext, List<Message> mediaUri) {
        mContext=applicationContext;
        this.medias=mediaUri;
    }

    @Override
    public int getCount() {
        return medias.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        Message msg = medias.get(position);
        File file = UsefulFunctions.getFile(mContext, msg.getMediaID()
                , msg.getMediaType(), msg.isSelf());
        ImageView imageView = new ImageView(mContext);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageURI(Uri.parse(file.toURI().toString()));
        container.addView(imageView, 0);
        return imageView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((ImageView) object);
    }

}