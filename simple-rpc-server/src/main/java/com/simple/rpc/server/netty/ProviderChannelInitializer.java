package com.simple.rpc.server.netty;

import com.simple.rpc.bean.Beat;
import com.simple.rpc.bean.Request;
import com.simple.rpc.bean.Response;
import com.simple.rpc.handler.ByteToObjectHandler;
import com.simple.rpc.handler.ObjectToByteHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ProviderChannelInitializer extends ChannelInitializer<SocketChannel> {
    private static final Logger logger= LoggerFactory.getLogger(ProviderChannelInitializer.class);

    private Map<String, Object> serviceBeanMap;
    private ThreadPoolExecutor rpcWorkerThreadPool;

    public ProviderChannelInitializer(Map<String, Object> serviceBeanMap,ThreadPoolExecutor rpcWorkerThreadPool){
        this.serviceBeanMap=serviceBeanMap;
        this.rpcWorkerThreadPool=rpcWorkerThreadPool;

    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline=ch.pipeline();
        //心跳检测
        pipeline.addLast(new IdleStateHandler(0,0, Beat.idleTime*Beat.retryTimes, TimeUnit.SECONDS));
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