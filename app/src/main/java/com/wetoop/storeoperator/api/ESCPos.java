package com.wetoop.storeoperator.api;

import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

/**
 * Created by bruce on 15-8-11.
 */
public final class ESCPos {
    // / <summary>
    // / 复位打印机
    // / </summary>
    public static byte[] ESC_ALT = {0x1b, 0x40};

    // / <summary>
    // / 选择页模式
    // / </summary>
    public static byte[] ESC_L = {0x1b, 0x4c};

    // / <summary>
    // / 页模式下取消打印数据
    // / </summary>
    public static byte[] ESC_CAN = {0x18};

    // / <summary>
    // / 打印并回到标准模式（在页模式下）
    // / </summary>
    public static byte[] FF = {0x0c};

    // / <summary>
    // / 页模式下打印缓冲区所有内容
    // / 只在页模式下有效，不清除缓冲区内容
    // / </summary>
    public static byte[] ESC_FF = {0x1b, 0x0c};

    // / <summary>
    // / 选择标准模式
    // / </summary>
    public static byte[] ESC_S = {0x1b, 0x53};

    // / <summary>
    // / 设置横向和纵向移动单位
    // / 分别将横向移动单位近似设置成1/x英寸，纵向移动单位设置成1/y英寸。
    // / 当x和y为0时，x和y被设置成默认值200。
    // / </summary>
    public static byte[] GS_P_x_y = {0x1d, 0x50, 0x00, 0x00};

    // / <summary>
    // / 选择国际字符集，值可以为0-15。默认值为0（美国）。
    // / </summary>
    public static byte[] ESC_R_n = {0x1b, 0x52, 0x00};

    // / <summary>
    // / 选择字符代码表，值可以为0-10,16-19。默认值为0。
    // / </summary>
    public static byte[] ESC_t_n = {0x1b, 0x74, 0x00};

    // / <summary>
    // / 打印并换行
    // / </summary>
    public static byte[] LF = {0x0a};

    public static byte[] CR = {0x0d};

    // / <summary>
    // / 设置行间距为[n*纵向或横向移动单位]英寸
    // / </summary>
    public static byte[] ESC_3_n = {0x1b, 0x33, 0x00};

    // / <summary>
    // / 设置字符右间距，当字符放大时，右间距也随之放大相同倍数
    // / </summary>
    public static byte[] ESC_SP_n = {0x1b, 0x20, 0x00};

    // / <summary>
    // / 在指定的钱箱插座引脚产生设定的开启脉冲。
    // / </summary>
    public static byte[] DLE_DC4_n_m_t = {0x10, 0x14, 0x01, 0x00, 0x01};

    // / <summary>
    // / 选择切纸模式并直接切纸，0为全切，1为半切
    // / </summary>
    public static byte[] GS_V_m = {0x1d, 0x56, 0x00};

    // / <summary>
    // / 进纸并且半切。
    // / </summary>
    public static byte[] GS_V_m_n = {0x1d, 0x56, 0x42, 0x00};

    // / <summary>
    // / 设置打印区域宽度，该命令仅在标准模式行首有效。
    // / 如果【左边距+打印区域宽度】超出可打印区域，则打印区域宽度为可打印区域-左边距。
    // / </summary>
    public static byte[] GS_W_nL_nH = {0x1d, 0x57, 0x76, 0x02};

    // / <summary>
    // / 设置绝对打印位置
    // / 将当前位置设置到距离行首（nL + nH x 256）处。
    // / 如果设置位置在指定打印区域外，该命令被忽略
    // / </summary>
    public static byte[] ESC_dollors_nL_nH = {0x1b, 0x24, 0x00, 0x00};

    /**
     * 选择对齐方式 0 左对齐 1 中间对齐 2 右对齐
     */
    public static byte[] ESC_a_n = {0x1b, 0x61, 0x00};

    // / <summary>
    // / 选择字符大小
    // / 0-3位选择字符高度，4-7位选择字符宽度
    // / 范围为从0-7
    // / </summary>
    public static byte[] GS_exclamationmark_n = {0x1d, 0x21, 0x00};

