/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package test.adapter;

import com.intel.mtwilson.rpc.v2.resource.CallableRpcAdapter;
import java.util.concurrent.Callable;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class CallableAdapterTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CallableAdapterTest.class);

    public static class IntegerAdder implements Callable {
        Integer x, y;
        @Override
        public Integer call() {
            return x + y;
        }
    }
    
    @Test
    public void testCallable() {
        IntegerAdder rpc = new IntegerAdder();
        rpc.x = 1;
        rpc.y = 2;
        CallableRpcAdapter adapter = new CallableRpcAdapter(rpc);
        adapter.invoke();
        log.debug("result = {}", adapter.getOutput());
    }
}
