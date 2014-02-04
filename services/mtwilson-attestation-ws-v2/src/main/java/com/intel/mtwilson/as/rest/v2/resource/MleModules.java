/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.resource;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mountwilson.as.common.ASException;
import com.intel.mtwilson.My;
import com.intel.mtwilson.as.controller.TblModuleManifestJpaController;
import com.intel.mtwilson.as.data.TblModuleManifest;
import com.intel.mtwilson.as.rest.v2.model.MleModule;
import com.intel.mtwilson.as.rest.v2.model.MleModuleCollection;
import com.intel.mtwilson.as.rest.v2.model.MleModuleFilterCriteria;
import com.intel.mtwilson.as.rest.v2.model.MleModuleLinks;
import com.intel.mtwilson.datatypes.ErrorCode;
import com.intel.mtwilson.datatypes.ModuleWhiteList;
import com.intel.mtwilson.jersey.resource.AbstractResource;
import com.intel.mtwilson.launcher.ws.ext.V2;
import com.intel.mtwilson.wlm.business.MleBO;
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
@Path("/mle-modules")
public class MleModules extends AbstractResource<MleModule, MleModuleCollection, MleModuleFilterCriteria, MleModuleLinks> {

    private Logger log = LoggerFactory.getLogger(getClass().getName());
    
    public MleModules() {
        super();
    }
    
    @Override
    protected MleModuleCollection search(MleModuleFilterCriteria criteria) {
        MleModuleCollection objCollection = new MleModuleCollection();
        try {
            TblModuleManifestJpaController jpaController = My.jpa().mwModuleManifest();
            if (criteria.id != null) {
                TblModuleManifest obj = jpaController.findTblModuleManifestByUuid(criteria.id.toString());
                if (obj != null) {
                    objCollection.getMleModules().add(convert(obj));
                }
            } else if (criteria.mleUuid != null) {
                List<TblModuleManifest> objList = jpaController.findTblModuleManifestByMleUuid(criteria.mleUuid.toString());
                if (objList != null && !objList.isEmpty()) {
                    for(TblModuleManifest obj : objList) {
                        objCollection.getMleModules().add(convert(obj));
                    }
                }                
            }else if (criteria.nameContains != null && !criteria.nameContains.isEmpty()) {
                List<TblModuleManifest> objList = jpaController.findTblModuleManifestByComponentNameLike(criteria.nameContains);
                if (objList != null && !objList.isEmpty()) {
                    for(TblModuleManifest obj : objList) {
                        objCollection.getMleModules().add(convert(obj));
                    }
                }                
            }
        } catch (ASException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during Module whitelist search.", ex);
            throw new ASException(ErrorCode.WS_MODULE_WHITELIST_RETRIEVAL_ERROR, ex.getClass().getSimpleName());
        }
        return objCollection;
    }

    @Override
    protected MleModule retrieve(String id) {
        if( id == null ) { return null; }
        try {
            TblModuleManifestJpaController jpaController = My.jpa().mwModuleManifest();
            TblModuleManifest obj = jpaController.findTblModuleManifestByUuid(id);
            if (obj != null) {
                MleModule convObj = convert(obj);
                return convObj;
            }
        } catch (ASException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during Module whitelist retrieval.", ex);
            throw new ASException(ErrorCode.WS_MODULE_WHITELIST_RETRIEVAL_ERROR, ex.getClass().getSimpleName());
        }
        return null;
    }

    @Override
    protected void store(MleModule item) {
        ModuleWhiteList obj = new ModuleWhiteList();
        try {
            obj.setDigestValue(item.getDigestValue());
            obj.setDescription(item.getDescription());
            new MleBO().updateModuleWhiteList(obj, null, item.getId().toString());
        } catch (ASException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during module whitelist update.", ex);
            throw new ASException(ErrorCode.WS_MODULE_WHITELIST_UPDATE_ERROR, ex.getClass().getSimpleName());
        }        
    }

    @Override
    protected void create(MleModule item) {
        ModuleWhiteList obj = new ModuleWhiteList();
        try {
            obj.setComponentName(item.getComponentName());
            obj.setDigestValue(item.getDigestValue());
            obj.setDescription(item.getDescription());
            obj.setEventName(item.getEventName());
            obj.setExtendedToPCR(item.getExtendedToPCR());
            obj.setPackageName(item.getPackageName());
            obj.setPackageVendor(item.getPackageVendor());
            obj.setPackageVersion(item.getPackageVersion());
            obj.setUseHostSpecificDigest(item.getUseHostSpecificDigest());
            new MleBO().addModuleWhiteList(obj, null, item.getId().toString(), item.getMleUuid());
        } catch (ASException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during module whitelist creation.", ex);
            throw new ASException(ErrorCode.WS_MODULE_WHITELIST_CREATE_ERROR, ex.getClass().getSimpleName());
        }        
    }

    @Override
    protected void delete(String id) {
        try {
            new MleBO().deleteModuleWhiteList(null, null, null, null, null, null, null, id);
        } catch (ASException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during module whitelist deletion.", ex);
            throw new ASException(ErrorCode.WS_MODULE_WHITELIST_DELETE_ERROR, ex.getClass().getSimpleName());
        }        
    }

    @Override
    protected MleModuleCollection createEmptyCollection() {
        return new MleModuleCollection();
    }
    
    private MleModule convert(TblModuleManifest obj) {
        MleModule convObj = new MleModule();
        convObj.setId(UUID.valueOf(obj.getUuid_hex()));
        convObj.setMleUuid(obj.getUuid_hex());
        convObj.setComponentName(obj.getComponentName());
        convObj.setDescription(obj.getDescription());
        convObj.setDigestValue(obj.getDigestValue());
        convObj.setEventName(obj.getEventID().getName());
        convObj.setExtendedToPCR(obj.getExtendedToPCR());
        convObj.setPackageName(obj.getPackageName());
        convObj.setPackageVendor(obj.getPackageVendor());
        convObj.setPackageVersion(obj.getPackageVersion());
        return convObj;
    }
    
}
