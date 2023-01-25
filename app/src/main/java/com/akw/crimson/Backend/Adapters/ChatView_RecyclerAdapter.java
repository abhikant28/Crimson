package com.akw.crimson.Backend.Adapters;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.provider.MediaStore;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.RecyclerView;

import com.akw.crimson.Backend.Communications.DownloadFileService;
import com.akw.crimson.Backend.Communications.UploadFileService;
import com.akw.crimson.Backend.Constants;
import com.akw.crimson.Backend.Database.TheViewModel;
import com.akw.crimson.Backend.UsefulFunctions;
import com.akw.crimson.R;
import com.akw.crimson.databinding.MessageReceivedLayoutBinding;
import com.akw.crimson.databinding.MessageSentLayoutBinding;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ChatView_RecyclerAdapter extends RecyclerView.Adapter {

    Cursor cursor;
    private OnItemClickListener mOnListItemClickListener;
    Context mContext;
    TheViewModel dbview;
    boolean active, unreadFound = false;
    private OnItemClickListener listener;


    public ChatView_RecyclerAdapter(Context context, Cursor c, OnItemClickListener onImageClickListener, TheViewModel db, boolean active) {
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
            case 0:
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
            if (cursor.getString(cursor.getColumnIndexOrThrow("msg")) != null) {
                String msg=cursor.getString(cursor.getColumnIndexOrThrow("msg"));
                msg.replaceAll("(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]", "<a href='$0'>$0</a>")
                        .replaceAll("[^\\s]+@[^\\s]+", "<a href='mailto:$0'>$0</a>")
                        .replaceAll("(\\d{3}[-\\.\\s]??\\d{3}[-\\.\\s]??\\d{4}|\\(\\d{3}\\)\\s*\\d{3}[-\\.\\s]??\\d{4}|\\d{3}[-\\.\\s]??\\d{4})", "<a href='tel:$0'>$0</a>");
                sent.MessageMsgBox.setText(msg);
                sent.MessageMsgBox.setPadding(25, 1, 25, 1);
                sent.MessageMsgBox.setMovementMethod(LinkMovementMethod.getInstance());

                List<String> igLinks = getInstagramLinks(msg);
                if(igLinks.size()!=0){
                 sent.MessageBViewLink.setVisibility(View.VISIBLE);
                 sent.MessageBViewLink.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View view) {
                         sent.MessageBViewLink.setVisibility(View.GONE);
                         sent.MessageWvLinkView.setVisibility(View.VISIBLE);
                         openInstagramPostInWebView(igLinks.get(0),sent.MessageWvLinkView);
                     }
                 });
                }
