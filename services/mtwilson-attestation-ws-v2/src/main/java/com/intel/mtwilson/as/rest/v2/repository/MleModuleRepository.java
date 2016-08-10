/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.repository;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.My;
import com.intel.mtwilson.as.controller.TblModuleManifestJpaController;
import com.intel.mtwilson.as.data.TblModuleManifest;
import com.intel.mtwilson.as.rest.v2.model.MleModule;
import com.intel.mtwilson.as.rest.v2.model.MleModuleCollection;
import com.intel.mtwilson.as.rest.v2.model.MleModuleFilterCriteria;
import com.intel.mtwilson.as.rest.v2.model.MleModuleLocator;
import com.intel.mtwilson.datatypes.ModuleWhiteList;
import com.intel.mtwilson.jaxrs2.server.resource.DocumentRepository;
import com.intel.mtwilson.repository.RepositoryCreateException;
import com.intel.mtwilson.repository.RepositoryDeleteException;
import com.intel.mtwilson.repository.RepositoryException;
import com.intel.mtwilson.repository.RepositoryRetrieveException;
import com.intel.mtwilson.repository.RepositorySearchException;
import com.intel.mtwilson.repository.RepositoryStoreException;
import com.intel.mtwilson.wlm.business.MleBO;
import java.util.List;
import org.apache.shiro.authz.annotation.RequiresPermissions;

/**
 *
 * @author ssbangal
 */
