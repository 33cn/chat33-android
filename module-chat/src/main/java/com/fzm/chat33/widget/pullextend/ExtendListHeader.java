package com.fzm.chat33.widget.pullextend;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.fuzamei.common.recycleviewbase.CommonAdapter;
import com.fuzamei.common.recycleviewbase.MultiItemTypeAdapter;
import com.fuzamei.common.recycleviewbase.ViewHolder;
import com.fuzamei.common.utils.ScreenUtils;
import com.fuzamei.common.utils.VibrateUtils;
import com.fuzamei.componentservice.app.AppRoute;
import com.fzm.chat33.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 这个类封装了下拉扩展布局
 */
public class ExtendListHeader extends ExtendLayout {

    float containerHeight = ScreenUtils.dp2px(60);
    float listHeight = ScreenUtils.dp2px(120);
    boolean arrivedListHeight = false;
    boolean shouldVibrate = true;

    private RecyclerView mRecyclerView;
    private CommonAdapter<ExtendOption> mAdapter;
    private List<ExtendOption> mData;

    /**
     * 原点
     */
    private ExtendPoint mExtendPoint;

    public ExtendListHeader(Context context) {
        super(context);

    }

    public ExtendListHeader(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void bindView(View container) {
        mRecyclerView = findViewById(R.id.rv_extends);
        mExtendPoint = findViewById(R.id.expend_point);

        mData = new ArrayList<>();
        mData.add(new ExtendOption(R.mipmap.icon_transfer, getContext().getString(R.string.chat_tips_input_transfer)));
        mData.add(new ExtendOption(R.mipmap.icon_receipt, getContext().getString(R.string.chat_tips_input_receipt)));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
        mAdapter = new CommonAdapter<ExtendOption>(getContext(), R.layout.item_extend_option, mData) {
            @Override
            protected void convert(ViewHolder holder, ExtendOption extendOption, int position) {
                holder.setImageResource(R.id.iv_option, mData.get(position).icon);
                holder.setText(R.id.tv_option, mData.get(position).option);
            }
        };
        mAdapter.setOnItemClickListener(new MultiItemTypeAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, RecyclerView.ViewHolder holder, int position) {
                if (position == 0) {
                    ARouter.getInstance().build(AppRoute.DEPOSIT_OUT).navigation();
                } else if (position == 1) {
                    ARouter.getInstance().build(AppRoute.DEPOSIT_IN).navigation();
                }
            }

            @Override
            public boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, int position) {
                return false;
            }
        });
        mRecyclerView.setAdapter(mAdapter);
    }

    public RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    @Override
    protected View createLoadingView(Context context, AttributeSet attrs) {
        return LayoutInflater.from(context).inflate(R.layout.layout_extend_header, null);
    }

    @Override
    public int getContentSize() {
        return (int) (containerHeight);
    }

    @Override
    public int getListSize() {
        return (int) (listHeight);
    }

    @Override
    protected void onReset() {
        mExtendPoint.setVisibility(VISIBLE);
        mExtendPoint.setAlpha(1);
        mExtendPoint.setTranslationY(0);
        mRecyclerView.setTranslationY(0);
        arrivedListHeight = false;
        shouldVibrate = true;
    }

    @Override
    protected void onReleaseToRefresh() {

    }

    @Override
    protected void onPullToRefresh() {

    }

    @Override
    protected void onArrivedListHeight() {
        arrivedListHeight = true;
    }

    @Override
    protected void onRefreshing() {

    }

    @Override
    public void onPull(int offset) {
        if (!arrivedListHeight) {
            mExtendPoint.setVisibility(VISIBLE);
            float percent = Math.abs(offset) / containerHeight;
            int moreOffset = Math.abs(offset) - (int) containerHeight;
            if (percent <= 1.0f) {
                mExtendPoint.setPercent(percent);
                mExtendPoint.setTranslationY(-Math.abs(offset) / 2 + mExtendPoint.getHeight() / 2);
                mRecyclerView.setTranslationY(-containerHeight);
            } else {
                if (shouldVibrate) {
                    VibrateUtils.simple(getContext(), 20);
                    shouldVibrate = false;
                }
                float subPercent = (moreOffset) / (listHeight - containerHeight);
                subPercent = Math.min(1.0f, subPercent);
                mExtendPoint.setTranslationY(-(int) containerHeight / 2 + mExtendPoint.getHeight() / 2 + (int) containerHeight * subPercent / 2);
                mExtendPoint.setPercent(1.0f);
                float alpha = (1 - subPercent * 2);
                mExtendPoint.setAlpha(Math.max(alpha, 0));
                mRecyclerView.setTranslationY(-(1 - subPercent) * containerHeight);
            }
        }
        if (Math.abs(offset) >= listHeight) {
            mExtendPoint.setVisibility(INVISIBLE);
            mRecyclerView.setTranslationY(-(Math.abs(offset) - listHeight) / 2);
        }
    }

    static class ExtendOption {
        int icon;
        String option;

        ExtendOption(int icon, String option) {
            this.icon = icon;
            this.option = option;
        }
    }
}
