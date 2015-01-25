/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.v2.vm.attestation.repository;

import com.intel.mountwilson.as.common.ASConfig;
import com.intel.mtwilson.v2.vm.attestation.model.VMAttestation;
import com.intel.mtwilson.v2.vm.attestation.model.VMAttestationCollection;
import com.intel.mtwilson.v2.vm.attestation.model.VMAttestationFilterCriteria;
import com.intel.mtwilson.v2.vm.attestation.model.VMAttestationLocator;
import com.intel.mtwilson.jaxrs2.server.resource.DocumentRepository;
import org.apache.shiro.authz.annotation.RequiresPermissions;


/**
 *
 * @author ssbangal
 */
public class VMAttestationRepository implements DocumentRepository<VMAttestation, VMAttestationCollection, VMAttestationFilterCriteria, VMAttestationLocator> {
    
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(VMAttestationRepository.class);
    
    private static final int DEFAULT_CACHE_VALIDITY_SECS = 3600;
    private static final int CACHE_VALIDITY_SECS = ASConfig.getConfiguration().getInt("saml.validity.seconds", DEFAULT_CACHE_VALIDITY_SECS);
        
    @Override
    @RequiresPermissions("vm_attestations:search")    
    public VMAttestationCollection search(VMAttestationFilterCriteria criteria) {
        log.debug("HostAttestation:Search - Got request to search for VM attestations.");        
        VMAttestationCollection objCollection = new VMAttestationCollection();
        VMAttestation obj = new VMAttestation();
        obj.setHostName(criteria.hostName);
        obj.setVmInstanceId(criteria.vmInstanceId);
        obj.setTrustStatus(true);
        objCollection.getVMAttestations().add(obj);
        return objCollection;
    }

    @Override
    @RequiresPermissions("vm_attestations:retrieve")    
    public VMAttestation retrieve(VMAttestationLocator locator) {
        if (locator == null || locator.id == null) { return null;}
        log.debug("HostAttestation:Retrieve - Got request to retrieve the host attestation with id {}.", locator.id.toString());        
        return null;
    }
        
    @Override
    @RequiresPermissions("host_attestations:store")    
    public void store(VMAttestation item) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    @RequiresPermissions("host_attestations:create")    
    public void create(VMAttestation item) {
        log.debug("HostAttestation:Create - Got request to create host attestation with id {}.", item.getId().toString());  
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    @RequiresPermissions("host_attestations:delete")    
    public void delete(VMAttestationLocator locator) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    @RequiresPermissions("host_attestations:delete")    
    public void delete(VMAttestationFilterCriteria criteria) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
   
}