    // / <summary>
    // / 选择字体
    // / 0 标准ASCII字体
    // / 1 压缩ASCII字体
    // / </summary>
    public static byte[] ESC_M_n = {0x1b, 0x4d, 0x00};

    // / <summary>
    // / 选择/取消加粗模式
    // / n的最低位为0，取消加粗模式
    // / n最低位为1，选择加粗模式
    // / 与0x01即可
    // / </summary>
    public static byte[] GS_E_n = {0x1b, 0x45, 0x00};

    // / <summary>
    // / 选择/取消下划线模式
    // / 0 取消下划线模式
    // / 1 选择下划线模式（1点宽）
    // / 2 选择下划线模式（2点宽）
    // / </summary>
    public static byte[] ESC_line_n = {0x1b, 0x2d, 0x00};

    // / <summary>
    // / 选择/取消倒置打印模式
    // / 0 为取消倒置打印
    // / 1 选择倒置打印
    // / </summary>
    public static byte[] ESC_lbracket_n = {0x1b, 0x7b, 0x00};

    // / <summary>
    // / 选择/取消黑白反显打印模式
    // / n的最低位为0是，取消反显打印
    // / n的最低位为1时，选择反显打印
    // / </summary>
    public static byte[] GS_B_n = {0x1d, 0x42, 0x00};

    // / <summary>
    // / 选择/取消顺时针旋转90度
    // / </summary>
    public static byte[] ESC_V_n = {0x1b, 0x56, 0x00};

    // / <summary>
    // / 打印下载位图
    // / 0 正常
    // / 1 倍宽
    // / 2 倍高
    // / 3 倍宽、倍高
    // / </summary>
    public static byte[] GS_backslash_m = {0x1d, 0x2f, 0x00};

    // / <summary>
    // / 打印NV位图
    // / 以m指定的模式打印flash中图号为n的位图
    // / 1≤n≤255
    // / </summary>
    public static byte[] FS_p_n_m = {0x1c, 0x70, 0x01, 0x00};

    // / <summary>
    // / 选择HRI字符的打印位置
    // / 0 不打印
    // / 1 条码上方
    // / 2 条码下方
    // / 3 条码上、下方都打印
    // / </summary>
    public static byte[] GS_H_n = {0x1d, 0x48, 0x00};

    // / <summary>
    // / 选择HRI使用字体
    // / 0 标准ASCII字体
    // / 1 压缩ASCII字体
    // / </summary>
    public static byte[] GS_f_n = {0x1d, 0x66, 0x00};

    // / <summary>
    // / 选择条码高度
    // / 1≤n≤255
    // / 默认值 n=162
    // / </summary>
    public static byte[] GS_h_n = {0x1d, 0x68, (byte) 0xa2};

    // / <summary>
    // / 设置条码宽度
    // / 2≤n≤6
    // / 默认值 n=3
    // / </summary>
    public static byte[] GS_w_n = {0x1d, 0x77, 0x03};

    // / <summary>
    // / 打印条码
    // / 0x41≤m≤0x49
    // / n的取值有条码类型m决定
    // / </summary>
    public static byte[] GS_k_m_n_ = {0x1d, 0x6b, 0x41, 0x0c};

    /**
     * version: 1 <= v <= 17 error correction level: 1 <= r <= 4
     */
    public static byte[] GS_k_m_v_r_nL_nH = {0x1d, 0x6b, 0x61, 0x00, 0x02,
            0x00, 0x00};

    // / <summary>
    // / 页模式下设置打印区域
    // / 该命令在标准模式下只设置内部标志位，不影响打印
    // / </summary>
    public static byte[] ESC_W_xL_xH_yL_yH_dxL_dxH_dyL_dyH = {0x1b, 0x57,
            0x00, 0x00, 0x00, 0x00, 0x48, 0x02, (byte) 0xb0, 0x04};

