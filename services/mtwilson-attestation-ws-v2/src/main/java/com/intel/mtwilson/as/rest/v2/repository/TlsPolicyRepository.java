/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.repository;

import com.intel.mountwilson.as.common.ASException;
import com.intel.mtwilson.My;
import com.intel.mtwilson.as.controller.TblHostsJpaController;
import com.intel.mtwilson.as.data.TblHosts;
import com.intel.mtwilson.as.rest.v2.model.TlsPolicy;
import com.intel.mtwilson.as.rest.v2.model.TlsPolicyCollection;
import com.intel.mtwilson.as.rest.v2.model.TlsPolicyFilterCriteria;
import com.intel.mtwilson.as.rest.v2.model.TlsPolicyLocator;
import com.intel.mtwilson.datatypes.ErrorCode;
import com.intel.mtwilson.jersey.resource.SimpleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

    
/**
 *
 * @author ssbangal
 */
public class TlsPolicyRepository implements SimpleRepository<TlsPolicy, TlsPolicyCollection, TlsPolicyFilterCriteria, TlsPolicyLocator> {

    Logger log = LoggerFactory.getLogger(getClass().getName());
    
    @Override
    public TlsPolicyCollection search(TlsPolicyFilterCriteria criteria) {
        TlsPolicyCollection objCollection = new TlsPolicyCollection();
        try {
            TblHostsJpaController jpaController = My.jpa().mwHosts();
            if (criteria.hostUuid != null) {
                TblHosts obj = jpaController.findHostByUuid(criteria.hostUuid.toString());
                if (obj != null) {
                    objCollection.getTlsPolicies().add(convert(obj));
                }
            }
        } catch (ASException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during search for hosts.", ex);
            throw new ASException(ErrorCode.AS_QUERY_HOST_ERROR, ex.getClass().getSimpleName());
        }
        return objCollection;
    }

    @Override
    public TlsPolicy retrieve(TlsPolicyLocator id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void store(TlsPolicy item) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void create(TlsPolicy item) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void delete(String id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    private TlsPolicy convert(TblHosts obj) {
        TlsPolicy convObj = new TlsPolicy();
        convObj.setHostUuid(obj.getUuid_hex());
        convObj.setHostName(obj.getName());
        convObj.setName(obj.getTlsPolicyName());
        convObj.setKeyStore(obj.getTlsKeystore());
        return convObj;
    }
    
}
