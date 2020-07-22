package com.fuzamei.common.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Created by zhengfan on 2017/8/23.
 * Explain
 */
public class CodeUtils {

    public static String getRandomString() {

        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i <= 3; i++) {
            int randomInt = new SecureRandom().nextInt(57);
            String string = code[randomInt];
            buffer.append(string);
        }
        return buffer.toString();
    }

    //58位数组
    private static final String[] code = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F", "G", "H", "J", "K", "L", "M", "N", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"};



    public static String md5(String password) {
        byte[] bytes = null;
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(password.getBytes());  //更新摘要
            bytes = digest.digest(); //再通过执行诸如填充之类的最终操作完成哈希计算。在调用此方法之后，摘要被重置。
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            /**
             * 0xFF默认是整形，一个byte跟0xFF相与会先将那个byte转化成整形运算
             */
            if ((b & 0xFF) < 0x10) {  //如果为1位 前面补个0
                builder.append("0");
            }
            builder.append(Integer.toHexString(b & 0xFF));
        }

        return builder.toString();
    }
}
