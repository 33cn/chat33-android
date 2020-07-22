package com.fzm.chat33.main.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.fzm.chat33.R;
import com.fzm.chat33.core.db.bean.BriefChatLog;
import com.fzm.chat33.core.db.bean.ChatMessage;
import com.fzm.chat33.widget.forward.ForwardRowBase;
import com.fzm.chat33.widget.forward.ForwardRowEncrypted;
import com.fzm.chat33.widget.forward.ForwardRowFile;
import com.fzm.chat33.widget.forward.ForwardRowImage;
import com.fzm.chat33.widget.forward.ForwardRowText;
import com.fzm.chat33.widget.forward.ForwardRowUnsupported;
import com.fzm.chat33.widget.forward.ForwardRowVideo;

import java.util.List;

/**
 * @author zhengjy
 * @since 2018/12/27
 * Description:
 */
public class ForwardListAdapter extends RecyclerView.Adapter<ForwardRowBase> {

    private static final int MESSAGE_TYPE_SYSTEM = 0;
    private static final int MESSAGE_TYPE_TXT = 1;
    private static final int MESSAGE_TYPE_IMAGE = 3;
    private static final int MESSAGE_TYPE_AUDIO = 4;
    private static final int MESSAGE_TYPE_REDBAG = 5;
    private static final int MESSAGE_TYPE_VIDEO = 6;
    private static final int MESSAGE_TYPE_FORWARD = 8;
    private static final int MESSAGE_TYPE_FILE = 9;
    private static final int MESSAGE_TYPE_UNSUPPORTED = 10;
    private static final int MESSAGE_TYPE_ENCRYPTED = 11;
    private static final int MESSAGE_TYPE_TRANSFER = 12;
    private static final int MESSAGE_TYPE_RECEIPT = 13;
    private static final int MESSAGE_TYPE_INVITATION = 14;

    private List<BriefChatLog> mData;
    private Context mContext;
    private ChatMessage message;

    public ForwardListAdapter(Context context, ChatMessage message) {
        this.mContext = context;
        this.message = message;
        this.mData = message.msg.sourceLog;
        long last = 0;
        if (mData != null) {
            for (int i = 0; i < mData.size(); i++) {
                if (i == 0) {
                    last = mData.get(i).datetime;
                    mData.get(i).showTime = true;
                } else {
                    long cur = mData.get(i).datetime;
                    if (cur - last > 60 * 10 * 1000) {
                        last = cur;
                        mData.get(i).showTime = true;
                    } else {
                        mData.get(i).showTime = false;
                    }
                }
            }
        }
    }

    @NonNull
    @Override
    public ForwardRowBase onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ForwardRowBase forwardRow;
        switch (viewType) {
            case MESSAGE_TYPE_ENCRYPTED:
                forwardRow = new ForwardRowEncrypted(mContext, LayoutInflater.from(mContext)
                        .inflate(R.layout.forward_row_encrypted, parent, false), this);
                break;
            case MESSAGE_TYPE_SYSTEM:
                forwardRow = new ForwardRowText(mContext, LayoutInflater.from(mContext)
                        .inflate(R.layout.forward_row_text, parent, false), this);
                break;
            case MESSAGE_TYPE_TXT:
                forwardRow = new ForwardRowText(mContext, LayoutInflater.from(mContext)
                        .inflate(R.layout.forward_row_text, parent, false), this);
                break;
            case MESSAGE_TYPE_AUDIO:
                forwardRow = new ForwardRowText(mContext, LayoutInflater.from(mContext)
                        .inflate(R.layout.forward_row_text, parent, false), this);
                break;
            case MESSAGE_TYPE_IMAGE:
                forwardRow = new ForwardRowImage(mContext, LayoutInflater.from(mContext)
                        .inflate(R.layout.forward_row_image, parent, false), this);
                break;
            case MESSAGE_TYPE_REDBAG:
                forwardRow = new ForwardRowText(mContext, LayoutInflater.from(mContext)
                        .inflate(R.layout.forward_row_text, parent, false), this);
                break;
            case MESSAGE_TYPE_VIDEO:
                forwardRow = new ForwardRowVideo(mContext, LayoutInflater.from(mContext)
                        .inflate(R.layout.forward_row_video, parent, false), this);
                break;
            case MESSAGE_TYPE_FORWARD:
                forwardRow = new ForwardRowText(mContext, LayoutInflater.from(mContext)
                        .inflate(R.layout.forward_row_text, parent, false), this);
                break;
            case MESSAGE_TYPE_FILE:
                forwardRow = new ForwardRowFile(mContext, LayoutInflater.from(mContext)
                        .inflate(R.layout.forward_row_file, parent, false), this);
                break;
            case MESSAGE_TYPE_TRANSFER:
                forwardRow = new ForwardRowText(mContext, LayoutInflater.from(mContext)
                        .inflate(R.layout.forward_row_text, parent, false), this);
                break;
            case MESSAGE_TYPE_RECEIPT:
                forwardRow = new ForwardRowText(mContext, LayoutInflater.from(mContext)
                        .inflate(R.layout.forward_row_text, parent, false), this);
                break;
            case MESSAGE_TYPE_INVITATION:
                forwardRow = new ForwardRowText(mContext, LayoutInflater.from(mContext)
                        .inflate(R.layout.forward_row_text, parent, false), this);
                break;
            default:
                forwardRow = new ForwardRowUnsupported(mContext, LayoutInflater.from(mContext)
                        .inflate(R.layout.forward_row_notification, parent, false), this);
                break;
        }
        forwardRow.setMessage(message);
        return forwardRow;
    }

    @Override
    public void onBindViewHolder(@NonNull ForwardRowBase holder, int position) {
        holder.setView(mData.get(position), position);
    }

    @Override
    public int getItemCount() {
        return mData != null ? mData.size() : 0;
    }

    public BriefChatLog getItem(int position) {
        if (mData == null) {
            return null;
        }
        return mData.get(position);
    }

    @Override
    public int getItemViewType(int position) {
        int msgType = mData.get(position).msgType;
        if (!TextUtils.isEmpty(mData.get(position).msg.encryptedMsg)) {
            return MESSAGE_TYPE_ENCRYPTED;
        } else if (msgType == ChatMessage.Type.SYSTEM) {
            return MESSAGE_TYPE_SYSTEM;
        } else if (msgType == ChatMessage.Type.TEXT) {
            return MESSAGE_TYPE_TXT;
        } else if (msgType == ChatMessage.Type.IMAGE) {
            return MESSAGE_TYPE_IMAGE;
        } else if (msgType == ChatMessage.Type.AUDIO) {
            return MESSAGE_TYPE_AUDIO;
        } else if (msgType == ChatMessage.Type.RED_PACKET) {
            return MESSAGE_TYPE_REDBAG;
        } else if (msgType == ChatMessage.Type.VIDEO) {
            return MESSAGE_TYPE_VIDEO;
        } else if (msgType == ChatMessage.Type.FORWARD) {
            return MESSAGE_TYPE_FORWARD;
        } else if (msgType == ChatMessage.Type.FILE) {
            return MESSAGE_TYPE_FILE;
        } else if (msgType == ChatMessage.Type.TRANSFER) {
            return MESSAGE_TYPE_TRANSFER;
        } else if (msgType == ChatMessage.Type.RECEIPT) {
            return MESSAGE_TYPE_RECEIPT;
        } else if (msgType == ChatMessage.Type.INVITATION) {
            return MESSAGE_TYPE_INVITATION;
        } else {
            return MESSAGE_TYPE_UNSUPPORTED;
        }
    }
}
