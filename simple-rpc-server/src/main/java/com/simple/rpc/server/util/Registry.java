package com.simple.rpc.server.util;

import com.simple.rpc.registry.ZkUtil;
import com.simple.rpc.registry.bean.ProviderInformation;
import com.simple.rpc.registry.bean.ServiceInformation;
import com.simple.rpc.util.StringUtil;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Registry {
    private static final Logger logger = LoggerFactory.getLogger(Registry.class);

    private ZkUtil zkUtil;
    private String zkNodePath;

    public Registry(String registerAddress){
        zkUtil=new ZkUtil(registerAddress);

    }

    /**
     * 将provider的信息写入注册中心
     * @param providerAddress provider的ip+port
     * @param serviceKeys provider提供的所有服务的信息
     */
    public void serviceInfoToRegistry(String providerAddress, Set<String> serviceKeys) {
        String[] ipAndPort = providerAddress.split(":");
        String ip = ipAndPort[0];
        int port = Integer.parseInt(ipAndPort[1]);

        List<ServiceInformation> serviceInformationList=new ArrayList<>();

        for(String serviceKey:serviceKeys){
            ServiceInformation serviceInformation=new ServiceInformation();


            String[]serviceInfo=serviceKey.split(StringUtil.ServiceName_Version_Delimiter);
            serviceInformation.setServiceInterface(serviceInfo[0]);

            if(serviceInfo.length==2){
                serviceInformation.setServiceVersion(serviceInfo[1]);
            }else{
                logger.warn("服务{}没有版本号",serviceInfo[0]);
            }
            serviceInformationList.add(serviceInformation);
        }

        ProviderInformation providerInformation=new ProviderInformation();
        providerInformation.setProviderHost(ip);
        providerInformation.setProviderPort(port);
        providerInformation.setServiceInfoList(serviceInformationList);

        String json= StringUtil.ObjectToJson(providerInformation);
        logger.info(json);

        //向zk写数据
        try {
            zkNodePath=zkUtil.createNodeByEphemeral(zkUtil.PROVIDER_FILE_NAME, json.getBytes());
        } catch (Exception e) {
            logger.error("向zk写入数据失败:{}",e);
        }
        logger.info("write to zk :{}",zkNodePath);

        /**
         * 添加连接监听器，如果zookeeper断线后重连需要重新注册服务，否则会出现服务器启动，但zookeeper中没有注册信息的情况
         */
        zkUtil.addConnectionStateListener(new ConnectionStateListener() {
            @Override
            public void stateChanged(CuratorFramework curatorFramework, ConnectionState connectionState) {
                if (connectionState == ConnectionState.RECONNECTED) {
                    logger.info("zk重连，服务将会重新注册", connectionState);
                    serviceInfoToRegistry(providerAddress, serviceKeys);
                }
            }
        });
    }

    /**
     * 关闭服务时，清除写入到注册中心的数据
     */
    public void clearRegistry(){
        try {
            zkUtil.deleteNode(zkNodePath);
            zkUtil.close();
        } catch (Exception e) {
            logger.error("remove information from zk exception");
            e.printStackTrace();
        }
    }
}
