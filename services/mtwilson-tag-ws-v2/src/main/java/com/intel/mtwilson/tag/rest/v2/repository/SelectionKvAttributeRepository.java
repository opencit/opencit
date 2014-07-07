/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.rest.v2.repository;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.jaxrs2.server.resource.DocumentRepository;
import com.intel.mtwilson.jooq.util.JooqContainer;
import com.intel.mtwilson.repository.RepositoryCreateConflictException;
import com.intel.mtwilson.repository.RepositoryCreateException;
import com.intel.mtwilson.repository.RepositoryInvalidInputException;
import com.intel.mtwilson.repository.RepositoryDeleteException;
import com.intel.mtwilson.repository.RepositoryException;
import com.intel.mtwilson.repository.RepositoryRetrieveException;
import com.intel.mtwilson.repository.RepositorySearchException;
import com.intel.mtwilson.tag.dao.TagJdbi;
import com.intel.mtwilson.tag.dao.jdbi.KvAttributeDAO;
import com.intel.mtwilson.tag.dao.jdbi.SelectionDAO;
import com.intel.mtwilson.tag.dao.jdbi.SelectionKvAttributeDAO;
import com.intel.mtwilson.tag.model.SelectionKvAttribute;
import com.intel.mtwilson.tag.model.SelectionKvAttributeCollection;
import com.intel.mtwilson.tag.model.SelectionKvAttributeFilterCriteria;
import com.intel.mtwilson.tag.model.SelectionKvAttributeLocator;
import static com.intel.mtwilson.tag.dao.jooq.generated.Tables.MW_TAG_SELECTION;
import static com.intel.mtwilson.tag.dao.jooq.generated.Tables.MW_TAG_SELECTION_KVATTRIBUTE;
import static com.intel.mtwilson.tag.dao.jooq.generated.Tables.MW_TAG_KVATTRIBUTE;
import com.intel.mtwilson.tag.model.CertificateLocator;
import com.intel.mtwilson.tag.model.KvAttribute;
import com.intel.mtwilson.tag.model.Selection;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jooq.DSLContext;
import org.jooq.JoinType;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SelectQuery;

/**
 *
 * @author ssbangal
 */
