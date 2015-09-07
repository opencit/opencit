/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.repository;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.My;
import com.intel.mtwilson.as.controller.TblOsJpaController;
import com.intel.mtwilson.as.data.TblOs;
import com.intel.mtwilson.as.rest.v2.model.Os;
import com.intel.mtwilson.as.rest.v2.model.OsCollection;
import com.intel.mtwilson.as.rest.v2.model.OsFilterCriteria;
import com.intel.mtwilson.as.rest.v2.model.OsLocator;
import com.intel.mtwilson.datatypes.OsData;
import com.intel.mtwilson.jaxrs2.server.resource.DocumentRepository;
import com.intel.mtwilson.repository.RepositoryCreateException;
import com.intel.mtwilson.repository.RepositoryDeleteException;
import com.intel.mtwilson.repository.RepositoryException;
import com.intel.mtwilson.repository.RepositoryInvalidInputException;
import com.intel.mtwilson.repository.RepositoryRetrieveException;
import com.intel.mtwilson.repository.RepositorySearchException;
import com.intel.mtwilson.repository.RepositoryStoreException;
import com.intel.mtwilson.wlm.business.OsBO;

import java.util.List;
import org.apache.shiro.authz.annotation.RequiresPermissions;

/**
 *
 * @author ssbangal
 */
