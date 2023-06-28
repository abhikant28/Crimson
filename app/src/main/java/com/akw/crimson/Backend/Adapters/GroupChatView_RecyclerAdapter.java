package com.akw.crimson.Backend.Adapters;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
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
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
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


public class GroupChatView_RecyclerAdapter extends RecyclerView.Adapter {

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

    public GroupChatView_RecyclerAdapter(Context context, Cursor c, OnItemClickListener onImageClickListener, TheViewModel db, boolean active) {
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

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        cursor.moveToPosition(position);
        if (holder instanceof SentMessageView) {
            SentMessageView viewHolder = (SentMessageView) holder;
            MessageSentLayoutBinding sent = viewHolder.sentMsgBinding;
            if (cursor.getString(cursor.getColumnIndexOrThrow("msg")) != null) {
                String msg = cursor.getString(cursor.getColumnIndexOrThrow("msg"));
                msg.replaceAll("(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]", "<a href='$0'>$0</a>")
                        .replaceAll("[^\\s]+@[^\\s]+", "<a href='mailto:$0'>$0</a>")
                        .replaceAll("(\\d{3}[-\\.\\s]??\\d{3}[-\\.\\s]??\\d{4}|\\(\\d{3}\\)\\s*\\d{3}[-\\.\\s]??\\d{4}|\\d{3}[-\\.\\s]??\\d{4})", "<a href='tel:$0'>$0</a>");
                sent.MessageMsgBox.setText(msg);
                sent.MessageMsgBox.setPadding(25, 1, 25, 1);
                sent.MessageMsgBox.setMovementMethod(LinkMovementMethod.getInstance());

                List<String> igLinks = getInstagramLinks(msg);
                if (igLinks.size() != 0) {
                    sent.MessageBViewLink.setVisibility(View.VISIBLE);
                    sent.MessageBViewLink.setOnClickListener(view -> {
                        sent.MessageBViewLink.setVisibility(View.GONE);
                        sent.MessageWvLinkView.setVisibility(View.VISIBLE);
                        openInstagramPostInWebView(igLinks.get(0), sent.MessageWvLinkView);
                    });
                }
            } else {
                sent.MessageMsgBox.setVisibility(View.GONE);
            }
            String t=cursor.getString(cursor.getColumnIndexOrThrow("sentTime"));
            sent.MessageTime.setText(UsefulFunctions.getTimeHhMm(t));
            sent.MessageTime.setPadding(25, 1, 25, 1);
            sent.MessageClMedia.setVisibility(View.GONE);
            sent.MessageCvDoc.setVisibility(View.GONE);
            if (active && cursor.getInt(cursor.getColumnIndexOrThrow("unreadUser")) == 1 && !unreadFound) {
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
                    if (file.exists()) {
                        if (cursor.getInt(cursor.getColumnIndexOrThrow("status")) == -1) {
                            if (Communicator.mediaUploading.contains(cursor.getString(cursor.getColumnIndexOrThrow("msg_ID")))) {
                                sent.MessageCvImageSize.setVisibility(View.GONE);
                                sent.MessagePbProgressBarMedia.setVisibility(View.VISIBLE);
                                sent.MessageIvMediaCancel.setVisibility(View.VISIBLE);
                            } else {
                                sent.MessageCvImageSize.setVisibility(View.VISIBLE);
                                sent.MessageTvImageSize.setVisibility(View.VISIBLE);
                                String s = cursor.getLong(cursor.getColumnIndexOrThrow("mediaSize")) + " Kb ";
                                sent.MessageTvImageSize.setText(s);
                                sent.MessageCvImageSize.setOnClickListener(view -> {
                                    cursor.moveToPosition(holder.getAdapterPosition());
                                    Intent intent;
                                    sent.MessageCvImageSize.setVisibility(View.GONE);
                                    sent.MessagePbProgressBarMedia.setVisibility(View.VISIBLE);
                                    sent.MessageIvMediaCancel.setVisibility(View.VISIBLE);
                                    Log.i("INTENT MSG ID:::::", cursor.getString(cursor.getColumnIndexOrThrow("msg_ID")));
                                    intent = new Intent(mContext.getApplicationContext(), UploadFileService.class);
                                    intent.putExtra(UploadFileService.EXTRA_RECEIVER, resultReceiver);
                                    Messenger messenger = new Messenger(new Handler() {
                                        @Override
                                        public void handleMessage(Message msg) {
                                            // Update progress bar
                                            int progress = msg.arg1;
                                            sent.MessagePbProgressBarMedia.setProgress(progress);
                                        }
                                    });
                                    intent.putExtra(Constants.Intent.KEY_INTENT_MESSENGER, messenger);
                                    intent.putExtra(Constants.Intent.KEY_INTENT_MESSAGE_ID, cursor.getString(cursor.getColumnIndexOrThrow("msg_ID")));
                                    mContext.startService(intent);
                                });
                            }
                        }
                        sent.MessageIvImage.setOnClickListener(view -> {
                            openMedia(viewHolder.mediaPos,view.getContext());
                        });
                    } else {
                        sent.MessageIvImage.setOnClickListener(view -> MediaMissingDialog());
                    }
                } else if (cursor.getInt(cursor.getColumnIndexOrThrow("mediaType")) == Constants.Media.KEY_MESSAGE_MEDIA_TYPE_DOCUMENT) {
                    sent.MessageCvDoc.setVisibility(View.VISIBLE);
                    if (file.exists()) {
                        sent.MessageTvDocName.setText(cursor.getString(cursor.getColumnIndexOrThrow("mediaID")));
                        sent.MessageTvDocSize.setText(String.valueOf(cursor.getLong(cursor.getColumnIndexOrThrow("mediaSize"))));
                        int l = cursor.getString(cursor.getColumnIndexOrThrow("mediaID")).lastIndexOf('.');
                        sent.MessageTvDocType.setText(cursor.getString(cursor.getColumnIndexOrThrow("mediaID")).substring(l + 1));
                        if (cursor.getInt(cursor.getColumnIndexOrThrow("status")) == -1) {
                            if (Communicator.mediaUploading.contains(cursor.getString(cursor.getColumnIndexOrThrow("msg_ID")))) {
                                sent.MessageIvDocUpload.setVisibility(View.GONE);
                                sent.MessagePbDocDownloadProgress.setVisibility(View.VISIBLE);
                                sent.MessageIvDocCancel.setVisibility(View.VISIBLE);
                            } else {
                                sent.MessageIvDocUpload.setVisibility(View.VISIBLE);
                                sent.MessageIvDocUpload.setOnClickListener(view -> {
                                    cursor.moveToPosition(holder.getAdapterPosition());
                                    Intent intent;
                                    sent.MessageIvDocUpload.setVisibility(View.GONE);
                                    sent.MessagePbDocDownloadProgress.setVisibility(View.VISIBLE);
                                    sent.MessageIvDocCancel.setVisibility(View.VISIBLE);
                                    intent = new Intent(mContext.getApplicationContext(), UploadFileService.class);
                                    intent.putExtra(UploadFileService.EXTRA_RECEIVER, resultReceiver);
                                    Messenger messenger = new Messenger(new Handler() {
                                        @Override
                                        public void handleMessage(Message msg) {
                                            // Update progress bar
                                            int progress = msg.arg1;
                                            sent.MessagePbProgressBarMedia.setProgress(progress);
                                        }
                                    });
                                    intent.putExtra(Constants.Intent.KEY_INTENT_MESSENGER, messenger);
                                    intent.putExtra(Constants.Intent.KEY_INTENT_MESSAGE_ID, cursor.getString(cursor.getColumnIndexOrThrow("msg_ID")));
                                    mContext.startService(intent);
                                });
                            }
                        }
                    } else {
                        //Media Missing
                    }
                } else if (cursor.getInt(cursor.getColumnIndexOrThrow("mediaType")) == Constants.Media.KEY_MESSAGE_MEDIA_TYPE_VIDEO) {
                    viewHolder.mediaPos = mediaPosition++;
                    if (file.exists()) {
                        sent.MessageClMedia.setVisibility(View.VISIBLE);
//                        sent.MessageVvVideo.setVisibility(View.VISIBLE);
                        sent.MessageIvImage.setVisibility(View.VISIBLE);
                        sent.MessageIvImage.setImageBitmap(ThumbnailUtils.createVideoThumbnail(file.getAbsolutePath(), MediaStore.Video.Thumbnails.MINI_KIND));
                        sent.MessageIbPlayVid.setVisibility(View.VISIBLE);
//                        sent.MessageVvVideo.setVideoURI(Uri.fromFile(file));
//                        sent.MessageVvVideo.seekTo(1);
                        sent.MessageIbPlayVid.setOnClickListener(view -> {
                            openMedia(viewHolder.mediaPos,view.getContext());
                        });
                        sent.MessageIvImage.setOnClickListener(view -> {
                            //Video
                            openMedia(viewHolder.mediaPos,view.getContext());

                        });
                        if (cursor.getInt(cursor.getColumnIndexOrThrow("status")) == -1) {
                            if (Communicator.mediaUploading.contains(cursor.getString(cursor.getColumnIndexOrThrow("msg_ID")))) {
                                sent.MessageCvVideoSize.setVisibility(View.GONE);
                                sent.MessagePbProgressBarMedia.setVisibility(View.VISIBLE);
                                sent.MessageIvMediaCancel.setVisibility(View.VISIBLE);
                            } else {
                                sent.MessageCvVideoSize.setVisibility(View.VISIBLE);
                                sent.MessageTvVideoSize.setVisibility(View.VISIBLE);
                                sent.MessageTvVideoSize.setText(cursor.getLong(cursor.getColumnIndexOrThrow("mediaSize")) + " Kb ");
                                sent.MessageCvVideoSize.setOnClickListener(view -> {
                                    cursor.moveToPosition(holder.getAdapterPosition());
                                    Intent intent;
                                    sent.MessageCvVideoSize.setVisibility(View.GONE);
                                    sent.MessagePbProgressBarMedia.setVisibility(View.VISIBLE);
                                    sent.MessageIvMediaCancel.setVisibility(View.VISIBLE);
                                    Log.i("INTENT MSG ID:::::", cursor.getString(cursor.getColumnIndexOrThrow("msg_ID")));
                                    intent = new Intent(mContext.getApplicationContext(), UploadFileService.class);
                                    intent.putExtra(UploadFileService.EXTRA_RECEIVER, resultReceiver);
                                    Messenger messenger = new Messenger(new Handler() {
                                        @Override
                                        public void handleMessage(Message msg) {
                                            // Update progress bar
                                            int progress = msg.arg1;
                                            sent.MessagePbProgressBarMedia.setProgress(progress);
                                        }
                                    });
                                    intent.putExtra(Constants.Intent.KEY_INTENT_MESSENGER, messenger);
                                    intent.putExtra(Constants.Intent.KEY_INTENT_MESSAGE_ID, cursor.getString(cursor.getColumnIndexOrThrow("msg_ID")));
                                    mContext.startService(intent);
                                });
                            }
                        }
                    } else {
                        sent.MessageIbPlayVid.setOnClickListener(view -> MediaMissingDialog());
                    }

                } else if (cursor.getInt(cursor.getColumnIndexOrThrow("mediaType")) == Constants.Media.KEY_MESSAGE_MEDIA_TYPE_AUDIO) {
                    sent.MessageMediaClLlAudio.setVisibility(View.VISIBLE);
                    sent.MessageLLMedia.setVisibility(View.VISIBLE);
                    sent.MessageClMedia.setVisibility(View.VISIBLE);
                    sent.MessageCl.setVisibility(View.VISIBLE);
                    sent.MessageMediaAudioTvAudioName.setVisibility(View.VISIBLE);
                    sent.MessageMediaAudioTvAudioName.setText(cursor.getString(cursor.getColumnIndexOrThrow("mediaID")));
                    if (file.exists()) {
                        sent.MessageMediaAudioTvAudioName.setVisibility(View.VISIBLE);
                        sent.MessageMediaAudioIvIcon.setImageResource(R.drawable.ic_baseline_headphones_24);
                        sent.MessageMediaAudioTvAudioLength.setText(UsefulFunctions.getAudioLength(file.getPath()));
                        sent.MessageMediaAudioIbPlayPause.setOnClickListener(view -> {
                            sent.MessageMediaAudioIbPlayPause.setImageResource(R.drawable.ic_baseline_pause_24);
                            if (ib_currAudioButton == null)
                                ib_currAudioButton = sent.MessageMediaAudioIbPlayPause;
                            if (audioMediaPlayer != null && audioMediaPlayer.isPlaying()) {

                                if (sent.MessageMediaAudioIbPlayPause == ib_currAudioButton) {
                                    pauseMedia();
                                    sent.MessageMediaAudioIbPlayPause.setImageResource(R.drawable.ic_baseline_audio_play_24);
                                    return;
                                } else {
                                    stopMedia();
                                    audioMediaPlayer = null;
                                    ib_currAudioButton.setImageResource(R.drawable.ic_baseline_audio_play_24);
                                    ib_currAudioButton = sent.MessageMediaAudioIbPlayPause;
                                }
                            }

                            if (audioMediaPlayer == null) {
                                Log.i("AUDIO BUTTON CLICK ::::::", "1_");
                                audioMediaPlayer = MediaPlayer.create(mContext, Uri.parse(file.getPath()));
                                audioMediaPlayer.setOnCompletionListener(mp -> {
                                    stopMedia();
                                    ib_currAudioButton.setImageResource(R.drawable.ic_baseline_audio_play_24);
                                    sent.MessageMediaAudioSbProgress.setProgress(0);
                                });
                            }

                            if (sent.MessageMediaAudioSbProgress.getProgress() != 0)
                                audioMediaPlayer.seekTo(sent.MessageMediaAudioSbProgress.getProgress());
                            audioMediaPlayer.start();
                            sent.MessageMediaAudioIbPlayPause.setImageResource(R.drawable.ic_baseline_pause_24);

                            // update seekbar
                            sent.MessageMediaAudioSbProgress.setMax(audioMediaPlayer.getDuration());
                            sent.MessageMediaAudioSbProgress.setProgress(audioMediaPlayer.getCurrentPosition());

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
                                        sent.MessageMediaAudioSbProgress.setProgress(audioMediaPlayer.getCurrentPosition());
                                        new Handler(Looper.getMainLooper()).postDelayed(this, 1000);
                                    } else {
                                        return;
                                    }
                                    prevAud = audioMediaPlayer.getAudioSessionId();
                                }
                            }, 1000);
                        });


