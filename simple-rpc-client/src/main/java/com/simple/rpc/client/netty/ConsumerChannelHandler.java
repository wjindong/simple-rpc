package com.simple.rpc.client.netty;

import com.simple.rpc.bean.Response;
import com.simple.rpc.client.util.ProviderContainer;
import com.simple.rpc.registry.bean.ProviderInformation;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

//TODO
public class ConsumerChannelHandler extends SimpleChannelInboundHandler<Response> {
    //当前channel对应的provider
    private ProviderInformation provider;

    private Channel channel;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Response msg) throws Exception {

    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        channel= ctx.channel();
    }

    /**
     * 当前通道被关闭时，删除此通道对应的 provider 信息
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        ProviderContainer.getInstance().removeProvider(provider);
    }

    /**
     * 关闭此handler对应的通道
     */
    public void close(){
        //发送一个空包，操作完成后关闭通道 ChannelFutureListener.CLOSE
        channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }

    public void setProvider(ProviderInformation provider) {
        this.provider = provider;
    }
}
