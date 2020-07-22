package com.fzm.chat33.widget.chatrow;

import android.Manifest;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;

import com.alibaba.android.arouter.launcher.ARouter;
import com.fuzamei.common.utils.PermissionUtil;
import com.fuzamei.common.utils.RoomUtils;
import com.fuzamei.common.utils.ShowUtils;
import com.fuzamei.common.utils.ToolUtils;
import com.fuzamei.componentservice.app.AppRoute;
import com.fuzamei.componentservice.config.AppConfig;
import com.fzm.chat33.R;
import com.fzm.chat33.main.adapter.ChatListAdapter;
import com.fzm.chat33.core.db.ChatDatabase;
import com.fzm.chat33.core.db.bean.ChatMessage;
import com.fzm.chat33.core.global.Chat33Const;
import com.fzm.chat33.hepler.FileDownloadManager;
import com.fzm.chat33.utils.FileUtils;
import com.qmuiteam.qmui.widget.QMUIProgressBar;

import java.io.File;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * @author zhengjy
 * @since 2019/02/11
 * Description:
 */
public class ChatRowFile extends ChatRowBase {

    private final int SAVE_FILE = 1;

    QMUIProgressBar pb_file;
    View bubbleLayout;
    ImageView iv_file_type, iv_cancel;
    TextView tv_file_name, tv_file_size, tv_forward, thumb_up;

    public ChatRowFile(FragmentActivity activity, ChatMessage message, int position, ChatListAdapter adapter) {
        super(activity, message, position, adapter);

    }

    @Override
    int getLayoutId() {
        return message.isSentType() ? R.layout.chat_row_sent_file : R.layout.chat_row_receive_file;

    }

    @Override
    void onFindViewById() {
        bubbleLayout = rootView.findViewById(R.id.chat_message_layout);
        iv_file_type = rootView.findViewById(R.id.iv_file_type);
        tv_file_name = rootView.findViewById(R.id.tv_file_name);
        tv_file_size = rootView.findViewById(R.id.tv_file_size);
        pb_file = rootView.findViewById(R.id.pb_file);
        thumb_up = rootView.findViewById(R.id.thumb_up);
        if (message.isSentType()) {
            tv_forward = rootView.findViewById(R.id.tv_forward);
        } else {
            iv_cancel = rootView.findViewById(R.id.iv_cancel);
        }
    }

    @Override
    void onSetUpView() {
        if (message.isSentType()) {
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
        tv_file_name.setText(message.msg.fileName);
        tv_file_size.setText(activity.getString(R.string.chat_file_size, ToolUtils.byte2Mb(message.msg.fileSize)));
        String fileNameExtension = FileUtils.getExtension(message.msg.fileName);
        switch (fileNameExtension) {
            case "doc":
            case "docx":
                iv_file_type.setImageResource(R.mipmap.icon_file_doc);
                break;
            case "pdf":
                iv_file_type.setImageResource(R.mipmap.icon_file_pdf);
                break;
            case "ppt":
            case "pptx":
                iv_file_type.setImageResource(R.mipmap.icon_file_other);
                break;
            case "xls":
            case "xlsx":
                iv_file_type.setImageResource(R.mipmap.icon_file_xls);
                break;
            case "mp3":
            case "wma":
            case "wav":
            case "ogg":
                iv_file_type.setImageResource(R.mipmap.icon_file_music);
                break;
            case "mp4":
            case "avi":
            case "rmvb":
            case "flv":
            case "f4v":
            case "mpg":
            case "mkv":
                iv_file_type.setImageResource(R.mipmap.icon_file_video);
                break;
            case "ext":
            default:
                iv_file_type.setImageResource(R.mipmap.icon_file_other);
                break;
        }
        if (message.msg.getLocalPath() != null && new File(message.msg.getLocalPath()).exists()) {
            if (iv_cancel != null) {
                iv_cancel.setVisibility(View.GONE);
            }
        }
        if (message.msg.downloading) {
            if (iv_cancel != null) {
                iv_cancel.setVisibility(View.VISIBLE);
            }
        } else {
            if (iv_cancel != null) {
                iv_cancel.setVisibility(View.GONE);
            }
        }
        if (iv_cancel != null) {
            iv_cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FileDownloadManager.INS.cancel(message.msg.fileUrl);
                    message.msg.downloading = false;
                    pb_file.setMaxValue(100);
                    pb_file.setVisibility(View.GONE);
                    if (iv_cancel != null) {
                        iv_cancel.setVisibility(View.GONE);
                    }
                }
            });
        }
        if (message.msg.getLocalPath() == null || !new File(message.msg.getLocalPath()).exists()) {
            // 自动下载文件
            doDownloadWork(true);
        }
    }

    public void clickBubble() {
        if (message.msg.getLocalPath() == null || !new File(message.msg.getLocalPath()).exists()) {
            if (message.msg.getLocalPath() != null) {
                message.msg.setLocalPath(null);
                RoomUtils.run(new Runnable() {
                    @Override
                    public void run() {
                        ChatDatabase.getInstance().chatMessageDao().insert(message);
                    }
                });
            }
            doDownloadWork(false);
        } else {
            ARouter.getInstance().build(AppRoute.FILE_DETAIL).withSerializable("message", message).navigation();
        }
    }

    @Override
    protected View chatMainView() {
        return bubbleLayout;
    }

    @Override
    public TextView thumbUpView() {
        return thumb_up;
    }

    @AfterPermissionGranted(SAVE_FILE)
    private void doDownloadWork(boolean auto) {
        if (PermissionUtil.hasWriteExternalPermission()) {
            File folder = new File(activity.getFilesDir().getPath() + "/download/file");
            if (!folder.exists()) {
                folder.mkdirs();
            }
            message.msg.downloading = true;
            FileDownloadManager.INS.download(folder, message, new FileDownloadManager.DownloadCallback() {
                @Override
                public void onStart() {
                    pb_file.setMaxValue(100);
                    if (pb_file.getVisibility() == View.GONE) {
                        pb_file.setVisibility(View.VISIBLE);
                    }
                    if (iv_cancel != null && iv_cancel.getVisibility() == View.GONE) {
                        iv_cancel.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onAlreadyRunning() {
                    if (!auto) {
                        ShowUtils.showSysToast(activity, R.string.chat_tips_downloading);
                    }
                }

                @Override
                public void onProgress(float progress) {
                    pb_file.setProgress((int) (progress * 100));
                }

                @Override
                public void onFinish(File file, Throwable throwable) {
                    message.msg.downloading = false;
                    pb_file.setVisibility(View.GONE);
                    if (iv_cancel != null) {
                        iv_cancel.setVisibility(View.GONE);
                    }
                    if (file != null) {
                        message.msg.setLocalPath(file.getAbsolutePath());
                        if (!auto) {
                            ShowUtils.showSysToast(activity, activity.getString(R.string.chat_tips_file_download_to, file.getAbsolutePath()));
                        }
                    } else {
                        message.msg.setLocalPath(null);
                        if (!auto) {
                            ShowUtils.showSysToast(activity, R.string.chat_tips_file_download_fail);
                        }
                    }
                    RoomUtils.run(new Runnable() {
                        @Override
                        public void run() {
                            ChatDatabase.getInstance().chatMessageDao().insert(message);
                        }
                    });
                }
            });
        } else {
            EasyPermissions.requestPermissions(activity, activity.getString(R.string.chat_error_permission_storage), SAVE_FILE, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
    }
}
