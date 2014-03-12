/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.rest.v2.repository;

import com.intel.dcsg.cpg.io.UUID;
import static com.intel.mtwilson.tag.dao.jooq.generated.Tables.MW_TAG_KVATTRIBUTE;
import com.intel.mtwilson.tag.dao.jdbi.KvAttributeDAO;
import com.intel.mtwilson.jersey.resource.SimpleRepository;
import com.intel.mtwilson.tag.dao.TagJdbi;
import com.intel.mtwilson.tag.model.KvAttribute;
import com.intel.mtwilson.tag.model.KvAttributeCollection;
import com.intel.mtwilson.tag.model.KvAttributeFilterCriteria;
import com.intel.mtwilson.tag.model.KvAttributeLocator;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SelectQuery;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ssbangal
 */
public class KvAttributeRepository extends ServerResource implements SimpleRepository<KvAttribute, KvAttributeCollection, KvAttributeFilterCriteria, KvAttributeLocator> {

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
            log.debug("Opening tag-value dao");
            log.debug("Fetching records using JOOQ");
            Result<Record> result = sql.fetch();
            for(Record r : result) {
                KvAttribute obj = new KvAttribute();
                obj.setId(UUID.valueOf(r.getValue(MW_TAG_KVATTRIBUTE.ID)));
                obj.setName(r.getValue(MW_TAG_KVATTRIBUTE.NAME));
                obj.setValue(r.getValue(MW_TAG_KVATTRIBUTE.VALUE)); //TODO: Change these after creating new JOOQ tables.
            }
            sql.close();
            log.debug("Closing tag-value dao");
            log.debug("Returning {} tags", objCollection.getKvAttributes().size());
            //return tags;
        } catch (ResourceException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during attribute search.", ex);
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Please see the server log for more details.");
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
                                    
        } catch (ResourceException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during attribute update.", ex);
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Please see the server log for more details.");
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
                throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Object not found.");
            }
                                    
        } catch (ResourceException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during attribute update.", ex);
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Please see the server log for more details.");
        }        
    }

    @Override
    public void create(KvAttribute item) {

        try(KvAttributeDAO dao = TagJdbi.kvAttributeDao()) {

            KvAttribute obj = dao.findById(item.getId());
            // Allowing the user to add only if it does not exist.
            if (obj == null)
                dao.insert(item.getId(), item.getName(), item.getValue());
            else {
                throw new ResourceException(Status.CLIENT_ERROR_CONFLICT, "Object with specified id already exists.");
            }
                        
        } catch (ResourceException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during attribute creation.", ex);
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Please see the server log for more details.");
        }         
    }

    @Override
    public void delete(KvAttributeLocator locator) {
        if( locator == null || locator.id == null ) { return; }
        try(KvAttributeDAO dao = TagJdbi.kvAttributeDao()) {
            
            dao.delete(locator.id);           
            
        } catch (ResourceException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during attribute deletion.", ex);
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Please see the server log for more details.");
        }        
    }
    
    @Override
    public void delete(KvAttributeFilterCriteria criteria) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
        
}
