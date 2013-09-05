/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.atag.resource;

import com.intel.mtwilson.atag.model.ConfigurationSearchCriteria;
import com.intel.mtwilson.atag.model.Configuration;
import com.intel.mtwilson.atag.dao.jdbi.ConfigurationDAO;
import com.intel.mtwilson.atag.dao.Derby;
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
public class ConfigurationListResource extends ServerResource {

    private Logger log = LoggerFactory.getLogger(getClass());
    private ConfigurationDAO dao = null;
    private DSLContext jooq = null;
    
    @Override
    protected void doInit() throws ResourceException {
        super.doInit();
        try {
            dao = Derby.configurationDao();
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
    public Configuration insertConfiguration(Configuration configuration) throws SQLException {
        configuration.setUuid(new UUID());
        long configurationId = dao.insert(configuration.getUuid(), configuration.getName(), configuration.getContentType(), configuration.getContent());
        configuration.setId(configurationId);
        return configuration;
    }

    /**
     * Note: must use the list wrapper class. If you try to accept Configuration[] configurations as a parameter, you will get this
     * exception: com.fasterxml.jackson.databind.JsonMappingException: Can not deserialize instance of
     * com.intel.dcsg.cpg.aconfiguration.model.Configuration out of START_ARRAY token
     *
     * java.lang.ClassCastException: java.util.LinkedHashMap cannot be cast to com.intel.dcsg.cpg.aconfiguration.model.Configuration
     *
     * @param configurations
     * @return
     * @throws SQLException
     */
    @Post("json:json")
    public Configuration[] insertConfigurations(Configuration[] configurations) throws SQLException {
        for (int i = 0; i < configurations.length; i++) {
            configurations[i] = insertConfiguration(configurations[i]);
        }
        return configurations;
    }

    /**
     * References:
     * http://www.jooq.org/doc/2.6/manual/sql-building/sql-statements/dsl-and-non-dsl/
     * http://comments.gmane.org/gmane.comp.java.restlet.devel/1115
     * http://blog.restlet.com/2006/11/15/reconsidering-content-negotiation/
     * http://www.jooq.org/doc/3.1/manual/sql-building/table-expressions/nested-selects/
     * 
     * Because configuration values are in a separate table, if the client wants to find configurations that
     * have specific values, we need to search for those values first. 
     * 
     * find configuration where configuration.id = configuration_value.configurationId and configuration_value
     * 
     * @param query
     * @return
     * @throws SQLException 
     */
    @Get("json")
    public Configuration[] search(/*ConfigurationSearchCriteria query*/) throws SQLException {
        ConfigurationSearchCriteria query = new ConfigurationSearchCriteria();
        query.id = getQuery().getFirstValue("id") == null || getQuery().getFirstValue("id").isEmpty() ? null : UUID.valueOf(getQuery().getFirstValue("id"));
        query.nameEqualTo = getQuery().getFirstValue("nameEqualTo");
        query.nameContains = getQuery().getFirstValue("nameContains");
        query.contentTypeEqualTo = getQuery().getFirstValue("contentTypeEqualTo");
        log.debug("Search: {}", getQuery().getQueryString());
        SelectQuery sql = jooq.select().from(CONFIGURATION).getQuery();
        if( query.id != null ) {
//            sql.addConditions(TAG.UUID.equal(query.id.toByteArray().getBytes())); // when uuid is stored in database as binary
            sql.addConditions(CONFIGURATION.UUID.equal(query.id.toString())); // when uuid is stored in database as the standard UUID string format (36 chars)
        }
        if( query.nameEqualTo != null && query.nameEqualTo.length() > 0 ) {
            sql.addConditions(CONFIGURATION.NAME.equal(query.nameEqualTo));
        }
        if( query.nameContains != null && query.nameContains.length() > 0 ) {
            sql.addConditions(CONFIGURATION.NAME.contains(query.nameContains));
        }
        if( query.contentTypeEqualTo != null && query.contentTypeEqualTo.length() > 0 ) {
            sql.addConditions(CONFIGURATION.CONTENTTYPE.equal(query.contentTypeEqualTo));
        }
        Result<Record> result = sql.fetch();
        Configuration[] configurations = new Configuration[result.size()];
        log.debug("Got {} records", configurations.length);
        int i = 0;
        for(Record r : result) {
            configurations[i] = new Configuration();
            configurations[i].setId(r.getValue(CONFIGURATION.ID));
            configurations[i].setUuid(UUID.valueOf(r.getValue(CONFIGURATION.UUID)));
            configurations[i].setName(r.getValue(CONFIGURATION.NAME));
            configurations[i].setContent(r.getValue(CONFIGURATION.CONTENT));
            configurations[i].setContentType(Configuration.ContentType.valueOf(r.getValue(CONFIGURATION.CONTENTTYPE)));
            i++;
        }
        sql.close();
        return configurations;
    }
}
