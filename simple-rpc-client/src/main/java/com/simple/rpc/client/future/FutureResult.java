package com.simple.rpc.client.future;

import com.simple.rpc.bean.Request;
import com.simple.rpc.bean.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

public class FutureResult implements Future<Object> {
    private static final Logger LOGGER= LoggerFactory.getLogger(FutureResult.class);

    private Request request=null;
    private Response response=null;

    private final Syn syn;

    public FutureResult(Request request){
        this.request=request;
        syn=new Syn();
    }

    @Override
    public boolean isDone() {
        return syn.isDone();
    }

    @Override
    public Object get() {
        syn.acquire(1);
        return response.getResult();
    }

    public void setResponse(Response response){
        this.response=response;
        syn.release(1);
    }

    /**
     * 指定等待时间，获取数据
     * @param timeout 最长等待时间
     * @param timeUnit 时间单位
     * @return 如果在指定的时间内获取结果成功，返回对应结果。 如果超时/返回结果中有异常信息 返回 null
     * @throws InterruptedException
     */
    @Override
    public Object get(long timeout, TimeUnit timeUnit) throws InterruptedException{
        boolean flag=syn.tryAcquireNanos(1,timeUnit.toNanos(timeout));
        if(flag){
            return get();
        }else{
            LOGGER.error("get 超时");
            return null;
        }
    }

    /**
     * 获取可能出现的异常信息
     */
    public String getErrorMsg(){
        //先要有返回值
        get();
        if(response!=null){
            return response.getThrowableMessage();
        }else{
            return null;
        }
    }

    public String getErrorMsg(long timeout, TimeUnit timeUnit) throws Exception{
        //先要有返回值
        get(timeout,timeUnit);
        if(response!=null){
            return response.getThrowableMessage();
        }else{
            return null;
        }
    }

    private static final class Syn extends AbstractQueuedSynchronizer{

        private final int done = 1;
        private final int undone = 0;

        @Override
        protected boolean tryAcquire(int arg) {
            return getState()==done;
        }

        @Override
        protected boolean tryRelease(int arg) {
            if(getState()== undone){
                if(compareAndSetState(undone,done)){
                    return true;
                }else{
                    return false;
                }
            }else return true;
        }

        private boolean isDone(){
            return getState()==done;
        }
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        LOGGER.error("调用了不支持的方法");
        throw new UnsupportedOperationException();
    }
    @Override
    public boolean isCancelled() {
        LOGGER.error("调用了不支持的方法");
        throw new UnsupportedOperationException();
    }
}
