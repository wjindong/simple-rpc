package com.unit.test;

import com.simple.rpc.server.ServiceProviderCore;

public class UnitTest {
    public static void main(String[] args) {
        ServiceProviderCore serviceProviderCore=new ServiceProviderCore("127.0.0.1:1259","127.0.0.1:1259");
        serviceProviderCore.addService(Object.class,"1.0",new Object());
        serviceProviderCore.start();
    }
}
