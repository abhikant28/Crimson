package com.akw.crimson.Adapters;


import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.cursoradapter.widget.CursorAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.akw.crimson.Backend.Constants;
import com.akw.crimson.Backend.Database.TheViewModel;
import com.akw.crimson.Backend.UsefulFunctions;
import com.akw.crimson.R;
import com.akw.crimson.databinding.MessageReceivedLayoutBinding;
import com.akw.crimson.databinding.MessageSentLayoutBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;


public class Chat_RecyclerAdapter2 extends RecyclerView.Adapter {

    CursorAdapter mCursorAdapter;
    Cursor cursor;
    Chat_RecyclerAdapter2.OnListItemClickListener mOnListItemClickListener;
    Context mContext;
    TheViewModel dbview;
    boolean active, unreadFound = false;
    public int unreadPosition;


    public Chat_RecyclerAdapter2(Context context, Cursor c, Chat_RecyclerAdapter2.OnListItemClickListener onImageClickListener, TheViewModel db, boolean active) {
        mOnListItemClickListener = onImageClickListener;
        mContext = context;
        dbview = db;
        this.active = active;
        this.cursor = c;
    }

    @Override
    public int getItemViewType(int position) {
        cursor.moveToPosition(position);
        return cursor.getInt(cursor.getColumnIndexOrThrow("self"));
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case 1:
                MessageSentLayoutBinding sentBinding = MessageSentLayoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
                return new SentMessageView(sentBinding);
            case 2:
                MessageReceivedLayoutBinding receivedBinding = MessageReceivedLayoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
                return new ReceivedMessageView(receivedBinding);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        cursor.moveToPosition(position);
        if (holder instanceof SentMessageView) {
            SentMessageView viewHolder = (SentMessageView) holder;
            MessageSentLayoutBinding sent = viewHolder.sentMsgBinding;
            sent.MessageMsgBox.setText(cursor.getString(cursor.getColumnIndexOrThrow("msg")));
            sent.MessageMsgBox.setPadding(25, 1, 25, 1);
            sent.MessageTime.setText(cursor.getString(cursor.getColumnIndexOrThrow("time")));
            sent.MessageTime.setPadding(25, 1, 25, 1);
            sent.MessageClMedia.setVisibility(View.GONE);
            sent.MessageCvDoc.setVisibility(View.GONE);
        } else {
            ReceivedMessageView viewHolder = (ReceivedMessageView) holder;
            MessageReceivedLayoutBinding received = viewHolder.receivedLayoutBinding;
            received.MessageMsgBox.setText(cursor.getString(cursor.getColumnIndexOrThrow("msg")));
            received.MessageMsgBox.setPadding(25, 1, 25, 1);
            received.MessageTime.setText(cursor.getString(cursor.getColumnIndexOrThrow("time")));
            received.MessageTime.setPadding(25, 1, 25, 1);
            if (active && cursor.getInt(cursor.getColumnIndexOrThrow("unread")) == 1 && !unreadFound) {
                received.MessageLL.addView(unreadDialog(), 0);
                unreadFound = true;
            }
            if (cursor.getInt(cursor.getColumnIndexOrThrow("media")) == 1) {
                File file = UsefulFunctions.getMediaFile(mContext, cursor.getString(cursor.getColumnIndexOrThrow("mediaID")), cursor.getInt(cursor.getColumnIndexOrThrow("mediaType")), cursor.getInt(cursor.getColumnIndexOrThrow("self")) == 1);
                if (cursor.getInt(cursor.getColumnIndexOrThrow("mediaType")) == Constants.KEY_MESSAGE_MEDIA_TYPE_IMAGE) {
                    received.MessageClMedia.setVisibility(View.VISIBLE);
                    received.MessageIvImage.setVisibility(View.VISIBLE);
                    if (file != null) {
                        received.MessageIvImage.setImageBitmap(BitmapFactory.decodeFile(file.getAbsolutePath()));
                    } else {
                        checkFirestore(cursor.getString(cursor.getColumnIndexOrThrow("mediaID"))
                                , received.MessageCvImageSize, received.MessageTvImageSize
                                , received.MessagePbProgressBarMedia, received.MessageIvMediaCancel
                                , cursor.getInt(cursor.getColumnIndexOrThrow("mediaType")));
                    }
                } else if (cursor.getInt(cursor.getColumnIndexOrThrow("mediaType")) == Constants.KEY_MESSAGE_MEDIA_TYPE_DOCUMENT) {
                    received.MessageCvDoc.setVisibility(View.VISIBLE);
                } else if (cursor.getInt(cursor.getColumnIndexOrThrow("mediaType")) == Constants.KEY_MESSAGE_MEDIA_TYPE_VIDEO) {
                }
            }
        }

    }

    @Override
    public int getItemCount() {
        return mCursorAdapter.getCount();
    }

