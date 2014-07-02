/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.repository;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mountwilson.as.common.ASException;
import com.intel.mtwilson.My;
import com.intel.mtwilson.as.controller.TblHostsJpaController;
import com.intel.mtwilson.as.data.TblHosts;
import com.intel.mtwilson.as.rest.v2.model.HostAik;
import com.intel.mtwilson.as.rest.v2.model.HostAikCollection;
import com.intel.mtwilson.as.rest.v2.model.HostAikFilterCriteria;
import com.intel.mtwilson.as.rest.v2.model.HostAikLocator;
import com.intel.mtwilson.i18n.ErrorCode;
import com.intel.mtwilson.jaxrs2.server.resource.SimpleRepository;
import org.apache.shiro.authz.annotation.RequiresPermissions;

/**
 *
 * @author ssbangal
 */
public class HostAikRepository implements SimpleRepository<HostAik, HostAikCollection, HostAikFilterCriteria, HostAikLocator> {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HostAikRepository.class);

    @Override
    @RequiresPermissions("host_aiks:search")    
    public HostAikCollection search(HostAikFilterCriteria criteria) {
        log.debug("HostAik:Search - Got request to search for the roles.");        
        HostAikCollection objCollection = new HostAikCollection();
        try {
            TblHostsJpaController jpaController = My.jpa().mwHosts();
            if (criteria.hostUuid != null) {
                TblHosts obj = jpaController.findHostByUuid(criteria.hostUuid);
                if (obj != null) {
                    objCollection.getAiks().add(convert(obj));
                }
            } // TODO: Need to add the AIKSha1 search criteria later when we have the capability of multiple AIKs
        } catch (ASException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during search for hosts.", ex);
            throw new ASException(ErrorCode.AS_QUERY_HOST_ERROR, ex.getClass().getSimpleName());
        }
        log.debug("HostAik:Search - Returning back {} of results.", objCollection.getAiks().size());                
        return objCollection;
    }

    @Override
    @RequiresPermissions("host_aiks:retrieve")    
    public HostAik retrieve(HostAikLocator locator) {
        if (locator == null || locator.hostUuid == null) { return null; }
        log.debug("HostAik:Retrieve - Got request to retrieve Aik for host with id {}.", locator.hostUuid);                
        String id = locator.hostUuid.toString();
        
        try {
            TblHostsJpaController jpaController = My.jpa().mwHosts();
            TblHosts obj = jpaController.findHostByUuid(id);
            if (obj != null) {
                HostAik hostAik = convert(obj);
                return hostAik;
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
    @RequiresPermissions("host_aiks:store")    
    public void store(HostAik item) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    @RequiresPermissions("host_aiks:create")    
    public void create(HostAik item) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    @RequiresPermissions("host_aiks:delete")    
    public void delete(HostAikLocator locator) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    @RequiresPermissions("host_aiks:delete,search")    
    public void delete(HostAikFilterCriteria criteria) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    private HostAik convert(TblHosts obj) {
        HostAik convObj = new HostAik();
        convObj.setId(UUID.valueOf(obj.getUuid_hex()));
        convObj.setAikPublicKey(obj.getAikPublicKey());
        convObj.setAikSha1(obj.getAikSha1());
        return convObj;
    }
    
}
