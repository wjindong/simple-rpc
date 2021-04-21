package com.simple.rpc.client.balance;

import com.simple.rpc.client.netty.ConsumerChannelHandler;
import com.simple.rpc.registry.bean.ProviderInformation;
import com.simple.rpc.registry.bean.ServiceInformation;
import com.simple.rpc.util.StringUtil;
import org.apache.commons.collections4.map.HashedMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface ProviderChoose {

    /**
     * 按provider按服务名分类
     *
     *  key1:{provider1,provider2,...}
     *  key2:{provider1,provider3,..}
     *
     * @param connected
     * @return   Map<String, List<RpcProtocol>> : String:serviceName:version , List: 有此服务的 ProviderInformation
     */
    public default Map<String, List<ProviderInformation>> getServiceMap(Map<ProviderInformation, ConsumerChannelHandler> connected) {

        Map<String, List<ProviderInformation>> res = new HashMap<>();
        if (connected == null || connected.size() == 0) return res;

        for (ProviderInformation provider : connected.keySet()) {
            for (ServiceInformation serviceInfo : provider.getServiceInfoList()) {
                String key = StringUtil.makeServiceKey(serviceInfo.getServiceInterface(), serviceInfo.getServiceVersion());
                List<ProviderInformation> rpcProtocolList = res.get(key);
                if (rpcProtocolList == null) {
                    rpcProtocolList = new ArrayList<>();
                }
                rpcProtocolList.add(provider);
                res.putIfAbsent(key, rpcProtocolList);
            }
        }

        return res;
    }

    public ProviderInformation getProvider(String key, Map<ProviderInformation, ConsumerChannelHandler> map) throws Exception;
}
