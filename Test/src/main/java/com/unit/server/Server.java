package com.unit.server;

import com.app.test.service.*;
import com.app.test.*;
import com.app.test.service.Math;
import com.simple.rpc.server.ServiceProviderCore;

public class Server {
    public static void main(String[] args) throws InterruptedException {
        String serverAddress = "127.0.0.1:18878";// 服务开启地址
        String registryAddress = "192.168.163.128:2181"; //zk 地址

        ServiceProviderCore rpcServer = new ServiceProviderCore(serverAddress, registryAddress);



        try {
            rpcServer.start(); //开始提供服务
        } catch (Exception ex) {
            System.err.println("Exception: "+ ex);
        }
        System.out.println("ok...");

        Thread.sleep(1000*10);
        rpcServer.addService(HelloService.class, "1.0", new HelloServiceImpl());
        rpcServer.updateZookeeper();

        Thread.sleep(1000*10);
        rpcServer.stop();

    }
}
