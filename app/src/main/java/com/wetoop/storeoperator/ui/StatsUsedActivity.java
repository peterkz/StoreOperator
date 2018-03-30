package com.wetoop.storeoperator.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tapadoo.alerter.Alerter;
import com.wetoop.storeoperator.App;
import com.wetoop.storeoperator.R;
import com.wetoop.storeoperator.api.model.Order;
import com.wetoop.storeoperator.api.model.StatsUsedData;
import com.wetoop.storeoperator.bean.PriceBean;
import com.wetoop.storeoperator.tools.LoginOutTools;
import com.wetoop.storeoperator.ui.adapter.StatsUsedAdapter;
import com.wetoop.storeoperator.ui.dialog.HintDialog;
import com.wetoop.storeoperator.ui.widget.WheelView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static com.wetoop.storeoperator.tools.StatusBarUtils.setWindowStatusBarColor;

/**
 * Created by Administrator on 2016/11/11.
 */
public class StatsUsedActivity extends Activity implements View.OnClickListener, DatePicker.OnDateChangedListener {
    private TextView totalOrder, totalPrice, noOrder, noOrderMain,
            startTimeText, endTimeText, priceType, priceNum, title_text;
    private RelativeLayout startTime, endTime;
    public ArrayList<String> list1 = new ArrayList<>();//年（初始）
    public ArrayList<String> list2 = new ArrayList<>();//月（初始）
    public ArrayList<String> list3 = new ArrayList<>();//日（初始）
    public ArrayList<String> list4 = new ArrayList<>();//时（初始）
    public ArrayList<String> list5 = new ArrayList<>();//分（初始）
    public ArrayList<String> list1Last = new ArrayList<>();//年（结束）
    public ArrayList<String> list2Last = new ArrayList<>();//月（结束）
    public ArrayList<String> list3Last = new ArrayList<>();//日（结束）
    public ArrayList<String> list4Last = new ArrayList<>();//时（结束）
    public ArrayList<String> list5Last = new ArrayList<>();//分（结束）
    private String yearTextTimeFirst, monthTextTimeFirst, dayTextTimeFirst, hourTextTimeFirst, minuteTextTimeFirst;
    private String text1TimeLast, text2TimeLast, text3TimeLast, text4TimeLast, text5TimeLast;
    private int totalOrderNum, totalPriceNum = 0;
    private Double totalOrderPrice;
    private ListView listView;
    private String yearNow = "";
    private String monthNow = "";
    private String dayNow = "";
    private String hourNow = "";
    private String minuteNow = "";
    private String timeFirst = "", timeLast = "";
    private Boolean checkedTimeFirst = false;
    private Boolean checkedChangeFirst = false;
    private Boolean checkedChangeLast = false;
    private WheelView wv4, wv5, wv4Last, wv5Last;
    private SwipeRefreshLayout swipeRefreshLayout;
    private DatePicker datePicker1, datePicker2;
    private Calendar caFirstMain = Calendar.getInstance();//得到一个Calendar的实例
    private ArrayList<Order> originalList = new ArrayList<Order>();
    private ArrayList<PriceBean> priceBeanList = new ArrayList<PriceBean>();
    private Boolean priceWrite = true;
    private String userName = "", codeStr = "";
    private boolean sumDisplay;
    private StatsUsedAdapter statsAdapter;
    private HintDialog logoutDialog;
    private TextView dayTextView;
    private TextView weekTextView;
    private TextView monthTextView;
    private String dayStart;
    private String dateEnd;
    private String weekStart;
    private String monthStart;
    private boolean searchBySelect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats_used);
        setWindowStatusBarColor(StatsUsedActivity.this, R.color.title_clicked_color);
        initView();
        initListAdapter();
        setDataView();
        setViewListener();
    }

    private void initView() {
        listView = (ListView) findViewById(R.id.listView);
        listView.addHeaderView(LayoutInflater.from(this).inflate(R.layout.stats_used_list_header, null));
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        title_text = (TextView) findViewById(R.id.title_text);
        totalOrder = (TextView) findViewById(R.id.totalOrder);
        totalPrice = (TextView) findViewById(R.id.totalPrice);
        startTime = (RelativeLayout) findViewById(R.id.startTime);
        endTime = (RelativeLayout) findViewById(R.id.endTime);
        startTimeText = (TextView) findViewById(R.id.startTimeChoice);
        endTimeText = (TextView) findViewById(R.id.endTimeChoice);
        noOrder = (TextView) findViewById(R.id.noOrder);
        noOrderMain = (TextView) findViewById(R.id.noOrderMain);
        priceType = (TextView) findViewById(R.id.priceType);
        priceNum = (TextView) findViewById(R.id.priceNum);
        dayTextView = (TextView) findViewById(R.id.search_by_day);
        weekTextView = (TextView) findViewById(R.id.search_by_week);
        monthTextView = (TextView) findViewById(R.id.search_by_month);
        initSearchDate();
    }

    private void initSearchDate() {
        final SimpleDateFormat formatStart = new SimpleDateFormat("yyyyMMdd000000");
        final SimpleDateFormat formatEnd = new SimpleDateFormat("yyyyMMddHHmmss");
        final SimpleDateFormat formatStartText = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
        final SimpleDateFormat formatEndText = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


        final Calendar dayCalendar = Calendar.getInstance();
        dayStart = formatStart.format(new Date());
        dateEnd = formatEnd.format(new Date());
        dayTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchBySelect = true;
                dateEnd = formatEnd.format(new Date());
                String dayStartText = formatStartText.format(new Date());
                String dateEndText = formatEndText.format(new Date());
                startTimeText.setText(dayStartText);
                endTimeText.setText(dateEndText);
                swipeRefreshLayout.setRefreshing(true);
                timeFirst = dayStart;
                timeLast = dateEnd;

                yearTextTimeFirst = String.valueOf(dayCalendar.get(Calendar.YEAR));
                monthTextTimeFirst = String.valueOf(dayCalendar.get(Calendar.MONTH));
                dayTextTimeFirst = String.valueOf(dayCalendar.get(Calendar.DAY_OF_MONTH));
                hourTextTimeFirst = "00";
                minuteTextTimeFirst = "00";
                caFirstMain.set(Integer.valueOf(yearTextTimeFirst), Integer.valueOf(monthTextTimeFirst), Integer.valueOf(dayTextTimeFirst));

                getDate(dayStart, dateEnd);
                selectDay(true);
            }
        });

        final Calendar weekCalendar = Calendar.getInstance();
        int thisWeekDay = weekCalendar.get(Calendar.DAY_OF_WEEK);
        weekCalendar.setTime(new Date());
        weekCalendar.add(Calendar.DATE, -(thisWeekDay == 1 ? 6 : thisWeekDay - 2));
        weekStart = formatStart.format(weekCalendar.getTime());
        weekTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchBySelect = true;
                dateEnd = formatEnd.format(new Date());
                String weekStartText = formatStartText.format(weekCalendar.getTime());
                String dateEndText = formatEndText.format(new Date());
                startTimeText.setText(weekStartText);
                endTimeText.setText(dateEndText);
                swipeRefreshLayout.setRefreshing(true);
                timeFirst = weekStart;
                timeLast = dateEnd;

                yearTextTimeFirst = String.valueOf(weekCalendar.get(Calendar.YEAR));
                monthTextTimeFirst = String.valueOf(weekCalendar.get(Calendar.MONTH));
                dayTextTimeFirst = String.valueOf(weekCalendar.get(Calendar.DAY_OF_MONTH));
                hourTextTimeFirst = "00";
                minuteTextTimeFirst = "00";
                caFirstMain.set(Integer.valueOf(yearTextTimeFirst), Integer.valueOf(monthTextTimeFirst), Integer.valueOf(dayTextTimeFirst));

                getDate(weekStart, dateEnd);
                selectWeek(true);
            }
        });


        final Calendar monthCalendar = Calendar.getInstance();
        int thisMonthDay = monthCalendar.get(Calendar.DAY_OF_MONTH);
        monthCalendar.setTime(new Date());
        monthCalendar.add(Calendar.DATE, -(thisMonthDay - 1));
        monthStart = formatStart.format(monthCalendar.getTime());
        monthTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchBySelect = true;
                dateEnd = formatEnd.format(new Date());
                String monthStartText = formatStartText.format(monthCalendar.getTime());
                String dateEndText = formatEndText.format(new Date());
                startTimeText.setText(monthStartText);
                endTimeText.setText(dateEndText);
                swipeRefreshLayout.setRefreshing(true);
                timeFirst = monthStart;
                timeLast = dateEnd;

                yearTextTimeFirst = String.valueOf(monthCalendar.get(Calendar.YEAR));
                monthTextTimeFirst = String.valueOf(monthCalendar.get(Calendar.MONTH));
                dayTextTimeFirst = String.valueOf(monthCalendar.get(Calendar.DAY_OF_MONTH));
                hourTextTimeFirst = "00";
                minuteTextTimeFirst = "00";
                caFirstMain.set(Integer.valueOf(yearTextTimeFirst), Integer.valueOf(monthTextTimeFirst), Integer.valueOf(dayTextTimeFirst));

                getDate(monthStart, dateEnd);
                selectMonth(true);
            }
        });
    }

    private void selectMonth(boolean select) {
        dayTextView.setBackgroundColor(getResources().getColor(R.color.line_gray));
        dayTextView.setTextColor(getResources().getColor(R.color.light_grey));
        weekTextView.setBackgroundColor(getResources().getColor(R.color.line_gray));
        weekTextView.setTextColor(getResources().getColor(R.color.light_grey));
        if (select) {
            monthTextView.setBackgroundResource(R.color.title_color);
        } else {
            monthTextView.setBackgroundColor(getResources().getColor(R.color.line_gray));
        }
        monthTextView.setTextColor(getResources().getColor(select ? R.color.white : R.color.light_grey));
    }

    private void selectWeek(boolean select) {
        dayTextView.setBackgroundColor(getResources().getColor(R.color.line_gray));
        dayTextView.setTextColor(getResources().getColor(R.color.light_grey));
        if (select) {
            weekTextView.setBackgroundResource(R.color.title_color);
        } else {
            weekTextView.setBackgroundColor(getResources().getColor(R.color.line_gray));
        }
        weekTextView.setTextColor(getResources().getColor(select ? R.color.white : R.color.light_grey));
        monthTextView.setBackgroundColor(getResources().getColor(R.color.line_gray));
        monthTextView.setTextColor(getResources().getColor(R.color.light_grey));
    }

    private void selectDay(boolean select) {
        if (select) {
            dayTextView.setBackgroundResource(R.color.title_color);
        } else {
            dayTextView.setBackgroundColor(getResources().getColor(R.color.line_gray));
        }
        dayTextView.setTextColor(getResources().getColor(select ? R.color.white : R.color.light_grey));
        weekTextView.setBackgroundColor(getResources().getColor(R.color.line_gray));
        weekTextView.setTextColor(getResources().getColor(R.color.light_grey));
        monthTextView.setBackgroundColor(getResources().getColor(R.color.line_gray));
        monthTextView.setTextColor(getResources().getColor(R.color.light_grey));
    }


    private void initListAdapter() {
        statsAdapter = new StatsUsedAdapter(StatsUsedActivity.this, originalList);
        listView.setAdapter(statsAdapter);
        statsAdapter.notifyDataSetChanged();
    }

    private void setDataView() {
        App app = (App) getApplication();
        if (app.getTotalName() != null) {
            if (app.getTotalName().indexOf("-") > 0) {
                String[] name = app.getTotalName().split("-");
                userName = name[1];
            }
        }
        noOrderMain.setText("请选择开始时间和结束时间");
    }

    private void setViewListener() {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    Order item = originalList.get(position - 1);
                    Intent intent = new Intent(StatsUsedActivity.this, OrderDetailActivity.class);
                    intent.putExtra(OrderDetailActivity.EXTRA_ORDER_ITEM, item);
                    intent.putExtra("statsUsed", "come");
                    startActivity(intent);
                }
            }
        });
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                noOrder.setVisibility(View.GONE);
                if (timeFirst != null && timeLast != null) {
                    if (!timeFirst.equals("") && !timeLast.equals("")) {
                        getDate(timeFirst, timeLast);
                    } else {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                } else {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        });
        startTime.setOnClickListener(this);
        endTime.setOnClickListener(this);
        RelativeLayout back = (RelativeLayout) findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void getTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ");
        Date curDate = new Date(System.currentTimeMillis());
        String str = formatter.format(curDate);
        String time = "";
        if (str != null && !"".equals(str)) {
            for (int i = 0; i < str.length(); i++) {
                if (str.charAt(i) >= 48 && str.charAt(i) <= 57) {
                    time += str.charAt(i);
                }
            }
        }
        String year = time.substring(0, 4);
        String month = time.substring(4, 6);
        String day = time.substring(6, 8);
        String hour = time.substring(8, 10);
        String minute = time.substring(10, 12);
        yearNow = year;
        monthNow = month;
        dayNow = day;
        hourNow = hour;
        minuteNow = minute;
        for (int num2 = 0; num2 <= Integer.valueOf(hourNow); num2++) {
            list4.add(String.valueOf(num2));
        }
        for (int num3 = 0; num3 <= Integer.valueOf(minuteNow) - 1; num3++) {
            list5.add(String.valueOf(num3));
        }
    }

    private void getLastTime() {
        list4Last.clear();
        list5Last.clear();
        text1TimeLast = null;
        text2TimeLast = null;
        text3TimeLast = null;
        text4TimeLast = null;
        text5TimeLast = null;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ");
        Date curDate = new Date(System.currentTimeMillis());
        String str = formatter.format(curDate);
        String time = "";
        if (str != null && !"".equals(str)) {
            for (int i = 0; i < str.length(); i++) {
                if (str.charAt(i) >= 48 && str.charAt(i) <= 57) {
                    time += str.charAt(i);
                }
            }
        }
        String year = time.substring(0, 4);
        String month = time.substring(4, 6);
        String day = time.substring(6, 8);
        String hour = time.substring(8, 10);
        String minute = time.substring(10, 12);
        yearNow = year;
        monthNow = month;
        dayNow = day;
        hourNow = hour;
        minuteNow = minute;
        for (int num2 = 0; num2 <= Integer.valueOf(hourNow); num2++) {
            list4Last.add(String.valueOf(num2));
        }

        if (Integer.valueOf(monthTextTimeFirst).equals(Integer.valueOf(month)) && Integer.valueOf(dayTextTimeFirst).equals(Integer.valueOf(dayNow)) && Integer.valueOf(hourTextTimeFirst).equals(Integer.valueOf(hourNow))) {
            for (int num3 = Integer.valueOf(minuteNow); num3 <= Integer.valueOf(minuteNow); num3++) {
                list5Last.add(String.valueOf(num3));
            }
        } else {
            for (int num3 = 0; num3 <= Integer.valueOf(minuteNow); num3++) {
                list5Last.add(String.valueOf(num3));
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.startTime:
                checkedTimeFirst = true;
                getTime();
                noOrder.setVisibility(View.GONE);
                noOrderMain.setVisibility(View.GONE);
                if (yearTextTimeFirst == null) {
                    yearTextTimeFirst = yearNow;
                }
                if (monthTextTimeFirst == null) {
                    if (Integer.valueOf(monthNow) < 10) {
                        monthTextTimeFirst = "0" + String.valueOf(Integer.valueOf(monthNow));
                    } else {
                        monthTextTimeFirst = String.valueOf(Integer.valueOf(monthNow));
                    }

                }
                if (dayTextTimeFirst == null) {
                    if (Integer.valueOf(dayNow) < 10) {
                        dayTextTimeFirst = "0" + String.valueOf(Integer.valueOf(dayNow));
                    } else {
                        dayTextTimeFirst = String.valueOf(Integer.valueOf(dayNow));
                    }

                }
                if (hourTextTimeFirst == null) {
                    hourTextTimeFirst = "00";
                }
                if (minuteTextTimeFirst == null) {
                    minuteTextTimeFirst = "00";
                }
                Calendar caFirst = Calendar.getInstance();
                Calendar caLast = Calendar.getInstance();
                caFirst.setTime(new Date()); //月份是从0开始的，所以11表示12月
                caLast.setTime(new Date());
                caFirst.add(Calendar.DAY_OF_YEAR, -60);
                View outerView1 = LayoutInflater.from(this).inflate(R.layout.wheel_view, null);
                datePicker1 = (DatePicker) outerView1.findViewById(R.id.datepicker);
                wv4 = (WheelView) outerView1.findViewById(R.id.wheel_view_wv4);
                wv5 = (WheelView) outerView1.findViewById(R.id.wheel_view_wv5);
                datePicker1.setMinDate(caFirst.getTimeInMillis());
                datePicker1.setMaxDate(caLast.getTimeInMillis());
                init(datePicker1);

                wv4.setOffset(0);
                wv4.setItemsReflush(list4);
                if (timeFirst != null) {
                    if (!timeFirst.equals("")) {
                        wv4.setSeletion(Integer.valueOf(hourTextTimeFirst));
                    } else {
                        wv4.setSeletion(0);
                    }
                } else {
                    wv4.setSeletion(0);
                }
                wv4.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (event.getAction() == MotionEvent.ACTION_UP) {
                            wv4.getParent().requestDisallowInterceptTouchEvent(false);
                        } else {
                            wv4.getParent().requestDisallowInterceptTouchEvent(true);
                        }
                        return false;
                    }
                });
                wv4.setOnWheelViewListener(new WheelView.OnWheelViewListener() {
                    @Override
                    public void onSelected(int selectedIndex, String item) {

                        if (Integer.valueOf(item) < 10) {
                            hourTextTimeFirst = "0" + item;
                        } else {
                            hourTextTimeFirst = item;
                        }

                        if (item.equals(hourNow) && Integer.valueOf(monthTextTimeFirst).equals(Integer.valueOf(monthNow)) && Integer.valueOf(dayTextTimeFirst).equals(Integer.valueOf(dayNow))) {
                            list5.clear();
                            for (int num3 = 0; num3 <= Integer.valueOf(minuteNow); num3++) {
                                list5.add(String.valueOf(num3));
                            }
                            wv5.setOffset(0);
                            wv5.setItemsReflush(list5);
                            wv5.setSeletion(0);
                            minuteTextTimeFirst = "00";
                        } else {
                            if (checkedChangeFirst) {
                                list5.clear();
                                for (int num3 = 0; num3 < 60; num3++) {
                                    list5.add(String.valueOf(num3));
                                }
                                wv5.setOffset(0);
                                wv5.setItemsReflush(list5);
                                wv5.setSeletion(0);
                                minuteTextTimeFirst = "00";
                            }
                        }
                    }
                });
                wv5.setOffset(0);
                wv5.setItemsReflush(list5);
                if (timeFirst != null) {
                    if (!timeFirst.equals("")) {
                        wv5.setSeletion(Integer.valueOf(minuteTextTimeFirst));
                    } else {
                        wv5.setSeletion(0);
                    }
                } else {
                    wv5.setSeletion(0);
                }
                wv5.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (event.getAction() == MotionEvent.ACTION_UP) {
                            wv5.getParent().requestDisallowInterceptTouchEvent(false);
                        } else {
                            wv5.getParent().requestDisallowInterceptTouchEvent(true);
                        }
                        return false;
                    }
                });
                wv5.setOnWheelViewListener(new WheelView.OnWheelViewListener() {
                    @Override
                    public void onSelected(int selectedIndex, String item) {
                        if (Integer.valueOf(item) < 10) {
                            minuteTextTimeFirst = "0" + item;
                        } else {
                            minuteTextTimeFirst = item;
                        }
                    }
                });

                final AlertDialog dialog1 = new AlertDialog.Builder(this).create();
                dialog1.setTitle("选择起始时间");
                dialog1.setView(outerView1);
                Button sureButton = (Button) outerView1.findViewById(R.id.sure_button);
                RelativeLayout cancelButton = (RelativeLayout) outerView1.findViewById(R.id.cancel_button);
                sureButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String checkMonth = "";
                        if ((Integer.valueOf(monthTextTimeFirst) + 1) < 10) {
                            checkMonth = "0" + String.valueOf(Integer.valueOf(monthTextTimeFirst) + 1);
                        } else {
                            checkMonth = String.valueOf(Integer.valueOf(monthTextTimeFirst) + 1);
                        }
                        timeFirst = yearTextTimeFirst + checkMonth + dayTextTimeFirst + hourTextTimeFirst + minuteTextTimeFirst + "00";
                        caFirstMain.set(Integer.valueOf(yearTextTimeFirst), Integer.valueOf(monthTextTimeFirst), Integer.valueOf(dayTextTimeFirst));
                        caFirstMain.set(Calendar.HOUR_OF_DAY, caFirstMain.getMinimum(Calendar.HOUR_OF_DAY));
                        caFirstMain.set(Calendar.MINUTE, caFirstMain.getMinimum(Calendar.MINUTE));
                        caFirstMain.set(Calendar.SECOND, caFirstMain.getMinimum(Calendar.SECOND));
                        caFirstMain.set(Calendar.MILLISECOND, caFirstMain.getMinimum(Calendar.MILLISECOND));
                        startTimeText.setText(yearTextTimeFirst + "-" + checkMonth + "-" + dayTextTimeFirst + " " + hourTextTimeFirst + ":" + minuteTextTimeFirst);
                        if (timeLast != null) {
                            if (!timeLast.equals("")) {
                                timeLast = "";
                                endTimeText.setText("请选择");
                            }
                        }
                        dialog1.dismiss();
                    }
                });
                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog1.dismiss();
                    }
                });
                dialog1.show();
                onDateChanged(null, Integer.parseInt(yearNow), Integer.parseInt(monthNow), Integer.parseInt(dayNow));
                break;
            case R.id.endTime:
                checkedTimeFirst = false;
                noOrder.setVisibility(View.GONE);
                noOrderMain.setVisibility(View.GONE);
                if (timeFirst != null) {
                    if (!timeFirst.equals("")) {
                        getLastTime();
                        if (text1TimeLast == null) {
                            text1TimeLast = yearNow;
                        }
                        if (text2TimeLast == null) {
                            if (Integer.valueOf(monthNow) < 10) {
                                text2TimeLast = "0" + String.valueOf(Integer.valueOf(monthNow));
                            } else {
                                text2TimeLast = String.valueOf(Integer.valueOf(monthNow));
                            }
                        }
                        if (text3TimeLast == null) {
                            if (Integer.valueOf(dayNow) < 10) {
                                text3TimeLast = "0" + dayNow;
                            } else {
                                text3TimeLast = dayNow;
                            }
                        }
                        if (text4TimeLast == null) {
                            if (Integer.valueOf(hourNow) < 10) {
                                text4TimeLast = "0" + String.valueOf(Integer.valueOf(hourNow));
                            } else {
                                text4TimeLast = String.valueOf(Integer.valueOf(hourNow));
                            }
                        }
                        if (text5TimeLast == null) {
                            if (Integer.valueOf(minuteNow) < 10) {
                                text5TimeLast = "0" + String.valueOf(Integer.valueOf(minuteNow));
                            } else {
                                text5TimeLast = String.valueOf(Integer.valueOf(minuteNow));
                            }
                        }
                        Calendar caLast11 = Calendar.getInstance();//得到一个Calendar的实例
                        caLast11.setTime(new Date());
                        caLast11.set(Calendar.HOUR_OF_DAY, caLast11.getMaximum(Calendar.HOUR_OF_DAY));
                        caLast11.set(Calendar.MINUTE, caLast11.getMaximum(Calendar.MINUTE));
                        caLast11.set(Calendar.SECOND, caLast11.getMaximum(Calendar.SECOND));
                        caLast11.set(Calendar.MILLISECOND, caLast11.getMaximum(Calendar.MILLISECOND));
                        View outerView2 = LayoutInflater.from(this).inflate(R.layout.wheel_view_end, null);
                        wv4Last = (WheelView) outerView2.findViewById(R.id.wheel_view_wv4);
                        wv5Last = (WheelView) outerView2.findViewById(R.id.wheel_view_wv5);
                        datePicker2 = (DatePicker) outerView2.findViewById(R.id.datepicker);
                        datePicker2.setMaxDate(caLast11.getTimeInMillis());
                        datePicker2.setMinDate(caFirstMain.getTimeInMillis());
                        datePicker2.setOnTouchListener(new View.OnTouchListener() {
                            @Override
                            public boolean onTouch(View v, MotionEvent event) {
                                if (event.getAction() == MotionEvent.ACTION_UP) {
                                    datePicker2.getParent().requestDisallowInterceptTouchEvent(false);
                                } else {
                                    datePicker2.getParent().requestDisallowInterceptTouchEvent(true);
                                }
                                return false;
                            }
                        });
                        init(datePicker2);

                        wv4Last.setOffset(0);
                        wv4Last.setItemsReflush(list4Last);
                        wv4Last.setSeletion(Integer.valueOf(hourNow));
                        wv4Last.setOnTouchListener(new View.OnTouchListener() {
                            @Override
                            public boolean onTouch(View v, MotionEvent event) {
                                if (event.getAction() == MotionEvent.ACTION_UP) {
                                    wv4Last.getParent().requestDisallowInterceptTouchEvent(false);
                                } else {
                                    wv4Last.getParent().requestDisallowInterceptTouchEvent(true);
                                }
                                return false;
                            }
                        });
                        wv4Last.setOnWheelViewListener(new WheelView.OnWheelViewListener() {
                            @Override
                            public void onSelected(int selectedIndex, String item) {
                                if (Integer.valueOf(item) < 10) {
                                    text4TimeLast = "0" + item;
                                } else {
                                    text4TimeLast = item;
                                }
                                if (item.equals(hourNow) && Integer.valueOf(text2TimeLast).equals(Integer.valueOf(monthNow)) && Integer.valueOf(text3TimeLast).equals(Integer.valueOf(dayNow))) {
                                    list5Last.clear();
                                    if (Integer.valueOf(text4TimeLast).equals(Integer.valueOf(hourTextTimeFirst))) {
                                        for (int num3 = Integer.valueOf(minuteTextTimeFirst); num3 < Integer.valueOf(minuteNow); num3++) {
                                            list5Last.add(String.valueOf(num3));
                                        }
                                        wv5Last.setOffset(0);
                                        wv5Last.setItemsReflush(list5Last);
                                        wv5Last.setSeletion(Integer.valueOf(minuteNow));
                                        text5TimeLast = minuteTextTimeFirst;
                                    } else {
                                        for (int num3 = 0; num3 < Integer.valueOf(minuteNow); num3++) {
                                            list5Last.add(String.valueOf(num3));
                                        }
                                        wv5Last.setOffset(0);
                                        wv5Last.setItemsReflush(list5Last);
                                        wv5Last.setSeletion(0);
                                        text5TimeLast = "00";
                                    }
                                } else {
                                    if (checkedChangeFirst) {
                                        list5Last.clear();
                                        if (Integer.valueOf(text4TimeLast).equals(Integer.valueOf(hourTextTimeFirst))) {
                                            for (int num3 = Integer.valueOf(minuteTextTimeFirst); num3 < 60; num3++) {
                                                list5Last.add(String.valueOf(num3));
                                            }
                                            wv5Last.setOffset(0);
                                            wv5Last.setItemsReflush(list5Last);
                                            wv5Last.setSeletion(0);
                                            text5TimeLast = "00";
                                        } else {
                                            for (int num3 = 0; num3 < 60; num3++) {
                                                list5Last.add(String.valueOf(num3));
                                            }
                                            wv5Last.setOffset(0);
                                            wv5Last.setItemsReflush(list5Last);
                                            wv5Last.setSeletion(0);
                                            text5TimeLast = "00";
                                        }
                                    }
                                }
                            }
                        });
                        wv5Last.setOffset(0);
                        wv5Last.setItemsReflush(list5Last);
                        wv5Last.setSeletion(Integer.valueOf(minuteNow));
                        wv5Last.setOnTouchListener(new View.OnTouchListener() {
                            @Override
                            public boolean onTouch(View v, MotionEvent event) {
                                if (event.getAction() == MotionEvent.ACTION_UP) {
                                    wv5Last.getParent().requestDisallowInterceptTouchEvent(false);
                                } else {
                                    wv5Last.getParent().requestDisallowInterceptTouchEvent(true);
                                }
                                return false;
                            }
                        });
                        wv5Last.setOnWheelViewListener(new WheelView.OnWheelViewListener() {
                            @Override
                            public void onSelected(int selectedIndex, String item) {
                                if (Integer.valueOf(item) < 10) {
                                    text5TimeLast = "0" + item;
                                } else {
                                    text5TimeLast = item;
                                }
                            }
                        });
                        final AlertDialog dialog2 = new AlertDialog.Builder(StatsUsedActivity.this).create();
                        dialog2.setTitle("选择结束时间");
                        dialog2.setView(outerView2);
                        sureButton = (Button) outerView2.findViewById(R.id.sure_button);
                        cancelButton = (RelativeLayout) outerView2.findViewById(R.id.cancel_button);
                        sureButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                selectDay(false);
                                selectWeek(false);
                                selectMonth(false);
                                String checkMonth = "";
                                if ((Integer.valueOf(text2TimeLast) + 1) < 10) {
                                    checkMonth = "0" + String.valueOf(Integer.valueOf(text2TimeLast) + 1);
                                } else {
                                    checkMonth = String.valueOf(Integer.valueOf(text2TimeLast) + 1);
                                }
                                timeLast = text1TimeLast + checkMonth + text3TimeLast + text4TimeLast + text5TimeLast + "00";
                                endTimeText.setText(text1TimeLast + "-" + checkMonth + "-" + text3TimeLast + " " + text4TimeLast + ":" + text5TimeLast);
                                swipeRefreshLayout.setRefreshing(true);
                                if (timeFirst != null) {
                                    if (!timeFirst.equals("")) {
                                        getDate(timeFirst, timeLast);
                                    }
                                }
                                dialog2.dismiss();
                            }
                        });
                        cancelButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog2.dismiss();
                            }
                        });

                        dialog2.show();
                        onDateChanged(null, Integer.parseInt(yearNow), Integer.parseInt(monthNow), Integer.parseInt(dayNow));
                    } else {
                        Alerter.create(StatsUsedActivity.this)
                                .setText("请先选择开始时间")
                                .setBackgroundColorRes(R.color.alerter_info)
                                .show();
                    }
                }
                break;
            case R.id.select_button:
