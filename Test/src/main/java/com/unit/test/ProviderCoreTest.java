package com.unit.test;

import com.simple.rpc.server.ServiceProviderCore;
import com.unit.hello.Hello;
import com.unit.hello.HelloImpl;

public class ProviderCoreTest {
    public static void main(String[] args) {
        String local="127.0.0.1:12594";
        String zk="192.168.163.128:2181,192.168.163.129:2181,192.168.163.130:2181";
        ServiceProviderCore providerCore=new ServiceProviderCore(local,zk);
        providerCore.addService(Hello.class.getName(),"1.023",new Object());
        providerCore.addService(HelloImpl.class.getName(),"",new Object());
        providerCore.start();
    }
}
