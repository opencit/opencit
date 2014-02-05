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
import com.intel.mtwilson.datatypes.ErrorCode;
import com.intel.mtwilson.datatypes.OsData;
import com.intel.mtwilson.jersey.resource.SimpleRepository;
import com.intel.mtwilson.wlm.business.OsBO;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ssbangal
 */
public class OsRepository implements SimpleRepository<Os, OsCollection, OsFilterCriteria, OsLocator>{

    private Logger log = LoggerFactory.getLogger(getClass().getName());
    
    @Override
    public OsCollection search(OsFilterCriteria criteria) {
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
    public Os retrieve(OsLocator locator) {
        if( locator == null || locator.id == null ) { return null; }
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
    public void store(Os item) {
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
    public void create(Os item) {
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
    public void delete(OsLocator locator) {
        if( locator == null || locator.id == null ) { return; }
        String id = locator.id.toString();
        try {
            new OsBO().deleteOs(null, null, id);
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
    public void delete(OsFilterCriteria criteria) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
