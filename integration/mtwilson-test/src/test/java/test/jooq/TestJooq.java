/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package test.jooq;

import test.jdbi.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.My;
import com.intel.mtwilson.jdbi.util.QueryManager;
import com.intel.mtwilson.jdbi.util.UUIDArgument;
import com.intel.mtwilson.jooq.util.JooqContainer;
import com.intel.mtwilson.tag.dao.TagJdbi;
import com.intel.mtwilson.tag.dao.jdbi.FileDAO;
import java.io.IOException;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.jooq.DSLContext;
import org.junit.BeforeClass;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.Query;
import org.skife.jdbi.v2.Update;

/**
 *
 * @author jbuhacoff
 */
public class TestJooq {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TestJdbi.class);
    private static String driverName;
    private static QueryManager manager;
    private static ObjectMapper mapper = new ObjectMapper();

    /**
     * Output:
     *
     * <pre>
     * 2014-04-28 03:40:39,086 DEBUG [main] c.i.d.c.o.AbstractObjectPool [AbstractObjectPool.java:35] Creating new object for pool, currently borrowed 0
     * 2014-04-28 03:40:39,086 DEBUG [main] c.i.d.c.u.j.ConnectionPool [ConnectionPool.java:34] creating new object for pool, now created 1 trashed 0 total 1
     * 2014-04-28 03:40:40,100 DEBUG [main] c.i.d.c.u.j.PooledConnection [PooledConnection.java:28] [6324614990660108288] constructor wrapping jdbc:mysql://10.1.71.56:3306/mw_as?autoReconnect=true, UserName=root@10.254.186.37, MySQL-AB JDBC Driver
     * 2014-04-28 03:40:40,123 DEBUG [main] c.i.d.c.o.AbstractObjectPool [AbstractObjectPool.java:38] Borrowing object from pool: PooledConnection[6324614990660108288] wrapping jdbc:mysql://10.1.71.56:3306/mw_as?autoReconnect=true, UserName=root@10.254.186.37, MySQL-AB JDBC Driver / 874329857
     * 2014-04-28 03:40:40,361 DEBUG [main] t.j.TestJdbi [TestJooq.java:60] doing something with jooq
     * 2014-04-28 03:40:40,361 DEBUG [main] c.i.d.c.u.j.PooledConnection [PooledConnection.java:33] [6324614990660108288] close
     * 2014-04-28 03:40:40,384 DEBUG [main] c.i.d.c.o.AbstractObjectPool [AbstractObjectPool.java:66] Returning object to pool: PooledConnection[6324614990660108288] wrapping jdbc:mysql://10.1.71.56:3306/mw_as?autoReconnect=true, UserName=root@10.254.186.37, MySQL-AB JDBC Driver / 874329857
     * 2014-04-28 03:40:40,385 DEBUG [main] t.j.TestJdbi [TestJooq.java:62] done
     * </pre>
     *
     * @throws Exception
     */
    @Test
    public void testJooqContainerAutoClose() throws Exception {
        try (JooqContainer jc = TagJdbi.jooq()) {
            log.debug("doing something with jooq");
        }
        log.debug("done");
    }

    /**
     * Leak:
     *
     * <pre>
     * 2014-04-28 03:42:35,519 DEBUG [main] c.i.d.c.o.AbstractObjectPool [AbstractObjectPool.java:35] Creating new object for pool, currently borrowed 0
     * 2014-04-28 03:42:35,519 DEBUG [main] c.i.d.c.u.j.ConnectionPool [ConnectionPool.java:34] creating new object for pool, now created 1 trashed 0 total 1
     * 2014-04-28 03:42:40,257 DEBUG [main] c.i.d.c.u.j.PooledConnection [PooledConnection.java:28] [77483934750853120] constructor wrapping jdbc:mysql://10.1.71.56:3306/mw_as?autoReconnect=true, UserName=root@10.254.186.37, MySQL-AB JDBC Driver
     * 2014-04-28 03:42:40,281 DEBUG [main] c.i.d.c.o.AbstractObjectPool [AbstractObjectPool.java:38] Borrowing object from pool: PooledConnection[77483934750853120] wrapping jdbc:mysql://10.1.71.56:3306/mw_as?autoReconnect=true, UserName=root@10.254.186.37, MySQL-AB JDBC Driver / 594066573
     * 2014-04-28 03:42:40,471 DEBUG [main] t.j.TestJdbi [TestJooq.java:71] doing something with jooq
     * 2014-04-28 03:42:40,471 DEBUG [main] t.j.TestJdbi [TestJooq.java:74] done
     * </pre>
     *
     * @throws Exception
     */
    @Test
    public void testDslContextAutoClose() throws Exception {
        DSLContext dsl = TagJdbi.jooq().getDslContext();
        try {
            log.debug("doing something with jooq");
        } finally {
            log.debug("done"); // there's not even a close method in the dslcontext object
        }
    }
}
