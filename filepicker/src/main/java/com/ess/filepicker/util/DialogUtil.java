package com.ess.filepicker.util;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;
import androidx.appcompat.app.AlertDialog;

import com.ess.filepicker.R;

/**
 * DialogUtil
 * Created by 李波 on 2018/2/27.
 */

public class DialogUtil {

    /***
     * 显示权限拒绝提醒对话框
     */
    public static void showPermissionDialog(final Activity mActivity, String permission) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder
                .setMessage(mActivity.getString(R.string.file_picker_error_permission, permission))
                .setTitle(R.string.file_picker_tip)
                .setPositiveButton(R.string.file_picker_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_SETTINGS);
                        mActivity.startActivity(intent);
                    }
                })
                .setNegativeButton(R.string.file_picker_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }
}
