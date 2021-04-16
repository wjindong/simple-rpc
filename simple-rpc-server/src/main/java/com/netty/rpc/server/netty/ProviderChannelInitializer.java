package com.netty.rpc.server.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

public class ProviderChannelInitializer extends ChannelInitializer<SocketChannel> {

    private Map<String, Object> serviceBeanMap;
    private ThreadPoolExecutor rpcWorkerThreadPool;

    public ProviderChannelInitializer(Map<String, Object> serviceBeanMap,ThreadPoolExecutor rpcWorkerThreadPool){
        this.serviceBeanMap=serviceBeanMap;
        this.rpcWorkerThreadPool=rpcWorkerThreadPool;
    }

    ///TODO
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {

    }
}
