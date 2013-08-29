/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.atag.resource;

import com.intel.mtwilson.atag.model.RdfTripleSearchCriteria;
import com.intel.mtwilson.atag.model.RdfTriple;
import com.intel.mtwilson.atag.dao.jdbi.RdfTripleDAO;
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
public class RdfTripleListResource extends ServerResource {

    private Logger log = LoggerFactory.getLogger(getClass());
    private RdfTripleDAO dao = null;
    private DSLContext jooq = null;
    
    @Override
    protected void doInit() throws ResourceException {
        super.doInit();
        try {
            dao = Derby.rdfTripleDao();
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
    public RdfTriple insertRdfTriple(RdfTriple rdfTriple) throws SQLException {
        rdfTriple.setUuid(new UUID());
        long rdfTripleId = dao.insert(rdfTriple.getUuid(), rdfTriple.getSubject(), rdfTriple.getPredicate(), rdfTriple.getObject());
        rdfTriple.setId(rdfTripleId);
        return rdfTriple;
    }

    /**
     * Note: must use the list wrapper class. If you try to accept RdfTriple[] rdfTriples as a parameter, you will get this
     * exception: com.fasterxml.jackson.databind.JsonMappingException: Can not deserialize instance of
     * com.intel.dcsg.cpg.ardfTriple.model.RdfTriple out of START_ARRAY token
     *
     * java.lang.ClassCastException: java.util.LinkedHashMap cannot be cast to com.intel.dcsg.cpg.ardfTriple.model.RdfTriple
     *
     * @param rdfTriples
     * @return
     * @throws SQLException
     */
    @Post("json:json")
    public RdfTriple[] insertRdfTriples(RdfTriple[] rdfTriples) throws SQLException {
        for (int i = 0; i < rdfTriples.length; i++) {
            rdfTriples[i] = insertRdfTriple(rdfTriples[i]);
        }
        return rdfTriples;
    }

    /**
     * References:
     * http://www.jooq.org/doc/2.6/manual/sql-building/sql-statements/dsl-and-non-dsl/
     * http://comments.gmane.org/gmane.comp.java.restlet.devel/1115
     * http://blog.restlet.com/2006/11/15/reconsidering-content-negotiation/
     * http://www.jooq.org/doc/3.1/manual/sql-building/table-expressions/nested-selects/
     * 
     * Because rdfTriple values are in a separate table, if the client wants to find rdfTriples that
     * have specific values, we need to search for those values first. 
     * 
     * find rdfTriple where rdfTriple.id = rdfTriple_value.rdfTripleId and rdfTriple_value
     * 
     * @param query
     * @return
     * @throws SQLException 
     */
    @Get("json")
    public RdfTriple[] search(/*RdfTripleSearchCriteria query*/) throws SQLException {
        RdfTripleSearchCriteria query = new RdfTripleSearchCriteria();
        query.id = getQuery().getFirstValue("id") == null || getQuery().getFirstValue("id").isEmpty() ? null : UUID.valueOf(getQuery().getFirstValue("id"));
        query.subjectEqualTo = getQuery().getFirstValue("subjectEqualTo");
        query.predicateEqualTo = getQuery().getFirstValue("predicateEqualTo");
        query.objectEqualTo = getQuery().getFirstValue("objectEqualTo");
        log.debug("Search: {}", getQuery().getQueryString());
        SelectQuery sql = jooq.select().from(RDF_TRIPLE).getQuery();
        if( query.id != null ) {
//            sql.addConditions(TAG.UUID.equal(query.id.toByteArray().getBytes())); // when uuid is stored in database as binary
            sql.addConditions(RDF_TRIPLE.UUID.equal(query.id.toString())); // when uuid is stored in database as the standard UUID string format (36 chars)
        }
        if( query.subjectEqualTo != null && query.subjectEqualTo.length() > 0 ) {
            sql.addConditions(RDF_TRIPLE.SUBJECT.equal(query.subjectEqualTo));
        }
        if( query.predicateEqualTo != null && query.predicateEqualTo.length() > 0 ) {
            sql.addConditions(RDF_TRIPLE.PREDICATE.equal(query.predicateEqualTo));
        }
        if( query.objectEqualTo != null && query.objectEqualTo.length() > 0 ) {
            sql.addConditions(RDF_TRIPLE.OBJECT.equal(query.objectEqualTo));
        }
        if( query.subjectContains != null && query.subjectContains.length() > 0 ) {
            sql.addConditions(RDF_TRIPLE.SUBJECT.contains(query.subjectContains));
        }
        if( query.predicateContains != null && query.predicateContains.length() > 0 ) {
            sql.addConditions(RDF_TRIPLE.PREDICATE.contains(query.predicateContains));
        }
        if( query.objectContains != null && query.objectContains.length() > 0 ) {
            sql.addConditions(RDF_TRIPLE.OBJECT.contains(query.objectContains));
        }
        Result<Record> result = sql.fetch();
        RdfTriple[] rdfTriples = new RdfTriple[result.size()];
        log.debug("Got {} records", rdfTriples.length);
        int i = 0;
        for(Record r : result) {
            rdfTriples[i] = new RdfTriple();
            rdfTriples[i].setId(r.getValue(RDF_TRIPLE.ID));
            rdfTriples[i].setUuid(UUID.valueOf(r.getValue(RDF_TRIPLE.UUID)));
            rdfTriples[i].setSubject(r.getValue(RDF_TRIPLE.SUBJECT));
            rdfTriples[i].setPredicate(r.getValue(RDF_TRIPLE.PREDICATE));
            rdfTriples[i].setObject(r.getValue(RDF_TRIPLE.OBJECT));
            i++;
        }
        sql.close();
        return rdfTriples;
    }
}
