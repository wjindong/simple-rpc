package com.simple.rpc.server.netty;

import com.simple.rpc.bean.Request;
import com.simple.rpc.bean.Response;
import com.simple.rpc.util.StringUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

public class ProviderChannelHandler extends SimpleChannelInboundHandler<Request> {
    private static final Logger logger = LoggerFactory.getLogger(ProviderChannelHandler.class);
    private static long callTimes=0;

    private Map<String,Object> serviceBeanMap=null;
    private ThreadPoolExecutor rpcWorkerThreadPool=null;

    public ProviderChannelHandler(Map<String,Object> serviceBeanMap, ThreadPoolExecutor rpcWorkerThreadPool){
        this.serviceBeanMap=serviceBeanMap;
        this.rpcWorkerThreadPool=rpcWorkerThreadPool;
        logger.info("server bean:{}",serviceBeanMap);
    }
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Request request) throws Exception {
        logger.debug("server call times: {}",++callTimes);
        if(callTimes%5000==0){
            logger.info("server call times: {}",callTimes);
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
            }catch (Throwable e){
                response.setThrowableMessage(e.getMessage());
            }
            //3.将response写入管道，返回给客户端
            ctx.writeAndFlush(response);
        });
    }

    /**
     * 通过反射执行任务，返回结果
     */
    private Object call(Request request) throws Throwable{
        String key=StringUtil.makeServiceKey(request.getServiceInterface(),request.getServiceVersion());
        Object serviceBean=serviceBeanMap.get(key);
        if(serviceBean==null){
            logger.error("请求了不存在的服务");
            return null;
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
        rpcWorkerThreadPool.shutdown();
    }
}
