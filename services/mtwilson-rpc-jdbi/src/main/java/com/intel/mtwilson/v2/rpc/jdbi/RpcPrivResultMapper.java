/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.v2.rpc.jdbi;

import java.sql.ResultSet;
import java.sql.SQLException;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.rpc.v2.model.Rpc;
import com.intel.mtwilson.rpc.v2.model.RpcPriv;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

/**
 * 
 * @author jbuhacoff
 */
public class RpcPrivResultMapper implements ResultSetMapper<RpcPriv> {

    @Override
    public RpcPriv map(int i, ResultSet rs, StatementContext sc) throws SQLException {
//        UUID uuid = UUID.valueOf(rs.getBytes("uuid")); // use this when uuid is a binary type in database
        UUID uuid = UUID.valueOf(rs.getString("ID")); // use this when uuid is a char type in database
        RpcPriv rpc = new RpcPriv();
        rpc.setId(uuid);
        rpc.setName(rs.getString("Name"));
        rpc.setInput(rs.getBytes("Input"));
        rpc.setOutput(rs.getBytes("Output"));
        rpc.setStatus(Rpc.Status.valueOf(rs.getString("Status")));
        rpc.setCurrent(rs.getLong("ProgressCurrent"));
        rpc.setMax(rs.getLong("ProgressMax"));
        return rpc;
    }
    
}