    // / <summary>
    // / 在页模式下选择打印区域方向
    // / 0≤n≤3
    // / </summary>
    public static byte[] ESC_T_n = {0x1b, 0x54, 0x00};

    // / <summary>
    // / 页模式下设置纵向绝对位置
    // / 这条命令只有在页模式下有效
    // / </summary>
    public static byte[] GS_dollors_nL_nH = {0x1d, 0x24, 0x00, 0x00};

    // / <summary>
    // / 页模式下设置纵向相对位置
    // / 页模式下，以当前点位参考点设置纵向移动距离
    // / 这条命令只在页模式下有效
    // / </summary>
    public static byte[] GS_backslash_nL_nH = {0x1d, 0x5c, 0x00, 0x00};

    // / <summary>
    // / 选择/取消汉字下划线模式
    // / </summary>
    public static byte[] FS_line_n = {0x1c, 0x2d, 0x00};

    /**
     * 设置模块类型，缺省[7]=3, 0<=n<=16
     */
    public static byte[] GS_leftbracket_k_pL_pH_cn_67_n = {0x1d, 0x28,
            0x6b, 0x03, 0x00, 0x31, 0x43, 0x3};

    /**
     * 设置QR码的水平效验误差 缺省[7]=48, 48 - 7%, 49 - 15%, 50 - 25%, 51 - %30
     */
    public static byte[] GS_leftbracket_k_pL_pH_cn_69_n = {0x1d, 0x28,
            0x6b, 0x03, 0x00, 0x31, 0x45, 0x30};

    /**
     * 4<=pl+ph*256<=7092(0<=pl<=255,0<=ph<=28) （(pL + pH×256
     * )-3）的字节在m(d1...dk)后作为图形的数据被处理。
     */
    public static byte[] GS_leftbracket_k_pL_pH_cn_80_m__d1dk = {0x1d,
            0x28, 0x6b, 0x03, 0x00, 0x31, 0x50, 0x30};

    /**
     * 打印二维码
     */
    public static byte[] GS_leftbracket_k_pL_pH_cn_fn_m = {0x1d, 0x28,
            0x6b, 0x03, 0x00, 0x31, 0x51, 0x30};

    public static final int BARCODE_TYPE_UPC_A = 0x41;
    public static final int BARCODE_TYPE_UPC_E = 0x42;
    public static final int BARCODE_TYPE_EAN13 = 0x43;
    public static final int BARCODE_TYPE_EAN8 = 0x44;
    public static final int BARCODE_TYPE_CODE39 = 0x45;
    public static final int BARCODE_TYPE_ITF = 0x46;
    public static final int BARCODE_TYPE_CODEBAR = 0x47;
    public static final int BARCODE_TYPE_CODE93 = 0x48;
    public static final int BARCODE_TYPE_CODE128 = 0x49;

    public static final int BARCODE_FONTPOSITION_NO = 0x00;
    public static final int BARCODE_FONTPOSITION_ABOVE = 0x01;
    public static final int BARCODE_FONTPOSITION_BELOW = 0x02;
    public static final int BARCODE_FONTPOSITION_ABOVEANDBELOW = 0x03;

    public static final int BARCODE_FONTTYPE_STANDARD = 0x00;
    public static final int BARCODE_FONTTYPE_SMALL = 0x01;

    public static final int ALIGN_LEFT = 0x00;
    public static final int ALIGN_CENTER = 0x01;
    public static final int ALIGN_RIGHT = 0x02;

    public static final int FONTSTYLE_NORMAL = 0x00;
    public static final int FONTSTYLE_BOLD = 0x08;
    public static final int FONTSTYLE_UNDERLINE1 = 0x80;
    public static final int FONTSTYLE_UNDERLINE2 = 0x100;
    public static final int FONTSTYLE_UPSIDEDOWN = 0x200;
    public static final int FONTSTYLE_BLACKWHITEREVERSE = 0x400;
    public static final int FONTSTYLE_TURNRIGHT90 = 0x1000;

