/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.resource;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.My;
import com.intel.mtwilson.as.controller.TblPcrManifestJpaController;
import com.intel.mtwilson.as.data.TblPcrManifest;
import com.intel.mtwilson.as.rest.v2.model.MlePcr;
import com.intel.mtwilson.as.rest.v2.model.MlePcrCollection;
import com.intel.mtwilson.as.rest.v2.model.MlePcrFilterCriteria;
import com.intel.mtwilson.as.rest.v2.model.MlePcrLinks;
import com.intel.mtwilson.jersey.resource.AbstractResource;
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
@Stateless
@Path("/mle-pcrs")
public class MlePcrs extends AbstractResource<MlePcr, MlePcrCollection, MlePcrFilterCriteria, MlePcrLinks>{

    @Override
    protected MlePcrCollection search(MlePcrFilterCriteria criteria) {
        MlePcrCollection objCollection = null;
        try {
            TblPcrManifestJpaController jpaController = My.jpa().mwPcrManifest();
            if (criteria.id != null) {
                MlePcr obj = convert(jpaController.findTblPcrManifestByUuid(criteria.id.toString()));            
                objCollection.getMlePcrs().add(obj);
            } else if (criteria.mleUuid != null) {
                List<TblPcrManifest> objList = jpaController.findTblPcrManifestByMleUuid(criteria.mleUuid.toString());
                if (objList != null && !objList.isEmpty()) {
                    for(TblPcrManifest obj : objList) {
                        objCollection.getMlePcrs().add(convert(obj));
                    }
                }                
            }else if (criteria.nameEqualTo != null && !criteria.nameEqualTo.isEmpty()) {
                List<TblPcrManifest> objList = jpaController.findTblPcrManifestByPcrName(criteria.nameEqualTo);
                if (objList != null && !objList.isEmpty()) {
                    for(TblPcrManifest obj : objList) {
                        objCollection.getMlePcrs().add(convert(obj));
                    }
                }                
            }
        } catch (IOException ex) {
            Logger.getLogger(Oems.class.getName()).log(Level.SEVERE, null, ex);
        }
        return objCollection;
    }

    @Override
    protected MlePcr retrieve(String id) {
        MlePcr obj = null;
        try {
            TblPcrManifestJpaController jpaController = My.jpa().mwPcrManifest();
            if (id != null) {
                obj = convert(jpaController.findTblPcrManifestByUuid(id));            
            } 
        } catch (IOException ex) {
            Logger.getLogger(Oems.class.getName()).log(Level.SEVERE, null, ex);
        }
        return obj;
    }

    @Override
    protected void store(MlePcr item) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void create(MlePcr item) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void delete(String id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected MlePcrCollection createEmptyCollection() {
        return new MlePcrCollection();
    }
    
    private MlePcr convert(TblPcrManifest obj) {
        MlePcr convObj = new MlePcr();
        if (obj != null) {  
            convObj.setId(UUID.valueOf(obj.getUuid_hex()));
            convObj.setMleUuid(obj.getUuid_hex());
            convObj.setPcrName(obj.getName());
            convObj.setPcrDigest(obj.getValue());
            convObj.setDescription(obj.getPCRDescription());
        } else {
            convObj = null;
        }
        return convObj;
    }
    
}
