package com.simple.rpc.util;

import com.alibaba.fastjson.JSON;

import java.util.ArrayList;
import java.util.List;

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
        if(!validIPv4(array[0])) return false;
        //check port
        try{
            Integer.valueOf(array[1]);
        }catch (NumberFormatException e){
            return false;
        }

        return true;
    }

    /**
     *  验证 IPv4 地址格式是否合法
     */
    public static boolean validIPv4(String string){
        String[]strings=splitIPv4(string);
        if(strings.length!=4) return false;

        for (int i=0;i<strings.length;i++){
            int n=stringToNum(strings[i]);

            if(n<0 || n>255) return false;

            if(n!=0 && strings[i].charAt(0)=='0') return false;
            if(strings[i].length()>=2 && strings[i].charAt(0)=='0' && strings[i].charAt(1)=='0') return false;
        }

        return true;
    }

    private static String[]splitIPv4(String s){
        List<String> res=new ArrayList<>();
        StringBuilder sb=new StringBuilder();

        for(char c:s.toCharArray()){
            if(c=='.'){
                res.add(sb.toString());
                sb=new StringBuilder();
            }
            else sb.append(c);
        }
        res.add(sb.toString());

        String[]strings=new String[res.size()];
        for(int i=0;i< res.size();i++) strings[i]=res.get(i);

        return strings;
    }

    private static int stringToNum(String s){
        if(s.length()==0) return -1;

        int res=0;
        for(char c:s.toCharArray()){
            if(c>='0' && c<='9'){
                res=res*10+c-'0';
            }else return -1;
        }

        return res;
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