    public static final int CODEPAGE_CHINESE = 255;
    public static final int CODEPAGE_BIG5 = 254;
    public static final int CODEPAGE_UTF_8 = 253;
    public static final int CODEPAGE_SHIFT_JIS = 252;
    public static final int CODEPAGE_EUC_KR = 251;
    public static final int CODEPAGE_CP437_Standard_Europe = 0;
    public static final int CODEPAGE_Katakana = 1;
    public static final int CODEPAGE_CP850_Multilingual = 2;
    public static final int CODEPAGE_CP860_Portuguese = 3;
    public static final int CODEPAGE_CP863_Canadian_French = 4;
    public static final int CODEPAGE_CP865_Nordic = 5;
    public static final int CODEPAGE_WCP1251_Cyrillic = 6;
    public static final int CODEPAGE_CP866_Cyrilliec = 7;
    public static final int CODEPAGE_MIK_Cyrillic_Bulgarian = 8;
    public static final int CODEPAGE_CP755_East_Europe_Latvian_2 = 9;
    public static final int CODEPAGE_Iran = 10;
    public static final int CODEPAGE_CP862_Hebrew = 15;
    public static final int CODEPAGE_WCP1252_Latin_I = 16;
    public static final int CODEPAGE_WCP1253_Greek = 17;
    public static final int CODEPAGE_CP852_Latina_2 = 18;
    public static final int CODEPAGE_CP858_Multilingual_Latin = 19;
    public static final int CODEPAGE_Iran_II = 20;
    public static final int CODEPAGE_Latvian = 21;
    public static final int CODEPAGE_CP864_Arabic = 22;
    public static final int CODEPAGE_ISO_8859_1_West_Europe = 23;
    public static final int CODEPAGE_CP737_Greek = 24;
    public static final int CODEPAGE_WCP1257_Baltic = 25;
    public static final int CODEPAGE_Thai = 26;
    public static final int CODEPAGE_CP720_Arabic = 27;
    public static final int CODEPAGE_CP855 = 28;
    public static final int CODEPAGE_CP857_Turkish = 29;
    public static final int CODEPAGE_WCP1250_Central_Eurpoe = 30;
    public static final int CODEPAGE_CP775 = 31;
    public static final int CODEPAGE_WCP1254_Turkish = 32;
    public static final int CODEPAGE_WCP1255_Hebrew = 33;
    public static final int CODEPAGE_WCP1256_Arabic = 34;
    public static final int CODEPAGE_WCP1258_Vietnam = 35;
    public static final int CODEPAGE_ISO_8859_2_Latin_2 = 36;
    public static final int CODEPAGE_ISO_8859_3_Latin_3 = 37;
    public static final int CODEPAGE_ISO_8859_4_Baltic = 38;
    public static final int CODEPAGE_ISO_8859_5_Cyrillic = 39;
    public static final int CODEPAGE_ISO_8859_6_Arabic = 40;
    public static final int CODEPAGE_ISO_8859_7_Greek = 41;
    public static final int CODEPAGE_ISO_8859_8_Hebrew = 42;
    public static final int CODEPAGE_ISO_8859_9_Turkish = 43;
    public static final int CODEPAGE_ISO_8859_15_Latin_3 = 44;
    public static final int CODEPAGE_Thai2 = 45;
    public static final int CODEPAGE_CP856 = 46;
    public static final int CODEPAGE_Cp874 = 47;

    /**
     * reusable init esc code
     */
    public static byte[] escInit() {
        return ESC_ALT;
    }

    /**
     * @param txt String to print
     */
    public static byte[] printLine(String txt) {
        byte[] data = new byte[1024 + txt.length() * 2];
        ByteBuffer dataBuffer = ByteBuffer.wrap(data);

        try {
            dataBuffer.put(new byte[]{0x1b, 0x40, 0x1c, 0x26});
            dataBuffer.put(txt.getBytes("GBK"));
        } catch (UnsupportedEncodingException e) {
            Log.e("ERROR", e.getMessage(), e);
        }

        dataBuffer.put(LF);

        return (dataBuffer.array());
    }

