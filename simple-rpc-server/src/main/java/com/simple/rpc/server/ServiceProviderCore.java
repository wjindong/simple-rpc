package com.simple.rpc.server;

import com.simple.rpc.server.netty.ProviderChannelInitializer;
import com.simple.rpc.server.util.Registry;
import com.simple.rpc.util.StringUtil;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ServiceProviderCore {
    private static final Logger logger = LoggerFactory.getLogger(ServiceProviderCore.class);

    private Thread providerWorkThread;  //Server工作线程

    private String providerAddress; //服务地址 IP+port

    private final Map<String, Object> serviceBeanMap = new HashMap<>(); //服务名与对应的服务实例映射

    // 创建工作线程池，处理客户端调用请求
    int processorNum = Runtime.getRuntime().availableProcessors();
    private final ThreadPoolExecutor rpcWorkerThreadPool = new ThreadPoolExecutor(
            processorNum,
            processorNum * 2,
            60L, TimeUnit.SECONDS,
            new LinkedBlockingDeque<>());

    //服务注册中心
    private Registry serviceRegistry = null;

    public ServiceProviderCore(String providerAddress, String serviceRegistryAddress) {
        if (!StringUtil.checkAddress(providerAddress)) {
            logger.error("provider address format error");
            return;
        }
        //可能会连接Zookeeper集群
        String[] strings = serviceRegistryAddress.split(",");
        for (String s : strings) {
            if (!StringUtil.checkAddress(s)) {
                logger.error("Registry address format error");
                return;
            }
        }

        this.providerAddress = providerAddress;
        this.serviceRegistry = new Registry(serviceRegistryAddress);
    }

    public void addService(Class<?> serviceInterface, String serviceVersion, Object bean) {
        String s = StringUtil.makeServiceKey(serviceInterface.getName(), serviceVersion);
        logger.info(s + "," + bean + "  added to map");
        serviceBeanMap.put(s, bean);
    }

    /**
     * 异步启动服务器，将注册的服务写入到zookeeper，启动netty监听端口
     */
    public void start() {
        providerWorkThread = new Thread(() -> {
            String[] ipAndPort = providerAddress.split(":");
            String ip = ipAndPort[0];
            int port = Integer.parseInt(ipAndPort[1]);

            EventLoopGroup bossGroup = new NioEventLoopGroup(1);
            EventLoopGroup workerGroup = new NioEventLoopGroup();

            try {
                //启动Netty服务，监听端口
                ServerBootstrap serverBootstrap = new ServerBootstrap();
                serverBootstrap
                        .group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .childHandler(new ProviderChannelInitializer(serviceBeanMap, rpcWorkerThreadPool))
                        //.option(ChannelOption.SO_BACKLOG, 128)
                        .childOption(ChannelOption.SO_KEEPALIVE, true)
                        .childOption(ChannelOption.TCP_NODELAY, true);

                ChannelFuture future = serverBootstrap.bind(ip, port);
                future.sync(); //确保Netty启动成功后才向Zk写信息
                if(future.isSuccess()){
                    logger.info("服务器启动，监听 {} 端口", port);
                }else{
                    logger.error("服务器启动失败");
                    return;
                }

                //向zookeeper注册服务
                updateZookeeper();
                
                future.channel().closeFuture().sync();
            } catch (Exception e) {
                if (e instanceof InterruptedException) {
                    logger.info("服务器工作线程被中断，已停止服务");
                } else {
                    logger.error("服务器错误：", e);
                }
            } finally {
                try {
                    //删除zookeeper中的数据
                    serviceRegistry.clearAndCloseRegistry();
                    workerGroup.shutdownGracefully();
                    bossGroup.shutdownGracefully();
                    rpcWorkerThreadPool.shutdown();
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        });
        providerWorkThread.start();
    }

    /**
     * 向zk更新服务提供信息
     */
    public void updateZookeeper(){
        serviceRegistry.clearRegistry();
        serviceRegistry.serviceInfoToRegistry(providerAddress, serviceBeanMap.keySet());
    }

    /**
     * 停止服务器
     * 打断 providerWorkThread
     */
    @SuppressWarnings("unused")
    public void stop() {
        if (providerWorkThread != null && providerWorkThread.isAlive()) {
            providerWorkThread.interrupt(); //不能用stop停止线程！！！
        }
    }
}