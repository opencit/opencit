/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.resource;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.My;
import com.intel.mtwilson.as.controller.TblOemJpaController;
import com.intel.mtwilson.as.controller.exceptions.NonexistentEntityException;
import com.intel.mtwilson.as.data.TblOem;
import com.intel.mtwilson.as.rest.v2.model.Oem;
import com.intel.mtwilson.as.rest.v2.model.OemCollection;
import com.intel.mtwilson.as.rest.v2.model.OemFilterCriteria;
import com.intel.mtwilson.as.rest.v2.model.OemLinks;
import com.intel.mtwilson.as.rest.v2.model.OsFilterCriteria;
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
@Path("/oems")
public class Oems extends AbstractResource<Oem, OemCollection, OemFilterCriteria, OemLinks>{

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
        } catch (IOException ex) {
            Logger.getLogger(Oems.class.getName()).log(Level.SEVERE, null, ex);
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
        } catch (IOException ex) {
            Logger.getLogger(UserCertificates.class.getName()).log(Level.SEVERE, null, ex);
        }
        return oem;
    }

    @Override
    protected void store(Oem item) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void create(Oem item) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void delete(String id) {
        try {
            TblOemJpaController oemJpaController = My.jpa().mwOem();
            if (id != null) {
                TblOem tblOem = oemJpaController.findTblOemByUUID(id);
                if (tblOem != null)
                    oemJpaController.destroy(tblOem.getId());
            }
        } catch (IOException ex) {
            Logger.getLogger(UserCertificates.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NonexistentEntityException ex) {
            Logger.getLogger(Oems.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /*
    @Override
    protected OemFilterCriteria createFilterCriteriaWithId(String id) {
        OemFilterCriteria criteria = new OemFilterCriteria();
        criteria.id = UUID.valueOf(id);
        return criteria;

    }
    */
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
