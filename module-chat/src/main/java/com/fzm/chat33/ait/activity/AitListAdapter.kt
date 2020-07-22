package com.fzm.chat33.ait.activity

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.fzm.chat33.R
import com.fzm.chat33.core.bean.comparator.PinyinComparator
import com.fzm.chat33.core.db.bean.RoomContact
import com.fzm.chat33.core.global.Chat33Const.*
import com.fzm.chat33.widget.ChatAvatarView
import com.fzm.chat33.widget.HighlightTextView
import java.util.*

/**
 * @author zhengjy
 * @since 2019/09/02
 * Description:
 */
class AitListAdapter(
        val context: Context,
        data: List<RoomContact>
) : RecyclerView.Adapter<AitListAdapter.ViewHolder>() {

    private val inflater: LayoutInflater by lazy { LayoutInflater.from(context) }
    private var keywords: String? = null

    val mData by lazy { mutableListOf<RoomContact>() }

    init {
        mData.addAll(data)
        Collections.sort(mData, PinyinComparator())
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(R.layout.adapter_group_ait_item, parent, false)
        val viewHolder = ViewHolder(view)
        viewHolder.tvTag = view.findViewById(R.id.tag)
        viewHolder.tvName = view.findViewById(R.id.tv_name)
        viewHolder.ivAvatar = view.findViewById(R.id.iv_avatar)
        viewHolder.tvMemberLevel = view.findViewById(R.id.tv_member_level)
        return viewHolder
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    @SuppressLint("DefaultLocale")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (mData[position].memberLevel > LEVEL_USER) {
            if (position > 0) {
                val last = mData[position - 1].memberLevel
                if (last > 1) {
                    holder.tvTag?.visibility = View.GONE
                } else {
                    holder.tvTag?.visibility = View.VISIBLE
                    holder.tvTag?.setText(R.string.chat_tips_member_type)
                }
            } else {
                holder.tvTag?.visibility = View.VISIBLE
                holder.tvTag?.setText(R.string.chat_tips_member_type)
            }
        } else {
            if (position > 0) {
                val last = mData[position - 1].firstLetter.toUpperCase()[0]
                val current = mData[position].firstLetter.toUpperCase()[0]
                if (last == current) {
                    holder.tvTag?.visibility = View.GONE
                } else {
                    holder.tvTag?.visibility = View.VISIBLE
                    holder.tvTag?.text = mData[position].firstLetter
                }
            } else {
                holder.tvTag?.visibility = View.VISIBLE
                holder.tvTag?.text = mData[position].firstLetter
            }
        }
        when (mData[position].memberLevel) {
            LEVEL_USER -> holder.tvMemberLevel?.visibility = View.GONE
            LEVEL_ADMIN -> {
                holder.tvMemberLevel?.visibility = View.VISIBLE
                holder.tvMemberLevel?.setText(R.string.core_tips_group_admin)
                holder.tvMemberLevel?.setBackgroundResource(R.drawable.shape_yellow_r4)
            }
            LEVEL_OWNER -> {
                holder.tvMemberLevel?.visibility = View.VISIBLE
                holder.tvMemberLevel?.setText(R.string.core_tips_group_master)
                holder.tvMemberLevel?.setBackgroundResource(R.drawable.shape_blue_r4)
            }
        }
        holder.itemView.setOnClickListener {
            mOnItemClickListener?.onItemClick(holder.itemView, position)
        }

        val bean = mData[position]

        holder.ivAvatar?.let {
            Glide.with(context).load(bean.avatar)
                    .apply(RequestOptions().placeholder(R.mipmap.default_avatar_round))
                    .into(it)
        }
        holder.ivAvatar?.setIconRes(if (bean.isIdentified()) R.drawable.ic_user_identified else -1)
        holder.tvName?.highlightSearchText(bean.getDisplayName(), keywords)
    }

    /**
     * 根据分类的首字母的Char ascii值获取其第一次出现该首字母的位置
     */
    @SuppressLint("DefaultLocale")
    fun getPositionForSection(section: Int): Int {
        for (i in 0 until itemCount) {
            val sortStr = mData[i].firstLetter
            val firstChar = sortStr.toUpperCase()[0]
            if (firstChar.toInt() == section) {
                return i
            }
        }
        return -1
    }

    fun setKeywords(keywords: String?) {
        this.keywords = keywords
    }

    fun resetList(list: List<RoomContact>?) {
        mData.clear()
        if (list != null) {
            mData.addAll(list)
        }
        Collections.sort(mData, PinyinComparator())
        notifyDataSetChanged()
    }

    interface OnItemClickListener {
        fun onItemClick(view: View, position: Int)
    }

    private var mOnItemClickListener: OnItemClickListener? = null

    fun setOnItemClickListener(mOnItemClickListener: OnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var ivAvatar: ChatAvatarView? = null
        var tvTag: TextView? = null
        var tvName: HighlightTextView? = null
        var tvMemberLevel: TextView? = null
    }
}