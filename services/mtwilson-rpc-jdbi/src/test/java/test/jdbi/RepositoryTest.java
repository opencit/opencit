/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package test.jdbi;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.rpc.v2.model.Rpc;
import java.sql.SQLException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.intel.mtwilson.v2.rpc.jdbi.*;

/**
 * References:
 * Validation queries: http://stackoverflow.com/questions/3668506/efficient-sql-test-query-or-validation-query-that-will-work-across-all-or-most
 * 
 * @author jbuhacoff
 */
public class RepositoryTest {
    private static Logger log = LoggerFactory.getLogger(RepositoryTest.class);

    
    @Test
    public void testRpc() throws SQLException {
        RpcDAO dao = MyJdbi.rpc();
        
        Rpc rpc = new Rpc();
        rpc.setId(new UUID());
        rpc.setName("test_dao");
        rpc.setStatus(Rpc.Status.QUEUE);
        
        dao.insert(rpc.getId(), rpc.getName(), null, null, rpc.getStatus().name(), null, null);

        Rpc existing = dao.findById(rpc.getId());
        log.debug("Found rpc id {} name {} status {}", existing.getId(), existing.getName(), existing.getStatus());
        
        dao.updateStatus(rpc.getId(), Rpc.Status.OUTPUT.name());
        existing = dao.findById(rpc.getId());
        log.debug("Updated status: {}", existing.getStatus());
        
        dao.delete(rpc.getId());
        
        dao.close();
    }
}
