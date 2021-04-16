package com.netty.rpc.registry.bean;

import java.io.Serializable;
import java.util.List;

/**
 * 服务提供方的详细信息，包含提供方IP、端口号，提供的所有服务的详细信息
 */
public class ProviderInformation implements Serializable {

    private String providerHost;
    private int providerPort;
    private List<ServiceInformation> serviceList;

    ///TODO:toJson fromJson

    // Getter and Setter
    public String getProviderHost(){
        return providerHost;
    }
    public void setProviderHost(String providerHost){
        this.providerHost=providerHost;
    }
    public int getProviderPort() {
        return providerPort;
    }
    public void setProviderPort(int providerPort) {
        this.providerPort = providerPort;
    }
    public List<ServiceInformation> getServiceInfoList() {
        return serviceList;
    }
    public void setServiceInfoList(List<ServiceInformation> serviceList) {
        this.serviceList = serviceList;
    }


//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//        RpcProtocol that = (RpcProtocol) o;
//        return port == that.port &&
//                Objects.equals(host, that.host) &&
//                isListEquals(serviceInfoList, that.getServiceInfoList());
//    }
//
//    private boolean isListEquals(List<RpcServiceInfo> thisList, List<RpcServiceInfo> thatList) {
//        if (thisList == null && thatList == null) {
//            return true;
//        }
//        if ((thisList == null && thatList != null)
//                || (thisList != null && thatList == null)
//                || (thisList.size() != thatList.size())) {
//            return false;
//        }
//        return thisList.containsAll(thatList) && thatList.containsAll(thisList);
//    }
//
//    @Override
//    public int hashCode() {
//        return Objects.hash(host, port, serviceInfoList.hashCode());
//    }
//
//    @Override
//    public String toString() {
//        return toJson();
//    }
}
