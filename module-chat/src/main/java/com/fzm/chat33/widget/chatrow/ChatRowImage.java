package com.fzm.chat33.widget.chatrow;

import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.transition.Transition;
import com.fzm.chat33.R;
import com.fuzamei.common.utils.ScreenUtils;
import com.fuzamei.common.utils.ToolUtils;
import com.fzm.chat33.core.db.ChatDatabase;
import com.fzm.chat33.hepler.FileDownloadManager;
import com.fzm.chat33.hepler.glide.GlideApp;
import com.fzm.chat33.main.adapter.ChatListAdapter;
import com.fzm.chat33.core.db.bean.ChatMessage;
import com.fzm.chat33.core.db.bean.ChatFile;
import com.fzm.chat33.core.db.dao.ChatMessageDao;
import com.fzm.chat33.core.global.Chat33Const;
import com.fuzamei.common.callback.GlideTarget;
import com.fuzamei.common.utils.RoomUtils;
import com.fuzamei.common.utils.ShowUtils;
import com.fzm.chat33.main.mvvm.ChatViewModel;

import java.io.File;

public class ChatRowImage extends ChatRowBase implements SnapChat {

    private View iv_lock, chat_message_snap;
    private ImageView ivImage;
    private TextView tv_count, tv_forward, thumb_up;
    private SnapChatCountDown timer;

    private String formatUrl;
    private ChatViewModel viewModel;

    public ChatRowImage(FragmentActivity activity, ChatMessage message, int position, ChatListAdapter adapter) {
        super(activity, message, position, adapter);
        formatUrl = "?x-oss-process=image/resize,h_"+ScreenUtils.dp2px(activity, 150)+"/quality,q_70/format,jpg/interlace,1";
        viewModel = ViewModelProviders.of(activity).get(ChatViewModel.class);
    }

    @Override
    int getLayoutId() {
        return message.isSentType() ? R.layout.chat_row_sent_image : R.layout.chat_row_receive_image;
    }

    @Override
    void onFindViewById() {
        ivImage = rootView.findViewById(R.id.iv_image);
        iv_lock = rootView.findViewById(R.id.iv_lock);
        thumb_up = rootView.findViewById(R.id.thumb_up);
        if (message.isSentType()) {
            tv_forward = rootView.findViewById(R.id.tv_forward);
        }
    }

    @Override
    protected View chatMainView() {
        return ivImage;
    }

    @Override
    public TextView thumbUpView() {
        return message.isSnap != 1 ? thumb_up : null;
    }

    @Override
    void onSetUpView() {
        if (!message.isSentType()) {
            tv_count = rootView.findViewById(R.id.tv_count);
            chat_message_snap = rootView.findViewById(R.id.chat_message_snap);
        } else {
            if (message.msg.sourceChannel == Chat33Const.CHANNEL_ROOM) {
                tv_forward.setVisibility(View.VISIBLE);
                tv_forward.setText(activity.getString(R.string.chat_forward_room_content, message.msg.sourceName));
            } else if (message.msg.sourceChannel == Chat33Const.CHANNEL_FRIEND) {
                tv_forward.setVisibility(View.VISIBLE);
                tv_forward.setText(activity.getString(R.string.chat_forward_friend_content, message.msg.sourceName));
            } else {
                tv_forward.setVisibility(View.GONE);
            }
        }
        if (message.isSnap == 1) {
            if (message.isSentType()) {
                iv_lock.setVisibility(View.VISIBLE);
            } else if (message.snapVisible == 0) {
                hideContent();
            } else {
                showContent();
            }
        } else {
            ivImage.setVisibility(View.VISIBLE);
            if (!message.isSentType()) {
                tv_count.setVisibility(View.GONE);
                chat_message_snap.setVisibility(View.GONE);
            }
            iv_lock.setVisibility(View.GONE);
        }
        if (chat_message_snap != null) {
            chat_message_snap.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ChatMessageDao.visibleSnapMsg.add(message.channelType + "-" + message.logId);
                    viewModel.readSnapMessage(message.logId,
                            message.channelType == Chat33Const.CHANNEL_ROOM ? 1 : 2).observe(activity, result -> {
                        if (result.isSucceed()) {
                            showContent();
                        } else {
                            ChatMessageDao.visibleSnapMsg.remove(message.channelType + "-" + message.logId);
                            ShowUtils.showToast(activity, result.error().getMessage());
                        }
                    });
                }
            });
        }
        if (message.msg.getHeight() > 0 && message.msg.getWidth() > 0) {
            int result[] = ToolUtils.getChatImageHeightWidth(activity, message.msg.getHeight(), message.msg.getWidth());
            int height = result[0];
            int width = result[1];
            setViewParams(height, width, ivImage);
        } else {
            setViewParams(ScreenUtils.dp2px(activity, 150f), ScreenUtils.dp2px(activity, 150f), ivImage);
        }
//        ivImage.setImageResource(R.drawable.bg_image_placeholder);
        setupChatImage();
    }

    private RequestOptions mImageOptions = new RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.DATA)
            .placeholder(R.drawable.bg_image_placeholder)
