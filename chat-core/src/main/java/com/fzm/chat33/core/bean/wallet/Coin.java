package com.fzm.chat33.core.bean.wallet;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ZX on 2018/5/30.
 */

public class Coin extends BaseBean {

    //1:首页推介,2:次页推介
    public static final int RECOMMEND_FIRST = 1;
    public static final int RECOMMEND_SECOND = 2;


    public static final int STATUS_ENABLE = 1;
    public static final int STATUS_DISABLE = -1;
    public static final String COIN_LIST = "coin_list";
    public static final String COIN_NAME= "coin_name";
    public static final String COIN_PRIV= "coin_priv";
    //静态的私钥
    public static String mPriv;

//    private PWallet pWallet;

    //拥有量
    private String balance;
    private float rmbBalance;
    //启用：1 禁用：0
    private int status;

    //基本BipWallet的5项
    private String privkey;
    private String pubkey;
    //过滤服务器返回的地址
    @SerializedName("abdce_a")
    private String address;
    private String pubToAddress;
    private String privToPub;
    //other

    //网络行情合并字段
    @SerializedName("id")
    private String netId;
    private int recommend;
    private String sid;
    private String icon;
    private String name;
    private String nickname;
    private String platform;
    private String chain;
    @SerializedName("contract_address")
    private String contractAddress;
    //单价
    private float rmb;
    private String usd;
    @SerializedName("chain_quotation")
    private Chains chains;

    public String getContractAddress() {
        return contractAddress;
    }

    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
    }

    public float getRmbBalance() {
        return rmbBalance;
    }

    public void setRmbBalance(float rmbBalance) {
        this.rmbBalance = rmbBalance;
    }

    public int getRecommend() {
        return recommend;
    }

    public void setRecommend(int recommend) {
        this.recommend = recommend;
    }

    public Chains getChains() {
        return chains;
    }

    public void setChains(Chains chains) {
        this.chains = chains;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getNetId() {
        return netId;
    }

    public void setNetId(String netId) {
        this.netId = netId;
    }

//    public PWallet getpWallet() {
//        return pWallet;
//    }
//
//    public void setpWallet(PWallet pWallet) {
//        this.pWallet = pWallet;
//    }

    public String getBalance() {
        if(TextUtils.isEmpty(balance)) {
            return "0";
        }
        return balance;
    }

    public void setBalance(String balance) {
        if(TextUtils.isEmpty(balance)) {
            balance = "0";
        }
        this.balance = balance;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

//    public String getPrivkey(String coinType, String mnem) {
//        HDWallet hdWallet = GoUtils.getHDWallet(coinType, mnem);
//        try {
//            byte[] bPrivkey = hdWallet.newKeyPriv(0);
//            privkey = Walletapi.byteTohex(bPrivkey);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return privkey;
//    }

    public void setPrivkey(String privkey) {
        this.privkey = privkey;
    }

    public String getPubkey() {
        return pubkey;
    }

    public void setPubkey(String pubkey) {
        this.pubkey = pubkey;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPubToAddress() {
        return pubToAddress;
    }

    public void setPubToAddress(String pubToAddress) {
        this.pubToAddress = pubToAddress;
    }

    public String getPrivToPub() {
        return privToPub;
    }

    public void setPrivToPub(String privToPub) {
        this.privToPub = privToPub;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getChain() {
        return chain;
    }

    public void setChain(String chain) {
        this.chain = chain;
    }

    public float getRmb() {
        return rmb;
    }

    public void setRmb(float rmb) {
        this.rmb = rmb;
    }

    public String getUsd() {
        return usd;
    }

    public void setUsd(String usd) {
        this.usd = usd;
    }

}
