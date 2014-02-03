/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.repository;

import com.intel.mountwilson.as.common.ASException;
import com.intel.mtwilson.My;
import com.intel.mtwilson.as.controller.TblHostsJpaController;
import com.intel.mtwilson.as.data.TblHosts;
import com.intel.mtwilson.as.rest.v2.model.HostTlsPolicy;
import com.intel.mtwilson.as.rest.v2.model.HostTlsPolicyCollection;
import com.intel.mtwilson.as.rest.v2.model.HostTlsPolicyFilterCriteria;
import com.intel.mtwilson.as.rest.v2.model.HostTlsPolicyLocator;
import com.intel.mtwilson.datatypes.ErrorCode;
import com.intel.mtwilson.jersey.resource.SimpleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

    
/**
 *
 * @author ssbangal
 */
public class HostTlsPolicyRepository implements SimpleRepository<HostTlsPolicy, HostTlsPolicyCollection, HostTlsPolicyFilterCriteria, HostTlsPolicyLocator> {

    Logger log = LoggerFactory.getLogger(getClass().getName());
    
    @Override
    public HostTlsPolicyCollection search(HostTlsPolicyFilterCriteria criteria) {
        HostTlsPolicyCollection objCollection = new HostTlsPolicyCollection();
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
    public HostTlsPolicy retrieve(HostTlsPolicyLocator id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void store(HostTlsPolicy item) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void create(HostTlsPolicy item) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void delete(String id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    private HostTlsPolicy convert(TblHosts obj) {
        HostTlsPolicy convObj = new HostTlsPolicy();
        convObj.setHostUuid(obj.getUuid_hex());
        convObj.setHostName(obj.getName());
        convObj.setName(obj.getTlsPolicyName());
        convObj.setKeyStore(obj.getTlsKeystore());
        return convObj;
    }
    
}
