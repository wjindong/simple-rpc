package com.simple.rpc.bean;

/**
 * 向服务器发送的心跳包，Request类型
 */
public class BeatToProvider {
    public static Request PING;
    static {
        PING=new Request();
        PING.setRequestId(Beat.BEAT_ID);
    }
}