//                setMessageAndTimeView(holder, sent.MessageTime, sent.MessageMsgBox);
            } else {
                sent.MessageMsgBox.setVisibility(View.GONE);
            }
            sent.MessageTime.setText(cursor.getString(cursor.getColumnIndexOrThrow("time")));
            sent.MessageTime.setPadding(25, 1, 25, 1);
            sent.MessageClMedia.setVisibility(View.GONE);
            sent.MessageCvDoc.setVisibility(View.GONE);
            if (active && cursor.getInt(cursor.getColumnIndexOrThrow("unread")) == 1 && !unreadFound) {
                sent.MessageLayout.addView(unreadDialog(), 0);
                unreadFound = true;
            }
            if (cursor.getInt(cursor.getColumnIndexOrThrow("media")) == 1) {
                Log.i("SENt::::", "MEDIA FOUND");
                sent.MessageLL.setPadding(20, 20, 20, 20);
                File file = UsefulFunctions.getFile(mContext, cursor.getString(cursor.getColumnIndexOrThrow("mediaID"))
                        , cursor.getInt(cursor.getColumnIndexOrThrow("mediaType"))
                        , cursor.getInt(cursor.getColumnIndexOrThrow("self")) == 1);

                Log.i("SENT::::", cursor.getString(cursor.getColumnIndexOrThrow("mediaID")));

                if (cursor.getInt(cursor.getColumnIndexOrThrow("mediaType")) == Constants.KEY_MESSAGE_MEDIA_TYPE_IMAGE) {
                    sent.MessageClMedia.setVisibility(View.VISIBLE);
                    sent.MessageIvImage.setVisibility(View.VISIBLE);
                    if (file.exists()) {
                        sent.MessageIvImage.setImageBitmap(BitmapFactory.decodeFile(file.getAbsolutePath()));
                        if (cursor.getInt(cursor.getColumnIndexOrThrow("status")) == -1) {
                            sent.MessageCvImageSize.setVisibility(View.VISIBLE);
                            sent.MessageTvImageSize.setVisibility(View.VISIBLE);
                            sent.MessageTvImageSize.setText(String.valueOf(cursor.getLong(cursor.getColumnIndexOrThrow("mediaSize"))) + " Kb ");
                            sent.MessageCvImageSize.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    cursor.moveToPosition(holder.getAdapterPosition());
                                    Log.i("CLICKED::::", view + "");
                                    Intent intent;
                                    Log.i("FIRESTORAGE :::::::", "STARTING");
                                    sent.MessageCvImageSize.setVisibility(View.GONE);
                                    sent.MessagePbProgressBarMedia.setVisibility(View.VISIBLE);
                                    sent.MessageIvMediaCancel.setVisibility(View.VISIBLE);
                                    Log.i("INTENT MSG ID:::::", cursor.getString(cursor.getColumnIndexOrThrow("msg_ID")));
                                    intent = new Intent(mContext.getApplicationContext(), UploadFileService.class);
                                    intent.putExtra(UploadFileService.EXTRA_RECEIVER, resultReceiver);

                                    intent.putExtra(Constants.KEY_INTENT_MESSAGE_ID, cursor.getString(cursor.getColumnIndexOrThrow("msg_ID")));
                                    mContext.startService(intent);
                                }
                            });
                        }
                    } else {
                        sent.MessageIvImage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                MediaMissingDialog();
                            }
                        });
                    }
                } else if (cursor.getInt(cursor.getColumnIndexOrThrow("mediaType")) == Constants.KEY_MESSAGE_MEDIA_TYPE_DOCUMENT) {
                    sent.MessageCvDoc.setVisibility(View.VISIBLE);
                    Log.i("Sent::::", "DOC FOUND");
                    if (file.exists()) {
                        sent.MessageTvDocName.setText(cursor.getString(cursor.getColumnIndexOrThrow("mediaID")));
                        sent.MessageTvDocSize.setText(String.valueOf(cursor.getLong(cursor.getColumnIndexOrThrow("mediaSize"))));
                        int l = cursor.getString(cursor.getColumnIndexOrThrow("mediaID")).lastIndexOf('.');
                        sent.MessageTvDocType.setText(cursor.getString(cursor.getColumnIndexOrThrow("mediaID")).substring(l + 1));
                        if (cursor.getInt(cursor.getColumnIndexOrThrow("status")) == -1) {
                            sent.MessageIvDocUpload.setVisibility(View.VISIBLE);
                            sent.MessageIvDocUpload.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    cursor.moveToPosition(holder.getAdapterPosition());
                                    Log.i("CLICKED::::", view + "");
                                    Intent intent;
                                    Log.i("FIRESTORAGE :::::::", "STARTING");
                                    sent.MessageIvDocUpload.setVisibility(View.GONE);
                                    sent.MessagePbDocDownloadProgress.setVisibility(View.VISIBLE);
                                    sent.MessageIvDocCancel.setVisibility(View.VISIBLE);
                                    Log.i("INTENT MSG ID:::::", cursor.getString(cursor.getColumnIndexOrThrow("msg_ID")));
                                    intent = new Intent(mContext.getApplicationContext(), UploadFileService.class);
                                    intent.putExtra(UploadFileService.EXTRA_RECEIVER, resultReceiver);
                                    intent.putExtra(Constants.KEY_INTENT_MESSAGE_ID, cursor.getString(cursor.getColumnIndexOrThrow("msg_ID")));
                                    mContext.startService(intent);
                                }
                            });
                        }
                    } else {

                    }
                } else if (cursor.getInt(cursor.getColumnIndexOrThrow("mediaType")) == Constants.KEY_MESSAGE_MEDIA_TYPE_VIDEO) {
                    if (file.exists()) {
                        sent.MessageClMedia.setVisibility(View.VISIBLE);
                        sent.MessageIvImage.setVisibility(View.VISIBLE);
                        sent.MessageIvImage.setImageBitmap(ThumbnailUtils.createVideoThumbnail(file.getAbsolutePath(), MediaStore.Video.Thumbnails.MINI_KIND));
                        sent.MessageIbPlayVid.setVisibility(View.VISIBLE);
                        sent.MessageIbPlayVid.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                //Video
                            }
                        });
                        if (cursor.getInt(cursor.getColumnIndexOrThrow("status")) == -1) {
                            sent.MessageCvVideoSize.setVisibility(View.VISIBLE);
                            sent.MessageTvVideoSize.setVisibility(View.VISIBLE);
                            sent.MessageTvVideoSize.setText(String.valueOf(cursor.getLong(cursor.getColumnIndexOrThrow("mediaSize"))) + " Kb ");
                            sent.MessageCvVideoSize.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    cursor.moveToPosition(holder.getAdapterPosition());
                                    Intent intent;
                                    Log.i("FIRESTORAGE :::::::", "STARTING");
                                    sent.MessageCvVideoSize.setVisibility(View.GONE);
                                    sent.MessagePbProgressBarMedia.setVisibility(View.VISIBLE);
                                    sent.MessageIvMediaCancel.setVisibility(View.VISIBLE);
                                    Log.i("INTENT MSG ID:::::", cursor.getString(cursor.getColumnIndexOrThrow("msg_ID")));
                                    intent = new Intent(mContext.getApplicationContext(), UploadFileService.class);
                                    intent.putExtra(UploadFileService.EXTRA_RECEIVER, resultReceiver);

                                    intent.putExtra(Constants.KEY_INTENT_MESSAGE_ID, cursor.getString(cursor.getColumnIndexOrThrow("msg_ID")));
                                    mContext.startService(intent);
                                }
                            });
                        }
                    } else {
                        sent.MessageIbPlayVid.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                MediaMissingDialog();
                            }
                        });
                    }

                }
            }
        } else {
            ReceivedMessageView viewHolder = (ReceivedMessageView) holder;
            MessageReceivedLayoutBinding received = viewHolder.receivedLayoutBinding;
            if (cursor.getString(cursor.getColumnIndexOrThrow("msg")) != null) {
                String msg=cursor.getString(cursor.getColumnIndexOrThrow("msg"));
                msg.replaceAll("(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]", "<a href='$0'>$0</a>")
                        .replaceAll("[^\\s]+@[^\\s]+", "<a href='mailto:$0'>$0</a>")
                        .replaceAll("(\\d{3}[-\\.\\s]??\\d{3}[-\\.\\s]??\\d{4}|\\(\\d{3}\\)\\s*\\d{3}[-\\.\\s]??\\d{4}|\\d{3}[-\\.\\s]??\\d{4})", "<a href='tel:$0'>$0</a>");
                received.MessageMsgBox.setText(msg);
                received.MessageMsgBox.setPadding(25, 1, 25, 1);
                received.MessageMsgBox.setMovementMethod(LinkMovementMethod.getInstance());
//                setMessageAndTimeView(holder, sent.MessageTime, sent.MessageMsgBox);
            } else {
                received.MessageMsgBox.setVisibility(View.GONE);
            }
            received.MessageMsgBox.setPadding(25, 1, 25, 1);
            received.MessageTime.setText(cursor.getString(cursor.getColumnIndexOrThrow("time")));
            received.MessageTime.setPadding(25, 1, 25, 1);
            if (active && cursor.getInt(cursor.getColumnIndexOrThrow("unread")) == 1 && !unreadFound) {
                received.MessageLayout.addView(unreadDialog(), 0);
                unreadFound = true;
            }
            if (cursor.getInt(cursor.getColumnIndexOrThrow("media")) == 1) {
                Log.i("RECEIVED::::", "MEDIA FOUND");
                File file = UsefulFunctions.getFile(mContext, cursor.getString(cursor.getColumnIndexOrThrow("mediaID"))
                        , cursor.getInt(cursor.getColumnIndexOrThrow("mediaType"))
                        , cursor.getInt(cursor.getColumnIndexOrThrow("self")) == 1);
                if (cursor.getInt(cursor.getColumnIndexOrThrow("mediaType")) == Constants.KEY_MESSAGE_MEDIA_TYPE_IMAGE) {
                    Log.i("MEDIA ID:::::", "fileID");

                    received.MessageClMedia.setVisibility(View.VISIBLE);
                    received.MessageIvImage.setVisibility(View.VISIBLE);
                    if (file.exists()) {
                        received.MessageIvImage.setImageBitmap(BitmapFactory.decodeFile(file.getAbsolutePath()));
                    } else {
                        received.MessageCvImageSize.setVisibility(View.VISIBLE);
                        received.MessageTvImageSize.setVisibility(View.VISIBLE);
                        received.MessageTvImageSize.setText(String.valueOf(cursor.getLong(cursor.getColumnIndexOrThrow("mediaType"))));
                        received.MessageCvImageSize.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                received.MessageIvMediaCancel.setVisibility(View.VISIBLE);
                                received.MessagePbProgressBarMedia.setVisibility(View.VISIBLE);
                                Log.i("CLICKED::::", view + "");
                                Intent intent;
                                Log.i("FIRESTORAGE :::::::", "STARTING");
                                cursor.moveToPosition(holder.getAdapterPosition());
                                intent = new Intent(mContext.getApplicationContext(), DownloadFileService.class);
                                intent.putExtra(UploadFileService.EXTRA_RECEIVER, resultReceiver);

                                intent.putExtra(Constants.KEY_INTENT_MESSAGE_ID, cursor.getString(cursor.getColumnIndexOrThrow("msg_ID")));
                                mContext.startService(intent);
                            }
                        });
                    }

                } else if (cursor.getInt(cursor.getColumnIndexOrThrow("mediaType")) == Constants.KEY_MESSAGE_MEDIA_TYPE_DOCUMENT) {
                    received.MessageCvDoc.setVisibility(View.VISIBLE);
                    if (file.exists()) {
                        received.MessageTvDocName.setText(cursor.getString(cursor.getColumnIndexOrThrow("mediaID")));
                        received.MessageTvImageSize.setText(String.valueOf(cursor.getLong(cursor.getColumnIndexOrThrow("mediaSize"))) + "KB");
                        int l = cursor.getString(cursor.getColumnIndexOrThrow("mediaID")).lastIndexOf('.');
                        received.MessageTvDocType.setText(cursor.getString(cursor.getColumnIndexOrThrow("mediaID")).substring(l + 1).toUpperCase());
                    } else {
                        received.MessageIvDocUpload.setVisibility(View.VISIBLE);
                        received.MessageIvDocUpload.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                received.MessageIvDocUpload.setVisibility(View.GONE);
                                received.MessagePbDocDownloadProgress.setVisibility(View.VISIBLE);
                                received.MessageIvDocCancel.setVisibility(View.VISIBLE);

                                Intent intent;
                                Log.i("FIRESTORAGE :::::::", "STARTING");
                                cursor.moveToPosition(holder.getAdapterPosition());
                                intent = new Intent(mContext.getApplicationContext(), DownloadFileService.class);
                                intent.putExtra(UploadFileService.EXTRA_RECEIVER, resultReceiver);

                                intent.putExtra(Constants.KEY_INTENT_MESSAGE_ID, cursor.getString(cursor.getColumnIndexOrThrow("msg_ID")));
                                mContext.startService(intent);
                            }
                        });
                    }
                } else if (cursor.getInt(cursor.getColumnIndexOrThrow("mediaType")) == Constants.KEY_MESSAGE_MEDIA_TYPE_VIDEO) {
                    received.MessageClMedia.setVisibility(View.VISIBLE);
                    received.MessageIvImage.setVisibility(View.VISIBLE);
                    if (file.exists()) {
                        Log.i("VIDEO::::::", file.getName());
                        received.MessageIvImage.setImageBitmap(ThumbnailUtils.createVideoThumbnail(file.getAbsolutePath(), MediaStore.Video.Thumbnails.MINI_KIND));
                        received.MessageIbPlayVid.setVisibility(View.VISIBLE);
                        received.MessageIbPlayVid.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                //Video
                            }
                        });
                    } else {
                        Log.i("VIDEO::::::", cursor.getString(cursor.getColumnIndexOrThrow("mediaID")));
                        received.MessageIbPlayVid.setVisibility(View.GONE);
                        received.MessageCvVideoSize.setVisibility(View.VISIBLE);
                        received.MessageTvVideoSize.setVisibility(View.VISIBLE);
                        received.MessageTvVideoSize.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent;
                                Log.i("FIRESTORAGE :::::::", "STARTING");
                                cursor.moveToPosition(holder.getAdapterPosition());
                                intent = new Intent(mContext.getApplicationContext(), DownloadFileService.class);
                                intent.putExtra(DownloadFileService.EXTRA_RECEIVER, resultReceiver);
                                intent.putExtra(Constants.KEY_INTENT_MESSAGE_ID, cursor.getString(cursor.getColumnIndexOrThrow("msg_ID")));
                                mContext.startService(intent);
                            }
                        });
                    }
                }
            }
        }

    }

    @Override
    public int getItemCount() {
        return cursor.getCount();
    }


    private ResultReceiver resultReceiver = new ResultReceiver(new Handler()) {
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            if (resultCode == DownloadFileService.RESULT_SUCCESS) {

            } else {
                // Download failed
            }
        }
    };
    public void openInstagramPostInWebView(String link, WebView webView) {
        // Use a regular expression to extract the post ID from the link
        String postId = getInstagramPostId(link);
        String postUrl = null;
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(postUrl);
        if(postId != null) {
             postUrl = "https://www.instagram.com/p/" + postId + "/";
            webView.getSettings().setJavaScriptEnabled(true);
            webView.loadUrl(postUrl);
        } else {
            Log.i("LINK:::::::", link+"___"+postId+"____"+postUrl);
            Toast.makeText(webView.getContext(), "Invalid Instagram post link", Toast.LENGTH_SHORT).show();
        }
    }
    public String getInstagramPostId(String url) {
        // Use a regular expression to match the post ID in the URL
        Pattern pattern = Pattern.compile("instagram://(?:[a-z]+)/(?:[0-9]+)/([0-9]+)");
        Matcher matcher = pattern.matcher(url);

        // Check if the URL matches the pattern
        if (matcher.find()) {
            // Return the post ID if the pattern matches
            return matcher.group(1);
        } else {
            // Return null if the pattern does not match
            return null;
        }
    }

    private void setMessageAndTimeView(RecyclerView.ViewHolder holder, TextView timeTv, TextView msgTv) {
        Log.i("TIME VIEW:::::", msgTv.getText().toString());
        ConstraintLayout constraintLayout = holder.itemView.findViewById(R.id.Message_cl);
        int msgLen = msgTv.getText().toString().length();
        int maxWid = msgTv.getMaxWidth();
        if (timeTv.getText().toString().length() + (msgLen) - 1 <= 23) {
            Log.i("TIME VIEW:::::", "<= 25");
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(constraintLayout);
            constraintSet.connect(timeTv.getId(), ConstraintSet.LEFT,msgTv.getId(), ConstraintSet.LEFT,  2);
            constraintSet.connect(timeTv.getId(), ConstraintSet.TOP,R.id.Message_cl,ConstraintSet.TOP,  5);
            constraintSet.applyTo(constraintLayout);
        } else if (timeTv.getText().toString().length() + (msgLen % 23) - 1 >= 20) {
            Log.i("TIME VIEW:::::", ">= 25");
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(constraintLayout);
            constraintSet.connect(timeTv.getId(), ConstraintSet.TOP, msgTv.getId(), ConstraintSet.BOTTOM, 2);
            constraintSet.applyTo(constraintLayout);
        }else {

        }
    }
    public List<String> getInstagramLinks(String text) {
        List<String> links = new ArrayList<>();
        // Regular expression to match Instagram links
        String pattern = "https?:\\/\\/(www\\.)?instagram\\.com\\/[A-Za-z0-9_](?:(?:[A-Za-z0-9_]|(?:\\.(?!\\.))){0,28}(?:[A-Za-z0-9_]))?";

        // Use the regular expression to find all matches in the text
        Pattern linkPattern = Pattern.compile(pattern);
        Matcher matcher = linkPattern.matcher(text);

        // Add each match to the list of links
        while (matcher.find()) {
            links.add(matcher.group());
        }

        return links;
    }

    private void MediaMissingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext.getApplicationContext());

        builder.setMessage("File is missing. Ask the user to resend the image");

        builder.setPositiveButton("Got it", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        }).setNegativeButton("", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
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

    public interface OnItemClickListener {
        void OnItemClick(String msgID, String fileName, CardView cvSize, TextView tvSize, ProgressBar progressBar, ImageView ivCancel, View view, boolean upload);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnListItemClickListener = listener;
    }

    public void setOnItemCLickListener(OnItemClickListener listener) {
        this.listener = listener;
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