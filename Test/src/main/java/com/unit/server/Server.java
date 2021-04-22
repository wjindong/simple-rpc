package com.unit.server;

import com.app.test.service.*;
import com.app.test.*;
import com.simple.rpc.server.ServiceProviderCore;

public class Server {
    public static void main(String[] args) throws InterruptedException {
        String serverAddress = "127.0.0.1:18878";
        String registryAddress = "192.168.163.128:2181";
        ServiceProviderCore rpcServer = new ServiceProviderCore(serverAddress, registryAddress);
        HelloService helloService1 = new HelloServiceImpl();
        rpcServer.addService(HelloService.class.getName(), "1.0", helloService1);
        HelloService helloService2 = new HelloServiceImpl2();
        rpcServer.addService(HelloService.class.getName(), "2.0", helloService2);
        PersonService personService = new PersonServiceImpl();
        rpcServer.addService(PersonService.class.getName(), "", personService);
        try {
            rpcServer.start();
        } catch (Exception ex) {
            System.err.println("Exception: "+ ex);
        }
        System.out.println("ok...");

        //Thread.sleep(1000*20);

        //rpcServer.stop();
    }
}
