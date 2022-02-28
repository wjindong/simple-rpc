package com.simple.rpc.client.balance.impl;

import com.simple.rpc.client.balance.ProviderChoose;
import com.simple.rpc.client.netty.ConsumerChannelHandler;
import com.simple.rpc.registry.bean.ProviderInformation;

import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

@SuppressWarnings("unused")
public class ProviderChooseByLeastActive implements ProviderChoose {

    @Override
    public ProviderInformation getProvider(String key, Map<ProviderInformation, ConsumerChannelHandler> map) throws Exception {
        Map<String, List<ProviderInformation>> serviceMap = getServiceMap(map);

        List<ProviderInformation> providerList = serviceMap.get(key);
        if (providerList == null || providerList.size() == 0) {
            throw new Exception("Can not find connection for service: " + key);
        }

        PriorityQueue<ProviderInformation> priorityQueue= new PriorityQueue<>((o1, o2) -> {
            int active1=0,active2=0;
            if(map.containsKey(o1)) active1=map.get(o1).getUndoneMapSize();
            if(map.containsKey(o2)) active2=map.get(o2).getUndoneMapSize();

            return active1-active2;
        });

        for(ProviderInformation provider:providerList){
            priorityQueue.offer(provider);
        }

        return priorityQueue.poll();
    }

}
