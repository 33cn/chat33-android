package com.fuzamei.common.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by mark on 2017/12/18.
 * Explain 日期转化工具类
 */
public class DateUtils {

    public static final long SECOND = 1000L;
    public static final long MINUTE = 60 * SECOND;
    public static final long HOUR = 60 * MINUTE;
    public static final long DAY = 24 * HOUR;

    public static final long QUIT_AWAIT_THRESHOLD = 2 * SECOND;

    public static String COMMON_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static String PATTERN_WITHOUT_TIME = "yyyy-MM-dd";
    public static String PATTERN_WITHOUT_DAY = "yyyy-MM";
    public static String PATTERN_WITHOUT_YEAR = "MM-dd HH:mm:ss";
    public static SimpleDateFormat sdf = new SimpleDateFormat(COMMON_PATTERN);

    /**
     * @param time 2017-09-25 15:25:15
     * @return
     * @throws ParseException
     */
    public static Date stringToDate(String time) throws ParseException {
        return sdf.parse(time);
    }

    public static Date stringToDate(String time, String pattern) throws ParseException {
        DateFormat df = new SimpleDateFormat(pattern);
        Date date = df.parse(time);
        return date;
    }

    public static String dateToString(Date date) {
        return sdf.format(date);
    }

    public static String dateToString(Date date, String pattern) {
        DateFormat df = new SimpleDateFormat(pattern);
        String strDate = df.format(date);
        return strDate;
    }

    /**
     * 字符时间转 10 位时间戳
     *
     * @param dateString 2017-09-25 15:25:15 格式
     * @return
     */
    public static long stringToTime(String dateString) throws ParseException {
        Date date = sdf.parse(dateString);
        return date.getTime() / 1000;
    }

    /**
     * 10 位时间戳转字符时间
     *
     * @param time
     * @param pattern
     * @return
     */
    public static String timeToStringForShort(long time, String pattern) {
        return timeToString(time * 1000, pattern);
    }

    public static String timeToStringForShort(long time) {
        return timeToString(time * 1000);
    }

    public static String timeToString(long time, String pattern) {
        DateFormat df = new SimpleDateFormat(pattern);
        Date date = new Date(time);
        return df.format(date);
    }

    public static String timeToString(long time) {
        return sdf.format(new Date(time));
    }

    /**
     * 比较日期大小  yyyy-MM-dd
     * @param date1
     * @param date2
     * @return 0是相等 1是date1大 -1是date1小
     */
    public static int compareDateWithoutTime(Date date1, Date date2) {
        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTime(date1);
        calendar1.set(Calendar.HOUR_OF_DAY, 0);
        calendar1.set(Calendar.MINUTE, 0);
        calendar1.set(Calendar.SECOND, 0);
        calendar1.set(Calendar.MILLISECOND, 0);

        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTime(date2);
        calendar2.set(Calendar.HOUR_OF_DAY, 0);
        calendar2.set(Calendar.MINUTE, 0);
        calendar2.set(Calendar.SECOND, 0);
        calendar2.set(Calendar.MILLISECOND, 0);

        if(calendar1.after(calendar2)) {
            return 1;
        } else if(calendar1.before(calendar2)) {
            return -1;
        }
        return 0;
    }

    /**
     * 比较日期大小  yyyy-MM-dd
     * @param str_date1
     * @param str_date2
     * @return 0是相等 1是str_date1大 -1是str_date1小
     */
    public static int compareDateStringWithoutTime(String str_date1, String str_date2) throws Exception {
        Date date1 = stringToDate(str_date1, PATTERN_WITHOUT_TIME);
        Date date2 = stringToDate(str_date2, PATTERN_WITHOUT_TIME);
        return compareDateWithoutTime(date1, date2);
    }

    /**
     * 获取指定日期所在月份开始的时间戳
     * @param c 指定日期
     * @return
     */
    public static long getMonthBegin(Calendar c) {

        //设置为1号,当前日期既为本月第一天
        c.set(Calendar.DAY_OF_MONTH, 1);
        //将小时至0
        c.set(Calendar.HOUR_OF_DAY, 0);
        //将分钟至0
        c.set(Calendar.MINUTE, 0);
        //将秒至0
        c.set(Calendar.SECOND,0);
        //将毫秒至0
        c.set(Calendar.MILLISECOND, 0);
        // 获取本月第一天的时间戳
        return c.getTimeInMillis();
    }

    /**
     * 获取指定日期所在月份结束的时间戳
     * @param c 指定日期
     * @return
     */
    public static long getMonthEnd(Calendar c) {

        //设置为当月最后一天
        c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
        //将小时至23
        c.set(Calendar.HOUR_OF_DAY, 23);
        //将分钟至59
        c.set(Calendar.MINUTE, 59);
        //将秒至59
        c.set(Calendar.SECOND,59);
        //将毫秒至999
        c.set(Calendar.MILLISECOND, 999);
        // 获取本月最后一天的时间戳
        return c.getTimeInMillis();
    }

    /**
     * 获取指定日期所在年份开始的时间戳
     * @param c 指定日期
     * @return
     */
    public static long getYearBegin(Calendar c) {

        c.set(Calendar.MONTH, 0);
        //设置为1号,当前日期既为本月第一天
        c.set(Calendar.DAY_OF_MONTH, 1);
        //将小时至0
        c.set(Calendar.HOUR_OF_DAY, 0);
        //将分钟至0
        c.set(Calendar.MINUTE, 0);
        //将秒至0
        c.set(Calendar.SECOND,0);
        //将毫秒至0
        c.set(Calendar.MILLISECOND, 0);
        // 获取本月第一天的时间戳
        return c.getTimeInMillis();
    }

    /**
     * 获取指定日期所在年份结束的时间戳
     * @param c 指定日期
     * @return
     */
    public static long getYearEnd(Calendar c) {

        // 设置为12月
        c.set(Calendar.MONTH, 11);
        //设置为当月最后一天
        c.set(Calendar.DAY_OF_MONTH, 31);
        //将小时至23
        c.set(Calendar.HOUR_OF_DAY, 23);
        //将分钟至59
        c.set(Calendar.MINUTE, 59);
        //将秒至59
        c.set(Calendar.SECOND,59);
        //将毫秒至999
        c.set(Calendar.MILLISECOND, 999);
        // 获取本月最后一天的时间戳
        return c.getTimeInMillis();
    }

    /**
     * 获取指定日期所属的周一
     *
     * @param date
     * @return
     */
    public static Date getWeekMonday(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        // 获得当前日期是一个星期的第几天
        int dayWeek = cal.get(Calendar.DAY_OF_WEEK);
        if (1 == dayWeek) {
            // 如果当前时间是星期天，则向上移动一周，再取本周的星期一
            cal.add(Calendar.WEEK_OF_YEAR, -1);
        }
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        return cal.getTime();
    }

    /**
     * 获取指定日期所属的周日
     *
     * @param date
     * @return
     */
    public static Date getWeekSunday(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        // 获得当前日期是一个星期的第几天
        int dayWeek = cal.get(Calendar.DAY_OF_WEEK);
        if (1 != dayWeek) {
            // 如果当前时间不是星期天，则向上移动一周，再取本周的星期天
            cal.add(Calendar.WEEK_OF_YEAR, 1);
        }
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        return cal.getTime();
    }
}