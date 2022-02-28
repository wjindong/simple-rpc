package com.simple.rpc.samples.provider;

import com.simple.rpc.samples.api.GreetingsService;
import com.simple.rpc.server.ServiceProviderCore;

public class Application2 {
    public static void main(String[] args) {
        String serverAddress = "127.0.0.1:18881";// provider address
        String zookeeperAddress = "192.168.163.128:2181";
        //连接zk集群
        //String zookeeperAddress = "192.168.163.128:2181,192.168.163.129:2181,...";

        ServiceProviderCore provider = new ServiceProviderCore(serverAddress, zookeeperAddress);
        provider.addService(GreetingsService.class,"1.0",new GreetingsServiceImpl());

        provider.start();
    }
}
