package com.fzm.chat33.hepler;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.fuzamei.common.utils.SharedPrefUtil;
import com.fuzamei.componentservice.config.AppConfig;
import com.fzm.chat33.R;
import com.fzm.chat33.core.Chat33;
import com.fzm.chat33.core.db.bean.BriefChatLog;
import com.fzm.chat33.core.db.bean.ChatMessage;
import com.fzm.chat33.utils.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author zhengjy
 * @since 2019/02/11
 * Description:下载聊天文件
 */
public enum FileDownloadManager {

    /**
     * 单例
     */
    INS;

    private static final String TEMP_FILE_SUFFIX = ".temp";

    private int oldProgress = 0;
    private Map<String, String> runningTasks = new ConcurrentHashMap<>();
    private Map<String, Call> runningCalls = new ConcurrentHashMap<>();

    FileDownloadManager() {

    }

    public void download(File folder, ChatMessage message, DownloadCallback callback) {
        String url, name;
        if (message.msgType == ChatMessage.Type.VIDEO) {
            url = message.msg.getMediaUrl();
            name = "video_" + UUID.randomUUID().toString()
                    + "." + FileUtils.getExtension(message.msg.getMediaUrl());
        } else if (message.msgType == ChatMessage.Type.FILE) {
            url = message.msg.fileUrl;
            name = message.msg.fileName;
        } else {
            url = message.msg.getImageUrl();
            name = "image_" + UUID.randomUUID().toString()
                    + "." + FileUtils.getExtension(message.msg.getImageUrl());
        }
        download(url, folder, name, message.msg.md5, "tempPath_" + message.logId, callback);
    }

    public void download(File folder, BriefChatLog chatLog, DownloadCallback callback) {
        String url, name;
        if (chatLog.msgType == ChatMessage.Type.VIDEO) {
            url = chatLog.msg.getMediaUrl();
            name = "video_" + UUID.randomUUID().toString()
                    + "." + FileUtils.getExtension(chatLog.msg.getMediaUrl());
        } else if (chatLog.msgType == ChatMessage.Type.FILE) {
            url = chatLog.msg.fileUrl;
            name = chatLog.msg.fileName;
        } else {
            url = chatLog.msg.getImageUrl();
            name = "image_" + UUID.randomUUID().toString()
                    + "." + FileUtils.getExtension(chatLog.msg.getImageUrl());
        }
        download(url, folder, name, chatLog.msg.md5, "tempPath_" + chatLog.logId, callback);
    }

