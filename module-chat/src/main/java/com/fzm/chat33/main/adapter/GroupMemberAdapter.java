package com.fzm.chat33.main.adapter;

import android.content.Context;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.fzm.chat33.R;
import com.fzm.chat33.core.db.bean.RoomContact;
import com.fzm.chat33.global.AppConst;
import com.fzm.chat33.main.mvvm.GroupMemberViewModel;
import com.fzm.chat33.widget.ChatAvatarView;
import com.fzm.chat33.widget.HighlightTextView;
import com.fuzamei.common.utils.ShowUtils;

import java.util.HashMap;
import java.util.List;

import static com.fzm.chat33.core.global.Chat33Const.LEVEL_ADMIN;
import static com.fzm.chat33.core.global.Chat33Const.LEVEL_USER;

/**
 * @author xp
 * @since 2017/7/19
 */

public class GroupMemberAdapter extends RecyclerView.Adapter<GroupMemberAdapter.ViewHolder> {

    final String ADD_ADMIN = "add_admin";
    final String REMOVE = "remove";
    final String MUTE = "mute";
    final String MUTE_REVERSE = "mute_reverse";

    private LayoutInflater mInflater;
    private List<RoomContact> mData;
    private String roomId;
    private Context mContext;
    private boolean selectable;
    private String action;
    private HashMap<String, Boolean> checkState = new HashMap<>();
    private SparseArray<MuteCountDown> muteTimers = new SparseArray<>();
    private String mSearchKeyword;
    private GroupMemberViewModel viewModel;

    private Observer<Integer> muteSingle = new Observer<Integer>() {
        @Override
        public void onChanged(Integer position) {
            if (position < mData.size()) {
                ShowUtils.showToastNormal(mContext, mContext.getString(R.string.chat_tips_chat_operate1));
                mData.get(position).setMutedType(1);
                notifyItemChanged(position);
            }
        }
    };

    public GroupMemberAdapter(Context context, String roomId, GroupMemberViewModel viewModel,
                              List<RoomContact> data, boolean selectable, String action) {
        this.mInflater = LayoutInflater.from(context);
        this.roomId = roomId;
        this.viewModel = viewModel;
        this.mData = data;
        this.mContext = context;
        this.selectable = selectable;
        this.action = action;
        viewModel.getMuteSingleResult().observeForever(muteSingle);
    }

    public void setCheckState() {
        if (MUTE_REVERSE.equals(action)) {
            for (int i = 0; i < mData.size(); i++) {
                if (mData.get(i).getMutedType() == 3) {
                    checkState.put(mData.get(i).getId(), true);
                }
            }
        }
    }

    public void checkAll(boolean checkAll) {
        for (int i = 0; i < mData.size(); i++) {
            checkState.put(mData.get(i).getId(), checkAll);
        }
        notifyDataSetChanged();
    }

    public void onDestroy() {
        cancelAllTimers();
        viewModel.getMuteSingleResult().removeObserver(muteSingle);
    }

