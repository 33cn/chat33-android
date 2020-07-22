package com.fzm.chat33.widget.forward;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.transition.Transition;
import com.fuzamei.common.utils.RoomUtils;
import com.fuzamei.common.utils.ScreenUtils;
import com.fuzamei.common.utils.ToolUtils;
import com.fzm.chat33.core.db.ChatDatabase;
import com.fzm.chat33.core.db.bean.ChatMessage;
import com.fzm.chat33.hepler.FileDownloadManager;
import com.fzm.chat33.hepler.glide.GlideApp;
import com.fzm.chat33.main.activity.ChatActivity;
import com.fzm.chat33.main.activity.LargePhotoActivity;
import com.fzm.chat33.core.db.bean.ChatFile;
import com.fuzamei.common.callback.GlideTarget;
import com.fzm.chat33.main.adapter.ForwardListAdapter;
import com.fzm.chat33.R;

import java.io.File;

/**
 * @author zhengjy
 * @since 2018/12/27
 * Description:
 */
public class ForwardRowImage extends ForwardRowBase {

    private ImageView ivImage;
    private String formatUrl;

    public ForwardRowImage(Context context, View root, ForwardListAdapter adapter) {
        super(context, root, adapter);
        formatUrl = "?x-oss-process=image/resize,h_" + ScreenUtils.dp2px(context, 150) + "/quality,q_70/format,jpg/interlace,1";
    }

    @Override
    protected void onFindViewById() {
        ivImage = rootView.findViewById(R.id.iv_image);
    }

    @Override
    protected void onSetUpView() {
        ivImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChatMessage temp;
                try {
                    temp = message.clone();
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                    return;
                }
                temp.briefPos = temp.msg.sourceLog.indexOf(chatLog) + 1;
                Intent intent = new Intent(mContext, LargePhotoActivity.class);
                intent.putExtra(LargePhotoActivity.CHAT_MESSAGE, temp);
                ((Activity) mContext).startActivityForResult(intent, ChatActivity.REQUEST_DEAD_TIME,
                        ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) mContext, v, "shareImage").toBundle());
            }
        });
        if (chatLog.msg.getHeight() > 0 && chatLog.msg.getWidth() > 0) {
            int result[] = ToolUtils.getChatImageHeightWidth(mContext, chatLog.msg.getHeight(), chatLog.msg.getWidth());
            int height = result[0];
            int width = result[1];
            setViewParams(height, width, ivImage);
        } else {
            //获取不到图片尺寸的情况，拿到bitmap后获取宽高，然后载入imageView
            setViewParams(ScreenUtils.dp2px(mContext, 150f), ScreenUtils.dp2px(mContext, 150f), ivImage);
        }
        ivImage.setImageResource(R.drawable.bg_image_placeholder);
        if (TextUtils.isEmpty(chatLog.msg.getLocalPath()) || new File(chatLog.msg.getLocalPath()).exists()) {
            doDownloadWork();
        } else {
            displayImage();
        }
    }

    private void doDownloadWork() {
        File folder = new File(mContext.getFilesDir().getPath() + "/picture");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        chatLog.msg.downloading = true;
        FileDownloadManager.INS.download(folder, chatLog, new FileDownloadManager.DownloadCallback() {
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
                chatLog.msg.downloading = false;
                if (file != null) {
                    chatLog.msg.setLocalPath(file.getAbsolutePath());
                } else {
                    chatLog.msg.setLocalPath(null);
                }
                displayImage();
                RoomUtils.run(new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < message.msg.sourceLog.size(); i++) {
                            if (chatLog.logId != null && chatLog.logId.equals(message.msg.sourceLog.get(i).logId)) {
                                message.msg.sourceLog.set(i, chatLog);
                            }
                        }
                        ChatDatabase.getInstance().chatMessageDao().updateSourceLog(message.logId, message.channelType, message.msg.sourceLog);
                    }
                });
            }
        });
    }

    private RequestOptions mImageOptions = new RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
            .centerCrop();

    private void displayImage() {
        ChatMessage temp;
        try {
            temp = message.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return;
        }
        temp.briefPos = temp.msg.sourceLog.indexOf(chatLog) + 1;
        GlideApp.with(mContext).load(temp).apply(mImageOptions)
                .into(new GlideTarget(ivImage, R.id.iv_image, chatLog.logId, new GlideTarget.Callback() {
                    @Override
                    public void onResourceLoading(ImageView view, @Nullable Drawable placeholder) {
                        view.setImageResource(R.drawable.bg_image_placeholder);
                    }

                    @Override
                    public void onResourceReady(ImageView view, @NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
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
}