    public void download(String url, File folder, String fileName, String md5, String tag, DownloadCallback callback) {
        String finalLocalName;
        Call call;
        long downloadLength = 0;
        if (TextUtils.isEmpty(url) || TextUtils.isEmpty(fileName)) {
            return;
        }
        fileName = AppConfig.ENC_PREFIX + fileName;
        synchronized (this) {
            if (runningTasks.containsKey(url)) {
                if (callback != null) {
                    callback.onAlreadyRunning();
                }
                return;
            }
            String localName = SharedPrefUtil.getInstance().getStringPref(tag, "");
            if (!TextUtils.isEmpty(localName) && new File(folder, localName).exists()) {
                // 如果原先下载到一半的文件本地文件名被占用，则重新分配文件名
                SharedPrefUtil.getInstance().setStringPref(tag, "");
                localName = null;
            }
            if (TextUtils.isEmpty(localName)) {
                localName = fileName;
                for (int count = 1; ; count++) {
                    File local = new File(folder, localName);
                    if (local.exists()) {
                        String localMd5 = FileUtils.getFileMD5(local);
                        if (md5 != null && md5.equalsIgnoreCase(localMd5)) {
                            if (callback != null) {
                                // 如果本地同名相同md5文件存在，则不重复下载
                                callback.onFinish(local, null);
                            }
                            return;
                        } else {
                            // 如果md5不同，则继续增加文件后缀数字
                            localName = increaseFileNumber(fileName, count);
                        }
                    } else {
                        // 如果本地文件不存在，则开始正常下载文件
                        break;
                    }
                }
            } else {
                // 断点续传点
//                File temp = new File(folder, localName + TEMP_FILE_SUFFIX);
//                if (temp.exists()) {
//                    downloadLength = temp.length();
//                }
            }
            finalLocalName = localName;
            SharedPrefUtil.getInstance().setStringPref(tag, localName);

            if (callback != null) {
                callback.onStart();
            }
            runningTasks.put(url, localName);
            OkHttpClient client = new OkHttpClient.Builder().readTimeout(30, TimeUnit.SECONDS)
                    .connectTimeout(30, TimeUnit.SECONDS).build();
            Request request = new Request.Builder().get().url(url)
                    .addHeader("Range", "bytes=" + downloadLength + "-")
                    .tag(tag).build();
            call = client.newCall(request);
            runningCalls.put(url, call);
        }
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull final IOException e) {
                runningTasks.remove(url);
                runningCalls.remove(url);
                SharedPrefUtil.getInstance().setStringPref(tag, "");
                AndroidSchedulers.mainThread().createWorker().schedule(new Runnable() {
                    @Override
                    public void run() {
                        if (callback != null) {
                            callback.onFinish(null, e);
                        }
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    final File file = saveFile(response, folder, finalLocalName, callback);
                    runningTasks.remove(url);
                    runningCalls.remove(url);
                    SharedPrefUtil.getInstance().setStringPref(tag, "");
                    AndroidSchedulers.mainThread().createWorker().schedule(new Runnable() {
                        @Override
                        public void run() {
                            if (callback != null) {
                                callback.onFinish(file, null);
                            }
                        }
                    });
                } else {
                    onFailure(call, new IOException(Chat33.getContext().getString(R.string.basic_error_request)));
                }
            }
        });
    }

    public void cancel(String key) {
        if (key == null) {
            return;
        }
        Call call = runningCalls.get(key);
        if (call != null && !call.isCanceled()) {
            call.cancel();
        }
        runningCalls.remove(key);
        runningTasks.remove(key);
    }

    public void clear() {
        runningTasks.clear();
        for (Call call : runningCalls.values()) {
            if (call != null && !call.isCanceled()) {
                call.cancel();
            }
        }
        runningCalls.clear();
    }

    private String increaseFileNumber(String name, int count) {
        String fileName;
        int dotPos = name.lastIndexOf('.');
        if (dotPos == -1) {
            fileName = name + "(" + count + ")";
        } else {
            fileName = name.substring(0, dotPos) + "(" + count + ")" + name.substring(dotPos);
        }
        return fileName;
    }

    /**
     * 将Response解析转换成File
     */
    public File saveFile(Response response, File folder, String fileName, DownloadCallback callback) throws IOException {
        if (response.body() == null) {
            return null;
        }
        long sum = 0;
        File file = new File(folder, fileName + TEMP_FILE_SUFFIX);
        InputStream is = response.body().byteStream();
        FileOutputStream fos = new FileOutputStream(file);
        final long total = response.body().contentLength();
        byte[] buf = new byte[2048];
        int len = 0;
        while ((len = is.read(buf)) != -1) {
            sum += len;
            fos.write(buf, 0, len);
            final long finalSum = sum;
            int progress = (int) (finalSum * 1.0f / total * 100);
            if (progress != oldProgress) {
                // 这里判断下，当进度值发生变化时再去更新
                oldProgress = progress;
                AndroidSchedulers.mainThread().createWorker().schedule(new Runnable() {
                    @Override
                    public void run() {
                        if (callback != null) {
                            callback.onProgress(finalSum * 1.0f / total);
                        }
                    }
                });
            }
        }
        File dest = new File(folder, fileName);
        file.renameTo(dest);
        fos.flush();
        response.body().close();
        is.close();
        fos.close();
        return dest;
    }

    public interface DownloadCallback {

        void onStart();

        void onAlreadyRunning();

        void onProgress(float progress);

        void onFinish(File file, Throwable throwable);
    }
}
