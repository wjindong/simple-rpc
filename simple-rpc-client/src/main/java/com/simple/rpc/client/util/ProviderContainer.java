package com.simple.rpc.client.util;


import com.simple.rpc.client.balance.ProviderChoose;
import com.simple.rpc.client.balance.impl.ProviderChooseByLRU;
import com.simple.rpc.client.netty.ConsumerChannelHandler;
import com.simple.rpc.client.netty.ConsumerChannelInitializer;
import com.simple.rpc.registry.bean.ProviderInformation;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 管理所有与服务器建立的连接
 */
public class ProviderContainer {
    private static final Logger LOGGER= LoggerFactory.getLogger(ProviderContainer.class);

    //使用线程池异步连接服务器
    private static ThreadPoolExecutor connectionWorkerPool;

    //线程池中的所有线程公用，保证线程安全
    private Map<ProviderInformation, ConsumerChannelHandler> connected = null;
    private Set<ProviderInformation> connecting =null;

    //此客户端的所有连接复用一个NioEventLoopGroup
    private EventLoopGroup eventLoopGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors());

    //实现通知等待模式
    private ReentrantLock reentrantLock=new ReentrantLock();
    private Condition condition=reentrantLock.newCondition();

    //路由策略
    private ProviderChoose providerChoose=new ProviderChooseByLRU();


    // 防止关闭客户端后，还有线程在等待连接
    private boolean isRunning=false;

    //单例模式，每个客户端只能有一个connection center
    private ProviderContainer(){
        connectionWorkerPool=new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(),
                Runtime.getRuntime().availableProcessors()*2,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingDeque<>());

        connected = new ConcurrentHashMap<>();
        connecting =new CopyOnWriteArraySet<>();
        isRunning=true;
    }
    private static volatile ProviderContainer instance=null;
    public static ProviderContainer getInstance(){
        if(instance==null){
            synchronized (ProviderContainer.class){
                if(instance==null){
                    instance=new ProviderContainer();
                }
            }
        }
        return instance;
    }


    /**
     * 更新容器中已连接的服务器
     * @param providerList 新的服务提供者信息
     */
    public void updateConnectedProvider(List<ProviderInformation> providerList){
        Set<ProviderInformation> newInfo= new HashSet<>();
        for(ProviderInformation provider:providerList) newInfo.add(provider);
        LOGGER.info(newInfo.toString());

        //连接新的服务提供者
        for(ProviderInformation provider:newInfo){
            if(!connecting.contains(provider)){
                connectProvider(provider);
            }
        }

        //删除无效的信息，关闭对应通道
        for(ProviderInformation oldProvider:connecting){
            if(!newInfo.contains(oldProvider)){
                LOGGER.info("删除无效的连接：{}",oldProvider);
                //关闭通道，此时handler可能为空，应为连接还没建立成功
                ConsumerChannelHandler handler=connected.get(oldProvider);
                if(handler!=null){
                    handler.close();
                }
                //删除信息
                connecting.remove(oldProvider);
                connected.remove(oldProvider);
            }
        }
    }

    /**
     * 根据key选择一个provider
     * @param key 服务名，interface：version
     * @return 与这个provider对应连接的handler
     */
    public ConsumerChannelHandler getHandler(String key) throws Exception{
        //检查是否与至少一个provider建立了连接
        checkConnected(5,TimeUnit.SECONDS);

        //根据不同的策略选取provider
        ProviderInformation provider= providerChoose.getProvider(key,connected);

        ConsumerChannelHandler handler=connected.get(provider);
        if (handler != null) {
            return handler;
        } else {
            LOGGER.error("不能找到 {} 的provider",key);
            throw new Exception("不能找到 "+ key+" 的provider");
        }
    }

    /**
     * 如果还没连接任何服务器，此方法将阻塞，直到连接服务器后被唤醒
     * 此方法返回时，则至少与一台服务器建立了连接
     */
    private void checkConnected(long time,TimeUnit timeUnit){
        int connectedNum=connected.size();
        while(isRunning && connectedNum<=0){
            reentrantLock.lock();
            try{
                LOGGER.info("没有连接到任何服务器，等待中...");
                if(time==0)  condition.await();
                else{
                    condition.await(time,timeUnit);
                }
                connectedNum=connected.size(); //update
            } catch (InterruptedException e) {
                LOGGER.info("等待连接服务器时被中断");
            } finally {
                reentrantLock.unlock();
            }
        }
    }

    /**
     * 唤醒所有在condition上等待的线程,让他们能够重新竞争锁
     */
    private void notifyAllWaiter(){
        reentrantLock.lock();
        try{
            condition.signalAll();
        }finally {
            reentrantLock.unlock();
        }
    }

    /**
     * 连接到指定的 provider
     * @param providerInformation
     */
    private void connectProvider(ProviderInformation providerInformation){
        //若此provider没有提供任何服务，则不建立连接
        if(providerInformation.getServiceInfoList()==null || providerInformation.getServiceInfoList().isEmpty()){
            LOGGER.info("{}:{} 没有提供任何服务",providerInformation.getProviderHost(),providerInformation.getProviderPort());
            return;
        }

        connecting.add(providerInformation);

        connectionWorkerPool.execute(()->{
            Bootstrap bootstrap=new Bootstrap();
            bootstrap.group(eventLoopGroup)
                    .handler(new ConsumerChannelInitializer());
            ChannelFuture channelFuture=bootstrap.connect(providerInformation.getProviderHost(),providerInformation.getProviderPort());
            channelFuture.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if(channelFuture.isSuccess()){

                        ConsumerChannelHandler handler=channelFuture.channel().pipeline().get(ConsumerChannelHandler.class);

                        if(connecting.contains(providerInformation)){
                            //将 handler 存入 Map
                            connected.put(providerInformation,handler);
                            handler.setProvider(providerInformation);
                            //唤醒等待的线程
                            notifyAllWaiter();
                        }else{
                            //说明在连接的过程中，provider已经下线
                            LOGGER.info("连接建立成功，但 {} 已下线，释放连接!",providerInformation.getProviderHost()+":"+providerInformation.getProviderPort());
                            handler.close();
                        }
                    }else{
                        //连接失败则删除set中的信息
                        connecting.remove(providerInformation);
                        LOGGER.error("连接失败。address:{}:{}",providerInformation.getProviderHost(),providerInformation.getProviderPort());
                    }
                }
            });

        });
    }

    /**
     * provider对应的通道关闭时，要删除这里对应的信息
     */
    public void removeProvider(ProviderInformation provider){
        connecting.remove(provider);
        connected.remove(provider);
        LOGGER.info("已断开与服务器 {} 的连接",provider.getProviderHost()+":"+provider.getProviderPort());
    }

    /**
     * 关闭与所有provider的连接，停止服务
     */
    public void stop(){
        isRunning=false;

        for (ProviderInformation provider : connecting) {
            ConsumerChannelHandler handler = connected.get(provider);
            if (handler != null) {
                handler.close();
            }
            connecting.remove(provider);
            connected.remove(provider);
        }

        //唤醒正在等待可用服务的线程，停止等待
        notifyAllWaiter();
        connectionWorkerPool.shutdown();
        eventLoopGroup.shutdownGracefully();
    }
}