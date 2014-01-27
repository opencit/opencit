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
    Logger log = LoggerFactory.getLogger(getClass().getName());
    
    @Override
    protected OemCollection search(OemFilterCriteria criteria) {
        OemCollection oemCollection = null;
        try {
            TblOemJpaController oemJpaController = My.jpa().mwOem();
            if (criteria.id != null) {
                Oem oem = convert(oemJpaController.findTblOemByUUID(criteria.id.toString()));            
                oemCollection.getOems().add(oem);
            } else if (criteria.nameEqualTo != null && !criteria.nameEqualTo.isEmpty()) {
                Oem oem = convert(oemJpaController.findTblOemByName(criteria.nameEqualTo));
                oemCollection.getOems().add(oem);                
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
        Oem oem = null;
        try {
            TblOemJpaController oemJpaController = My.jpa().mwOem();
            if (id != null) {
                TblOem tblOem = oemJpaController.findTblOemByUUID(id);
                if (tblOem != null)
                    oem = convert(tblOem);
            }
        } catch (ASException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during OEM retrieval.", ex);
            throw new ASException(ErrorCode.WS_OEM_RETRIEVAL_ERROR, ex.getClass().getSimpleName());
        }
        return oem;
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

    private Oem convert(TblOem tblOemObj) {
        Oem oem = new Oem();
        if (tblOemObj != null) {
            oem.setId(UUID.valueOf(tblOemObj.getUuid_hex()));
            oem.setName(tblOemObj.getName());
            oem.setDescription(tblOemObj.getDescription());
        } else {
            oem = null;
        }
        return oem;
    }
    
}
