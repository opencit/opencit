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
import com.intel.mtwilson.repository.RepositoryStoreConflictException;
import com.intel.mtwilson.repository.RepositoryStoreException;
import com.intel.mtwilson.tag.dao.TagJdbi;
import com.intel.mtwilson.tag.dao.jdbi.SelectionDAO;
import static com.intel.mtwilson.tag.dao.jooq.generated.Tables.MW_TAG_SELECTION;
import com.intel.mtwilson.tag.model.Selection;
import com.intel.mtwilson.tag.model.SelectionCollection;
import com.intel.mtwilson.tag.model.SelectionFilterCriteria;
import com.intel.mtwilson.tag.model.SelectionKvAttribute;
import com.intel.mtwilson.tag.model.SelectionKvAttributeCollection;
import com.intel.mtwilson.tag.model.SelectionKvAttributeFilterCriteria;
import com.intel.mtwilson.tag.model.SelectionKvAttributeLocator;
import com.intel.mtwilson.tag.model.SelectionLocator;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SelectQuery;

/**
 *
 * @author ssbangal
 */
public class SelectionRepository implements DocumentRepository<Selection, SelectionCollection, SelectionFilterCriteria, SelectionLocator> {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SelectionRepository.class);
    

    @Override
    @RequiresPermissions("tag_selections:search")         
    public SelectionCollection search(SelectionFilterCriteria criteria) {
        log.debug("Selection:Search - Got request to search for the Selections.");        
        SelectionCollection objCollection = new SelectionCollection();
        try(JooqContainer jc = TagJdbi.jooq()) {
            DSLContext jooq = jc.getDslContext();
            
            SelectQuery sql = jooq.select().from(MW_TAG_SELECTION).getQuery();
            if (criteria.filter) {
                if( criteria.id != null ) {
                    sql.addConditions(MW_TAG_SELECTION.ID.equal(criteria.id.toString())); // when uuid is stored in database as the standard UUID string format (36 chars)
                }
                if( criteria.nameEqualTo != null  && criteria.nameEqualTo.length() > 0 ) {
                    sql.addConditions(MW_TAG_SELECTION.NAME.equalIgnoreCase(criteria.nameEqualTo));
                }
                if( criteria.nameContains != null  && criteria.nameContains.length() > 0 ) {
                    sql.addConditions(MW_TAG_SELECTION.NAME.lower().contains(criteria.nameContains.toLowerCase()));
                }
                if( criteria.descriptionEqualTo != null  && criteria.descriptionEqualTo.length() > 0 ) {
                    sql.addConditions(MW_TAG_SELECTION.DESCRIPTION.equalIgnoreCase(criteria.descriptionEqualTo));
                }
                if( criteria.descriptionContains != null  && criteria.descriptionContains.length() > 0 ) {
                    sql.addConditions(MW_TAG_SELECTION.DESCRIPTION.lower().contains(criteria.descriptionContains.toLowerCase()));
                }
            }
            sql.addOrderBy(MW_TAG_SELECTION.NAME);
            Result<Record> result = sql.fetch();
            log.debug("Got {} selection records", result.size());
            for(Record r : result) {
                Selection obj = new Selection();
                obj.setId(UUID.valueOf(r.getValue(MW_TAG_SELECTION.ID)));
                obj.setName(r.getValue(MW_TAG_SELECTION.NAME));
                obj.setDescription(r.getValue(MW_TAG_SELECTION.DESCRIPTION));
                
                objCollection.getSelections().add(obj);
            }
            sql.close();
        } catch (Exception ex) {
            log.error("Selection:Search - Error during selection search.", ex);
            throw new RepositorySearchException(ex, criteria);
        }
        log.debug("Selection:Search - Returning back {} of results.", objCollection.getSelections().size());                                
        return objCollection;
    }

    @Override
    @RequiresPermissions("tag_selections:retrieve")         
    public Selection retrieve(SelectionLocator locator) {
        if (locator == null || locator.id == null ) { return null;}
        log.debug("Selection:Retrieve - Got request to retrieve Selection with id {}.", locator.id);                
        try(SelectionDAO dao = TagJdbi.selectionDao()) {
            
            Selection obj = dao.findById(locator.id);
            if (obj != null)
                return obj;
                                    
        } catch (Exception ex) {
            log.error("Selection:Retrieve - Error during Selection retrieval.", ex);
            throw new RepositoryRetrieveException(ex, locator);
        }        
        return null;
    }

    @Override
    @RequiresPermissions("tag_selections:store")         
    public void store(Selection item) {
        log.debug("Selection:Store - Got request to update Selection with id {}.", item.getId().toString());        
        SelectionLocator locator = new SelectionLocator(); 
        locator.id = item.getId();

        try(SelectionDAO dao = TagJdbi.selectionDao()) {
            
            Selection obj = dao.findById(item.getId());           
            if (obj == null) {
                log.error("Selection:Store - Selection will not be updated since it does not exist.");
                throw new RepositoryStoreConflictException(locator);
            }
            
            dao.update(item.getId(), item.getDescription());
            log.debug("Selection:Store - Updated the Selection {} successfully.", item.getId().toString());                
                                    
        } catch (RepositoryException re) {
            throw re;            
        } catch (Exception ex) {
            log.error("Selection:Store - Error during Selection update.", ex);
            throw new RepositoryStoreException(ex, locator);
        }        
    }

    @Override
    @RequiresPermissions("tag_selections:create")         
    public void create(Selection item) {
        log.debug("Selection:Create - Got request to create a new Selection {}.", item.getId().toString());
        SelectionLocator locator = new SelectionLocator();
        locator.id = item.getId();        
        try(SelectionDAO dao = TagJdbi.selectionDao()) {
            Selection obj = dao.findById(item.getId());
            if (obj != null) {
                log.error("Selection:Create - Selection {} will not be created since a duplicate Selection already exists.", item.getId().toString());                
                throw new RepositoryCreateConflictException(locator);
            }
            
            if (item.getName() == null || item.getName().isEmpty()) {
                log.error("Selection:Create - Invalid input specified by the user.");
                throw new RepositoryInvalidInputException(locator);
            }
            
            obj = dao.findByName(item.getName());
            if (obj != null) {
                log.error("Selection:Create - Selection {} will not be created since a duplicate Selection already exists.", item.getName());                
                throw new RepositoryCreateConflictException(locator);
            }
            
            dao.insert(item);
                                    
        } catch (RepositoryException re) {
            throw re;            
        } catch (Exception ex) {
            log.error("Selection:Create - Error during Selection creation.", ex);
            throw new RepositoryCreateException(ex, locator);
        }        
    }

    @Override
    @RequiresPermissions("tag_selections:delete")         
    public void delete(SelectionLocator locator) {
        if (locator == null || locator.id == null ) { return ;}
        log.debug("Selection:Delete - Got request to delete Selection with id {}.", locator.id.toString());                        
        try(SelectionDAO dao = TagJdbi.selectionDao()) {            
            Selection obj = dao.findById(locator.id);
            if (obj != null) {
                // First we need to retrieve all the entries from the selection_kv_attribute table
                // pertaining to this selection and delete them first so that they don't become orphan
                // entries.
                SelectionKvAttributeRepository repo = new SelectionKvAttributeRepository();
                SelectionKvAttributeFilterCriteria fc = new SelectionKvAttributeFilterCriteria();
                fc.nameEqualTo = obj.getName();
                SelectionKvAttributeCollection coll = repo.search(fc);
                for (SelectionKvAttribute skvObj : coll.getSelectionKvAttributeValues()) {
                    SelectionKvAttributeLocator kvlocator = new SelectionKvAttributeLocator();
                    kvlocator.id = skvObj.getId();
                    repo.delete(kvlocator);
                }
                dao.deleteById(locator.id);
            } else {
                log.info("Selection:Delete - Selection does not exist in the system.");                
            }
        } catch (Exception ex) {
            log.error("Selection:Delete - Error during Selection deletion.", ex);
            throw new RepositoryDeleteException(ex, locator);
        }        
    }
    
    @Override
    @RequiresPermissions("tag_selections:delete,search")         
    public void delete(SelectionFilterCriteria criteria) {
        log.debug("Selection:Delete - Got request to delete Selection by search criteria.");        
        SelectionCollection objCollection = search(criteria);
        try { 
            for (Selection obj : objCollection.getSelections()) {
                SelectionLocator locator = new SelectionLocator();
                locator.id = obj.getId();
                delete(locator);
            }
        } catch(RepositoryException re) {
            throw re;
        } catch (Exception ex) {
            log.error("Selection:Delete - Error during Selection deletion.", ex);
            throw new RepositoryDeleteException(ex);
        }
    }
        
}
