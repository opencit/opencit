/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.client.jaxrs;

import com.intel.mtwilson.attestation.client.jaxrs.HostAttestations;
import com.intel.mtwilson.My;
import com.intel.mtwilson.as.rest.v2.model.HostAttestation;
import com.intel.mtwilson.as.rest.v2.model.HostAttestationCollection;
import com.intel.mtwilson.as.rest.v2.model.HostAttestationFilterCriteria;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author ssbangal
 */
public class HostAttestationTest {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HostAttestationTest.class);

    private static HostAttestations client = null;
    
    @BeforeClass
    public static void init() throws Exception {
        client = new HostAttestations(My.configuration().getClientProperties());
    }
    
    @Test
    public void testSearchCollection() {
        HostAttestationFilterCriteria criteria = new HostAttestationFilterCriteria();
        criteria.nameEqualTo = "10.1.71.155";
        HostAttestationCollection objCollection = client.searchHostAttestations(criteria);
        for(HostAttestation obj : objCollection.getHostAttestations()) {
            log.debug("Host Attestation for {} is {} & {}", obj.getHostName(), obj.getHostTrustResponse().trust.bios, obj.getHostTrustResponse().trust.vmm);
        }
        
        String hostSaml = client.searchHostAttestationsSaml(criteria);
        log.debug("Host SAML assertion is {}", hostSaml);

    }

    @Test
    public void testCreate() {
        HostAttestation hostAttestation = new HostAttestation();
        hostAttestation.setHostUuid("de07c08a-7fc6-4c07-be08-0ecb2f803681");
        HostAttestation createHostAttestation = client.createHostAttestation(hostAttestation);
        log.debug("Host Attestation for {} is {} & {}", createHostAttestation.getHostName(), createHostAttestation.getHostTrustResponse().trust.bios, createHostAttestation.getHostTrustResponse().trust.vmm);
        
        String hostSaml = client.createHostAttestationSaml(hostAttestation);
        log.debug("Host SAML assertion is {}", hostSaml);
        
        client.retrieveHostAttestation("32923691-9847-4493-86ee-3036a4f24940");
        client.deleteHostAttestation("32923691-9847-4493-86ee-3036a4f24940");

    }
    
}
