/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.atag.resource;

import com.intel.mtwilson.atag.model.FileSearchCriteria;
import com.intel.mtwilson.atag.model.File;
import com.intel.mtwilson.atag.dao.jdbi.FileDAO;
import com.intel.mtwilson.atag.Derby;
import static com.intel.mtwilson.atag.dao.jooq.generated.Tables.*;
import com.intel.dcsg.cpg.io.UUID;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.SelectConditionStep;
import org.jooq.SelectJoinStep;
import org.jooq.SelectQuery;
import org.jooq.impl.DSL;
import static org.jooq.impl.DSL.*;
import org.restlet.data.Status;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * References: http://restlet.org/learn/guide/2.2/core/resource/
 *
 * @author jbuhacoff
 */
public class FileListResource extends ServerResource {

    private Logger log = LoggerFactory.getLogger(getClass());
    private FileDAO dao = null;
    private DSLContext jooq = null;
    
    @Override
    protected void doInit() throws ResourceException {
        super.doInit();
        try {
            dao = Derby.fileDao();
            jooq = Derby.jooq();
        } catch (SQLException e) {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Cannot open database", e);
        }
    }

    @Override
    protected void doRelease() throws ResourceException {
        if (dao != null) {
            dao.close();
        }
        super.doRelease();
    }

//    @Post("json:json")
    public File insertFile(File file) throws SQLException {
        file.setUuid(new UUID());
        long fileId = dao.insert(file.getUuid(), file.getName(), file.getContentType(), file.getContent());
        file.setId(fileId);
        return file;
    }

    /**
     * Note: must use the list wrapper class. If you try to accept File[] files as a parameter, you will get this
     * exception: com.fasterxml.jackson.databind.JsonMappingException: Can not deserialize instance of
     * com.intel.dcsg.cpg.afile.model.File out of START_ARRAY token
     *
     * java.lang.ClassCastException: java.util.LinkedHashMap cannot be cast to com.intel.dcsg.cpg.afile.model.File
     *
     * @param files
     * @return
     * @throws SQLException
     */
    @Post("json:json")
    public File[] insertFiles(File[] files) throws SQLException {
        for (int i = 0; i < files.length; i++) {
            files[i] = insertFile(files[i]);
        }
        return files;
    }

    /**
     * References:
     * http://www.jooq.org/doc/2.6/manual/sql-building/sql-statements/dsl-and-non-dsl/
     * http://comments.gmane.org/gmane.comp.java.restlet.devel/1115
     * http://blog.restlet.com/2006/11/15/reconsidering-content-negotiation/
     * http://www.jooq.org/doc/3.1/manual/sql-building/table-expressions/nested-selects/
     * 
     * Because file values are in a separate table, if the client wants to find files that
     * have specific values, we need to search for those values first. 
     * 
     * find file where file.id = file_value.fileId and file_value
     * 
     * @param query
     * @return
     * @throws SQLException 
     */
    @Get("json")
    public File[] search(/*FileSearchCriteria query*/) throws SQLException {
        FileSearchCriteria query = new FileSearchCriteria();
        query.id = getQuery().getFirstValue("id") == null || getQuery().getFirstValue("id").isEmpty() ? null : UUID.valueOf(getQuery().getFirstValue("id"));
        query.nameEqualTo = getQuery().getFirstValue("nameEqualTo");
        query.nameContains = getQuery().getFirstValue("nameContains");
        query.contentTypeEqualTo = getQuery().getFirstValue("contentTypeEqualTo");
        query.contentTypeStartsWith = getQuery().getFirstValue("contentTypeStartsWith");
        log.debug("Search: {}", getQuery().getQueryString());
        SelectQuery sql = jooq.select().from(FILE).getQuery();
        if( query.id != null ) {
//            sql.addConditions(TAG.UUID.equal(query.id.toByteArray().getBytes())); // when uuid is stored in database as binary
            sql.addConditions(FILE.UUID.equal(query.id.toString())); // when uuid is stored in database as the standard UUID string format (36 chars)
        }
        if( query.nameEqualTo != null && query.nameEqualTo.length() > 0 ) {
            sql.addConditions(FILE.NAME.equal(query.nameEqualTo));
        }
        if( query.nameContains != null && query.nameContains.length() > 0 ) {
            sql.addConditions(FILE.NAME.contains(query.nameContains));
        }
        if( query.contentTypeEqualTo != null && query.contentTypeEqualTo.length() > 0 ) {
            sql.addConditions(FILE.CONTENTTYPE.equal(query.contentTypeEqualTo));
        }
        if( query.contentTypeStartsWith != null && query.contentTypeStartsWith.length() > 0 ) {
            sql.addConditions(FILE.CONTENTTYPE.startsWith(query.contentTypeStartsWith));
        }
        Result<Record> result = sql.fetch();
        File[] files = new File[result.size()];
        log.debug("Got {} records", files.length);
        int i = 0;
        for(Record r : result) {
            files[i] = new File();
            files[i].setId(r.getValue(FILE.ID));
            files[i].setUuid(UUID.valueOf(r.getValue(FILE.UUID)));
            files[i].setName(r.getValue(FILE.NAME));
            files[i].setContentType(r.getValue(FILE.CONTENTTYPE));
            files[i].setContent(r.getValue(FILE.CONTENT));
            i++;
        }
        sql.close();
        return files;
    }
}
