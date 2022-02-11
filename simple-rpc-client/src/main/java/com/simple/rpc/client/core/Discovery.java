package com.simple.rpc.client.core;

import com.simple.rpc.registry.ZkUtil;
import com.simple.rpc.registry.bean.ProviderInformation;
import com.simple.rpc.util.StringUtil;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class Discovery {
    private static final Logger logger = LoggerFactory.getLogger(Discovery.class);

    private final ZkUtil zkUtil;

    public Discovery(String registryAddress) {
        zkUtil=new ZkUtil(registryAddress);

        //获取zk中的数据
        logger.info("开始获取ZK中的数据");
        discoveryAndUpdateService();

        //对zk中注册的数据进行监听，以便及时感知变化
        try {
            zkUtil.addChildrenListener(zkUtil.REGISTRY_CENTER, (client, event) -> {
                PathChildrenCacheEvent.Type type=event.getType();
                switch (type){
                    case CONNECTION_RECONNECTED:
                        logger.info("重新连接zk，开始更新服务信息");
                        discoveryAndUpdateService();
                        break;
                    case CHILD_ADDED:
                    case CHILD_UPDATED:
                    case CHILD_REMOVED:
                        logger.info("zk数据变化，开始更新服务信息");
                        discoveryAndUpdateService();
                        break;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 从Zookeeper获取所有的服务信息，并将服务信息重新封装成 ProviderInformation，将所有的 ProviderInformation 放入集合中。
     * 然后调用 UpdateConnectedServer 连接到这些 Provider
     */
    private void discoveryAndUpdateService(){
        try {
            //1.获取zk中provider信息的path
            List<String> pathList= zkUtil.getChildrenPath(zkUtil.REGISTRY_CENTER);

            List<ProviderInformation> providerInformations=new ArrayList<>();

            //2.获取path对应的provider信息，封装成 ProviderInformation 对象，存入List
            for(String path:pathList){
                byte[] data=zkUtil.getNodeData(zkUtil.REGISTRY_CENTER+"/"+path);
                ProviderInformation provider = (ProviderInformation) StringUtil.JsonToObject(new String(data),ProviderInformation.class);
                providerInformations.add(provider);
            }
            logger.info("provider information: {}",providerInformations);

            //3.处理获取的信息
            handleProviderInformation(providerInformations);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    private void handleProviderInformation(List<ProviderInformation> list){
        ProviderContainer.getInstance().updateConnectedProvider(list);
    }


    public void stop() {
        this.zkUtil.close();
        ProviderContainer.getInstance().stop();
    }
}
