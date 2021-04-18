package com.simple.rpc.registry.bean;

import java.io.Serializable;

/**
 * 描述一个服务的相关信息，包含服务实现的接口，服务的版本
 */
public class ServiceInformation implements Serializable {
    private String serviceInterface;
    private String serviceVersion;

    // Getter and Setter
    public String getServiceInterface() {
        return serviceInterface;
    }
    public void setServiceInterface(String serviceInterface) {
        this.serviceInterface = serviceInterface;
    }
    public String getServiceVersion() {
        return serviceVersion;
    }
    public void setServiceVersion(String serviceVersion) {
        this.serviceVersion = serviceVersion;
    }

    //    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//        RpcServiceInfo that = (RpcServiceInfo) o;
//        return Objects.equals(serviceName, that.serviceName) &&
//                Objects.equals(version, that.version);
//    }
//
//    @Override
//    public int hashCode() {
//        return Objects.hash(serviceName, version);
//    }
//
//    public String toJson() {
//        String json = JsonUtil.objectToJson(this);
//        return json;
//    }
//
//    @Override
//    public String toString() {
//        return toJson();
//    }
}
