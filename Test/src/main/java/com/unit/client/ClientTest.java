package com.unit.client;

import com.simple.rpc.client.ServiceConsumerCore;

public class ClientTest {
    public static void main(String[] args) throws InterruptedException {
        ServiceConsumerCore consumerCore=new ServiceConsumerCore("192.168.163.128:2181");


        for(int i=0;i<10;i++){
            Thread.sleep(1000);
            System.out.println("ping ");
        }
        consumerCore.stop();
    }
}
