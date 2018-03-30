package com.wetoop.storeoperator.utils;

/**
 * Created by User on 2018/3/14.
 */

public class NfcConstant {
    public static byte[] SELECTHEAD = new byte[]{
            (byte) 0x00, // CLA Class
            (byte) 0xA4, // INS Instruction
            (byte) 0x04, // P1  Parameter 1
            (byte) 0x00, // P2  Parameter 2
            (byte) 0x07, // Length
    };
    public static byte[] SELECT1 = new byte[]{
            (byte) 0x00, // CLA Class
            (byte) 0xA4, // INS Instruction
            (byte) 0x00, // P1  Parameter 1
            (byte) 0x0C, // P2  Parameter 2
            (byte) 0x02, // Length
            (byte) 0xE1, (byte) 0x03
    };
    public static byte[] SELECT2 = new byte[]{
            (byte) 0x00,
            (byte) 0xB0,
            (byte) 0x00,
            (byte) 0x00,
            (byte) 0x0F,
    };
    public static byte[] SELECT3 = new byte[]{
            (byte) 0x00, // CLA Class
            (byte) 0xA4, // INS Instruction
            (byte) 0x00, // P1  Parameter 1
            (byte) 0x0C, // P2  Parameter 2
            (byte) 0x02, // Length
            (byte) 0xE1, (byte) 0x04
    };
    public static byte[] SELECT4 = new byte[]{
            (byte) 0x00,
            (byte) 0xB0,
            (byte) 0x00,
            (byte) 0x00,
            (byte) 0x02,
    };
    public static byte[] SELECT5 = new byte[]{
            (byte) 0x00,
            (byte) 0xB0,
            (byte) 0x00,
            (byte) 0x02,
            (byte) 0x08
    };
}
