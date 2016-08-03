/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.repository;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.My;
import com.intel.mtwilson.as.controller.TblPcrManifestJpaController;
import com.intel.mtwilson.as.data.TblPcrManifest;
import com.intel.mtwilson.as.rest.v2.model.MlePcr;
import com.intel.mtwilson.as.rest.v2.model.MlePcrCollection;
import com.intel.mtwilson.as.rest.v2.model.MlePcrFilterCriteria;
import com.intel.mtwilson.as.rest.v2.model.MlePcrLocator;
import com.intel.mtwilson.datatypes.PCRWhiteList;
import com.intel.mtwilson.jaxrs2.server.resource.DocumentRepository;
import com.intel.mtwilson.repository.RepositoryCreateException;
import com.intel.mtwilson.repository.RepositoryDeleteException;
import com.intel.mtwilson.repository.RepositoryException;
import com.intel.mtwilson.repository.RepositoryRetrieveException;
import com.intel.mtwilson.repository.RepositorySearchException;
import com.intel.mtwilson.repository.RepositoryStoreException;
import com.intel.mtwilson.wlm.business.MleBO;
import java.util.List;
import org.apache.shiro.authz.annotation.RequiresPermissions;

/**
 *
 * @author ssbangal
 */
public class MlePcrRepository implements DocumentRepository<MlePcr, MlePcrCollection, MlePcrFilterCriteria, MlePcrLocator>{

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MlePcrRepository.class);
    
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
        } catch (Exception ex) {
            log.error("MlePcr:Search - Error during search of PCR whitelists for MLE.", ex);
            throw new RepositorySearchException(ex, criteria);
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
        } catch (Exception ex) {
            log.error("MlePcr:Retrieve - Error during retrieval of PCR whitelists for MLE.", ex);
            throw new RepositoryRetrieveException(ex, locator);
        }
        return null;
    }

    @Override
    @RequiresPermissions("mle_pcrs:store")    
    public void store(MlePcr item) {
        log.debug("MlePcr:Store - Got request to update Mle PCR with id {}.", item.getPcrIndex()); 
        MlePcrLocator locator = new MlePcrLocator();
        locator.mleUuid = UUID.valueOf(item.getMleUuid());
        locator.pcrIndex = item.getPcrIndex();
        
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
            log.debug("MlePcr:Store - Updated the MlePcrs for MLE with id {} successfully.", item.getMleUuid()); 
            
        } catch (Exception ex) {
            log.error("MlePcr:Store - Error during PCR whitelist update.", ex);
            throw new RepositoryStoreException(ex, locator);
        }        
    }

    @Override
    @RequiresPermissions("mle_pcrs:create")    
    public void create(MlePcr item) {
        log.debug("MlePcr:Create - Create a new Mle PCR with id {}.", item.getPcrIndex());        
        MlePcrLocator locator = new MlePcrLocator();
        locator.mleUuid = UUID.valueOf(item.getMleUuid());
        locator.pcrIndex = item.getPcrIndex();

        PCRWhiteList obj = new PCRWhiteList();
        try {
            obj.setPcrName(item.getPcrIndex());
            obj.setPcrDigest(item.getPcrValue());
            log.debug("MlePcr:Create - About to add pcr {} with value {} & UUID {} for MLE {}", obj.getPcrName(), obj.getPcrDigest(), item.getId().toString(), item.getMleUuid());
            new MleBO().addPCRWhiteList(obj, null, item.getId().toString(), item.getMleUuid());
        } catch (Exception ex) {
            log.error("MlePcr:Create - Error during PCR whitelist creation.", ex);
            throw new RepositoryCreateException(ex, locator);
        }        
    }

    @Override
    @RequiresPermissions("mle_pcrs:delete")    
    public void delete(MlePcrLocator locator) {
        if (locator.mleUuid == null || locator.pcrIndex == null) { return ;}
        log.debug("MlePcr:Delete - Got request to create a new Mle PCR.");
        String mleUuid = locator.mleUuid.toString();
        String pcrIndex = locator.pcrIndex;
        try {
            List<TblPcrManifest> pcrs = My.jpa().mwPcrManifest().findTblPcrManifestByMleUuid(mleUuid);
            for (TblPcrManifest pcr : pcrs) {
                if (pcr.getName().equalsIgnoreCase(pcrIndex)) {
                    log.debug("MlePcr:Delete - About to delete pcr index {} for mle with uuid {}.", locator.pcrIndex, locator.mleUuid.toString());
                    new MleBO().deletePCRWhiteList(null, null, null, null, null, null, null, pcr.getUuid_hex());
                }
            }            
        } catch (Exception ex) {
            log.error("MlePcr:Delete - Error during PCR whitelist deletion.", ex);
            throw new RepositoryDeleteException(ex, locator);
        }
    }
    
    private MlePcr convert(TblPcrManifest obj) {
        MlePcr convObj = new MlePcr();
        convObj.setId(UUID.valueOf(obj.getUuid_hex()));
        convObj.setMleUuid(obj.getMle_uuid_hex());
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
        } catch (RepositoryException re) {
            throw re;
        } catch (Exception ex) {
            log.error("MlePcr:Delete - Error during PCR whitelist deletion.", ex);
            throw new RepositoryDeleteException(ex);
        }
    }
    
}
