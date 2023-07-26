package com.czw.bluetoothlib.util;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * @author chenxiaojin
 * @date 2020/5/9
 * @description
 */
public class ByteUtil {
    // 十六进制字符
    private static final char[] HEX_CHAR = {'0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    /**
     * 字节数组转16进制字符
     *
     * @param bytes
     * @return
     */
    public static String bytesToHex(byte[] bytes) {
        // 一个byte为8位，可用两个十六进制位标识
        char[] buf = new char[bytes.length * 2];
        int a = 0;
        int index = 0;
        for (byte b : bytes) { // 使用除与取余进行转换
            if (b < 0) {
                a = 256 + b;
            } else {
                a = b;
            }

            buf[index++] = HEX_CHAR[a / 16];
            buf[index++] = HEX_CHAR[a % 16];
        }

        return new String(buf);
    }

    public static String string2Unicode(String word) {
        StringBuffer unicode = new StringBuffer();
        for (int i = 0; i < word.length(); i++) {
            // 取出每一个字符
            char c = word.charAt(i);
            // 转换为unicode, 实测不需要\\u
            unicode.append("\\u" + Integer.toHexString(c));
        }
        return unicode.toString();
    }

    /**
     * char转为16bit unicode
     * 高位在前, 低位在后
     *
     * @param word
     * @return
     */
    public static byte[] chatTo16bitUnicodeWithBytes(char word) {
        byte[] data = hexStringToByteArray(Integer.toHexString(word));
        for (int i = 0; i < data.length; i++) {
            Log.e("ByteUtil", "unicode:" + Integer.toHexString(data[i]));
        }

        if (data.length == 1) {
            data = new byte[]{0x00, data[0]};
        }
        return data;
    }


    /**
     * 16进制转字节数组
     *
     * @param s
     * @return
     */
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }


    public static byte[] sumCheck(byte[] data) {
        int sum = 0;
        for (int i = 0; i < data.length; i++) {
            sum = sum + data[i];
        }
        if (sum > 0xff) { //超过了255，使用补码（补码 = 原码取反 + 1）
            sum = ~sum;
            sum = sum + 1;
        }
        return new byte[]{(byte) (sum & 0xff)};

    }

    /**
     * 字节数组转16进制字符串 中间中 空格符 分割
     *
     * @param bytes
     * @return
     */
    public static String bytesToHexSegment(byte[] bytes) {
        if (bytes == null) {
            return "null";
        }
        // 一个byte为8位，可用两个十六进制位标识
        char[] buf = new char[bytes.length * 2];
        int a = 0;
        int index = 0;
        StringBuffer stringBuffer = new StringBuffer();

        try {
            for (byte b : bytes) { // 使用除与取余进行转换
                if (b < 0) {
                    a = 256 + b;
                } else {
                    a = b;
                }

                buf[index++] = HEX_CHAR[a / 16];
                stringBuffer.append(buf[index - 1]);
                buf[index++] = HEX_CHAR[a % 16];
                stringBuffer.append(buf[index - 1]);
                stringBuffer.append(" ");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stringBuffer.toString();
    }

    /**
     * 字节数组转化成集合
     */
    public static List<Integer> bytesToArrayList(byte[] bytes) {
        List<Integer> datas = new ArrayList<>();
        if (bytes != null) {
            for (int i = 0; i < bytes.length; i++) {
                datas.add(bytes[i] & 0xff);
            }
        }
        return datas;
    }


    public static int bytes2Int(byte[] bytes) {
        //如果不与0xff进行按位与操作，转换结果将出错，有兴趣的同学可以试一下。
        int int1 = bytes[3] & 0xff;
        int int2 = (bytes[2] & 0xff) << 8;
        int int3 = (bytes[1] & 0xff) << 16;
        int int4 = (bytes[0] & 0xff) << 24;

        return int1 + int2 + int3 + int4;
    }

    public static int bytes2Short(byte[] bytes) {
        int int1 = (bytes[1] & 0xff);
        int int2 = (bytes[0] & 0xff) << 8;
        return int1 + int2;
    }

    /**
     * 十六进制String转换成Byte[] * @param hexString the hex string * @return byte[]
     */
    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        if (hexString.length() % 2 != 0) {
            hexString = "0" + hexString;
        }
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }


    public static String bytesToHex1(byte byteData) {
        // 一个byte为8位，可用两个十六进制位标识
        char[] buf = new char[2];
        int a = 0;
        int index = 0;
        if (byteData < 0) {
            a = 256 + byteData;
        } else {
            a = byteData;
        }

        buf[index++] = HEX_CHAR[a / 16];
        buf[index++] = HEX_CHAR[a % 16];
        return new String(buf);
    }


    /**
     * Convert char to byte * @param c char * @return byte
     */
    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    public static void main(String[] args) {
        byte[] data = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x0a, (byte) 0x20};
        long l = bytes2Int(data);
        System.out.print(l);
    }


}
