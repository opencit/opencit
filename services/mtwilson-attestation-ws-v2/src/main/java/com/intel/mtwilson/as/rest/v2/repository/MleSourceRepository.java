/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.repository;

import com.intel.mtwilson.as.rest.v2.resource.*;
import com.intel.mountwilson.as.common.ASException;
import com.intel.mtwilson.My;
import com.intel.mtwilson.as.controller.MwMleSourceJpaController;
import com.intel.mtwilson.as.data.MwMleSource;
import com.intel.mtwilson.as.rest.v2.model.MleSource;
import com.intel.mtwilson.as.rest.v2.model.MleSourceCollection;
import com.intel.mtwilson.as.rest.v2.model.MleSourceFilterCriteria;
import com.intel.mtwilson.as.rest.v2.model.MleSourceLocator;
import com.intel.mtwilson.datatypes.ErrorCode;
import com.intel.mtwilson.jersey.resource.AbstractJsonapiResource;
import com.intel.mtwilson.jersey.resource.AbstractResource;
import com.intel.mtwilson.jersey.resource.AbstractSimpleResource;
import com.intel.mtwilson.jersey.resource.SimpleRepository;
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
public class MleSourceRepository implements SimpleRepository<MleSource, MleSourceCollection, MleSourceFilterCriteria, MleSourceLocator>{
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MleSources2.class);
    
    @Override
    public MleSourceCollection search(MleSourceFilterCriteria criteria) {
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
    public MleSource retrieve(MleSourceLocator locator) {
        if( locator.id == null ) { return null; }
        String id = locator.id.toString();
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
    public void store(MleSource item) {
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
    public void create(MleSource item) {
        com.intel.mtwilson.datatypes.MleSource obj = new com.intel.mtwilson.datatypes.MleSource();
        try {
            obj.setHostName(item.getName());
            obj.setMleData(null);
            new MleBO().addMleSource(obj, item.getId().toString(), item.getMleUuid());
        } catch (ASException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during MLE source host mapping creation.", ex);
            throw new ASException(ErrorCode.WS_MLE_HOST_MAP_CREATE_ERROR, ex.getClass().getSimpleName());
        }
    }

    @Override
    public void delete(MleSourceLocator locator) {
        if (locator == null || locator.id == null) { return; }
        try {
            new MleBO().deleteMleSource(null, null, null, null, null, locator.id.toString());
        } catch (ASException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during MLE source host mapping deletion.", ex);
            throw new ASException(ErrorCode.WS_MLE_HOST_MAP_DELETE_ERROR, ex.getClass().getSimpleName());
        }
    }

    private MleSource convert(MwMleSource obj) {
        MleSource convObj = new MleSource();
        convObj.setMleUuid(obj.getUuid_hex());
        convObj.setName(obj.getHostName());
        return convObj;
    }

    @Override
    public void delete(MleSourceFilterCriteria criteria) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