public class OsRepository implements DocumentRepository<Os, OsCollection, OsFilterCriteria, OsLocator>{

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OsRepository.class);
    
    @Override
    @RequiresPermissions("oss:search")    
    public OsCollection search(OsFilterCriteria criteria) {
        log.debug("Os:Search - Got request to search for the Os.");        
        OsCollection osCollection = new OsCollection();
        try {
            TblOsJpaController osJpaController = My.jpa().mwOs();
            if (criteria.filter == false) {
                List<TblOs> osList = osJpaController.findTblOsEntities();
                if (osList != null && !osList.isEmpty()) {
                    for(TblOs tblOsObj : osList) {
                        osCollection.getOss().add(convert(tblOsObj));
                    }
                }                                
            } else if (criteria.id != null) {
                TblOs tblOs = osJpaController.findTblOsByUUID(criteria.id.toString());
                if (tblOs != null) {
                    osCollection.getOss().add(convert(tblOs));
                }
            } else if (criteria.nameEqualTo != null && !criteria.nameEqualTo.isEmpty()) {
                List<TblOs> osList = osJpaController.findTblOsByName(criteria.nameEqualTo);
                if (osList != null && !osList.isEmpty()) {
                    for(TblOs tblOsObj : osList) {
                        osCollection.getOss().add(convert(tblOsObj));
                    }
                }                
            } else if (criteria.nameContains != null && !criteria.nameContains.isEmpty()) {
                List<TblOs> osList = osJpaController.findTblOsByNameLike(criteria.nameContains);
                if (osList != null && !osList.isEmpty()) {
                    for(TblOs tblOsObj : osList) {
                        osCollection.getOss().add(convert(tblOsObj));
                    }
                }                
            }
        } catch (Exception ex) {
            log.error("Os:Search - Error during Os search.", ex);
            throw new RepositorySearchException(ex, criteria);
        }
        log.debug("Os:Search - Returning back {} of results.", osCollection.getOss().size());                
        return osCollection;
    }

    @Override
    @RequiresPermissions("oss:retrieve")    
    public Os retrieve(OsLocator locator) {
        if( locator == null || locator.id == null ) { return null; }
        log.debug("Os:Retrieve - Got request to retrieve Os with id {}.", locator.id);                
        String id = locator.id.toString();
        try {
            TblOsJpaController osJpaController = My.jpa().mwOs();            
            TblOs tblOs = osJpaController.findTblOsByUUID(id);
            if (tblOs != null) {
                Os os = convert(tblOs);
                return os;
            }
        } catch (RepositoryException re) {
            throw re;
        } catch (Exception ex) {
            log.error("Os:Retrieve - Error during Os retrieval.", ex);
            throw new RepositoryRetrieveException(ex, locator);
        }
        return null;
    }

    @Override
    @RequiresPermissions("oss:store")    
    public void store(Os item) {
        if (item == null || item.getId() == null) { throw new RepositoryInvalidInputException();}
        log.debug("Os:Store - Got request to update Os with id {}.", item.getId().toString());        
        OsLocator locator = new OsLocator();
        locator.id = item.getId();
        
        OsData obj = new OsData();
        try {
            TblOsJpaController osJpaController = My.jpa().mwOs();
            TblOs tblOs = osJpaController.findTblOsByUUID(item.getId().toString());
            if (tblOs == null) {
                log.error("Os:Store - OS specified with UUID {} is not valid.", item.getId().toString());
                throw new RepositoryInvalidInputException(locator);                                                        
            }
            
            obj.setName(item.getName());
            obj.setDescription(item.getDescription());
            new OsBO().updateOs(obj, item.getId().toString());
            log.debug("Os:Store - Updated the Os with id {} successfully.", item.getId().toString());                    
        } catch (RepositoryException re) {
            throw re;
        } catch (Exception ex) {
            log.error("Os:Store - Error during Os update.", ex);
            throw new RepositoryStoreException(ex, locator);
        }
    }

    @Override
    @RequiresPermissions("oss:create")    
    public void create(Os item) {
        log.debug("Os:Create - Got request to create a new role.");
        OsLocator locator = new OsLocator();
        locator.id = item.getId();

        OsData obj = new OsData();
        try {
            TblOsJpaController osJpaController = My.jpa().mwOs();
            TblOs tblOs = osJpaController.findTblOsByUUID(item.getId().toString());
            if (tblOs != null) {
                log.error("Os:Store - OS specified with UUID {} already exists.", item.getId().toString());
                throw new RepositoryInvalidInputException(locator);                                                        
            }

            if (item.getName() == null || item.getName().isEmpty() || item.getVersion() == null || item.getVersion().isEmpty()) {
                log.error("Os:Store - Some of the required input parameters are missing.");
                throw new RepositoryInvalidInputException(locator);                                                                        
            }
            
            obj.setName(item.getName());
            obj.setVersion(item.getVersion());
            obj.setDescription(item.getDescription());
            new OsBO().createOs(obj, item.getId().toString());
            log.debug("Os:Store - Created the Os {} successfully.", item.getName());                                
        } catch (RepositoryException re) {
            throw re;
        } catch (Exception ex) {
            log.error("Os:Create - Error during Os creation.", ex);
            throw new RepositoryCreateException(ex, locator);
        }
    }

    @Override
    @RequiresPermissions("oss:delete")    
    public void delete(OsLocator locator) {
        if( locator == null || locator.id == null ) { return; }
        log.debug("Os:Delete - Got request to delete Os with id {}.", locator.id.toString());        
        String id = locator.id.toString();
        try {
            new OsBO().deleteOs(null, null, id);
            log.debug("Os:Delete - Deleted the Os with id {} successfully.", locator.id.toString()); 
        } catch (Exception ex) {
            log.error("Os:Delete - Error during Os deletion.", ex);
            throw new RepositoryDeleteException(ex, locator);
        }
    }
    
    private Os convert(TblOs tblOsObj) {
        Os os = new Os();
        os.setId(UUID.valueOf(tblOsObj.getUuid_hex()));
        os.setName(tblOsObj.getName());
        os.setVersion(tblOsObj.getVersion());
        os.setDescription(tblOsObj.getDescription());
        return os;
    }

    @Override
    @RequiresPermissions("oss:delete,search")    
    public void delete(OsFilterCriteria criteria) {
        log.debug("Os:Delete - Got request to delete Os by search criteria.");        
        OsCollection objCollection = search(criteria);
        try { 
            for (Os obj : objCollection.getOss()) {
                OsLocator locator = new OsLocator();
                locator.id = obj.getId();
                delete(locator);
            }
        } catch (RepositoryException re) {
            throw re;
        } catch (Exception ex) {
            log.error("Os:Delete - Error during Os deletion.", ex);
            throw new RepositoryDeleteException(ex);
        }
    }
    
}
