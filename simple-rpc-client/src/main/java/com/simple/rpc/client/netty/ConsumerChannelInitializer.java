package com.simple.rpc.client.netty;

import com.simple.rpc.bean.Beat;
import com.simple.rpc.bean.Request;
import com.simple.rpc.bean.Response;
import com.simple.rpc.handler.ByteToObjectHandler;
import com.simple.rpc.handler.ObjectToByteHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

public class ConsumerChannelInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        //心跳检测
        ch.pipeline().addLast(new IdleStateHandler(0,0, Beat.idleTime, TimeUnit.SECONDS));
        //TCP粘包处理
        ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(65535,0,2,0,2));
        ch.pipeline().addLast(new ByteToObjectHandler(Response.class));
        ch.pipeline().addLast(new ObjectToByteHandler(Request.class));
        ch.pipeline().addLast(new ConsumerChannelHandler());
    }
}
