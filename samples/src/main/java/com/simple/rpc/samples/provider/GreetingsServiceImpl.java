package com.simple.rpc.samples.provider;

import com.simple.rpc.samples.api.GreetingsService;

public class GreetingsServiceImpl implements GreetingsService {
    @Override
    public String sayHi(String name) {
        return "hi "+name+"from provider";
    }
}
