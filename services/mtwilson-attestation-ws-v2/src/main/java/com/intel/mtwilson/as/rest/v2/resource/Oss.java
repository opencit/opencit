/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.resource;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.My;
import com.intel.mtwilson.as.controller.TblOsJpaController;
import com.intel.mtwilson.as.controller.exceptions.NonexistentEntityException;
import com.intel.mtwilson.as.data.TblOs;
import com.intel.mtwilson.as.rest.v2.model.Os;
import com.intel.mtwilson.as.rest.v2.model.OsCollection;
import com.intel.mtwilson.as.rest.v2.model.OsFilterCriteria;
import com.intel.mtwilson.as.rest.v2.model.OsLinks;
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
@Path("/oss")
public class Oss extends AbstractResource<Os, OsCollection, OsFilterCriteria, OsLinks>{

    @Override
    protected OsCollection search(OsFilterCriteria criteria) {
        OsCollection osCollection = null;
        try {
            TblOsJpaController osJpaController = My.jpa().mwOs();
            if (criteria.id != null) {
                Os os = convert(osJpaController.findTblOsByUUID(criteria.id.toString()));            
                osCollection.getOss().add(os);
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
        } catch (IOException ex) {
            Logger.getLogger(Oems.class.getName()).log(Level.SEVERE, null, ex);
        }
        return osCollection;
    }

    @Override
    protected Os retrieve(String id) {
        Os os = null;
        if (id != null) {
            try {
                TblOsJpaController osJpaController = My.jpa().mwOs();            
                os = convert(osJpaController.findTblOsByUUID(id));
            } catch (IOException ex) {
                Logger.getLogger(Oss.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return os;
    }

    @Override
    protected void store(Os item) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void create(Os item) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void delete(String id) {
        try {
            TblOsJpaController osJpaController = My.jpa().mwOs();
            if (id != null) {
                TblOs tblOs = osJpaController.findTblOsByUUID(id);
                if (tblOs != null)
                    osJpaController.destroy(tblOs.getId());
            }
        } catch (IOException ex) {
            Logger.getLogger(UserCertificates.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NonexistentEntityException ex) {
            Logger.getLogger(Oems.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /*
    @Override
    protected OsFilterCriteria createFilterCriteriaWithId(String id) {
        OsFilterCriteria criteria = new OsFilterCriteria();
        criteria.id = UUID.valueOf(id);
        return criteria;
    }
    */
    @Override
    protected OsCollection createEmptyCollection() {
        return new OsCollection();
    }
    
    private Os convert(TblOs tblOsObj) {
        Os os = new Os();
        if (tblOsObj != null) {
            os.setId(UUID.valueOf(tblOsObj.getUuid_hex()));
            os.setName(tblOsObj.getName());
            os.setVersion(tblOsObj.getVersion());
            os.setDescription(tblOsObj.getDescription());
        } else {
            os = null;
        }
        return os;
    }
    
}
