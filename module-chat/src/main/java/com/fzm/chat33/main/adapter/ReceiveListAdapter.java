package com.fzm.chat33.main.adapter;

import android.app.Activity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.fuzamei.common.utils.FinanceUtils;
import com.fzm.chat33.R;
import com.fzm.chat33.core.bean.RedPacketReceiveInfo;
import com.fzm.chat33.core.provider.CoinManager;
import com.fzm.chat33.utils.StringUtils;
import com.fuzamei.common.utils.ScreenUtils;

import java.text.SimpleDateFormat;
import java.util.List;


public class ReceiveListAdapter extends BaseAdapter {
    private List<RedPacketReceiveInfo> list;
    private Activity mContext;

    public ReceiveListAdapter(List<RedPacketReceiveInfo> list, Activity context) {
        this.list = list;
        this.mContext = context;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public RedPacketReceiveInfo getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_recieve_record, null);
            holder = new ViewHolder();
            holder.id_user = convertView.findViewById(R.id.id_user);
            holder.time = convertView.findViewById(R.id.time);
            holder.amount = convertView.findViewById(R.id.amount);
            holder.image = convertView.findViewById(R.id.image);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        RedPacketReceiveInfo item = getItem(position);
        holder.id_user.setText(item.getUserName());
        CoinManager.INSTANCE.getCoinByName(item.getCoinName(), redPacketCoin -> {
            holder.amount.setText(FinanceUtils.getPlainNum(item.getAmount(), redPacketCoin == null ? 1 : redPacketCoin.decimalPlaces) + item.getCoinName());
            return null;
        });
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String time = sdf.format(item.getCreatedAt());
        holder.time.setText(time);
        if (!TextUtils.isEmpty(item.getUserAvatar())) {
            Glide.with(mContext).load(StringUtils.aliyunFormat(item.getUserAvatar(), ScreenUtils.dp2px(mContext, 35)
                    , ScreenUtils.dp2px(mContext, 35)))
                    .into(holder.image);
        } else {
            holder.image.setImageResource(R.mipmap.default_avatar_round);
        }

        return convertView;
    }

    static class ViewHolder {

        TextView id_user;
        TextView time;
        TextView amount;
        ImageView image;

    }
}