    //
//    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
//        private TextView tv_received_msg;
//        private TextView tv_received_time;
//        private TextView tv_sent_msg;
//        private TextView tv_sent_time;
//        //private TextView tv_unreadMsgs;
//        private LinearLayout ll_message,ll_received,ll_sent;
////        private TextView tv_unreadCount;
////        private ImageView iv_userPic;
//
//        Chat_RecyclerAdapter2.OnListItemClickListener onListItemClickListener;
//
//        public ViewHolder(View itemView, Chat_RecyclerAdapter2.OnListItemClickListener onListItemClickListener) {
//            super(itemView);
////            ll_message=itemView.findViewById(R.id.Message_Layout_Unread);
//            tv_received_time = itemView.findViewById(R.id.Message_Received_time);
//            tv_received_msg = itemView.findViewById(R.id.Message_Received_msgBox);
//            tv_sent_msg=itemView.findViewById(R.id.Message_Sent_msgBox);
//            tv_sent_time=itemView.findViewById(R.id.Message_Sent_time);
//            ll_message=itemView.findViewById(R.id.Message_LinearLayout);
//            ll_received=itemView.findViewById(R.id.Message_Received_LL);
//            ll_sent=itemView.findViewById(R.id.Message_Sent_LL);
//            this.onListItemClickListener=onListItemClickListener;
//            itemView.setOnClickListener(this);
//        }
//
//        @Override
//        public void onClick(View view) {
//            onListItemClickListener.onListItemClick(getAdapterPosition());
//        }
//    }
    private void checkFirestore(String fileName, CardView cvSize, TextView tvSize
            , ProgressBar progressBar, ImageView ivCancel, int mediaType) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference fileRef = storageRef.child(fileName);

        fileRef.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
            @Override
            public void onSuccess(StorageMetadata storageMetadata) {
                // File exists
                long fileSize = storageMetadata.getSizeBytes() / 1024;
                cvSize.setVisibility(View.VISIBLE);
                tvSize.setVisibility(View.VISIBLE);
                tvSize.setText(String.valueOf(fileSize));
                cvSize.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        beginDownload(fileName, ivCancel, progressBar, mediaType);
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                cvSize.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());

                        builder.setMessage("File is missing. Ask the user to resend the image");

                        builder.setPositiveButton("Got it", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });
                        builder.setNegativeButton("", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User cancelled the dialog
                            }
                        });
                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                    }
                });
            }
        });
    }

    private void beginDownload(String fileName, ImageView ivCancel, ProgressBar progressBar, int mediaType) {
        progressBar.setVisibility(View.VISIBLE);
        ivCancel.setVisibility(View.VISIBLE);

        File localFile = UsefulFunctions.getOutputMediaFile(mContext, false, mediaType);
        String folder="";
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        switch (mediaType) {
            case Constants.KEY_MESSAGE_MEDIA_TYPE_IMAGE:
                folder = "images";
                break;
            case Constants.KEY_MESSAGE_MEDIA_TYPE_VIDEO:
                folder = "videos";
                break;
            case Constants.KEY_MESSAGE_MEDIA_TYPE_DOCUMENT:
                folder = "documents";
                break;
            case Constants.KEY_MESSAGE_MEDIA_TYPE_AUDIO:
                folder = "audios";
                break;
        }

        StorageReference fileRef = storageRef.child(folder+"/"+fileName);

        Task<byte[]> task = fileRef.getBytes(Long.MAX_VALUE)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        // File downloaded successfully
                        Log.d("Download", "onSuccess: Downloaded file.");
                        FileOutputStream fos = null;
                        try {
                            fos = new FileOutputStream(localFile);
                            fos.write(bytes);
                            fos.close();
                            // Access the downloaded file
                            Log.d("Download", "onSuccess: File path: " + localFile.getAbsolutePath());
                            Log.d("Download", "onSuccess: File size: " + localFile.length() + " bytes");
                            progressBar.setVisibility(View.GONE);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle failed download
                        Log.e("Download", "onFailure: Failed to download file.", e);
                        localFile.delete();
                    }
                });


    }

    private TextView unreadDialog() {
        ConstraintLayout.LayoutParams lparams = new ConstraintLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lparams.setMargins(0, 0, 0, 15);
        TextView tv = new TextView(mContext.getApplicationContext());

        tv.setLayoutParams(lparams);
        tv.setText("Unread Messages");
        tv.setTextColor(Color.WHITE);
        tv.setPadding(25, 5, 25, 5);
        tv.setBackgroundResource(R.drawable.round_box_chat_input);
        tv.setGravity(Gravity.CENTER_HORIZONTAL);
        return tv;
    }

    public interface OnListItemClickListener {
        void onListItemClick(int position);
    }

    public void setOnItemClickListener(OnListItemClickListener listener) {
        this.mOnListItemClickListener = listener;
    }

    class SentMessageView extends RecyclerView.ViewHolder {
        MessageSentLayoutBinding sentMsgBinding;

        public SentMessageView(@NonNull MessageSentLayoutBinding binding) {
            super(binding.getRoot());
            this.sentMsgBinding = binding;
        }
    }

    class ReceivedMessageView extends RecyclerView.ViewHolder {
        MessageReceivedLayoutBinding receivedLayoutBinding;

        public ReceivedMessageView(@NonNull MessageReceivedLayoutBinding binding) {
            super(binding.getRoot());
            this.receivedLayoutBinding = binding;
        }
    }

}
