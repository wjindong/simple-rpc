package com.simple.rpc.handler;

import com.simple.rpc.serializer.Serializer;
import com.simple.rpc.serializer.SerializerFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 将channel中inbound方向的字节流转化为指定的Java对象，供后续handle调用
 */
public class ByteToObjectHandler extends ByteToMessageDecoder {
    private static final Logger logger = LoggerFactory.getLogger(ByteToObjectHandler.class);
    private Class<?> classType; //转化的对象类型
    private Serializer serializer= SerializerFactory.getInstance();

    public ByteToObjectHandler(Class<?> classType){
        this.classType=classType;
    }


    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        /**
         * 不用手动读取长度，直接在 LengthFieldBasedFrameDecoder 中跳过开头的长度域
         */
//        if (in.readableBytes() < 4) {
//            return;
//        }
//        in.markReaderIndex();
//        int dataLength = in.readInt();
//        if (in.readableBytes() < dataLength) {
//            in.resetReaderIndex();
//            return;
//        }
//        byte[] data = new byte[dataLength];
        byte[]data=new byte[in.readableBytes()];
        in.readBytes(data);
        //Object obj = null;
        try {
            Object obj = serializer.deserialize(data, classType);
            out.add(obj);
        } catch (Exception e) {
            logger.error("ByteToObjectHandler error: " + e.toString());
        }
    }
}
