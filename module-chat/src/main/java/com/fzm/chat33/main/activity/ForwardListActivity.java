package com.fzm.chat33.main.activity;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.alibaba.android.arouter.facade.annotation.Autowired;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.fuzamei.componentservice.app.AppRoute;
import com.fzm.chat33.core.bean.comparator.DateComparator;
import com.fzm.chat33.core.db.bean.BriefChatLog;
import com.fzm.chat33.core.db.bean.ChatMessage;
import com.fuzamei.componentservice.base.LoadableActivity;
import com.fzm.chat33.R;
import com.fzm.chat33.main.adapter.ForwardListAdapter;
import com.fzm.chat33.core.global.Chat33Const;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;

/**
 * @author zhengjy
 * @since 2018/12/27
 * Description:转发消息查看列表
 */
@Route(path = AppRoute.FORWARD_MESSAGE)
public class ForwardListActivity extends LoadableActivity {

    private RecyclerView rv_forward;
    private View toolbar_back_button;
    private TextView title_tv, tv_date;

    private ForwardListAdapter mAdapter;
    private List<BriefChatLog> chatLogs;
    private String startDate;
    private String endDate;
    @Autowired
    public ChatMessage message;

    @Override
    protected boolean enableSlideBack() {
        return true;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_forward_list;
    }

    @Override
    protected void initView() {
        rv_forward = findViewById(R.id.rv_forward);
        toolbar_back_button = findViewById(R.id.toolbar_back_button);
        title_tv = findViewById(R.id.title_tv);
        tv_date = findViewById(R.id.tv_date);
    }

    @Override
    protected void initData() {
        ARouter.getInstance().inject(this);
        chatLogs = message.msg.sourceLog;
        startDate = formatDate(chatLogs.get(0).datetime);
        endDate = formatDate(chatLogs.get(chatLogs.size() - 1).datetime);
        if (startDate.equals(endDate)) {
            tv_date.setText(startDate);
        } else {
            tv_date.setText(startDate + " ~ " + endDate);
        }
        Collections.sort(chatLogs, new DateComparator());
        if (message.msg.sourceChannel == Chat33Const.CHANNEL_FRIEND) {
            title_tv.setText(getString(R.string.chat_title_forward_list1, message.msg.forwardUserName, message.msg.sourceName));
        } else if (message.msg.sourceChannel == Chat33Const.CHANNEL_ROOM) {
            title_tv.setText(getString(R.string.chat_title_forward_list2, message.msg.sourceName));
        }
    }

    @Override
    protected void setEvent() {
        toolbar_back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        rv_forward.setLayoutManager(new LinearLayoutManager(this));
        rv_forward.setNestedScrollingEnabled(false);
        mAdapter = new ForwardListAdapter(this, message);
        rv_forward.setAdapter(mAdapter);
        rv_forward.setFocusable(false);
    }

    public static String formatDate(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(time);
    }
}
