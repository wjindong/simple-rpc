package com.simple.rpc.client.async;

import com.simple.rpc.client.future.FutureResult;

public interface AsyncClient {
    FutureResult call(String methodName,Object... args) throws Exception;
}
