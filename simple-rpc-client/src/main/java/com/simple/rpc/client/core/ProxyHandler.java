package com.simple.rpc.client.core;

import com.simple.rpc.bean.Request;
import com.simple.rpc.client.async.AsyncClient;
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

    private Class<?> serviceClass;
    private String serviceVersion;

    public ProxyHandler(Class<?> serviceClass,String serviceVersion){
        this.serviceClass=serviceClass;
        this.serviceVersion=serviceVersion;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        /**
         * object方法特殊处理
         */
        if (Object.class == method.getDeclaringClass()) {
            String name = method.getName();
            if ("equals".equals(name)) {
                return proxy == args[0];
            } else if ("hashCode".equals(name)) {
                return System.identityHashCode(proxy);
            } else if ("toString".equals(name)) {
                return proxy.getClass().getName() + "@" +
                        Integer.toHexString(System.identityHashCode(proxy)) +
                        ", with InvocationHandler " + this;
            } else { //wait notify notifyAll
                LOGGER.error("调用了Object中不支持的方法");
                throw new IllegalStateException(String.valueOf(method));
            }
        }

        Request request=createRequest(method.getName(),args);
        String key= StringUtil.makeServiceKey(serviceClass.getName(),serviceVersion);
        ConsumerChannelHandler handler=ProviderContainer.getInstance().getHandler(key);

        FutureResult result= handler.sendRequest(request);

        return result.get();
    }

    @Override
    public FutureResult call(String methodName, Object[] args) throws Exception{
        Request request=createRequest(methodName,args);
        String key= StringUtil.makeServiceKey(serviceClass.getName(),serviceVersion);
        ConsumerChannelHandler handler=ProviderContainer.getInstance().getHandler(key);

        FutureResult result= handler.sendRequest(request);

        return result;
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
