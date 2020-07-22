package com.fzm.chat33.adapter

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.dsladapter.DslLoadMoreItem
import com.angcyo.dsladapter.DslViewHolder

/**
 * @author zhengjy
 * @since 2019/12/10
 * Description:
 */
class ChatLoadMoreItem : DslLoadMoreItem() {

    init {
//        itemStateLayoutMap[ADAPTER_LOAD_NORMAL] = R.layout.base_loading_layout
//        itemStateLayoutMap[ADAPTER_LOAD_LOADING] = R.layout.base_loading_layout
//        itemStateLayoutMap[ADAPTER_LOAD_NO_MORE] = R.layout.base_no_more_layout
//        itemStateLayoutMap[ADAPTER_LOAD_ERROR] = R.layout.base_error_layout
//        itemStateLayoutMap[ADAPTER_LOAD_RETRY] = R.layout.base_error_layout

    }

    override fun _onBindStateLayout(itemHolder: DslViewHolder, state: Int) {
        onBindStateLayout(itemHolder, state)

        if (itemEnableLoadMore) {
            if (itemState == ADAPTER_LOAD_NORMAL || itemState == ADAPTER_LOAD_LOADING) {

            } else if (itemState == ADAPTER_LOAD_ERROR) {
                itemHolder.clickItem {
                    if (itemState == ADAPTER_LOAD_ERROR || itemState == ADAPTER_LOAD_RETRY) {
                        //失败的情况下, 点击触发重新加载
                        _notifyLoadMore(itemHolder)
                        updateAdapterItem()
                    }
                }
            } else {
                itemHolder.itemView.isClickable = false
            }
        } else {
            itemHolder.itemView.isClickable = false
        }
    }

    override var onItemViewAttachedToWindow: (itemHolder: DslViewHolder) -> Unit = {
        _notifyLoadMore(it)
    }

    override var onItemViewDetachedToWindow: (itemHolder: DslViewHolder) -> Unit = {

    }
}