public class MleModuleRepository implements DocumentRepository<MleModule, MleModuleCollection, MleModuleFilterCriteria, MleModuleLocator> {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MleModuleRepository.class);
    
    @Override
    @RequiresPermissions("mle_modules:search")    
    public MleModuleCollection search(MleModuleFilterCriteria criteria) {
        log.debug("MleModule:Search - Got request to search for the Mle modules.");
        MleModuleCollection objCollection = new MleModuleCollection();
        try {
            TblModuleManifestJpaController jpaController = My.jpa().mwModuleManifest();
            if (criteria.mleUuid != null) {
                List<TblModuleManifest> objList = jpaController.findTblModuleManifestByMleUuid(criteria.mleUuid.toString());
                if (objList != null && !objList.isEmpty()) {                    
                    // Before we add to the collection we need to check if the user has specified any other search criteria
                    if (criteria.filter == false) {
                        for(TblModuleManifest obj : objList) {
                            objCollection.getMleModules().add(convert(obj));
                        }                        
                    } else if (criteria.id != null) {
                        for(TblModuleManifest obj : objList) {
                            if (obj.getUuid_hex().contains(criteria.id.toString()))
                                objCollection.getMleModules().add(convert(obj));
                        }                        
                    } else if (criteria.nameContains != null && !criteria.nameContains.isEmpty()) {
                        for(TblModuleManifest obj : objList) {
                            if (obj.getComponentName().contains(criteria.nameContains))
                                objCollection.getMleModules().add(convert(obj));
                        }
                    } else if (criteria.valueEqualTo != null && !criteria.valueEqualTo.isEmpty()) {
                        for(TblModuleManifest obj : objList) {
                            if (obj.getDigestValue().equalsIgnoreCase(criteria.valueEqualTo))
                                objCollection.getMleModules().add(convert(obj));
                        }                        
                    } 
                }                
            }
        } catch (Exception ex) {
            log.error("MleModule:Search - Error during Module whitelist search.", ex);
            throw new RepositorySearchException(ex, criteria);
        }
        log.debug("MleModule:Search - Returning back {} of results.", objCollection.getMleModules().size());
        return objCollection;
    }

    @Override
    @RequiresPermissions("mle_modules:retrieve")    
    public MleModule retrieve(MleModuleLocator locator) {
        if( locator.id == null ) { return null; }
        log.debug("MleModule:Retrieve - Got request to retrieve Mle Module with id {}.", locator.id);                        
        try {
            TblModuleManifestJpaController jpaController = My.jpa().mwModuleManifest();
            TblModuleManifest obj = jpaController.findTblModuleManifestByUuid(locator.id.toString());
            if (obj != null) {
                MleModule convObj = convert(obj);
                return convObj;
            }
        } catch (Exception ex) {
            log.error("MleModule:Retrieve - Error during Module whitelist retrieval.", ex);
            throw new RepositoryRetrieveException(ex, locator);
        }
        return null;
    }

    @Override
    @RequiresPermissions("mle_modules:store")    
    public void store(MleModule item) {
        log.debug("MleModule:Store - Got request to update Mle module with id {}.", item.getId().toString());        
        MleModuleLocator locator = new MleModuleLocator();
        locator.mleUuid = UUID.valueOf(item.getMleUuid());
        locator.id = item.getId();
        
        ModuleWhiteList obj = new ModuleWhiteList();
        try {
            obj.setDigestValue(item.getModuleValue());
            obj.setDescription(item.getDescription());
            new MleBO().updateModuleWhiteList(obj, null, item.getId().toString());
            log.debug("MleModule:Store - Updated the MleModule with id {} successfully.", item.getId().toString()); 
            
        } catch (Exception ex) {
            log.error("MleModule:Store - Error during module whitelist update.", ex);
            throw new RepositoryStoreException(ex, locator);
        }        
    }

    @Override
    @RequiresPermissions("mle_modules:create")    
    public void create(MleModule item) {
        log.debug("MleModule:Create - Got request to create a new Mle module.");
        MleModuleLocator locator = new MleModuleLocator();
        locator.mleUuid = UUID.valueOf(item.getMleUuid());
        locator.id = item.getId();
        
        ModuleWhiteList obj = new ModuleWhiteList();
        try {
            obj.setComponentName(item.getModuleName());
            obj.setDigestValue(item.getModuleValue());
            obj.setDescription(item.getDescription());
            obj.setEventName(item.getEventName());
            obj.setExtendedToPCR(item.getExtendedToPCR());
            obj.setPackageName(item.getPackageName());
            obj.setPackageVendor(item.getPackageVendor());
            obj.setPackageVersion(item.getPackageVersion());
            obj.setUseHostSpecificDigest(item.getUseHostSpecificDigest());
            new MleBO().addModuleWhiteList(obj, null, item.getId().toString(), item.getMleUuid());
            log.debug("MleModule:Create - Created the MleModule {} successfully.", item.getModuleName()); 
        } catch (Exception ex) {
            log.error("MleModule:Create - Error during MleModule creation.", ex);
            throw new RepositoryCreateException(ex, locator);
        }
    }

    @Override
    @RequiresPermissions("mle_modules:delete")    
    public void delete(MleModuleLocator locator) {
        if (locator.id == null) { return; }
        log.debug("MleModule:Delete - Got request to delete Mle Module with id {}.", locator.id.toString());        
        try {
            new MleBO().deleteModuleWhiteList(null, null, null, null, null, null, null, null, locator.id.toString());
            log.debug("MleModule:Delete - Deleted the MleModule with id {} successfully.", locator.id.toString());            
        } catch (Exception ex) {
            log.error("MleModule:Delete - Error during module whitelist deletion.", ex);
            throw new RepositoryDeleteException(ex, locator);
        }        
    }
    
    private MleModule convert(TblModuleManifest obj) {
        MleModule convObj = new MleModule();
        convObj.setId(UUID.valueOf(obj.getUuid_hex()));
        convObj.setMleUuid(obj.getMle_uuid_hex());
        convObj.setModuleName(obj.getComponentName());
        convObj.setDescription(obj.getDescription());
        convObj.setModuleValue(obj.getDigestValue());
        convObj.setEventName(obj.getEventID().getName());
        convObj.setExtendedToPCR(obj.getExtendedToPCR());
        convObj.setPackageName(obj.getPackageName());
        convObj.setPackageVendor(obj.getPackageVendor());
        convObj.setPackageVersion(obj.getPackageVersion());
        return convObj;
    }

    @Override
    @RequiresPermissions("mle_modules:delete,search")    
    public void delete(MleModuleFilterCriteria criteria) {
        log.debug("MleModule:Delete - Got request to delete MleModule by search criteria.");        
        MleModuleCollection objCollection = search(criteria);
        try { 
            for (MleModule obj : objCollection.getMleModules()) {
                MleModuleLocator locator = new MleModuleLocator();
                locator.id = obj.getId();
                delete(locator);
            }
        } catch (RepositoryException re) {
            throw re;
        } catch (Exception ex) {
            log.error("MleModule:Delete - Error during module whitelist deletion.", ex);
            throw new RepositoryDeleteException(ex);
        }        
    }
    
}
