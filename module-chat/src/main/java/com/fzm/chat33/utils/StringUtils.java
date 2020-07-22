package com.fzm.chat33.utils;

import android.text.TextUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author zhengjy
 * @since 2018/12/28
 * Description:
 */
public class StringUtils {
    /**
     * 判断是不是一个1开头11位的手机号码
     *
     * @param mobiles 手机号
     */
    public static boolean isMobileNO(String mobiles) {
        Pattern p = Pattern.compile("^((1[0-9][0-9]))\\d{8}$");
        Matcher m = p.matcher(mobiles);
        return m.matches();
    }

    public static String formatMutedTime(long millisUntilFinished){
        StringBuilder builder = new StringBuilder();
        String str = "";
        long hour = millisUntilFinished/(3600*1000);
        String hourStr = "";
        if(hour==0){
            hourStr = "00";
        }else if(hour>0 && hour<10){
            hourStr = "0"+hour;
        }else {
            hourStr = ""+hour;
        }

        long min = (millisUntilFinished/(60*1000))%60;
        String minStr = "";
        if(min==0){
            minStr = "00";
        }else if(min>0 && min<10){
            minStr = "0"+min;
        }else {
            minStr = ""+min;
        }

        long mil = (millisUntilFinished%(60*1000))/1000;
        String milStr = "";
        if(mil==0){
            milStr = "00";
        }else if(min>0 && mil<10){
            milStr = "0"+mil;
        }else {
            milStr = ""+mil;
        }
        builder.append(hourStr);
        builder.append(":");
        builder.append(minStr);
        builder.append(":");
        builder.append(milStr);
        str = builder.toString();
        return str;
    }

    public static String formateTime(long millisUntilFinished) {
        StringBuilder builder = new StringBuilder();
        String str = "";
        long hour = millisUntilFinished / (3600 * 1000);
        String hourStr = "";
        if (hour == 0) {

        } else if (hour > 0 && hour < 10) {
            hourStr = "0" + hour;
        } else {
            hourStr = "" + hour;
        }

        long min = (millisUntilFinished / (60 * 1000)) % 60;
        String minStr = "";
        if (min == 0) {

        } else if (min > 0 && min < 10) {
            minStr = "0" + min;
        } else {
            minStr = "" + min;
        }

        long mil = (millisUntilFinished % (60 * 1000)) / 1000;
        String milStr = "";
        if (mil == 0) {
            milStr = "0";
        } else if (min > 0 && mil < 10) {
            milStr = "0" + mil;
        } else {
            milStr = "" + mil;
        }
        builder.append(hourStr);
        if (!"".equals(hourStr)) {
            builder.append(":");
        }
        builder.append(minStr);
        if (!"".equals(minStr)) {
            builder.append(":");
        }
        builder.append(milStr);
        str = builder.toString();
        return str;
    }

    public static String aliyunFormat(String url, int width, int height) {
        String formatUrl = "?x-oss-process=image/resize,m_mfit,h_" + height + ",w_" + width + "/quality,q_70/format,jpg/interlace,1";
        if (TextUtils.isEmpty(url)) {
            return "";
        } else {
            return url + formatUrl;
        }
    }

    private static boolean checkPasswordLength(String password){
        if (password.length() > 16 || password.length() < 8) {
            return false;
        }
        return true;
    }

    public static boolean isEncryptPassword(String password) {
        if(!checkPasswordLength(password)) {
            return false;
        }
        Pattern p = Pattern.compile("^(?![0-9]+$)(?![a-zA-Z]+$)(?!\\W+$)(([\\w])|([a-zA-Z_\\W+])|([0-9_\\W+])){8,16}$");

        Matcher m = p.matcher(password);

        return m.matches();
    }
}
