/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package test.adapter;

import com.intel.mtwilson.rpc.v2.resource.RunnableRpcAdapter;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class RunnableAdapterTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RunnableAdapterTest.class);

    public static class IntegerAdder implements Runnable {
        Integer x, y, result;
        @Override
        public void run() {
            result = x + y;
        }
    }
    
    @Test
    public void testRunnable() {
        IntegerAdder rpc = new IntegerAdder();
        rpc.x = 1;
        rpc.y = 2;
        RunnableRpcAdapter adapter = new RunnableRpcAdapter(rpc);
        adapter.invoke();
        log.debug("result = {}", rpc.result);
    }
    
}
