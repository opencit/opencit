/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package test.jooq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.My;
import com.intel.mtwilson.jdbi.util.QueryManager;
import org.junit.Test;
import com.intel.mtwilson.tag.dao.TagJdbi;
import com.intel.mtwilson.tag.dao.jdbi.KvAttributeDAO;
import com.intel.mtwilson.tag.model.KvAttributeCollection;
import com.intel.mtwilson.tag.model.KvAttributeFilterCriteria;
import com.intel.mtwilson.tag.rest.v2.repository.KvAttributeRepository;
import java.io.IOException;
import org.junit.BeforeClass;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;

/**
 * Derby names all tables in all uppercase so when generating Jooq objects
 * from a Derby database they will not be usable with a case-sensitive database
 * unless Jooq is configured to ignore case or use all lowercase table names
 * (which is our naming convention).
 * 
 * @author jbuhacoff
 */
public class TestUUID {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TestUUID.class);
    
    private static String driverName;
    private static QueryManager manager;
    private static ObjectMapper mapper = new ObjectMapper();
    
    @BeforeClass
    public static void initQueryManager() throws IOException {
        driverName = My.configuration().getDatabaseProtocol(); // "postgresql" or "mysql"
        manager = new QueryManager("/test-jdbi.properties", driverName);        
    }

    @Test
    public void testCreateTable() throws Exception {
        DBI dbi = new DBI(TagJdbi.getDataSource());
        dbi.define("driver", "derby"/*driverName*/); // driverName like "mysql" or "postgresql";  DBI makes such definitions available to every StatementContext... so we can get this in the mapper!
        Handle h = dbi.open();
//        h.execute(manager.getQuery("mw_tag_selection.create")); // works but table must not exist or you'll get error.  TODO check if table exists before executing.
        //h.execute("insert into something (id, name) values (?, ?)", 1, "Brian");
        h.execute("drop table mw_tag_kvattribute");
        h.execute(manager.getQuery("mw_tag_kvattribute.create"));
        h.close();
    }
    
    /**
     * mysql:  java.lang.IllegalArgumentException: UUID must be 16 bytes; up to 4 hyphens allowed for standard UUID hex format
     * 
     * now it works when mysql has char(36) type and using UUIDObjectConverter and data has been inserted as 16 bytes instead of 36-char format
     * 
     * does not work when mysql has binary(16) type and jooq was generated with String (with and without using coerce to byte[] in the getValue call)  (uuid comes out wrong)
     * 
     * works when mysql has binary(16) and jooq was generated with String and using coerce to byte[] in the select statement (before the getValue call) and using the converter in getValue
     * 
     * works when postgresql has char(36) and jooq was generated with String and using coerce to byte[] in the select statement
     * 
     * works when postgresql has uuid and jooq was generated with String and using coerce to byte[] in the select statement
     */
    @Test
    public void testSearchUUID() throws Exception {
        KvAttributeFilterCriteria criteria = new KvAttributeFilterCriteria();
        criteria.nameEqualTo="test-name1";
        KvAttributeRepository repository = new KvAttributeRepository();
        KvAttributeCollection results = repository.search(criteria);
        log.debug("results: {}", mapper.writeValueAsString(results));
    }


    @Test
    public void testInsertUUID() throws Exception {
        KvAttributeDAO dao = TagJdbi.kvAttributeDao();
        UUID uuid = new UUID();
        log.debug("inserting uuid {}", uuid); // like mysql:  bd75944d-3929-44a5-b2c6-d4a13b5ee2a2    postgresql: 01791f57-748f-404a-b5db-cb3290de3b52
        dao.insert(uuid, "test-name1", "test-value1");
        dao.close();
    }

}
