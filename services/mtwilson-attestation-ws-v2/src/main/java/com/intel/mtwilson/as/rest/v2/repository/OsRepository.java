/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.repository;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mountwilson.as.common.ASException;
import com.intel.mtwilson.My;
import com.intel.mtwilson.as.controller.TblOsJpaController;
import com.intel.mtwilson.as.data.TblOs;
import com.intel.mtwilson.as.rest.v2.model.Os;
import com.intel.mtwilson.as.rest.v2.model.OsCollection;
import com.intel.mtwilson.as.rest.v2.model.OsFilterCriteria;
import com.intel.mtwilson.as.rest.v2.model.OsLocator;
import com.intel.mtwilson.i18n.ErrorCode;
import com.intel.mtwilson.datatypes.OsData;
import com.intel.mtwilson.jaxrs2.server.resource.SimpleRepository;
import com.intel.mtwilson.wlm.business.OsBO;

import java.util.List;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import org.apache.shiro.authz.annotation.RequiresPermissions;

/**
 *
 * @author ssbangal
 */
public class OsRepository implements SimpleRepository<Os, OsCollection, OsFilterCriteria, OsLocator>{

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
        } catch (ASException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during OS search.", ex);
            throw new ASException(ErrorCode.WS_OS_RETRIEVAL_ERROR, ex.getClass().getSimpleName());
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
        } catch (ASException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during OS retrieval.", ex);
            throw new ASException(ErrorCode.WS_OS_RETRIEVAL_ERROR, ex.getClass().getSimpleName());
        }
        return null;
    }

    @Override
    @RequiresPermissions("oss:store")    
    public void store(Os item) {
        if (item == null || item.getId() == null) { throw new WebApplicationException(Response.Status.BAD_REQUEST);}
        log.debug("Os:Store - Got request to update Os with id {}.", item.getId().toString());        
        OsData obj = new OsData();
        try {
            obj.setName(item.getName());
            obj.setDescription(item.getDescription());
            new OsBO().updateOs(obj, item.getId().toString());
            log.debug("Os:Store - Updated the Os with id {} successfully.", item.getId().toString());                    
        } catch (ASException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during OS update.", ex);
            throw new ASException(ErrorCode.WS_OS_UPDATE_ERROR, ex.getClass().getSimpleName());
        }        
    }

    @Override
    @RequiresPermissions("oss:create")    
    public void create(Os item) {
        log.debug("Os:Create - Got request to create a new role.");
        OsData obj = new OsData();
        try {
            obj.setName(item.getName());
            obj.setVersion(item.getVersion());
            obj.setDescription(item.getDescription());
            new OsBO().createOs(obj, item.getId().toString());
            log.debug("Os:Store - Created the Os {} successfully.", item.getName());                                
        } catch (ASException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during OS creation.", ex);
            throw new ASException(ErrorCode.WS_OS_CREATE_ERROR, ex.getClass().getSimpleName());
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
        } catch (ASException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during OS delete.", ex);
            throw new ASException(ErrorCode.WS_OS_DELETE_ERROR, ex.getClass().getSimpleName());
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
        } catch (Exception ex) {
            log.error("Error during Os deletion.", ex);
            throw new WebApplicationException("Please see the server log for more details.", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }
    
}
