package com.fzm.chat33.core.utils;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PinyinUtils {
    /**
     * 获取拼音
     *
     * @param inputString
     * @return
     */
    public static String getPingYin(String inputString) {
        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        format.setVCharType(HanyuPinyinVCharType.WITH_V);

        char[] input = inputString.trim().toCharArray();
        String output = "";

        try {
            for (char curChar : input) {
                if (Character.toString(curChar).matches("[\\u4E00-\\u9FA5]+")) {
                    String[] temp = PinyinHelper.toHanyuPinyinStringArray(curChar, format);
                    if (temp == null) {
                        return "#";
                    }
                    output += temp[0];
                } else
                    output += Character.toString(curChar);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "#";
        }
        return output;
    }

    /**
     * 获取单个字符拼音
     *
     * @param curChar
     * @return
     */
    public static String getCharPingYin(char curChar) {
        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        format.setVCharType(HanyuPinyinVCharType.WITH_V);

        String output = "";

        try {
            if (Character.toString(curChar).matches("[\\u4E00-\\u9FA5]+")) {
                String[] temp = PinyinHelper.toHanyuPinyinStringArray(curChar, format);
                if (temp == null) {
                    return "#";
                }
                output += temp[0];
            } else
                output += Character.toString(curChar);
        } catch (Exception e) {
            e.printStackTrace();
            return "#";
        }
        return output;
    }

    /**
     * 获取第一个字的拼音首字母
     * @param chinese
     * @return
     */
    public static String getFirstSpell(String chinese) {
        StringBuffer pinYinBF = new StringBuffer();
        char[] arr = chinese.toCharArray();
        HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
        defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        for (char curChar : arr) {
            if (curChar > 128) {
                try {
                    String[] temp = PinyinHelper.toHanyuPinyinStringArray(curChar, defaultFormat);
                    if (temp != null) {
                        pinYinBF.append(temp[0].charAt(0));
                    }
                } catch (BadHanyuPinyinOutputFormatCombination e) {
                    e.printStackTrace();
                }
            } else {
                pinYinBF.append(curChar);
            }
        }
        return pinYinBF.toString().replaceAll("\\W", "").trim();
    }

    /**
     * 判断字符串中是否包含中文
     * @param str
     * 待校验字符串
     * @return 是否为中文
     * @warn 不能校验是否为中文标点符号
     */
    public static boolean isContainChinese(String str) {
        Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
        Matcher m = p.matcher(str);
        if (m.find()) {
            return true;
        }
        return false;
    }
}
