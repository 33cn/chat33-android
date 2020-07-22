package com.fzm.chat33.redpacket.activity;

import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import com.alibaba.android.arouter.facade.annotation.Autowired;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.bumptech.glide.Glide;
import com.fuzamei.common.utils.BarUtils;
import com.fuzamei.common.utils.FinanceUtils;
import com.fuzamei.common.utils.RoomUtils;
import com.fuzamei.common.utils.ScreenUtils;
import com.fuzamei.common.utils.ShowUtils;
import com.fuzamei.common.widget.SharePopupWindow;
import com.fuzamei.componentservice.app.AppRoute;
import com.fuzamei.componentservice.base.DILoadableActivity;
import com.fuzamei.componentservice.config.AppConfig;
import com.fuzamei.componentservice.helper.WeChatHelper;
import com.fzm.chat33.R;
import com.fzm.chat33.core.bean.RedPacketReceiveInfo;
import com.fzm.chat33.core.db.ChatDatabase;
import com.fzm.chat33.core.provider.CoinManager;
import com.fzm.chat33.core.response.RedPacketInfoResponse;
import com.fzm.chat33.main.adapter.ReceiveListAdapter;
import com.fzm.chat33.redpacket.mvvm.PacketInfoViewModel;
import com.fzm.chat33.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * 创建日期：2018/7/11 on 16:27
 * 描述:
 * 作者:wdl
 */
@Route(path = "/app/redPacketInfo")
public class RedPacketInfoActivity extends DILoadableActivity implements View.OnClickListener {
    private ReceiveListAdapter receiveListAdapter;

    ListView lvReceive;
    TextView tvUid;
    TextView tv_unit;
    View ll_wallet, share_red_packet;
    TextView tvRemark;
    TextView tvAmount;
    TextView tvReceiveCount;
    TextView tvReceiveCode;
    ImageView ivHead;
    TextView toolbar_back_button;
    SharePopupWindow sharePopup;

    private List<RedPacketReceiveInfo> receiveList = new ArrayList<>();

    @Autowired
    public String packetUrl;
    @Autowired
    public String packetId;
    @Autowired
    public boolean fromRecord = false;
    private String currentCoinName = "";

    @Inject
    public ViewModelProvider.Factory provider;
    private PacketInfoViewModel viewModel;


