package com.fuzamei.common.recycleviewbase;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by
 * 携带脚布局的单类布局通用adapter
 */

public abstract class FooterAdapter<T> extends MultiItemTypeAdapter<T> {
    public FooterAdapter(Context context, final int layoutId, final int footerLayoutId, List<T> datas) {
        super(context, datas);
        // 不要影响datas
        mDatas = new ArrayList<>();
        for (T t : datas) {
            mDatas.add(t);
        }
        mDatas.add(null);// null对应footer

        // 普通布局
        addItemViewDelegate(new ItemViewDelegate<T>() {
            @Override
            public int getItemViewLayoutId() {
                return layoutId;
            }

            @Override
            public boolean isForViewType(T item, int position) {
                return position != getItemCount() - 1;
            }

            @Override
            public void convert(ViewHolder holder, T t, int position) {
                FooterAdapter.this.convert(holder, t, position);
            }
        });
        // 脚布局
        addItemViewDelegate(new ItemViewDelegate<T>() {
            @Override
            public int getItemViewLayoutId() {
                return footerLayoutId;
            }

            @Override
            public boolean isForViewType(T item, int position) {
                return position == getItemCount() - 1;
            }

            @Override
            public void convert(ViewHolder holder, T t, int position) {
                FooterAdapter.this.convertFooter(holder, t, position);
            }
        });
    }

    protected abstract void convert(ViewHolder holder, T t, int position);
    protected abstract void convertFooter(ViewHolder holder, T t, int position);
}