//            .placeholder(new BitmapDrawable(activity.getResources(),
//                    ImageUtils.drawable2Bitmap(ContextCompat.getDrawable(activity, R.drawable.bg_image_placeholder))))
            .centerCrop();

    private void doDownloadWork() {
        File folder = new File(activity.getFilesDir().getPath() + "/picture");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        message.msg.downloading = true;
        FileDownloadManager.INS.download(folder, message, new FileDownloadManager.DownloadCallback() {
            @Override
            public void onStart() {

            }

            @Override
            public void onAlreadyRunning() {

            }

            @Override
            public void onProgress(float progress) {

            }

            @Override
            public void onFinish(File file, Throwable throwable) {
                message.msg.downloading = false;
                if (file != null) {
                    message.msg.setLocalPath(file.getAbsolutePath());
                    if (message.isSnap == 1) {
                        showContent();
                    } else {
                        setupChatImage();
                    }
                } else {
                    message.msg.setLocalPath(null);
                }
                RoomUtils.run(new Runnable() {
                    @Override
                    public void run() {
                        ChatDatabase.getInstance().chatMessageDao().insert(message);
                    }
                });
            }
        });
    }

    private void setupChatImage() {
        if(message.msg == null){
            return;
        }
        if (!message.isSentType() && message.isSnap == 1) {
            return;
        }
        final String imageUrl = message.msg.getLocalPath();
        if (TextUtils.isEmpty(imageUrl) || !new File(imageUrl).exists()) {
            doDownloadWork();
        } else {
            if (imageUrl.endsWith("gif")) {
                displayGif();
            } else {
                displayImage();
            }
        }
    }

    private void displayGif() {
        GlideApp.with(activity).asGif().load(message).apply(mImageOptions).into(ivImage);
//        Glide.with(activity).asGif().load(imageUrl.startsWith("http")
//                ? ToolUtils.createCookieUrl(imageUrl + formatUrl)
//                : "file://" + imageUrl).apply(mImageOptions).into(ivImage);
    }

    private void displayImage() {
        GlideApp.with(activity).load(message).apply(mImageOptions)
//        Glide.with(activity).load(imageUrl.startsWith("http")
//                ? ToolUtils.createCookieUrl(imageUrl + formatUrl)
//                : "file://" + imageUrl).apply(mImageOptions)
                .into(new GlideTarget(ivImage, R.id.iv_image, message.logId, new GlideTarget.Callback() {
                    @Override
                    public void onResourceLoading(ImageView view, @Nullable Drawable placeholder) {
                        view.setImageResource(R.drawable.bg_image_placeholder);
                    }

                    @Override
                    public void onResourceReady(ImageView view, @NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        if (message.isSnap == 1 && !message.isSentType() && message.snapCounting == 0) {
                            startCount();
                        }
                        view.setImageDrawable(resource);
                    }
                }));
    }

    private void setViewParams(int height, int width, View view) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.height = height;
        params.width = width;
        view.setLayoutParams(params);
    }

    @Override
    public void hideContent() {
        ivImage.setVisibility(View.GONE);
        tv_count.setVisibility(View.GONE);
        chat_message_snap.setVisibility(View.VISIBLE);
        iv_lock.setVisibility(View.VISIBLE);
    }

    @Override
    public void showContent() {
        if (message.snapVisible == 0) {
            message.snapVisible = 1;
            RoomUtils.run(new Runnable() {
                @Override
                public void run() {
                    ChatDatabase.getInstance().chatMessageDao()
                            .updateVisible(message.snapVisible, message.channelType, message.logId);
                }
            });
        }
        ivImage.setVisibility(View.VISIBLE);
        tv_count.setVisibility(View.VISIBLE);
        chat_message_snap.setVisibility(View.GONE);
        iv_lock.setVisibility(View.GONE);
        final String imageUrl = message.msg.getLocalPath();
        if (TextUtils.isEmpty(imageUrl) || !new File(imageUrl).exists()) {
            doDownloadWork();
        } else {
            displayImage();
        }
        if (message.snapCounting == 0) {
            tv_count.setText(com.fzm.chat33.utils.StringUtils.formateTime(30_000));
            if (itemClickListener != null) {
                itemClickListener.onMessageShow(ivImage, message, position);
            }
        } else {
            startCount();
        }
    }

    @Override
    public void startCount() {
        if (itemClickListener != null) {
            itemClickListener.onMessageCountDown(message.logId, timer);
        }
        if (message.snapCounting == 0) {
            message.snapCounting = 1;
            RoomUtils.run(new Runnable() {
                @Override
                public void run() {
                    ChatDatabase.getInstance().chatMessageDao()
                            .updateCounting(message.snapCounting, message.channelType, message.logId);
                }
            });
        }
        if (message.timer == null) {
            tv_count.setTag(message);
            timer = new SnapChatCountDown(calculateRemainTime(), 1000L, tv_count, message, new SnapChatCountDown.OnFinishListener() {
                @Override
                public void onFinish(Object object) {
                    destroyContent(object);
                }
            });
            message.timer = timer;
            timer.start();
        } else {
            timer = (SnapChatCountDown) message.timer;
            tv_count.setTag(message);
            tv_count.setText(timer.getCurrentText());
            timer.setCountView(tv_count);
        }
    }

    @Override
    public void destroyContent(Object object) {
        timer = null;
        ((ChatMessage) object).timer = null;
        if (itemClickListener != null) {
            itemClickListener.onMessageDestroy(rootView, (ChatMessage) object);
        }
    }

    @Override
    public long calculateRemainTime() {
        if (message.destroyTime == 0) {
            message.destroyTime = System.currentTimeMillis() + 30_000;
            RoomUtils.run(new Runnable() {
                @Override
                public void run() {
                    ChatDatabase.getInstance().chatMessageDao()
                            .updateDestroyTime(message.destroyTime, message.channelType, message.logId);
                }
            });
            return 30_000;
        } else {
            return message.destroyTime - System.currentTimeMillis();
        }
    }
}
