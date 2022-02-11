package com.simple.rpc.client;

import com.simple.rpc.client.async.AsyncClient;
import com.simple.rpc.client.core.Discovery;
import com.simple.rpc.client.core.ProxyHandler;

import java.lang.reflect.Proxy;

public class ServiceConsumerCore {
    private final Discovery discovery;

    public ServiceConsumerCore(String registryAddress){
        this.discovery=new Discovery(registryAddress);
    }

    @SuppressWarnings("unchecked")
    public <T> T createService(Class<T> serviceClass,String serviceVersion){
        return (T)Proxy.newProxyInstance(serviceClass.getClassLoader(),new Class[]{serviceClass},
                new ProxyHandler(serviceClass,serviceVersion));
    }

    public <T> AsyncClient createAsyncService(Class<T> serviceClass, String serviceVersion){
        return new ProxyHandler(serviceClass,serviceVersion);
    }

    public void stop(){
        discovery.stop();
    }
}
