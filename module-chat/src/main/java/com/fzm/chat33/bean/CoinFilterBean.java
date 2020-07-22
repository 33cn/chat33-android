package com.fzm.chat33.bean;

import java.io.Serializable;

/**
 * @author zhengjy
 * @since 2019/03/12
 * Description:
 */
public class CoinFilterBean implements Serializable {

    public final String coinName;
    public boolean isSelected;

    public CoinFilterBean(String coinName, boolean isSelected) {
        this.coinName = coinName;
        this.isSelected = isSelected;
    }

    public CoinFilterBean(String coinName) {
        this.coinName = coinName;
        this.isSelected = false;
    }
}