    /**
     * Prints n lines of blank paper.
     */
    public static byte[] feed(int feed) {
        byte[] data = new byte[1024];
        ByteBuffer dataBuffer = ByteBuffer.wrap(data);

        dataBuffer.put((byte) 0x1B);
        dataBuffer.put((byte) 'd');
        dataBuffer.put((byte) feed);

        return (dataBuffer.array());
    }

    /**
     * Sets bold
     */
    public static byte[] setBold(Boolean bool) {
        byte[] data = new byte[1024];
        ByteBuffer dataBuffer = ByteBuffer.wrap(data);

        dataBuffer.put((byte) 0x1B);
        dataBuffer.put((byte) 'E');
        dataBuffer.put((byte) (bool ? 1 : 0));

        return (dataBuffer.array());
    }

    /**
     * Sets white on black printing
     */
    public static byte[] setInverse(Boolean bool) {
        byte[] data = new byte[1024];
        ByteBuffer dataBuffer = ByteBuffer.wrap(data);

        dataBuffer.put((byte) 0x1D);
        dataBuffer.put((byte) 'B');
        dataBuffer.put((byte) (bool ? 1 : 0));

        return (dataBuffer.array());
    }

    /**
     * Sets underline and weight
     *
     * @param val 0 = no underline.
     *            1 = single weight underline.
     *            2 = double weight underline.
     */

    public static byte[] setUnderline(int val) {
        byte[] data = new byte[1024];
        ByteBuffer dataBuffer = ByteBuffer.wrap(data);

        dataBuffer.put((byte) 0x1B);
        dataBuffer.put((byte) '-');
        dataBuffer.put((byte) val);

        return (dataBuffer.array());
    }


    /**
     * Sets left, center, right justification
     *
     * @param val 0 = left justify.
     *            1 = center justify.
     *            2 = right justify.
     */

    public static byte[] setJustification(int val) {
        byte[] data = new byte[1024];
        ByteBuffer dataBuffer = ByteBuffer.wrap(data);

        dataBuffer.put((byte) 0x1B);
        dataBuffer.put((byte) 'a');
        dataBuffer.put((byte) val);

        return (dataBuffer.array());
    }

    /**
     * Encode and print QR code
     *
     * @param strCodedata           String to be encoded in QR.
     * @param nErrorCorrectionLevel The degree of error correction. (48 <= n <= 51)
     *                              48 = level L / 7% recovery capacity.
     *                              49 = level M / 15% recovery capacity.
     *                              50 = level Q / 25% recovery capacity.
     *                              51 = level H / 30% recovery capacity.
     * @param nWidthX               The size of the QR module (pixel) in dots.
     *                              The QR code will not print if it is too big.
     *                              Try setting this low and experiment in making it larger.
     */
    public static byte[] printQR(String strCodedata, int nWidthX, int nErrorCorrectionLevel) {
        if (nWidthX < 2 | nWidthX > 6 | nErrorCorrectionLevel < 1
                | nErrorCorrectionLevel > 4) {
            return null;
        }

        byte[] bCodeData = null;
        try {
            bCodeData = strCodedata.getBytes("GBK");
        } catch (UnsupportedEncodingException e) {
            Log.e("ERROR", e.getMessage(), e);
            return null;
        }

        GS_w_n[2] = (byte) nWidthX;
        GS_k_m_v_r_nL_nH[4] = (byte) nErrorCorrectionLevel;
        GS_k_m_v_r_nL_nH[5] = (byte) (bCodeData.length & 0xff);
        GS_k_m_v_r_nL_nH[6] = (byte) ((bCodeData.length & 0xff00) >> 8);

        byte[] data = new byte[1024 + strCodedata.length()];
        ByteBuffer dataBuffer = ByteBuffer.wrap(data);
        dataBuffer.put(GS_w_n);
        dataBuffer.put(GS_k_m_v_r_nL_nH);
        dataBuffer.put(bCodeData);

        return (dataBuffer.array());
    }

