package com.simple.rpc.bean;

import java.io.Serializable;

public class Response implements Serializable {
    private Long requestId; //相应信息对应的请求id
    private String throwableMessage; //出现异常时的信息
    private Object result; //响应结果


    /////////////////  Getter and Setter ///////////////////////
    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public String getThrowableMessage() {
        return throwableMessage;
    }

    public void setThrowableMessage(String throwableMessage) {
        this.throwableMessage = throwableMessage;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }
}
