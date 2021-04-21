package com.simple.rpc.client.netty;

import com.simple.rpc.bean.Request;
import com.simple.rpc.bean.Response;
import com.simple.rpc.handler.ByteToObjectHandler;
import com.simple.rpc.handler.ObjectToByteHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class ConsumerChannelInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        //TODO:心跳检测

        ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(65535,0,2,0,2));
        ch.pipeline().addLast(new ByteToObjectHandler(Response.class));
        ch.pipeline().addLast(new ObjectToByteHandler(Request.class));
        ch.pipeline().addLast(new ConsumerChannelHandler());
    }
}
