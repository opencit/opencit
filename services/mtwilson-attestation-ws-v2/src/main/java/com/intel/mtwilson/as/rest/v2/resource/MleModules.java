/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.resource;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.My;
import com.intel.mtwilson.as.controller.TblModuleManifestJpaController;
import com.intel.mtwilson.as.controller.TblPcrManifestJpaController;
import com.intel.mtwilson.as.data.TblModuleManifest;
import com.intel.mtwilson.as.data.TblPcrManifest;
import com.intel.mtwilson.as.rest.v2.model.MleModule;
import com.intel.mtwilson.as.rest.v2.model.MleModuleCollection;
import com.intel.mtwilson.as.rest.v2.model.MleModuleFilterCollection;
import com.intel.mtwilson.as.rest.v2.model.MleModuleLinks;
import com.intel.mtwilson.as.rest.v2.model.MlePcr;
import com.intel.mtwilson.as.rest.v2.model.MlePcrCollection;
import com.intel.mtwilson.jersey.resource.AbstractResource;
import com.intel.mtwilson.launcher.ws.ext.V2;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.ws.rs.Path;

/**
 *
 * @author ssbangal
 */
@V2
@Stateless
@Path("/mle-modules")
public class MleModules extends AbstractResource<MleModule, MleModuleCollection, MleModuleFilterCollection, MleModuleLinks> {

    @Override
    protected MleModuleCollection search(MleModuleFilterCollection criteria) {
        MleModuleCollection objCollection = null;
        try {
            TblModuleManifestJpaController jpaController = My.jpa().mwModuleManifest();
            if (criteria.id != null) {
                MleModule obj = convert(jpaController.findTblModuleManifestByUuid(criteria.id.toString()));            
                objCollection.getMleModules().add(obj);
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
        } catch (IOException ex) {
            Logger.getLogger(Oems.class.getName()).log(Level.SEVERE, null, ex);
        }
        return objCollection;
    }

    @Override
    protected MleModule retrieve(String id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void store(MleModule item) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void create(MleModule item) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void delete(String id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected MleModuleCollection createEmptyCollection() {
        return new MleModuleCollection();
    }
    
    private MleModule convert(TblModuleManifest obj) {
        MleModule convObj = new MleModule();
        if (obj != null) {  
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
        } else {
            convObj = null;
        }
        return convObj;
    }
    
}
