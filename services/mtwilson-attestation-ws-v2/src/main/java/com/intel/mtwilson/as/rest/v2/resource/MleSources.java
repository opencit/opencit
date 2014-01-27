/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.resource;

import com.intel.mountwilson.as.common.ASException;
import com.intel.mtwilson.My;
import com.intel.mtwilson.as.controller.MwMleSourceJpaController;
import com.intel.mtwilson.as.data.MwMleSource;
import com.intel.mtwilson.as.rest.v2.model.MleSource;
import com.intel.mtwilson.as.rest.v2.model.MleSourceCollection;
import com.intel.mtwilson.as.rest.v2.model.MleSourceFilterCriteria;
import com.intel.mtwilson.as.rest.v2.model.MleSourceLinks;
import com.intel.mtwilson.datatypes.ErrorCode;
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
@Path("/mle-sources")
public class MleSources extends AbstractResource<MleSource, MleSourceCollection, MleSourceFilterCriteria, MleSourceLinks>{

    Logger log = LoggerFactory.getLogger(getClass().getName());

    @Override
    protected MleSourceCollection search(MleSourceFilterCriteria criteria) {
        MleSourceCollection mleSourceCollection = new MleSourceCollection();
        
        try {
            MwMleSourceJpaController jpaController = My.jpa().mwMleSource();
            if (criteria.id != null) {
                MwMleSource obj = jpaController.findByUuid(criteria.id.toString());
                if (obj != null) {
                    mleSourceCollection.getMleSources().add(convert(obj));
                }
            } else if (criteria.mleUuid != null) {
                MwMleSource obj = jpaController.findByMleUuid(criteria.mleUuid.toString());            
                if (obj != null) {
                    mleSourceCollection.getMleSources().add(convert(obj));
                }
            } else if (criteria.nameEqualTo != null && !criteria.nameEqualTo.isEmpty()) {
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
        } catch (ASException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during MLE Source search.", ex);
            throw new ASException(ErrorCode.WS_MLE_HOST_MAP_RETRIEVAL_ERROR, ex.getClass().getSimpleName());
        }
        return mleSourceCollection;
    }

    @Override
    protected MleSource retrieve(String id) {
        if( id == null ) { return null; }
        try {
            MwMleSourceJpaController jpaController = My.jpa().mwMleSource();
            MwMleSource obj = jpaController.findByUuid(id);
            if (obj != null) {
                MleSource mleSource = convert(obj);
                return mleSource;            
            } 
        } catch (ASException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during MLE Source search.", ex);
            throw new ASException(ErrorCode.WS_MLE_HOST_MAP_RETRIEVAL_ERROR, ex.getClass().getSimpleName());
        }
        return null;
    }

    @Override
    protected void store(MleSource item) {
        com.intel.mtwilson.datatypes.MleSource obj = new com.intel.mtwilson.datatypes.MleSource();
        try {
            obj.setHostName(item.getName());
            new MleBO().updateMleSource(obj, item.getId().toString());
        } catch (ASException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during MLE source host mapping update.", ex);
            throw new ASException(ErrorCode.WS_MLE_HOST_MAP_UPDATE_ERROR, ex.getClass().getSimpleName());
        }
    }

    @Override
    protected void create(MleSource item) {
        com.intel.mtwilson.datatypes.MleSource obj = new com.intel.mtwilson.datatypes.MleSource();
        try {
            obj.setHostName(item.getName());
            obj.setMleData(null);
            new MleBO().addMleSource(obj, item.getId().toString());
        } catch (ASException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during MLE source host mapping creation.", ex);
            throw new ASException(ErrorCode.WS_MLE_HOST_MAP_CREATE_ERROR, ex.getClass().getSimpleName());
        }
    }

    @Override
    protected void delete(String id) {
        try {
            new MleBO().deleteMleSource(null, null, null, null, null, id);
        } catch (ASException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during MLE source host mapping deletion.", ex);
            throw new ASException(ErrorCode.WS_MLE_HOST_MAP_DELETE_ERROR, ex.getClass().getSimpleName());
        }
    }

    @Override
    protected MleSourceCollection createEmptyCollection() {
        return new MleSourceCollection();
    }

    private MleSource convert(MwMleSource obj) {
        MleSource convObj = new MleSource();
        convObj.setMleUuid(obj.getUuid_hex());
        convObj.setName(obj.getHostName());
        return convObj;
    }
    
}
