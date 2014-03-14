/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package test.jdbi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.My;
import com.intel.mtwilson.jdbi.util.QueryManager;
import com.intel.mtwilson.jdbi.util.UUIDArgument;
import com.intel.mtwilson.tag.dao.TagJdbi;
import com.intel.mtwilson.tag.dao.jdbi.SelectionDAO;
import com.intel.mtwilson.tag.dao.jdbi.SelectionResultMapper;
import com.intel.mtwilson.tag.model.Selection;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
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
public class TestJdbi {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TestJdbi.class);
    private static String driverName;
    private static QueryManager manager;
    private static ObjectMapper mapper = new ObjectMapper();
    
    @BeforeClass
    public static void initQueryManager() throws IOException {
        driverName = My.configuration().getDatabaseProtocol(); // "postgresql" or "mysql"
        manager = new QueryManager("/test-jdbi.properties", driverName);        
    }
    

    @Test
    public void testDBConnection() throws Exception {
        try (Connection connection = TagJdbi.getConnection()) {
            log.debug(connection.getCatalog());
        }
        
    }
    
    @Test
    public void testCreateTable() throws Exception {
        DBI dbi = new DBI(TagJdbi.getDataSource());
        dbi.define("driver", driverName); // driverName like "mysql" or "postgresql";  DBI makes such definitions available to every StatementContext... so we can get this in the mapper!
        Handle h = dbi.open();
//        h.execute(manager.getQuery("mw_tag_selection.create")); // works but table must not exist or you'll get error.  TODO check if table exists before executing.
        //h.execute("insert into something (id, name) values (?, ?)", 1, "Brian");
        h.execute(manager.getQuery("test_table.create"));
        h.close();
    }
    @Test
    public void testDropTable() throws Exception {
        DBI dbi = new DBI(TagJdbi.getDataSource());
        dbi.define("driver", driverName); // driverName like "mysql" or "postgresql";  DBI makes such definitions available to every StatementContext... so we can get this in the mapper!
        Handle h = dbi.open();
//        h.execute(manager.getQuery("mw_tag_selection.create")); // works but table must not exist or you'll get error.  TODO check if table exists before executing.
        //h.execute("insert into something (id, name) values (?, ?)", 1, "Brian");
        h.execute(manager.getQuery("test_table.drop"));
        h.close();
    }
    
    @Test
    public void testInsert() throws Exception {
        
        log.debug("insert sql for postgresql from manager: {}", manager.getQuery("test_table.insert"));
        
        DBI dbi = new DBI(TagJdbi.getDataSource());
        dbi.define("driver", driverName); // driverName like "mysql" or "postgresql";  DBI makes such definitions available to every StatementContext... so we can get this in the mapper!
        Handle h = dbi.open();
        h.registerArgumentFactory(new UUIDArgument());
//        h.execute(manager.getQuery("mw_tag_selection.create")); // works but table must not exist or you'll get error.  TODO check if table exists before executing.
        //h.execute("insert into something (id, name) values (?, ?)", 1, "Brian");
        Update query = h.createStatement(manager.getQuery("test_table.insert"));
        // postgresql:
//        query.bind("id", new UUID()); // org.skife.jdbi.v2.exceptions.UnableToCreateStatementException: Exception while binding 'id' [statement:"insert into test_table (id, name, length, created, content, flag) values (:id, :name, :length, :created, :content, :flag)", located:"insert into test_table (id, name, length, created, content, flag) values (:id, :name, :length, :created, :content, :flag)", rewritten:"insert into test_table (id, name, length, created, content, flag) values (?, ?, ?, ?, ?, ?)", arguments:{ positional:{}, named:{content:[0, 1, 2, 3],id:63efbf44-fd76-4816-afec-3147e203b7e6,flag:true,created:Thu Mar 13 11:50:21 PDT 2014,name:'test',length:0}, finder:[]}] // Caused by: org.postgresql.util.PSQLException: Can't infer the SQL type to use for an instance of com.intel.dcsg.cpg.io.UUID. Use setObject() with an explicit Types value to specify the type to use.
//        query.bind("id", new UUID().toByteArray().getBytes()); // org.skife.jdbi.v2.exceptions.UnableToExecuteStatementException: org.postgresql.util.PSQLException: ERROR: column "id" is of type uuid but expression is of type bytea
//        query.bind("id", new UUID().toString()); // org.skife.jdbi.v2.exceptions.UnableToExecuteStatementException: org.postgresql.util.PSQLException: ERROR: column "id" is of type uuid but expression is of type character varying
//        query.bind("id", new UUID().uuidValue()); // works on postgresql with uuid field
        // mysql:
//        query.bind("id", new UUID()); // org.skife.jdbi.v2.exceptions.UnableToCreateStatementException: Exception while binding 'id' [statement:"insert into test_table (id, name, length, created, content, flag) values (:id, :name, :length, :created, :content, :flag)", located:"insert into test_table (id, name, length, created, content, flag) values (:id, :name, :length, :created, :content, :flag)", rewritten:"insert into test_table (id, name, length, created, content, flag) values (?, ?, ?, ?, ?, ?)", arguments:{ positional:{}, named:{content:[0, 1, 2, 3],id:dc008d43-125d-494c-8033-58f0dfb97d43,flag:true,created:Thu Mar 13 12:11:38 PDT 2014,name:'test',length:0}, finder:[]}] // Caused by: java.io.NotSerializableException: com.intel.dcsg.cpg.io.UUID
//        query.bind("id", new UUID().toByteArray().getBytes()); // works on mysql  with binary(16) field
        query.bind("id", new UUID());
        // both:
        query.bind("name", "test5");
        query.bind("content", new byte[] { 0, 1, 2, 3 });
        query.bind("length", 0);
        query.bind("flag", true);
        query.bind("created", new Date());
        query.execute();
        h.close();
    }
    
    
    @Test
    public void testFindAll() throws Exception {
        DBI dbi = new DBI(TagJdbi.getDataSource());
        dbi.define("driver", driverName); // driverName like "mysql" or "postgresql";  DBI makes such definitions available to every StatementContext... so we can get this in the mapper!
        Handle h = dbi.open();
//        h.execute(manager.getQuery("mw_tag_selection.create")); // works but table must not exist or you'll get error.  TODO check if table exists before executing.
        //h.execute("insert into something (id, name) values (?, ?)", 1, "Brian");
        Query<Map<String,Object>> query = h.createQuery(manager.getQuery("test_table.findAll"));
        List<TestClass> selection = query.map(new TestResultMapper()).list();
            log.debug("selection: {}", mapper.writeValueAsString(selection));
        h.close();
    }

    @Test
    public void testFindByName() throws Exception {
        DBI dbi = new DBI(TagJdbi.getDataSource());
        dbi.define("driver", driverName); // driverName like "mysql" or "postgresql";  DBI makes such definitions available to every StatementContext... so we can get this in the mapper!
        Handle h = dbi.open();
//        h.execute(manager.getQuery("mw_tag_selection.create")); // works but table must not exist or you'll get error.  TODO check if table exists before executing.
        //h.execute("insert into something (id, name) values (?, ?)", 1, "Brian");
        Query<Map<String,Object>> query = h.createQuery(manager.getQuery("test_table.findByName"));
        query.bind("name", "test"); 
        List<TestClass> selection = query.map(new TestResultMapper()).list();
            log.debug("selection: {}", mapper.writeValueAsString(selection));
        h.close();
    }

}
