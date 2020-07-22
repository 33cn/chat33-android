package com.fzm.chat33.redpacket.activity;

import android.graphics.Typeface;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.flyco.tablayout.SegmentTabLayout;
import com.flyco.tablayout.listener.OnTabSelectListener;
import com.fuzamei.common.recycleviewbase.CommonAdapter;
import com.fuzamei.common.recycleviewbase.MultiItemTypeAdapter;
import com.fuzamei.common.recycleviewbase.ViewHolder;
import com.fuzamei.common.utils.BarUtils;
import com.fuzamei.common.utils.DateUtils;
import com.fuzamei.componentservice.app.AppRoute;
import com.fuzamei.componentservice.base.DILoadableActivity;
import com.fzm.chat33.R;
import com.fzm.chat33.bean.ChooseTimeBean;
import com.fzm.chat33.bean.CoinFilterBean;
import com.fzm.chat33.core.Chat33;
import com.fzm.chat33.core.bean.RedPacketCoin;
import com.fzm.chat33.core.provider.CoinManager;
import com.fzm.chat33.global.AppConst;
import com.fzm.chat33.redpacket.fragment.RedPacketRecordFragment;
import com.fzm.chat33.redpacket.mvvm.PacketRecordViewModel;
import com.pl.wheelview.WheelView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

/**
 * 创建日期：2018/9/11 on 16:31
 * 描述:
 * 作者:wdl
 */
@Route(path = AppRoute.RED_PACKET_RECORDS, extras = AppConst.NEED_LOGIN)
public class RedPacketRecordActivity extends DILoadableActivity implements View.OnClickListener {

    SegmentTabLayout stl_title;
    View fl_title;
    TextView tvRecordNum;
    TextView tvRecordAmount;
    TextView tvTime;
    LinearLayout ly_time;
    TextView tv_return;

    DrawerLayout drawer_layout;
    WheelView wv_year;
    WheelView wv_month;
    RecyclerView rv_coin;
    View reset, confirm;

    private CommonAdapter<CoinFilterBean> coinAdapter;
    private String currentDate;
    private String tempDate;
    private SimpleDateFormat sdf;
    private HashMap<Integer, List<ChooseTimeBean>> hashMap;

    // 用于记录真实选中状态
    private ArrayList<CoinFilterBean> coinList = new ArrayList<>();
    // 用于记录临时选中状态
    private SparseBooleanArray stateList = new SparseBooleanArray();

    @Inject
    public ViewModelProvider.Factory provider;
    private PacketRecordViewModel viewModel;

    private CoinFilterBean currentCoin;
    private boolean confirmFilter = false;

    private List<Integer> yearList = new ArrayList<>();
    private List<String> yearStrList = new ArrayList<>();
    private List<String> monthList = new ArrayList<>();

    private int currentYear = 2017;
    private final String allYear = Chat33.getContext().getString(R.string.chat_date_all_year);
    private String currentMonth = allYear;

    private final String[] mTitles = Chat33.getContext().getResources().getStringArray(R.array.chat_red_packet_tab);
    private ArrayList<Fragment> fragments = new ArrayList<>();

