package com.wetoop.storeoperator.tools;

/**
 * Created by User on 2018/2/5.
 */

public class ByteArrayChange {
    public static  String ByteArrayToHexString(byte[] bytesId) {   //Byte数组转换为16进制字符串
        // TODO 自动生成的方法存根
        int i, j, in;
        String[] hex = {
                "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"
        };
        String output = "";

        for (j = 0; j < bytesId.length; ++j) {
            in = bytesId[j] & 0xff;
            i = (in >> 4) & 0x0f;
            output += hex[i];
            i = in & 0x0f;
            output += hex[i];
        }
        return output;
    }
}
