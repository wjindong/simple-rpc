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

///TODO
public class ProviderChannelHandler extends SimpleChannelInboundHandler<Request> {
    private static final Logger logger = LoggerFactory.getLogger(ProviderChannelHandler.class);
    private static long callTimes=0;

    private Map<String,Object> serviceBeanMap=null;
    private ThreadPoolExecutor rpcWorkerThreadPool=null;

    public ProviderChannelHandler(Map<String,Object> serviceBeanMap, ThreadPoolExecutor rpcWorkerThreadPool){
        this.serviceBeanMap=serviceBeanMap;
        this.rpcWorkerThreadPool=rpcWorkerThreadPool;
    }
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Request request) throws Exception {
        logger.info("server call times: {}",++callTimes);

        //将任务提交线程池
        rpcWorkerThreadPool.execute(()->{
            //1.设置请求id
            Response response=new Response();
            response.setRequestId(request.getRequestId());
            //2.通过反射执行请求
            try{
                Object res=call(request);
                response.setResult(res);
                System.out.println(res);
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
        String className = request.getServiceInterface();
        String version = request.getServiceVersion();
        String serviceKey = StringUtil.makeServiceKey(className,version);
        Object serviceBean = serviceBeanMap.get(serviceKey);
        if (serviceBean == null) {
            logger.error("Can not find service implement with interface name: {} and version: {}", className, version);
            return null;
        }

        Class<?> serviceClass = serviceBean.getClass();
        String methodName = request.getMethodName();
        Class<?>[] parameterTypes = request.getParameterTypes();
        Object[] parameters = request.getParameters();

        logger.debug(serviceClass.getName());
        logger.debug(methodName);
        for (int i = 0; i < parameterTypes.length; ++i) {
            logger.debug(parameterTypes[i].getName());
        }
        for (int i = 0; i < parameters.length; ++i) {
            logger.debug(parameters[i].toString());
        }

        // JDK reflect
        Method method = serviceClass.getMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method.invoke(serviceBean, parameters);
    }
}
