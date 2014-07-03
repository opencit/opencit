/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.rest.v2.repository;

import com.intel.dcsg.cpg.io.UUID;
import static com.intel.mtwilson.tag.dao.jooq.generated.Tables.MW_TAG_KVATTRIBUTE;
import com.intel.mtwilson.tag.dao.jdbi.KvAttributeDAO;
import com.intel.mtwilson.jaxrs2.server.resource.DocumentRepository;
import com.intel.mtwilson.jooq.util.JooqContainer;
import com.intel.mtwilson.tag.dao.TagJdbi;
import com.intel.mtwilson.tag.model.KvAttribute;
import com.intel.mtwilson.tag.model.KvAttributeCollection;
import com.intel.mtwilson.tag.model.KvAttributeFilterCriteria;
import com.intel.mtwilson.tag.model.KvAttributeLocator;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SelectQuery;
//import org.restlet.data.Status;
//import org.restlet.resource.ResourceException;
//import org.restlet.resource.ServerResource;

/**
 *
 * @author ssbangal
 */
public class KvAttributeRepository implements DocumentRepository<KvAttribute, KvAttributeCollection, KvAttributeFilterCriteria, KvAttributeLocator> {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(KvAttributeRepository.class);
    

    @Override
    @RequiresPermissions("tag_kv_attributes:search")     
    public KvAttributeCollection search(KvAttributeFilterCriteria criteria) {
        KvAttributeCollection objCollection = new KvAttributeCollection();
        try(JooqContainer jc = TagJdbi.jooq()) {
            DSLContext jooq = jc.getDslContext();
            
             SelectQuery sql = jooq.select().from(MW_TAG_KVATTRIBUTE).getQuery();
//            SelectQuery sql = jooq.select(MW_TAG_KVATTRIBUTE.ID.coerce(byte[].class), MW_TAG_KVATTRIBUTE.NAME, MW_TAG_KVATTRIBUTE.VALUE).from(MW_TAG_KVATTRIBUTE).getQuery();
             // We will process the filter criteria only if required. If the user has explicity set the filter to false, then we will return back
             // all the data.
             if (criteria.filter) {
                if( criteria.id != null ) {
                    sql.addConditions(MW_TAG_KVATTRIBUTE.ID.equalIgnoreCase(criteria.id.toString())); // when uuid is stored in database as the standard UUID string format (36 chars)
                }
                if( criteria.nameEqualTo != null  && criteria.nameEqualTo.length() > 0 ) {
                    sql.addConditions(MW_TAG_KVATTRIBUTE.NAME.equalIgnoreCase(criteria.nameEqualTo));
                }
                if( criteria.nameContains != null  && criteria.nameContains.length() > 0 ) {
                    sql.addConditions(MW_TAG_KVATTRIBUTE.NAME.lower().contains(criteria.nameContains.toLowerCase()));
                }
                if( criteria.valueEqualTo != null  && criteria.valueEqualTo.length() > 0 ) {
                    sql.addConditions(MW_TAG_KVATTRIBUTE.VALUE.equalIgnoreCase(criteria.valueEqualTo));
                }
                if( criteria.valueContains != null  && criteria.valueContains.length() > 0 ) {
                    sql.addConditions(MW_TAG_KVATTRIBUTE.VALUE.lower().contains(criteria.valueContains.toLowerCase()));
                }
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
        }        
        return objCollection;
    }

    @Override
    @RequiresPermissions("tag_kv_attributes:retrieve")     
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
    @RequiresPermissions("tag_kv_attributes:store")     
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
    @RequiresPermissions("tag_kv_attributes:create")     
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
    @RequiresPermissions("tag_kv_attributes:delete")     
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
    @RequiresPermissions("tag_kv_attributes:delete,search")     
    public void delete(KvAttributeFilterCriteria criteria) {
        log.debug("KvAttribute:Delete - Got request to delete KvAttribute by search criteria.");        
        KvAttributeCollection objCollection = search(criteria);
        try { 
            for (KvAttribute obj : objCollection.getKvAttributes()) {
                KvAttributeLocator locator = new KvAttributeLocator();
                locator.id = obj.getId();
                delete(locator);
            }
        } catch (Exception ex) {
            log.error("Error during KvAttribute deletion.", ex);
            throw new WebApplicationException("Please see the server log for more details.", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }
        
}
