package com.fuzamei.common.utils;

import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.StringRes;

import com.fuzamei.common.FzmFramework;
import com.fuzamei.commonlib.R;


/**
 * Created by zhengfan on 2016/7/7.
 * Explain  封装的toast
 */
public class ShowUtils {


    private static Toast toast;

    /**
     * 显示正确之后的提示toast
     *
     * @param con
     * @param msgString
     */
    public static void showToastSuccess(Context con, String msgString) {
        if (con != null) {
            View toastView = getViewSuccess(con, msgString);
            if (toast != null) {
                toast.cancel();
            }
            toast = new Toast(con);
            toast.setView(toastView);
            toast.setDuration(Toast.LENGTH_SHORT);
            setToastPos(toast);
            toast.show();
        }
    }


    /**
     * 显示正确之后的提示toast
     *
     * @param con
     */
    public static void showToastSuccessWithTip(Context con, String titleString, String tipString) {
        if (con != null) {
            View toastView = getViewSuccessWithTip(con, titleString, tipString);
            if (toast != null) {
                toast.cancel();
            }
            toast = new Toast(con);
            toast.setView(toastView);
            toast.setDuration(Toast.LENGTH_SHORT);
            setToastPos(toast);
            toast.show();
        }
    }

    /**
     * 普通的提示toast
     *
     * @param con
     * @param msgString
     */
    public static void showToastNormal(Context con, String msgString) {
        if (con != null) {
            View toastView = getViewNormal(con, msgString);
            if (toast != null) {
                toast.cancel();
            }
            toast = new Toast(con);
            toast.setView(toastView);
            toast.setDuration(Toast.LENGTH_SHORT);
            setToastPos(toast);
            toast.show();
        }
    }

    public static void showToastNormal(Context con, int resId) {
        showToastNormal(con, con.getString(resId));
    }

    public static void showToastNormal(String msgString) {
        showToastNormal(FzmFramework.context, msgString);
    }

    private static View getViewSuccess(Context mContext, String msgString) {
        try {
            View toastView = LayoutInflater.from(mContext).inflate(
                    R.layout.basic_toast_view_success, null);
            toastView.setBackgroundResource(R.drawable.basic_bg_trans_70);
            TextView toastText = (TextView) toastView.findViewById(R.id.toast_tv_title);
            toastText.setText(msgString);
            return toastView;
        } catch (OutOfMemoryError e) {

        }
        return null;
    }


    private static View getViewSuccessWithTip(Context mContext, String title, String tip) {
        try {
            View toastView = LayoutInflater.from(mContext).inflate(
                    R.layout.basic_toast_view_successwithtip, null);
            toastView.setBackgroundResource(R.drawable.basic_bg_trans_70);
            TextView toast_tv_title = (TextView) toastView.findViewById(R.id.toast_tv_title);
            toast_tv_title.setText(title);

            TextView toast_tv_tip = (TextView) toastView.findViewById(R.id.toast_tv_tip);
            toast_tv_tip.setText(tip);
            return toastView;
        } catch (OutOfMemoryError e) {

        }
        return null;
    }


    private static View getViewNormal(Context mContext, String msgString) {
        try {
            View toastView = LayoutInflater.from(mContext).inflate(
                    R.layout.basic_toast_view_normal, null);
            toastView.setBackgroundResource(R.drawable.basic_bg_trans_70);
            TextView toastText = (TextView) toastView.findViewById(R.id.toast_tv_title);
            toastText.setText(msgString);
            return toastView;
        } catch (OutOfMemoryError e) {

        }
        return null;
    }

    public static void showToast(Context con, @StringRes int resId) {
        showToast(con, con.getString(resId));
    }
    /**
     * 错误之后的提示
     *
     * @param con
     * @param msgString
     */
    public static void showToast(Context con, String msgString) {
        if (con != null) {
            View toastView = getView(con, msgString);
            if (toast != null) {
                toast.cancel();
            }
            toast = new Toast(con);
            toast.setView(toastView);
            toast.setDuration(Toast.LENGTH_SHORT);
            setToastPos(toast);
            toast.show();
        }
    }

    public static void showToast(String msgString) {
        showToast(FzmFramework.context, msgString);
    }

    public static void showToast(int resId) {
        showToast(FzmFramework.context, FzmFramework.context.getString(resId));
    }

    private static View getView(Context mContext, String msgString) {
        try {
            View toastView = LayoutInflater.from(mContext).inflate(
                    R.layout.basic_toast_view, null);
            toastView.setBackgroundResource(R.drawable.basic_bg_red);
            TextView toastText = (TextView) toastView.findViewById(R.id.toast_tv_title);
            toastText.setText(msgString);
            return toastView;
        } catch (OutOfMemoryError e) {

        }
        return null;
    }

    //**********************************************************************


    private static void setToastPos(Toast toast) {
        if (toast != null) {
            toast.setGravity(Gravity.CENTER, 0, 0);
        }
    }

    public static Toast showSysToast(Context context, @StringRes int resId) {
        return showSysToast(context, context.getString(resId));
    }
    public static Toast showSysToast(Context context, String string) {
        if (context != null && !TextUtils.isEmpty(string)) {
            if (toast != null) {
                toast.cancel();
            }
            toast = Toast.makeText(context, string, Toast.LENGTH_SHORT);
            toast.show();
        }
        return toast;
    }

    public static Toast showSysToast(String string) {
        return showSysToast(FzmFramework.context, string);
    }

    public static Toast showSysToast(int resId) {
        return showSysToast(FzmFramework.context, FzmFramework.context.getString(resId));
    }

}
