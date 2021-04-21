package com.simple.rpc.handler;

import com.simple.rpc.serializer.Serializer;
import com.simple.rpc.serializer.SerializerFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ObjectToByteHandler extends MessageToByteEncoder {
    private static final Logger logger = LoggerFactory.getLogger(ByteToObjectHandler.class);
    private Class<?> classType; //转化的对象类型
    private Serializer serializer= SerializerFactory.getInstance();

    public ObjectToByteHandler(Class<?> classType){
        this.classType=classType;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        //不是指定的类型则直接返回
        if(classType.isInstance(msg)) return;

        try {
            byte[] data = serializer.serialize(msg);
            //先写入数据长度，再写入数据。处理TCP粘包
            out.writeShort(data.length);
            out.writeBytes(data);
        } catch (Exception e) {
            logger.error("ObjectToByteHandler error: " + e.toString());
        }
    }
}
