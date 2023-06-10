package com.akw.crimson.Backend.Adapters;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.ResultReceiver;
import android.provider.MediaStore;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.akw.crimson.Backend.Communications.Communicator;
import com.akw.crimson.Backend.Communications.DownloadFileService;
import com.akw.crimson.Backend.Communications.UploadFileService;
import com.akw.crimson.Backend.Constants;
import com.akw.crimson.Backend.Database.TheViewModel;
import com.akw.crimson.Backend.UsefulFunctions;
import com.akw.crimson.Chat.Chat_Fragment_MediaView;
import com.akw.crimson.R;
import com.akw.crimson.databinding.MessageReceivedLayoutBinding;
import com.akw.crimson.databinding.MessageSentLayoutBinding;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ChatView_RecyclerAdapter extends RecyclerView.Adapter {

    MediaPlayer audioMediaPlayer;
    Cursor cursor;
    ImageButton ib_currAudioButton;
    Context mContext;
    Handler handler;
    TheViewModel dbView;
    boolean active, isAudioPlaying = false, unreadFound = false;
    int mediaPosition = 0, lastPlayedPosition = -1;
    private OnItemClickListener mOnListItemClickListener;
    private OnItemClickListener listener;
    private final ResultReceiver resultReceiver = new ResultReceiver(new Handler()) {
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            if (resultCode == DownloadFileService.RESULT_SUCCESS) {

            } else {
                // Download failed
            }
        }
    };

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        if (audioMediaPlayer != null) {
            audioMediaPlayer.stop();
            audioMediaPlayer.release();
            audioMediaPlayer = null;
        }
    }

    public ChatView_RecyclerAdapter(Context context, Cursor c, OnItemClickListener onImageClickListener, TheViewModel db, boolean active) {
        mOnListItemClickListener = onImageClickListener;
        mContext = context;
        dbView = db;
        this.active = active;
        this.cursor = c;
        this.handler = new Handler();
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

    private void setMsg(Cursor cursor, TextView msgBox, TextView time, boolean sent) {
        String t;
        if (sent) {
            t = cursor.getString(cursor.getColumnIndexOrThrow("sentTime"));
        } else {
            t = cursor.getString(cursor.getColumnIndexOrThrow("receivedTime"));
        }
        time.setText(UsefulFunctions.getTimeHhMm(t));
        time.setPadding(25, 1, 25, 1);


        if (cursor.getString(cursor.getColumnIndexOrThrow("msg")) != null) {
            String msg = cursor.getString(cursor.getColumnIndexOrThrow("msg"));
            msg.replaceAll("(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]", "<a href='$0'>$0</a>")
                    .replaceAll("[^\\s]+@[^\\s]+", "<a href='mailto:$0'>$0</a>")
                    .replaceAll("(\\d{3}[-\\.\\s]??\\d{3}[-\\.\\s]??\\d{4}|\\(\\d{3}\\)\\s*\\d{3}[-\\.\\s]??\\d{4}|\\d{3}[-\\.\\s]??\\d{4})", "<a href='tel:$0'>$0</a>");
            msgBox.setText(msg);
            msgBox.setPadding(25, 1, 25, 1);
            msgBox.setMovementMethod(LinkMovementMethod.getInstance());


        } else {
            msgBox.setVisibility(View.GONE);
        }
        msgBox.setPadding(25, 1, 25, 1);
    }

    private void setImage(boolean sent, int adapterPosition, File file, int mediaPos, ConstraintLayout messageClMedia, ImageView messageIvImage, CardView messageCvImageSize, ProgressBar messagePbProgressBarMedia, ImageView messageIvMediaCancel, TextView messageTvImageSize) {

        messageClMedia.setVisibility(View.VISIBLE);
        messageIvImage.setVisibility(View.VISIBLE);
        if (file.exists()) {
            messageIvImage.setImageBitmap(BitmapFactory.decodeFile(file.getAbsolutePath()));
            messageIvImage.setOnClickListener(view -> {
                openMedia(mediaPos, view.getContext());
            });

            if (this.cursor.getInt(this.cursor.getColumnIndexOrThrow("status")) == Constants.Message.MESSAGE_STATUS_BULK_MEDIA_UPLOAD_PENDING) {
                if (Communicator.uploading.contains(this.cursor.getString(this.cursor.getColumnIndexOrThrow("msg_ID")))) {
                    messageCvImageSize.setVisibility(View.GONE);
                    messagePbProgressBarMedia.setVisibility(View.VISIBLE);
                    messageIvMediaCancel.setVisibility(View.VISIBLE);
                } else {
                    messageCvImageSize.setVisibility(View.VISIBLE);
                    messageTvImageSize.setVisibility(View.VISIBLE);
                    String s = this.cursor.getLong(this.cursor.getColumnIndexOrThrow("mediaSize")) + " Kb ";
                    messageTvImageSize.setText(s);
                    messageCvImageSize.setOnClickListener(view -> {
                        this.cursor.moveToPosition(adapterPosition);
                        Intent intent;
                        messageCvImageSize.setVisibility(View.GONE);
                        messagePbProgressBarMedia.setVisibility(View.VISIBLE);
                        messageIvMediaCancel.setVisibility(View.VISIBLE);
                        Log.i("INTENT MSG ID:::::", this.cursor.getString(this.cursor.getColumnIndexOrThrow("msg_ID")));
                        intent = new Intent(mContext.getApplicationContext(), UploadFileService.class);
                        intent.putExtra(UploadFileService.EXTRA_RECEIVER, resultReceiver);
                        Messenger messenger = new Messenger(new Handler() {
                            @Override
                            public void handleMessage(Message msg) {
                                // Update progress bar
                                int progress = msg.arg1;
                                messagePbProgressBarMedia.setProgress(progress);
                            }
                        });
                        intent.putExtra(Constants.Intent.KEY_INTENT_MESSENGER, messenger);
                        intent.putExtra(Constants.Intent.KEY_INTENT_MESSAGE_ID, this.cursor.getString(this.cursor.getColumnIndexOrThrow("msg_ID")));
                        mContext.startService(intent);
                    });

                }
            }
            messageIvImage.setOnClickListener(view -> {
                openMedia(mediaPos, view.getContext());
            });
        } else {
            if (sent) {
                messageIvImage.setOnClickListener(view -> MediaMissingDialog());
            } else {
                messageCvImageSize.setVisibility(View.VISIBLE);
                messageTvImageSize.setVisibility(View.VISIBLE);
                messageTvImageSize.setText(String.valueOf(cursor.getLong(cursor.getColumnIndexOrThrow("mediaSize"))));
                messageCvImageSize.setOnClickListener(view -> {
                    Intent intent;
                    messageIvMediaCancel.setVisibility(View.VISIBLE);
                    messagePbProgressBarMedia.setVisibility(View.VISIBLE);
                    Log.i("CLICKED::::", view + "");
                    this.cursor.moveToPosition(adapterPosition);
                    intent = new Intent(mContext.getApplicationContext(), DownloadFileService.class);
                    intent.putExtra(UploadFileService.EXTRA_RECEIVER, resultReceiver);
                    intent.putExtra(Constants.Intent.KEY_INTENT_MESSAGE_ID, cursor.getString(cursor.getColumnIndexOrThrow("msg_ID")));
                    mContext.startService(intent);
                });
            }
        }
    }

    private void setDocument(boolean sent, File file, int adapterPosition, CardView messageCvDoc, TextView messageTvDocName, TextView messageTvImageSize, TextView messageTvDocType, ImageView messageIvDocUpload, ProgressBar messagePbDocTransferProgress, ImageView messageIvDocCancel) {
        Log.i("Adapter.setDocument::::::", "setting Docs");
        messageCvDoc.setVisibility(View.VISIBLE);
        messageTvDocName.setText(cursor.getString(cursor.getColumnIndexOrThrow("mediaID")));
        messageTvImageSize.setText(UsefulFunctions.getSizeValue(cursor.getLong(cursor.getColumnIndexOrThrow("mediaSize"))));
        int l = cursor.getString(cursor.getColumnIndexOrThrow("mediaID")).lastIndexOf('.');
        messageTvDocType.setText(cursor.getString(cursor.getColumnIndexOrThrow("mediaID")).substring(l + 1).toUpperCase());
        if (file.exists()) {
            Log.i("Adapter.setDocument::::::", "Doc Found");
            messageCvDoc.setOnClickListener(view -> {

                Uri fileUri = FileProvider.getUriForFile(mContext, "com.akw.crimson.fileprovider", file);

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(fileUri);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

//                List<ResolveInfo> resInfoList = mContext.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
//                if (!resInfoList.isEmpty()) {
//                    for (ResolveInfo resolveInfo : resInfoList) {
//                        String packageName = resolveInfo.activityInfo.packageName;
                mContext.grantUriPermission("com.akw.crimson", fileUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                    }
                mContext.startActivity(intent);
//                } else {
//                    // Handle case when no suitable app is available to open the file
//                    Toast.makeText(mContext, "No app available to open the file", Toast.LENGTH_SHORT).show();
//                }
            });
            if (cursor.getInt(cursor.getColumnIndexOrThrow("status")) == -1) {
                Log.i("Adapter.setDocument::::::", "Doc Pending Upload");
                if (Communicator.uploading.contains(cursor.getString(cursor.getColumnIndexOrThrow("msg_ID")))) {
                    messageIvDocUpload.setVisibility(View.GONE);
                    messagePbDocTransferProgress.setVisibility(View.VISIBLE);
                    messageIvDocCancel.setVisibility(View.VISIBLE);
                } else {
                    messageIvDocUpload.setVisibility(View.VISIBLE);
                    messageIvDocUpload.setOnClickListener(view -> {
                        //Start Download
                        Log.i("Adapter.setDocument::::::", "Upload Clicked");
                        cursor.moveToPosition(adapterPosition);
                        Intent intent;
                        messageIvDocUpload.setVisibility(View.GONE);
                        messagePbDocTransferProgress.setVisibility(View.VISIBLE);
                        messageIvDocCancel.setVisibility(View.VISIBLE);
                        intent = new Intent(mContext.getApplicationContext(), UploadFileService.class);
                        intent.putExtra(UploadFileService.EXTRA_RECEIVER, resultReceiver);
                        Messenger messenger = new Messenger(new Handler() {
                            @Override
                            public void handleMessage(Message msg) {
                                // Update progress bar
                                int progress = msg.arg1;
                                messagePbDocTransferProgress.setProgress(progress);
                            }
                        });
                        intent.putExtra(Constants.Intent.KEY_INTENT_MESSENGER, messenger);
                        intent.putExtra(Constants.Intent.KEY_INTENT_MESSAGE_ID, cursor.getString(cursor.getColumnIndexOrThrow("msg_ID")));
                        mContext.startService(intent);
                    });
                }
            }


        } else {
            messageIvDocUpload.setVisibility(View.VISIBLE);
            Log.i("Adapter.setDocument::::::", "Doc Missing");

            if (sent) {
                //Doc Missing
            } else {

                Log.i("Adapter.setDocument::::::", "Doc Download");
                messageIvDocUpload.setVisibility(View.VISIBLE);
                messageIvDocUpload.setOnClickListener(view -> {
                    messageIvDocUpload.setVisibility(View.GONE);
                    messagePbDocTransferProgress.setVisibility(View.VISIBLE);
                    messageIvDocCancel.setVisibility(View.VISIBLE);
                    cursor.moveToPosition(adapterPosition);
                    Intent intent;
                    intent = new Intent(mContext.getApplicationContext(), DownloadFileService.class);
                    intent.putExtra(UploadFileService.EXTRA_RECEIVER, resultReceiver);
                    intent.putExtra(Constants.Intent.KEY_INTENT_MESSAGE_ID, cursor.getString(cursor.getColumnIndexOrThrow("msg_ID")));

                    mContext.startService(intent);
                });

            }
        }
    }

    private void setAudio(boolean sent, int position, File file, LinearLayout messageMediaClLlAudio, TextView messageMediaAudioTvAudioName, ImageView messageMediaAudioIvIcon, TextView messageMediaAudioTvAudioLength, ImageButton messageMediaAudioIbPlayPause, SeekBar messageMediaAudioSbProgress) {
        messageMediaClLlAudio.setVisibility(View.VISIBLE);
        Log.i("Adapter.setAudio::::::", "Setting Audio ");
        if (file.exists()) {
            messageMediaAudioTvAudioName.setVisibility(View.VISIBLE);
            messageMediaAudioTvAudioName.setText(cursor.getString(cursor.getColumnIndexOrThrow("mediaID")));
            messageMediaAudioIvIcon.setImageResource(R.drawable.ic_baseline_headphones_24);
            messageMediaAudioTvAudioLength.setText(UsefulFunctions.getAudioLength(file.getPath()));
            messageMediaAudioIbPlayPause.setOnClickListener(view -> {
                messageMediaAudioIbPlayPause.setImageResource(R.drawable.ic_baseline_pause_24);
                if (ib_currAudioButton == null)
                    ib_currAudioButton = messageMediaAudioIbPlayPause;
                if (audioMediaPlayer != null && audioMediaPlayer.isPlaying()) {

                    if (messageMediaAudioIbPlayPause == ib_currAudioButton) {
                        pauseMedia();
                        messageMediaAudioIbPlayPause.setImageResource(R.drawable.ic_baseline_audio_play_24);
                        return;
                    } else {
                        stopMedia();
                        audioMediaPlayer = null;
                        ib_currAudioButton.setImageResource(R.drawable.ic_baseline_audio_play_24);
                        ib_currAudioButton = messageMediaAudioIbPlayPause;
                    }
                }

                if (audioMediaPlayer == null) {
                    Log.i("AUDIO BUTTON CLICK ::::::", "1_");
                    audioMediaPlayer = MediaPlayer.create(mContext, Uri.parse(file.getPath()));
                    audioMediaPlayer.setOnCompletionListener(mp -> {
                        stopMedia();
                        ib_currAudioButton.setImageResource(R.drawable.ic_baseline_audio_play_24);
                        messageMediaAudioSbProgress.setProgress(0);
                    });
                }

                if (messageMediaAudioSbProgress.getProgress() != 0)
                    audioMediaPlayer.seekTo(messageMediaAudioSbProgress.getProgress());
                audioMediaPlayer.start();
                messageMediaAudioIbPlayPause.setImageResource(R.drawable.ic_baseline_pause_24);

                // update seekbar
                messageMediaAudioSbProgress.setMax(audioMediaPlayer.getDuration());
                messageMediaAudioSbProgress.setProgress(audioMediaPlayer.getCurrentPosition());

                int p = position;
                lastPlayedPosition = p;

                // update seekbar progress on UI thread
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    int prevAud = audioMediaPlayer.getAudioSessionId();

                    @Override
                    public void run() {
                        if (audioMediaPlayer != null && audioMediaPlayer.getAudioSessionId() != prevAud) {
                            return;
                        }
                        if (audioMediaPlayer != null && audioMediaPlayer.isPlaying()) {
                            messageMediaAudioSbProgress.setProgress(audioMediaPlayer.getCurrentPosition());
                            new Handler(Looper.getMainLooper()).postDelayed(this, 1000);
                        } else {
                            return;
                        }
                        prevAud = audioMediaPlayer.getAudioSessionId();
                    }
                }, 1000);
            });


            messageMediaAudioSbProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    messageMediaAudioTvAudioLength.setText(UsefulFunctions.getStringMmSsTimeVale(audioMediaPlayer.getCurrentPosition()));
                    if (fromUser && audioMediaPlayer != null) {
                        audioMediaPlayer.seekTo(progress);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });

            if (cursor.getInt(cursor.getColumnIndexOrThrow("status")) == -1) {
                Log.i("AUDIO TEMP::::::", "FOUND");
                messageMediaAudioIvIcon.setImageResource(R.drawable.ic_baseline_upload_24);
                messageMediaAudioIvIcon.setOnClickListener(view -> {
                    messageMediaAudioIvIcon.setImageResource(R.drawable.ic_outline_cancel_24);
                    Intent intent;
                    cursor.moveToPosition(position);

                    intent = new Intent(mContext.getApplicationContext(), UploadFileService.class);
                    intent.putExtra(UploadFileService.EXTRA_RECEIVER, resultReceiver);
                    Messenger messenger = new Messenger(new Handler() {
                        @Override
                        public void handleMessage(Message msg) {
                            // Update progress bar
                            int progress = msg.arg1;
                            messageMediaAudioSbProgress.setProgress(progress);
                        }
                    });
                    intent.putExtra(Constants.Intent.KEY_INTENT_MESSENGER, messenger);
                    intent.putExtra(Constants.Intent.KEY_INTENT_MESSAGE_ID, cursor.getString(cursor.getColumnIndexOrThrow("msg_ID")));
                    mContext.startService(intent);
                });
                messageMediaAudioTvAudioLength.setText(UsefulFunctions.getSizeValue(cursor.getLong(cursor.getColumnIndexOrThrow("mediaSize"))));

            }
        } else {
            if (sent) {

            } else {
                messageMediaAudioIvIcon.setImageResource(R.drawable.ic_baseline_download_24);
                messageMediaAudioIbPlayPause.setOnClickListener(view -> {
                    messageMediaAudioIbPlayPause.setImageResource(R.drawable.ic_outline_cancel_24);
                    Intent intent;
                    cursor.moveToPosition(position);
                    intent = new Intent(mContext.getApplicationContext(), DownloadFileService.class);
                    intent.putExtra(DownloadFileService.EXTRA_RECEIVER, resultReceiver);
                    intent.putExtra(Constants.Intent.KEY_INTENT_MESSAGE_ID, cursor.getString(cursor.getColumnIndexOrThrow("msg_ID")));
                    mContext.startService(intent);
                });
                messageMediaAudioTvAudioLength.setText(UsefulFunctions.getSizeValue(cursor.getLong(cursor.getColumnIndexOrThrow("mediaSize"))));

            }
        }
    }


    private void setVideo(boolean sent, File file, int adapterPosition, int mediaPos, ConstraintLayout messageClMedia, ImageView messageIvImage, ImageButton messageIbPlayVid, CardView messageCvVideoSize, ProgressBar messagePbProgressBarMedia, ImageView messageIvMediaCancel, TextView messageTvVideoSize) {
        messageClMedia.setVisibility(View.VISIBLE);
        messageIvImage.setVisibility(View.VISIBLE);
        messageIbPlayVid.setVisibility(View.VISIBLE);
        if (file.exists()) {
            messageIvImage.setImageBitmap(ThumbnailUtils.createVideoThumbnail(file.getAbsolutePath(), MediaStore.Video.Thumbnails.MINI_KIND));
            messageIbPlayVid.setOnClickListener(view -> {
                openMedia(mediaPos, view.getContext());
            });
            messageIvImage.setOnClickListener(view -> {
                //Video
                openMedia(mediaPos, view.getContext());

            });
            if (cursor.getInt(cursor.getColumnIndexOrThrow("status")) == -1) {
                if (Communicator.uploading.contains(cursor.getString(cursor.getColumnIndexOrThrow("msg_ID")))) {
                    messageCvVideoSize.setVisibility(View.GONE);
                    messagePbProgressBarMedia.setVisibility(View.VISIBLE);
                    messageIvMediaCancel.setVisibility(View.VISIBLE);
                } else {
                    messageCvVideoSize.setVisibility(View.VISIBLE);
                    messageTvVideoSize.setVisibility(View.VISIBLE);
                    messageTvVideoSize.setText(cursor.getLong(cursor.getColumnIndexOrThrow("mediaSize")) + " Kb ");
                    messageCvVideoSize.setOnClickListener(view -> {
                        cursor.moveToPosition(adapterPosition);
                        Intent intent;
                        messageCvVideoSize.setVisibility(View.GONE);
                        messagePbProgressBarMedia.setVisibility(View.VISIBLE);
                        messageIvMediaCancel.setVisibility(View.VISIBLE);
                        Log.i("INTENT MSG ID:::::", cursor.getString(cursor.getColumnIndexOrThrow("msg_ID")));
                        intent = new Intent(mContext.getApplicationContext(), UploadFileService.class);
                        intent.putExtra(UploadFileService.EXTRA_RECEIVER, resultReceiver);
                        Messenger messenger = new Messenger(new Handler() {
                            @Override
                            public void handleMessage(Message msg) {
                                // Update progress bar
                                int progress = msg.arg1;
                                messagePbProgressBarMedia.setProgress(progress);
                            }
                        });
                        intent.putExtra(Constants.Intent.KEY_INTENT_MESSENGER, messenger);
                        intent.putExtra(Constants.Intent.KEY_INTENT_MESSAGE_ID, cursor.getString(cursor.getColumnIndexOrThrow("msg_ID")));
                        mContext.startService(intent);
                    });
                }
            }
        } else {
            if (sent) {
                messageIbPlayVid.setOnClickListener(view -> MediaMissingDialog());
            } else {
                messageCvVideoSize.setVisibility(View.VISIBLE);
                messageTvVideoSize.setOnClickListener(view -> {
                    Intent intent;
                    cursor.moveToPosition(adapterPosition);
                    intent = new Intent(mContext.getApplicationContext(), DownloadFileService.class);
                    intent.putExtra(DownloadFileService.EXTRA_RECEIVER, resultReceiver);
                    intent.putExtra(Constants.Intent.KEY_INTENT_MESSAGE_ID, cursor.getString(cursor.getColumnIndexOrThrow("msg_ID")));
                    mContext.startService(intent);
                });
                messageIvImage.setOnClickListener(view -> {
                    Intent intent;
                    cursor.moveToPosition(adapterPosition);
                    intent = new Intent(mContext.getApplicationContext(), DownloadFileService.class);
                    intent.putExtra(DownloadFileService.EXTRA_RECEIVER, resultReceiver);
                    intent.putExtra(Constants.Intent.KEY_INTENT_MESSAGE_ID, cursor.getString(cursor.getColumnIndexOrThrow("msg_ID")));
                    mContext.startService(intent);
                });

            }
        }

    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        cursor.moveToPosition(position);
        if (holder instanceof SentMessageView) {
            SentMessageView viewHolder = (SentMessageView) holder;
            MessageSentLayoutBinding sent = viewHolder.sentMsgBinding;
            setMsg(cursor, sent.MessageMsgBox, sent.MessageTime, true);


            if (active && cursor.getInt(cursor.getColumnIndexOrThrow("unread")) == 1 && !unreadFound) {
                sent.MessageLayout.addView(unreadDialog(), 0);
                unreadFound = true;
            }
            if (cursor.getInt(cursor.getColumnIndexOrThrow("media")) == 1) {
                Log.i("SENt::::", "MEDIA FOUND");
                sent.MessageLLMedia.setVisibility(View.VISIBLE);
                sent.MessageClMedia.setVisibility(View.VISIBLE);
                sent.MessageLLMedia.setPadding(20, 20, 20, 20);
                File file = UsefulFunctions.FileUtil.getFile(mContext, cursor.getString(cursor.getColumnIndexOrThrow("mediaID"))
                        , cursor.getInt(cursor.getColumnIndexOrThrow("mediaType"))
                        , cursor.getInt(cursor.getColumnIndexOrThrow("self")) == 1);

                Log.i(".Backend.Adapters SENT::::", cursor.getString(cursor.getColumnIndexOrThrow("mediaID")));

                if (cursor.getInt(cursor.getColumnIndexOrThrow("mediaType")) == Constants.Media.KEY_MESSAGE_MEDIA_TYPE_IMAGE) {
                    viewHolder.mediaPos = mediaPosition++;

                    setImage(true, holder.getAdapterPosition(), file, viewHolder.mediaPos, sent.MessageClMedia, sent.MessageIvImage, sent.MessageCvImageSize, sent.MessagePbProgressBarMedia, sent.MessageIvMediaCancel, sent.MessageTvImageSize);

                } else if (cursor.getInt(cursor.getColumnIndexOrThrow("mediaType")) == Constants.Media.KEY_MESSAGE_MEDIA_TYPE_DOCUMENT) {

                    setDocument(true, file, holder.getAdapterPosition(), sent.MessageCvDoc, sent.MessageTvDocName, sent.MessageTvDocSize, sent.MessageTvDocType
                            , sent.MessageIvDocUpload, sent.MessagePbDocDownloadProgress, sent.MessageIvDocCancel);


                } else if (cursor.getInt(cursor.getColumnIndexOrThrow("mediaType")) == Constants.Media.KEY_MESSAGE_MEDIA_TYPE_VIDEO) {
                    viewHolder.mediaPos = mediaPosition++;

                    setVideo(true, file, holder.getAdapterPosition(), viewHolder.mediaPos, sent.MessageClMedia, sent.MessageIvImage, sent.MessageIbPlayVid, sent.MessageCvVideoSize, sent.MessagePbProgressBarMedia, sent.MessageIvMediaCancel, sent.MessageTvVideoSize);

                } else if (cursor.getInt(cursor.getColumnIndexOrThrow("mediaType")) == Constants.Media.KEY_MESSAGE_MEDIA_TYPE_AUDIO) {

                    setAudio(true, holder.getAdapterPosition(), file, sent.MessageMediaClLlAudio, sent.MessageMediaAudioTvAudioName, sent.MessageMediaAudioIvIcon, sent.MessageMediaAudioTvAudioLength
                            , sent.MessageMediaAudioIbPlayPause, sent.MessageMediaAudioSbProgress);

                }
            }
        } else {
            ReceivedMessageView viewHolder = (ReceivedMessageView) holder;
            MessageReceivedLayoutBinding received = viewHolder.receivedLayoutBinding;
            setMsg(cursor, received.MessageMsgBox, received.MessageTime, false);

            if (active && cursor.getInt(cursor.getColumnIndexOrThrow("unread")) == 1 && !unreadFound) {
                received.MessageLayout.addView(unreadDialog(), 0);
                unreadFound = true;
            }

            //Message Media Handling

            if (cursor.getInt(cursor.getColumnIndexOrThrow("media")) == 1) {
                Log.i("RECEIVED::::", "MEDIA FOUND");
                File file = UsefulFunctions.FileUtil.getFile(mContext, cursor.getString(cursor.getColumnIndexOrThrow("mediaID"))
                        , cursor.getInt(cursor.getColumnIndexOrThrow("mediaType"))
                        , cursor.getInt(cursor.getColumnIndexOrThrow("self")) == 1);
                if (cursor.getInt(cursor.getColumnIndexOrThrow("mediaType")) == Constants.Media.KEY_MESSAGE_MEDIA_TYPE_IMAGE) {
                    viewHolder.mediaPos = mediaPosition++;
                    setImage(false, holder.getAdapterPosition(), file, viewHolder.mediaPos, received.MessageClMedia
                            , received.MessageIvImage, received.MessageCvImageSize, received.MessagePbProgressBarMedia
                            , received.MessageIvMediaCancel, received.MessageTvImageSize);

                } else if (cursor.getInt(cursor.getColumnIndexOrThrow("mediaType")) == Constants.Media.KEY_MESSAGE_MEDIA_TYPE_DOCUMENT) {
                    setDocument(false, file, holder.getAdapterPosition(), received.MessageCvDoc, received.MessageTvDocName, received.MessageTvDocSize, received.MessageTvDocType
                            , received.MessageIvDocUpload, received.MessagePbDocDownloadProgress, received.MessageIvDocCancel);

                } else if (cursor.getInt(cursor.getColumnIndexOrThrow("mediaType")) == Constants.Media.KEY_MESSAGE_MEDIA_TYPE_VIDEO) {
                    viewHolder.mediaPos = mediaPosition++;
                    setVideo(true, file, holder.getAdapterPosition(), viewHolder.mediaPos, received.MessageClMedia, received.MessageIvImage, received.MessageIbPlayVid, received.MessageCvVideoSize, received.MessagePbProgressBarMedia, received.MessageIvMediaCancel, received.MessageTvVideoSize);

                } else if (cursor.getInt(cursor.getColumnIndexOrThrow("mediaType")) == Constants.Media.KEY_MESSAGE_MEDIA_TYPE_AUDIO) {
                    setAudio(false, holder.getAdapterPosition(), file, received.MessageMediaClLlAudio, received.MessageMediaAudioTvAudioName, received.MessageMediaAudioIvIcon, received.MessageMediaAudioTvAudioLength
                            , received.MessageMediaAudioIbPlayPause, received.MessageMediaAudioSbProgress);

                }
            }
        }
    }


    @Override
    public int getItemCount() {
        return cursor.getCount();
    }


    private void startAudioMediaPlayer(String filePath, SeekBar seekBar) {
        Log.i("ChatView_Adapter startAudioMediaPlayer:::::::", "");
        try {
            audioMediaPlayer = new MediaPlayer();
            audioMediaPlayer.setDataSource(filePath);
            audioMediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        audioMediaPlayer.start();
        isAudioPlaying = true;
        if (ib_currAudioButton != null)
            ib_currAudioButton.setImageResource(R.drawable.ic_baseline_pause_24);
        audioMediaPlayer.setOnCompletionListener(audioMediaPlayer -> stopAudioMediaPlayer(seekBar));
        // Update progress bar
        updateAudioProgressBar();
    }

    private void stopAudioMediaPlayer(SeekBar seekBar) {
        audioMediaPlayer.pause();
        audioMediaPlayer.seekTo(0);
        isAudioPlaying = false;
        ib_currAudioButton.setImageResource(R.drawable.ic_baseline_audio_play_24);
        seekBar.setProgress(0);
        audioMediaPlayer.release();
    }

    private void audioTogglePlayPause() {
        if (isAudioPlaying) {
            audioMediaPlayer.pause();
            isAudioPlaying = false;
        } else {
            audioMediaPlayer.start();
            isAudioPlaying = true;
        }
    }


    private void openMedia(int pos, Context cxt) {
        Chat_Fragment_MediaView myFragment = new Chat_Fragment_MediaView();
        Bundle bun = new Bundle();
        bun.putInt(Constants.Intent.KEY_INTENT_LIST_POSITION, pos);
        bun.putInt(Constants.Intent.KEY_INTENT_USER_TYPE, Constants.User.USER_TYPE_USER);
        myFragment.setArguments(bun);
        FragmentTransaction transaction = ((FragmentActivity) cxt).getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.chat_frameLayout_media, myFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void pauseMedia() {
        audioMediaPlayer.pause();
    }

    private void stopMedia() {
        audioMediaPlayer.stop();
        audioMediaPlayer.reset();
        audioMediaPlayer.release();
        audioMediaPlayer = null;
    }

    private void updateAudioProgressBar() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (audioMediaPlayer != null && isAudioPlaying) {
                    int progress = audioMediaPlayer.getCurrentPosition();
                    // currPb.setProgress(progress);
                    handler.postDelayed(this, 100);
                }
            }
        }, 100);
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

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnListItemClickListener = listener;
    }

    public void setOnItemCLickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void OnItemClick(String msgID, String fileName, CardView cvSize, TextView tvSize, ProgressBar progressBar, ImageView ivCancel, View view, boolean upload);
    }

    class SentMessageView extends RecyclerView.ViewHolder {
        MessageSentLayoutBinding sentMsgBinding;
        int mediaPos;

        public SentMessageView(@NonNull MessageSentLayoutBinding binding) {
            super(binding.getRoot());
            this.sentMsgBinding = binding;
        }
    }

    class ReceivedMessageView extends RecyclerView.ViewHolder {
        MessageReceivedLayoutBinding receivedLayoutBinding;
        int mediaPos;

        public ReceivedMessageView(@NonNull MessageReceivedLayoutBinding binding) {
            super(binding.getRoot());
            this.receivedLayoutBinding = binding;
        }

    }

    public void openInstagramPostInWebView(String link, WebView webView) {
        // Use a regular expression to extract the post ID from the link
        String postId = getInstagramPostId(link);
        String postUrl = null;
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(postUrl);
        if (postId != null) {
            postUrl = "https://www.instagram.com/p/" + postId + "/";
            webView.getSettings().setJavaScriptEnabled(true);
            webView.loadUrl(postUrl);
        } else {
            Log.i("LINK:::::::", link + "___" + postId + "____" + postUrl);
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


}