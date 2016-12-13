/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.policy.impl;

import com.intel.mtwilson.My;
import com.intel.mtwilson.as.business.AssetTagCertBO;
import java.util.HashSet;
import com.intel.mtwilson.as.data.MwAssetTagCertificate;
import com.intel.mtwilson.as.data.TblHosts;
import com.intel.mtwilson.model.Bios;
import com.intel.mtwilson.model.Vmm;
import com.intel.mtwilson.policy.*;
import com.intel.mtwilson.policy.impl.vendor.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Example for setting up a whitelist:
 * 
 * HostTrustPolicyFactory hostTrustPolicyFactory = new HostTrustPolicyFactory(entityManagerFactory);
 * List<TrustPolicy> trustPolicy = createTrustPolicyWhitelistFromHost(tblHostsRecord, hostReport);
 * add general or non-vendor-specific trust policies, then:
 * saveTrustPolicyForMle(....);
 * 
 * 
 * Example for loading and using the trust policy "whitelist" for a host:
 * HostTrustPolicyFactory hostTrustPolicyFactory = new HostTrustPolicyFactory(entityManagerFactory);
 * TrustPolicy trustPolicy = loadTrustPolicyForHost(tblHostsRecord)
 * PolicyEngine policyEngine = new PolicyEngine();
 * 
 * @author jbuhacoff
 */
public class HostTrustPolicyManager {
 
    private Logger log = LoggerFactory.getLogger(getClass());
    
    private EntityManagerFactory entityManagerFactory;
    private JpaPolicyReader reader;

    private Map<String,VendorHostTrustPolicyFactory> vendorFactoryMap = new HashMap<String,VendorHostTrustPolicyFactory>();
    //private Logger log = LoggerFactory.getLogger(getClass());
    public HostTrustPolicyManager(EntityManagerFactory entityManagerFactory) {
        reader = new JpaPolicyReader(entityManagerFactory);
        // we initialize the map with the known vendors; but this could also be done through IoC
        vendorFactoryMap.put("intel", new IntelHostTrustPolicyFactory(reader));
        vendorFactoryMap.put("citrix", new CitrixHostTrustPolicyFactory(reader));
        vendorFactoryMap.put("vmware", new VmwareHostTrustPolicyFactory(reader));
        vendorFactoryMap.put("microsoft", new MicrosoftHostTrustPolicyFactory(reader));
        vendorFactoryMap.put("intel-da", new IntelTpmDaHostTrustPolicyFactory(reader));
        this.entityManagerFactory = entityManagerFactory;
    }
    
