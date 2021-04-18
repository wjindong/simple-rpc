package com.app.test.service;


public class FooService implements Foo {
    private HelloService helloService1;

    private HelloService helloService2;

    @Override
    public String say(String s) {
        return helloService1.hello(s);
    }
}
