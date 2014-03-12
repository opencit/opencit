/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.rest.v2.repository;

import com.intel.mtwilson.tag.dao.jdbi.TpmPasswordDAO;
import com.intel.mtwilson.jersey.resource.SimpleRepository;
import com.intel.mtwilson.tag.dao.TagJdbi;
import com.intel.mtwilson.tag.model.TpmPassword;
import com.intel.mtwilson.tag.model.TpmPasswordCollection;
import com.intel.mtwilson.tag.model.TpmPasswordFilterCriteria;
import com.intel.mtwilson.tag.model.TpmPasswordLocator;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ssbangal
 */
public class TpmPasswordRepository extends ServerResource implements SimpleRepository<TpmPassword, TpmPasswordCollection, TpmPasswordFilterCriteria, TpmPasswordLocator> {

    private Logger log = LoggerFactory.getLogger(getClass().getName());

    @Override
    public TpmPasswordCollection search(TpmPasswordFilterCriteria criteria) {
        TpmPasswordCollection objCollection = new TpmPasswordCollection();
        TpmPasswordDAO dao = null;
        
        try {
            dao = TagJdbi.tpmPasswordDao();
            TpmPassword obj = dao.findById(criteria.id);
            if (obj != null){
                objCollection.getTpmPasswords().add(obj);
            } else {
                log.debug("tpm-password did not get any results back for {}.", criteria.id.toString());
                setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            }                
        } catch (ResourceException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during tpm password search.", ex);
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Please see the server log for more details.");
        } finally {
            if (dao != null)
                dao.close();
        }        
        return objCollection;
    }

    @Override
    public TpmPassword retrieve(TpmPasswordLocator locator) {
        if (locator == null || locator.id == null ) { return null;}
        try(TpmPasswordDAO dao = TagJdbi.tpmPasswordDao()) {
            
            TpmPassword obj = dao.findById(locator.id);
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
    public void store(TpmPassword item) {
        try(TpmPasswordDAO dao = TagJdbi.tpmPasswordDao()) {
            
            TpmPassword obj = dao.findById(item.getId());
            if (obj != null)
                dao.update(item.getId(), item.getPassword());
            else {
                throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Object with the specified id does not exist.");
            }
                                    
        } catch (ResourceException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during attribute update.", ex);
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Please see the server log for more details.");
        }        
    }

    @Override
    public void create(TpmPassword item) {
        
        try(TpmPasswordDAO dao = TagJdbi.tpmPasswordDao()) {
            
            TpmPassword obj = dao.findById(item.getId());
            if (obj == null){
                dao.insert(item.getId(), item.getPassword());
            } else {
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
    public void delete(TpmPasswordLocator locator) {
        if( locator == null || locator.id == null ) { return; }
        
        try(TpmPasswordDAO dao = TagJdbi.tpmPasswordDao()) {
            
            dao.delete(locator.id);
            
        } catch (ResourceException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during attribute deletion.", ex);
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Please see the server log for more details.");
        }        
    }
    
    @Override
    public void delete(TpmPasswordFilterCriteria criteria) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
        
}
