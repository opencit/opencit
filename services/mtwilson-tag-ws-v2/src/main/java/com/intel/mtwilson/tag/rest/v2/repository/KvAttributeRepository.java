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
import com.intel.mtwilson.repository.RepositoryCreateException;
import com.intel.mtwilson.repository.RepositoryDeleteException;
import com.intel.mtwilson.repository.RepositoryException;
import com.intel.mtwilson.repository.RepositoryRetrieveException;
import com.intel.mtwilson.repository.RepositorySearchException;
import com.intel.mtwilson.repository.RepositoryStoreConflictException;
import com.intel.mtwilson.repository.RepositoryStoreException;
import com.intel.mtwilson.tag.dao.TagJdbi;
import com.intel.mtwilson.tag.model.CertificateLocator;
import com.intel.mtwilson.tag.model.KvAttribute;
import com.intel.mtwilson.tag.model.KvAttributeCollection;
import com.intel.mtwilson.tag.model.KvAttributeFilterCriteria;
import com.intel.mtwilson.tag.model.KvAttributeLocator;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SelectQuery;

/**
 *
 * @author ssbangal
 */
public class KvAttributeRepository implements DocumentRepository<KvAttribute, KvAttributeCollection, KvAttributeFilterCriteria, KvAttributeLocator> {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(KvAttributeRepository.class);
    

    @Override
    @RequiresPermissions("tag_kv_attributes:search")     
    public KvAttributeCollection search(KvAttributeFilterCriteria criteria) {
        log.debug("KvAttribute:Search - Got request to search for the KvAttributes.");        
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
                obj.setValue(r.getValue(MW_TAG_KVATTRIBUTE.VALUE)); 
                objCollection.getKvAttributes().add(obj);
            }
            sql.close();
        } catch (Exception ex) {
            log.error("KvAttribute:Search - Error during attribute search.", ex);
            throw new RepositorySearchException(ex, criteria);
        }        
        log.debug("KvAttribute:Search - Returning back {} of results.", objCollection.getKvAttributes().size());                                
        return objCollection;
    }

    @Override
    @RequiresPermissions("tag_kv_attributes:retrieve")     
    public KvAttribute retrieve(KvAttributeLocator locator) {
        if (locator == null || locator.id == null ) { return null;}
        log.debug("KvAttribute:Retrieve - Got request to retrieve KvAttribute with id {}.", locator.id);                
        try(KvAttributeDAO dao = TagJdbi.kvAttributeDao()) {
            
            KvAttribute obj = dao.findById(locator.id);
            if (obj != null)
                return obj;
                                    
        } catch (Exception ex) {
            log.error("KvAttribute:Retrieve - Error during attribute update.", ex);
            throw new RepositoryRetrieveException(ex, locator);
        }        
        return null;
    }

    @Override
    @RequiresPermissions("tag_kv_attributes:store")     
    public void store(KvAttribute item) {
        log.debug("KvAttribute:Store - Got request to update KvAttribute with id {}.", item.getId().toString());        
        KvAttributeLocator locator = new KvAttributeLocator();
        locator.id = item.getId();
        
        try(KvAttributeDAO dao = TagJdbi.kvAttributeDao()) {
            
            KvAttribute obj = dao.findById(item.getId());
            // Allowing the user to only edit the value.
            if (obj != null) {
                dao.update(item.getId(), obj.getName(), item.getValue());
                log.debug("KvAttribute:Store - Updated the KvAttribute {} successfully.", item.getId().toString());                                
            } else {
                log.error("KvAttribute:Store - KvAttribute will not be updated since it does not exist.");
                throw new RepositoryStoreConflictException(locator);
            }                                    
        } catch (RepositoryException re) {
            throw re;            
        } catch (Exception ex) {
            log.error("KvAttribute:Store - Error during KvAttribute update.", ex);
            throw new RepositoryStoreException(ex, locator);
        }        
    }

    @Override
    @RequiresPermissions("tag_kv_attributes:create")     
    public void create(KvAttribute item) {
        log.debug("KvAttribute:Create - Got request to create a new KvAttribute {}.", item.getId().toString());
        CertificateLocator locator = new CertificateLocator();
        locator.id = item.getId();
        try(KvAttributeDAO dao = TagJdbi.kvAttributeDao()) {

            dao.insert(item.getId(), item.getName(), item.getValue());
                log.debug("KvAttribute:Create - Created the KvAttribute {} successfully.", item.getId().toString());
                                    
        } catch (RepositoryException re) {
            throw re;            
        } catch (Exception ex) {
            log.error("KvAttribute:Create - Error during KvAttribute creation.", ex);
            throw new RepositoryCreateException(ex, locator);
        }        
    }

    @Override
    @RequiresPermissions("tag_kv_attributes:delete")     
    public void delete(KvAttributeLocator locator) {
        if( locator == null || locator.id == null ) { return; }
        log.debug("KvAttribute:Delete - Got request to delete KvAttribute with id {}.", locator.id.toString());                
        try(KvAttributeDAO dao = TagJdbi.kvAttributeDao()) {
            
            dao.delete(locator.id);           
            
        } catch (Exception ex) {
            log.error("KvAttribute:Delete - Error during KvAttribute deletion.", ex);
            throw new RepositoryDeleteException(ex, locator);
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
        } catch(RepositoryException re) {
            throw re;
        } catch (Exception ex) {
            log.error("KvAttribute:Delete - Error during KvAttribute deletion.", ex);
            throw new RepositoryDeleteException(ex);
        }
    }
        
}
