package com.app.test.service;

public class MathImpl implements Math{

    @Override
    public double divide(Double a, Double b) throws Exception{
        if(b==0) throw new Exception("除数为零");
        System.out.println("计算中...");
        Thread.sleep(1000*60);
        return a/b;
    }
}
