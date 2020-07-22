package com.fuzamei.common.net.rxjava;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;


public class HttpResult<T> implements Serializable{
    private static final long serialVersionUID = 8343685326377195995L;

    @SerializedName(value = "code", alternate = "result")
    private int code;
    public String error;
    private String message;
    private T data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
