package com.simple.rpc.registry;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import java.rmi.registry.Registry;
import java.util.List;

/**
 * 使用 Curator 连接 Zookeeper 的工具类
 * <p/>
 * 原生的 Zookeeper 有以下缺点：
 * 连接对象异步创建，需要开发人员自行编码等待。连接没有自动重连超时机制。watcher一次注册生效一次。不支持递归创建树形节点
 * <p/>
 * curator特点：
 * 解决session会话超时重连。watcher反复注册。简化开发api。提供了 事务、分布式锁、共享计数器、缓存等机制
 */
public class ZkUtil {
    //namespace作为zk中节点的顶级目录
    private final String ZK_NAME_SPACE="simple-rpc";
    //服务注册中心目录
    public final String REGISTRY_CENTER="/provider-information-center";
    //服务提供者的文件名
    public final String PROVIDER_FILE_NAME= REGISTRY_CENTER+"/provider-";

    private CuratorFramework client=null;

    public ZkUtil(String zookeeperAddress){
        /**
         *    RetryPolicy,重连策略
         *
         * 3秒后重连一次，只重连1次
         * RetryPolicy retryPolicy = new RetryOneTime(3000);
         *
         * 每3秒重连一次，重连3次
         * RetryPolicy retryPolicy = new RetryNTimes(3,3000)
         *
         * 每3秒重连一次，总等待时间超过10秒后停止重连
         * RetryPolicy retryPolicy=new RetryUntilElapsed(10000,3000);
         */
        //baseSleepTimeMs * Math.max(1, random.nextInt(1 << (retryCount + 1)))
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);

        client= CuratorFrameworkFactory.builder().connectString(zookeeperAddress)
                .sessionTimeoutMs(5000).connectionTimeoutMs(5000)
                .namespace(ZK_NAME_SPACE)
                .retryPolicy(retryPolicy)
                .build();
        client.start();
    }

    /**
     * 向指定路劲添加 临时有序节点
     * @param path 路劲
     * @param data 节点中的数据
     * @return 创建临时有序节点的路径
     */
    public String createNodeByEphemeral(String path,byte[]data) throws Exception {
        return client.create()
                .creatingParentsIfNeeded() //如果父节点不存在，则创建父节点
                .withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
                .forPath(path, data);
    }

    public void deleteNode(String path) throws Exception {
        client.delete().forPath(path);
    }

    public byte[] getNodeData(String path) throws Exception {
        return client.getData().forPath(path);
    }

    public List<String> getChildrenPath(String path) throws Exception {
        return client.getChildren().forPath(path);
    }

    public void close() {
        client.close();
    }

    /**
     * 添加连接状态监听器
     */
    public void addConnectionStateListener(ConnectionStateListener listener) {
        client.getConnectionStateListenable().addListener(listener);
    }

    /**
     * 添加子节点监听器，但不关心孙节点的变化
     * @param path
     * @param listener
     * @throws Exception
     */
    public void addChildrenListener(String path, PathChildrenCacheListener listener) throws Exception {
        // arg1:连接对象  arg2:监视的节点路径  arg3:事件中是否可以获取节点的数据
        PathChildrenCache pathChildrenCache = new PathChildrenCache(client, path, true);
        //BUILD_INITIAL_CACHE 代表使用同步的方式进行缓存初始化。
        pathChildrenCache.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);
        pathChildrenCache.getListenable().addListener(listener);
    }
}
