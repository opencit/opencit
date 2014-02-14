/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.repository;

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
import com.intel.mtwilson.as.rest.v2.model.MleLocator;
import com.intel.mtwilson.datatypes.ErrorCode;
import com.intel.mtwilson.datatypes.MleData;
import com.intel.mtwilson.jersey.resource.SimpleRepository;
import com.intel.mtwilson.wlm.business.MleBO;
import java.util.Collection;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ssbangal
 */
public class MleRepository implements SimpleRepository<Mle, MleCollection, MleFilterCriteria, MleLocator> {

    private Logger log = LoggerFactory.getLogger(getClass().getName());

    @Override
    public MleCollection search(MleFilterCriteria criteria) {
        MleCollection mleCollection = new MleCollection();
        try {
            TblMleJpaController jpaController = My.jpa().mwMle();
            if (criteria.id != null) {
                TblMle tblMle = jpaController.findTblMleByUUID(criteria.id.toString());
                if (tblMle != null) {
                    mleCollection.getMles().add(convert(tblMle));
                }
            } else if (criteria.nameContains != null && !criteria.nameContains.isEmpty()) {
                List<TblMle> mleList = jpaController.findByNameLike(criteria.nameContains);
                if (mleList != null && !mleList.isEmpty()) {
                    for(TblMle mleObj : mleList) {
                        mleCollection.getMles().add(convert(mleObj));
                    }
                }                
            }  else if (criteria.nameEqualTo != null && !criteria.nameEqualTo.isEmpty()) {
                List<TblMle> mleList = jpaController.findByName(criteria.nameEqualTo);
                if (mleList != null && !mleList.isEmpty()) {
                    for(TblMle mleObj : mleList) {
                        mleCollection.getMles().add(convert(mleObj));
                    }
                }                
            } else if (criteria.osUuid != null && !criteria.osUuid.isEmpty()) {
                List<TblMle> mleList = jpaController.findByOsUuid(criteria.osUuid);
                if (mleList != null && !mleList.isEmpty()) {
                    for(TblMle mleObj : mleList) {
                        mleCollection.getMles().add(convert(mleObj));
                    }
                }                
            } else if (criteria.oemUuid != null && !criteria.oemUuid.isEmpty()) {
                List<TblMle> mleList = jpaController.findByOemUuid(criteria.oemUuid);
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
    public Mle retrieve(MleLocator locator) {
        if( locator == null || locator.id == null ) { return null; }
        String id = locator.id.toString();
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
    public void store(Mle item) {
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
    public void create(Mle item) {
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
    public void delete(MleLocator locator) {
        if( locator == null || locator.id == null ) { return; }
        String id = locator.id.toString();
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
    public void delete(MleFilterCriteria criteria) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    private Mle convert(TblMle tblMleObj) {
        Mle mle = new Mle();
        mle.setId(UUID.valueOf(tblMleObj.getUuid_hex()));
        mle.setName(tblMleObj.getName());
        mle.setVersion(tblMleObj.getVersion());
        mle.setAttestationType(Mle.AttestationType.valueOf(tblMleObj.getAttestationType()));
        mle.setMleType(Mle.MleType.valueOf(tblMleObj.getMLEType()));
        if (tblMleObj.getMLEType().equalsIgnoreCase(Mle.MleType.BIOS.name())) {
            mle.setOemUuid(tblMleObj.getOemId().getUuid_hex());
            mle.setOsUuid(null);
        } else {
            mle.setOemUuid(null);
            mle.setOsUuid(tblMleObj.getOsId().getUuid_hex());
        }
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
