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
import com.intel.mtwilson.tag.dao.jdbi.SelectionDAO;
import com.intel.mtwilson.tag.dao.TagJdbi;
import com.intel.mtwilson.tag.dao.jdbi.KvAttributeDAO;
import com.intel.mtwilson.tag.model.KvAttributeCollection;
import com.intel.mtwilson.tag.model.KvAttributeFilterCriteria;
import com.intel.mtwilson.tag.model.Selection;
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
        h.execute(manager.getQuery("mw_tag_kvattribute.create"));
        h.close();
    }
    
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
        dao.insert(new UUID(), "test-name1", "test-value1");
        dao.close();
    }

}
