package com.fuzamei.common.callback;

import androidx.recyclerview.widget.DiffUtil;

import java.util.List;

/**
 * @author zhengjy
 * @since 2018/10/23
 * Description:
 */
public class DiffCallBack<T extends Unique> extends DiffUtil.Callback {

    private List<T> oldData;
    private List<T> newData;

    public DiffCallBack(List<T> oldData, List<T> newData) {
        this.oldData = oldData;
        this.newData = newData;
    }

    @Override
    public int getOldListSize() {
        return oldData.size();
    }

    @Override
    public int getNewListSize() {
        return newData.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return oldData.get(oldItemPosition).getId().equals(newData.get(newItemPosition).getId());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return oldData.get(oldItemPosition).hashCode() == newData.get(newItemPosition).hashCode();
    }
}
