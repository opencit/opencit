/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.repository;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.as.rest.v2.resource.*;
import com.intel.mtwilson.My;
import com.intel.mtwilson.as.controller.MwMleSourceJpaController;
import com.intel.mtwilson.as.data.MwMleSource;
import com.intel.mtwilson.as.rest.v2.model.MleSource;
import com.intel.mtwilson.as.rest.v2.model.MleSourceCollection;
import com.intel.mtwilson.as.rest.v2.model.MleSourceFilterCriteria;
import com.intel.mtwilson.as.rest.v2.model.MleSourceLocator;
import com.intel.mtwilson.jaxrs2.server.resource.DocumentRepository;
import com.intel.mtwilson.repository.RepositoryCreateException;
import com.intel.mtwilson.repository.RepositoryDeleteException;
import com.intel.mtwilson.repository.RepositoryException;
import com.intel.mtwilson.repository.RepositoryInvalidInputException;
import com.intel.mtwilson.repository.RepositoryRetrieveException;
import com.intel.mtwilson.repository.RepositorySearchException;
import com.intel.mtwilson.repository.RepositoryStoreException;
import com.intel.mtwilson.wlm.business.MleBO;
import org.apache.shiro.authz.annotation.RequiresPermissions;

/**
 *
 * @author ssbangal
 */
public class MleSourceRepository implements DocumentRepository<MleSource, MleSourceCollection, MleSourceFilterCriteria, MleSourceLocator>{
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MleSources.class);
    
    @Override
    @RequiresPermissions("mle_sources:search")    
    public MleSourceCollection search(MleSourceFilterCriteria criteria) {
        log.debug("MleSource:Search - Got request to search for the mle source mapping.");        
        MleSourceCollection mleSourceCollection = new MleSourceCollection();
        
        try {
            MwMleSourceJpaController jpaController = My.jpa().mwMleSource();
            if (criteria.mleUuid != null) {
                MwMleSource obj = jpaController.findByMleUuid(criteria.mleUuid.toString());            
                if (obj != null) {
                    mleSourceCollection.getMleSources().add(convert(obj));
                }
            } 
        } catch (Exception ex) {
            log.error("MleSource:Search - Error during MLE Source search.", ex);
            throw new RepositorySearchException(ex, criteria);
        }
        log.debug("MleSource:Search - Returning back {} of results.", mleSourceCollection.getMleSources().size());                
        return mleSourceCollection;
    }

    @Override
    @RequiresPermissions("mle_sources:retrieve")    
    public MleSource retrieve(MleSourceLocator locator) {
        if( locator.mleUuid == null ) { return null; }
        log.debug("MleSource:Retrieve - Got request to retrieve mle source mapping with id {}.", locator.id);                
        String mleUuid = locator.mleUuid.toString();
        try {
            MwMleSourceJpaController jpaController = My.jpa().mwMleSource();
            MwMleSource obj = jpaController.findByMleUuid(mleUuid);
            if (obj != null) {
                MleSource mleSource = convert(obj);
                return mleSource;            
            } 
        } catch (Exception ex) {
            log.error("MleSource:Retrieve - Error during MleSource retrieval.", ex);
            throw new RepositoryRetrieveException(ex, locator);
        }
        return null;
    }

    @Override
    @RequiresPermissions("mle_sources:store")    
    public void store(MleSource item) {
        if (item == null || item.getMleUuid() == null) { throw new RepositoryInvalidInputException();}
        log.debug("MleSource:Store - Got request to update the mle source mapping for MLE with id {}.", item.getMleUuid());        
        MleSourceLocator locator = new MleSourceLocator();
        locator.mleUuid = UUID.valueOf(item.getMleUuid());
        locator.id = item.getId();
        
        com.intel.mtwilson.datatypes.MleSource obj = new com.intel.mtwilson.datatypes.MleSource();
        try {
            obj.setHostName(item.getName());
            new MleBO().updateMleSource(obj, item.getMleUuid());
            log.debug("MleSource:Store - Successfully updated the mle source mapping for MLE with id {}.", item.getMleUuid());        
        } catch (Exception ex) {
            log.error("MleSource:Store - Error during MLE source host mapping update.", ex);
            throw new RepositoryStoreException(ex, locator);
        }
    }

    @Override
    @RequiresPermissions("mle_sources:create")    
    public void create(MleSource item) {
        log.debug("MleSource:Create - Got request to create a new mle source mapping.");
        MleSourceLocator locator = new MleSourceLocator();
        locator.mleUuid = UUID.valueOf(item.getMleUuid());
        locator.id = item.getId();

        com.intel.mtwilson.datatypes.MleSource obj = new com.intel.mtwilson.datatypes.MleSource();
        try {
            obj.setHostName(item.getName());
            obj.setMleData(null);
            new MleBO().addMleSource(obj, item.getId().toString(), item.getMleUuid());
            log.debug("MleSource:Create - Successfully created the mle source mapping.");
        } catch (Exception ex) {
            log.error("MleSource:Create - Error during MLE source host mapping creation.", ex);
            throw new RepositoryCreateException(ex, locator);
        }
    }

    @Override
    @RequiresPermissions("mle_sources:delete")    
    public void delete(MleSourceLocator locator) {
        if (locator == null || locator.mleUuid == null) { return; }
        log.debug("MleSource:Delete - Got request to delete Mle Source mapping for MLE with id {}.", locator.mleUuid.toString());        
        try {
            new MleBO().deleteMleSource(null, null, null, null, null, locator.mleUuid.toString());
            log.debug("MleSource:Delete - Deleted Mle Source mapping for MLE with id {}.", locator.mleUuid.toString());        
        } catch (Exception ex) {
            log.error("MleSource:Delete - Error during MLE source host mapping deletion.", ex);
            throw new RepositoryDeleteException(ex, locator);
        }
    }

    private MleSource convert(MwMleSource obj) {
        MleSource convObj = new MleSource();
        convObj.setMleUuid(obj.getMle_uuid_hex());
        convObj.setName(obj.getHostName());
        convObj.setId(UUID.valueOf(obj.getUuid_hex()));
        return convObj;
    }

    @Override
    @RequiresPermissions("mle_sources:delete,search")    
    public void delete(MleSourceFilterCriteria criteria) {
        log.debug("MleSource:Delete - Got request to delete MleSource by search criteria.");        
        MleSourceCollection objCollection = search(criteria);
        try { 
            for (MleSource obj : objCollection.getMleSources()) {
                MleSourceLocator locator = new MleSourceLocator();
                locator.mleUuid = UUID.valueOf(obj.getMleUuid());
                delete(locator);
            }
        } catch (RepositoryException re) {
            throw re;
        } catch (Exception ex) {
            log.error("MleSource:Delete - Error during MleSource deletion.", ex);
            throw new RepositoryDeleteException(ex);
        }
    }
    
}
