package com.wetoop.storeoperator.ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Rect;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.Result;
import com.tapadoo.alerter.Alerter;
import com.wetoop.storeoperator.App;
import com.wetoop.storeoperator.R;
import com.wetoop.storeoperator.api.model.Order;
import com.wetoop.storeoperator.api.model.ResultMessage;
import com.wetoop.storeoperator.api.model.UserInfo;
import com.wetoop.storeoperator.sql.SwitchUserBean;
import com.wetoop.storeoperator.sql.SwitchUserSql;
import com.wetoop.storeoperator.tools.LoginOutTools;
import com.wetoop.storeoperator.tools.NdefTextRecord;
import com.wetoop.storeoperator.ui.dialog.HintDialog;
import com.wetoop.storeoperator.zxing.BeepManager;
import com.wetoop.storeoperator.zxing.CameraManager;
import com.wetoop.storeoperator.zxing.InactivityTimer;
import com.wetoop.storeoperator.zxing.ScanQRActivityHandler;
import com.wetoop.storeoperator.zxing.decode.DecodeThread;

import net.steamcrafted.loadtoast.LoadToast;

import org.json.JSONArray;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static com.wetoop.storeoperator.tools.StatusBarUtils.setWindowStatusBarColor;
import static com.wetoop.storeoperator.ui.fragment.OrderCancelFragment.cancelList;
import static com.wetoop.storeoperator.ui.fragment.OrderPayFragment.payList;
import static com.wetoop.storeoperator.ui.fragment.OrderWaitFragment.waitList;
import static com.wetoop.storeoperator.utils.NfcConstant.SELECT1;
import static com.wetoop.storeoperator.utils.NfcConstant.SELECT2;
import static com.wetoop.storeoperator.utils.NfcConstant.SELECT3;
import static com.wetoop.storeoperator.utils.NfcConstant.SELECT4;
import static com.wetoop.storeoperator.utils.NfcConstant.SELECT5;
import static com.wetoop.storeoperator.utils.NfcConstant.SELECTHEAD;

/**
 * Created by bruce on 15-8-13.
 */
public class ScannerActivity extends Activity implements SurfaceHolder.Callback {
    private static String TAG = "ScannerActivity";
    private App app;
    private String allowComing = "no";//判断是否来自收款
    private String allowPriceStr;
    private String orderId = null;
    private int count = 0;
    private int numCutOff = 0;//防止在数据加载过程中连续的扫描
    private int numNFC = 0;//禁止NFC扫描时重复进入
    private RelativeLayout openFlash;
    private boolean flash = true;
    private TextView titleText;
    private TextView noNFCText;
    private TextView allowPayPrice;
    private ImageView noNFC;
    private ImageView openFlashImage;
    private ArrayList<SwitchUserBean> switchUserBeanArrayList = new ArrayList<>();
    private HintDialog logoutDialog1, logoutDialog2;
    private SurfaceView scanPreview;
    private RelativeLayout scanContainer;
    private RelativeLayout scanCropView;
    private ImageView scanLine;
    private CameraManager cameraManager;
    private ScanQRActivityHandler scanQRActivityHandler;
    private InactivityTimer inactivityTimer;
    private BeepManager beepManager;
    private Rect mCropRect = null;
    private boolean isHasSurface = false;
    private boolean dialogShow = false;//用于重启摄像头验证
    private NfcAdapter nfcAdapter = null;
    private PendingIntent mPendingIntent;
    private LoadToast loadToast;
    private HintDialog hintNfcDialog;

