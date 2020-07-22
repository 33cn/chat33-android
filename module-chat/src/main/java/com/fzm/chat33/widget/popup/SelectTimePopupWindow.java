package com.fzm.chat33.widget.popup;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.fzm.chat33.R;
import com.fzm.chat33.bean.ChooseTimeBean;
import com.fzm.chat33.widget.wheel.OnWheelChangedListener;
import com.fzm.chat33.widget.wheel.WheelView;
import com.fzm.chat33.widget.wheel.adapter.AbstractWheelTextAdapter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class SelectTimePopupWindow extends BasePopupWindow {

    private WheelView wvYear;

    private WheelView wvMonth;

    private FrameLayout root;

    private CalendarTextAdapter yearAdapter;
    private CalendarTextAdapter monthAdapter;
    private TextView tvTime;
    private TextView confirm;
    private TextView cancel;
    private LinkedHashMap<Integer, List<ChooseTimeBean>> hashMap;
    private SelectTimeCallBack callBack;

    private String currentData;
    private SimpleDateFormat sdf;
    private List<String> yearSL;
    private List<Integer> yearIL;

    private String currentMonth;
    private String currentYear;

    public SelectTimePopupWindow(Context context, View popupView, String currentData) {
        super(context, popupView);
        sdf = new SimpleDateFormat("yyyy/MM");
        this.currentData = currentData;
        hashMap = getMonthBetween("2017/01", sdf.format(System.currentTimeMillis()));
        String[] date = currentData.split("/");
        if (date.length == 2) {
            currentYear = context.getString(R.string.chat_date_year_str, date[0]);
            currentMonth = context.getString(R.string.chat_date_month_str, date[1]);
        } else {
            currentYear = context.getString(R.string.chat_date_year_str, date[0]);
            currentMonth = context.getString(R.string.chat_date_all_year);
        }
        findView();
        initData();
    }

    public void setCallBack(SelectTimeCallBack callBack) {
        this.callBack = callBack;
    }

    private void findView() {
        tvTime = mRootView.findViewById(R.id.tv_time);
        confirm = mRootView.findViewById(R.id.confirm);
        confirm.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callBack != null) {
                    callBack.result(currentData);
                }
                dismiss();
            }
        });
        cancel = mRootView.findViewById(R.id.cancel);
        cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        root = mRootView.findViewById(R.id.root);
        root.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                dismiss();
            }
        });

        wvYear = mRootView.findViewById(R.id.wv_year);
        wvMonth = mRootView.findViewById(R.id.wv_month);

        tvTime.setText(context.getString(R.string.chat_selected_date, currentYear + currentMonth));
    }

    private void initData() {

        yearSL = new ArrayList<>();
        yearIL = new ArrayList<>();
        for (Map.Entry<Integer, List<ChooseTimeBean>> entry : hashMap.entrySet()) {
            yearSL.add(context.getString(R.string.chat_date_year, entry.getKey()));
            yearIL.add(entry.getKey());
            if (hashMap.get(entry.getKey()) != null) {
                Collections.reverse(hashMap.get(entry.getKey()));
            }

        }
        Collections.reverse(yearSL);
        Collections.reverse(yearIL);
        wvYear.addChangingListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                currentData = yearIL.get(newValue) + "";
                tvTime.setText(context.getString(R.string.chat_selected_year_all_year, yearIL.get(newValue)));
                showMonth(yearIL.get(newValue));
            }
        });
        wvMonth.addChangingListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                if (newValue == 0) {
                    currentData = yearIL.get(wvYear.getCurrentItem()) + "";
                    tvTime.setText(context.getString(R.string.chat_selected_date_all_year, yearSL.get(wvYear.getCurrentItem())));
                } else {
                    int value = newValue - 1;
                    List<ChooseTimeBean> list = hashMap.get(yearIL.get(wvYear.getCurrentItem()));
                    if (list != null && value < list.size()) {
                        currentData = sdf.format(list.get(value).data);
                        tvTime.setText(context.getString(R.string.chat_selected_date, yearSL.get(wvYear.getCurrentItem()) + list.get(value).showTime));
                    }

                }
            }
        });
        if (yearSL.size() > 0) {
            int currentItem = yearSL.indexOf(currentYear);
            if (currentItem < 0) {
                currentItem = 0;
            }
            yearAdapter = new CalendarTextAdapter(context, yearSL, currentItem);
            yearAdapter.setTextColor(ContextCompat.getColor(context, R.color.chat_widget));
            yearAdapter.setTextSize(18);

            wvYear.setCurrentItem(currentItem);
            wvYear.setVisibleItems(5);
            wvYear.setViewAdapter(yearAdapter);
            showMonth(yearIL.get(currentItem));
        }
    }

    private void showMonth(int year) {
        ArrayList<String> Month = getMonth(year);
        if (Month != null) {
            int currentItem = Month.indexOf(currentMonth);
            if (currentItem < 0) {
                currentItem = 0;
            }
            monthAdapter = new CalendarTextAdapter(context, Month, currentItem);
            monthAdapter.setTextColor(ContextCompat.getColor(context, R.color.chat_widget));
            monthAdapter.setTextSize(18);
            wvMonth.setVisibleItems(5);
            wvMonth.setCurrentItem(currentItem);
            wvMonth.setViewAdapter(monthAdapter);
        }
    }

    private ArrayList<String> getMonth(int year) {
        List<ChooseTimeBean> list = hashMap.get(year);
        ArrayList<String> monthL = new ArrayList<>();
        monthL.add(context.getString(R.string.chat_date_all_year));
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                monthL.add(list.get(i).showTime);
            }
        }
        return monthL;
    }

    public interface SelectTimeCallBack {
        void result(String time);
    }

    private class CalendarTextAdapter extends AbstractWheelTextAdapter {
        List<String> list;

        protected CalendarTextAdapter(Context context, List<String> list,
                                      int currentItem) {
            super(context, R.layout.layout_wv_text, currentItem);
            this.list = list;
            setItemTextResource(R.id.text);
        }

        @Override
        public View getItem(int index, View cachedView, ViewGroup parent) {
            View view = super.getItem(index, cachedView, parent);
            return view;
        }

        @Override
        public int getItemsCount() {
            return list.size();
        }

        @Override
        protected CharSequence getItemText(int index) {
            return list.get(index) + "";
        }
    }
    /**
     * 获取时间段内所有的年月集合
     *
     * @param minDate 最小时间  2017-01
     * @param maxDate 最大时间 2017-10
     * @return 日期集合 格式为 key年,value月
     * @throws Exception
     */
    public LinkedHashMap<Integer, List<ChooseTimeBean>> getMonthBetween(String minDate, String maxDate) {
        LinkedHashMap<Integer, List<ChooseTimeBean>> result = new LinkedHashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM");//格式化为年月

        Calendar min = Calendar.getInstance();
        Calendar max = Calendar.getInstance();

        try {
            min.setTime(sdf.parse(minDate));
            min.set(min.get(Calendar.YEAR), min.get(Calendar.MONTH), 1);

            max.setTime(sdf.parse(maxDate));
            max.set(max.get(Calendar.YEAR), max.get(Calendar.MONTH), 2);
            Calendar curr = min;
            while (curr.before(max)) {
                List<ChooseTimeBean> list;
                if (result.get(curr.get(Calendar.YEAR)) == null) {
                    list = new ArrayList<>();

                } else {
                    list = result.get(curr.get(Calendar.YEAR));
                }
                ChooseTimeBean chooseTimeBean = new ChooseTimeBean();
                chooseTimeBean.data = curr.getTimeInMillis();
                chooseTimeBean.showTime = context.getString(R.string.chat_date_month, curr.get(Calendar.MONTH) + 1);
                list.add(chooseTimeBean);
                result.put(curr.get(Calendar.YEAR), list);
                curr.add(Calendar.MONTH, 1);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return result;
    }
}