    private void cancelAllTimers() {
        if (muteTimers == null) {
            return;
        }
        for (int i = 0; i < muteTimers.size(); i++) {
            MuteCountDown timer = muteTimers.get(muteTimers.keyAt(i));
            if (timer != null) {
                timer.cancel();
            }
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.adapter_group_name_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        viewHolder.tvTag = view.findViewById(R.id.tag);
        viewHolder.cb_select = view.findViewById(R.id.cb_select);
        viewHolder.tvName = view.findViewById(R.id.tv_name);
        viewHolder.iv_avatar = view.findViewById(R.id.iv_avatar);
        viewHolder.tv_mute_time = view.findViewById(R.id.tv_mute_time);
        viewHolder.tv_cancel_mute = view.findViewById(R.id.tv_cancel_mute);
        if (selectable) {
            viewHolder.cb_select.setVisibility(View.VISIBLE);
        } else {
            viewHolder.cb_select.setVisibility(View.GONE);
        }
        viewHolder.tv_member_level = view.findViewById(R.id.tv_member_level);
        return viewHolder;
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);
        if (muteTimers.get(holder.hashCode()) != null) {
            muteTimers.get(holder.hashCode()).cancel();
            muteTimers.remove(holder.hashCode());
        }
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        //如果当前位置等于该分类首字母的Char的位置 ，则认为是第一次出现
        RoomContact data = mData.get(position);
        if (data.getMemberLevel() > LEVEL_USER) {
            if (position > 0) {
                int last = mData.get(position - 1).getMemberLevel();
                if (last > 1) {
                    holder.tvTag.setVisibility(View.GONE);
                } else {
                    holder.tvTag.setVisibility(View.VISIBLE);
                    holder.tvTag.setText(R.string.chat_tips_member_type);
                }
            } else {
                holder.tvTag.setVisibility(View.VISIBLE);
                holder.tvTag.setText(R.string.chat_tips_member_type);
            }
        } else {
            if (position > 0) {
                char last = mData.get(position - 1).getFirstLetter().toUpperCase().charAt(0);
                char current = data.getFirstLetter().toUpperCase().charAt(0);
                if (last == current) {
                    holder.tvTag.setVisibility(View.GONE);
                } else {
                    holder.tvTag.setVisibility(View.VISIBLE);
                    holder.tvTag.setText(data.getFirstLetter());
                }
            } else {
                holder.tvTag.setVisibility(View.VISIBLE);
                holder.tvTag.setText(data.getFirstLetter());
            }
        }
        if (MUTE.equals(action) && data.getMutedType() == 2) {
            if (data.getDeadline() == AppConst.TIME_FOREVER) {
                holder.tv_mute_time.setText(R.string.chat_tips_mute_state2);
                holder.tv_mute_time.setVisibility(View.VISIBLE);
                holder.tv_cancel_mute.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        viewModel.setMutedSingle(roomId, data.getId(), 0, position);
                    }
                });
                holder.tv_cancel_mute.setVisibility(View.VISIBLE);
            } else if (data.getDeadline() > System.currentTimeMillis()) {
                // 被禁言则显示剩余禁言时间
                if (muteTimers.get(holder.hashCode()) == null) {
                    MuteCountDown timer = new MuteCountDown(data.getDeadline() - System.currentTimeMillis(), 1000L, holder);
                    muteTimers.put(holder.hashCode(), timer);
                    timer.start();
                }
                holder.tv_cancel_mute.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        viewModel.setMutedSingle(roomId, data.getId(), 0, position);
                    }
                });
                holder.tv_mute_time.setVisibility(View.VISIBLE);
                holder.tv_cancel_mute.setVisibility(View.VISIBLE);
            }
        } else {
            holder.tv_mute_time.setVisibility(View.GONE);
            holder.tv_cancel_mute.setVisibility(View.GONE);
        }
        if (data.getMemberLevel() == LEVEL_USER) {
            holder.tv_member_level.setVisibility(View.GONE);
        } else if (data.getMemberLevel() == LEVEL_ADMIN) {
            holder.tv_member_level.setVisibility(View.VISIBLE);
            holder.tv_member_level.setText(R.string.core_tips_group_admin);
            holder.tv_member_level.setBackgroundResource(R.drawable.shape_yellow_r4);
        } else {
            holder.tv_member_level.setVisibility(View.VISIBLE);
            holder.tv_member_level.setText(R.string.core_tips_group_master);
            holder.tv_member_level.setBackgroundResource(R.drawable.shape_blue_r4);
        }

        holder.itemView.setClickable(true);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectable) {
                    holder.cb_select.performClick();
                } else {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(holder.itemView, position);
                    }
                }
            }
        });
        holder.cb_select.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // 记录选中状态
                checkState.put(data.getId(), isChecked);
                if (mOnCheckChangedListener != null) {
                    mOnCheckChangedListener.onCheckChanged(buttonView, isChecked, data);
                }
            }
        });

        //holder.tvName.setText(data.getDisplayName());
        holder.tvName.highlightSearchText(data.getDisplayName(), mSearchKeyword);
        if (!TextUtils.isEmpty(data.getAvatar())) {
            Glide.with(mContext).load(data.getAvatar())
                    .apply(new RequestOptions().placeholder(R.mipmap.default_avatar_round)).into(holder.iv_avatar);
        } else {
            holder.iv_avatar.setImageResource(R.mipmap.default_avatar_round);
        }
        holder.iv_avatar.setIconRes(data.isIdentified() ? R.drawable.ic_user_identified : -1);
        holder.cb_select.setChecked(Boolean.TRUE.equals(checkState.get(data.getId())));
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

    public interface OnCheckChangedListener {
        void onCheckChanged(View view, boolean checked, RoomContact bean);
    }

    private OnCheckChangedListener mOnCheckChangedListener;

    public void setOnCheckChangedListener(OnCheckChangedListener mOnCheckChangedListener) {
        this.mOnCheckChangedListener = mOnCheckChangedListener;
    }
    //**************************************************************

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox cb_select;
        ChatAvatarView iv_avatar;
        TextView tvTag, tv_member_level, tv_mute_time, tv_cancel_mute;
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
        if (mData.get(position).getMemberLevel() == LEVEL_USER) {
            return mData.get(position).getFirstLetter().charAt(0);
        } else {
            return mData.get(position).priority();
        }
    }

    /**
     * 根据分类的首字母的Char ascii值获取其第一次出现该首字母的位置
     */
    public int getPositionForSection(int section, int memberLevel) {
        for (int i = 0; i < getItemCount(); i++) {
            if (mData.get(i).getMemberLevel() == LEVEL_USER) {
                String sortStr = mData.get(i).getFirstLetter();
                char firstChar = sortStr.toUpperCase().charAt(0);
                if (firstChar == section) {
                    return i;
                }
            } else {
                int priority = mData.get(i).priority();
                if (priority > 1 && memberLevel > 1) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * 倒计时控制类
     */
    public class MuteCountDown extends CountDownTimer {

        private ViewHolder holder;

        public MuteCountDown(long millisInFuture, long countDownInterval, ViewHolder holder) {
            super(millisInFuture, countDownInterval);
            this.holder = holder;
        }

        @Override
        public void onTick(long millisUntilFinished) {
            holder.tv_mute_time.setText(mContext.getString(R.string.chat_tips_mute_state5, com.fzm.chat33.utils.StringUtils.formatMutedTime(millisUntilFinished)));
        }

        @Override
        public void onFinish() {
            holder.tv_mute_time.setVisibility(View.GONE);
            holder.tv_cancel_mute.setVisibility(View.GONE);
        }
    }

    public void setSearchKeyword(String keyword) {
        mSearchKeyword = keyword;
    }
}
