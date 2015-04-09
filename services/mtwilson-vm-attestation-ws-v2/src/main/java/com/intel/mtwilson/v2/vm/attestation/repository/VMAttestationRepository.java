/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.v2.vm.attestation.repository;

import com.intel.mtwilson.My;
import com.intel.mtwilson.agent.HostAgent;
import com.intel.mtwilson.agent.HostAgentFactory;
import com.intel.mtwilson.as.controller.TblHostsJpaController;
import com.intel.mtwilson.as.data.TblHosts;
import com.intel.mtwilson.v2.vm.attestation.model.VMAttestation;
import com.intel.mtwilson.v2.vm.attestation.model.VMAttestationCollection;
import com.intel.mtwilson.v2.vm.attestation.model.VMAttestationFilterCriteria;
import com.intel.mtwilson.v2.vm.attestation.model.VMAttestationLocator;
import com.intel.mtwilson.jaxrs2.server.resource.DocumentRepository;
import com.intel.mtwilson.trustagent.model.VMAttestationResponse;
import com.intel.mtwilson.repository.RepositorySearchException;
import org.apache.shiro.authz.annotation.RequiresPermissions;


/**
 *
 * @author ssbangal
 */
public class VMAttestationRepository implements DocumentRepository<VMAttestation, VMAttestationCollection, VMAttestationFilterCriteria, VMAttestationLocator> {
    
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(VMAttestationRepository.class);
            
    @Override
    @RequiresPermissions("vm_attestations:search")    
    public VMAttestationCollection search(VMAttestationFilterCriteria criteria) {
        log.debug("HostAttestation:Search - Got request to search for VM attestations.");        
        VMAttestationCollection objCollection = new VMAttestationCollection();
        try {
            if (criteria.hostName != null && !criteria.hostName.isEmpty() && 
                    criteria.vmInstanceId != null && !criteria.vmInstanceId.isEmpty()) {

                TblHostsJpaController jpaController = My.jpa().mwHosts();
                TblHosts obj = jpaController.findByName(criteria.hostName);
                if (obj != null) {
                    HostAgentFactory factory = new HostAgentFactory();
                    HostAgent agent = factory.getHostAgent(obj);
                    VMAttestationResponse vmAttestationReport = agent.getVMAttestationStatus(criteria.vmInstanceId);
                    objCollection.getVMAttestations().add(convert(vmAttestationReport, criteria));
                }
                
            }
        } catch (Exception ex) {
            log.error("Host:Retrieve - Error during search for hosts.", ex);
            throw new RepositorySearchException(ex, criteria);
        }
        
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
   
    private VMAttestation convert(VMAttestationResponse obj, VMAttestationFilterCriteria criteria) {
        VMAttestation convObj = new VMAttestation();
        convObj.setHostName(criteria.hostName);
        convObj.setVmInstanceId(criteria.vmInstanceId);
        convObj.setTrustStatus(obj.isTrustStatus());
        return convObj;
    }
    
}
