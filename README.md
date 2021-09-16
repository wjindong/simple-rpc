# A simple RPC framework
RPC framework enables callers to call remote services as easily and transparently as local methods. This project is a demo of a RPC framework,uses Netty as the network communication component and Zookeeper as the service registry.

## Features
* Interface oriented transparent remote method call based on dynamic proxy
* The problem of TCP packet sticking and unpacking is solved, and the heartbeat detection between client and server is realized
* Support real-time perception of service instances online and offline
* Support synchronous call and asynchronous call

## Getting started
*See [ samples ](https://github.com/wjindong/simple-rpc/tree/master/samples) on GitHub.*
### Define service interfaces
```java
package com.simple.rpc.samples.api;

public interface GreetingsService {
    String sayHi(String name);
}
```
### Implement service interface for the provider
```java
package com.simple.rpc.samples.provider;

import com.simple.rpc.samples.api.GreetingsService;

public class GreetingsServiceImpl implements GreetingsService {
    @Override
    public String sayHi(String name) {
        return "hi "+name+"from provider";
    }
}
```
### Start service provider
```java
package com.simple.rpc.samples.provider;

import com.simple.rpc.samples.api.GreetingsService;
import com.simple.rpc.server.ServiceProviderCore;

public class Application {
    public static void main(String[] args) {
        String serverAddress = "127.0.0.1:18879";// provider address
        String registryAddress = "192.168.163.128:2181"; //Zookeeper address

        ServiceProviderCore provider = new ServiceProviderCore(serverAddress, registryAddress);
        provider.addService(GreetingsService.class,"1.0",new GreetingsServiceImpl());

        provider.start();
    }
}
```
### Call remote service in the consumer
```java
package com.simple.rpc.samples.client;
//import ...

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
```
