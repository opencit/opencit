/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.rest.v2.repository;

import com.intel.dcsg.cpg.io.UUID;
import static com.intel.mtwilson.atag.dao.jooq.generated.Tables.SELECTION;
import static com.intel.mtwilson.atag.dao.jooq.generated.Tables.SELECTION_TAG_VALUE;
import static com.intel.mtwilson.atag.dao.jooq.generated.Tables.TAG;
import static com.intel.mtwilson.atag.dao.jooq.generated.Tables.TAG_VALUE;
import com.intel.mtwilson.tag.dao.jdbi.KvAttributeDAO;
import com.intel.mtwilson.jersey.resource.SimpleRepository;
import com.intel.mtwilson.tag.dao.TagJdbi;
import com.intel.mtwilson.tag.dao.jdbi.SelectionDAO;
import com.intel.mtwilson.tag.dao.jdbi.SelectionKvAttributeDAO;
import com.intel.mtwilson.tag.model.Selection;
import com.intel.mtwilson.tag.model.SelectionCollection;
import com.intel.mtwilson.tag.model.SelectionFilterCriteria;
import com.intel.mtwilson.tag.model.SelectionLocator;
import com.intel.mtwilson.tag.model.SelectionKvAttribute;
import java.util.ArrayList;
import java.util.HashMap;
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
        // TODO need to implement the search after creating the new JOOQ tables.
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
            if (obj != null) {
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
            obj = dao.findByName(item.getName());
            if (obj != null) {
                throw new ResourceException(Status.CLIENT_ERROR_CONFLICT, "Object with the specified id already exists.");
            }
            
            dao.insert(item.getId(), item.getName(), item.getDescription());
                                    
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
                dao.delete(locator.id);
                                    
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
