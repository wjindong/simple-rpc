package com.simple.rpc.registry.bean;

import com.simple.rpc.util.StringUtil;

import java.io.Serializable;
import java.util.Objects;

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

    @Override
    public String toString() {
        return StringUtil.ObjectToJson(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServiceInformation that = (ServiceInformation) o;
        return Objects.equals(serviceInterface, that.serviceInterface) &&
                Objects.equals(serviceVersion, that.serviceVersion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceInterface, serviceVersion);
    }
}
