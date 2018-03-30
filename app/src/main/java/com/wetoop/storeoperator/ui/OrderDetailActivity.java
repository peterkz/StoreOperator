package com.wetoop.storeoperator.ui;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tapadoo.alerter.Alerter;
import com.wetoop.storeoperator.App;
import com.wetoop.storeoperator.BluetoothService;
import com.wetoop.storeoperator.R;
import com.wetoop.storeoperator.api.model.Order;
import com.wetoop.storeoperator.api.model.ResultMessage;
import com.wetoop.storeoperator.api.model.UserInfo;
import com.wetoop.storeoperator.bean.OrderBean;
import com.wetoop.storeoperator.bluetooth.BluetoothHandler;
import com.wetoop.storeoperator.sql.OrderSql;
import com.wetoop.storeoperator.sql.SwitchUserBean;
import com.wetoop.storeoperator.sql.SwitchUserSql;
import com.wetoop.storeoperator.tools.LoginOutTools;
import com.wetoop.storeoperator.ui.adapter.BluetoothPrinterAdapter;

import net.steamcrafted.loadtoast.LoadToast;

import org.json.JSONArray;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static com.wetoop.storeoperator.tools.StatusBarUtils.setWindowStatusBarColor;

public class OrderDetailActivity extends AppCompatActivity {
    public static final String EXTRA_ORDER_ITEM = "OrderActivity.EXTRA_ORDER_ITEM";
    private BluetoothHandler bluetoothHandler;
    String checked_n;
    private App app;
    private Order orderItem;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private String Purchased_sql = "";
    private String Purchased_service = "";
    private int countTest = 0;
    private LoadToast loadToast;
    /**
     * card
     * -1：列表点击进入
     * 1：二维码订单二维码
     * 2：付款
     * 3：订单号输入
     **/
    private int card;
    private String id, card2_id;
    private String statsUsedCome;
    private RelativeLayout backButton; // 返回按钮
    private TextView printButton; // 打印按钮
    private View statusColor; // 状态颜色
    private ImageView statusImageView; // 状态图标
    private TextView numberView; // 订单号
    private TextView nameView; // 商品名称
    private TextView customView; // 客户
    private TextView addressView;//地址
    private TextView createTimeView; // 付款时间
    private TextView usedTimeView;  // 使用时间
    private TextView controllerView; // 核销人员
    private TextView orderStatusText;//订单状态
    private TextView remarkView; // 备注
    private TextView reserveTimeView; // 预约时间
    private TextView scoreDeductibleView; // 积分抵扣
    private TextView advancePayView; // 其中预存
    private TextView paymentView; // 订单总价
    private TextView paymentViewImage; // 人民币符号
    private Button setUsedButton; // 设为已使用按钮

    private String emptyText = "<font color='#00B3EF'>(无)</font>";

