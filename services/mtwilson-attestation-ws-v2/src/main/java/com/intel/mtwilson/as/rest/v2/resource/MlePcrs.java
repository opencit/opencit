/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.resource;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mountwilson.as.common.ASException;
import com.intel.mtwilson.My;
import com.intel.mtwilson.as.controller.TblPcrManifestJpaController;
import com.intel.mtwilson.as.data.TblPcrManifest;
import com.intel.mtwilson.as.rest.v2.model.MlePcr;
import com.intel.mtwilson.as.rest.v2.model.MlePcrCollection;
import com.intel.mtwilson.as.rest.v2.model.MlePcrFilterCriteria;
import com.intel.mtwilson.as.rest.v2.model.MlePcrLinks;
import com.intel.mtwilson.datatypes.ErrorCode;
import com.intel.mtwilson.datatypes.PCRWhiteList;
import com.intel.mtwilson.jersey.resource.AbstractResource;
import com.intel.mtwilson.launcher.ws.ext.V2;
import com.intel.mtwilson.wlm.business.MleBO;
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
@Path("/mle-pcrs")
public class MlePcrs extends AbstractResource<MlePcr, MlePcrCollection, MlePcrFilterCriteria, MlePcrLinks>{

    Logger log = LoggerFactory.getLogger(getClass().getName());
    
    @Override
    protected MlePcrCollection search(MlePcrFilterCriteria criteria) {
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
            }else if (criteria.nameEqualTo != null && !criteria.nameEqualTo.isEmpty()) {
                List<TblPcrManifest> objList = jpaController.findTblPcrManifestByPcrName(criteria.nameEqualTo);
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
    protected MlePcr retrieve(String id) {
        if (id == null) { return null;}
        try {
            TblPcrManifestJpaController jpaController = My.jpa().mwPcrManifest();
            TblPcrManifest obj = jpaController.findTblPcrManifestByUuid(id);   
            if (obj != null) {
                MlePcr pcrObj = convert(obj);
                return pcrObj;
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
    protected void store(MlePcr item) {
        PCRWhiteList obj = new PCRWhiteList();
        try {
            obj.setPcrName(item.getPcrName());
            obj.setPcrDigest(item.getPcrDigest());
            new MleBO().updatePCRWhiteList(obj, null, item.getId().toString());
        } catch (ASException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during PCR whitelist update.", ex);
            throw new ASException(ErrorCode.WS_PCR_WHITELIST_UPDATE_ERROR, ex.getClass().getSimpleName());
        }        
    }

    @Override
    protected void create(MlePcr item) {
        PCRWhiteList obj = new PCRWhiteList();
        try {
            obj.setPcrName(item.getPcrName());
            obj.setPcrDigest(item.getPcrDigest());
            new MleBO().addPCRWhiteList(obj, null, item.getId().toString(), item.getMleUuid());
        } catch (ASException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during PCR whitelist creation.", ex);
            throw new ASException(ErrorCode.WS_PCR_WHITELIST_CREATE_ERROR, ex.getClass().getSimpleName());
        }        
    }

    @Override
    protected void delete(String id) {
        try {
            new MleBO().deletePCRWhiteList(null, null, null, null, null, null, id);
        } catch (ASException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during PCR whitelist deletion.", ex);
            throw new ASException(ErrorCode.WS_PCR_WHITELIST_DELETE_ERROR, ex.getClass().getSimpleName());
        }
    }

    @Override
    protected MlePcrCollection createEmptyCollection() {
        return new MlePcrCollection();
    }
    
    private MlePcr convert(TblPcrManifest obj) {
        MlePcr convObj = new MlePcr();
        convObj.setId(UUID.valueOf(obj.getUuid_hex()));
        convObj.setMleUuid(obj.getUuid_hex());
        convObj.setPcrName(obj.getName());
        convObj.setPcrDigest(obj.getValue());
        convObj.setDescription(obj.getPCRDescription());
        return convObj;
    }
    
}
