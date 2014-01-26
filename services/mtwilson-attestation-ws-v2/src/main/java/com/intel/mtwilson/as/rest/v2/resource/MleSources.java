/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.resource;

import com.intel.mtwilson.My;
import com.intel.mtwilson.as.controller.MwMleSourceJpaController;
import com.intel.mtwilson.as.data.MwMleSource;
import com.intel.mtwilson.as.rest.v2.model.MleSource;
import com.intel.mtwilson.as.rest.v2.model.MleSourceCollection;
import com.intel.mtwilson.as.rest.v2.model.MleSourceFilterCriteria;
import com.intel.mtwilson.as.rest.v2.model.MleSourceLinks;
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
@Path("/mle-sources")
public class MleSources extends AbstractResource<MleSource, MleSourceCollection, MleSourceFilterCriteria, MleSourceLinks>{

    @Override
    protected MleSourceCollection search(MleSourceFilterCriteria criteria) {
        MleSourceCollection mleSourceCollection = null;
        try {
            MwMleSourceJpaController jpaController = My.jpa().mwMleSource();
            if (criteria.id != null) {
                MleSource mleSource = convert(jpaController.findByMleUuid(criteria.id.toString()));            
                mleSourceCollection.getMleSources().add(mleSource);
            } else if (criteria.mleUuid != null) {
                MleSource mleSource = convert(jpaController.findByMleUuid(criteria.mleUuid.toString()));            
                mleSourceCollection.getMleSources().add(mleSource);
            }else if (criteria.nameEqualTo != null && !criteria.nameEqualTo.isEmpty()) {
                List<MwMleSource> mleSourceList = jpaController.findByHostName(criteria.nameEqualTo);
                if (mleSourceList != null && !mleSourceList.isEmpty()) {
                    for(MwMleSource mleSourceObj : mleSourceList) {
                        mleSourceCollection.getMleSources().add(convert(mleSourceObj));
                    }
                }                
            } else if (criteria.nameContains != null && !criteria.nameContains.isEmpty()) {
                List<MwMleSource> mleSourceList = jpaController.findByHostNameLike(criteria.nameContains);
                if (mleSourceList != null && !mleSourceList.isEmpty()) {
                    for(MwMleSource mleSourceObj : mleSourceList) {
                        mleSourceCollection.getMleSources().add(convert(mleSourceObj));
                    }
                }                
            }
        } catch (IOException ex) {
            Logger.getLogger(Oems.class.getName()).log(Level.SEVERE, null, ex);
        }
        return mleSourceCollection;
    }

    @Override
    protected MleSource retrieve(String id) {
        MleSource obj = null;
        try {
            MwMleSourceJpaController jpaController = My.jpa().mwMleSource();
            if (id != null) {
                obj = convert(jpaController.findByMleUuid(id));            
            } 
        } catch (IOException ex) {
            Logger.getLogger(Oems.class.getName()).log(Level.SEVERE, null, ex);
        }
        return obj;
    }

    @Override
    protected void store(MleSource item) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void create(MleSource item) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void delete(String id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected MleSourceCollection createEmptyCollection() {
        return new MleSourceCollection();
    }

    private MleSource convert(MwMleSource obj) {
        MleSource convObj = new MleSource();
        if (obj != null) {            
            convObj.setMleUuid(obj.getUuid_hex());
            convObj.setName(obj.getHostName());
        } else {
            convObj = null;
        }
        return convObj;
    }
    
}
