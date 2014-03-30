/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.rest.v2.repository;

import com.intel.dcsg.cpg.io.UUID;
import static com.intel.mtwilson.tag.dao.jooq.generated.Tables.MW_TAG_KVATTRIBUTE;
import com.intel.mtwilson.tag.dao.jdbi.KvAttributeDAO;
import com.intel.mtwilson.jersey.resource.SimpleRepository;
import com.intel.mtwilson.jooq.util.UUIDConverter;
import com.intel.mtwilson.tag.dao.TagJdbi;
import com.intel.mtwilson.tag.model.KvAttribute;
import com.intel.mtwilson.tag.model.KvAttributeCollection;
import com.intel.mtwilson.tag.model.KvAttributeFilterCriteria;
import com.intel.mtwilson.tag.model.KvAttributeLocator;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import org.jooq.Converter;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SelectQuery;
//import org.restlet.data.Status;
//import org.restlet.resource.ResourceException;
//import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ssbangal
 */
public class KvAttributeRepository implements SimpleRepository<KvAttribute, KvAttributeCollection, KvAttributeFilterCriteria, KvAttributeLocator> {

    private Logger log = LoggerFactory.getLogger(getClass().getName());

    @Override
    public KvAttributeCollection search(KvAttributeFilterCriteria criteria) {
        KvAttributeCollection objCollection = new KvAttributeCollection();
        KvAttributeDAO dao = null;
        DSLContext jooq = null;
        
        try {
            dao = TagJdbi.kvAttributeDao();
            jooq = TagJdbi.jooq();
            
             SelectQuery sql = jooq.select().from(MW_TAG_KVATTRIBUTE).getQuery();
//            SelectQuery sql = jooq.select(MW_TAG_KVATTRIBUTE.ID.coerce(byte[].class), MW_TAG_KVATTRIBUTE.NAME, MW_TAG_KVATTRIBUTE.VALUE).from(MW_TAG_KVATTRIBUTE).getQuery();
            if( criteria.id != null ) {
    //            sql.addConditions(TAG.UUID.equal(query.id.toByteArray().getBytes())); // when uuid is stored in database as binary
                sql.addConditions(MW_TAG_KVATTRIBUTE.ID.equal(criteria.id.toString())); // when uuid is stored in database as the standard UUID string format (36 chars)
            }
            if( criteria.nameEqualTo != null  && criteria.nameEqualTo.length() > 0 ) {
                sql.addConditions(MW_TAG_KVATTRIBUTE.NAME.equal(criteria.nameEqualTo));
            }
            if( criteria.nameContains != null  && criteria.nameContains.length() > 0 ) {
                sql.addConditions(MW_TAG_KVATTRIBUTE.NAME.contains(criteria.nameContains));
            }
            if( criteria.valueEqualTo != null  && criteria.valueEqualTo.length() > 0 ) {
                sql.addConditions(MW_TAG_KVATTRIBUTE.VALUE.equal(criteria.valueEqualTo));
            }
            if( criteria.valueContains != null  && criteria.valueContains.length() > 0 ) {
                sql.addConditions(MW_TAG_KVATTRIBUTE.VALUE.contains(criteria.valueContains));
            }
            sql.addOrderBy(MW_TAG_KVATTRIBUTE.NAME, MW_TAG_KVATTRIBUTE.VALUE);
            log.debug("Opening tag-value dao");
            log.debug("Fetching records using JOOQ");
            Result<Record> result = sql.fetch();
            
//            ConverterFactory converterFactory = new ConverterFactory();
//            Converter uuidConverter = converterFactory.getConverter();
//            UUIDStringConverter uuidConverter = new UUIDStringConverter();
//            UUIDConverter uuidConverter = new UUIDConverter();
//            log.debug("coercing uuid field to byte[]");
//            UUIDByteArrayConverter uuidConverter = new UUIDByteArrayConverter(); // can't use this one when generating with derby  char(36)
            
            for(Record r : result) {
                KvAttribute obj = new KvAttribute();
                obj.setId(UUID.valueOf(r.getValue(MW_TAG_KVATTRIBUTE.ID)));
//                obj.setId(r.getValue(MW_TAG_KVATTRIBUTE.ID.coerce(byte[].class), uuidConverter));
//                obj.setId(r.getValue(MW_TAG_KVATTRIBUTE.ID, uuidConverter));
//                obj.setId(r.getValue(MW_TAG_KVATTRIBUTE.ID));
                obj.setName(r.getValue(MW_TAG_KVATTRIBUTE.NAME));
                obj.setValue(r.getValue(MW_TAG_KVATTRIBUTE.VALUE)); //TODO: Change these after creating new JOOQ tables.
                objCollection.getKvAttributes().add(obj);
            }
            sql.close();
            log.debug("Closing tag-value dao");
            log.debug("Returning {} tags", objCollection.getKvAttributes().size());
            //return tags;
        } catch (WebApplicationException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during attribute search.", ex);
            throw new WebApplicationException("Please see the server log for more details.", Response.Status.INTERNAL_SERVER_ERROR);
        } finally {
            if (dao != null)
                dao.close();
        }        
        return objCollection;
    }

