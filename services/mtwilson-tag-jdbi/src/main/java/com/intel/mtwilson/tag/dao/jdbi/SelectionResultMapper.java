/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tag.dao.jdbi;

import com.intel.mtwilson.tag.model.CertificateRequest;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.tag.model.Selection;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

/**
 * 
 * @author jbuhacoff
 */
public class SelectionResultMapper implements ResultSetMapper<Selection> {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SelectionResultMapper.class);
    
    @Override
    public Selection map(int i, ResultSet rs, StatementContext sc) throws SQLException {
        String driver = (String)sc.getAttribute("driver");
        log.debug("driver is {}", driver);
//        UUID uuid = UUID.valueOf(rs.getBytes("uuid")); // use this when uuid is a binary type in database
//        UUID uuid = UUID.valueOf(rs.getString("uuid")); // use this when uuid is a char type in database
//        Selection selection = new Selection(rs.getLong("id"), uuid);
        Selection selection = new Selection();
        selection.setId(UUID.valueOf(rs.getString("id")));
        selection.setName(rs.getString("name"));
        selection.setDescription(rs.getString("description"));
        return selection;
    }
    
}
