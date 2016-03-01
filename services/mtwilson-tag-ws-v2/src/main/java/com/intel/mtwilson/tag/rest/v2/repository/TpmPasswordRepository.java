/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.rest.v2.repository;

import com.intel.mtwilson.tag.dao.jdbi.TpmPasswordDAO;
import com.intel.mtwilson.jaxrs2.server.resource.DocumentRepository;
import com.intel.mtwilson.repository.RepositoryCreateConflictException;
import com.intel.mtwilson.repository.RepositoryCreateException;
import com.intel.mtwilson.repository.RepositoryDeleteException;
import com.intel.mtwilson.repository.RepositoryException;
import com.intel.mtwilson.repository.RepositoryRetrieveException;
import com.intel.mtwilson.repository.RepositorySearchException;
import com.intel.mtwilson.repository.RepositoryStoreConflictException;
import com.intel.mtwilson.repository.RepositoryStoreException;
import com.intel.mtwilson.tag.dao.TagJdbi;
import com.intel.mtwilson.tag.model.TpmPassword;
import com.intel.mtwilson.tag.model.TpmPasswordCollection;
import com.intel.mtwilson.tag.model.TpmPasswordFilterCriteria;
import com.intel.mtwilson.tag.model.TpmPasswordLocator;
import com.intel.mtwilson.util.ASDataCipher;
import java.util.Date;
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
            if (criteria.id != null) {                
                TpmPassword obj = dao.findById(criteria.id);
                if (obj != null) {
                    obj.setPassword(null); 
                    objCollection.getTpmPasswords().add(obj);
                }
            }
        } catch (Exception ex) {
            log.error("TpmPassword:Search - Error during tpm password search.", ex);
            throw new RepositorySearchException(ex, criteria);
        }       
        log.debug("Certificate:Search - Returning back {} of results.", objCollection.getTpmPasswords().size());                                
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
                String cipherPassword = obj.getPassword();
                obj.setPassword(ASDataCipher.cipher.decryptString(cipherPassword));
                return obj;
            }
                                    
        } catch (Exception ex) {
            log.error("TpmPassword:Retrieve - Error during TpmPassword retrieval.", ex);
            throw new RepositoryRetrieveException(ex, locator);
        }        
        return null;
    }

    @Override
    @RequiresPermissions("tpm_passwords:store")         
    public void store(TpmPassword item) {
        log.debug("TpmPassword:Store - Got request to update TpmPassword with id {}.", item.getId().toString());        
        TpmPasswordLocator locator = new TpmPasswordLocator();
        locator.id = item.getId();        
        try(TpmPasswordDAO dao = TagJdbi.tpmPasswordDao()) {
            
            TpmPassword obj = dao.findById(item.getId());
            if (obj != null) {
                Date modifiedOn = new Date();
                dao.update(item.getId(), ASDataCipher.cipher.encryptString(item.getPassword()), modifiedOn);
                item.setModifiedOn(modifiedOn);
            }
            else {
                log.error("TpmPassword:Store - TpmPassword will not be updated since it does not exist.");
                throw new RepositoryStoreConflictException(locator);
            }
        } catch (RepositoryException re) {
            throw re;            
        } catch (Exception ex) {
            log.error("TpmPassword:Store - Error during TpmPassword update.", ex);
            throw new RepositoryStoreException(ex, locator);
        }        
    }

    @Override
    @RequiresPermissions("tpm_passwords:create")         
    public void create(TpmPassword item) {
        log.debug("TpmPassword:Create - Got request to create a new TpmPassword.");
        TpmPasswordLocator locator = new TpmPasswordLocator();
        locator.id = item.getId();
        
        try(TpmPasswordDAO dao = TagJdbi.tpmPasswordDao()) {
            
            TpmPassword obj = dao.findById(item.getId());
            if (obj == null){
                Date modifiedOn = new Date();
                dao.insert(item.getId(), ASDataCipher.cipher.encryptString(item.getPassword()), modifiedOn);
                item.setModifiedOn(modifiedOn);
                log.debug("TpmPassword:Create - Created the TpmPassword {} successfully.", item.getId().toString());
            } else {
                log.error("TpmPassword:Create - TpmPassword {} will not be created since a duplicate TpmPassword already exists.", item.getId().toString());                
                throw new RepositoryCreateConflictException(locator);
            }
        } catch (RepositoryException re) {
            throw re;            
        } catch (Exception ex) {
            log.error("TpmPassword:Create - Error during TpmPassword creation.", ex);
            throw new RepositoryCreateException(ex, locator);
        }        
    }

    @Override
    @RequiresPermissions("tpm_passwords:delete")         
    public void delete(TpmPasswordLocator locator) {
        if( locator == null || locator.id == null ) { return; }
        log.debug("TpmPassword:Delete - Got request to delete TpmPassword with id {}.", locator.id.toString());        
        try(TpmPasswordDAO dao = TagJdbi.tpmPasswordDao()) {
            TpmPassword obj = dao.findById(locator.id);
            if (obj == null){            
                dao.delete(locator.id);
                log.debug("TpmPassword:Delete - Deleted the TpmPassword {} successfully.", locator.id.toString());                
            }else {
                log.info("TpmPassword:Delete - TpmPassword does not exist in the system.");                
            }
        } catch (Exception ex) {
            log.error("TpmPassword:Delete - Error during TpmPassword deletion.", ex);
            throw new RepositoryDeleteException(ex, locator);
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
        } catch(RepositoryException re) {
            throw re;
        } catch (Exception ex) {
            log.error("TpmPassword:Delete - Error during TpmPassword deletion.", ex);
            throw new RepositoryDeleteException(ex);
        }
    }
        
}
