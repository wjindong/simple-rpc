package com.simple.rpc.client.core;

import com.simple.rpc.bean.Request;
import com.simple.rpc.client.async.AsyncClient;
import com.simple.rpc.client.exception.SendToServerException;
import com.simple.rpc.client.future.FutureResult;
import com.simple.rpc.client.netty.ConsumerChannelHandler;
import com.simple.rpc.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.UUID;

public class ProxyHandler implements InvocationHandler, AsyncClient {
    private static final Logger LOGGER= LoggerFactory.getLogger(ProxyHandler.class);

    private final Class<?> serviceClass;
    private final String serviceVersion;

    public ProxyHandler(Class<?> serviceClass,String serviceVersion){
        this.serviceClass=serviceClass;
        this.serviceVersion=serviceVersion;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // object方法特殊处理
        if (Object.class == method.getDeclaringClass()) {
            String name = method.getName();
            switch (name) {
                case "equals":
                    return proxy == args[0];
                case "hashCode":
                    return System.identityHashCode(proxy);
                case "toString":
                    return proxy.getClass().getName() + "@" +
                            Integer.toHexString(System.identityHashCode(proxy)) +
                            ", with InvocationHandler " + this;
                default:  //wait notify notifyAll
                    LOGGER.error("调用了Object中不支持的方法");
                    throw new IllegalStateException(String.valueOf(method));
            }
        }

        Request request=createRequest(method.getName(),args);
        String key= StringUtil.makeServiceKey(serviceClass.getName(),serviceVersion);
        ConsumerChannelHandler handler=ProviderContainer.getInstance().getHandler(key);

        FutureResult result=null;
        try {
            result= handler.sendRequest(request);
        }catch (Exception e){
            if(e instanceof SendToServerException){
                //发送失败则重试一次
                try {
                    handler=ProviderContainer.getInstance().getHandler(key);
                }catch (Exception e1){
                    if(e1 instanceof SendToServerException){
                        throw e1;
                    }
                }
                result=handler.sendRequest(request);
            }
        }

        return result.get();
    }

    @Override
    public FutureResult call(String methodName, Object[] args) throws Exception{
        Request request=createRequest(methodName,args);
        String key= StringUtil.makeServiceKey(serviceClass.getName(),serviceVersion);
        ConsumerChannelHandler handler=ProviderContainer.getInstance().getHandler(key);

        return handler.sendRequest(request);
    }

    private Request createRequest(String methodName,Object[]args){
        Request request=new Request();
        request.setRequestId(UUID.randomUUID().toString());
        request.setServiceInterface(serviceClass.getName());
        request.setServiceVersion(serviceVersion);
        request.setMethodName(methodName);

        if(args!=null && args.length>0){
            Class<?>[]argsTypes=new Class[args.length];
            for(int i=0;i<argsTypes.length;i++){
                argsTypes[i]=args[i].getClass();
            }
            request.setParameterTypes(argsTypes);
        }

        request.setParameters(args);

        return request;
    }
}
