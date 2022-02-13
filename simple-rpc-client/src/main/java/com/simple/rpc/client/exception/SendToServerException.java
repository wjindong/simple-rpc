package com.simple.rpc.client.exception;

import com.simple.rpc.registry.bean.ProviderInformation;

public class SendToServerException extends Exception{
    private String resId;
    private ProviderInformation provider;
    private String msg;

    public SendToServerException(){}
    public SendToServerException(String resId,String msg,ProviderInformation provider){
        this.resId=resId;
        this.msg=msg;
        this.provider=provider;
    }

    public String getResId() {
        return resId;
    }

    public void setResId(String resId) {
        this.resId = resId;
    }

    public ProviderInformation getProvider() {
        return provider;
    }

    public void setProvider(ProviderInformation provider) {
        this.provider = provider;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
