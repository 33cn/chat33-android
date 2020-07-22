package com.fuzamei.common.utils;

import android.text.TextUtils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.regex.Pattern;

/**
 * @author zhengjy
 *@since 2018/09/19
 * Description: 用于金融数据的格式化
 * 如果要进行精确的Double计算则使用{@link ArithUtils}
 */
public class FinanceUtils {

    private FinanceUtils() {
    }

    /**
     * 将Double转换成String，防止出现科学计数法
     *
     * @param num
     * @return
     */
    public static String getPlainNum(double num, int count) {
        return getMoney(num, false, count);
    }

    public static String getPlainNum(String num, int count) {
        if (num == null) {
            return "";
        }
        if (num.length() > 0 && matchDecimal(num)) {
            return num;
        }
        num = num.replace(",", "");
        return getMoney(Double.parseDouble(num), false, count);
    }

    public static String getAccuracy(int count) {
        if (count == 0) {
            return "1";
        }
        StringBuilder sb = new StringBuilder("0.");
        for (int i = 0; i < count; i++) {
            if (i == count - 1) {
                sb.append("1");
            } else {
                sb.append("0");
            }
        }
        return sb.toString();
    }

    /**
     * 将Double转换成String，防止出现科学计数法，并且格式为: 23,345.98
     *
     * @param num
     * @return
     */
    public static String getGroupNum(double num, int count) {
        return getMoney(num, true, count);
    }

    public static String getGroupNum(String num, int count) {
        if (num == null) {
            return "";
        }
        if (num.length() > 0 && matchDecimal(num)) {
            return num;
        }
        num = num.replace(",", "");
        return getMoney(Double.parseDouble(num), true, count);
    }

    private static String getMoney(double num, boolean group, int count) {
        NumberFormat nf = NumberFormat.getInstance();
        // 整数部分
        nf.setMaximumIntegerDigits(100);
        // 小数部分
        nf.setMaximumFractionDigits(100);
        nf.setGroupingUsed(group);
        String result = nf.format(num);
        return checkString(result, count);
    }

    /**
     * 去除末位多余0
     * @param num
     * @return
     */
    public static String stripZero(String num) {
        try {
            BigDecimal bigDecimal = new BigDecimal(num);
            return bigDecimal.stripTrailingZeros().toPlainString();
        } catch (Exception e) {
            e.printStackTrace();
            return "0";
        }
    }

    /**
     * 字符串截取小数点后number位
     * 末位不足，则补全 0
     *
     * @param number
     * @param count
     * @return
     */
    public static String checkString(String number, int count) {
        if (number.contains(".")) {
            if (count == 0) {
                number = number.substring(0, number.indexOf("."));
            } else if (number.length() - 1 - number.indexOf(".") >= count) {
                number = number.substring(0, number.indexOf(".") + count + 1);
            } else if (number.length() - 1 - number.indexOf(".") < count) {
                int add = count - (number.length() - 1 - number.indexOf("."));
                StringBuilder builder = new StringBuilder(number);
                for (int i = 0; i < add; i++) {
                    builder.append("0");
                }
                number = builder.toString();
            }
        } else {
            if (count > 0) {
                StringBuilder builder = new StringBuilder(number);
                builder.append(".");
                for (int i = 0; i < count; i++) {
                    builder.append("0");
                }
                number = builder.toString();
            }
        }
        return number;
    }

    /**
     * 保证EditText中的字符串符合格式
     *
     * @param num
     * @return
     */
    public static String formatEditText(String num) {
        if (num == null) {
            return "";
        }
        if (num.length() > 0 && matchDecimal(num)) {
            return num;
        }
        num = num.replace(",", "");
        return formatEditText(Double.parseDouble(num));
    }

    public static String formatEditText(double num) {
        NumberFormat nf = NumberFormat.getInstance();
        // 整数部分
        nf.setMaximumIntegerDigits(100);
        // 小数部分
        nf.setMaximumFractionDigits(100);
        nf.setGroupingUsed(true);
        return nf.format(num);
    }

    public static CharSequence formatString(CharSequence charSequence, int number) {
//        charSequence = charSequence.toString().replace(",", "");
        if ('.' == charSequence.charAt(charSequence.length() - 1)
                || "".contentEquals(charSequence)) {
            return charSequence;
        }
        int index = charSequence.toString().indexOf(".");
        if (index != -1) {
            if (charSequence.length() - index - 1 > number) {
                return charSequence.subSequence(0, index + number + 1);
            } else if (charSequence.length() > 0 && matchDecimal(charSequence.toString())) {
                return charSequence;
            }
        }
        StringBuilder sb = new StringBuilder("#");
        if (number > 0) {
            sb.append(".");
            for (int i = 1; i <= number; i++) {
                sb.append("#");
            }
        }
        DecimalFormat format = new DecimalFormat(sb.toString());
        return format.format(Double.parseDouble(charSequence.toString()));
    }


    /**
     * 符合如下条件的字符串小数不做格式化，eg: 0.000, 32.000
     *
     * @param num
     * @return
     */
    public static boolean matchDecimal(String num) {
        Pattern pattern = Pattern.compile("^\\d+\\.0*");
        return pattern.matcher(num).find();
    }
}