//                selectSpinnerWindow.showAsDropDown(selectButton);
//                transparentBackground.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void getDate(String startTime, String endTime) {
        if (!TextUtils.isEmpty(startTime) && !searchBySelect && new SimpleDateFormat("yyyyMMddHHmmss").equals(endTime)) {
            if (startTime.equals(dayStart)) {
                selectDay(true);
            } else if (startTime.equals(weekStart)) {
                selectWeek(true);
            } else if (startTime.equals(monthStart)) {
                selectMonth(true);
            }
        }
        priceBeanList.clear();
        final App app = (App) getApplication();
        app.getApiService().usedOrder(app.getToken(), startTime, endTime, new Callback<StatsUsedData>() {

            @Override
            public void success(StatsUsedData statsUsedData, Response response) {
                searchBySelect = false;
                totalPriceNum = 0;
                swipeRefreshLayout.setRefreshing(false);
                if (statsUsedData.getUserName() == null && statsUsedData.getList() == null) {
                    if (statsUsedData.getErrorCode() != null) {
                        String errorCode = statsUsedData.getErrorCode();
                        if ("401".equals(errorCode)) {
                            logoutDialog = new HintDialog(StatsUsedActivity.this, "提示", "登录过期，是否重新登录", "登  录", new HintDialog.OnCustomDialogListener() {
                                @Override
                                public void back(String query) {
                                    if (query.equals("confirm")) {
                                        LoginOutTools.loginPastDueFinish(app, StatsUsedActivity.this);
                                    } else {
                                        logoutDialog.progressBar.setVisibility(View.GONE);
                                        logoutDialog.comfirmBt.setVisibility(View.VISIBLE);
                                    }
                                    logoutDialog.dismiss();
                                }
                            });
                            logoutDialog.setTitle("提示");
                            logoutDialog.show();
                        } else {
                            noOrder.setVisibility(View.VISIBLE);
                            noOrder.setText("加载失败:" + statsUsedData.getErrorMessage());
                        }
                    } else {
                        noOrder.setVisibility(View.VISIBLE);
                        noOrder.setText("加载失败:" + statsUsedData.getErrorMessage());
                    }
                } else {
                    sumDisplay = statsUsedData.isSumDisplay();
                    if (statsUsedData.getList().size() > 0) {
                        originalList.clear();
                        originalList.addAll(statsUsedData.getList());
                        totalOrderNum = originalList.size();
                        if (originalList.size() > 0) {
                            Double total1 = 0.00;
                            Double total2 = 0.00;
                            String priceName = "";
                            String priceId = "";
                            for (int listNum = 0; listNum < originalList.size(); listNum++) {
                                total1 = originalList.get(listNum).getTotalPrice() + total1;
                                if (originalList.get(listNum).getBonus_price() != null) {
                                    total2 = Double.valueOf(originalList.get(listNum).getBonus_price()) + total2;
                                }
                                if (originalList.get(listNum).getDetails().size() > 0) {
                                    totalPriceNum++;
                                }
                                for (int detailNum = 0; detailNum < originalList.get(listNum).getDetails().size(); detailNum++) {
                                    PriceBean priceBean = new PriceBean();
                                    priceName = statsUsedData.getList().get(listNum).getDetails().get(detailNum).getDetailsName();
                                    priceId = statsUsedData.getList().get(listNum).getDetails().get(detailNum).getDetailsId();

                                    priceWrite = true;
                                    for (int j = 0; j < priceBeanList.size(); j++) {
                                        if (priceId.equals(priceBeanList.get(j).getId())) {
                                            priceWrite = false;
                                        }
                                    }
                                    int num1 = 0;
                                    if (priceWrite) {
                                        priceBean.setId(priceId);
                                        priceBean.setPriceType(priceName);
                                        for (int y = 0; y < statsUsedData.getList().size(); y++) {

                                            for (int i = 0; i < statsUsedData.getList().get(y).getDetails().size(); i++) {
                                                if (statsUsedData.getList().get(y).getDetails().get(i).getDetailsId().equals(priceId)) {
                                                    num1 = statsUsedData.getList().get(y).getDetails().get(i).getDetailsQty() + num1;
                                                }
                                            }
                                        }
                                        priceBean.setPriceNum(num1);
                                        priceBeanList.add(priceBean);
                                    }
                                }
                            }
                            totalOrderPrice = total1 - total2;
                            Message msg = new Message();
                            msg.what = 1;//标志是哪个线程传数据
                            mHandler_code.sendMessage(msg);//发送message信息
                        } else if (originalList.size() == 0) {
                            totalOrderPrice = 0.0;
                            Message msg = new Message();
                            msg.what = 2;//标志是哪个线程传数据
                            mHandler_code.sendMessage(msg);//发送message信息
                        }
                    } else {
                        totalOrderPrice = 0.0;
                        Message msg = new Message();
                        msg.what = 2;//标志是哪个线程传数据
                        mHandler_code.sendMessage(msg);//发送message信息
                    }

                }
            }

            @Override
            public void failure(RetrofitError error) {
                searchBySelect = false;
                swipeRefreshLayout.setRefreshing(false);
                noOrder.setVisibility(View.VISIBLE);
                if (originalList.size() < 1)
                    noOrderMain.setVisibility(View.VISIBLE);
                if (error.getKind() == RetrofitError.Kind.CONVERSION) {
                    Alerter.create(StatsUsedActivity.this)
                            .setText("登陆失效")
                            .setBackgroundColorRes(R.color.alerter_confirm)
                            .show();
                } else {
                    totalPrice.setText("--");
                    totalOrder.setText("--");
                    noOrder.setVisibility(View.VISIBLE);
                    noOrder.setText("加载失败");
                    originalList.clear();
                    priceType.setText("--");
                    priceNum.setText("--");
                    statsAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    Handler mHandler_code = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    noOrderMain.setVisibility(View.GONE);
                    noOrder.setVisibility(View.GONE);
                    String result = String.format("%.2f", totalOrderPrice);
                    totalOrder.setText(String.valueOf(totalOrderNum));
                    if (sumDisplay) {
                        totalPrice.setText(result);
                        title_text.setText("全部核销的订单");
                    } else {
                        if (!userName.equals(""))
                            title_text.setText(userName + " 核销的订单");
                    }

                    String priceTypeTextStr = "";
                    String priceNumTextStr = "";
                    for (int i = 0; i < priceBeanList.size(); i++) {
                        priceTypeTextStr = priceBeanList.get(i).getPriceType() + priceTypeTextStr;
                        priceNumTextStr = priceBeanList.get(i).getPriceNum() + priceNumTextStr;
                    }
                    if (totalOrderNum > totalPriceNum) {
                        priceType.setText(priceTypeTextStr + "\n\n" + "其他");
                        int otherNum = totalOrderNum - totalPriceNum;
                        priceNum.setText(priceNumTextStr + "\n\n" + String.valueOf(otherNum));
                    } else {
                        priceType.setText(priceTypeTextStr);
                        priceNum.setText(priceNumTextStr);
                    }
                    statsAdapter.notifyDataSetChanged();
                    break;
                case 2:
                    if (sumDisplay) {
                        totalPrice.setText("--");
                        title_text.setText("全部核销的订单");
                    } else {
                        if (!userName.equals(""))
                            title_text.setText(userName + " 核销的订单");
                    }
                    totalOrder.setText("0");
                    noOrder.setVisibility(View.VISIBLE);
                    if (sumDisplay) {
                        noOrder.setText("无核销订单");
                    } else {
                        noOrder.setText(userName + "无核销订单");
                    }
                    originalList.clear();
                    priceType.setText("--");
                    priceNum.setText("--");
                    statsAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };

    public void init(DatePicker datePicker) {
        Calendar calendar = Calendar.getInstance();

        datePicker.init(calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH), this);
    }

    @Override
    public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        if (checkedTimeFirst) {
            yearTextTimeFirst = String.valueOf(datePicker1.getYear());
            if (monthOfYear < 10) {
                monthTextTimeFirst = "0" + String.valueOf(datePicker1.getMonth());
            } else {
                monthTextTimeFirst = String.valueOf(datePicker1.getMonth());
            }
            if (dayOfMonth < 10) {
                dayTextTimeFirst = "0" + String.valueOf(datePicker1.getDayOfMonth());
            } else {
                dayTextTimeFirst = String.valueOf(datePicker1.getDayOfMonth());
            }

            if (yearTextTimeFirst.equals(yearNow) && String.valueOf(Integer.valueOf(monthTextTimeFirst) + 1).equals(monthNow) && dayTextTimeFirst.equals(dayNow)) {
                list4.clear();
                for (int num2 = 0; num2 <= Integer.valueOf(hourNow); num2++) {
                    list4.add(String.valueOf(num2));
                }
                wv4.setOffset(0);
                wv4.setItemsReflush(list4);
                wv4.setSeletion(0);
                hourTextTimeFirst = "00";
                list5.clear();
                for (int num3 = 0; num3 <= Integer.valueOf(minuteNow); num3++) {
                    list5.add(String.valueOf(num3));
                }
                wv5.setOffset(0);
                wv5.setItemsReflush(list5);
                wv5.setSeletion(0);
                minuteTextTimeFirst = "00";
                checkedChangeFirst = true;
            } else {
                if (checkedChangeFirst) {
                    list4.clear();
                    for (int num2 = 0; num2 < 24; num2++) {
                        list4.add(String.valueOf(num2));
                    }
                    wv4.setOffset(0);
                    wv4.setItemsReflush(list4);
                    wv4.setSeletion(0);
                    hourTextTimeFirst = "00";
                    list5.clear();
                    for (int num3 = 0; num3 < 60; num3++) {
                        list5.add(String.valueOf(num3));
                    }
                    wv5.setOffset(0);
                    wv5.setItemsReflush(list5);
                    wv5.setSeletion(0);
                    minuteTextTimeFirst = "00";
                    checkedChangeFirst = false;
                }
            }
        } else {
            text1TimeLast = String.valueOf(datePicker2.getYear());
            if (monthOfYear < 10) {
                text2TimeLast = "0" + String.valueOf(datePicker2.getMonth());
            } else {
                text2TimeLast = String.valueOf(datePicker2.getMonth());
            }
            if (dayOfMonth < 10) {
                text3TimeLast = "0" + String.valueOf(datePicker2.getDayOfMonth());
            } else {
                text3TimeLast = String.valueOf(datePicker2.getDayOfMonth());
            }
            if (text1TimeLast.equals(yearNow) && String.valueOf(Integer.valueOf(text2TimeLast) + 1).equals(monthNow) && text3TimeLast.equals(dayNow)) {
                list4Last.clear();
                for (int num2 = 0; num2 <= Integer.valueOf(hourNow); num2++) {
                    list4Last.add(String.valueOf(num2));
                }
                wv4Last.setOffset(0);
                wv4Last.setItemsReflush(list4);
                wv4Last.setSeletion(Integer.valueOf(hourNow));
                text4TimeLast = hourNow;
                list5Last.clear();
                for (int num3 = 0; num3 <= Integer.valueOf(minuteNow); num3++) {
                    list5Last.add(String.valueOf(num3));
                }
                wv5Last.setOffset(0);
                wv5Last.setItemsReflush(list5Last);
                wv5Last.setSeletion(Integer.valueOf(minuteNow));
                text5TimeLast = minuteNow;
                checkedChangeLast = true;
            } else {
                if (checkedChangeLast) {
                    list4Last.clear();
                    for (int num2 = 0; num2 < 24; num2++) {
                        list4Last.add(String.valueOf(num2));
                    }
                    wv4Last.setOffset(0);
                    wv4Last.setItemsReflush(list4);
                    wv4Last.setSeletion(0);
                    text4TimeLast = "00";
                    list5Last.clear();
                    for (int num3 = 0; num3 < 60; num3++) {
                        list5Last.add(String.valueOf(num3));
                    }
                    wv5Last.setOffset(0);
                    wv5Last.setItemsReflush(list5Last);
                    wv5Last.setSeletion(0);
                    text5TimeLast = "00";
                    checkedChangeLast = false;
                }
            }
        }
    }
}
