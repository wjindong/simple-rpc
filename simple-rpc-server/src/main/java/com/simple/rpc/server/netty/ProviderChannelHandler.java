package com.simple.rpc.server.netty;

import com.simple.rpc.bean.*;
import com.simple.rpc.util.StringUtil;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.LongAdder;

public class ProviderChannelHandler extends SimpleChannelInboundHandler<Request> {
    private static final Logger logger = LoggerFactory.getLogger(ProviderChannelHandler.class);

    //handler调用次数计数
    private static LongAdder callTimes=new LongAdder();

    private Map<String,Object> serviceBeanMap=null;
    private ThreadPoolExecutor rpcWorkerThreadPool=null;

    public ProviderChannelHandler(Map<String,Object> serviceBeanMap, ThreadPoolExecutor rpcWorkerThreadPool){
        this.serviceBeanMap=serviceBeanMap;
        this.rpcWorkerThreadPool=rpcWorkerThreadPool;
        logger.info("server bean:{}",serviceBeanMap);
    }
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Request request) throws Exception {
        //心跳包响应
        if(Beat.BEAT_ID.equals(request.getRequestId())){
            logger.info("收到客户端 {} 的心跳",ctx.channel().remoteAddress());
            ctx.writeAndFlush(BeatToConsumer.PING).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if(future.isSuccess()){
                        logger.info("响应心跳成功");
                    }
                }
            });
            return;
        }

        callTimes.increment();
        logger.debug("server call times: {}",callTimes.longValue());
        if(callTimes.longValue()%5000==0){
            logger.info("server call times: {}",callTimes.longValue());
        }

        //将任务提交线程池
        rpcWorkerThreadPool.execute(()->{
            //1.设置请求id
            Response response=new Response();
            response.setRequestId(request.getRequestId());
            //2.通过反射执行请求
            try{
                Object res=call(request);
                response.setResult(res);
            }catch (Exception e){
                logger.info(e.toString());
                response.setThrowableMessage(e.toString());
                logger.error("出现异常");
            }
            //3.将response写入管道，返回给客户端
            //logger.info(StringUtil.ObjectToJson(response));
            ctx.writeAndFlush(response);
        });
    }

    /**
     * 通过反射执行任务，返回结果
     */
    private Object call(Request request) throws Exception{
        String key=StringUtil.makeServiceKey(request.getServiceInterface(),request.getServiceVersion());
        Object serviceBean=serviceBeanMap.get(key);
        if(serviceBean==null){
            logger.error("请求了不存在的服务");
            throw new Exception("请求了不存在的服务:"+key);
        }

        Class<?> serviceClass=serviceBean.getClass();
        String methodName=request.getMethodName();
        Class<?>[] parameterTypes= request.getParameterTypes();
        Object[] parameters= request.getParameters();

        Method method=serviceClass.getMethod(methodName,parameterTypes);
        return method.invoke(serviceBean,parameters);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        logger.info("断开与 {} 的连接",ctx.channel().remoteAddress());
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent){
            logger.error("客户端 {} 超时未响应，关闭连接",ctx.channel().remoteAddress());
            ctx.close();
        }else {
            super.userEventTriggered(ctx,evt);
        }
    }
}
