/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.rest.v2.repository;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.jersey.resource.SimpleRepository;
import com.intel.mtwilson.jooq.util.JooqContainer;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ssbangal
 */
public class SelectionRepository implements SimpleRepository<Selection, SelectionCollection, SelectionFilterCriteria, SelectionLocator> {

    private Logger log = LoggerFactory.getLogger(getClass().getName());

    @Override
    @RequiresPermissions("tag_selections:search")         
    public SelectionCollection search(SelectionFilterCriteria criteria) {
        SelectionCollection objCollection = new SelectionCollection();
        try(JooqContainer jc = TagJdbi.jooq()) {
            DSLContext jooq = jc.getDslContext();
            
            SelectQuery sql = jooq.select().from(MW_TAG_SELECTION).getQuery();
            if( criteria.id != null ) {
    //            sql.addConditions(TAG.UUID.equal(query.id.toByteArray().getBytes())); // when uuid is stored in database as binary
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
            log.debug("Closed jooq sql statement");
        } catch (WebApplicationException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during selection search.", ex);
            throw new WebApplicationException("Please see the server log for more details.", Response.Status.INTERNAL_SERVER_ERROR);
        }
        return objCollection;
    }

    @Override
    @RequiresPermissions("tag_selections:retrieve")         
    public Selection retrieve(SelectionLocator locator) {
        if (locator == null || locator.id == null ) { return null;}
        try(SelectionDAO dao = TagJdbi.selectionDao()) {
            
            Selection obj = dao.findById(locator.id);
            if (obj != null)
                return obj;
                                    
        } catch (WebApplicationException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during Selection search.", ex);
            throw new WebApplicationException("Please see the server log for more details.", Response.Status.INTERNAL_SERVER_ERROR);
        }        
        return null;
    }

    @Override
    @RequiresPermissions("tag_selections:store")         
    public void store(Selection item) {

        try(SelectionDAO dao = TagJdbi.selectionDao()) {
            
            Selection obj = null;            
            obj = dao.findById(item.getId());
            
            if (obj == null) {
                throw new WebApplicationException("Object with the specified id does not exist.", Response.Status.NOT_FOUND);
            }
            
            dao.update(item.getId(), item.getDescription());
                                    
        } catch (WebApplicationException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during Selection update.", ex);
            throw new WebApplicationException("Please see the server log for more details.", Response.Status.INTERNAL_SERVER_ERROR);
        }        
    }

    @Override
    @RequiresPermissions("tag_selections:create")         
    public void create(Selection item) {
        
        try(SelectionDAO dao = TagJdbi.selectionDao()) {
            Selection obj = null;
            obj = dao.findById(item.getId());
            if (obj != null) {
                throw new WebApplicationException("Object with the specified id already exists.", Response.Status.CONFLICT);
            }
            
            if (item.getName() == null || item.getName().isEmpty()) {
                log.error("Invalid input specified by the user.");
                throw new WebApplicationException("Invalid input specified by the user.", Response.Status.PRECONDITION_FAILED);                
            }
            
            obj = dao.findByName(item.getName());
            if (obj != null) {
                throw new WebApplicationException("Object with the specified name already exists.", Response.Status.CONFLICT);
            }
            
//            dao.insert(item.getId().toString(), item.getName(), item.getDescription());
//            dao.insert(item.getId(), item.getName(), item.getDescription());
            dao.insert(item);
                                    
        } catch (WebApplicationException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during Selection creation.", ex);
            throw new WebApplicationException("Please see the server log for more details.", Response.Status.INTERNAL_SERVER_ERROR);
        }        
        
    }

    @Override
    @RequiresPermissions("tag_selections:delete")         
    public void delete(SelectionLocator locator) {
        if (locator == null || locator.id == null ) { return ;}
        
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
            }
                                    
        } catch (WebApplicationException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during Selection deletion.", ex);
            throw new WebApplicationException("Please see the server log for more details.", Response.Status.INTERNAL_SERVER_ERROR);
        }        
    }
    
    @Override
    @RequiresPermissions("tag_selections:delete,search")         
    public void delete(SelectionFilterCriteria criteria) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
        
}
