/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.repository;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mountwilson.as.common.ASException;
import com.intel.mtwilson.My;
import com.intel.mtwilson.as.controller.TblPcrManifestJpaController;
import com.intel.mtwilson.as.data.TblPcrManifest;
import com.intel.mtwilson.as.rest.v2.model.MlePcr;
import com.intel.mtwilson.as.rest.v2.model.MlePcrCollection;
import com.intel.mtwilson.as.rest.v2.model.MlePcrFilterCriteria;
import com.intel.mtwilson.as.rest.v2.model.MlePcrLocator;
import com.intel.mtwilson.datatypes.ErrorCode;
import com.intel.mtwilson.datatypes.PCRWhiteList;
import com.intel.mtwilson.jersey.resource.SimpleRepository;
import com.intel.mtwilson.wlm.business.MleBO;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ssbangal
 */
public class MlePcrRepository implements SimpleRepository<MlePcr, MlePcrCollection, MlePcrFilterCriteria, MlePcrLocator>{

    Logger log = LoggerFactory.getLogger(getClass().getName());
    
    @Override
    public MlePcrCollection search(MlePcrFilterCriteria criteria) {
        MlePcrCollection objCollection = new MlePcrCollection();
        try {
            TblPcrManifestJpaController jpaController = My.jpa().mwPcrManifest();
            if (criteria.id != null) {
                TblPcrManifest obj = jpaController.findTblPcrManifestByUuid(criteria.id.toString());
                if (obj != null) {
                    objCollection.getMlePcrs().add(convert(obj));
                }
            } else if (criteria.mleUuid != null) {
                List<TblPcrManifest> objList = jpaController.findTblPcrManifestByMleUuid(criteria.mleUuid.toString());
                if (objList != null && !objList.isEmpty()) {
                    for(TblPcrManifest obj : objList) {
                        objCollection.getMlePcrs().add(convert(obj));
                    }
                }                
            } else if (criteria.indexEqualTo != null && !criteria.indexEqualTo.isEmpty()) {
                List<TblPcrManifest> objList = jpaController.findTblPcrManifestByPcrName(criteria.indexEqualTo);
                if (objList != null && !objList.isEmpty()) {
                    for(TblPcrManifest obj : objList) {
                        objCollection.getMlePcrs().add(convert(obj));
                    }
                }                
            } else if (criteria.valueEqualTo != null && !criteria.valueEqualTo.isEmpty()) {
                List<TblPcrManifest> objList = jpaController.findByPcrValue(criteria.valueEqualTo);
                if (objList != null && !objList.isEmpty()) {
                    for(TblPcrManifest obj : objList) {
                        objCollection.getMlePcrs().add(convert(obj));
                    }
                }                
            }
        } catch (ASException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during search of PCR whitelists for MLE.", ex);
            throw new ASException(ErrorCode.WS_PCR_WHITELIST_RETRIEVAL_ERROR, ex.getClass().getSimpleName());
        }
        return objCollection;
    }

    @Override
    public MlePcr retrieve(MlePcrLocator locator) {
        if (locator.mleUuid == null || locator.pcrIndex == null) { return null;}
        String mleUuid = locator.mleUuid.toString();
        String pcrIndex = locator.pcrIndex;
        try {
            TblPcrManifestJpaController jpaController = My.jpa().mwPcrManifest();
            List<TblPcrManifest> pcrs = jpaController.findTblPcrManifestByMleUuid(mleUuid);
            for (TblPcrManifest pcr : pcrs) {
                if (pcr.getName().equalsIgnoreCase(pcrIndex)) {
                    MlePcr obj = convert(pcr);
                    return obj;
                }
            }
        } catch (ASException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during retrieval of PCR whitelists for MLE.", ex);
            throw new ASException(ErrorCode.WS_PCR_WHITELIST_RETRIEVAL_ERROR, ex.getClass().getSimpleName());
        }
        return null;
    }

    @Override
    public void store(MlePcr item) {
        PCRWhiteList obj = new PCRWhiteList();
        try {
            obj.setPcrName(item.getPcrIndex());
            obj.setPcrDigest(item.getPcrValue());
            new MleBO().updatePCRWhiteList(obj, null, item.getId().toString());
        } catch (ASException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during PCR whitelist update.", ex);
            throw new ASException(ErrorCode.WS_PCR_WHITELIST_UPDATE_ERROR, ex.getClass().getSimpleName());
        }        
    }

    @Override
    public void create(MlePcr item) {
        PCRWhiteList obj = new PCRWhiteList();
        try {
            obj.setPcrName(item.getPcrIndex());
            obj.setPcrDigest(item.getPcrValue());
            log.debug("About to add pcr {} with value {} & UUID {} for MLE {}", obj.getPcrName(), obj.getPcrDigest(), item.getId().toString(), item.getMleUuid());
            new MleBO().addPCRWhiteList(obj, null, item.getId().toString(), item.getMleUuid());
        } catch (ASException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during PCR whitelist creation.", ex);
            throw new ASException(ErrorCode.WS_PCR_WHITELIST_CREATE_ERROR, ex.getClass().getSimpleName());
        }        
    }

    @Override
    public void delete(MlePcrLocator locator) {
        if (locator.mleUuid == null || locator.pcrIndex == null) { return ;}
        String mleUuid = locator.mleUuid.toString();
        String pcrIndex = locator.pcrIndex;
        try {
            TblPcrManifestJpaController jpaController = My.jpa().mwPcrManifest();
            List<TblPcrManifest> pcrs = jpaController.findTblPcrManifestByMleUuid(mleUuid);
            for (TblPcrManifest pcr : pcrs) {
                if (pcr.getName().equalsIgnoreCase(pcrIndex)) {
                    new MleBO().deletePCRWhiteList(null, null, null, null, null, null, pcr.getUuid_hex());
                }
            }            
        } catch (ASException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during PCR whitelist deletion.", ex);
            throw new ASException(ErrorCode.WS_PCR_WHITELIST_DELETE_ERROR, ex.getClass().getSimpleName());
        }
    }
    
    private MlePcr convert(TblPcrManifest obj) {
        MlePcr convObj = new MlePcr();
        convObj.setId(UUID.valueOf(obj.getUuid_hex()));
        convObj.setMleUuid(obj.getUuid_hex());
        convObj.setPcrIndex(obj.getName());
        convObj.setPcrValue(obj.getValue());
        convObj.setDescription(obj.getPCRDescription());
        return convObj;
    }

    @Override
    public void delete(MlePcrFilterCriteria criteria) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
