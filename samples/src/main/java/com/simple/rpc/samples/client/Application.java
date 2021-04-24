package com.simple.rpc.samples.client;

import com.simple.rpc.client.ServiceConsumerCore;
import com.simple.rpc.client.async.AsyncClient;
import com.simple.rpc.client.future.FutureResult;
import com.simple.rpc.samples.api.GreetingsService;

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
        FutureResult result = asyncClient.call("sayHi","simple-rpc");

        res = (String) result.get(3, TimeUnit.SECONDS);
        String error=result.getErrorMsg(3, TimeUnit.SECONDS);
        //res = (String) result.get();
        //error=result.getErrorMsg();

        System.out.println(res);
        System.out.println(error);
    }
}
