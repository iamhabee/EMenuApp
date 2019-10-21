package com.arke.sdk.util.data;

import java.io.UnsupportedEncodingException;

/**
 * Bytes utils.
 */

public class BytesUtil {

    /**
     * Change byte to hex string data.
     */
    public static String byte2HexString(byte data) {
        StringBuilder buffer = new StringBuilder();
        String hex = Integer.toHexString(data & 0xFF);
        if (hex.length() == 1) {
            buffer.append('0');
        }
        buffer.append(hex);
        return buffer.toString().toUpperCase();
    }

    /**
     * Change bytes to hex string data.
     */
    public static String bytes2HexString(byte[] data) {
        StringBuilder buffer = new StringBuilder();
        for (byte b : data) {
            String hex = Integer.toHexString(b & 0xFF);
            if (hex.length() == 1) {
                buffer.append('0');
            }
            buffer.append(hex);
        }
        return buffer.toString().toUpperCase();
    }

    /**
     * Change hex string data to bytes.
     */
    public static byte[] hexString2Bytes(String data) {
        byte[] result = new byte[(data.length() + 1) / 2];
        if ((data.length() & 0x1) == 1) {
            data = data + "0";
        }
        for (int i = 0; i < result.length; i++) {
            result[i] = ((byte) (hex2byte(data.charAt(i * 2 + 1)) | hex2byte(data.charAt(i * 2)) << 4));
        }
        return result;
    }

    /**
     * Change hex char to byte.
     */
    public static byte hex2byte(char hex) {
        if ((hex <= 'f') && (hex >= 'a')) {
            return (byte) (hex - 'a' + 10);
        }

        if ((hex <= 'F') && (hex >= 'A')) {
            return (byte) (hex - 'A' + 10);
        }

        if ((hex <= '9') && (hex >= '0')) {
            return (byte) (hex - '0');
        }

        return 0;
    }

    /**
     * Sub bytes.
     */
    public static byte[] subBytes(byte[] data, int offset, int len) {
        if ((offset < 0) || (data.length <= offset)) {
            return null;
        }

        if ((len < 0) || (data.length < offset + len)) {
            len = data.length - offset;
        }

        byte[] ret = new byte[len];

        System.arraycopy(data, offset, ret, 0, len);
        return ret;
    }

    /**
     * Change bytes to sring data.
     */
    public static String fromBytes(byte[] data, String charsetName) throws UnsupportedEncodingException {
        return new String(data, charsetName);
    }

    /**
     * Change byte array to GBK string data.
     */
    public static String fromGBK(byte[] data) throws UnsupportedEncodingException {
        return fromBytes(data, "GBK");
    }

    /**
     * Merge bytes.
     */
    public static byte[] mergeBytes(byte[] bytesA, byte[] bytesB) {
        if ((bytesA == null) || (bytesA.length == 0))
            return bytesB;
        if ((bytesB == null) || (bytesB.length == 0)) {
            return bytesA;
        }

        byte[] bytes = new byte[bytesA.length + bytesB.length];

        System.arraycopy(bytesA, 0, bytes, 0, bytesA.length);
        System.arraycopy(bytesB, 0, bytes, bytesA.length, bytesB.length);

        return bytes;
    }

    /**
     * Merge byte array.
     */
    public static byte[] merge(byte[]... data) {
        if (data == null) {
            return null;
        }

        byte[] bytes = null;
        for (byte[] aData : data) {
            bytes = mergeBytes(bytes, aData);
        }

        return bytes;
    }

    /**
     * Compare bytes.
     */
    public static int bytecmp(byte[] hex1, byte[] hex2, int len) {
        for (int i = 0; i < len; i++) {
            if (hex1[i] != hex2[i]) {
                return 1;
            }
        }

        return 0;
    }

    /**
     * Change hex string data to byte array.
     */
    public static byte[] hexString2ByteArray(String hexStr) {
        if (hexStr == null) return null;
        if (hexStr.length() % 2 != 0) {
            return null;
        }
        byte[] data = new byte[hexStr.length() / 2];
        for (int i = 0; i < hexStr.length() / 2; i++) {
            char hc = hexStr.charAt(2 * i);
            char lc = hexStr.charAt(2 * i + 1);
            byte hb = hexChar2Byte(hc);
            byte lb = hexChar2Byte(lc);
            if ((hb < 0) || (lb < 0)) {
                return null;
            }
            int n = hb << 4;
            data[i] = ((byte) (n + lb));
        }
        return data;
    }

    /**
     * Change hex char to byte data.
     */
    public static byte hexChar2Byte(char c) {
        if ((c >= '0') && (c <= '9')) return (byte) (c - '0');
        if ((c >= 'a') && (c <= 'f')) return (byte) (c - 'a' + 10);
        if ((c >= 'A') && (c <= 'F')) return (byte) (c - 'A' + 10);
        return -1;
    }

    /**
     * Change byte array to hex string data.
     */
    public static String byteArray2HexString(byte[] arr) {
        StringBuilder sb = new StringBuilder();
        for (byte anArr : arr) {
            sb.append(String.format("%02x", anArr).toUpperCase());
        }
        return sb.toString();
    }

    /**
     * Change int type data to four places byte array.
     */
    public static byte[] toFourByteArray(int i) {
        byte[] array = new byte[4];
        array[0] = (byte) (i >> 24 & 0x7F);
        array[1] = (byte) (i >> 16);
        array[2] = (byte) (i >> 8);
        array[3] = (byte) i;
        return array;
    }

    /**
     * Change long type data to BCD bytes.
     */
    public static byte[] toBCDAmountBytes(long data) {
        byte[] bcd = {0, 0, 0, 0, 0, 0};
        byte[] bcdDou = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        if (data <= 0) {
            return bcd;
        }

        int i = bcdDou.length - 1;

        while (data != 0) {
            bcdDou[i] = (byte) (data % 10);
            data /= 10;
            i--;
        }

        for (i = bcd.length - 1; i >= 0; i--) {
            bcd[i] = (byte) (((bcdDou[i * 2 + 1] & 0x0f)) | ((bcdDou[i * 2] << 4) & 0xf0));
        }

        return bcd;
    }
}
