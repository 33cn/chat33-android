package com.fzm.chat33.main.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.android.arouter.launcher.ARouter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.fuzamei.common.ext.format
import com.fuzamei.common.utils.FinanceUtils
import com.fuzamei.common.utils.ScreenUtils
import com.fuzamei.componentservice.app.AppRoute
import com.fzm.chat33.R
import com.fzm.chat33.core.bean.PraiseBean
import com.fzm.chat33.core.db.bean.InfoCacheBean
import com.fzm.chat33.core.provider.*
import com.fzm.chat33.utils.StringUtils
import kotlinx.android.synthetic.main.item_praise.view.*

/**
 * 创建日期：2019/11/19
 * 描述:
 * 作者:Andy
 */
class ChatPraiseAdapter(val context: Context, val targetId: String?, private val isDetail: Boolean) : RecyclerView.Adapter<ChatPraiseViewHolder>(){

    private val mInflater: LayoutInflater = LayoutInflater.from(context)
    private val mDatas = ArrayList<PraiseBean>()

    fun addList(list: List<PraiseBean>?) {
        if(!list.isNullOrEmpty()) {
            mDatas.addAll(list)
        }
        notifyDataSetChanged()
    }

    fun resetList(list: List<PraiseBean>?) {
        mDatas.clear()
        addList(list)
    }

    fun clearList() {
        mDatas.clear()
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return mDatas.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatPraiseViewHolder {
        val view = mInflater.inflate(R.layout.item_praise, parent, false)
        return ChatPraiseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatPraiseViewHolder, position: Int) {
        val data = mDatas[position]
        holder.itemView.name.tag = data
        val options = RequestOptions().placeholder(R.mipmap.default_avatar_round)
        InfoProvider.getInstance().strategy(ChatPraiseInfoStrategy(data, targetId)).load(object : OnFindInfoListener<InfoCacheBean> {
            override fun onFindInfo(bean: InfoCacheBean, place: Int) {
                if (context == null) {
                    return
                }
                if (data != holder.itemView.name.tag) {
                    return
                }
                Glide.with(context).load(StringUtils.aliyunFormat(bean.avatar, ScreenUtils.dp2px(context, 35f), ScreenUtils.dp2px(context, 35f)))
                        .apply(options)
                        .into(holder.itemView.iv_user_head)
                holder.itemView.iv_user_head.setIconRes(if (bean.isIdentified) R.drawable.ic_user_identified else -1)
                holder.itemView.name.text = bean.displayName
            }

            override fun onNotExist() {
                holder.itemView.iv_user_head.setImageResource(R.mipmap.default_avatar_round)
                holder.itemView.name.setText(R.string.chat_tips_no_name)
            }
        })
        holder.itemView.time.text = data.createTime.format("yyyy/MM/dd HH:mm")
        CoinManager.getCoinByName(data.coinName) { redPacketCoin ->
            holder.itemView.price.text =FinanceUtils.getPlainNum(data.amount, redPacketCoin?.decimalPlaces ?: 1) + data.coinName
        }
        holder.itemView.price.visibility = if(data.type == 2) View.VISIBLE else View.GONE
        holder.itemView.praise.text = context.getString(if(data.type == 2) R.string.chat_label_reward else R.string.chat_label_praise)
        holder.itemView.praise.setBackgroundResource(if(data.type == 2) R.drawable.bg_praise_reward else R.drawable.bg_praise)
        if(isDetail) {
            holder.itemView.icon_right.visibility = View.GONE
        } else {
            holder.itemView.icon_right.visibility = View.VISIBLE
            holder.itemView.setOnClickListener {
                ARouter.getInstance().build(AppRoute.MESSAGE_PRAISE)
                        .withInt("channelType", data.channelType)
                        .withString("logId", data.logId)
                        .withString("targetId", targetId)
                        .navigation()
            }
        }
    }
}

class ChatPraiseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)