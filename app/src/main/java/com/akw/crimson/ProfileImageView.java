package com.akw.crimson;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.core.view.ViewCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.akw.crimson.Backend.Constants;
import com.akw.crimson.Backend.UsefulFunctions;
import com.akw.crimson.Chat.ChatActivity;

import java.io.File;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileImageView#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileImageView extends DialogFragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = Constants.Intent.KEY_INTENT_USERID;
    private static final String ARG_PARAM2 = Constants.Intent.KEY_INTENT_PIC;

    // TODO: Rename and change types of parameters
    private String userId;
    private String pic;

    public ProfileImageView() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static ProfileImageView newInstance(String param1, String param2) {
        ProfileImageView fragment = new ProfileImageView();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userId = getArguments().getString(ARG_PARAM1);
            pic = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.fragment_profile_image_view, container, false);

        // Inflate the layout for this fragment
        ImageView iv_profilePic=v.findViewById(R.id.profilePicFragment_iv_profilePic);
        File file= UsefulFunctions.FileUtil.getFile(v.getContext(),pic, Constants.Media.KEY_MESSAGE_MEDIA_TYPE_PROFILE);
        iv_profilePic.setImageURI(Uri.fromFile(file));
        ViewCompat.setTransitionName(iv_profilePic,"hero_image");
//        Transition transition = TransitionInflater.from(requireContext())
//                .inflateTransition(R.transition.shared_image);
//        setSharedElementEnterTransition(transition);

        v.findViewById(R.id.profilePicFragment_ib_profile).setOnClickListener(view -> {

        });
        v.findViewById(R.id.profilePicFragment_ib_message).setOnClickListener(view -> {
            Intent intent = new Intent(v.getContext(), ChatActivity.class);
            intent.putExtra(Constants.Intent.KEY_INTENT_USERID, userId);
            startActivity(intent);
        });
        return v;
    }
}