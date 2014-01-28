/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.resource;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mountwilson.as.common.ASException;
import com.intel.mtwilson.My;
import com.intel.mtwilson.as.controller.TblMleJpaController;
import com.intel.mtwilson.as.controller.TblOemJpaController;
import com.intel.mtwilson.as.controller.TblOsJpaController;
import com.intel.mtwilson.as.data.MwMleSource;
import com.intel.mtwilson.as.data.TblMle;
import com.intel.mtwilson.as.data.TblOem;
import com.intel.mtwilson.as.data.TblOs;
import com.intel.mtwilson.as.rest.v2.model.Mle;
import com.intel.mtwilson.as.rest.v2.model.MleCollection;
import com.intel.mtwilson.as.rest.v2.model.MleFilterCriteria;
import com.intel.mtwilson.as.rest.v2.model.MleLinks;
import com.intel.mtwilson.datatypes.ErrorCode;
import com.intel.mtwilson.datatypes.MleData;
import com.intel.mtwilson.jersey.resource.AbstractResource;
import com.intel.mtwilson.launcher.ws.ext.V2;
import com.intel.mtwilson.wlm.business.MleBO;
import java.util.Collection;
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
@Path("/mles")
public class Mles extends AbstractResource<Mle, MleCollection, MleFilterCriteria, MleLinks> {

    Logger log = LoggerFactory.getLogger(getClass().getName());

    @Override
    protected MleCollection search(MleFilterCriteria criteria) {
        MleCollection mleCollection = new MleCollection();
        try {
            TblMleJpaController jpaController = My.jpa().mwMle();
            if (criteria.id != null) {
                TblMle tblMle = jpaController.findTblMleByUUID(criteria.id.toString());
                if (tblMle != null) {
                    mleCollection.getMles().add(convert(tblMle));
                }
            } else if (criteria.nameContains != null && !criteria.nameContains.isEmpty()) {
                List<TblMle> mleList = jpaController.findMleByNameSearchCriteria(criteria.nameContains);
                if (mleList != null && !mleList.isEmpty()) {
                    for(TblMle mleObj : mleList) {
                        mleCollection.getMles().add(convert(mleObj));
                    }
                }                
            }
        } catch (ASException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during MLE search.", ex);
            throw new ASException(ErrorCode.WS_MLE_RETRIEVAL_ERROR, ex.getClass().getSimpleName());
        }
        return mleCollection;
    }

    @Override
    protected Mle retrieve(String id) {
        if( id == null ) { return null; }
        try {
            TblMleJpaController jpaController = My.jpa().mwMle();
            TblMle tblMle = jpaController.findTblMleByUUID(id); 
            if (tblMle != null) {
                Mle mle = convert(tblMle);
                return mle;
            } 
        } catch (ASException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during MLE search.", ex);
            throw new ASException(ErrorCode.WS_MLE_RETRIEVAL_ERROR, ex.getClass().getSimpleName());
        }
        return null;
    }

    @Override
    protected void store(Mle item) {
        try {
            // Only the description and the PCR white lists are editable.
            MleData obj = new MleData();

            obj.setDescription(item.getDescription());
            obj.setManifestList(item.getMleManifests());
                
            new MleBO().updateMle(obj, item.getId().toString());
                        
        } catch (ASException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during MLE search.", ex);
            throw new ASException(ErrorCode.WS_MLE_RETRIEVAL_ERROR, ex.getClass().getSimpleName());
        }
    }

    @Override
    protected void create(Mle item) {
        try {
            // Since the new APIs accept the UUID of the OEM and OS associated with the MLE, we need to verify the UUIDs
            // then form the MleData object before calling into the business layer.
            MleData obj = new MleData();
            obj.setName(item.getName());
            obj.setVersion(item.getVersion());
            obj.setDescription(item.getDescription());
            obj.setAttestationType(item.getAttestationType().toString());
            obj.setMleType(item.getMleType().toString());
            obj.setManifestList(item.getMleManifests());
                        
            // If the MLE type is BIOS, then the user has to have specified the OEM UUID
            if (item.getMleType() == Mle.MleType.BIOS) {
                TblOemJpaController oemJpaController = My.jpa().mwOem();
                TblOem tblOem = oemJpaController.findTblOemByUUID(item.getOemUuid());
                if (tblOem != null) {
                    obj.setOemName(tblOem.getName());
                } else {
                    log.error("The OEM specified with UUID {} does not exist.", item.getOemUuid());
                    throw new ASException(ErrorCode.WS_OEM_DOES_NOT_EXIST, item.getOemUuid());
                }                
            } else {
                TblOsJpaController osJpaController = My.jpa().mwOs();            
                TblOs tblOs = osJpaController.findTblOsByUUID(item.getOsUuid());
                if (tblOs != null) {
                    obj.setOsName(tblOs.getName());
                    obj.setOsVersion(tblOs.getVersion());
                } else {
                    log.error("The OS specified with UUID {} does not exist.", item.getOsUuid());
                    throw new ASException(ErrorCode.WS_OS_DOES_NOT_EXIST, item.getOsUuid());
                }                                
            }
            
            // Call into the business layer to create the MLE
            new MleBO().addMLe(obj, item.getId().toString());
            
        } catch (ASException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during MLE creation.", ex);
            throw new ASException(ErrorCode.WS_MLE_CREATE_ERROR, ex.getClass().getSimpleName());
        }
    }

    @Override
    protected void delete(String id) {
        try {
            new MleBO().deleteMle(null, null, null, null, null, id);
        } catch (ASException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during MLE delete.", ex);
            throw new ASException(ErrorCode.WS_MLE_DELETE_ERROR, ex.getClass().getSimpleName());
        }
    }

    @Override
    protected MleCollection createEmptyCollection() {
        return new MleCollection();
    }
    
    
    private Mle convert(TblMle tblMleObj) {
        Mle mle = new Mle();
        mle.setId(UUID.valueOf(tblMleObj.getUuid_hex()));
        mle.setName(tblMleObj.getName());
        mle.setVersion(tblMleObj.getVersion());
        mle.setOemUuid(tblMleObj.getOemId().getUuid_hex());
        mle.setOsUuid(tblMleObj.getOsId().getUuid_hex());
        mle.setDescription(tblMleObj.getDescription());   
        // Since there will be only one entry per MLE in the MleSource table, we will try to get it and return it back to the caller
        Collection<MwMleSource> mwMleSourceCollection = tblMleObj.getMwMleSourceCollection();
        if (mwMleSourceCollection != null && !mwMleSourceCollection.isEmpty()) {
            MwMleSource mleSource = (MwMleSource) mwMleSourceCollection.toArray()[0];
            mle.setSource(mleSource.getHostName());
        }
        return mle;
    }
    
}
