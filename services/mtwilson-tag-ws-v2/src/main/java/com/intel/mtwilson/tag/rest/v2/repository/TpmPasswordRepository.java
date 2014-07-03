/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.rest.v2.repository;

import com.intel.mtwilson.tag.dao.jdbi.TpmPasswordDAO;
import com.intel.mtwilson.jaxrs2.server.resource.DocumentRepository;
import com.intel.mtwilson.tag.dao.TagJdbi;
import com.intel.mtwilson.tag.model.TpmPassword;
import com.intel.mtwilson.tag.model.TpmPasswordCollection;
import com.intel.mtwilson.tag.model.TpmPasswordFilterCriteria;
import com.intel.mtwilson.tag.model.TpmPasswordLocator;
import java.util.Date;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import org.apache.shiro.authz.annotation.RequiresPermissions;

/**
 *
 * @author ssbangal
 */
public class TpmPasswordRepository implements DocumentRepository<TpmPassword, TpmPasswordCollection, TpmPasswordFilterCriteria, TpmPasswordLocator> {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TpmPasswordRepository.class);
    

    @Override
    @RequiresPermissions("tpm_passwords:search")         
    public TpmPasswordCollection search(TpmPasswordFilterCriteria criteria) {
        TpmPasswordCollection objCollection = new TpmPasswordCollection();
        log.debug("TpmPassword:Search - Got request to search for the TpmPasswords.");  
        
        try(TpmPasswordDAO dao = TagJdbi.tpmPasswordDao()) {
            if (criteria.id == null) {
                log.error("Search criteria is not specified.");
                throw new WebApplicationException("Search criteria is not specified.", Response.Status.BAD_REQUEST);
            }
                
            TpmPassword obj = dao.findById(criteria.id);
            if (obj != null) {
                // TODO INSECURE: decrypt the password,  only provide the password if the user ALSO has tpm_passwords:retrieve permission
                obj.setPassword(null); // prevent giving out the password in search results... see TODO comment above
                objCollection.getTpmPasswords().add(obj);
            }

        } catch (WebApplicationException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during tpm password search.", ex);
            throw new WebApplicationException("Please see the server log for more details.", Response.Status.INTERNAL_SERVER_ERROR);
        }       
        return objCollection;
    }

    @Override
    @RequiresPermissions("tpm_passwords:retrieve")         
    public TpmPassword retrieve(TpmPasswordLocator locator) {
        if (locator == null || locator.id == null ) { return null;}
        log.debug("TpmPassword:Retrieve - Got request to retrieve TpmPassword with id {}.", locator.id);
        
        try(TpmPasswordDAO dao = TagJdbi.tpmPasswordDao()) {
            
            TpmPassword obj = dao.findById(locator.id);
            if (obj != null) {
                // TODO INSECURE: decrypt
                return obj;
            }
                                    
        } catch (WebApplicationException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during attribute update.", ex);
            throw new WebApplicationException("Please see the server log for more details.", Response.Status.INTERNAL_SERVER_ERROR);
        }        
        return null;
    }

    @Override
    @RequiresPermissions("tpm_passwords:store")         
    public void store(TpmPassword item) {
        log.debug("TpmPassword:Store - Got request to update TpmPassword with id {}.", item.getId().toString());        
        
        try(TpmPasswordDAO dao = TagJdbi.tpmPasswordDao()) {
            
            TpmPassword obj = dao.findById(item.getId());
            if (obj != null) {
                // TODO INSECURE: encrypt password
                Date modifiedOn = new Date();
                dao.update(item.getId(), item.getPassword(), modifiedOn);
                item.setModifiedOn(modifiedOn);
            }
            else {
                throw new WebApplicationException("Object with the specified id does not exist.", Response.Status.NOT_FOUND);
            }
                                    
        } catch (WebApplicationException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during attribute update.", ex);
            throw new WebApplicationException("Please see the server log for more details.", Response.Status.INTERNAL_SERVER_ERROR);
        }        
    }

    @Override
    @RequiresPermissions("tpm_passwords:create")         
    public void create(TpmPassword item) {
        log.debug("TpmPassword:Create - Got request to create a new TpmPassword.");
        try(TpmPasswordDAO dao = TagJdbi.tpmPasswordDao()) {
            
            TpmPassword obj = dao.findById(item.getId());
            if (obj == null){
                // TODO INSECURE: encrypt password
                Date modifiedOn = new Date();
                dao.insert(item.getId(), item.getPassword(), modifiedOn);
                item.setModifiedOn(modifiedOn);
            } else {
                throw new WebApplicationException("Object with specified id already exists.", Response.Status.CONFLICT);
            }
                        
        } catch (WebApplicationException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during attribute creation.", ex);
            throw new WebApplicationException("Please see the server log for more details.", Response.Status.INTERNAL_SERVER_ERROR);
        }        
    }

    @Override
    @RequiresPermissions("tpm_passwords:delete")         
    public void delete(TpmPasswordLocator locator) {
        if( locator == null || locator.id == null ) { return; }
        log.debug("TpmPassword:Delete - Got request to delete TpmPassword with id {}.", locator.id.toString());        
        try(TpmPasswordDAO dao = TagJdbi.tpmPasswordDao()) {
            
            dao.delete(locator.id);
            
        } catch (WebApplicationException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during attribute deletion.", ex);
            throw new WebApplicationException("Please see the server log for more details.", Response.Status.INTERNAL_SERVER_ERROR);
        }        
    }
    
    @Override
    @RequiresPermissions("tpm_passwords:delete,search")         
    public void delete(TpmPasswordFilterCriteria criteria) {
        log.debug("TpmPassword:Delete - Got request to delete TpmPassword by search criteria.");        
        TpmPasswordCollection objCollection = search(criteria);
        try { 
            for (TpmPassword obj : objCollection.getTpmPasswords()) {
                TpmPasswordLocator locator = new TpmPasswordLocator();
                locator.id = obj.getId();
                delete(locator);
            }
        } catch (Exception ex) {
            log.error("Error during TpmPassword deletion.", ex);
            throw new WebApplicationException("Please see the server log for more details.", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }
        
}
