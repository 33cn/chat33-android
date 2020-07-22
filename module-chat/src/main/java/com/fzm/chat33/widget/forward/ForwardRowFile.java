package com.fzm.chat33.widget.forward;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.fuzamei.common.utils.PermissionUtil;
import com.fuzamei.common.utils.RoomUtils;
import com.fuzamei.common.utils.ShowUtils;
import com.fuzamei.common.utils.ToolUtils;
import com.fuzamei.componentservice.app.AppRoute;
import com.fuzamei.componentservice.config.AppConfig;
import com.fzm.chat33.R;
import com.fzm.chat33.main.adapter.ForwardListAdapter;
import com.fzm.chat33.core.db.ChatDatabase;
import com.fzm.chat33.hepler.FileDownloadManager;
import com.fzm.chat33.utils.FileUtils;
import com.qmuiteam.qmui.widget.QMUIProgressBar;

import java.io.File;
import java.util.Locale;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * @author zhengjy
 * @since 2019/02/14
 * Description:
 */
public class ForwardRowFile extends ForwardRowBase {

    private final int SAVE_FILE = 1;

    QMUIProgressBar pb_file;
    View bubbleLayout;
    ImageView iv_file_type;
    TextView tv_file_name, tv_file_size;

    public ForwardRowFile(Context context, View root, ForwardListAdapter adapter) {
        super(context, root, adapter);
    }

    @Override
    protected void onFindViewById() {
        bubbleLayout = rootView.findViewById(R.id.chat_message_layout);
        iv_file_type = rootView.findViewById(R.id.iv_file_type);
        tv_file_name = rootView.findViewById(R.id.tv_file_name);
        tv_file_size = rootView.findViewById(R.id.tv_file_size);
        pb_file = rootView.findViewById(R.id.pb_file);
    }

    @Override
    protected void onSetUpView() {
        tv_file_name.setText(chatLog.msg.fileName);
        tv_file_size.setText(mContext.getString(R.string.chat_file_size, ToolUtils.byte2Mb(chatLog.msg.fileSize)));
        String fileNameExtension = FileUtils.getExtension(chatLog.msg.fileName);
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
        bubbleLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (chatLog.msg.getLocalPath() == null || !new File(chatLog.msg.getLocalPath()).exists()) {
                    if (chatLog.msg.getLocalPath() != null) {
                        chatLog.msg.setLocalPath(null);
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
                    doDownloadWork(false);
                } else {
                    ARouter.getInstance().build(AppRoute.FILE_DETAIL)
                            .withSerializable("message", message)
                            .withSerializable("chatLog", chatLog)
                            .navigation();
                }
            }
        });
        if (chatLog.msg.getLocalPath() == null || !new File(chatLog.msg.getLocalPath()).exists()) {
            // 自动下载文件
            doDownloadWork(true);
        }
    }

    @AfterPermissionGranted(SAVE_FILE)
    private void doDownloadWork(boolean auto) {
        if (PermissionUtil.hasWriteExternalPermission()) {
            File folder = new File(mContext.getFilesDir().getPath() + "/download/file");
            if (!folder.exists()) {
                folder.mkdirs();
            }
            FileDownloadManager.INS.download(folder, chatLog, new FileDownloadManager.DownloadCallback() {
                @Override
                public void onStart() {
                    pb_file.setMaxValue(100);
                    if (pb_file.getVisibility() == View.GONE) {
                        pb_file.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onAlreadyRunning() {
                    if (!auto) {
                        ShowUtils.showSysToast(mContext, R.string.chat_tips_downloading);
                    }
                }

                @Override
                public void onProgress(float progress) {
                    pb_file.setProgress((int) (progress * 100));
                }

                @Override
                public void onFinish(File file, Throwable throwable) {
                    pb_file.setVisibility(View.GONE);
                    if (file != null) {
                        chatLog.msg.setLocalPath(file.getAbsolutePath());
                        if (!auto) {
                            ShowUtils.showSysToast(mContext, mContext.getString(R.string.chat_tips_file_download_to, file.getAbsolutePath()));
                        }
                    } else {
                        chatLog.msg.setLocalPath(null);
                        if (!auto) {
                            ShowUtils.showSysToast(mContext, R.string.chat_tips_file_download_fail);
                        }
                    }
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
        } else {
            EasyPermissions.requestPermissions((Activity) mContext, mContext.getString(R.string.chat_error_permission_storage),
                    SAVE_FILE, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
    }
}
