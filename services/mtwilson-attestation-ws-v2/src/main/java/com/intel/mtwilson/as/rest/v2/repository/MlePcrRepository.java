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
import com.intel.mtwilson.as.rest.v2.model.MleModule;
import com.intel.mtwilson.as.rest.v2.model.MleModuleCollection;
import com.intel.mtwilson.as.rest.v2.model.MleModuleLocator;
import com.intel.mtwilson.as.rest.v2.model.MlePcr;
import com.intel.mtwilson.as.rest.v2.model.MlePcrCollection;
import com.intel.mtwilson.as.rest.v2.model.MlePcrFilterCriteria;
import com.intel.mtwilson.as.rest.v2.model.MlePcrLocator;
import com.intel.mtwilson.i18n.ErrorCode;
import com.intel.mtwilson.datatypes.PCRWhiteList;
import com.intel.mtwilson.jaxrs2.server.resource.SimpleRepository;
import com.intel.mtwilson.wlm.business.MleBO;
import java.util.List;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ssbangal
 */
public class MlePcrRepository implements SimpleRepository<MlePcr, MlePcrCollection, MlePcrFilterCriteria, MlePcrLocator>{

    Logger log = LoggerFactory.getLogger(getClass().getName());
    
    @Override
    @RequiresPermissions("mle_pcrs:search")    
    public MlePcrCollection search(MlePcrFilterCriteria criteria) {
        log.debug("MlePcr:Search - Got request to search for the Mle PCRs.");
        MlePcrCollection objCollection = new MlePcrCollection();
        try {
            TblPcrManifestJpaController jpaController = My.jpa().mwPcrManifest();
            if (criteria.mleUuid != null) {
                List<TblPcrManifest> objList = jpaController.findTblPcrManifestByMleUuid(criteria.mleUuid.toString());
                if (objList != null && !objList.isEmpty()) {
                    if (criteria.filter == false) {
                        for(TblPcrManifest obj : objList) {
                            objCollection.getMlePcrs().add(convert(obj));
                        }                        
                    } else if (criteria.id != null) {
                        for(TblPcrManifest obj : objList) {
                            if (obj.getUuid_hex().equalsIgnoreCase(criteria.id.toString()))
                                objCollection.getMlePcrs().add(convert(obj));
                        }                        
                    } else if (criteria.indexEqualTo != null && !criteria.indexEqualTo.isEmpty()) {
                        for(TblPcrManifest obj : objList) {
                            if (obj.getName().equalsIgnoreCase(criteria.indexEqualTo))
                                objCollection.getMlePcrs().add(convert(obj));
                        }
                    } else if (criteria.valueEqualTo != null && !criteria.valueEqualTo.isEmpty()) {
                        for(TblPcrManifest obj : objList) {
                            if (obj.getValue().equalsIgnoreCase(criteria.valueEqualTo))
                                objCollection.getMlePcrs().add(convert(obj));
                        }                        
                    }
                }                
            }
        } catch (ASException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during search of PCR whitelists for MLE.", ex);
            throw new ASException(ErrorCode.WS_PCR_WHITELIST_RETRIEVAL_ERROR, ex.getClass().getSimpleName());
        }
        log.debug("MlePcr:Search - Returning back {} of results.", objCollection.getMlePcrs().size());
        return objCollection;
    }

    @Override
    @RequiresPermissions("mle_pcrs:retrieve")    
    public MlePcr retrieve(MlePcrLocator locator) {
        if (locator.mleUuid == null || locator.pcrIndex == null) { return null;}
        log.debug("MlePcr:Retrieve - Got request to retrieve Mle PCR with id {}.", locator.pcrIndex);                        
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
    @RequiresPermissions("mle_pcrs:store")    
    public void store(MlePcr item) {
        log.debug("MlePcr:Store - Got request to update Mle PCR with id {}.", item.getPcrIndex().toString());        
        PCRWhiteList obj = new PCRWhiteList();
        try {
            obj.setPcrName(item.getPcrIndex());
            obj.setPcrDigest(item.getPcrValue());

            List<TblPcrManifest> pcrs = My.jpa().mwPcrManifest().findTblPcrManifestByMleUuid(item.getMleUuid());
            for (TblPcrManifest pcr : pcrs) {
                if (pcr.getName().equalsIgnoreCase(item.getPcrIndex())) {
                    new MleBO().updatePCRWhiteList(obj, null, pcr.getUuid_hex());
                }
            }            
            
        } catch (ASException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during PCR whitelist update.", ex);
            throw new ASException(ErrorCode.WS_PCR_WHITELIST_UPDATE_ERROR, ex.getClass().getSimpleName());
        }        
    }

    @Override
    @RequiresPermissions("mle_pcrs:create")    
    public void create(MlePcr item) {
        log.debug("MlePcr:Store - Create a new Mle PCR with id {}.", item.getPcrIndex().toString());        
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
    @RequiresPermissions("mle_pcrs:delete")    
    public void delete(MlePcrLocator locator) {
        if (locator.mleUuid == null || locator.pcrIndex == null) { return ;}
        log.debug("MlePcr:Create - Got request to create a new Mle PCR.");
        String mleUuid = locator.mleUuid.toString();
        String pcrIndex = locator.pcrIndex;
        try {
            List<TblPcrManifest> pcrs = My.jpa().mwPcrManifest().findTblPcrManifestByMleUuid(mleUuid);
            for (TblPcrManifest pcr : pcrs) {
                if (pcr.getName().equalsIgnoreCase(pcrIndex)) {
                    log.debug("About to delete pcr index {} for mle with uuid {}.", locator.pcrIndex, locator.mleUuid.toString());
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
    @RequiresPermissions("mle_pcrs:delete,search")    
    public void delete(MlePcrFilterCriteria criteria) {
        log.debug("MlePcr:Delete - Got request to delete Mle PCR by search criteria.");        
        MlePcrCollection objCollection = search(criteria);
        try { 
            for (MlePcr obj : objCollection.getMlePcrs()) {
                MlePcrLocator locator = new MlePcrLocator();
                locator.mleUuid = UUID.valueOf(obj.getMleUuid());
                locator.pcrIndex = obj.getPcrIndex();
                delete(locator);
            }
        } catch (Exception ex) {
            log.error("Error during MlePcr deletion.", ex);
            throw new WebApplicationException("Please see the server log for more details.", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }
    
}
