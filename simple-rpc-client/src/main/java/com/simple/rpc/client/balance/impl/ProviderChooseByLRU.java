package com.simple.rpc.client.balance.impl;

import com.simple.rpc.client.balance.ProviderChoose;
import com.simple.rpc.client.netty.ConsumerChannelHandler;
import com.simple.rpc.registry.bean.ProviderInformation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ProviderChooseByLRU implements ProviderChoose {
    private ConcurrentMap<String, LinkedHashMap<ProviderInformation, ProviderInformation>> jobLRUMap =
            new ConcurrentHashMap<>();
    private long CACHE_VALID_TIME = 0;

    public ProviderInformation doRoute(String serviceKey, List<ProviderInformation> addressList) {
        // cache clear
        if (System.currentTimeMillis() > CACHE_VALID_TIME) {
            jobLRUMap.clear();
            CACHE_VALID_TIME = System.currentTimeMillis() + 1000 * 60 * 60 * 24;
        }

        // init lru
        LinkedHashMap<ProviderInformation, ProviderInformation> lruHashMap = jobLRUMap.get(serviceKey);
        if (lruHashMap == null) {
            /**
             * LinkedHashMap
             * a、accessOrder：ture=访问顺序排序（get/put时排序）/ACCESS-LAST；false=插入顺序排期/FIFO；
             * b、removeEldestEntry：新增元素时将会调用，返回true时会删除最老元素；
             *      可封装LinkedHashMap并重写该方法，比如定义最大容量，超出是返回true即可实现固定长度的LRU算法；
             */
            lruHashMap = new LinkedHashMap<ProviderInformation, ProviderInformation>(16, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<ProviderInformation, ProviderInformation> eldest) {
                    if (super.size() > 1000) {
                        return true;
                    } else {
                        return false;
                    }
                }
            };
            jobLRUMap.putIfAbsent(serviceKey, lruHashMap);
        }

        // put new
        for (ProviderInformation address : addressList) {
            if (!lruHashMap.containsKey(address)) {
                lruHashMap.put(address, address);
            }
        }
        // remove old
        List<ProviderInformation> delKeys = new ArrayList<>();
        for (ProviderInformation existKey : lruHashMap.keySet()) {
            if (!addressList.contains(existKey)) {
                delKeys.add(existKey);
            }
        }
        if (delKeys.size() > 0) {
            for (ProviderInformation delKey : delKeys) {
                lruHashMap.remove(delKey);
            }
        }

        // load
        ProviderInformation eldestKey = lruHashMap.entrySet().iterator().next().getKey();
        ProviderInformation eldestValue = lruHashMap.get(eldestKey);
        return eldestValue;
    }

    @Override
    public ProviderInformation getProvider(String serviceKey, Map<ProviderInformation, ConsumerChannelHandler> connectedServerNodes) throws Exception {
        Map<String, List<ProviderInformation>> serviceMap = getServiceMap(connectedServerNodes);
        List<ProviderInformation> addressList = serviceMap.get(serviceKey);
        if (addressList != null && addressList.size() > 0) {
            return doRoute(serviceKey, addressList);
        } else {
            throw new Exception("Can not find connection for service: " + serviceKey);
        }
    }
}
