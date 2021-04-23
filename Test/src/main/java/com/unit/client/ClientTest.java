package com.unit.client;

import com.app.test.service.HelloService;
import com.app.test.service.Math;
import com.app.test.service.MathImpl;
import com.app.test.service.Person;
import com.simple.rpc.client.ServiceConsumerCore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientTest {
    private static Logger logger= LoggerFactory.getLogger(ClientTest.class);

    private static ServiceConsumerCore consumerCore=new ServiceConsumerCore("192.168.163.128:2181");
    public static void main(String[] args) throws InterruptedException {
        Math math=consumerCore.createService(Math.class,"1.0");

        try{
            double a = math.divide(5.0,0.0);
        }catch (Exception e){
            e.printStackTrace();
        }

        doTest(10,10000);
    }


    private static void doTest(int threadNum,int requestNum) throws InterruptedException {
        Thread[] threads = new Thread[threadNum];

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < threadNum; ++i) {
            threads[i] = new Thread(() -> {
                for (int i1 = 0; i1 < requestNum; i1++) {
                    try {
                        final HelloService syncClient = consumerCore.createService(HelloService.class, "1.0");
                        String result = syncClient.hello(new Person("wang","-123"));
                        //logger.info(result);
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
        String msg = String.format("同步调用耗时:%sms, req/s=%s", time, ((double) (requestNum * threadNum)) / time * 1000);
        System.out.println(msg);
    }
}
