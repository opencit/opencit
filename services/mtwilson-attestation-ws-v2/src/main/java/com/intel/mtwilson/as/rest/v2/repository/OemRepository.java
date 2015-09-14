/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.repository;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.My;
import com.intel.mtwilson.as.controller.TblOemJpaController;
import com.intel.mtwilson.as.data.TblOem;
import com.intel.mtwilson.as.rest.v2.model.Oem;
import com.intel.mtwilson.as.rest.v2.model.OemCollection;
import com.intel.mtwilson.as.rest.v2.model.OemFilterCriteria;
import com.intel.mtwilson.as.rest.v2.model.OemLocator;
import com.intel.mtwilson.datatypes.OemData;
import com.intel.mtwilson.jaxrs2.server.resource.DocumentRepository;
import com.intel.mtwilson.repository.RepositoryCreateException;
import com.intel.mtwilson.repository.RepositoryDeleteException;
import com.intel.mtwilson.repository.RepositoryException;
import com.intel.mtwilson.repository.RepositoryInvalidInputException;
import com.intel.mtwilson.repository.RepositoryRetrieveException;
import com.intel.mtwilson.repository.RepositorySearchException;
import com.intel.mtwilson.repository.RepositoryStoreException;
import com.intel.mtwilson.wlm.business.OemBO;

import java.util.List;
import org.apache.shiro.authz.annotation.RequiresPermissions;

/**
 *
 * @author ssbangal
 */
