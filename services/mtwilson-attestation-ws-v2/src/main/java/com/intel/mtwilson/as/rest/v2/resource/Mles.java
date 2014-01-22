/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.resource;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.My;
import com.intel.mtwilson.as.controller.TblMleJpaController;
import com.intel.mtwilson.as.controller.exceptions.IllegalOrphanException;
import com.intel.mtwilson.as.controller.exceptions.NonexistentEntityException;
import com.intel.mtwilson.as.data.TblMle;
import com.intel.mtwilson.as.rest.v2.model.Mle;
import com.intel.mtwilson.as.rest.v2.model.MleCollection;
import com.intel.mtwilson.as.rest.v2.model.MleFilterCriteria;
import com.intel.mtwilson.as.rest.v2.model.MleLinks;
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
@Path("/mles")
public class Mles extends AbstractResource<Mle, MleCollection, MleFilterCriteria, MleLinks> {

    @Override
    protected MleCollection search(MleFilterCriteria criteria) {
        MleCollection mleCollection = null;
        try {
            TblMleJpaController jpaController = My.jpa().mwMle();
            if (criteria.id != null) {
                Mle mle = convert(jpaController.findTblMleByUUID(criteria.id.toString()));            
                mleCollection.getMles().add(mle);
            } else if (criteria.nameContains != null && !criteria.nameContains.isEmpty()) {
                List<TblMle> mleList = jpaController.findMleByNameSearchCriteria(criteria.nameContains);
                if (mleList != null && !mleList.isEmpty()) {
                    for(TblMle mleObj : mleList) {
                        mleCollection.getMles().add(convert(mleObj));
                    }
                }                
            }
        } catch (IOException ex) {
            Logger.getLogger(Oems.class.getName()).log(Level.SEVERE, null, ex);
        }
        return mleCollection;
    }

    @Override
    protected Mle retrieve(String id) {
        Mle mle = null;
        try {
            TblMleJpaController jpaController = My.jpa().mwMle();
            if (id != null) {
                mle = convert(jpaController.findTblMleByUUID(id));            
            } 
        } catch (IOException ex) {
            Logger.getLogger(Oems.class.getName()).log(Level.SEVERE, null, ex);
        }
        return mle;
    }

    @Override
    protected void store(Mle item) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void create(Mle item) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void delete(String id) {
        try {
            TblMleJpaController jpaController = My.jpa().mwMle();
            if (id != null) {
                TblMle mleObj = jpaController.findTblMleByUUID(id);
                if (mleObj != null)
                    jpaController.destroy(mleObj.getId());
            } 
        } catch (IOException ex) {
            Logger.getLogger(Oems.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalOrphanException ex) {
            Logger.getLogger(Mles.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NonexistentEntityException ex) {
            Logger.getLogger(Mles.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected MleFilterCriteria createFilterCriteriaWithId(String id) {
        MleFilterCriteria criteria = new MleFilterCriteria();
        criteria.id = UUID.valueOf(id);
        return criteria;
    }
    
    private Mle convert(TblMle tblMleObj) {
        Mle mle = new Mle();
        if (tblMleObj != null) {
            mle.setId(UUID.valueOf(tblMleObj.getUuid_hex()));
            mle.setName(tblMleObj.getName());
            mle.setVersion(tblMleObj.getVersion());
            mle.setOemUUID(tblMleObj.getOemId().getUuid_hex());
            mle.setOsUUID(tblMleObj.getOsId().getUuid_hex());
            mle.setDescription(tblMleObj.getDescription());            
        } else {
            mle = null;
        }
        return mle;
    }
    
}
