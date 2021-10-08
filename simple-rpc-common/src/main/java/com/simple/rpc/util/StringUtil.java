package com.simple.rpc.util;

import com.alibaba.fastjson.JSON;

public class StringUtil {
    public static final String ServiceName_Version_Delimiter=":";

    /**
     * 根据服务接口名称和版本返回key
     * @param name 服务接口名
     * @param version 服务版本
     * @return name:version
     *  若version为空或空字符串，返回 name
     */
    public static String makeServiceKey(String name, String version) {
        String res = name;
        if (version != null && version.trim().length() > 0) {
            res = res.concat(ServiceName_Version_Delimiter.concat(version));
        }
        return res;
    }

    /**
     * 检查地址是否是 IP:port 的结构
     * @param address
     * @return
     */
    public static boolean checkAddress(String address){
        String[] array = address.split(":");
        if(array.length<=1) return false;
        //check ip
        String[] ip=array[0].split("\\.");
        if(ip.length<4) return false;
        //check port
        try{
            Integer.valueOf(array[1]);
        }catch (NumberFormatException e){
            return false;
        }

        return true;
    }

    /**
     * 将对象转化为 JSON 字符串
     */
    public static String ObjectToJson(Object obj){
        return JSON.toJSONString(obj);
    }

    public static Object JsonToObject(String json,Class<?> classType){
        return JSON.parseObject(json,classType);
    }
}
