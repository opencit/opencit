/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.rest.v2.repository;

import com.intel.mtwilson.api.ApiException;
import com.intel.mtwilson.datatypes.TxtHostRecord;
import com.intel.mtwilson.jaxrs2.server.resource.DocumentRepository;
import com.intel.mtwilson.repository.RepositorySearchException;
import com.intel.mtwilson.tag.common.Global;
import com.intel.mtwilson.tag.model.HostUuid;
import com.intel.mtwilson.tag.model.HostUuidCollection;
import com.intel.mtwilson.tag.model.HostUuidFilterCriteria;
import com.intel.mtwilson.tag.model.HostUuidLocator;
import java.io.IOException;
import java.security.SignatureException;
import java.util.List;
import org.apache.shiro.authz.annotation.RequiresPermissions;

/**
 *
 * @author ssbangal
 */
public class HostUuidRepository implements DocumentRepository<HostUuid, HostUuidCollection, HostUuidFilterCriteria, HostUuidLocator> {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HostUuidRepository.class);
    

    @Override
    @RequiresPermissions("host_uuids:search")         
    public HostUuidCollection search(HostUuidFilterCriteria criteria) {
        HostUuidCollection objCollection = new HostUuidCollection();
        log.debug("HostUuid:Search - Got request to search for the host UUID.");  
        try {
            // We had initially named the hostNameEqualTo search criteria as hostId, which users might confuse with host UUID.
            // So, adding additional search criteria to support host names.
            String hostName;
            if (criteria.hostId != null && !criteria.hostId.isEmpty())
                hostName = criteria.hostId;
            else if (criteria.hostNameEqualTo != null && !criteria.hostNameEqualTo.isEmpty())
                hostName = criteria.hostNameEqualTo;
            else {
                String errorMessage = "HostUuid:Search - Invalid search criteria specified.";
                log.error(errorMessage);
                throw new RepositorySearchException(errorMessage);
            }
                
            List<TxtHostRecord> hostList = Global.mtwilson().queryForHosts(hostName,true);
            if(hostList != null && !hostList.isEmpty()) {
                log.debug("Search for host uuid returned " + hostList.get(0).Hardware_Uuid);
                HostUuid obj = new HostUuid();
                obj.setHardwareUuid(hostList.get(0).Hardware_Uuid);
                objCollection.getHostUuids().add(obj);
            }
        } catch (IOException | ApiException | SignatureException ex) {
            log.error("HostUuid:Search - Error during search for host hardware uuid.", ex);
            throw new RepositorySearchException(ex, criteria);
        }        
        log.debug("HostUuid:Search - Returning back {} of results.", objCollection.getHostUuids().size());                                
        return objCollection;
    }

    @Override
    @RequiresPermissions("host_uuids:retrieve")         
    public HostUuid retrieve(HostUuidLocator locator) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    @RequiresPermissions("host_uuids:store")         
    public void store(HostUuid item) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    @RequiresPermissions("host_uuids:create")         
    public void create(HostUuid item) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    @RequiresPermissions("host_uuids:delete")         
    public void delete(HostUuidLocator locator) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    @RequiresPermissions("host_uuids:delete,search")         
    public void delete(HostUuidFilterCriteria criteria) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
        
}
