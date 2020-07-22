package com.fzm.chat33.core.bean.wallet;

public class Chains extends BaseBean {


    /**
     * low : 0
     * high : 0
     * last : 0
     * rmb : 0
     */

    private float low;
    private float high;
    private float last;
    private float rmb;


    public float getLow() {
        return low;
    }

    public void setLow(float low) {
        this.low = low;
    }

    public float getHigh() {
        return high;
    }

    public void setHigh(float high) {
        this.high = high;
    }

    public float getLast() {
        return last;
    }

    public void setLast(float last) {
        this.last = last;
    }

    public float getRmb() {
        return rmb;
    }

    public void setRmb(float rmb) {
        this.rmb = rmb;
    }
}
