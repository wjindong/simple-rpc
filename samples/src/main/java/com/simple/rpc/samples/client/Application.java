package com.simple.rpc.samples.client;

import com.simple.rpc.client.ServiceConsumerCore;
import com.simple.rpc.client.async.AsyncClient;
import com.simple.rpc.client.future.FutureResult;
import com.simple.rpc.samples.api.GreetingsService;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class Application {
    private static ServiceConsumerCore client=new ServiceConsumerCore("192.168.163.128:2181");

    public static void main(String[] args) throws Exception {
        //Synchronous call
        GreetingsService service=client.createService(GreetingsService.class,"1.0");
        String res= service.sayHi("simple-rpc");
        System.out.println(res);

        //Asynchronous call
        AsyncClient asyncClient=client.createAsyncService(GreetingsService.class,"1.0");
        FutureResult result = asyncClient.call("sayHi","simple-rpc-asy ");

        res = (String) result.get(3, TimeUnit.SECONDS);
        String error=result.getErrorMsg(3, TimeUnit.SECONDS);
        //res = (String) result.get();
        //error=result.getErrorMsg();

        System.out.println(res);
        System.out.println(error);

        //性能测试
        //1. 单次调用延迟
        long startTime=System.currentTimeMillis();
        service.sayHi("test");
        long cost=System.currentTimeMillis()-startTime;
        System.out.println("单次调用延迟："+cost+"ms");

        //2. qps
        int synCallTimes=20000,threadNum=30;
        CountDownLatch latch=new CountDownLatch(threadNum);

        startTime=System.currentTimeMillis();

        for(int i=0;i<threadNum;i++){
            new Thread(()->{
                for(int j=0;j<synCallTimes;j++){
                    service.sayHi(String.valueOf(j));
                }
                latch.countDown();
            }).start();
        }
        latch.await();

        cost=System.currentTimeMillis()-startTime;
        System.out.println("qps: "+synCallTimes*threadNum/1.0/cost*1000.0);

        client.stop();
    }
}