    /**
     * Optional to call this -  the HostTrustPolicyFactory default constructor already
     * creates a map with Intel, Vmware, and Citrix vendor-specific factories.
     * It is recommended to supply an EnumMap instance
     * @param map of vendor host agent factories
     */
    public void setVendorFactoryMap(Map<String,VendorHostTrustPolicyFactory> map) {
        vendorFactoryMap = map;
    }
        
    
    public EntityManagerFactory getEntityManagerFactory() {
        return entityManagerFactory;
    }
    
    
    /**
     * CALL THIS FROM ATTESTATION SERVICE HostTrustBO TO GET THE TRUST POLICY FOR VERIFY HOST TRUST
     * 
     * In Mt Wilson 1.1 and 1.2, each host has exactly one policy assigned and it is stored in the database
     * tables this way:
     * mw_hosts (link to bios and vmm records in mw_mle table)
     * mw_mle (contains name and version of mle)
     * mw_pcr_manifest (link to either bios or vmm record in mw_mle table)
     * mw_module_manifest (link to either bios or vmm record in mw_mle table)
     * mw_host_specific_manifest (link to a module record in mw_module_manifest table)
     * 
     * Each record in mw_hosts stores a single host.
     * Each record in mw_pcr_manifest stores a single PCR index and value.
     * Each record in mw_module_manifest stores a single module value along with vmware-specific attributes
     * Each record in mw_host_specific_manifest stores a single module value
     * 
     * This method reads data from the schema above and instantiates a Policy object containing
     * a set of rules that reflects that data for a single host - that is, data from mw_host_specific_manifest
     * is automatically merged with the corresponding data from mw_module_manifest.
     * 
     * Precondition is that MLEs, PCR Manifests, Module Manifests, and Host Specific (Module) Manifests are
     * already defined in the database.
     * 
     * The whitelist service and automation functions in management service store that data directly into
     * the database and are not aware of this class or the Policy class.
     * 
     * NOTE:  if there are no whitelist data available for a host, so that no rules are generated,
     * the empty policy will always evaluate to "untrusted" 
     * 
     * This method delegates to vendor-specific factories for the work of instantiating the Rules, but
     * it does organize the work into bios, vmm, and location and adds the host-specific module values.
     * 
     */
    public Policy loadTrustPolicyForHost(TblHosts host, String hostId) {
        VendorHostTrustPolicyFactory factory = getVendorHostTrustPolicyFactoryForHost(host);        
        HashSet<Rule> rules = new HashSet<>();
        // only add bios policy if the host is linked with a bios mle
        if( host.getBiosMleId() != null ) {
            Bios bios = new Bios(host.getBiosMleId().getName(), host.getBiosMleId().getVersion(), host.getBiosMleId().getOemId().getName());
            rules.addAll(factory.loadTrustRulesForBios(bios, host));
        }
        // only add vmm policy if the host is linked with a vmm mle
        if( host.getVmmMleId() != null ) {
            Vmm vmm = new Vmm(host.getVmmMleId().getName(), host.getVmmMleId().getVersion(), host.getVmmMleId().getOsId().getName(), host.getVmmMleId().getOsId().getVersion());
            rules.addAll(factory.loadTrustRulesForVmm(vmm,host));
        }
         // only add location policy if the host is expected to be somewhere specific... otherwise, an empty location will result in a policy that can't be met
        //if( host.getLocation() != null && !host.getLocation().trim().isEmpty() ) {
        //    rules.addAll(factory.loadTrustRulesForLocation(host.getLocation(), host));
        //}
        
        // Add the rules for the asset tag verification
        try {
            // Since this function can be called either during attestation or during verification of mle matches during which
            // the host is actually not registered, we need to check for that explicitly.
            if (host.getId() != null && host.getId() != 0) {
                // Need to add the certificate whitelist only if the host is associated with a valid asset tag certificate.
                AssetTagCertBO atagCertBO = new AssetTagCertBO();
                MwAssetTagCertificate atagCertForHost = atagCertBO.findValidAssetTagCertForHost(host.getId());            
                if (atagCertForHost != null) { 
                    rules.addAll(factory.loadTrustRulesForAssetTag(atagCertForHost, host));
                }
                else {
                    log.info("Asset tag certificate is either not present or valid for host {}.", host.getName());
                }
            }
        } catch (Exception ex) {
            log.error("Exception when looking up the asset tag whitelist.", ex);
            // We cannot do anything ... just log the error and proceed
            log.info("Error during look up of asset tag certificates for the host {}", host.getName());
        }
        
        Policy policy = new Policy(String.format("Host trust policy for host with AIK %s", hostId), rules);
        return policy;
    }
    
    public Policy loadTrustPolicyForMLEVerification(TblHosts host, String hostId) {
        VendorHostTrustPolicyFactory factory = getVendorHostTrustPolicyFactoryForHost(host);        
        HashSet<Rule> rules = new HashSet<Rule>();
        // only add bios policy if the host is linked with a bios mle
        if( host.getBiosMleId() != null ) {
            Bios bios = new Bios(host.getBiosMleId().getName(), host.getBiosMleId().getVersion(), host.getBiosMleId().getOemId().getName());
            rules.addAll(factory.loadTrustRulesForBios(bios, host));
        }
        // only add vmm policy if the host is linked with a vmm mle
        if( host.getVmmMleId() != null ) {
            Vmm vmm = new Vmm(host.getVmmMleId().getName(), host.getVmmMleId().getVersion(), host.getVmmMleId().getOsId().getName(), host.getVmmMleId().getOsId().getVersion());
            rules.addAll(factory.loadComparisonRulesForVmm(vmm,host));
        }
        Policy policy = new Policy(String.format("Host trust policy for host with AIK %s", hostId), rules);
        return policy;
    }

    /*
    public Policy createWhitelistFromHost(TblHosts host) throws IOException {
        HostAgentFactory agentFactory = new HostAgentFactory();
        HostAgent agent = agentFactory.getHostAgent(host);
        HostReport hostReport = new HostReport();
        hostReport.pcrManifest = agent.getPcrManifest();
        hostReport.variables = agent.getHostAttributes();
        VendorHostTrustPolicyFactory factory = getVendorHostTrustPolicyFactoryForHost(host);
//        Set<Rule> rules = factory.createWhitelistRules(host);
//        Set<Rule> rules = factory.createHostSpecificRules(host);
        Policy policy = new Policy("Automatically generated policy from "+host.getName(), rules);
        return policy;
    }
    */
    