    /**
     * Encode and print barcode
     *
     * @param strCodedata      String to be encoded in the barcode.
     *                         Different barcodes have different requirements on the length
     *                         of data that can be encoded.
     * @param nType            Specify the type of barcode
     *                         65 = UPC-A.
     *                         66 = UPC-E.
     *                         67 = JAN13(EAN).
     *                         68 = JAN8(EAN).
     *                         69 = CODE39.
     *                         70 = ITF.
     *                         71 = CODABAR.
     *                         72 = CODE93.
     *                         73 = CODE128.
     * @param nHeight          height of the barcode in points (1 <= n <= 255)
     * @param nWidthX          width of module (2 <= n <=6).
     *                         Barcode will not print if this value is too large.
     * @param nHriFontType     Set font of HRI characters
     *                         0 = font A
     *                         1 = font B
     * @param nHriFontPosition set position of HRI characters
     *                         0 = not printed.
     *                         1 = Above barcode.
     *                         2 = Below barcode.
     *                         3 = Both above and below barcode.
     */
    public static byte[] printBarcode(String strCodedata, int nOrgx,
                                      int nType, int nWidthX, int nHeight, int nHriFontType,
                                      int nHriFontPosition) {
        if (nOrgx < 0 | nOrgx > 65535 | nType < 0x41 | nType > 0x49
                | nWidthX < 2 | nWidthX > 6 | nHeight < 1 | nHeight > 255) {
            return null;
        }

        byte[] bCodeData = null;
        try {
            bCodeData = strCodedata.getBytes("GBK");
        } catch (UnsupportedEncodingException e) {
            Log.e("ERROR", e.getMessage(), e);
            return null;
        }

        ESC_dollors_nL_nH[2] = (byte) (nOrgx % 0x100);
        ESC_dollors_nL_nH[3] = (byte) (nOrgx / 0x100);
        GS_w_n[2] = (byte) nWidthX;
        GS_h_n[2] = (byte) nHeight;
        GS_f_n[2] = (byte) (nHriFontType & 0x01);
        GS_H_n[2] = (byte) (nHriFontPosition & 0x03);
        GS_k_m_n_[2] = (byte) nType;
        GS_k_m_n_[3] = (byte) bCodeData.length;

        byte[] data = new byte[1024 + strCodedata.length()];
        ByteBuffer dataBuffer = ByteBuffer.wrap(data);

        dataBuffer.put(ESC_dollors_nL_nH);
        dataBuffer.put(GS_w_n);
        dataBuffer.put(GS_h_n);
        dataBuffer.put(GS_f_n);
        dataBuffer.put(GS_H_n);
        dataBuffer.put(GS_k_m_n_);
        dataBuffer.put(bCodeData);

        return (dataBuffer.array());
    }

