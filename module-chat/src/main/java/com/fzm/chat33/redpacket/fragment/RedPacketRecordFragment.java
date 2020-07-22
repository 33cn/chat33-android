package com.fzm.chat33.redpacket.fragment;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.fuzamei.common.recycleviewbase.CommonAdapter;
import com.fuzamei.common.recycleviewbase.MultiItemTypeAdapter;
import com.fuzamei.common.recycleviewbase.RecyclerViewDivider;
import com.fuzamei.common.recycleviewbase.ViewHolder;
import com.fuzamei.common.utils.FinanceUtils;
import com.fuzamei.common.widget.MultiStatusLayout;
import com.fuzamei.componentservice.base.DILoadableFragment;
import com.fzm.chat33.R;
import com.fzm.chat33.core.bean.RedPacketRecord;
import com.fzm.chat33.core.provider.CoinManager;
import com.fzm.chat33.core.response.RedPacketInfoResponse;
import com.fzm.chat33.redpacket.mvvm.PacketRecordViewModel;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zhengjy
 * @since 2019/03/18
 * Description:
 */
public class RedPacketRecordFragment extends DILoadableFragment {

    private TextView tv_record_num;
    private TextView tv_record_amount;

    private SmartRefreshLayout refreshLayout;
    private MultiStatusLayout statusLayout;
    private RecyclerView rv_record;

    private List<RedPacketInfoResponse> packetList = new ArrayList<>();
    private CommonAdapter<RedPacketInfoResponse> recordAdapter;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    private boolean refresh = true;
    private PacketRecordViewModel viewModel;
    private int type;

    public static Fragment create(int type) {
        Fragment fragment = new RedPacketRecordFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("type", type);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_red_packet_record;
    }

    @Override
    protected void initView(View view, Bundle savedInstanceState) {
        viewModel = ViewModelProviders.of(activity).get(PacketRecordViewModel.class);
        type = getArguments().getInt("type");
        tv_record_num = view.findViewById(R.id.tv_record_num);
        tv_record_amount = view.findViewById(R.id.tv_record_amount);
        refreshLayout = view.findViewById(R.id.refreshLayout);
        statusLayout = view.findViewById(R.id.statusLayout);
        rv_record = view.findViewById(R.id.rv_record);
        if (type == 1) {
            viewModel.getClearSendList().observe(this, it -> packetList.clear());
            viewModel.getSendRedPacketRecord().observe(this, this::bindRecordData);
        } else {
            viewModel.getClearReceiveList().observe(this, it -> packetList.clear());
            viewModel.getReceiveRedPacketRecord().observe(this, this::bindRecordData);
        }
    }