    /**
     * GIVEN A POLICY, CREATES HOST-SPECIFIC RECORDS IN THE DATABASE CORRESPONDING TO THE POLICY SO THAT
     * WHEN YOU CALL loadTrustPolicyForHost(host,hostId) THOSE HOST-SPECIFIC RECORDS WILL BE INCLUDED AUTOMATICALLY.
     * CALL THIS WHEN REGISTERING A NEW HOST.
     * @param host
     * @param policy 
     * @return  
     */
    /*
    public void storeHostRulesForPolicy(TblHosts host, Policy policy) {
        
    }
    */
    /*
    public Set<Rule> loadTrustPolicy(TblMle bios, TblMle vmm) {
        throw new UnsupportedOperationException("todo");
    }
    */
    protected VendorHostTrustPolicyFactory getVendorHostTrustPolicyFactoryForHost(TblHosts host) {
        if( host.getAddOnConnectionInfo() == null ) {
            throw new IllegalArgumentException("Connection info missing");
        }
        for(String vendorProtocol : vendorFactoryMap.keySet()) {
            String prefix = vendorProtocol.toLowerCase()+":"; // "INTEL" or "intel" becomes "intel:"
            if( host.getAddOnConnectionInfo().startsWith(prefix) ) {
                String vendorKey = vendorProtocol;
                boolean useDaMode = "2.0".equals(host.getTpmVersion()) && host.getAddOnConnectionInfo().startsWith("intel:");
                if(useDaMode) {
                    vendorKey += "-da";
                }
                VendorHostTrustPolicyFactory factory = vendorFactoryMap.get(vendorKey);
                if( factory != null ) {
                    return factory;
                }
            }
        }
        throw new UnsupportedOperationException("No policy factory registered for this host");        
    }
    
    /**
     * CALL THIS METHOD FROM MANAGEMENT SERVICE AUTOMATING SETUP OF A NEW HOST'S WHITELIST FROM HOST INFO
     * 
     * Call this method when you are registering a new host and want to GENERATE a trust
     * policy for that host.  The vendor-specific factory will be called to do it so you
     * get the best available set of rules.
     * 
     * This method is used to generate a set of rules from a host given its vendor and
     * built-in knowledge about how that vendor's PCRs are extended. 
     * If you need to load an existing trust policy for a host, use loadTrustPolicyForHost() instead.
     * 
     * @param host only required in order to detect the vendor (using connection string) and delegate to the appropriate factory
     * @param hostReport the information to be used for generating the rules
     * @return 
     */
//    protected Set<Rule> generateTrustRulesForHost(TblHosts host, HostReport hostReport) {
//        VendorHostTrustPolicyFactory factory = getVendorHostTrustPolicyFactoryForHost(host);
//        return factory.generateTrustRulesForHost(hostReport);
//    }
    
    /*
    // implies that only the host-specific parts will be stored ????   it's not clear from this that someone should call storetrustPolicyforHost AND forBios AND forVmm...  should have just one call for "store trust policy for host" that does all three, and a separate "store trust policy" that only does the mle's (with variables for host-specific things) and doesn't write anything host-specific
    public void storeTrustPolicyForHost(TblHosts host, Policy trustPolicy) {
        throw new UnsupportedOperationException("Cannot save:: not implemented yet");
        
    }
    
    // implies that anything host-specific will be identified and written out with variables / placeholders
    public void storeTrustPolicyForBios(TblMle host, Policy trustPolicy) {
        throw new UnsupportedOperationException("Cannot save: not implemented yet");
        
    }
    
    // implies that anything host-specific will be identified and written out with variables / placeholders
    public void storeTrustPolicyForVmm(TblMle host, Policy trustPolicy) {
        throw new UnsupportedOperationException("Cannot save: not implemented yet");
        
    }
*/
    
    
    ///////////////////////////////////// BEYOND THIS POINT,  CODE TAKEN FROM THE OLD  "GKV FACTORY"  ///// NEED TO LOAD FROM DB, THEN TURN INTO POLICIES !!!
    
    /**
     * Creates a list of PcrMatchesConstant policies for the given bios.
     * @param bios
     * @return 
     */
    /*
    public List<Rule> loadTrustRulesForBios(Bios bios, TblHosts tblHosts) {
        throw new UnsupportedOperationException("need to call vendor-specific code....");
    }

    public List<Rule> loadTrustRulesForVmm(Vmm vmm, TblHosts tblHosts) {
        throw new UnsupportedOperationException("need to call vendor-specific code....");
        
//        ArrayList<Rule> list = new ArrayList<Rule>();
//        list.add(loadPcrMatchesConstantRulesForVmm(vmmMle, tblHosts)); // in whitelistutil
    }
    */
    /*
    public List<Rule> loadTrustRulesForLocation(TblHosts tblHosts) {
        throw new UnsupportedOperationException("need to call vendor-specific code....");
//        TblLocationPcr locationPcr = locationPcrJpaController.findTblLocationPcr(tblHosts.getLocationId());
//        ArrayList<Rule> list = new ArrayList<Rule>();
//        PcrIndex pcrIndex = HostBO.LOCATION_PCR;
//        Sha1Digest pcrValue = new Sha1Digest(locationPcr.getPcrValue());
//        PcrMatchesConstant rule = new PcrMatchesConstant(new Pcr(pcrIndex, pcrValue));
//        rule.setMarkers(Marker.LOCATION.name());
//        list.add(rule);
//        return list;
    }
    */
}
