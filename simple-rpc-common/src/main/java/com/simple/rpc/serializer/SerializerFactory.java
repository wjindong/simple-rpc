package com.simple.rpc.serializer;

import com.simple.rpc.serializer.protostuff.ProtostuffSerializer;

/**
 * 产生序列化对象的工厂类，client与server端的序列化对象均从这里获取，便于后续实现可插拔的序列化实现
 */
public class SerializerFactory {
    private SerializerFactory(){}

    private volatile static Serializer instance=null;
    public static Serializer getInstance(){
        if(instance==null){
            synchronized (SerializerFactory.class){
                if(instance==null){
                    instance=new ProtostuffSerializer();
                }
            }
        }
        return instance;
    }
}
