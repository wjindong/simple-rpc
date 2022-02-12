package com.simple.rpc.client.netty;

import com.simple.rpc.bean.Beat;
import com.simple.rpc.bean.BeatToProvider;
import com.simple.rpc.bean.Request;
import com.simple.rpc.bean.Response;
import com.simple.rpc.client.core.ProviderContainer;
import com.simple.rpc.client.future.FutureResult;
import com.simple.rpc.registry.bean.ProviderInformation;

import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

public class ConsumerChannelHandler extends SimpleChannelInboundHandler<Response> {
    private static final Logger logger= LoggerFactory.getLogger(ConsumerChannelHandler.class);

    //当前channel对应的provider
    private ProviderInformation provider;

    // 存放未完成的PRC请求，等待服务端返回结果后进行处理  <requestId,FutureResult>
    private final ConcurrentHashMap<String, FutureResult> undone = new ConcurrentHashMap<>();

    private Channel channel;

    //发送ping的重试次数
    private int pingRetryTimes=0;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Response msg){
        if(Beat.BEAT_ID.equals(msg.getRequestId())){
            logger.info("收到服务端的响应");
            pingRetryTimes=0;
            return;
        }

        String requestId=msg.getRequestId();
        FutureResult futureResult=undone.get(requestId);
        futureResult.setResponse(msg);

        undone.remove(requestId);
    }

    public FutureResult sendRequest(Request request){
        FutureResult futureResult=new FutureResult(request);
        undone.put(request.getRequestId(),futureResult);

        channel.writeAndFlush(request).addListener((ChannelFutureListener) future -> {
            if(!future.isSuccess()){
                logger.error("发送请求失败,request id:{}",request.getRequestId());
            }
        });
        return futureResult;
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

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent){
            if(pingRetryTimes>Beat.retryTimes){
                logger.error("重试{}次没有收到响应，关闭连接",pingRetryTimes);
                ctx.close();
                return;
            }
            IdleStateEvent event = (IdleStateEvent)evt;
            if(event.state() == IdleState.READER_IDLE){
                logger.info("READER_IDLE");
            }else if(event.state() == IdleState.WRITER_IDLE){
                logger.info("WRITER_IDLE");
            }else if(event.state() == IdleState.ALL_IDLE){
                logger.info("ALL_IDLE");
            }
            //发送心跳
            sendRequest(BeatToProvider.PING);
            logger.info("向服务器 {} 发送心跳",ctx.channel().remoteAddress());
        }else {
            super.userEventTriggered(ctx,evt);
        }
    }
}
