package com.akw.crimson.Chat;

import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import com.akw.crimson.Backend.Adapters.ChatMediaViewAdapter;
import com.akw.crimson.Backend.Constants;
import com.akw.crimson.R;

public class Chat_Fragment_MediaView extends Fragment {

    ScaleGestureDetector mScaleGestureDetector;
    GestureDetector gestureDetector;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.chat_fragment_media_view, container, false);

        int position = getArguments().getInt(Constants.KEY_INTENT_LIST_POSITION);
        gestureDetector = new GestureDetector(container.getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                Log.i("MOTION DETECTED:::::", Math.abs(velocityY)+"");
                if ((e2.getY() - e1.getY() > 100 || e1.getY() - e2.getY() > 100) && Math.abs(velocityY) > 100) {
                    closeFragment();
                    return true;
                }
                return false;
            }
        });
        ViewPager viewPager = v.findViewById(R.id.chat_FragmentMediaView_ViewPager);
        ChatMediaViewAdapter adapter = new ChatMediaViewAdapter(v.getContext(), ChatActivity.mediaList);
        viewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(position);


        return v;
    }


    private void closeFragment() {
        FragmentManager fragmentManager = getFragmentManager();
        if (fragmentManager != null) {
            fragmentManager.popBackStack();
        }
    }

//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        mScaleGestureDetector.onTouchEvent(event);
//        return true;
//    }
}