    @Override
    public KvAttribute retrieve(KvAttributeLocator locator) {
        if (locator == null || locator.id == null ) { return null;}
        try(KvAttributeDAO dao = TagJdbi.kvAttributeDao()) {
            
            KvAttribute obj = dao.findById(locator.id);
            if (obj != null)
                return obj;
                                    
        } catch (WebApplicationException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during attribute update.", ex);
            throw new WebApplicationException("Please see the server log for more details.", Response.Status.INTERNAL_SERVER_ERROR);
        }        
        return null;
    }

    @Override
    public void store(KvAttribute item) {
        
        try(KvAttributeDAO dao = TagJdbi.kvAttributeDao()) {
            
            KvAttribute obj = dao.findById(item.getId());
            // Allowing the user to only edit the value.
            if (obj != null)
                dao.update(item.getId(), obj.getName(), item.getValue());
            else {
                throw new WebApplicationException("Object not found.", Response.Status.NOT_FOUND);
            }
                                    
        } catch (WebApplicationException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during attribute update.", ex);
            throw new WebApplicationException("Please see the server log for more details.", Response.Status.INTERNAL_SERVER_ERROR);
        }        
    }

    @Override
    public void create(KvAttribute item) {

        try(KvAttributeDAO dao = TagJdbi.kvAttributeDao()) {

            dao.insert(item.getId(), item.getName(), item.getValue());
            
            /*KvAttribute obj = dao.findById(item.getId());
            // Allowing the user to add only if it does not exist.
            if (obj == null) {
                if (item.getName() == null || item.getName().isEmpty() || item.getValue() == null || item.getValue().isEmpty()) {
                    log.error("Invalid input specified by the user.");
                    throw new WebApplicationException("Invalid input specified by the user.", Response.Status.PRECONDITION_FAILED);
                }
                //TODO: Create the unique name value pair mapping in the DB.
                KvAttributeFilterCriteria fc = new KvAttributeFilterCriteria();
                fc.nameEqualTo = item.getName();
                fc.valueEqualTo = item.getValue();
                KvAttributeCollection objCollection = search(fc);
                if (objCollection.getKvAttributes().size() == 0)
                    dao.insert(item.getId(), item.getName(), item.getValue());   
                else {
                    log.error("The key value pair already exists.");
                    throw new WebApplicationException("The key value pair already exists.", Response.Status.CONFLICT);
                }
            } else {
                throw new WebApplicationException("Object with specified id already exists.", Response.Status.CONFLICT);
            } */
                        
        } catch (WebApplicationException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during attribute creation.", ex);
            throw new WebApplicationException("Please see the server log for more details.", Response.Status.INTERNAL_SERVER_ERROR);
        }         
    }

    @Override
    public void delete(KvAttributeLocator locator) {
        if( locator == null || locator.id == null ) { return; }
        try(KvAttributeDAO dao = TagJdbi.kvAttributeDao()) {
            // TODO: Catch the SQLException -- see how JDBI returns the # of rows affected.
            dao.delete(locator.id);           
            
        } catch (WebApplicationException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during attribute deletion.", ex);
            throw new WebApplicationException("Please see the server log for more details.", Response.Status.INTERNAL_SERVER_ERROR);
        }        
    }
    
    @Override
    public void delete(KvAttributeFilterCriteria criteria) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
        
}