    private String[] nfcAID;
    private byte[] selectResult;
    private static int NFCSIZE = 128;
    private final int READER_FLAGS = NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK;
    private NfcAdapter.ReaderCallback mReaderCallback = new NfcAdapter.ReaderCallback() {
        @Override
        public void onTagDiscovered(Tag tag) {
            Message msg0 = new Message();
            msg0.what = 0;//读取成功0
            handlerMessage.sendMessage(msg0);
            IsoDep isoDep = IsoDep.get(tag);
            try {
                isoDep.connect();
                for (int i = 0; i < nfcAID.length; i++) {
                    byte[] nfcAidByte = HexStringToByteArray(nfcAID[i]);
                    byte[] SelectAidResult = new byte[SELECTHEAD.length + nfcAidByte.length];
                    System.arraycopy(SELECTHEAD, 0, SelectAidResult, 0, SELECTHEAD.length);
                    System.arraycopy(nfcAidByte, 0, SelectAidResult, SELECTHEAD.length, nfcAidByte.length);//把aid连接到SELECT后面
                    byte[] result = isoDep.transceive(SelectAidResult);
                    if ((result[0] == (byte) 0x90 && result[1] == (byte) 0x00)) {
                        byte[] result1 = isoDep.transceive(SELECT1);
                        if ((result1[result1.length - 2] != (byte) 0x90 && result1[result1.length - 1] != (byte) 0x00)){
                            Message msg3 = new Message();
                            msg3.what = 3;
                            handlerMessage.sendMessage(msg3);
                            return;
                        }

                        byte[] result2 = isoDep.transceive(SELECT2);
                        if ((result2[result2.length - 2] != (byte) 0x90 && result2[result2.length - 1] != (byte) 0x00)){
                            Message msg3 = new Message();
                            msg3.what = 3;
                            handlerMessage.sendMessage(msg3);
                            return;
                        }

                        System.arraycopy(result2, 9, SELECT3, 5, 2);
                        byte[] result3 = isoDep.transceive(SELECT3);
                        if ((result3[result3.length - 2] != (byte) 0x90 && result3[result3.length - 1] != (byte) 0x00)){
                            Message msg3 = new Message();
                            msg3.what = 3;
                            handlerMessage.sendMessage(msg3);
                            return;
                        }

                        byte[] result4 = isoDep.transceive(SELECT4);
                        if ((result4[result4.length - 2] != (byte) 0x90 && result4[result4.length - 1] != (byte) 0x00)){
                            Message msg3 = new Message();
                            msg3.what = 3;
                            handlerMessage.sendMessage(msg3);
                            return;
                        }

                        short resultSizeShort = (short) (result4[0]<<8 | result4[1] & 0xFF);
                        int resultSizeNum = (int) resultSizeShort;
                        byte[] result5;
                        if(resultSizeNum < NFCSIZE){
                            SELECT5[4] = result4[1];
                            result5 = isoDep.transceive(SELECT5);
                            if (result5[result5.length - 2] == (byte) 0x90 && result5[result5.length - 1] == (byte) 0x00) {
                                selectResult = new byte[resultSizeNum];
                                System.arraycopy(result5, 0, selectResult, 0, result5.length - 2);
                            }
                        }else {
                            short nfcShort = (short) NFCSIZE;
                            SELECT5[4] = (byte) (nfcShort & 0xff);
                            result5 = isoDep.transceive(SELECT5);
                            if (result5[result5.length - 2] == (byte) 0x90 && result5[result5.length - 1] == (byte) 0x00) {
                                selectResult = new byte[resultSizeNum];
                                System.arraycopy(result5, 0, selectResult, 0, result5.length - 2);
                                int num = 2+NFCSIZE;
                                int selectResultBool = 0;
                                int arrayNum = 0;
                                while (selectResultBool <= resultSizeNum) {
                                    short x = (short) num;
                                    SELECT5[2] = (byte) ((x >> 8) & 0xff);
                                    SELECT5[3] = (byte) (x & 0xff);
                                    if ((resultSizeNum - num) < NFCSIZE) {
                                        x = (short) (resultSizeNum - num + 2);
                                        SELECT5[4] = (byte) (x & 0xff);
                                        selectResultBool = resultSizeNum;
                                    } else {
                                        x = (short) 8;
                                        SELECT5[4] = (byte) (x & 0xff);
                                    }
                                    byte[] result6 = isoDep.transceive(SELECT5);
                                    if (result6[result6.length - 2] == (byte) 0x90 && result6[result6.length - 1] == (byte) 0x00) {
                                        System.arraycopy(result6, 0, selectResult, result5.length - 2 + arrayNum, result6.length - 2);
                                        arrayNum = arrayNum + (result6.length - 2);
                                    } else {
                                        Message msg3 = new Message();
                                        msg3.what = 3;//读取失败
                                        handlerMessage.sendMessage(msg3);
                                    }
                                    selectResultBool = selectResultBool + NFCSIZE;
                                    num = 2 + NFCSIZE + selectResultBool;
                                }
                            }
                        }
                        try {
                            NdefMessage ndefMessage = new NdefMessage(selectResult);
                            NdefRecord record = ndefMessage.getRecords()[0];
                            NdefTextRecord textRecord = NdefTextRecord.parse(record);
                            if (textRecord != null) {
                                String nfcMsg = textRecord.getText();
                                if (numNFC == 0) {
                                    numNFC++;
                                    getOrderItemData(app.getToken(), nfcMsg);
                                }
                            } else {
                                Message msg3 = new Message();
                                msg3.what = 3;//读取失败
                                handlerMessage.sendMessage(msg3);
                            }
                        } catch (FormatException e) {
                            Message msg = new Message();
                            msg.what = 1;//读取数据成功，但是解析时抛异常
                            handlerMessage.sendMessage(msg);
                            //e.printStackTrace();
                        }
                        return;
                    } else if ((result[0] == (byte) 0x6A && result[1] == (byte) 0x82)) {
                        Message msg2 = new Message();
                        msg2.what = 2;//nfc初始化失败，可继续循环
                        handlerMessage.sendMessage(msg2);
                    } else {
                        Message msg2 = new Message();
                        msg2.what = 2;//nfc初始化失败，跳出循环
                        handlerMessage.sendMessage(msg2);
                        return;
                    }
                }
                isoDep.close();

            } catch (IOException e) {
                Message msg1 = new Message();
                msg1.what = 1;//初始化解析抛异常
                handlerMessage.sendMessage(msg1);
            }

        }
    };

