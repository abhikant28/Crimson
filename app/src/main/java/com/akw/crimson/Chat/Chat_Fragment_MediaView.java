package com.akw.crimson.Chat;

import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;

import com.akw.crimson.Backend.Adapters.MediaViewAdapter;
import com.akw.crimson.Backend.Communications.Communicator;
import com.akw.crimson.Backend.Constants;
import com.akw.crimson.R;

public class Chat_Fragment_MediaView extends Fragment {

    ScaleGestureDetector mScaleGestureDetector;
    GestureDetector gestureDetector;
    Toolbar toolBar;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getActivity().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            ActionBar actionBar = activity.getSupportActionBar();
            requireActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            if (actionBar != null) {
                actionBar.show();
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.chat_fragment_media_view, container, false);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        toolBar = v.findViewById(R.id.chat_FragmentMediaView_toolbar);

        if (activity != null) {
            ActionBar actionBar = activity.getSupportActionBar();
            requireActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            if (actionBar != null) {
                actionBar.hide();
            }
        }
        int position = getArguments().getInt(Constants.KEY_INTENT_LIST_POSITION);
        Log.i("MEDIA LIST::::::", ChatActivity.mediaList.size() + "");
        gestureDetector = new GestureDetector(container.getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                Log.i("MOTION DETECTED:::::", Math.abs(velocityY) + "");
                if ((e2.getY() - e1.getY() > 100 || e1.getY() - e2.getY() > 100) && Math.abs(velocityY) > 100) {
                    closeFragment();
                    return true;
                }
                return false;
            }
        });
        ViewPager2 viewPager = v.findViewById(R.id.chat_FragmentMediaView_ViewPager);
        MediaViewAdapter adapter = new MediaViewAdapter(v.getContext(), ChatActivity.mediaList, Communicator.localDB, toolBar);
        viewPager.setOnTouchListener((v1, event) -> gestureDetector.onTouchEvent(event));
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

}