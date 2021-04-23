package com.unit.client;

import com.app.test.service.HelloService;
import com.app.test.service.Math;
import com.app.test.service.Person;
import com.simple.rpc.client.ServiceConsumerCore;
import com.simple.rpc.client.async.AsyncClient;
import com.simple.rpc.client.future.FutureResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class AsyncClientTest {
    private static Logger logger= LoggerFactory.getLogger(AsyncClientTest.class);

    private static ServiceConsumerCore consumerCore=new ServiceConsumerCore("192.168.163.128");

    public static void main(String[] args) throws Exception {
        AsyncClient service= consumerCore.createAsyncService(Math.class,"1.0");
        //FutureResult futureResult= service.call("divide",5.0,0.0);

//        System.out.println("===");
//        System.out.println("===");
//        System.out.println(futureResult.get(100000,TimeUnit.SECONDS));
//        System.out.println(futureResult.getErrorMsg());
//        System.out.println("===");
//        System.out.println("===");

        doTest(10,10000);
        consumerCore.stop();
    }

    private static void doTest(int threadNum,int requestNum) throws InterruptedException {
        Thread[] threads = new Thread[threadNum];

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < threadNum; ++i) {
            threads[i] = new Thread(() -> {
                for (int i1 = 0; i1 < requestNum; i1++) {
                    try {
                        final AsyncClient client = consumerCore.createAsyncService(HelloService.class, "1.0");
                        FutureResult result = client.call("hello",new Person("wang","-123"));
                        result.get();
                        //logger.info(result.get().toString());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }
        long time = (System.currentTimeMillis() - startTime);
        String msg = String.format("异步调用耗时:%sms, req/s=%s", time, ((double) (requestNum * threadNum)) / time * 1000);
        System.out.println(msg);
    }

}