    private void initView() {
        backButton = (RelativeLayout) findViewById(R.id.back_button);
        printButton = (TextView) findViewById(R.id.print_button);
        statusColor = findViewById(R.id.order_status_color);
        statusImageView = (ImageView) findViewById(R.id.order_status_view);
        numberView = (TextView) findViewById(R.id.order_number_view);
        nameView = (TextView) findViewById(R.id.order_name_view);
        customView = (TextView) findViewById(R.id.order_custom_view);
        createTimeView = (TextView) findViewById(R.id.order_create_time_view);
        usedTimeView = (TextView) findViewById(R.id.order_use_time_view);
        controllerView = (TextView) findViewById(R.id.order_controller_view);
        remarkView = (TextView) findViewById(R.id.order_remark_view);
        reserveTimeView = (TextView) findViewById(R.id.order_reserve_time_view);
        scoreDeductibleView = (TextView) findViewById(R.id.score_deductible_view);
        advancePayView = (TextView) findViewById(R.id.order_advance_pay_view);
        paymentView = (TextView) findViewById(R.id.order_payment_view);
        paymentViewImage = (TextView) findViewById(R.id.order_payment_view_image);
        orderStatusText = (TextView) findViewById(R.id.order_status_text);
        addressView = (TextView) findViewById(R.id.order_address_view);
        setUsedButton = (Button) findViewById(R.id.set_used_button);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setWindowStatusBarColor(OrderDetailActivity.this, R.color.title_clicked_color);
        setContentView(R.layout.activity_order_detail);
        app = (App) getApplication();
        app.addActivity(this);
        //判断是否从扫描（ScannerActivity）过来
        Intent card_intent = getIntent();
        card = card_intent.getIntExtra("card", -1);
        id = card_intent.getStringExtra("id");
        if (card_intent.getStringExtra("statsUsed") != null) {
            if (!card_intent.getStringExtra("statsUsed").equals("")) {
                statsUsedCome = card_intent.getStringExtra("statsUsed");
            }
        }
        if (card_intent.getParcelableExtra(EXTRA_ORDER_ITEM) != null) {
            if (!"".equals(card_intent.getParcelableExtra(EXTRA_ORDER_ITEM))) {
                orderItem = card_intent.getParcelableExtra(EXTRA_ORDER_ITEM);
            }
        }
        checked_n = app.getChecked();
        if ("true".equals(app.getShowList())) {
            if ("true".equals(checked_n)) {
                autoUseData(app);//自动打印使用
                if (countTest == 1) {
                    Alerter.create(OrderDetailActivity.this)
                            .setText("有新数据正在自动打印，手动打印无效")
                            .setBackgroundColorRes(R.color.alerter_confirm)
                            .show();
                }
            } else {
                checkLogin(app);//检测是否已经有登陆的
            }
        }
        initView();
        setClickedListener();
        setupDataView();
    }

    private void setupDataView() {
        if (card == 1 || card == 3) {
            catchCard1(id, orderItem);
        } else {
            catch_card2(id);
        }
        setupViewByStatus();
        nameView.setText("订购内容：" + orderItem.getTitle());
        paymentView.setText(String.format("%.2f", orderItem.getTotalPrice()));
        advancePayView.setText(getString(R.string.funds_pay) + "：" + orderItem.getFundsAdjust());
        numberView.setText(orderItem.getId());
        createTimeView.setText(getString(R.string.created) + dateFormat.format(orderItem.getCreatedAt()));
        if (orderItem.getPurchasedAt() != null) {
            orderStatusText.setText(R.string.status_purchased);
            orderStatusText.setTextColor(getResources().getColor(R.color.status_purchased));
            paymentView.setTextColor(getResources().getColor(R.color.status_purchased));
            paymentViewImage.setTextColor(getResources().getColor(R.color.status_purchased));
            if (orderItem.getUsed() == null) {
                usedTimeView.setText(Html.fromHtml(getString(R.string.used) + "<font color='#00B3EF'>（未使用）</font>"));
            } else {
                orderStatusText.setText(R.string.status_used);
                orderStatusText.setTextColor(getResources().getColor(R.color.status_used));
                paymentView.setTextColor(getResources().getColor(R.color.status_used));
                paymentViewImage.setTextColor(getResources().getColor(R.color.status_used));
                statusImageView.setImageResource(R.mipmap.order_used_icon);
                usedTimeView.setText(getString(R.string.used) + orderItem.getUsed());
            }
        } else {
            orderStatusText.setText(R.string.status_created);
            orderStatusText.setTextColor(getResources().getColor(R.color.status_wait));
            paymentView.setTextColor(getResources().getColor(R.color.status_wait));
            paymentViewImage.setTextColor(getResources().getColor(R.color.status_wait));
        }
        if (orderItem.getCancelledAt() != null) {
            statusImageView.setImageResource(R.mipmap.order_cancelled_icon);
            orderStatusText.setText(R.string.status_cancelled);
            orderStatusText.setTextColor(getResources().getColor(R.color.status_cancelled));
            paymentView.setTextColor(getResources().getColor(R.color.status_cancelled));
            paymentViewImage.setTextColor(getResources().getColor(R.color.status_cancelled));
        }
        if (!TextUtils.isEmpty(orderItem.getRefunded())) {
            setUsedButton.setVisibility(View.GONE);
            orderStatusText.setText(R.string.status_refund);
            orderStatusText.setTextColor(getResources().getColor(R.color.status_refund));
            paymentView.setTextColor(getResources().getColor(R.color.status_refund));
            paymentViewImage.setTextColor(getResources().getColor(R.color.status_refund));
            statusImageView.setImageResource(R.mipmap.order_refunded_icon);
        }
        if (!TextUtils.isEmpty(orderItem.getBonus_price()))
            scoreDeductibleView.setText("积分抵扣：" + orderItem.getBonus_price());
        else scoreDeductibleView.setText(Html.fromHtml("积分抵扣：" + emptyText));

        if (!TextUtils.isEmpty(orderItem.getOperator()))
            controllerView.setText("核销人员：" + orderItem.getOperator());
        else controllerView.setText(Html.fromHtml("核销人员：" + emptyText));

        if (!TextUtils.isEmpty(orderItem.getCustomer_note()))
            remarkView.setText("备注：" + orderItem.getCustomer_note());
        else remarkView.setText(Html.fromHtml("备注：" + emptyText));

        if (!TextUtils.isEmpty(orderItem.getAddress()))
            addressView.setText("寄送地址：" + orderItem.getAddress());
        else addressView.setText(Html.fromHtml("寄送地址：" + emptyText));

        if (!TextUtils.isEmpty(orderItem.getMobile()))
            customView.setText(getString(R.string.mobile) + orderItem.getMobile());
        else customView.setText(Html.fromHtml(getString(R.string.mobile) + emptyText));

        if (!TextUtils.isEmpty(orderItem.getBooking_at()))
            reserveTimeView.setText("预约日期：" + orderItem.getBooking_at());
        else reserveTimeView.setText(Html.fromHtml("预约日期：" + emptyText));

        stringToJsonArray();
    }

