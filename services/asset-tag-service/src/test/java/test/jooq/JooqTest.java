/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package test.jooq;
import com.intel.mtwilson.atag.dao.Derby;
import static com.intel.mtwilson.atag.dao.jooq.generated.Tables.*;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.atag.dao.jooq.DatabaseFileRepository;
import com.intel.mtwilson.atag.model.File;
import java.sql.SQLException;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import static org.jooq.impl.DSL.*;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * References:
 * http://www.jooq.org/doc/3.1/manual/getting-started/tutorials/jooq-in-7-steps/jooq-in-7-steps-step7/
 * @author jbuhacoff
 */
public class JooqTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JooqTest.class);
    
    @Test
    public void testJooqCodeGeneration() throws SQLException {
DSLContext create = DSL.using(Derby.getConnection(), SQLDialect.DERBY); // Note that the DSLContext doesn't close the connection. We'll have to do that ourselves.
Result<Record> result = create.select().from(TAG).fetch();  // TAG is a generated constant in com.intel...jooq.generated.Tables class; 

for (Record r : result) {
    Long id = r.getValue(TAG.ID);
    UUID uuid = UUID.valueOf(r.getValue(TAG.UUID));
    String name = r.getValue(TAG.NAME);
    String oid = r.getValue(TAG.OID);

    System.out.println("ID: " + id + " uuid: "+uuid+"  name: " + name + " oid: " + oid);
}
    }
    
    
    @Test
    public void testJooqDao() throws Exception {
        DatabaseFileRepository r = new DatabaseFileRepository();
        r.open();
        File file = r.findByUuid(new UUID()); // will not be found since we are generating a random uuid. will the result be null ?
        assertNull(file);
        r.close();
    }
}
