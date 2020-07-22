package com.fzm.chat33.main.adapter;

import android.content.Context;

import com.fuzamei.common.recycleviewbase.ItemViewDelegate;
import com.fuzamei.common.recycleviewbase.MultiItemTypeAdapter;
import com.fuzamei.common.recycleviewbase.ViewHolder;

import java.util.List;

/**
 * @author zhengjy
 * @since 2019/09/18
 * Description:
 */
public abstract class HeaderAdapter<T> extends MultiItemTypeAdapter<T> {

    public HeaderAdapter(Context context, int headerId, int layoutId, List<T> datas) {
        super(context, datas);
        if (datas.size() > 0) {
            // null对应Header
            datas.add(0, null);
        }
        mDatas = datas;

        // 头布局
        addItemViewDelegate(new ItemViewDelegate<T>() {
            @Override
            public int getItemViewLayoutId() {
                return headerId;
            }

            @Override
            public boolean isForViewType(T item, int position) {
                return position == 0;
            }

            @Override
            public void convert(ViewHolder holder, T t, int position) {
                HeaderAdapter.this.convertHeader(holder, t, position);
            }
        });
        // 普通布局
        addItemViewDelegate(new ItemViewDelegate<T>() {
            @Override
            public int getItemViewLayoutId() {
                return layoutId;
            }

            @Override
            public boolean isForViewType(T item, int position) {
                return position != 0;
            }

            @Override
            public void convert(ViewHolder holder, T t, int position) {
                HeaderAdapter.this.convert(holder, t, position);
            }
        });
    }

    public void clear() {
        mDatas.clear();
    }

    public void addAll(List<T> datas) {
        if (datas.size() > 0) {
            // null对应Header
            mDatas.add(0, null);
        }
        mDatas.addAll(datas);
    }

    protected abstract void convertHeader(ViewHolder holder, T t, int position);

    protected abstract void convert(ViewHolder holder, T t, int position);
}