    @Override
    protected void initView() {
        viewModel = ViewModelProviders.of(this, provider).get(PacketInfoViewModel.class);
        lvReceive = findViewById(R.id.lv_receive);
        tvUid = findViewById(R.id.tv_uid);
        tv_unit = findViewById(R.id.tv_unit);
        ll_wallet = findViewById(R.id.ll_wallet);
        ll_wallet.setOnClickListener(this);
        share_red_packet = findViewById(R.id.share_red_packet);
        share_red_packet.setOnClickListener(this);
        tvRemark = findViewById(R.id.tv_remark);
        tvAmount = findViewById(R.id.tv_amount);
        tvReceiveCount = findViewById(R.id.tv_receive_count);
        ivHead = findViewById(R.id.iv_head);

        toolbar_back_button = findViewById(R.id.toolbar_back_button);
        toolbar_back_button.setOnClickListener(this);

        tvReceiveCode = findViewById(R.id.tv_receive_code);
        tvReceiveCode.setOnClickListener(this);
        tvReceiveCode.setVisibility(fromRecord ? View.GONE : View.VISIBLE);

        receiveListAdapter = new ReceiveListAdapter(receiveList, this);
        lvReceive.setAdapter(receiveListAdapter);
        viewModel.getLoading().observe(this, this::setupLoading);
        viewModel.getRedPacketInfo().observe(this, it -> {
            bindRedPacketInfo(it);
        });
        viewModel.getRedPacketReceiveList().observe(this, it -> {
            if (it.rows != null) {
                receiveList.clear();
                receiveList.addAll(it.rows);
                receiveListAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_receive_info;
    }

    @Override
    protected void setStatusBar() {
        BarUtils.setStatusBarColor(this, ContextCompat.getColor(this, R.color.chat_red_packet), 0);
        BarUtils.setStatusBarLightMode(this, true);
    }

    public void initData() {
        ARouter.getInstance().inject(this);
        getRedPacketInfo();
        getRedPacketReceiveInfo();
    }

    @Override
    protected void setEvent() {

    }

    private void getRedPacketInfo() {
        viewModel.redPacketInfo(packetId);
    }

    private void bindRedPacketInfo(RedPacketInfoResponse response) {
        packetUrl = response.packetUrl;
        tvUid.setText(response.senderName);
        tv_unit.setText(getString(R.string.chat_red_packet_owner, response.coinName));
        if (response.remark.length() > 20) {
            tvRemark.setText(response.remark.substring(0, 20) + "…");
        } else {
            tvRemark.setText(response.remark);
        }
        if (!TextUtils.isEmpty(response.senderAvatar)) {
            Glide.with(RedPacketInfoActivity.this).load(StringUtils.aliyunFormat(response.senderAvatar,
                    ScreenUtils.dp2px(RedPacketInfoActivity.this, 70), ScreenUtils.dp2px(RedPacketInfoActivity.this, 70)))
                    .into(ivHead);
        } else {
            ivHead.setImageResource(R.mipmap.default_avatar_round);
        }
        if (response.type == 1) {
            Drawable drawable = ContextCompat.getDrawable(this, R.mipmap.icon_red_envelopes_ping);
            drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
            tv_unit.setCompoundDrawables(null, null, drawable, null);
            tv_unit.setCompoundDrawablePadding(5);
        } else {
            tv_unit.setCompoundDrawables(null, null, null, null);
        }
        tvReceiveCount.setText(getString(R.string.chat_red_packet_recevie_count, (response.size - response.remain), response.size, response.remain));
        if (response.revInfo != null) {
            tvAmount.setTextColor(ContextCompat.getColor(this, R.color.chat_white));
            tvAmount.setTextSize(40);
            CoinManager.INSTANCE.getCoinByName(response.revInfo.getCoinName(), redPacketCoin -> {
                tvAmount.setText(FinanceUtils.getPlainNum(response.revInfo.getAmount(), redPacketCoin == null ? 1 : redPacketCoin.decimalPlaces) + " " + response.revInfo.getCoinName());
                return null;
            });
//                    ll_wallet.setVisibility(View.VISIBLE);
        } else {
            tvAmount.setTextColor(ContextCompat.getColor(this, R.color.chat_red_text_light));
            tvAmount.setTextSize(30);
            if (response.status == 2) {
                tvAmount.setText(R.string.chat_red_packet_recevie_finish);
            } else if (response.status == 3) {
                tvAmount.setText(R.string.chat_red_packet_overdue);
            } else if (response.status == 4) {
                tvAmount.setText(R.string.chat_red_packet_recevie_finish);
            }
//                    ll_wallet.setVisibility(View.GONE);
        }
        currentCoinName = response.coinName;
        if (!TextUtils.isEmpty(AppConfig.WX_APP_ID)) {
            if (response.status == 1 && response.remain > 0) {
                share_red_packet.setVisibility(View.VISIBLE);
            } else {
                share_red_packet.setVisibility(View.GONE);
            }
        }
        RoomUtils.run(new Runnable() {
            @Override
            public void run() {
                ChatDatabase.getInstance().chatMessageDao().updateRedPacketStatus(response.status, packetId);
            }
        });
    }

    private void getRedPacketReceiveInfo() {
        viewModel.redPacketReceiveList(packetId);
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.toolbar_back_button) {
            finish();
        } else if (i == R.id.tv_receive_code) {
            ARouter.getInstance().build(AppRoute.RED_PACKET_RECORDS).navigation();
        } else if (i == R.id.ll_wallet) {
            ShowUtils.showToastNormal(this, R.string.chat_wallet_see);
        } else if (i == R.id.share_red_packet) {
            if (sharePopup == null) {
                sharePopup = new SharePopupWindow(this, new SharePopupWindow.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, PopupWindow popupWindow, int position) {
                        int shareType;
                        if (position == 0) {
                            shareType = WeChatHelper.TIMELINE;
                        } else {
                            shareType = WeChatHelper.SESSION;
                        }
                        WeChatHelper.INS.shareWeb(packetUrl, getString(R.string.chat_tip_packet_share_title, currentCoinName),
                                getString(R.string.chat_tip_packet_share_content), shareType);
                    }
                });
            }
            sharePopup.showAtLocation(share_red_packet, Gravity.BOTTOM, 0, 0);
        }
    }
}
