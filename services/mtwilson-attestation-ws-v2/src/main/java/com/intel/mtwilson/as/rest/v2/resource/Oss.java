/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.resource;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mountwilson.as.common.ASException;
import com.intel.mtwilson.My;
import com.intel.mtwilson.as.controller.TblOsJpaController;
import com.intel.mtwilson.as.data.TblOs;
import com.intel.mtwilson.as.rest.v2.model.Os;
import com.intel.mtwilson.as.rest.v2.model.OsCollection;
import com.intel.mtwilson.as.rest.v2.model.OsFilterCriteria;
import com.intel.mtwilson.as.rest.v2.model.OsLinks;
import com.intel.mtwilson.datatypes.ErrorCode;
import com.intel.mtwilson.datatypes.OsData;
import com.intel.mtwilson.jersey.resource.AbstractResource;
import com.intel.mtwilson.launcher.ws.ext.V2;
import com.intel.mtwilson.wlm.business.OsBO;

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
@Path("/oss")
public class Oss extends AbstractResource<Os, OsCollection, OsFilterCriteria, OsLinks>{

    private Logger log = LoggerFactory.getLogger(getClass().getName());

    public Oss() {
        super();
    }
    
    @Override
    protected OsCollection search(OsFilterCriteria criteria) {
        // start with a collection instance; if we don't find anything we'll return the empty collection
        OsCollection osCollection = new OsCollection();
        try {
            TblOsJpaController osJpaController = My.jpa().mwOs();
            if (criteria.id != null) {
                TblOs tblOs = osJpaController.findTblOsByUUID(criteria.id.toString());
                if (tblOs != null) {
                    osCollection.getOss().add(convert(tblOs));
                }
            } else if (criteria.nameEqualTo != null && !criteria.nameEqualTo.isEmpty()) {
                List<TblOs> osList = osJpaController.findTblOsByName(criteria.nameContains);
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
        return osCollection;
    }

    @Override
    protected Os retrieve(String id) {
        if( id == null ) { return null; }
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
    protected void store(Os item) {
        OsData obj = new OsData();
        try {
            obj.setName(item.getName());
            obj.setDescription(item.getDescription());
            new OsBO().updateOs(obj, item.getId().toString());
        } catch (ASException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during OS update.", ex);
            throw new ASException(ErrorCode.WS_OS_UPDATE_ERROR, ex.getClass().getSimpleName());
        }        
    }

    @Override
    protected void create(Os item) {
        OsData obj = new OsData();
        try {
            obj.setName(item.getName());
            obj.setVersion(item.getVersion());
            obj.setDescription(item.getDescription());
            new OsBO().createOs(obj, item.getId().toString());
        } catch (ASException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during OS creation.", ex);
            throw new ASException(ErrorCode.WS_OS_CREATE_ERROR, ex.getClass().getSimpleName());
        }
    }

    @Override
    protected void delete(String id) {
        try {
            new OsBO().deleteOs(null, null, id);
        } catch (ASException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during OS delete.", ex);
            throw new ASException(ErrorCode.WS_OS_DELETE_ERROR, ex.getClass().getSimpleName());
        }
    }

    @Override
    protected OsCollection createEmptyCollection() {
        return new OsCollection();
    }
    
    private Os convert(TblOs tblOsObj) {
        Os os = new Os();
        os.setId(UUID.valueOf(tblOsObj.getUuid_hex()));
        os.setName(tblOsObj.getName());
        os.setVersion(tblOsObj.getVersion());
        os.setDescription(tblOsObj.getDescription());
        return os;
    }
    
}
