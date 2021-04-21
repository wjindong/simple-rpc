package com.simple.rpc.registry.bean;

import com.simple.rpc.util.StringUtil;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * 服务提供方的详细信息，包含提供方IP、端口号，提供的所有服务的详细信息
 */
public class ProviderInformation implements Serializable {

    private String providerHost;
    private int providerPort;
    private List<ServiceInformation> serviceList;

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

    @Override
    public String toString() {
        return StringUtil.ObjectToJson(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProviderInformation that = (ProviderInformation) o;
        return providerPort == that.providerPort &&
                Objects.equals(providerHost, that.providerHost) &&
                Objects.equals(serviceList, that.serviceList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(providerHost, providerPort, serviceList);
    }
}