    private void setClickedListener() {
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (statsUsedCome != null) {
                    if (statsUsedCome.equals("come")) {
                        finish();
                    } else {
                        backFinish();
                    }
                } else {
                    backFinish();
                }

            }
        });

        if (card == 2) {
            printButton.setVisibility(View.GONE);
        } else {
            printButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (orderItem.getPurchasedAt() != null) {
                        if (checked_n.equals("true")) {
                            AlertDialog dialog = new AlertDialog.Builder(OrderDetailActivity.this)
                                    .setTitle("提示")
                                    .setMessage("是否要打印订单")
                                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            broadcastData();
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
                        } else {
                            bluetoothHandler = BluetoothHandler.newInstance(OrderDetailActivity.this, mHandler);
                            if (bluetoothHandler.bluetoothAdapter != null) {
                                boolean enabled = bluetoothHandler.bluetoothAdapter.isEnabled();
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {//如果 API level 是大于等于 23(Android 5.0) 时
                                    if (ContextCompat.checkSelfPermission(OrderDetailActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                        //请求权限
                                        ActivityCompat.requestPermissions(OrderDetailActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 10001);
                                    } else {
                                        openOrPrint(enabled);
                                    }
                                } else {
                                    openOrPrint(enabled);
                                }
                            } else {
                                Alerter.create(OrderDetailActivity.this)
                                        .setText("此设备不支持蓝牙功能")
                                        .setBackgroundColorRes(R.color.alerter_confirm)
                                        .show();
                            }
                        }
                    } else {
                        Alerter.create(OrderDetailActivity.this)
                                .setText("未付款的单不能打印")
                                .setBackgroundColorRes(R.color.alerter_confirm)
                                .show();
                    }
                }
            });
        }
    }

    private void openOrPrint(boolean enabled) {
        if (enabled) {
            bluetoothHandler.start();
            autoPrint();
        } else {
            openBluetooth();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 10001) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //用户允许改权限，0表示允许，-1表示拒绝 PERMISSION_GRANTED = 0， PERMISSION_DENIED = -1
                //这里进行授权被允许的处理
                openOrPrint(bluetoothHandler.bluetoothAdapter.isEnabled());
            } else {
                //这里进行权限被拒绝的处理
                Alerter.create(this)
                        .setText("蓝牙开启失败")
                        .setBackgroundColorRes(R.color.alerter_confirm)
                        .show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void openBluetooth() {
        Alerter.create(this)
                .setText("正在开启蓝牙，请稍后重试")
                .setBackgroundColorRes(R.color.alerter_info)
                .show();
        bluetoothHandler.bluetoothAdapter.enable();
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                bluetoothHandler.start();
            }
        };
        timer.schedule(task, 1000); //2秒后
    }

    /**
     * 根据订单状态设置界面实现样式
     */
    private void setupViewByStatus() {
        switch (orderItem.getStatus()) {
            case CREATED:
                usedTimeView.setText(Html.fromHtml(getString(R.string.purchased) + emptyText));
                statusColor.setBackgroundColor(getResources().getColor(R.color.status_wait));
                orderStatusText.setText(R.string.status_created);
                paymentView.setTextColor(getResources().getColor(R.color.status_wait));
                orderStatusText.setTextColor(getResources().getColor(R.color.status_wait));
                paymentViewImage.setTextColor(getResources().getColor(R.color.status_wait));
                statusImageView.setVisibility(View.GONE);
                if(card != 2)
                    setUsedButton.setVisibility(View.GONE);
                break;
            case PAID:
                statusColor.setBackgroundColor(getResources().getColor(R.color.status_purchased));
                statusImageView.setVisibility(View.GONE);
                orderStatusText.setText(R.string.status_purchased);
                paymentView.setTextColor(getResources().getColor(R.color.status_purchased));
                orderStatusText.setTextColor(getResources().getColor(R.color.status_purchased));
                paymentViewImage.setTextColor(getResources().getColor(R.color.status_purchased));
                if (card == -1 || card == 2) {
                    setUsedButton.setVisibility(View.GONE);
                } else {
                    setUsedButton.setVisibility(View.VISIBLE);
                    setUsedButton.setText("设为已使用");
                }
                break;
            case REFUNDED:
                usedTimeView.setText("退款时间：" + orderItem.getRefunded());
                statusColor.setBackgroundColor(getResources().getColor(R.color.status_refund));
                orderStatusText.setText(R.string.status_refund);
                paymentView.setTextColor(getResources().getColor(R.color.status_refund));
                orderStatusText.setTextColor(getResources().getColor(R.color.status_refund));
                paymentViewImage.setTextColor(getResources().getColor(R.color.status_refund));
                statusImageView.setVisibility(View.VISIBLE);
                statusImageView.setImageResource(R.mipmap.order_refunded_icon);
                setUsedButton.setVisibility(View.GONE);
                break;
            case CANCELLED:
                usedTimeView.setText("取消时间：" + dateFormat.format(orderItem.getCancelledAt()));
                statusColor.setBackgroundColor(getResources().getColor(R.color.status_cancelled));
                orderStatusText.setText(R.string.status_cancelled);
                paymentView.setTextColor(getResources().getColor(R.color.status_cancelled));
                orderStatusText.setTextColor(getResources().getColor(R.color.status_cancelled));
                paymentViewImage.setTextColor(getResources().getColor(R.color.status_cancelled));
                statusImageView.setVisibility(View.VISIBLE);
                statusImageView.setImageResource(R.mipmap.order_cancelled_icon);
                setUsedButton.setVisibility(View.GONE);
                break;
            case USED:
                usedTimeView.setText(getString(R.string.used) + orderItem.getUsed());
                statusColor.setBackgroundColor(getResources().getColor(R.color.status_purchased));
                orderStatusText.setText(R.string.status_used);
                paymentView.setTextColor(getResources().getColor(R.color.status_used));
                orderStatusText.setTextColor(getResources().getColor(R.color.status_used));
                paymentViewImage.setTextColor(getResources().getColor(R.color.status_used));
                statusImageView.setVisibility(View.VISIBLE);
                statusImageView.setImageResource(R.mipmap.order_used_icon);
                setUsedButton.setVisibility(View.GONE);
                break;
        }
    }

    /**
     * 显示“使用”按钮
     */
    public void catchCard1(final String id, final Order orderItem) {
        setUsedButton.setVisibility(View.VISIBLE);
        if (orderItem.getUsed() != null) {
            setUsedButton.setClickable(false);
            setUsedButton.setBackgroundColor(getResources().getColor(R.color.action_bar_color_status));
            statusImageView.setImageResource(R.mipmap.order_used_icon);
            usedTimeView.setText(getString(R.string.used) + orderItem.getUsed());
        } else {
            setUsedButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    loadToast = new LoadToast(OrderDetailActivity.this);
                    loadToast.setText("处理订单中");
                    loadToast.setTranslationY(100);
                    loadToast.show();
                    if (orderItem.getPurchasedAt() != null) {
                        setUsedButton.setVisibility(View.GONE);
                        setUsedButton.setClickable(false);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            setUsedButton.setBackground(getResources().getDrawable(R.drawable.treated_clicked));
                        }
                        app.getApiService().used(app.getToken(), id, new Callback<Order>() {
                            @Override
                            public void success(Order resultMessage, Response response) {
                                if (loadToast != null) loadToast.success();
                                if (resultMessage != null ) {
                                    if("401".equals(resultMessage.getErrorCode())) {
                                        App app = (App) OrderDetailActivity.this.getApplication();
                                        app.setJump("false");
                                        LoginOutTools.loginPastDueFinish(app, OrderDetailActivity.this,false);
                                    }else{
                                        if(resultMessage.isRequest_another()){
                                            AlertDialog dialog = new AlertDialog.Builder(OrderDetailActivity.this)
                                                    .setTitle("提示")
                                                    .setMessage(resultMessage.getErrorMessage())
                                                    .setPositiveButton("切换账号", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            Intent intent = new Intent(OrderDetailActivity.this, LoginActivity.class);
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
                                        Alerter.create(OrderDetailActivity.this)
                                                .setText("使用订单成功")
                                                .setBackgroundColorRes(R.color.alerter_info)
                                                .show();
                                        setUsedButton.setVisibility(View.VISIBLE);
                                        setUsedButton.setClickable(false);
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                            setUsedButton.setBackground(getResources().getDrawable(R.drawable.treated_clicked));
                                        }
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                        }
                                        statusImageView.setImageResource(R.mipmap.order_used_icon);
                                        setUsedButton.setText("已使用");
                                        usedTimeView.setText(getString(R.string.used));
                                        getOrderDetail(id);//
                                    }
                                } else {
                                    Alerter.create(OrderDetailActivity.this)
                                            .setText("处理订单出错")
                                            .setBackgroundColorRes(R.color.alerter_alert)
                                            .show();
                                }
                            }

                            @Override
                            public void failure(RetrofitError error) {
                                setUsedButton.setClickable(true);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                    setUsedButton.setBackground(getResources().getDrawable(R.drawable.untreated_background));
                                }
                                setUsedButton.setVisibility(View.VISIBLE);
                                Alerter.create(OrderDetailActivity.this)
                                        .setText("处理订单出错")
                                        .setBackgroundColorRes(R.color.alerter_alert)
                                        .show();
                            }
                        });
                    } else {
                        Alerter.create(OrderDetailActivity.this)
                                .setText("订单未付款")
                                .setBackgroundColorRes(R.color.alerter_confirm)
                                .show();
                    }
                }
            });
        }
    }

    /**
     * 不显示“使用”按钮
     */
    public void catch_card2(String id) {
        card2_id = id;
        if (orderItem.getPurchasedAt() == null) {
            setUsedButton.setVisibility(View.VISIBLE);
            setUsedButton.setText("刷新订单状态");
            setUsedButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setUsedButton.setVisibility(View.GONE);
                    getOrderDetail(card2_id);
                }
            });
        }else{
            setUsedButton.setVisibility(View.GONE);
        }
    }

    private void getOrderDetail(String id) {
        loadToast = new LoadToast(OrderDetailActivity.this);
        loadToast.setText("刷新订单状态中");
        loadToast.setTranslationY(100);
        loadToast.show();
        app.getApiService().orderItem(app.getToken(), id, json.toString(), new Callback<Order>() {
            @Override
            public void success(Order order, Response response) {
                if (order != null) {
                    String itemId = order.getId();
                    Log.e("itemId", itemId);
                    if (itemId == null || itemId.equals("@")) {
                        if (loadToast != null) loadToast.error();
                        setUsedButton.setVisibility(View.VISIBLE);
                        Alerter.create(OrderDetailActivity.this)
                                .setText("刷新失败")
                                .setBackgroundColorRes(R.color.alerter_confirm)
                                .show();
                    } else {
                        if (loadToast != null) loadToast.success();
                        orderItem = order;
                        setupViewByStatus();
                        if (order.getPurchasedAt() != null) {
                            AlphaAnimation alphaAnimation = (AlphaAnimation) AnimationUtils.loadAnimation(OrderDetailActivity.this, R.anim.anim_button_out);
                            setUsedButton.startAnimation(alphaAnimation);
                            setUsedButton.setClickable(false);
                            createTimeView.setText(getString(R.string.created) + dateFormat.format(order.getCreatedAt()));
                            if (order.getUsed() == null) {
                                usedTimeView.setText(Html.fromHtml(getString(R.string.used) + "<font color='#00B3EF'>（未使用）</font>"));
                            }
                        } else {
                            setUsedButton.setVisibility(View.VISIBLE);
                            Alerter.create(OrderDetailActivity.this)
                                    .setText("等待付款状态")
                                    .setBackgroundColorRes(R.color.alerter_confirm)
                                    .show();
                        }
                    }
                } else {
                    if (loadToast != null) loadToast.error();
                    setUsedButton.setVisibility(View.VISIBLE);
                    Alerter.create(OrderDetailActivity.this)
                            .setText("刷新失败")
                            .setBackgroundColorRes(R.color.alerter_confirm)
                            .show();
                }
            }

            @Override
            public void failure(RetrofitError error) {
                if (loadToast != null) loadToast.error();
                setUsedButton.setVisibility(View.VISIBLE);
                setUsedButton.setClickable(true);
                Alerter.create(OrderDetailActivity.this)
                        .setText("刷新失败")
                        .setBackgroundColorRes(R.color.alerter_alert)
                        .show();
            }
        });
    }

    private JSONArray json = new JSONArray();
    private ArrayList<SwitchUserBean> switchUserBeanArrayList = new ArrayList<>();

    private void stringToJsonArray() {
        switchUserBeanArrayList = switchUserList();
        for (int i = 0; i < switchUserBeanArrayList.size(); i++) {
            json.put(switchUserBeanArrayList.get(i).getToken());
        }
    }

    private ArrayList<SwitchUserBean> switchUserList() {
        SwitchUserSql cameraSql = new SwitchUserSql(OrderDetailActivity.this);
        return cameraSql.queryDataToSQLite();
    }

    public void autoPrint() {
        bluetoothHandler.showDeviceList(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(OrderDetailActivity.this, BluetoothService.class);
                intent.putExtra("buletooth", bluetoothHandler.getAdapter().getItem(position));
                intent.putExtra("orderItem", orderItem);
                intent.setPackage(getPackageName());
                startService(intent);
            }
        });
    }

    private void checkLogin(final App app) {
        app.getApiService().checkLogin(app.getToken(), new Callback<UserInfo>() {
            @Override
            public void success(UserInfo resultMessage, Response response) {
                if (resultMessage != null) {
                    if (resultMessage.getErrorCode() == 401 && app.getLoginTab() == 0) {
                        Message msg = new Message();
                        msg.what = 1;//登陆失效
                        mHandler_code.sendMessage(msg);
                    }
                } else {
                    Alerter.create(OrderDetailActivity.this)
                            .setText("数据加载失败")
                            .setBackgroundColorRes(R.color.alerter_confirm)
                            .show();
                }
            }

            @Override
            public void failure(RetrofitError error) {
            }
        });
    }

    public void autoUseData(final App app) {
        //获取数据
        app.getApiService().orderList(app.getToken(), 0, new Callback<List<Order>>() {
            @Override
            public void success(List<Order> orders, Response response) {
                if (orders == null) {
                    Toast.makeText(OrderDetailActivity.this, "数据加载失败", Toast.LENGTH_SHORT).show();
                    return;
                }
                int count = 0;
                if (orders.size() > 0) {
                    Order item = orders.get(0);
                    Purchased_service = item.getId();
                    OrderSql os = new OrderSql(OrderDetailActivity.this);
                    ArrayList<OrderBean> sql_list = os.getPurchasedId();
                    for (int j = 0; j < sql_list.size(); j++) {
                        Purchased_sql = sql_list.get(j).getId();
                        if (!Purchased_service.equals(Purchased_sql)) {
                            count = 1;
                        }
                    }
                    if (count == 1) {
                        countTest = 1;
                    }
                }
            }

            @Override
            public void failure(RetrofitError error) {
            }
        });
    }

    void broadcastData() {
        Intent broadcast = new Intent();
        broadcast.setAction("print");
        broadcast.putExtra("orderItem", orderItem);
        sendBroadcast(broadcast);
        Message msg = new Message();
        msg.what = 24;//标志是哪个线程传数据
        mHandler.sendMessage(msg);//发送message信息
        printButton.setClickable(false);
        printButton.setText("正在连接");
    }

    private void checkThread() {
        int count = 10 * 1000;//如果发送验证码成功，则使验证码的按钮失效60s
        TimeCount time = new TimeCount(count, 1000);
        time.start();
    }

    private class TimeCount extends CountDownTimer {
        public TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);//参数依次为总时长,和计时的时间间隔
        }

        @Override
        public void onFinish() {//计时完毕时触发
            printButton.setClickable(true);
            printButton.setText("打印");
        }

        @Override
        public void onTick(long millisUntilFinished) {//计时过程显示
        }
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BluetoothHandler.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothHandler.STATE_CONNECTED:   //已连接
                            break;
                        case BluetoothHandler.STATE_CONNECTING:  //正在连接
                            break;
                        case BluetoothHandler.STATE_LISTEN:     //监听连接的到来
                            break;
                        case BluetoothHandler.STATE_NONE:
                            break;
                    }
                    break;
                case BluetoothHandler.MESSAGE_CONNECTION_LOST:    //蓝牙已断开连接
                    break;
                case BluetoothHandler.MESSAGE_UNABLE_CONNECT:     //无法连接设备
                    Alerter.create(OrderDetailActivity.this)
                            .setText(R.string.bluetooth_error)
                            .setBackgroundColorRes(R.color.alerter_confirm)
                            .show();
                    break;
                case 24:
                    checkThread();
                    Alerter.create(OrderDetailActivity.this)
                            .setText("正在请求打印，请勿多次点击···")
                            .setBackgroundColorRes(R.color.alerter_confirm)
                            .show();
                    break;
            }
        }

    };
    /**
     * 登陆有冲突时获取子线程的信息，并且跳回登陆界面
     */
    Handler mHandler_code = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    App app = (App) OrderDetailActivity.this.getApplication();
                    app.setJump("false");
                    LoginOutTools.loginPastDueFinish(app, OrderDetailActivity.this);
                    break;
            }
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            if (statsUsedCome != null) {
                if (statsUsedCome.equals("come")) {
                    finish();
                } else {
                    backFinish();
                }
            } else {
                backFinish();
            }
        }
        return false;
    }

    private void backFinish() {
        if (card == 2) {//“收款”，返回时若检测到订单未付款则取消订单
            payBack();
        } else if (card == 1) {//二维码扫描
            Intent intent = new Intent(OrderDetailActivity.this, ScannerActivity.class);
            startActivity(intent);
            finish();
        } else {
            App app = (App) getApplication();
            Intent intent = new Intent();
            intent.setAction("callRefresh");
            intent.putExtra("refresh", "false");
            intent.putExtra("tab3refresh", "false");
            if (app.getJump().equals("simple")) {
                intent.putExtra("jump", "simple");
            } else if (app.getJump().equals("showList")) {
                intent.putExtra("jump", "showList");
            }
            app.setJump("false");
            sendBroadcast(intent);
            finish();
        }
    }

    private void payBack() {
        if (orderItem.getPurchasedAt() == null) {
            AlertDialog dialog = new AlertDialog.Builder(OrderDetailActivity.this)
                    .setTitle("提示")
                    .setMessage("正在处理订单，是否要取消订单")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {//添加确定按钮
                        @Override
                        public void onClick(DialogInterface dialog, int which) {//确定按钮的响应事件
                            app.getApiService().allow_cancelled_pay(app.getToken(), id, new Callback<Order>() {
                                @Override
                                public void success(Order resultMessage, Response response) {
                                    if (resultMessage != null) {
                                        if("401".equals(resultMessage.getErrorCode())){
                                            App app = (App) OrderDetailActivity.this.getApplication();
                                            app.setJump("false");
                                            LoginOutTools.loginPastDueFinish(app, OrderDetailActivity.this,false);
                                        }else{
                                            if(resultMessage.isRequest_another()){
                                                AlertDialog dialog = new AlertDialog.Builder(OrderDetailActivity.this)
                                                        .setTitle("提示")
                                                        .setMessage(resultMessage.getErrorMessage())
                                                        .setPositiveButton("切换账号", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                Intent intent = new Intent(OrderDetailActivity.this, LoginActivity.class);
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
                                            Toast.makeText(OrderDetailActivity.this,"取消订单成功",Toast.LENGTH_SHORT).show();
                                            App app = (App) getApplication();
                                            Intent intent = new Intent();
                                            intent.setAction("callRefresh");
                                            intent.putExtra("refresh", "false");
                                            intent.putExtra("tab3refresh", "false");
                                            if (app.getJump().equals("simple")) {
                                                intent.putExtra("jump", "simple");
                                            } else if (app.getJump().equals("showList")) {
                                                intent.putExtra("jump", "showList");
                                            }
                                            app.setJump("false");
                                            sendBroadcast(intent);
                                            finish();
                                        }
                                    }
                                }

                                @Override
                                public void failure(RetrofitError error) {
                                    Alerter.create(OrderDetailActivity.this)
                                            .setText("取消订单失败，请重试")
                                            .setBackgroundColorRes(R.color.alerter_alert)
                                            .show();
                                }

                            });

                        }
                    }).setNegativeButton("取消", new DialogInterface.OnClickListener() {//添加返回按钮
                        @Override
                        public void onClick(DialogInterface dialog, int which) {//响应事件

                        }
                    })
                    .create();
            dialog.show();
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLUE);
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.GRAY);
        } else {
            Intent intent = new Intent();
            intent.setAction("callRefresh");
            intent.putExtra("refresh", "false");
            intent.putExtra("tab3refresh", "false");
            if (app.getJump().equals("simple")) {
                intent.putExtra("jump", "simple");
            } else if (app.getJump().equals("showList")) {
                intent.putExtra("jump", "showList");
            }
            app.setJump("false");
            sendBroadcast(intent);
            finish();
        }
    }
}