public class OemRepository implements DocumentRepository<Oem, OemCollection, OemFilterCriteria, OemLocator>{

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OemRepository.class);
    
    @Override
    @RequiresPermissions("oems:search")    
    public OemCollection search(OemFilterCriteria criteria) {
        log.debug("Oem:Search - Oem:Got request to search for the Oems.");        
        OemCollection oemCollection = new OemCollection();
        try {
            TblOemJpaController oemJpaController = My.jpa().mwOem();
            if (criteria.filter == false) {
                List<TblOem> oemList = oemJpaController.findTblOemEntities();
                if (oemList != null && !oemList.isEmpty()) {
                    for(TblOem tblOemObj : oemList) {
                        oemCollection.getOems().add(convert(tblOemObj));
                    }
                }                                
            } else if (criteria.id != null) {
                TblOem tblOem = oemJpaController.findTblOemByUUID(criteria.id.toString());
                if( tblOem != null ) {
                    oemCollection.getOems().add(convert(tblOem));
                }
            } else if (criteria.nameEqualTo != null && !criteria.nameEqualTo.isEmpty()) {
                // re-arranged slightly to look more like the nameContains case below
                TblOem tblOem = oemJpaController.findTblOemByName(criteria.nameEqualTo);
                if( tblOem != null ) {
                    oemCollection.getOems().add(convert(tblOem));                
                }
            } else if (criteria.nameContains != null && !criteria.nameContains.isEmpty()) {
                List<TblOem> oemList = oemJpaController.findTblOemByNameLike(criteria.nameContains);
                if (oemList != null && !oemList.isEmpty()) {
                    for(TblOem tblOemObj : oemList) {
                        oemCollection.getOems().add(convert(tblOemObj));
                    }
                }                
            }
        } catch (Exception ex) {
            log.error("Oem:Search - Error during OEM search.", ex);
            throw new RepositorySearchException(ex, criteria);
        }
        log.debug("Oem:Search - Returning back {} of results.", oemCollection.getOems().size());                
        return oemCollection;
    }

    @Override
    @RequiresPermissions("oems:retrieve")    
    public Oem retrieve(OemLocator locator) {
        if( locator == null || locator.id == null ) { return null; }
        log.debug("Oem:Retrieve - Got request to retrieve Oem with id {}.", locator.id);                
        String id = locator.id.toString();
        try {
            TblOemJpaController oemJpaController = My.jpa().mwOem();
            TblOem tblOem = oemJpaController.findTblOemByUUID(id);
            if (tblOem != null) {
                Oem oem = convert(tblOem);
                return oem;
            }
        } catch (Exception ex) {
            log.error("Oem:Retrieve - Error during OEM retrieval.", ex);
            throw new RepositoryRetrieveException(ex, locator);
        }
        return null;
    }

    @Override
    @RequiresPermissions("oems:store")    
    public void store(Oem item) {
        if (item == null || item.getId() == null) { throw new RepositoryInvalidInputException();}
        log.debug("Oem:Store - Got request to update Oem with id {}.", item.getId().toString());        
        OemLocator locator = new OemLocator();
        locator.id = item.getId();
        
        OemData obj = new OemData();
        try {            
            TblOemJpaController oemJpaController = My.jpa().mwOem();
            TblOem tblOem = oemJpaController.findTblOemByUUID(item.getId().toString());
            if (tblOem == null) {
                log.error("Oem:Store - OEM specified with UUID {} is not valid.", item.getId().toString());
                throw new RepositoryInvalidInputException(locator);                                        
            }

            obj.setDescription(item.getDescription());
            new OemBO().updateOem(obj, item.getId().toString());
            log.debug("Oem:Store - Updated the Oem with id {} successfully.", item.getId().toString()); 
        } catch (RepositoryException re) {
            throw re;
        } catch (Exception ex) {
            log.error("Oem:Store - Error during Oem update.", ex);
            throw new RepositoryStoreException(ex, locator);
        }
    }

    @Override
    @RequiresPermissions("oems:create")    
    public void create(Oem item) {
        log.debug("Oem:Create - Got request to create a new Oem.");
        OemLocator locator = new OemLocator();
        locator.id = item.getId();
        OemData obj = new OemData();
        try {
            TblOemJpaController oemJpaController = My.jpa().mwOem();
            TblOem tblOem = oemJpaController.findTblOemByName(item.getName());
            if (tblOem != null) {
                log.error("Oem:Create - OEM specified {} already exists.", item.getId().toString());
                throw new RepositoryInvalidInputException(locator);                                        
            }
            
            if (item.getName()==null || item.getName().isEmpty()) {
                log.error("Oem:Create - Invalid name specified for the OEM.");
                throw new RepositoryInvalidInputException(locator);                                                        
            }
            
            obj.setName(item.getName());
            obj.setDescription(item.getDescription());
            new OemBO().createOem(obj, item.getId().toString());
            log.debug("Oem:Create - Created the Oem {} successfully.", item.getName()); 
        } catch (RepositoryException re) {
            throw re;
        } catch (Exception ex) {
            log.error("Oem:Create - Error during role creation.", ex);
            throw new RepositoryCreateException(ex, locator);
        }
    }

    @Override
    @RequiresPermissions("oems:delete")    
    public void delete(OemLocator locator) {
        if( locator == null || locator.id == null ) { return; }
        log.debug("Oem:Delete - Got request to delete Oem with id {}.", locator.id.toString());        
        String id = locator.id.toString();
        try {
            new OemBO().deleteOem(null, id);
            log.debug("Oem:Delete - Deleted the Oem with id {} successfully.", locator.id.toString());
        } catch (Exception ex) {
            log.error("Oem:Delete - Error during Oem deletion.", ex);
            throw new RepositoryDeleteException(ex, locator);
        }
    }


    // passing null to this method would be a programming error and NullPointerException is appropriate
    // calling code should check for null before calling
    private Oem convert(TblOem tblOemObj) {
        Oem oem = new Oem();
        oem.setId(UUID.valueOf(tblOemObj.getUuid_hex()));
        oem.setName(tblOemObj.getName());
        oem.setDescription(tblOemObj.getDescription());
        return oem;
    }

    @Override
    @RequiresPermissions("oems:delete,search")    
    public void delete(OemFilterCriteria criteria) {
        log.debug("Oem:Delete - Got request to delete Oem by search criteria.");        
        OemCollection objCollection = search(criteria);
        try { 
            for (Oem obj : objCollection.getOems()) {
                OemLocator locator = new OemLocator();
                locator.id = obj.getId();
                delete(locator);
            }
        } catch (RepositoryException re) {
            throw re;
        } catch (Exception ex) {
            log.error("Oem:Delete - Error during Oem deletion.", ex);
            throw new RepositoryDeleteException(ex);
        }
    }
    
}
