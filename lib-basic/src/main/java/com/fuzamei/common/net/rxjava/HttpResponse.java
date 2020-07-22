package com.fuzamei.common.net.rxjava;


import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class HttpResponse<T> implements Serializable {

    //code ： 0：正常返回 ，1：返回空数据
    private int code;
    private String count = "0";
    @SerializedName(value = "message", alternate = "msg")
    public String message;
    private T data;


    public String getMsg() {
        return message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setMsg(String msg) {
        this.message = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