    @Override
    public void initData() {
        refreshLayout.setOnRefreshLoadMoreListener(new OnRefreshLoadMoreListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                refresh = true;
                viewModel.obtainRequest(type).pageNum = 0;
                viewModel.requestRedPacketRecords(type);
            }

            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                refresh = false;
                viewModel.requestRedPacketRecords(type);
            }
        });
        rv_record.setLayoutManager(new LinearLayoutManager(activity));
        rv_record.addItemDecoration(new RecyclerViewDivider(activity, LinearLayoutManager.VERTICAL,
                0.5f, ContextCompat.getColor(activity, R.color.chat_divide_light)));
        recordAdapter = new CommonAdapter<RedPacketInfoResponse>(activity, R.layout.item_red_packet_record, packetList) {
            @Override
            protected void convert(ViewHolder holder, RedPacketInfoResponse response, int position) {
                TextView tv_name = holder.getView(R.id.name);
                String status;
                int receive = response.size - response.remain;
                if (type == 2) {
                    tv_name.setText(response.senderName);
                    holder.setText(R.id.time, dateFormat.format(response.revInfo.getCreatedAt()));
                    if (response.type == 1) {
                        Drawable drawable = ContextCompat.getDrawable(activity, R.mipmap.icon_red_envelopes_ping);
                        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                        tv_name.setCompoundDrawables(null, null, drawable, null);
                        tv_name.setCompoundDrawablePadding(5);
                    } else {
                        tv_name.setCompoundDrawables(null, null, null, null);
                    }
                    if (response.revInfo.getStatus() == 1) {
                        status = getString(R.string.chat_red_packet_receive_status1);
                    } else if (response.revInfo.getStatus() == 2) {
                        status = getString(R.string.chat_red_packet_receive_status2);
                    } else if (response.revInfo.getStatus() == 3) {
                        status = getString(R.string.chat_red_packet_receive_status3);
                    } else if (response.revInfo.getStatus() == 4) {
                        status = getString(R.string.chat_red_packet_receive_status4);
                    } else {
                        status = getString(R.string.chat_red_packet_receive_status5);
                    }
                } else {
                    tv_name.setCompoundDrawables(null, null, null, null);
                    holder.setText(R.id.time, dateFormat.format(response.createdAt));
                    if (response.type == 1) {
                        tv_name.setText(R.string.chat_red_packet_luck);
                    } else {
                        tv_name.setText(R.string.chat_red_packet_normal);
                    }
                    if (response.status == 1) {
                        status = getString(R.string.chat_red_packet_count_status1, receive , response.size);
                    } else if (response.status == 2) {
                        status = getString(R.string.chat_red_packet_count_status2, receive , response.size);
                    } else if (response.status == 3) {
                        status = getString(R.string.chat_red_packet_count_status3, receive , response.size);
                    } else if (response.status == 4) {
                        status = getString(R.string.chat_red_packet_count_status4, receive , response.size);
                    } else {
                        status = getString(R.string.chat_red_packet_count_status5, receive , response.size);
                    }
                }
                holder.setText(R.id.packet_status, status);
                CoinManager.INSTANCE.getCoinByName(response.coinName, redPacketCoin -> {
                    if (type == 1) {
                        holder.setText(R.id.amount, FinanceUtils.getPlainNum(response.amount, redPacketCoin == null ? 1 : redPacketCoin.decimalPlaces) + response.coinName);
                    } else {
                        holder.setText(R.id.amount, FinanceUtils.getPlainNum(response.revInfo.getAmount(), redPacketCoin == null ? 1 : redPacketCoin.decimalPlaces) + response.coinName);
                    }
                    return null;
                });
            }
        };
        recordAdapter.setOnItemClickListener(new MultiItemTypeAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, RecyclerView.ViewHolder holder, int position) {
                ARouter.getInstance().build("/app/redPacketInfo")
                        .withString("packetUrl", packetList.get(position).packetUrl)
                        .withString("packetId", packetList.get(position).packetId)
                        .withBoolean("fromRecord", true)
                        .navigation();
            }

            @Override
            public boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, int position) {
                return false;
            }
        });
        rv_record.setAdapter(recordAdapter);
        viewModel.requestRedPacketRecords(type);
    }

    @Override
    public void setEvent() {

    }

    private void bindRecordData(RedPacketRecord data) {
        if(data == null) {
            refreshLayout.finishRefresh(false);
            refreshLayout.finishLoadMore(false);
        } else {
            switchRecord(data);
            viewModel.obtainRequest(type).pageNum++;
            if (refresh) {
                packetList.clear();
                refreshLayout.finishRefresh();
                if (data.redPackets.size() < 20) {
                    refreshLayout.finishLoadMoreWithNoMoreData();
                }
            } else {
                if (data.redPackets.size() < 20) {
                    refreshLayout.finishLoadMoreWithNoMoreData();
                } else {
                    refreshLayout.finishLoadMore();
                }
            }
            if (data.redPackets != null) {
                packetList.addAll(data.redPackets);
                if (packetList.size() == 0) {
                    rv_record.setVisibility(View.GONE);
                    statusLayout.showEmpty();
                } else {
                    rv_record.setVisibility(View.VISIBLE);
                    statusLayout.showContent();
                }
                recordAdapter.notifyDataSetChanged();
            }
        }
    }

    private void switchRecord(RedPacketRecord record) {
        if (type == 2) {
            if (viewModel.getCoinTypeNum() == 1) {
                tv_record_num.setText(Html.fromHtml(getString(R.string.chat_red_packet_receive_total, record.count, record.coinName)));
                CoinManager.INSTANCE.getCoinByName(record.coinName, redPacketCoin -> {
                    tv_record_amount.setText(FinanceUtils.getPlainNum(record.sum, redPacketCoin == null ? 1 : redPacketCoin.decimalPlaces) + " " + record.coinName);
                    return null;
                });
            } else {
                tv_record_num.setText(R.string.chat_red_packet_recevie_total_label);
                tv_record_amount.setText(getString(R.string.chat_count, record.count));
            }
        } else if (type == 1) {
            if (viewModel.getCoinTypeNum() == 1) {
                tv_record_num.setText(Html.fromHtml(getString(R.string.chat_red_packet_send_total, record.count, record.coinName)));
                CoinManager.INSTANCE.getCoinByName(record.coinName, redPacketCoin -> {
                    tv_record_amount.setText(FinanceUtils.getPlainNum(record.sum, redPacketCoin == null ? 1 : redPacketCoin.decimalPlaces) + " " + record.coinName);
                    return null;
                });
            } else {
                tv_record_num.setText(R.string.chat_red_packet_send_total_label);
                tv_record_amount.setText(getString(R.string.chat_count, record.count));
            }
        }
    }
}
