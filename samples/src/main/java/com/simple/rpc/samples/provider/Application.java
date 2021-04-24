package com.simple.rpc.samples.provider;

import com.simple.rpc.samples.api.GreetingsService;
import com.simple.rpc.server.ServiceProviderCore;

public class Application {
    public static void main(String[] args) {
        String serverAddress = "127.0.0.1:18879";// provider address
        String registryAddress = "192.168.163.128:2181"; //Zookeeper address

        ServiceProviderCore provider = new ServiceProviderCore(serverAddress, registryAddress);
        provider.addService(GreetingsService.class,"1.0",new GreetingsServiceImpl());

        provider.start();
    }
}
