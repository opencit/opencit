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
import com.intel.mtwilson.i18n.ErrorCode;
import com.intel.mtwilson.jaxrs2.server.resource.SimpleRepository;
import org.apache.shiro.authz.annotation.RequiresPermissions;

    
/**
 *
 * @author ssbangal
 */
public class HostTlsPolicyRepository implements SimpleRepository<HostTlsPolicy, HostTlsPolicyCollection, HostTlsPolicyFilterCriteria, HostTlsPolicyLocator> {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HostTlsPolicyRepository.class);
    
    @Override
    @RequiresPermissions("host_tls_policies:search")    
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
        // throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    @RequiresPermissions("host_tls_policies:retrieve")    
    public HostTlsPolicy retrieve(HostTlsPolicyLocator locator) {
        if (locator.hostUuid == null) { return null; }
        String id = locator.hostUuid.toString();
        try {
            TblHostsJpaController jpaController = My.jpa().mwHosts();
            TblHosts obj = jpaController.findHostByUuid(id);
            if (obj != null) {
                HostTlsPolicy htp = convert(obj);
                return htp;
            }
        } catch (ASException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during search for hosts.", ex);
            throw new ASException(ErrorCode.AS_QUERY_HOST_ERROR, ex.getClass().getSimpleName());
        }
        return null;
    }

    @Override
    @RequiresPermissions("host_tls_policies:store")    
    public void store(HostTlsPolicy item) {
        try {
            TblHostsJpaController jpaController = My.jpa().mwHosts();
            TblHosts obj = jpaController.findHostByUuid(item.getHostUuid());
            if (obj != null) {
                obj.setTlsPolicyName(item.getName());
                obj.setTlsKeystore(item.getKeyStore());
                jpaController.edit(obj);
            }
        } catch (ASException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during update of host Tls Policy.", ex);
            throw new ASException(ErrorCode.AS_UPDATE_HOST_ERROR, ex.getClass().getSimpleName());
        }
    }

    @Override
    @RequiresPermissions("host_tls_policies:create")    
    public void create(HostTlsPolicy item) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    @RequiresPermissions("host_tls_policies:delete")    
    public void delete(HostTlsPolicyLocator locator) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    @RequiresPermissions("host_tls_policies:delete,search")    
    public void delete(HostTlsPolicyFilterCriteria criteria) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private HostTlsPolicy convert(TblHosts obj) {
        HostTlsPolicy convObj = new HostTlsPolicy();
        convObj.setHostUuid(obj.getUuid_hex());
        convObj.setName(obj.getTlsPolicyName());
        convObj.setKeyStore(obj.getTlsKeystore());
        return convObj;
    }
    
}
