package com.fzm.chat33.hepler;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.StrictMode;
import android.provider.MediaStore;

import com.fzm.chat33.R;
import com.fzm.chat33.core.db.bean.BriefChatLog;
import com.fzm.chat33.core.db.bean.ChatMessage;

import java.io.File;
import java.lang.reflect.Method;

import pub.devrel.easypermissions.AfterPermissionGranted;

/**
 * @author zhengjy
 * @since 2019/05/16
 * Description:
 */
public class ShareHelper {

    private static final int SAVE_FILE = 1;

    @AfterPermissionGranted(SAVE_FILE)
    public static void shareFile(Activity activity, ChatMessage message) {
        Uri fileUri = null;
        File file = new File(message.msg.getLocalPath());
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setPackage("com.tencent.mm");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (message.msgType == ChatMessage.Type.VIDEO) {
                share.setType("video/*");
                fileUri = getVideoContentUri(activity, file);
            } else if (message.msgType == ChatMessage.Type.FILE) {
                share.setType("*/*");
                fileUri = getFileContentUri(activity, file);
            }
            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            fileUri = Uri.fromFile(file);
        }
        if (fileUri == null) {
            fileUri = forceGetFileUri(file);
        }
        share.putExtra(Intent.EXTRA_STREAM, fileUri);
        activity.startActivity(Intent.createChooser(share, activity.getString(R.string.chat_action_share)));
    }

    @AfterPermissionGranted(SAVE_FILE)
    public static void shareFile(Activity activity, BriefChatLog message) {
        Uri fileUri = null;
        File file = new File(message.msg.getLocalPath());
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setPackage("com.tencent.mm");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (message.msgType == ChatMessage.Type.VIDEO) {
                share.setType("video/*");
                fileUri = getVideoContentUri(activity, file);
            } else if (message.msgType == ChatMessage.Type.FILE) {
                share.setType("*/*");
                fileUri = getFileContentUri(activity, file);
            }
            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            fileUri = Uri.fromFile(file);
        }
        if (fileUri == null) {
            fileUri = forceGetFileUri(file);
        }
        share.putExtra(Intent.EXTRA_STREAM, fileUri);
        activity.startActivity(Intent.createChooser(share, activity.getString(R.string.chat_action_share)));
    }

    private static Uri getFileContentUri(Context context, File file) {
        String volumeName = "external";
        String filePath = file.getAbsolutePath();
        String[] projection = new String[]{MediaStore.Files.FileColumns._ID};
        Uri uri = null;

        Cursor cursor = context.getContentResolver().query(MediaStore.Files.getContentUri(volumeName), projection,
                MediaStore.Images.Media.DATA + "=? ", new String[] { filePath }, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int id = cursor.getInt(cursor.getColumnIndex(MediaStore.Files.FileColumns._ID));
                uri = MediaStore.Files.getContentUri(volumeName, id);
            }
            cursor.close();
        }

        return uri;
    }

    private static Uri getVideoContentUri(Context context, File videoFile) {
        Uri uri = null;
        String filePath = videoFile.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Video.Media._ID }, MediaStore.Video.Media.DATA + "=? ",
                new String[] { filePath }, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
                Uri baseUri = Uri.parse("content://media/external/video/media");
                uri = Uri.withAppendedPath(baseUri, "" + id);
            }

            cursor.close();
        }

        if (uri == null) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Video.Media.DATA, filePath);
            uri = context.getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
        }

        return uri;
    }

    private static Uri forceGetFileUri(File shareFile) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                @SuppressLint("PrivateApi")
                Method rMethod = StrictMode.class.getDeclaredMethod("disableDeathOnFileUriExposure");
                rMethod.invoke(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return Uri.parse("file://" + shareFile.getAbsolutePath());
    }
}