    /**
     * Encode and print PDF 417 barcode
     *
     * @param code  String to be encoded in the barcode.
     *              Different barcodes have different requirements on the length
     *              of data that can be encoded.
     * @param type  Specify the type of barcode
     *              0 - Standard PDF417
     *              1 - Standard PDF417
     * @param h     Height of the vertical module in dots 2 <= n <= 8.
     * @param w     Height of the horizontal module in dots 1 <= n <= 4.
     * @param cols  Number of columns 0 <= n <= 30.
     * @param rows  Number of rows 0 (automatic), 3 <= n <= 90.
     * @param error set error correction level 48 <= n <= 56 (0 - 8).
     */
    public static byte[] printPSDCode(String code, int type, int h, int w, int cols, int rows, int error) {
        byte[] data = new byte[1024 + code.length()];
        ByteBuffer dataBuffer = ByteBuffer.wrap(data);

        //print function 82
        dataBuffer.put((byte) 0x1D);
        dataBuffer.put("(k".getBytes());
        dataBuffer.put((byte) code.length()); //pl Code length
        dataBuffer.put((byte) 0); //ph
        dataBuffer.put((byte) 48); //cn
        dataBuffer.put((byte) 80); //fn
        dataBuffer.put((byte) 48); //m
        dataBuffer.put(code.getBytes()); //data to be encoded


        //function 65 specifies the number of columns
        dataBuffer.put((byte) 0x1D);//init
        dataBuffer.put("(k".getBytes());//adjust height of barcode
        dataBuffer.put((byte) 3); //pl
        dataBuffer.put((byte) 0); //pH
        dataBuffer.put((byte) 48); //cn
        dataBuffer.put((byte) 65); //fn
        dataBuffer.put((byte) cols);

        //function 66 number of rows
        dataBuffer.put((byte) 0x1D);//init
        dataBuffer.put("(k".getBytes());//adjust height of barcode
        dataBuffer.put((byte) 3); //pl
        dataBuffer.put((byte) 0); //pH
        dataBuffer.put((byte) 48); //cn
        dataBuffer.put((byte) 66); //fn
        dataBuffer.put((byte) rows); //num rows

        //module width function 67
        dataBuffer.put((byte) 0x1D);
        dataBuffer.put("(k".getBytes());
        dataBuffer.put((byte) 3);//pL
        dataBuffer.put((byte) 0);//pH
        dataBuffer.put((byte) 48);//cn
        dataBuffer.put((byte) 67);//fn
        dataBuffer.put((byte) w);//size of module 1<= n <= 4

        //module height fx 68
        dataBuffer.put((byte) 0x1D);
        dataBuffer.put("(k".getBytes());
        dataBuffer.put((byte) 3);//pL
        dataBuffer.put((byte) 0);//pH
        dataBuffer.put((byte) 48);//cn
        dataBuffer.put((byte) 68);//fn
        dataBuffer.put((byte) h);//size of module 2 <= n <= 8

        //error correction function 69
        dataBuffer.put((byte) 0x1D);
        dataBuffer.put("(k".getBytes());
        dataBuffer.put((byte) 4);//pL
        dataBuffer.put((byte) 0);//pH
        dataBuffer.put((byte) 48);//cn
        dataBuffer.put((byte) 69);//fn
        dataBuffer.put((byte) 48);//m
        dataBuffer.put((byte) error);//error correction

        //choose pdf417 type function 70
        dataBuffer.put((byte) 0x1D);
        dataBuffer.put("(k".getBytes());
        dataBuffer.put((byte) 3);//pL
        dataBuffer.put((byte) 0);//pH
        dataBuffer.put((byte) 48);//cn
        dataBuffer.put((byte) 70);//fn
        dataBuffer.put((byte) type);//set mode of pdf 0 or 1

        //print function 81
        dataBuffer.put((byte) 0x1D);
        dataBuffer.put("(k".getBytes());
        dataBuffer.put((byte) 3); //pl
        dataBuffer.put((byte) 0); //ph
        dataBuffer.put((byte) 48); //cn
        dataBuffer.put((byte) 81); //fn
        dataBuffer.put((byte) 48); //m

        return (dataBuffer.array());
    }

    public static byte[] printNVImage(int n, int m) {
        if (m < 0 | m > 52 | n < 1 | n > 255) {
            return null;
        }

        FS_p_n_m[2] = (byte) n;
        FS_p_n_m[3] = (byte) m;

        return (FS_p_n_m);
    }

    /**
     * Store custom character
     * input array of column bytes.	NOT WORKING
     *
     * @param spacing Integer representing Vertical motion of unit in inches. 0-255
     */
    public static byte[] setLineSpacing(int spacing) {
        byte[] data = new byte[1024];
        ByteBuffer dataBuffer = ByteBuffer.wrap(data);

        //function ESC 3
        dataBuffer.put((byte) 0x1B);
        dataBuffer.put((byte) '3');
        dataBuffer.put((byte) spacing);

        return (dataBuffer.array());
    }

    public static byte[] cut() {
        return GS_V_m_n;
    }

}
