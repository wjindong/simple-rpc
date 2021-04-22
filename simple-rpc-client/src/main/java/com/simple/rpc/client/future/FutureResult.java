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

    @Override
    public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        throw new  UnsupportedOperationException();
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
