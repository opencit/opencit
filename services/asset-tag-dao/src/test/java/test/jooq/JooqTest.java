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
import static org.junit.Assert.*;
import org.junit.Test;
/**
 * References:
 * http://www.jooq.org/doc/3.1/manual/getting-started/tutorials/jooq-in-7-steps/jooq-in-7-steps-step7/
 * 
 * How to use a mock database for junit testing:
 * http://blog.jooq.org/2013/02/20/easy-mocking-of-your-database/
 * 
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
        
        // create new record
        File file1 = new File();
        file1.setUuid(new UUID());
        file1.setName("test");
        file1.setContentType("text/plain");
        file1.setContent("foobar".getBytes("UTF-8"));
        
        // store record
        int insertResult = r.insert(file1);
        assertEquals(1, insertResult);
        
        // fetch record
        File file2 = r.findByUuid(file1.getUuid());
        assertEquals("test", file2.getName());
        assertEquals(file1.getUuid().toString(), file2.getUuid().toString());
        assertEquals(file1.getName(), file2.getName());
        assertEquals(file1.getContentType(), file2.getContentType());
        assertArrayEquals(file1.getContent(), file2.getContent());
        
        // update record
        file1.setName("test updated");
        int updateResult = r.update(file1);
        assertEquals(1, updateResult);
        file2 = r.findByName(file1.getName());
        assertEquals("test updated", file2.getName());
        assertEquals(file1.getUuid().toString(), file2.getUuid().toString());
        assertEquals(file1.getName(), file2.getName());
        assertEquals(file1.getContentType(), file2.getContentType());
        assertArrayEquals(file1.getContent(), file2.getContent());
        
        // delete record
        int deleteResult = r.delete(file1.getUuid());
        assertEquals(1, deleteResult);
        file2 = r.findByUuid(file1.getUuid());
        assertNull(file2);
        file2 = r.findByName(file1.getName());
        assertNull(file2);
        
        r.close();
    }

}
