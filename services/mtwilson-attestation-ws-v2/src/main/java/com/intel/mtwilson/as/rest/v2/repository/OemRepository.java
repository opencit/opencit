/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.repository;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mountwilson.as.common.ASException;
import com.intel.mtwilson.My;
import com.intel.mtwilson.as.controller.TblOemJpaController;
import com.intel.mtwilson.as.data.TblOem;
import com.intel.mtwilson.as.rest.v2.model.Oem;
import com.intel.mtwilson.as.rest.v2.model.OemCollection;
import com.intel.mtwilson.as.rest.v2.model.OemFilterCriteria;
import com.intel.mtwilson.as.rest.v2.model.OemLocator;
import com.intel.mtwilson.i18n.ErrorCode;
import com.intel.mtwilson.datatypes.OemData;
import com.intel.mtwilson.jaxrs2.server.resource.DocumentRepository;
import com.intel.mtwilson.wlm.business.OemBO;

import java.util.List;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
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
        log.debug("Oem:Search - Got request to search for the Oems.");        
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
        } catch (ASException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during OEM search.", ex);
            throw new ASException(ErrorCode.WS_OEM_RETRIEVAL_ERROR, ex.getClass().getSimpleName());
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
        } catch (ASException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during OEM retrieval.", ex);
            throw new ASException(ErrorCode.WS_OEM_RETRIEVAL_ERROR, ex.getClass().getSimpleName());
        }
        return null;
    }

    @Override
    @RequiresPermissions("oems:store")    
    public void store(Oem item) {
        if (item == null || item.getId() == null) { throw new WebApplicationException(Response.Status.BAD_REQUEST);}
        log.debug("Oem:Store - Got request to update Oem with id {}.", item.getId().toString());        
        
        OemData obj = new OemData();
        try {            
            obj.setDescription(item.getDescription());
            new OemBO().updateOem(obj, item.getId().toString());
            log.debug("Oem:Store - Updated the Oem with id {} successfully.", item.getId().toString()); 
        } catch (ASException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during OEM update.", ex);
            throw new ASException(ErrorCode.WS_OEM_UPDATE_ERROR, ex.getClass().getSimpleName());
        }        
    }

    @Override
    @RequiresPermissions("oems:create")    
    public void create(Oem item) {
        log.debug("Oem:Create - Got request to create a new Oem.");
        OemData obj = new OemData();
        try {
            obj.setName(item.getName());
            obj.setDescription(item.getDescription());
            new OemBO().createOem(obj, item.getId().toString());
            log.debug("Oem:Store - Created the Oem {} successfully.", item.getName()); 
        } catch (ASException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during OEM creation.", ex);
            throw new ASException(ErrorCode.WS_OEM_CREATE_ERROR, ex.getClass().getSimpleName());
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
        } catch (ASException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during OEM delete.", ex);
            throw new ASException(ErrorCode.WS_OEM_DELETE_ERROR, ex.getClass().getSimpleName());
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
        } catch (Exception ex) {
            log.error("Error during Oem deletion.", ex);
            throw new WebApplicationException("Please see the server log for more details.", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }
    
}