    @Override
    public void onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.END)) {
            drawer_layout.closeDrawer(GravityCompat.END);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected int getLayoutId() {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return R.layout.activity_red_bag_record;
    }

    @Override
    protected void setStatusBar() {
        BarUtils.setStatusBarColor(this, ContextCompat.getColor(this, R.color.chat_red_packet), 0);
        BarUtils.setStatusBarLightMode(this, true);
    }

    @Override
    protected void initView() {
        fl_title = findViewById(R.id.fl_title);
        stl_title = findViewById(R.id.stl_title);
        drawer_layout = findViewById(R.id.drawer_layout);
        wv_year = findViewById(R.id.wv_year);
        wv_month = findViewById(R.id.wv_month);
        rv_coin = findViewById(R.id.rv_coin);
        reset = findViewById(R.id.reset);
        reset.setOnClickListener(this);
        confirm = findViewById(R.id.confirm);
        confirm.setOnClickListener(this);

        tvRecordNum = findViewById(R.id.tv_record_num);
        tvRecordAmount = findViewById(R.id.tv_record_amount);
        tvTime = findViewById(R.id.tv_time);

        ly_time = findViewById(R.id.ly_time);
        ly_time.setOnClickListener(this);

        tv_return = findViewById(R.id.tv_return);
        tv_return.setOnClickListener(this);
        BarUtils.addMarginTopEqualStatusBarHeight(this, fl_title);

        rv_coin.setLayoutManager(new GridLayoutManager(this, 3));
        coinAdapter = new CommonAdapter<CoinFilterBean>(this, R.layout.item_coin_filter, coinList) {
            @Override
            protected void convert(ViewHolder holder, CoinFilterBean coin, int position) {
                TextView coin_type = holder.getView(R.id.coin_type);
                coin_type.setText(coin.coinName);
                if (stateList.get(position)) {
                    coin_type.setBackgroundResource(R.drawable.sl_red_packet);
                    coin_type.setTextColor(ContextCompat.getColor(instance, R.color.chat_white));
                    coin_type.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                } else {
                    coin_type.setBackgroundResource(0);
                    coin_type.setTextColor(ContextCompat.getColor(instance, R.color.chat_text_grey_dark));
                    coin_type.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                }
            }
        };
        coinAdapter.setOnItemClickListener(new MultiItemTypeAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, RecyclerView.ViewHolder holder, int position) {
                for (int i = 0; i < coinList.size(); i++) {
                    stateList.put(i, false);
                }
                stateList.put(position, true);
                coinAdapter.notifyDataSetChanged();
            }

            @Override
            public boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, int position) {
                return false;
            }
        });
        rv_coin.setAdapter(coinAdapter);
        coinList.add(new CoinFilterBean(getString(R.string.chat_all), true));
        CoinManager.INSTANCE.getCoinList(redPacketCoins -> {
            for (RedPacketCoin coin : redPacketCoins) {
                coinList.add(new CoinFilterBean(coin.coinName));
            }
            coinAdapter.notifyDataSetChanged();
            return null;
        });
        syncStatus(false);

        drawer_layout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {

            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                syncStatus(confirmFilter);
                if (confirmFilter) {
                    tvTime.setText(currentDate);
                    prepareParams();
                    confirmFilter = false;
                } else {
                    String[] date = currentDate.split("/");
                    if (date.length == 2) {
                        currentYear = Integer.parseInt(date[0]);
                        currentMonth = getString(R.string.chat_date_month_str, date[1]);
                    } else {
                        currentYear = Integer.parseInt(date[0]);
                        currentMonth = allYear;
                    }
                }
                showYear(false);
                coinAdapter.notifyDataSetChanged();
            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });
    }

    private void syncStatus(boolean confirm) {
        if (confirm) {
            currentDate = tempDate;
            for (int i = 0; i < stateList.size(); i++) {
                coinList.get(i).isSelected = stateList.get(i);
                if (stateList.get(i)) {
                    currentCoin = coinList.get(i);
                }
            }
        } else {
            tempDate = currentDate;
            for (int i = 0; i < coinList.size(); i++) {
                stateList.put(i, coinList.get(i).isSelected);
                if (coinList.get(i).isSelected) {
                    currentCoin = coinList.get(i);
                }
            }
        }
    }

    @Override
    protected void initData() {
        viewModel = ViewModelProviders.of(this, provider).get(PacketRecordViewModel.class);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
        currentDate = tempDate = sdf.format(System.currentTimeMillis());
        tvTime.setText(currentDate);
        Calendar c = Calendar.getInstance();
        String[] date = currentDate.split("/");
        if (date.length == 2) {
            currentYear = Integer.parseInt(date[0]);
            currentMonth = getString(R.string.chat_date_month_str, date[1]);
            c.set(Integer.parseInt(date[0]), Integer.parseInt(date[1]) - 1, 1);
            viewModel.setDate(DateUtils.getMonthBegin(c), DateUtils.getMonthEnd(c));
        } else {
            currentYear = Integer.parseInt(date[0]);
            currentMonth = allYear;
            c.set(Integer.parseInt(date[0]), 1, 1);
            viewModel.setDate(DateUtils.getYearBegin(c), DateUtils.getYearEnd(c));
        }
        fragments.add(RedPacketRecordFragment.create(2));
        fragments.add(RedPacketRecordFragment.create(1));
        stl_title.setTabData(mTitles, this, R.id.fl_container, fragments);
        stl_title.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelect(int position) {
                stl_title.setCurrentTab(position);
            }

            @Override
            public void onTabReselect(int position) {

            }
        });
        initDateInfo();
    }

    private void initDateInfo() {
        sdf = new SimpleDateFormat("yyyy/MM");
        hashMap = getMonthBetween("2017/01", sdf.format(System.currentTimeMillis()));
        for (Map.Entry<Integer, List<ChooseTimeBean>> entry : hashMap.entrySet()) {
            yearList.add(entry.getKey());
            if (hashMap.get(entry.getKey()) != null) {
                Collections.reverse(hashMap.get(entry.getKey()));
            }
        }
        Collections.reverse(yearList);
        wv_year.setOnSelectListener(new WheelView.OnSelectListener() {
            @Override
            public void endSelect(int id, String text) {
                tempDate = String.valueOf(yearList.get(id));
                showMonth(yearList.get(id), true);
            }

            @Override
            public void selecting(int id, String text) {

            }
        });
        wv_month.setOnSelectListener(new WheelView.OnSelectListener() {
            @Override
            public void endSelect(int id, String text) {
                if (id == 0) {
                    tempDate = String.valueOf(yearList.get(id));
                } else {
                    int value = id - 1;
                    if (wv_year.getSelected() == -1) {
                        return;
                    }
                    List<ChooseTimeBean> list = hashMap.get(yearList.get(wv_year.getSelected()));
                    if (list != null && value < list.size()) {
                        tempDate = sdf.format(list.get(value).data);
                    }
                }
            }

            @Override
            public void selecting(int id, String text) {

            }
        });

        if (yearList.size() > 0) {
            for (int year : yearList) {
                yearStrList.add(String.valueOf(year));
            }
        }
        showYear(true);
    }

    private void showYear(boolean reset) {
        int currentItem = yearList.indexOf(currentYear);
        if (currentItem < 0 || reset) {
            currentItem = 0;
        }
        wv_year.setData((ArrayList<String>) yearStrList);
        wv_year.setDefault(currentItem);
        showMonth(yearList.get(currentItem), reset);
    }

    private void showMonth(int year, boolean reset) {
        monthList = getMonth(year);
        if (monthList != null) {
            int currentItem = monthList.indexOf(currentMonth);
            if (currentItem < 0 || reset) {
                currentMonth = allYear;
                currentItem = 0;
            }
            wv_month.setData((ArrayList<String>) monthList);
            wv_month.setDefault(currentItem);
        }
    }

    private ArrayList<String> getMonth(int year) {
        List<ChooseTimeBean> list = hashMap.get(year);
        ArrayList<String> monthL = new ArrayList<>();
        monthL.add(allYear);
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                monthL.add(list.get(i).showTime);
            }
        }
        return monthL;
    }

    private void prepareParams() {
        if (coinList.get(0).isSelected) {
            // coinTypeNum等于-1代表选择全部币种
            viewModel.setCoinTypeNum(-1);
        } else {
            viewModel.setCoinTypeNum(1);
        }
        viewModel.clearList();
        CoinManager.INSTANCE.getCoinByName(currentCoin.coinName, redPacketCoin -> {
            Calendar c = Calendar.getInstance();
            String[] dateStr = currentDate.split("/");
            if (dateStr.length == 2) {
                currentYear = Integer.parseInt(dateStr[0]);
                currentMonth = getString(R.string.chat_date_month_str, dateStr[1]);
                c.set(Integer.parseInt(dateStr[0]), Integer.parseInt(dateStr[1]) - 1, 1);
                viewModel.setDate(DateUtils.getMonthBegin(c), DateUtils.getMonthEnd(c));
            } else {
                currentYear = Integer.parseInt(dateStr[0]);
                currentMonth = allYear;
                c.set(Integer.parseInt(dateStr[0]), 1, 1);
                viewModel.setDate(DateUtils.getYearBegin(c), DateUtils.getYearEnd(c));
            }
            if (redPacketCoin == null) {
                viewModel.changeCoin(0);
            } else {
                viewModel.changeCoin(redPacketCoin.coinId);
            }
            viewModel.requestRedPacketRecords(1);
            viewModel.requestRedPacketRecords(2);
            return null;
        });
    }

    @Override
    protected void setEvent() {

    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.tv_return) {
            finish();
        } else if (i == R.id.ly_time) {
            drawer_layout.openDrawer(GravityCompat.END);
        } else if (i == R.id.reset) {
            currentDate = tempDate = new SimpleDateFormat("yyyy").format(System.currentTimeMillis());
            showYear(true);
            for (int j = 0; j < coinList.size(); j++) {
                if (j == 0) {
                    coinList.get(j).isSelected = true;
                    stateList.put(j, true);
                } else {
                    coinList.get(j).isSelected = false;
                    stateList.put(j, false);
                }
            }
            coinAdapter.notifyDataSetChanged();
        } else if (i == R.id.confirm) {
            confirmFilter = true;
            drawer_layout.closeDrawer(GravityCompat.END);
        }
    }

    /**
     * 获取时间段内所有的年月集合
     *
     * @param minDate 最小时间  2017/01
     * @param maxDate 最大时间 2017/10
     * @return 日期集合 格式为 key年,value月
     * @throws Exception
     */
    public HashMap<Integer, List<ChooseTimeBean>> getMonthBetween(String minDate, String maxDate) {
        HashMap<Integer, List<ChooseTimeBean>> result = new HashMap<>();
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
                if (curr.get(Calendar.MONTH) + 1 < 10) {
                    chooseTimeBean.showTime = getString(R.string.chat_date_month_lower, curr.get(Calendar.MONTH) + 1);
                } else {
                    chooseTimeBean.showTime = getString(R.string.chat_date_month, curr.get(Calendar.MONTH) + 1);
                }
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
