package com.unit.test;

import com.simple.rpc.registry.ZkUtil;

public class ZkUtilTest {
    public static void main(String[] args) throws Exception {
        ZkUtil zkUtil=new ZkUtil("192.168.163.128:2181");
        String path = zkUtil.createNodeByEphemeral(zkUtil.PROVIDER_FILE_NAME, "test".getBytes());
        System.out.println("=====");
        System.out.println(path);
        System.out.println("=====");
        Thread.sleep(5000);
        //System.out.println(zkUtil.getChildrenPath(zkUtil.PROVIDER_FILE_NAME));
        zkUtil.deleteNode(path);
        System.out.println("delete ok");
        Thread.sleep(10000);
        System.out.println("end");
    }
}
