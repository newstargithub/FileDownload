package com.halo.update.callback;

/**
 * 网络请求响应结果
 */
public class Result<T> {
    public static final int CODE_SUCCESS = 10000;
    public static final int CODE_NO_VERSION = 30001;

    private int code;

    //对应的提示信息
    private String msg;

    //result: 返回前端需要的结果数据
    private T result;

    public Result() {
    }

    public Result(String msg, int statusCode) {
        this.msg = msg;
        this.code = statusCode;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }

    public int getStatusCode() {
        return code;
    }

    public T getData() {
        return result;
    }
}