public class SelectionKvAttributeRepository implements DocumentRepository<SelectionKvAttribute, SelectionKvAttributeCollection, SelectionKvAttributeFilterCriteria, SelectionKvAttributeLocator> {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SelectionKvAttributeRepository.class);
    

    @Override
    @RequiresPermissions("tag_selection_kv_attributes:search")         
    public SelectionKvAttributeCollection search(SelectionKvAttributeFilterCriteria criteria) {
        log.debug("SelectionKvAttribute:Search - Got request to search for the SelectionKvAttributes.");        
        SelectionKvAttributeCollection objCollection = new SelectionKvAttributeCollection();
        try(JooqContainer jc = TagJdbi.jooq()) {
            DSLContext jooq = jc.getDslContext();
            
            SelectQuery sql = jooq.select()
                    .from(MW_TAG_KVATTRIBUTE.join(MW_TAG_SELECTION_KVATTRIBUTE, JoinType.JOIN)
                    .on(MW_TAG_KVATTRIBUTE.ID.equal(MW_TAG_SELECTION_KVATTRIBUTE.KVATTRIBUTEID))
                    .join(MW_TAG_SELECTION, JoinType.JOIN).on(MW_TAG_SELECTION_KVATTRIBUTE.SELECTIONID.equal(MW_TAG_SELECTION.ID)))                    
                    .getQuery();
            if (criteria.filter) {
                if( criteria.attrNameEqualTo != null  && criteria.attrNameEqualTo.length() > 0 ) {
                    sql.addConditions(MW_TAG_KVATTRIBUTE.NAME.equalIgnoreCase(criteria.attrNameEqualTo));
                }
                if( criteria.attrNameContains != null  && criteria.attrNameContains.length() > 0 ) {
                    sql.addConditions(MW_TAG_KVATTRIBUTE.NAME.lower().contains(criteria.attrNameContains.toLowerCase()));
                }
                if( criteria.attrValueEqualTo != null  && criteria.attrValueEqualTo.length() > 0 ) {
                    sql.addConditions(MW_TAG_KVATTRIBUTE.VALUE.equalIgnoreCase(criteria.attrValueEqualTo));
                }
                if( criteria.attrValueContains != null  && criteria.attrValueContains.length() > 0 ) {
                    sql.addConditions(MW_TAG_KVATTRIBUTE.VALUE.lower().contains(criteria.attrValueContains.toLowerCase()));
                }
                if( criteria.id != null ) {
                    sql.addConditions(MW_TAG_SELECTION.ID.equalIgnoreCase(criteria.id.toString())); // when uuid is stored in database as the standard UUID string format (36 chars)
                }
                if( criteria.nameEqualTo != null  && criteria.nameEqualTo.length() > 0 ) {
                    sql.addConditions(MW_TAG_SELECTION.NAME.equalIgnoreCase(criteria.nameEqualTo));
                }
                if( criteria.nameContains != null  && criteria.nameContains.length() > 0  ) {
                    sql.addConditions(MW_TAG_SELECTION.NAME.lower().contains(criteria.nameContains.toLowerCase()));
                }
            }
            sql.addOrderBy(MW_TAG_SELECTION.NAME);
            Result<Record> result = sql.fetch();
            log.debug("Got {} selection records", result.size());
            for(Record r : result) {
                SelectionKvAttribute sAttr = new SelectionKvAttribute();
                sAttr.setId(UUID.valueOf(r.getValue(MW_TAG_SELECTION_KVATTRIBUTE.ID)));
                sAttr.setSelectionId(UUID.valueOf(r.getValue(MW_TAG_SELECTION.ID)));
                sAttr.setSelectionName(r.getValue(MW_TAG_SELECTION.NAME));
                sAttr.setKvAttributeId(UUID.valueOf(r.getValue(MW_TAG_KVATTRIBUTE.ID)));
                sAttr.setKvAttributeName(r.getValue(MW_TAG_KVATTRIBUTE.NAME));
                sAttr.setKvAttributeValue(r.getValue(MW_TAG_KVATTRIBUTE.VALUE));
                sAttr.setSelectionDescription(r.getValue(MW_TAG_SELECTION.DESCRIPTION));
                
                objCollection.getSelectionKvAttributeValues().add(sAttr);
            }
            sql.close();
        } catch (Exception ex) {
            log.error("Error during attribute search.", ex);
            throw new RepositorySearchException(ex, criteria);
        }
        log.debug("SelectionKvAttribute:Search - Returning back {} of results.", objCollection.getSelectionKvAttributeValues().size());                                
        return objCollection;
    }

    @Override
    @RequiresPermissions("tag_selection_kv_attributes:retrieve")         
    public SelectionKvAttribute retrieve(SelectionKvAttributeLocator locator) {
        if (locator == null || locator.id == null ) { return null;}
        log.debug("SelectionKvAttribute:Retrieve - Got request to retrieve SelectionKvAttribute with id {}.", locator.id);                
        try(SelectionKvAttributeDAO dao = TagJdbi.selectionKvAttributeDao()) {
            
            SelectionKvAttribute obj = dao.findById(locator.id);
            if (obj != null)
                return obj;
                                    
        } catch (Exception ex) {
            log.error("SelectionKvAttribute:Retrieve - Error during SelectionKvAttribute retrieval.", ex);
            throw new RepositoryRetrieveException(ex, locator);
        }        
        return null;
    }

    @Override
    @RequiresPermissions("tag_selection_kv_attributes:store")         
    public void store(SelectionKvAttribute item) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    @RequiresPermissions("tag_selection_kv_attributes:create")         
    public void create(SelectionKvAttribute item) {
        log.debug("SelectionKvAttribute:Create - Got request to create a new SelectionKvAttribute {}.", item.getId().toString());
        CertificateLocator locator = new CertificateLocator();
        locator.id = item.getId();
        try(SelectionKvAttributeDAO dao = TagJdbi.selectionKvAttributeDao();
                SelectionDAO selectionDao = TagJdbi.selectionDao();
                KvAttributeDAO attrDao = TagJdbi.kvAttributeDao()) {
            
            Selection selectionObj;
            SelectionKvAttribute obj = dao.findById(item.getId());
            if (obj == null) {
                if (item.getSelectionName() == null || item.getKvAttributeId() == null) {
                    log.error("SelectionKvAttribute:Create - Invalid input specified by the user.");
                    throw new RepositoryInvalidInputException(locator);
                }
                
                selectionObj = selectionDao.findByName(item.getSelectionName());
                if (selectionObj == null) {
                    log.error("SelectionKvAttribute:Create - Invalid input specified by the user. Specified selection is not configured.");
                    throw new RepositoryInvalidInputException(locator);
                }
                
                KvAttribute attrObj = attrDao.findById(item.getKvAttributeId());
                if (attrObj == null) {
                    log.error("SelectionKvAttribute:Create - Invalid input specified by the user. Specified attribute is not configured.");
                    throw new RepositoryInvalidInputException(locator);
                }
                dao.insert(item.getId(), selectionObj.getId(), item.getKvAttributeId());
                log.debug("SelectionKvAttribute:Create - Created the SelectionKvAttribute {} successfully.", item.getId().toString());
            } else {
                log.error("SelectionKvAttribute:Create - SelectionKvAttribute {} will not be created since a duplicate SelectionKvAttribute already exists.", item.getId().toString());                
                throw new RepositoryCreateConflictException(locator);
            }
                        
        } catch (RepositoryException re) {
            throw re;            
        } catch (Exception ex) {
            log.error("SelectionKvAttribute:Create - Error during SelectionKvAttribute creation.", ex);
            throw new RepositoryCreateException(ex, locator);
        }        
    }

    @Override
    @RequiresPermissions("tag_selection_kv_attributes:delete")         
    public void delete(SelectionKvAttributeLocator locator) {
        if (locator == null || locator.id == null) { return; }
        log.debug("SelectionKvAttribute:Delete - Got request to delete SelectionKvAttribute with id {}.", locator.id.toString());                        
        try(SelectionKvAttributeDAO dao = TagJdbi.selectionKvAttributeDao()) {
            SelectionKvAttribute obj = dao.findById(locator.id);
            if (obj != null) {
                dao.delete(locator.id);
                log.debug("SelectionKvAttribute:Delete - Deleted the SelectionKvAttribute {} successfully.", locator.id.toString());                                
            } else {
                log.info("SelectionKvAttribute:Delete - SelectionKvAttribute does not exist in the system.");                                
            }                        
        } catch (Exception ex) {
            log.error("SelectionKvAttribute:Delete - Error during SelectionKvAttribute deletion.", ex);
            throw new RepositoryDeleteException(ex, locator);            
        }         
    }
    
    @Override
    @RequiresPermissions("tag_selection_kv_attributes:delete,search")         
    public void delete(SelectionKvAttributeFilterCriteria criteria) {
        log.debug("SelectionKvAttribute:Delete - Got request to delete SelectionKvAttribute by search criteria.");        
        SelectionKvAttributeCollection objCollection = search(criteria);
        try { 
            for (SelectionKvAttribute obj : objCollection.getSelectionKvAttributeValues()) {
                SelectionKvAttributeLocator locator = new SelectionKvAttributeLocator();
                locator.id = obj.getId();
                delete(locator);
            }
        } catch(RepositoryException re) {
            throw re;
        } catch (Exception ex) {
            log.error("SelectionKvAttribute:Delete - Error during Certificate deletion.", ex);
            throw new RepositoryDeleteException(ex);
        }
    }
        
}