                        sent.MessageMediaAudioSbProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override
                            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                sent.MessageMediaAudioTvAudioLength.setText(UsefulFunctions.getStringMmSsTimeVale(audioMediaPlayer.getCurrentPosition()));
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
                            sent.MessageMediaAudioIvIcon.setImageResource(R.drawable.ic_baseline_upload_24);
                            sent.MessageMediaAudioIvIcon.setOnClickListener(view -> {
                                sent.MessageMediaAudioIvIcon.setImageResource(R.drawable.ic_outline_cancel_24);
                                Intent intent;
                                cursor.moveToPosition(holder.getAdapterPosition());

                                intent = new Intent(mContext.getApplicationContext(), UploadFileService.class);
                                intent.putExtra(UploadFileService.EXTRA_RECEIVER, resultReceiver);
                                Messenger messenger = new Messenger(new Handler() {
                                    @Override
                                    public void handleMessage(Message msg) {
                                        // Update progress bar
                                        int progress = msg.arg1;
                                        sent.MessagePbProgressBarMedia.setProgress(progress);
                                    }
                                });
                                intent.putExtra(Constants.Intent.KEY_INTENT_MESSENGER, messenger);
                                intent.putExtra(Constants.Intent.KEY_INTENT_MESSAGE_ID, cursor.getString(cursor.getColumnIndexOrThrow("msg_ID")));
                                mContext.startService(intent);
                            });
                            sent.MessageMediaAudioTvAudioLength.setText(UsefulFunctions.getSizeValue(cursor.getLong(cursor.getColumnIndexOrThrow("mediaSize"))));

                        }
                    } else {
                        sent.MessageClMedia.setVisibility(View.GONE);
                    }
                }
            }
        } else {
            ReceivedMessageView viewHolder = (ReceivedMessageView) holder;
            MessageReceivedLayoutBinding received = viewHolder.receivedLayoutBinding;
            if (cursor.getString(cursor.getColumnIndexOrThrow("msg")) != null) {
                String msg = cursor.getString(cursor.getColumnIndexOrThrow("msg"));
                msg.replaceAll("(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]", "<a href='$0'>$0</a>")
                        .replaceAll("[^\\s]+@[^\\s]+", "<a href='mailto:$0'>$0</a>")
                        .replaceAll("(\\d{3}[-\\.\\s]??\\d{3}[-\\.\\s]??\\d{4}|\\(\\d{3}\\)\\s*\\d{3}[-\\.\\s]??\\d{4}|\\d{3}[-\\.\\s]??\\d{4})", "<a href='tel:$0'>$0</a>");
                received.MessageMsgBox.setText(msg);
                received.MessageMsgBox.setPadding(25, 1, 25, 1);
                received.MessageMsgBox.setMovementMethod(LinkMovementMethod.getInstance());
            } else {
                received.MessageMsgBox.setVisibility(View.GONE);
            }
            received.MessageMsgBox.setPadding(25, 1, 25, 1);
            String t= cursor.getString(cursor.getColumnIndexOrThrow("receivedTime"));
            received.MessageTime.setText(UsefulFunctions.getTimeHhMm(t));
            received.MessageTime.setPadding(25, 1, 25, 1);
            if (active && cursor.getInt(cursor.getColumnIndexOrThrow("unreadUser")) == 1 && !unreadFound) {
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
                    Log.i("MEDIA ID:::::", "fileID");

                    received.MessageClMedia.setVisibility(View.VISIBLE);
                    received.MessageIvImage.setVisibility(View.VISIBLE);
                    if (file.exists()) {
                        received.MessageIvImage.setOnClickListener(view -> {
                            openMedia(viewHolder.mediaPos,view.getContext());
                        });
                    } else {
                        received.MessageCvImageSize.setVisibility(View.VISIBLE);
                        received.MessageTvImageSize.setVisibility(View.VISIBLE);
                        received.MessageTvImageSize.setText(String.valueOf(cursor.getLong(cursor.getColumnIndexOrThrow("mediaType"))));
                        received.MessageCvImageSize.setOnClickListener(view -> {
                            received.MessageIvMediaCancel.setVisibility(View.VISIBLE);
                            received.MessagePbProgressBarMedia.setVisibility(View.VISIBLE);
                            Log.i("CLICKED::::", view + "");
                            Intent intent;
                            cursor.moveToPosition(holder.getAdapterPosition());
                            intent = new Intent(mContext.getApplicationContext(), DownloadFileService.class);
                            intent.putExtra(UploadFileService.EXTRA_RECEIVER, resultReceiver);
                            intent.putExtra(Constants.Intent.KEY_INTENT_MESSAGE_ID, cursor.getString(cursor.getColumnIndexOrThrow("msg_ID")));
                            mContext.startService(intent);

                        });
                    }

                } else if (cursor.getInt(cursor.getColumnIndexOrThrow("mediaType")) == Constants.Media.KEY_MESSAGE_MEDIA_TYPE_DOCUMENT) {
                    received.MessageCvDoc.setVisibility(View.VISIBLE);
                    if (file.exists()) {
                        received.MessageTvDocName.setText(cursor.getString(cursor.getColumnIndexOrThrow("mediaID")));
                        received.MessageTvImageSize.setText(cursor.getLong(cursor.getColumnIndexOrThrow("mediaSize")) + "KB");
                        int l = cursor.getString(cursor.getColumnIndexOrThrow("mediaID")).lastIndexOf('.');
                        received.MessageTvDocType.setText(cursor.getString(cursor.getColumnIndexOrThrow("mediaID")).substring(l + 1).toUpperCase());
                    } else {
                        received.MessageIvDocUpload.setVisibility(View.VISIBLE);
                        received.MessageIvDocUpload.setOnClickListener(view -> {
                            received.MessageIvDocUpload.setVisibility(View.GONE);
                            received.MessagePbDocDownloadProgress.setVisibility(View.VISIBLE);
                            received.MessageIvDocCancel.setVisibility(View.VISIBLE);
                            cursor.moveToPosition(holder.getAdapterPosition());

                            Intent intent;
                            intent = new Intent(mContext.getApplicationContext(), DownloadFileService.class);
                            intent.putExtra(UploadFileService.EXTRA_RECEIVER, resultReceiver);
                            intent.putExtra(Constants.Intent.KEY_INTENT_MESSAGE_ID, cursor.getString(cursor.getColumnIndexOrThrow("msg_ID")));

                            mContext.startService(intent);
                        });
                    }
                } else if (cursor.getInt(cursor.getColumnIndexOrThrow("mediaType")) == Constants.Media.KEY_MESSAGE_MEDIA_TYPE_VIDEO) {

                    viewHolder.mediaPos = mediaPosition++;
                    received.MessageClMedia.setVisibility(View.VISIBLE);
//                    received.MessageVvVideo.setVisibility(View.VISIBLE);
                    received.MessageIbPlayVid.setVisibility(View.VISIBLE);
                    received.MessageIvImage.setVisibility(View.VISIBLE);
                    if (file.exists()) {
                        Log.i("VIDEO::::::", file.getName());
//                        received.MessageVvVideo.setVideoURI(Uri.fromFile(file));
//                        received.MessageVvVideo.seekTo(1);
                        received.MessageIvImage.setImageBitmap(ThumbnailUtils.createVideoThumbnail(file.getAbsolutePath(), MediaStore.Video.Thumbnails.MINI_KIND));
                        received.MessageIvImage.setOnClickListener(view -> {
                            openMedia(viewHolder.mediaPos,view.getContext());
                        });

                    } else {
                        Log.i("ChatView_RecyclerAdapter.VIDEO::::::", cursor.getString(cursor.getColumnIndexOrThrow("mediaID")));
                        received.MessageCvVideoSize.setVisibility(View.VISIBLE);
                        received.MessageTvVideoSize.setOnClickListener(view -> {
                            Intent intent;
                            cursor.moveToPosition(holder.getAdapterPosition());
                            intent = new Intent(mContext.getApplicationContext(), DownloadFileService.class);
                            intent.putExtra(DownloadFileService.EXTRA_RECEIVER, resultReceiver);
                            intent.putExtra(Constants.Intent.KEY_INTENT_MESSAGE_ID, cursor.getString(cursor.getColumnIndexOrThrow("msg_ID")));
                            mContext.startService(intent);
                        });
                        received.MessageIvImage.setOnClickListener(view -> {
                            Intent intent;
                            cursor.moveToPosition(holder.getAdapterPosition());
                            intent = new Intent(mContext.getApplicationContext(), DownloadFileService.class);
                            intent.putExtra(DownloadFileService.EXTRA_RECEIVER, resultReceiver);
                            intent.putExtra(Constants.Intent.KEY_INTENT_MESSAGE_ID, cursor.getString(cursor.getColumnIndexOrThrow("msg_ID")));
                            mContext.startService(intent);
                        });
                    }
                } else if (cursor.getInt(cursor.getColumnIndexOrThrow("mediaType")) == Constants.Media.KEY_MESSAGE_MEDIA_TYPE_AUDIO) {
                    received.MessageMediaClLlAudio.setVisibility(View.VISIBLE);
                    if (file.exists()) {
                        received.MessageMediaAudioTvAudioName.setVisibility(View.VISIBLE);
                        received.MessageMediaAudioTvAudioName.setText(cursor.getString(cursor.getColumnIndexOrThrow("mediaID")));
                        received.MessageMediaAudioIvIcon.setImageResource(R.drawable.ic_baseline_headphones_24);
                        received.MessageMediaAudioTvAudioLength.setText(UsefulFunctions.getAudioLength(file.getPath()));
                        received.MessageMediaAudioIbPlayPause.setOnClickListener(view -> {
                            received.MessageMediaAudioIbPlayPause.setImageResource(R.drawable.ic_baseline_pause_24);
//                            startAudioMediaPlayer(file.getPath(), received.MessageMediaAudioSbProgress);
                            if (ib_currAudioButton == null)
                                ib_currAudioButton = received.MessageMediaAudioIbPlayPause;
                            if (audioMediaPlayer != null && audioMediaPlayer.isPlaying()) {

                                if (received.MessageMediaAudioIbPlayPause == ib_currAudioButton) {
                                    pauseMedia();
                                    received.MessageMediaAudioIbPlayPause.setImageResource(R.drawable.ic_baseline_audio_play_24);
                                    return;
                                } else {
                                    stopMedia();
                                    audioMediaPlayer = null;
                                    ib_currAudioButton.setImageResource(R.drawable.ic_baseline_audio_play_24);
                                    ib_currAudioButton = received.MessageMediaAudioIbPlayPause;
                                }
                            }

                            if (audioMediaPlayer == null) {
                                Log.i("AUDIO BUTTON CLICK ::::::", "1_");
                                audioMediaPlayer = MediaPlayer.create(mContext, Uri.parse(file.getPath()));
                                audioMediaPlayer.setOnCompletionListener(mp -> {
                                    stopMedia();
                                    ib_currAudioButton.setImageResource(R.drawable.ic_baseline_audio_play_24);
                                    received.MessageMediaAudioSbProgress.setProgress(0);
                                });
                            }

                            if (received.MessageMediaAudioSbProgress.getProgress() != 0)
                                audioMediaPlayer.seekTo(received.MessageMediaAudioSbProgress.getProgress());
                            audioMediaPlayer.start();
                            received.MessageMediaAudioIbPlayPause.setImageResource(R.drawable.ic_baseline_pause_24);

                            // update seekbar
                            received.MessageMediaAudioSbProgress.setMax(audioMediaPlayer.getDuration());
                            received.MessageMediaAudioSbProgress.setProgress(audioMediaPlayer.getCurrentPosition());

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
                                        received.MessageMediaAudioSbProgress.setProgress(audioMediaPlayer.getCurrentPosition());
                                        new Handler(Looper.getMainLooper()).postDelayed(this, 1000);
                                    } else {
                                        return;
                                    }
                                    prevAud = audioMediaPlayer.getAudioSessionId();
                                }
                            }, 1000);
                        });


                        received.MessageMediaAudioSbProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override
                            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                received.MessageMediaAudioTvAudioLength.setText(UsefulFunctions.getStringMmSsTimeVale(progress));
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
                    } else {
                        received.MessageMediaAudioIvIcon.setImageResource(R.drawable.ic_baseline_download_24);
                        received.MessageMediaAudioIbPlayPause.setOnClickListener(view -> {
                            received.MessageMediaAudioIbPlayPause.setImageResource(R.drawable.ic_outline_cancel_24);
                            Intent intent;
                            cursor.moveToPosition(holder.getAdapterPosition());
                            intent = new Intent(mContext.getApplicationContext(), DownloadFileService.class);
                            intent.putExtra(DownloadFileService.EXTRA_RECEIVER, resultReceiver);
                            intent.putExtra(Constants.Intent.KEY_INTENT_MESSAGE_ID, cursor.getString(cursor.getColumnIndexOrThrow("msg_ID")));
                            mContext.startService(intent);
                        });
                        received.MessageMediaAudioTvAudioLength.setText(UsefulFunctions.getSizeValue(cursor.getLong(cursor.getColumnIndexOrThrow("mediaSize"))));

                    }
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


    private void openMedia(int pos, Context cxt){
        Chat_Fragment_MediaView myFragment = new Chat_Fragment_MediaView();
        Bundle bun = new Bundle();
        bun.putInt(Constants.Intent.KEY_INTENT_LIST_POSITION, pos);
        bun.putInt(Constants.Intent.KEY_INTENT_USER_TYPE, Constants.User.USER_TYPE_GROUP);
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