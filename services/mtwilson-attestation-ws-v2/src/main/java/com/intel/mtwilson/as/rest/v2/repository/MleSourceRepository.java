/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.repository;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.as.rest.v2.resource.*;
import com.intel.mountwilson.as.common.ASException;
import com.intel.mtwilson.My;
import com.intel.mtwilson.as.controller.MwMleSourceJpaController;
import com.intel.mtwilson.as.data.MwMleSource;
import com.intel.mtwilson.as.rest.v2.model.MleSource;
import com.intel.mtwilson.as.rest.v2.model.MleSourceCollection;
import com.intel.mtwilson.as.rest.v2.model.MleSourceFilterCriteria;
import com.intel.mtwilson.as.rest.v2.model.MleSourceLocator;
import com.intel.mtwilson.i18n.ErrorCode;
import com.intel.mtwilson.jaxrs2.server.resource.SimpleRepository;
import com.intel.mtwilson.wlm.business.MleBO;
import org.apache.shiro.authz.annotation.RequiresPermissions;

/**
 *
 * @author ssbangal
 */
public class MleSourceRepository implements SimpleRepository<MleSource, MleSourceCollection, MleSourceFilterCriteria, MleSourceLocator>{
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MleSources.class);
    
    @Override
    @RequiresPermissions("mle_sources:search")    
    public MleSourceCollection search(MleSourceFilterCriteria criteria) {
        MleSourceCollection mleSourceCollection = new MleSourceCollection();
        
        try {
            MwMleSourceJpaController jpaController = My.jpa().mwMleSource();
            if (criteria.mleUuid != null) {
                MwMleSource obj = jpaController.findByMleUuid(criteria.mleUuid.toString());            
                if (obj != null) {
                    mleSourceCollection.getMleSources().add(convert(obj));
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
    @RequiresPermissions("mle_sources:retrieve")    
    public MleSource retrieve(MleSourceLocator locator) {
        if( locator.mleUuid == null ) { return null; }
        String mleUuid = locator.mleUuid.toString();
        try {
            MwMleSourceJpaController jpaController = My.jpa().mwMleSource();
            MwMleSource obj = jpaController.findByMleUuid(mleUuid);
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
    @RequiresPermissions("mle_sources:store")    
    public void store(MleSource item) {
        com.intel.mtwilson.datatypes.MleSource obj = new com.intel.mtwilson.datatypes.MleSource();
        try {
            obj.setHostName(item.getName());
            new MleBO().updateMleSource(obj, item.getMleUuid());
        } catch (ASException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during MLE source host mapping update.", ex);
            throw new ASException(ErrorCode.WS_MLE_HOST_MAP_UPDATE_ERROR, ex.getClass().getSimpleName());
        }
    }

    @Override
    @RequiresPermissions("mle_sources:create")    
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
    @RequiresPermissions("mle_sources:delete")    
    public void delete(MleSourceLocator locator) {
        if (locator == null || locator.mleUuid == null) { return; }
        try {
            new MleBO().deleteMleSource(null, null, null, null, null, locator.mleUuid.toString());
        } catch (ASException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during MLE source host mapping deletion.", ex);
            throw new ASException(ErrorCode.WS_MLE_HOST_MAP_DELETE_ERROR, ex.getClass().getSimpleName());
        }
    }

    private MleSource convert(MwMleSource obj) {
        MleSource convObj = new MleSource();
        convObj.setMleUuid(obj.getMle_uuid_hex());
        convObj.setName(obj.getHostName());
        convObj.setId(UUID.valueOf(obj.getUuid_hex()));
        return convObj;
    }

    @Override
    @RequiresPermissions("mle_sources:delete,search")    
    public void delete(MleSourceFilterCriteria criteria) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
