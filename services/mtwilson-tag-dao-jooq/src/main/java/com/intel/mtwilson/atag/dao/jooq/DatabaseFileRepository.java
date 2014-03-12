/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.atag.dao.jooq;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.atag.dao.Derby;
import static com.intel.mtwilson.atag.dao.jooq.generated.Tables.FILE;
import com.intel.mtwilson.atag.dao.jooq.generated.tables.records.FileRecord;
import com.intel.mtwilson.atag.model.File;
import java.sql.SQLException;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.jooq.SelectQuery;

/**
 * Draft of a file repository implementation using jooq. 
 * 
 * In comparison with JDBI and JPA, the only method which is not easily implemented here is to create a table.
 * JDBI and JPA both have the disadvantage that the sql statements are either in annotations or hard-coded another
 * way so so not portable across different
 * databases. Jooq also allows hard-coding a create statement by simply using the {@code execute(String)} method
 * in jooq, but this again is not portable across databases unless combined with a type of localization support 
 * where the specific
 * string comes from a database-specific file with the create statements for each table. 
 * 
 * It seems that the best use of Jooq assumes that the database already exists, and that in general the *Repository
 * interfaces should also each assume that its repository already exists ,and not include any repository-management
 * functions such as create table, drop table, etc. 
 * 
 * See also:
 * http://www.jooq.org/doc/3.0/manual/sql-execution/fetching/recordmapper/
 * 
 * http://blog.jooq.org/2013/08/06/use-modelmapper-and-jooq-to-regain-control-of-your-domain-model/
 * http://modelmapper.org/
 *
 * http://www.jooq.org/doc/3.0/manual/sql-execution/fetching/recordhandler/
 * 
 * @author jbuhacoff
 */
public class DatabaseFileRepository implements RecordMapper<FileRecord,File> {
    // XXX TODO this can be implementdd in a super class common to all jooq database*repository classes
    private DSLContext jooq = null;
    
    // XXX TODO this can be implementdd in a super class common to all jooq database*repository classes
    public void open() throws SQLException {
            jooq = Derby.jooq();        // tied to Derby and to a singleton connection... XXX need to normalize and make it closeable and borrow from connection pool
    }
    
    // XXX TODO this can be implementdd in a super class common to all jooq database*repository classes
    public void close() {
        // XXX TODO return the connection to the connection pool
    }
    
    /**
     * Maps a {@link FileRecord} instance to a {@link File} instance.
     * 
     * This was originally in a separate class called FileRecordMapper but since the DatabaseFileRepository is 
     * the only class that needs to make use of that mapper, and since this pattern will be repeated for every
     * table in the schema, it makes sense to include the mapping function here instead of multiplying the
     * number of classes needed for each table.
     * @param record
     * @return 
     */
    @Override
    public File map(FileRecord record) {
        File file = new File();
        file.setUuid(UUID.valueOf(record.getUuid()));
        file.setName(record.getName());
        file.setContentType(record.getContenttype());
        file.setContent(record.getContent());
        return file;
    }
    
    
    /**
     * XXX TODO can also use {@link http://www.jooq.org/doc/3.1/manual/sql-execution/fetching/pojos-with-recordmapper-provider/ RecordMapperProvider}
     * or {@link http://blog.jooq.org/2013/08/06/use-modelmapper-and-jooq-to-regain-control-of-your-domain-model/ ModelMapper} to the same thing
     * @param uuid of the file to find
     * @return the File object or null if it was not found
     */
    public File findByUuid(UUID uuid) {
        FileRecord record = jooq.select().from(FILE).where(FILE.UUID.equal(uuid.toString())).fetchOneInto(FileRecord.class);
        if( record == null ) { return null; }
        return map(record);
    }

    /**
     * 
     * @param name of the file to find
     * @return the File object or null if it was not found
     */
    public File findByName(String name) {
        FileRecord record = jooq.select().from(FILE).where(FILE.NAME.equal(name)).fetchOneInto(FileRecord.class);
        if( record == null ) { return null; }
        return map(record);
    }

    public int insert(File file) {
        int result = jooq.insertInto(FILE, FILE.UUID, FILE.NAME, FILE.CONTENTTYPE, FILE.CONTENT)
                .values(
                    file.getUuid().toString(), 
                    file.getName(), 
                    file.getContentType(), 
                    file.getContent())
                .execute();
        return result;
    }
    
    public int update(File file) {
        int result = jooq.update(FILE)
                .set(FILE.NAME, file.getName())
                .set(FILE.CONTENTTYPE, file.getContentType())
                .set(FILE.CONTENT, file.getContent())
                .where(FILE.UUID.equal(file.getUuid().toString()))
                .execute();
        return result;
    }
    
    public int delete(UUID uuid) {
        int deleted = jooq.delete(FILE).where(FILE.UUID.equal(uuid.toString())).execute();
        return deleted;
    }
}