    public static byte[] HexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    private Handler handlerMessage = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    loadToast = new LoadToast(ScannerActivity.this);
                    loadToast.setText("正在极速识别");
                    loadToast.setTranslationY(100);
                    loadToast.show();
                    break;
                case 1:
                    if(loadToast != null) loadToast.error();
                    Alerter.create(ScannerActivity.this)
                            .setText("读取失败，请靠近读卡区域")
                            .setBackgroundColorRes(R.color.alerter_confirm)
                            .show();
                    break;
                case 2:
                    if(loadToast != null) loadToast.error();
                    Alerter.create(ScannerActivity.this)
                            .setText("极速识别初始化失败")
                            .setBackgroundColorRes(R.color.alerter_confirm)
                            .show();
                    break;
                case 3:
                    if(loadToast != null) loadToast.error();
                    Alerter.create(ScannerActivity.this)
                            .setText("极速识别读取失败")
                            .setBackgroundColorRes(R.color.alerter_confirm)
                            .show();
                    break;
            }
        }
    };

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_scanner);
        setWindowStatusBarColor(ScannerActivity.this, R.color.title_clicked_color);
        app = (App) getApplication();
        app.addActivity(this);
        initViews();
        setDataView();
        setClickedListener();
        stringToJsonArray();
        getNFCAID(app.getToken());
    }

    public Handler getHandler() {
        return scanQRActivityHandler;
    }

    public CameraManager getCameraManager() {
        return cameraManager;
    }

    private void initViews() {
        scanPreview = (SurfaceView) findViewById(R.id.capture_preview);
        scanContainer = (RelativeLayout) findViewById(R.id.capture_container);
        scanCropView = (RelativeLayout) findViewById(R.id.capture_crop_view);
        scanLine = (ImageView) findViewById(R.id.capture_scan_line);
        openFlash = (RelativeLayout) findViewById(R.id.openFlash);
        openFlashImage = (ImageView) findViewById(R.id.openFlashImage);
        titleText = (TextView) findViewById(R.id.titleText);
        allowPayPrice = (TextView) findViewById(R.id.allow_pay_price);
        noNFC = (ImageView) findViewById(R.id.noNFC);
        noNFCText = (TextView) findViewById(R.id.noNFCText);
    }

    private void setDataView() {
        Intent intent = getIntent();
        if (intent.getStringExtra("allow_coming") != null) {
            allowComing = intent.getStringExtra("allow_coming");
            allowPriceStr = intent.getStringExtra("allow_pay");
        }
        /**只有从收款按钮进入，coming才会有值，否则为null*/
        if (allowComing.equals("coming")) {
            titleText.setText("收款：" + app.getTitle());
        } else {
            titleText.setText("扫描验证：" + app.getTitle());
        }
        /**扫描框里的横线移动动画设置*/
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        inactivityTimer = new InactivityTimer(this);
        beepManager = new BeepManager(this);
        TranslateAnimation animation = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.9f);
        animation.setDuration(4500);
        animation.setRepeatCount(-1);
        animation.setRepeatMode(Animation.RESTART);
        scanLine.startAnimation(animation);
    }

    private void setClickedListener() {
        openFlash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    flash = cameraManager.flashControlHandler();
                    if (flash) {
                        openFlashImage.setImageResource(R.mipmap.flashlight_open);
                    } else {
                        openFlashImage.setImageResource(R.mipmap.flashlight_close);
                    }
                } catch (Exception ex) {
                }
            }
        });
        noNFC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nfcInit();
            }
        });
        RelativeLayout back = (RelativeLayout) findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                back();
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onResume() {
        super.onResume();
        App app = (App) getApplication();
        app.setNFCResume(0);//此时Activity进入resume状态
        cameraManager = new CameraManager(getApplication());
        scanQRActivityHandler = null;
        if (isHasSurface) {
            initCamera(scanPreview.getHolder());
        } else {
            scanPreview.getHolder().addCallback(this);
        }
        inactivityTimer.onResume();

        if (!allowComing.equals("coming")) {//扫描进入
            allowPayPrice.setVisibility(View.GONE);
            nfcAdapter = NfcAdapter.getDefaultAdapter(this);
            mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
                    getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
            IntentFilter filter = new IntentFilter(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED);
            this.registerReceiver(mReceiver, filter);
            enableForegroundDispatch();
            if (nfcAdapter == null) {
                noNFC.setVisibility(View.GONE);
                noNFCText.setVisibility(View.GONE);
            } else {
                noNFC.setVisibility(View.VISIBLE);
                if (!nfcAdapter.isEnabled()) {
                    noNFC.setImageDrawable(getResources().getDrawable(R.mipmap.nfc_gray));
                } else {
                    noNFC.setImageDrawable(getResources().getDrawable(R.mipmap.nfc));
                }
            }
        }else{//收款进入，无nfc功能
            allowPayPrice.setVisibility(View.VISIBLE);
            allowPayPrice.setText("收款金额："+allowPriceStr);
            noNFC.setVisibility(View.GONE);
            noNFCText.setVisibility(View.GONE);
        }
    }

    private void nfcInit() {
        if (nfcAdapter == null) {
            noNFC.setVisibility(View.GONE);
            noNFCText.setVisibility(View.GONE);
        } else {
            noNFC.setVisibility(View.VISIBLE);
            if (!nfcAdapter.isEnabled()) {
                noNFCText.setVisibility(View.GONE);
                noNFC.setImageDrawable(getResources().getDrawable(R.mipmap.nfc_gray));
                hintNfcDialog = new HintDialog(ScannerActivity.this, "提示", "极速识别未启用（需进入设置开启NFC）", "设置NFC", new HintDialog.OnCustomDialogListener() {
                    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                    @Override
                    public void back(String query) {
                        if (query.equals("confirm")) {
                            Intent setNfc = new Intent(Settings.ACTION_NFC_SETTINGS);
                            startActivity(setNfc);
                        }
                        hintNfcDialog.dismiss();
                    }
                });
                hintNfcDialog.show();
            } else {
                noNFC.setImageDrawable(getResources().getDrawable(R.mipmap.nfc));
                if (noNFCText.getVisibility() == View.VISIBLE)
                    noNFCText.setVisibility(View.GONE);
                else
                    noNFCText.setVisibility(View.VISIBLE);
            }
        }
    }

    private void enableForegroundDispatch() {
        if (mPendingIntent == null)
            mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
                    getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        if (nfcAdapter != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                Bundle bundle = new Bundle();
                nfcAdapter.enableReaderMode(this, mReaderCallback, READER_FLAGS, bundle);
            }
        }
    }

    private void disableForegroundDispatch() {
        // TODO 自动生成的方法存根
        if (nfcAdapter != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                nfcAdapter.disableReaderMode(this);
            }
        }
    }

    @Override
    protected void onPause() {
        App app = (App) getApplication();
        app.setNFCResume(1);//此时Activity进入pause状态，nfc的开关变化时不影响应用
        inactivityTimer.onPause();
        cameraManager.closeDriver();
        if (!isHasSurface) {
            scanPreview.getHolder().removeCallback(this);
        }
        if (scanQRActivityHandler != null) {
            scanQRActivityHandler.quitSynchronously();
            scanQRActivityHandler = null;
        }
        if (!allowComing.equals("coming"))
            disableForegroundDispatch();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (scanQRActivityHandler != null) {
            scanQRActivityHandler.quitSynchronously();
            scanQRActivityHandler = null;
        }
        inactivityTimer.shutdown();
        beepManager.close();
        cameraManager.closeDriver();
        if (!isHasSurface) {
            scanPreview.getHolder().removeCallback(this);
        }
        if (!allowComing.equals("coming"))
            this.unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!isHasSurface) {
            isHasSurface = true;
            initCamera(holder);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isHasSurface = false;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    public void handleDecode(Result rawResult, Bundle bundle) {
        inactivityTimer.onActivity();
        beepManager.playBeepSoundAndVibrate();
        disposeResult(rawResult);
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        if (surfaceHolder == null) {
            throw new IllegalStateException("No SurfaceHolder provided");
        }
        if (cameraManager.isOpen()) {
            Log.w(TAG, "initCamera() while already open -- late SurfaceView callback?");
            return;
        }
        try {
            cameraManager.openDriver(surfaceHolder);
            if (scanQRActivityHandler == null) {
                scanQRActivityHandler = new ScanQRActivityHandler(this, cameraManager, DecodeThread.QRCODE_MODE);
            }
            initCrop();
        } catch (IOException | RuntimeException ioe) {
            displayFrameworkBugMessageAndExit();
        }
    }

    //重启扫描
    private void restartCamera() {
        if (scanQRActivityHandler != null) {
            scanQRActivityHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (scanQRActivityHandler != null)
                        scanQRActivityHandler.restartPreviewAndDecode();
                }
            }, 500);
        }
    }

    private void displayFrameworkBugMessageAndExit() {
        // camera error
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.app_name));
        builder.setMessage("摄像头获取错误");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }

        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                finish();
            }
        });
        builder.show();
    }

    public Rect getCropRect() {
        return mCropRect;
    }

    /**
     * 初始化截取的矩形区域
     */
    private void initCrop() {
        int cameraWidth = cameraManager.getCameraResolution().y;
        int cameraHeight = cameraManager.getCameraResolution().x;

        /** 获取布局中扫描框的位置信息 */
        int[] location = new int[2];
        scanCropView.getLocationInWindow(location);

        int cropLeft = location[0];
        int cropTop = location[1] - getStatusBarHeight();

        int cropWidth = scanCropView.getWidth();
        int cropHeight = scanCropView.getHeight();

        /** 获取布局容器的宽高 */
        int containerWidth = scanContainer.getWidth();
        int containerHeight = scanContainer.getHeight();

        /** 计算最终截取的矩形的左上角顶点x坐标和y坐标 */
        int x = cropLeft * cameraWidth / containerWidth;
        int y = cropTop * cameraHeight / containerHeight;

        /** 计算最终截取的矩形的宽度和高度 */
        int width = cropWidth * cameraWidth / containerWidth;
        int height = cropHeight * cameraHeight / containerHeight;

        /** 生成最终的截取的矩形 */
        mCropRect = new Rect(x, y, width + x, height + y);
    }

    private int getStatusBarHeight() {
        try {
            Class<?> c = Class.forName("com.android.internal.R$dimen");
            Object obj = c.newInstance();
            Field field = c.getField("status_bar_height");
            int x = Integer.parseInt(field.get(obj).toString());
            return getResources().getDimensionPixelSize(x);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private JSONArray json = new JSONArray();
    private void stringToJsonArray() {
        switchUserBeanArrayList = switchUserList();
        for (int i = 0; i < switchUserBeanArrayList.size(); i++) {
            json.put(switchUserBeanArrayList.get(i).getToken());
        }
    }

    private ArrayList<SwitchUserBean> switchUserList() {
        SwitchUserSql cameraSql = new SwitchUserSql(ScannerActivity.this);
        return cameraSql.queryDataToSQLite();
    }

    //获取二维码扫描返回的结果
    public void disposeResult(Result rawResult) {
        loadToast = new LoadToast(ScannerActivity.this);
        loadToast.setText("读取数据中");
        loadToast.setTranslationY(100);
        loadToast.show();
        String text = rawResult.getText();
        if (allowComing.equals("coming") && count == 0) {//收款的处理
            final App app = (App) getApplication();
            app.getApiService().allow_pay(app.getToken(), allowPriceStr, text, new Callback<ResultMessage>() {
                @Override
                public void success(ResultMessage resultMessage, Response response) {
                    if (resultMessage != null) {
                        orderId = resultMessage.getOrder_id();
                        if (orderId != null && count == 0) {
                            count++;
                            final App app = (App) getApplication();
                            getPayOrderData(app.getToken(), orderId);
                        }
                        if (resultMessage.getErrorCode() != 200) {
                            Alerter.create(ScannerActivity.this)
                                    .setText(resultMessage.getErrorMessage())
                                    .setBackgroundColorRes(R.color.alerter_confirm)
                                    .show();
                        }
                    }
                    if(loadToast != null) loadToast.success();
                    if(!dialogShow) restartCamera();
                }

                @Override
                public void failure(RetrofitError error) {
                    if(loadToast != null) loadToast.error();
                    if(!dialogShow) restartCamera();
                    Alerter.create(ScannerActivity.this)
                            .setText("网络不给力")
                            .setBackgroundColorRes(R.color.alerter_alert)
                            .show();
                }
            });

        } else {
            if (text.startsWith("https://s.wwz.dk/#")) {
                try {
                    URI uri = new URI(text);
                    final String orderId = uri.getFragment();
                    if (orderId != null) {
                        if (numCutOff == 0) {
                            numCutOff = 1;
                            final App app = (App) getApplication();
                            getOrderItemData(app.getToken(), orderId);
                        }
                    } else {
                        numCutOff = 0;
                        Alerter.create(ScannerActivity.this)
                                .setText(R.string.qr_error)
                                .setBackgroundColorRes(R.color.alerter_confirm)
                                .show();
                        if(loadToast != null) loadToast.error();
                        restartCamera();
                    }

                } catch (URISyntaxException e) {
                    numCutOff = 0;
                    Alerter.create(ScannerActivity.this)
                            .setText(R.string.qr_error)
                            .setBackgroundColorRes(R.color.alerter_confirm)
                            .show();
                    if(loadToast != null) loadToast.error();
                    restartCamera();
                }
            } else {
                if (count != 1) {
                    Alerter.create(ScannerActivity.this)
                            .setText(R.string.qr_error)
                            .setBackgroundColorRes(R.color.alerter_confirm)
                            .show();
                }
                numCutOff = 0;
                if(loadToast != null) loadToast.error();
                restartCamera();
            }

        }
    }

    /**
     * 收款扫描的处理
     * 分开处理是因为在收款中若操作失败，会调用取消订单的接口
     */
    private void getPayOrderData(final String token, final String payOrderId) {
        final App app = (App) getApplication();
        app.getApiService().orderItem(token, payOrderId, json.toString(), new Callback<Order>() {
            @Override
            public void success(final Order order, Response response) {
                if (order != null) {
                    String itemId = order.getId();
                    if (itemId == null) {
                        if(loadToast != null) loadToast.error();
                        allowCancelledPayFailure(payOrderId);
                    } else if (itemId.startsWith("@")) {
                        if(loadToast != null) loadToast.hide();
                        numCutOff = 0;
                        if (order.getSwitch_id() != null) {
                            dialogShow = true;
                            logoutDialog1 = new HintDialog(ScannerActivity.this, "提示", order.getTitle(), "立即切换", new HintDialog.OnCustomDialogListener() {
                                @Override
                                public void back(String query) {
                                    if (query.equals("confirm")) {
                                        loadToast = new LoadToast(ScannerActivity.this);
                                        loadToast.setText("读取数据中");
                                        loadToast.setTranslationY(100);
                                        loadToast.show();
                                        logoutDialog1.progressBar.setVisibility(View.VISIBLE);
                                        logoutDialog1.comfirmBt.setVisibility(View.GONE);
                                        if (order.getSwitch_id() != null) {
                                            for (int i = 0; i < switchUserBeanArrayList.size(); i++) {
                                                if (order.getSwitch_id().equals(switchUserBeanArrayList.get(i).getId())) {
                                                    app.setLoginName(switchUserBeanArrayList.get(i).getLoginName());
                                                    app.setUserId(switchUserBeanArrayList.get(i).getId());
                                                    App app = (App) getApplication();
                                                    String title = switchUserBeanArrayList.get(i).getTitle();
                                                    if (title.indexOf("-") > 0) {
                                                        String[] s1 = title.split("-");
                                                        app.setTitle(s1[0]);
                                                    } else {
                                                        app.setTitle(title);
                                                    }
                                                    app.setToken(switchUserBeanArrayList.get(i).getToken());
                                                    if (payList != null) {
                                                        payList.clear();
                                                    }
                                                    if (waitList != null) {
                                                        waitList.clear();
                                                    }
                                                    if (cancelList != null) {
                                                        cancelList.clear();
                                                    }
                                                    checkLogin(switchUserBeanArrayList.get(i).getToken());
                                                }
                                            }
                                            getPayOrderData(app.getToken(), payOrderId);
                                        }
                                    }
                                    dialogShow = false;//标记初始化
                                    restartCamera();
                                    logoutDialog1.progressBar.setVisibility(View.GONE);
                                    logoutDialog1.comfirmBt.setVisibility(View.VISIBLE);
                                    logoutDialog1.dismiss();
                                }
                            });
                            logoutDialog1.setTitle("提示");
                            logoutDialog1.show();
                        } else {
                            Alerter.create(ScannerActivity.this)
                                    .setText(order.getTitle())
                                    .setBackgroundColorRes(R.color.alerter_confirm)
                                    .show();
                        }
                    } else {
                        if(loadToast != null) loadToast.success();
                        Alerter.create(ScannerActivity.this)
                                .setText("确认订单成功")
                                .setBackgroundColorRes(R.color.alerter_info)
                                .show();
                        Intent intent = new Intent(ScannerActivity.this, OrderDetailActivity.class);
                        intent.putExtra(OrderDetailActivity.EXTRA_ORDER_ITEM, order);
                        intent.putExtra("card", 2);//设置标记
                        intent.putExtra("id", orderId);
                        startActivity(intent);
                        finish();
                    }
                    if(!dialogShow)  restartCamera();

                } else {
                    if(loadToast != null) loadToast.error();
                    allowCancelledPayFailure(payOrderId);
                }
                if (logoutDialog1 != null) {
                    logoutDialog1.progressBar.setVisibility(View.GONE);
                    logoutDialog1.comfirmBt.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                if(!dialogShow) restartCamera();
                if (logoutDialog1 != null) {
                    logoutDialog1.progressBar.setVisibility(View.GONE);
                    logoutDialog1.comfirmBt.setVisibility(View.VISIBLE);
                }
                if(loadToast != null) loadToast.error();
                Alerter.create(ScannerActivity.this)
                        .setText("二维码扫描错误")
                        .setBackgroundColorRes(R.color.alerter_alert)
                        .show();
            }
        });
    }

    /**
     * 订单扫描的处理
     */
    private void getOrderItemData(final String token, final String getOrderId) {
        final App app = (App) getApplication();
        app.getApiService().orderItem(token, getOrderId, json.toString(), new Callback<Order>() {
            @Override
            public void success(final Order order, Response response) {
                if(loadToast != null) loadToast.hide();
                numNFC = 0;
                if (order != null) {
                    String itemId = order.getId();
                    String refunded = order.getRefunded();
                    if (itemId == null) {
                        if (order.getErrorCode() != null) {
                            String errorCode = order.getErrorCode();
                            if ("401".equals(errorCode)) {
                                dialogShow = true;
                                logoutDialog2 = new HintDialog(ScannerActivity.this, "提示", "登录过期，是否重新登录", "登  录", new HintDialog.OnCustomDialogListener() {
                                    @Override
                                    public void back(String query) {
                                        if (query.equals("confirm")) {
                                            app.setChecked("false");
                                            app.setJump("false");
                                            logoutDialog2.progressBar.setVisibility(View.VISIBLE);
                                            logoutDialog2.comfirmBt.setVisibility(View.GONE);
                                            LoginOutTools.loginPastDue(app, ScannerActivity.this);
                                        } else {
                                            logoutDialog2.progressBar.setVisibility(View.GONE);
                                            logoutDialog2.comfirmBt.setVisibility(View.VISIBLE);
                                        }
                                        dialogShow = false;//标记初始化
                                        restartCamera();
                                        logoutDialog2.dismiss();
                                    }
                                });
                                logoutDialog2.setTitle("提示");
                                logoutDialog2.show();
                            } else {
                                Alerter.create(ScannerActivity.this)
                                        .setText(order.getErrorMessage())
                                        .setBackgroundColorRes(R.color.alerter_confirm)
                                        .show();
                            }
                        } else {
                            Alerter.create(ScannerActivity.this)
                                    .setText("网络错误")
                                    .setBackgroundColorRes(R.color.alerter_confirm)
                                    .show();
                        }
                        if(loadToast != null) loadToast.error();
                    } else if (itemId.startsWith("@")) {
                        if (order.getSwitch_id() != null) {
                            if(loadToast != null) loadToast.error();
                            if (order.getSwitch_auto()) {
                                switchUser(order, app, getOrderId);
                            } else {
                                dialogShow = true;
                                logoutDialog2 = new HintDialog(ScannerActivity.this, "提示", order.getTitle(), "立即切换", new HintDialog.OnCustomDialogListener() {
                                    @Override
                                    public void back(String query) {
                                        if (query.equals("confirm")) {
                                            loadToast = new LoadToast(ScannerActivity.this);
                                            loadToast.setText("读取数据中");
                                            loadToast.setTranslationY(100);
                                            loadToast.show();
                                            logoutDialog2.progressBar.setVisibility(View.VISIBLE);
                                            logoutDialog2.comfirmBt.setVisibility(View.GONE);
                                            if (order.getSwitch_id() != null) {
                                                switchUser(order, app, getOrderId);
                                            } else {
                                                LoginOutTools.loginPastDue(app, ScannerActivity.this);
                                            }
                                        }
                                        dialogShow = false;//标记初始化
                                        restartCamera();
                                        logoutDialog2.progressBar.setVisibility(View.GONE);
                                        logoutDialog2.comfirmBt.setVisibility(View.VISIBLE);
                                        logoutDialog2.dismiss();
                                    }
                                });
                                logoutDialog2.setCanceledOnTouchOutside(false);
                                logoutDialog2.setTitle("提示");
                                logoutDialog2.show();
                            }

                        } else if (order.getMandatory()) {
                            restartCamera();
                            AlertDialog dialog = new AlertDialog.Builder(ScannerActivity.this)
                                    .setTitle("提示")
                                    .setMessage(order.getTitle())
                                    .setPositiveButton("关闭", null)
                                    .create();
                            dialog.show();
                            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLUE);
                        } else {
                            Alerter.create(ScannerActivity.this)
                                    .setText(order.getTitle())
                                    .setBackgroundColorRes(R.color.alerter_confirm)
                                    .show();
                        }
                        if(loadToast != null) loadToast.error();
                    } else {
                        if (logoutDialog2 != null) {
                            logoutDialog2.progressBar.setVisibility(View.GONE);
                            logoutDialog2.comfirmBt.setVisibility(View.VISIBLE);
                        }
                        if(loadToast != null) loadToast.success();
                        if (refunded != null) {
                            if (refunded.equals("@")) {
                                Alerter.create(ScannerActivity.this)
                                        .setText(R.string.qr_order_error)
                                        .setBackgroundColorRes(R.color.alerter_confirm)
                                        .show();
                            } else {
                                BluetoothAdapter mAdapter = BluetoothAdapter.getDefaultAdapter();
                                if (mAdapter.isEnabled()) {
                                    mAdapter.disable();
                                }
                                Intent intent = new Intent(ScannerActivity.this, OrderDetailActivity.class);
                                intent.putExtra(OrderDetailActivity.EXTRA_ORDER_ITEM, order);
                                intent.putExtra("card", 1);//设置标记
                                intent.putExtra("id", getOrderId);
                                startActivity(intent);
                                finish();
                            }
                        } else {
                            /*BluetoothAdapter mAdapter = BluetoothAdapter.getDefaultAdapter();
                            if (mAdapter.isEnabled()) {
                                mAdapter.disable();
                            }*///这个用于自动打印的时候可能有用
                            Intent intent = new Intent(ScannerActivity.this, OrderDetailActivity.class);
                            intent.putExtra(OrderDetailActivity.EXTRA_ORDER_ITEM, order);
                            intent.putExtra("card", 1);//设置标记
                            intent.putExtra("id", getOrderId);
                            startActivity(intent);
                            finish();
                        }
                    }
                } else {
                    Alerter.create(ScannerActivity.this)
                            .setText(R.string.qr_network_error)
                            .setBackgroundColorRes(R.color.alerter_confirm)
                            .show();
                    if(loadToast != null) loadToast.error();
                }
                if (logoutDialog2 != null) {
                    logoutDialog2.progressBar.setVisibility(View.GONE);
                    logoutDialog2.comfirmBt.setVisibility(View.VISIBLE);
                }
                numCutOff = 0;
                if(!dialogShow) restartCamera();
            }

            @Override
            public void failure(RetrofitError error) {
                if(!dialogShow) restartCamera();
                if (logoutDialog2 != null) {
                    logoutDialog2.progressBar.setVisibility(View.GONE);
                    logoutDialog2.comfirmBt.setVisibility(View.VISIBLE);
                }
                numCutOff = 0;
                if(loadToast != null) loadToast.error();
                Alerter.create(ScannerActivity.this)
                        .setText(R.string.qr_network_error)
                        .setBackgroundColorRes(R.color.alerter_alert)
                        .show();
            }
        });
    }

    private void switchUser(Order order, App app, String getOrderId) {
        for (int i = 0; i < switchUserBeanArrayList.size(); i++) {
            if (order.getSwitch_id().equals(switchUserBeanArrayList.get(i).getId())) {
                app.setLoginName(switchUserBeanArrayList.get(i).getLoginName());
                app.setUserId(switchUserBeanArrayList.get(i).getId());
                String title = switchUserBeanArrayList.get(i).getTitle();
                if (title.indexOf("-") > 0) {
                    String[] s1 = title.split("-");
                    app.setTitle(s1[0]);
                } else {
                    app.setTitle(title);
                }
                app.setToken(switchUserBeanArrayList.get(i).getToken());
                if (payList != null) {
                    payList.clear();
                }
                if (waitList != null) {
                    waitList.clear();
                }
                if (cancelList != null) {
                    cancelList.clear();
                }
                checkLogin(switchUserBeanArrayList.get(i).getToken());
            }
        }
        getOrderItemData(app.getToken(), getOrderId);
    }

    private void jumpToSimple() {
        Intent intent = new Intent();
        intent.setAction("callRefresh");
        intent.putExtra("refresh", "false");
        intent.putExtra("tab3refresh", "false");
        intent.putExtra("jump", "simple");
        sendBroadcast(intent);
    }

    private void jumpToShowList() {
        Intent intent = new Intent();
        intent.setAction("callRefresh");
        intent.putExtra("refresh", "false");
        intent.putExtra("tab3refresh", "false");
        intent.putExtra("jump", "showList");
        sendBroadcast(intent);
    }

    private void checkLogin(String token) {
        final App app = (App) getApplication();
        app.getApiService().checkLogin(token, new Callback<UserInfo>() {
            @Override
            public void success(UserInfo resultMessage, Response response) {
                if (resultMessage != null) {
                    if (resultMessage.getErrorCode() == 200) {
                        if (resultMessage.getResult().equals("ok")) {
                            String title_name = resultMessage.getMessage() + "-" + resultMessage.getName();
                            app.setTotalName(title_name);
                            app.setAllowPay(resultMessage.getAllow_pay());
                            String sowList = resultMessage.getAllow_show_list();
                            Intent intent = new Intent();
                            intent.setAction("callRefresh");
                            intent.putExtra("tab3refresh", "true");
                            if (app.getShowList().equals(sowList)) {//同一类型的账号切换，只刷新数据，不跳转
                                intent.putExtra("refresh", "true");
                                app.setJump("false");
                            } else {//不同一类型的账号切换，需要跳转
                                app.setShowList(sowList);
                                intent.putExtra("refresh", "false");
                                if (sowList.equals("true")) {//为true时跳到MainActivity，反之进入SimpleMainActivity
                                    app.setJump("showList");
                                } else {
                                    app.setJump("simple");
                                }
                            }
                            sendBroadcast(intent);
                        } else {
                            app.setJump("false");
                        }
                    } else {
                        app.setJump("false");
                    }
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Alerter.create(ScannerActivity.this)
                        .setText("网络连接错误")
                        .setBackgroundColorRes(R.color.alerter_alert)
                        .show();
            }
        });
    }

    private void getNFCAID(String token) {
        final App app = (App) getApplication();
        app.getApiService().checkLogin(token, new Callback<UserInfo>() {
            @Override
            public void success(UserInfo resultMessage, Response response) {
                if (resultMessage != null) {
                    if (resultMessage.getErrorCode() == 200) {
                        if (resultMessage.getResult().equals("ok")) {
                            nfcAID = resultMessage.getCard_prefix();
                        }
                    } else {
                        Alerter.create(ScannerActivity.this)
                                .setText("当前帐号登录过期")
                                .setBackgroundColorRes(R.color.alerter_confirm)
                                .show();
                    }
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Alerter.create(ScannerActivity.this)
                        .setText("网络连接错误")
                        .setBackgroundColorRes(R.color.alerter_alert)
                        .show();
            }
        });
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void onReceive(Context context, Intent intent) {
            App app = (App) context.getApplicationContext();
            if (app.getNFCResume() == 0) {
                final String action = intent.getAction();
                assert action != null;
                if (action.equals(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED)) {
                    final int state = intent.getIntExtra(NfcAdapter.EXTRA_ADAPTER_STATE,
                            NfcAdapter.STATE_OFF);
                    switch (state) {
                        case NfcAdapter.STATE_OFF:
                            noNFCText.setVisibility(View.GONE);
                            noNFC.setImageDrawable(getResources().getDrawable(R.mipmap.nfc_gray));
                            break;
                        case NfcAdapter.STATE_TURNING_OFF:
                            noNFCText.setVisibility(View.GONE);
                            noNFC.setImageDrawable(getResources().getDrawable(R.mipmap.nfc_gray));
                            break;
                        case NfcAdapter.STATE_ON:
                            enableForegroundDispatch();
                            noNFC.setImageDrawable(getResources().getDrawable(R.mipmap.nfc));
                            break;
                        case NfcAdapter.STATE_TURNING_ON:
                            enableForegroundDispatch();
                            noNFC.setImageDrawable(getResources().getDrawable(R.mipmap.nfc));
                            break;
                    }
                }
            }
        }
    };

    private void allowCancelledPayFailure(String payOrderId){
        Alerter.create(ScannerActivity.this)
                .setText("订单扫描失败，正在取消订单")
                .setBackgroundColorRes(R.color.alerter_confirm)
                .show();
        app.getApiService().allow_cancelled_pay(app.getToken(), payOrderId, new Callback<Order>() {
            @Override
            public void success(Order resultMessage, Response response) {
                if(!dialogShow) restartCamera();
                if (resultMessage != null) {
                    if("401".equals(resultMessage.getErrorCode())){
                        App app = (App) ScannerActivity.this.getApplication();
                        app.setJump("false");
                        LoginOutTools.loginPastDueFinish(app, ScannerActivity.this,false);
                    }else{
                        if(resultMessage.isRequest_another()){
                            AlertDialog dialog = new AlertDialog.Builder(ScannerActivity.this)
                                    .setTitle("提示")
                                    .setMessage(resultMessage.getErrorMessage())
                                    .setPositiveButton("切换账号", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent intent = new Intent(ScannerActivity.this, LoginActivity.class);
                                            intent.putExtra("allow_finish", "true");
                                            intent.putExtra("finish", "true");
                                            intent.putExtra("userName",app.getLoginName());
                                            intent.putExtra("outLogin", app.getShowList());//设标记，可用于返回当前界面
                                            startActivity(intent);
                                        }
                                    })
                                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    })
                                    .create();
                            dialog.show();
                            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLUE);
                            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.GRAY);
                            return;
                        }
                        Toast.makeText(ScannerActivity.this,"取消订单成功",Toast.LENGTH_SHORT).show();
                        if (app.getJump().equals("simple")) {
                            jumpToSimple();
                            app.setJump("false");
                        } else if (app.getJump().equals("showList")) {
                            jumpToShowList();
                            app.setJump("false");
                        }
                        finish();
                    }
                }
            }

            @Override
            public void failure(RetrofitError error) {
                if(!dialogShow) restartCamera();
                Alerter.create(ScannerActivity.this)
                        .setText("取消订单失败")
                        .setBackgroundColorRes(R.color.alerter_alert)
                        .show();
            }
        });
    }

    /**
     * 在这个方法中，若是收款未完成时退出，则调用取消订单接口
     */
    private void back() {
        if (orderId != null) {
            AlertDialog dialog = new AlertDialog.Builder(ScannerActivity.this)
                    .setTitle("提示")
                    .setMessage("正在处理订单，是否要取消订单")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {//添加确定按钮
                        @Override
                        public void onClick(DialogInterface dialog, int which) {//确定按钮的响应事件
                            app.getApiService().allow_cancelled_pay(app.getToken(), orderId, new Callback<Order>() {
                                @Override
                                public void success(Order resultMessage, Response response) {
                                    if (resultMessage != null) {
                                        if("401".equals(resultMessage.getErrorCode())){
                                            App app = (App) ScannerActivity.this.getApplication();
                                            app.setJump("false");
                                            LoginOutTools.loginPastDueFinish(app, ScannerActivity.this,false);
                                        }else{
                                            if(resultMessage.isRequest_another()){
                                                AlertDialog dialog = new AlertDialog.Builder(ScannerActivity.this)
                                                        .setTitle("提示")
                                                        .setMessage(resultMessage.getErrorMessage())
                                                        .setPositiveButton("切换账号", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                Intent intent = new Intent(ScannerActivity.this, LoginActivity.class);
                                                                intent.putExtra("allow_finish", "true");
                                                                intent.putExtra("finish", "true");
                                                                intent.putExtra("userName",app.getLoginName());
                                                                intent.putExtra("outLogin", app.getShowList());//设标记，可用于返回当前界面
                                                                startActivity(intent);
                                                            }
                                                        })
                                                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {

                                                            }
                                                        })
                                                        .create();
                                                dialog.show();
                                                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLUE);
                                                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.GRAY);
                                                return;
                                            }
                                            Toast.makeText(ScannerActivity.this,"取消订单成功",Toast.LENGTH_SHORT).show();
                                            if (app.getJump().equals("simple")) {
                                                jumpToSimple();
                                            } else if (app.getJump().equals("showList")) {
                                                jumpToShowList();
                                            }
                                            app.setJump("false");
                                            finish();
                                        }
                                    }
                                }

                                @Override
                                public void failure(RetrofitError error) {
                                    Alerter.create(ScannerActivity.this)
                                            .setText("取消订单失败")
                                            .setBackgroundColorRes(R.color.alerter_alert)
                                            .show();
                                }
                            });

                        }
                    }).setNegativeButton("取消", new DialogInterface.OnClickListener() {//添加返回按钮
                        @Override
                        public void onClick(DialogInterface dialog, int which) {//响应事件
                        }
                    }).create();
            dialog.show();
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLUE);
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.GRAY);
        } else {
            if (app.getJump().equals("simple")) {
                jumpToSimple();
            } else if (app.getJump().equals("showList")) {
                jumpToShowList();
            }
            app.setJump("false");
            finish();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            back();
        }
        return false;
    }

}
