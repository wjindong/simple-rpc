package com.unit.client;

import com.app.test.service.HelloService;
import com.simple.rpc.client.ServiceConsumerCore;

public class ClientTest {
    public static void main(String[] args) throws InterruptedException {
        ServiceConsumerCore consumerCore=new ServiceConsumerCore("192.168.163.128:2181");

        int threadNum = 10;
        final int requestNum = 100000;
        Thread[] threads = new Thread[threadNum];

        long startTime = System.currentTimeMillis();
        //benchmark for sync call
        for (int i = 0; i < threadNum; ++i) {
            threads[i] = new Thread(() -> {
                for (int i1 = 0; i1 < requestNum; i1++) {
                    try {
                        final HelloService syncClient = consumerCore.createService(HelloService.class, "1.0");
                        String result = syncClient.hello(Integer.toString(i1));
                        if (!result.equals("Hello " + i1)) {
                            System.err.println("error = " + result);
                            System.exit(0);
                        }
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

        Thread.sleep(1000*10);
        System.out.println("关闭client");
        consumerCore.stop();
    }
}
