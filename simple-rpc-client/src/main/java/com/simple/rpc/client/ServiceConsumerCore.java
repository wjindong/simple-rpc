package com.simple.rpc.client;

import com.simple.rpc.client.util.Discovery;

//TODO
public class ServiceConsumerCore {
    private Discovery discovery;

    public ServiceConsumerCore(String registryAddress){
        this.discovery=new Discovery(registryAddress);
    }

    public void stop(){
        discovery.stop();
    }
}
