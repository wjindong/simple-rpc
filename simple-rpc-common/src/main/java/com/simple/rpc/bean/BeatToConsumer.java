package com.simple.rpc.bean;

/**
 * 向客户端发送的心跳包，Response类型
 */
public class BeatToConsumer {
    public static Response PING;
    static {
        PING=new Response();
        PING.setRequestId(Beat.BEAT_ID);
    }
}
