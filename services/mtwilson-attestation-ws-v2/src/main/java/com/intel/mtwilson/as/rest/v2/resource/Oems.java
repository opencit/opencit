/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.resource;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mountwilson.as.common.ASException;
import com.intel.mtwilson.My;
import com.intel.mtwilson.as.controller.TblOemJpaController;
import com.intel.mtwilson.as.data.TblOem;
import com.intel.mtwilson.as.rest.v2.model.Oem;
import com.intel.mtwilson.as.rest.v2.model.OemCollection;
import com.intel.mtwilson.as.rest.v2.model.OemFilterCriteria;
import com.intel.mtwilson.as.rest.v2.model.OemLinks;
import com.intel.mtwilson.datatypes.ErrorCode;
import com.intel.mtwilson.datatypes.OemData;
import com.intel.mtwilson.jersey.resource.AbstractResource;
import com.intel.mtwilson.launcher.ws.ext.V2;
import com.intel.mtwilson.wlm.business.OemBO;

import java.util.List;
import javax.ejb.Stateless;
import javax.ws.rs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ssbangal
 */
@V2
@Stateless
@Path("/oems")
public class Oems extends AbstractResource<Oem, OemCollection, OemFilterCriteria, OemLinks>{
    private Logger log = LoggerFactory.getLogger(getClass().getName());
    
    public Oems() {
        super();
    }
    
    @Override
    protected OemCollection search(OemFilterCriteria criteria) {
        // start with a collection instance; if we don't find anything we'll return the empty collection
        OemCollection oemCollection = new OemCollection();
        try {
            TblOemJpaController oemJpaController = My.jpa().mwOem();
            if (criteria.id != null) {
                // re-arranged slightly to look more like the nameContains case below
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
        return oemCollection;
    }

    @Override
    protected Oem retrieve(String id) {
        if( id == null ) { return null; }
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
    protected void store(Oem item) {
        OemData obj = new OemData();
        try {
            obj.setName(item.getName());
            obj.setDescription(item.getDescription());
            new OemBO().updateOem(obj, item.getId().toString());
        } catch (ASException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during OEM update.", ex);
            throw new ASException(ErrorCode.WS_OEM_UPDATE_ERROR, ex.getClass().getSimpleName());
        }        
    }

    @Override
    protected void create(Oem item) {
        OemData obj = new OemData();
        try {
            obj.setName(item.getName());
            obj.setDescription(item.getDescription());
            new OemBO().createOem(obj, item.getId().toString());
        } catch (ASException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during OEM creation.", ex);
            throw new ASException(ErrorCode.WS_OEM_CREATE_ERROR, ex.getClass().getSimpleName());
        }
    }

    @Override
    protected void delete(String id) {
        try {
            new OemBO().deleteOem(null, id);
        } catch (ASException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during OEM delete.", ex);
            throw new ASException(ErrorCode.WS_OEM_DELETE_ERROR, ex.getClass().getSimpleName());
        }
    }

    @Override
    protected OemCollection createEmptyCollection() {
        return new OemCollection();
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
    
}
