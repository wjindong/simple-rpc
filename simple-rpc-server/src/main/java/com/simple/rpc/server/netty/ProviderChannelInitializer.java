package com.simple.rpc.server.netty;

import com.simple.rpc.bean.Request;
import com.simple.rpc.bean.Response;
import com.simple.rpc.handler.ByteToObjectHandler;
import com.simple.rpc.handler.ObjectToByteHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

public class ProviderChannelInitializer extends ChannelInitializer<SocketChannel> {

    private Map<String, Object> serviceBeanMap;
    private ThreadPoolExecutor rpcWorkerThreadPool;

    public ProviderChannelInitializer(Map<String, Object> serviceBeanMap,ThreadPoolExecutor rpcWorkerThreadPool){
        this.serviceBeanMap=serviceBeanMap;
        this.rpcWorkerThreadPool=rpcWorkerThreadPool;
        System.out.println("server bean:"+serviceBeanMap);
    }

    ///TODO
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline=ch.pipeline();
        ///TODO:1.心跳检测

        //TCP粘包处理
        pipeline.addLast(new LengthFieldBasedFrameDecoder(65535,0,2,0,2));
        //inbound     bytebuf ->> Request
        pipeline.addLast(new ByteToObjectHandler(Request.class));
        //ountbound   Response ->> bytebuf
        pipeline.addLast(new ObjectToByteHandler(Response.class));
        //负责处理客户端的请求。 根据Request调用对应的方法，将结果封装为 Response
        pipeline.addLast(new ProviderChannelHandler(serviceBeanMap,rpcWorkerThreadPool));
    }
}