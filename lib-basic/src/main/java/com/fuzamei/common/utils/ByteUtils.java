package com.fuzamei.common.utils;

/**
 * @author zhengjy
 * @since 2019/05/22
 * Description:
 */
public class ByteUtils {

    public static String bytes2Hex(byte[] data) {
        StringBuilder stringBuilder = new StringBuilder();
        for (byte b : data) {
            int v = b & 0xFF;
            String hex = Integer.toHexString(v);
            if (hex.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hex);
        }
        return stringBuilder.toString();
    }

    public static byte[] hex2Bytes(String data) {
        if (data.startsWith("0x") || data.startsWith("0X")) {
            data = data.substring(2);
        }
        String upper = data.toUpperCase();
        int length = upper.length() / 2;
        char[] hexChars = upper.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }
}
