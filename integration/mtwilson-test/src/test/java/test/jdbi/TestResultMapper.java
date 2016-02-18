/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package test.jdbi;

import java.sql.ResultSet;
import java.sql.SQLException;
import com.intel.mtwilson.jdbi.util.UUIDMapper;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

/**
 * 
 * @author jbuhacoff
 */
public class TestResultMapper implements ResultSetMapper<TestClass> {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TestResultMapper.class);
    private static final UUIDMapper uuidMapper = new UUIDMapper();
    
    @Override
    public TestClass map(int i, ResultSet rs, StatementContext sc) throws SQLException {
//        UUID uuid = UUID.valueOf(rs.getBytes("uuid")); // use this when uuid is a binary type in database
//        UUID uuid = UUID.valueOf(rs.getString("uuid")); // use this when uuid is a char type in database
//        Selection selection = new Selection(rs.getLong("id"), uuid);
        TestClass test = new TestClass();
//        test.setId(UUID.valueOf(rs.getBytes("uuid"))); 
        test.setId(uuidMapper.getUUID(rs, sc, "id"));
        test.setName(rs.getString("name"));
        test.setLength(rs.getLong("length"));
        test.setCreated(rs.getTimestamp("created")); // getTimestamp for date+time,  or getDate for just the date 
        test.setContent(rs.getBytes("content"));
        test.setFlag(rs.getBoolean("flag"));
        return test;
    }
    
}
