package com.fzm.chat33.main.adapter;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.fzm.chat33.R;
import com.fzm.chat33.core.db.bean.FriendBean;
import com.fzm.chat33.core.global.Chat33Const;
import com.fzm.chat33.main.activity.ContactSelectActivity;
import com.fzm.chat33.widget.ChatAvatarView;
import com.fzm.chat33.widget.HighlightTextView;

import java.util.HashMap;
import java.util.List;

/**
 * @author xp
 * @since 2017/7/19
 */

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.ViewHolder> {
    private LayoutInflater mInflater;
    private List<FriendBean> mData;
    private Context mContext;
    private boolean selectable;
    private List<String> users;
    private HashMap<String, Boolean> checkState = new HashMap<>();
    private String mSearchKeyword;

    public FriendsAdapter(Context context, List<FriendBean> data) {
        this(context, data, false, null);
    }

    public FriendsAdapter(Context context, List<FriendBean> data, boolean selectable, List<String> users) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.mContext = context;
        this.selectable = selectable;
        this.users = users;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.adapter_name_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        viewHolder.tvTag = view.findViewById(R.id.tag);
        viewHolder.cb_select = view.findViewById(R.id.cb_select);
        viewHolder.tvName = view.findViewById(R.id.tv_name);
        viewHolder.tvPosition = view.findViewById(R.id.tv_identification);
        viewHolder.iv_avatar = view.findViewById(R.id.iv_avatar);
        if (selectable) {
            viewHolder.cb_select.setVisibility(View.VISIBLE);
        } else {
            viewHolder.cb_select.setVisibility(View.GONE);
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        final FriendBean bean = mData.get(position);
        if (position > 0) {
            char last = mData.get(position - 1).getFirstLetter().toUpperCase().charAt(0);
            char current = bean.getFirstLetter().toUpperCase().charAt(0);
            if (last == current) {
                holder.tvTag.setVisibility(View.GONE);
            } else {
                holder.tvTag.setVisibility(View.VISIBLE);
                holder.tvTag.setText(bean.getFirstLetter());
            }
        } else {
            holder.tvTag.setVisibility(View.VISIBLE);
            holder.tvTag.setText(bean.getFirstLetter());
        }

        // 防止复用时出现错乱
        holder.cb_select.setTag(position);
        holder.itemView.setTag(position);
        if (users != null && users.contains(bean.getId())) {
            holder.cb_select.setChecked(true);
            holder.cb_select.setEnabled(false);
            holder.itemView.setClickable(false);
        } else {
            holder.cb_select.setChecked(Boolean.TRUE.equals(checkState.get(bean.getId())));
            holder.cb_select.setEnabled(true);
            holder.itemView.setClickable(true);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!selectable) {
                        if (mOnItemClickListener != null) {
                            mOnItemClickListener.onItemClick(holder.itemView, position);
                        }
                    }
                    holder.cb_select.performClick();
                }
            });
        }

        holder.cb_select.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.getTag().equals(position)) {
                    checkState.put(mData.get(position).getId(), isChecked);
                    if (mOnCheckChangedListener != null) {
                        mOnCheckChangedListener.onCheckChanged(buttonView, isChecked, mData.get(position));
                    }
                }
            }
        });

        if (!TextUtils.isEmpty(bean.getPosition())) {
            holder.tvPosition.setText(bean.getPosition());
            holder.tvPosition.setVisibility(View.VISIBLE);
        } else {
            holder.tvPosition.setVisibility(View.GONE);
        }

        Glide.with(mContext).load(bean.getAvatar())
                .apply(new RequestOptions().placeholder(R.mipmap.default_avatar_round))
                .into(holder.iv_avatar);
        holder.iv_avatar.setIconRes(bean.isIdentified() ? R.drawable.ic_user_identified : -1);
        //holder.tvName.setText(bean.getDisplayName());
        holder.tvName.highlightSearchText(bean.getDisplayName(), mSearchKeyword);
    }

    public void removeCheck(String id) {
        for (int i = 0; i < mData.size(); i++) {
            if (mData.get(i).getId().equals(id)) {
                checkState.remove(id);
                notifyItemChanged(i);
                break;
            }
        }
    }

    public void check(String id) {
        for (int i = 0; i < mData.size(); i++) {
            if (mData.get(i).getId().equals(id)
                    && mData.get(i).channelType() == Chat33Const.CHANNEL_FRIEND) {
                check(i);
                break;
            }
        }
    }

    public void check(int position) {
        checkState.put(mData.get(position).getId(), true);
        notifyItemChanged(position);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    //**********************itemClick************************
    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    private OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    private ContactSelectActivity.OnCheckChangedListener mOnCheckChangedListener;

    public void setOnCheckChangedListener(ContactSelectActivity.OnCheckChangedListener mOnCheckChangedListener) {
        this.mOnCheckChangedListener = mOnCheckChangedListener;
    }
    //**************************************************************

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox cb_select;
        ChatAvatarView iv_avatar;
        TextView tvTag, tvPosition;
        HighlightTextView tvName;

        public ViewHolder(View itemView) {
            super(itemView);
        }
    }

    public Object getItem(int position) {
        return mData.get(position);
    }

    /**
     * 根据ListView的当前位置获取分类的首字母的char ascii值
     */
    @Deprecated
    public int getSectionForPosition(int position) {
        return mData.get(position).getFirstLetter().charAt(0);
    }

    /**
     * 根据分类的首字母的Char ascii值获取其第一次出现该首字母的位置
     */
    public int getPositionForSection(int section) {
        for (int i = 0; i < getItemCount(); i++) {
            String sortStr = mData.get(i).getFirstLetter();
            char firstChar = sortStr.toUpperCase().charAt(0);
            if (firstChar == section) {
                return i;
            }
        }
        return -1;
    }

    public void setSearchKeyword(String keyword) {
        mSearchKeyword = keyword;
    }
}
