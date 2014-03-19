/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.rest.v2.repository;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.jersey.resource.SimpleRepository;
import com.intel.mtwilson.tag.dao.TagJdbi;
import com.intel.mtwilson.tag.dao.jdbi.SelectionDAO;
import static com.intel.mtwilson.tag.dao.jooq.generated.Tables.MW_TAG_SELECTION;
import com.intel.mtwilson.tag.model.Selection;
import com.intel.mtwilson.tag.model.SelectionCollection;
import com.intel.mtwilson.tag.model.SelectionFilterCriteria;
import com.intel.mtwilson.tag.model.SelectionLocator;
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
public class SelectionRepository extends ServerResource implements SimpleRepository<Selection, SelectionCollection, SelectionFilterCriteria, SelectionLocator> {

    private Logger log = LoggerFactory.getLogger(getClass().getName());

    @Override
    public SelectionCollection search(SelectionFilterCriteria criteria) {
        SelectionCollection objCollection = new SelectionCollection();
        DSLContext jooq = null;
        
        try {
            jooq = TagJdbi.jooq();
            
            SelectQuery sql = jooq.select().from(MW_TAG_SELECTION).getQuery();
            if( criteria.id != null ) {
    //            sql.addConditions(TAG.UUID.equal(query.id.toByteArray().getBytes())); // when uuid is stored in database as binary
                sql.addConditions(MW_TAG_SELECTION.ID.equal(criteria.id.toString())); // when uuid is stored in database as the standard UUID string format (36 chars)
            }
            if( criteria.nameEqualTo != null  && criteria.nameEqualTo.length() > 0 ) {
                sql.addConditions(MW_TAG_SELECTION.NAME.equal(criteria.nameEqualTo));
            }
            if( criteria.nameContains != null  && criteria.nameContains.length() > 0 ) {
                sql.addConditions(MW_TAG_SELECTION.NAME.contains(criteria.nameContains));
            }
            if( criteria.descriptionEqualTo != null  && criteria.descriptionEqualTo.length() > 0 ) {
                sql.addConditions(MW_TAG_SELECTION.DESCRIPTION.equal(criteria.descriptionEqualTo));
            }
            if( criteria.descriptionContains != null  && criteria.descriptionContains.length() > 0 ) {
                sql.addConditions(MW_TAG_SELECTION.DESCRIPTION.contains(criteria.descriptionContains));
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
        } catch (ResourceException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during selection search.", ex);
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Please see the server log for more details.");
        }
        return objCollection;
    }

    @Override
    public Selection retrieve(SelectionLocator locator) {
        if (locator == null || locator.id == null ) { return null;}
        try(SelectionDAO dao = TagJdbi.selectionDao()) {
            
            Selection obj = dao.findById(locator.id);
            if (obj != null)
                return obj;
                                    
        } catch (ResourceException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during Selection search.", ex);
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Please see the server log for more details.");
        }        
        return null;
    }

    @Override
    public void store(Selection item) {

        try(SelectionDAO dao = TagJdbi.selectionDao()) {
            
            Selection obj = null;            
            obj = dao.findById(item.getId());
            
            if (obj == null) {
                throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Object with the specified id does not exist.");
            }
            
            dao.update(item.getId(), item.getDescription());
                                    
        } catch (ResourceException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during Selection update.", ex);
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Please see the server log for more details.");
        }        
    }

    @Override
    public void create(Selection item) {
        
        try(SelectionDAO dao = TagJdbi.selectionDao()) {
            Selection obj = null;
            obj = dao.findById(item.getId());
            if (obj != null) {
                throw new ResourceException(Status.CLIENT_ERROR_CONFLICT, "Object with the specified id already exists.");
            }
            
            if (item.getName() == null || item.getName().isEmpty()) {
                log.error("Invalid input specified by the user.");
                throw new ResourceException(Status.CLIENT_ERROR_PRECONDITION_FAILED, "Invalid input specified by the user.");                
            }
            
            obj = dao.findByName(item.getName());
            if (obj != null) {
                throw new ResourceException(Status.CLIENT_ERROR_CONFLICT, "Object with the specified name already exists.");
            }
            
//            dao.insert(item.getId().toString(), item.getName(), item.getDescription());
//            dao.insert(item.getId(), item.getName(), item.getDescription());
            dao.insert(item);
                                    
        } catch (ResourceException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during Selection creation.", ex);
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Please see the server log for more details.");
        }        
        
    }

    @Override
    public void delete(SelectionLocator locator) {
        if (locator == null || locator.id == null ) { return ;}
        
        try(SelectionDAO dao = TagJdbi.selectionDao()) {            
            Selection obj = dao.findById(locator.id);
            if (obj != null)
                dao.deleteById(locator.id);
                //dao.delete(locator.id.toString());
                                    
        } catch (ResourceException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during Selection deletion.", ex);
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Please see the server log for more details.");
        }        
    }
    
    @Override
    public void delete(SelectionFilterCriteria criteria) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
        
}
