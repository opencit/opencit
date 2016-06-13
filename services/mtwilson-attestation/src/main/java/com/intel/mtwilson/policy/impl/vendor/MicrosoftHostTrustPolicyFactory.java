/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.policy.impl.vendor;

import com.intel.mtwilson.as.data.MwAssetTagCertificate;
import com.intel.mtwilson.policy.impl.JpaPolicyReader;
import com.intel.mtwilson.as.data.TblHosts;
import com.intel.dcsg.cpg.x509.X509Util;
import com.intel.mtwilson.model.Bios;
import com.intel.mtwilson.model.Vmm;
import com.intel.mtwilson.policy.Rule;
import com.intel.mtwilson.policy.impl.TrustMarker;
import com.intel.mtwilson.policy.impl.VendorHostTrustPolicyFactory;
import com.intel.mtwilson.policy.rule.AikCertificateTrusted;
import com.intel.mtwilson.util.ResourceFinder;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.io.IOUtils;

/**
 * Needs to create a policy to check AIK Certificate is signed by trusted Privacy CA
 * @author jbuhacoff
 */
public class MicrosoftHostTrustPolicyFactory implements VendorHostTrustPolicyFactory {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MicrosoftHostTrustPolicyFactory.class);
    private X509Certificate[] cacerts = null;
    private JpaPolicyReader reader;
    public MicrosoftHostTrustPolicyFactory(JpaPolicyReader util) {
        this.reader = util;
    }

    @Override
    public Set<Rule> loadTrustRulesForBios(Bios bios, TblHosts host) {
        if( cacerts == null ) {
            cacerts = loadTrustedAikCertificateAuthorities();
        }
        HashSet<Rule> rules = new HashSet<>();
        AikCertificateTrusted aikcert = new AikCertificateTrusted(cacerts);
        aikcert.setMarkers(TrustMarker.BIOS.name());
        rules.add(aikcert);
        Set<Rule> pcrConstantRules = reader.loadPcrMatchesConstantRulesForBios(bios, host);
        rules.addAll(pcrConstantRules);
        return rules;
    }

    @Override
    public Set<Rule> loadTrustRulesForVmm(Vmm vmm, TblHosts host) {
        if( cacerts == null ) {
            cacerts = loadTrustedAikCertificateAuthorities();
        }
        HashSet<Rule> rules = new HashSet<>();
        AikCertificateTrusted aikcert = new AikCertificateTrusted(cacerts);
        aikcert.setMarkers(TrustMarker.VMM.name());
        rules.add(aikcert);
        // first, load the list of pcr's marked for this host's vmm mle 
        Set<Rule> pcrConstantRules = reader.loadPcrMatchesConstantRulesForVmm(vmm, host);
        rules.addAll(pcrConstantRules);

        // Next we need to add all the modules
        if( host.getVmmMleId().getRequiredManifestList().contains("19") ) {
            Set<Rule> pcrEventLogRules = reader.loadPcrEventLogIncludesRuleForVmm(vmm, host);
            rules.addAll(pcrEventLogRules);
            
            // Add rules to verify the meaurement log which would contain modules for attesting application/data
            Set<Rule> xmlMeasurementLogRules = reader.loadXmlMeasurementLogRuleForVmm(vmm, host);
            rules.addAll(xmlMeasurementLogRules);
            
        }        

        return rules;    
    }

    // Since the open source tBoot does not support PCR 22, we will not support it here.
    @Override
    public Set<Rule> loadTrustRulesForLocation(String location, TblHosts host) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Set<Rule> loadComparisonRulesForVmm(Vmm vmm, TblHosts host) {
        HashSet<Rule> rules = new HashSet<>();
        // first, load the list of pcr's marked for this host's vmm mle 
        Set<Rule> pcrConstantRules = reader.loadPcrMatchesConstantRulesForVmm(vmm, host);
        rules.addAll(pcrConstantRules);

        // Next we need to add all the modules
        if( host.getVmmMleId().getRequiredManifestList().contains("19") ) {
            Set<Rule> pcrEventLogRules = reader.loadPcrEventLogIncludesRuleForVmm(vmm, host);
            rules.addAll(pcrEventLogRules);
        }
        return rules;    
    }

    private X509Certificate[] loadTrustedAikCertificateAuthorities() {
        HashSet<X509Certificate> pcaList = new HashSet<>();
        try (InputStream privacyCaIn = new FileInputStream(ResourceFinder.getFile("PrivacyCA.list.pem"))) {
            List<X509Certificate> privacyCaCerts = X509Util.decodePemCertificates(IOUtils.toString(privacyCaIn));
            pcaList.addAll(privacyCaCerts);
            //IOUtils.closeQuietly(privacyCaIn);
            log.debug("Added {} certificates from PrivacyCA.list.pem", privacyCaCerts.size());
        } catch(Exception ex) {
            log.warn("Cannot load PrivacyCA.list.pem", ex);            
        }
        
        try (InputStream privacyCaIn = new FileInputStream(ResourceFinder.getFile("PrivacyCA.pem"))) {
            X509Certificate privacyCaCert = X509Util.decodeDerCertificate(IOUtils.toByteArray(privacyCaIn));
            pcaList.add(privacyCaCert);
            //IOUtils.closeQuietly(privacyCaIn);
            log.debug("Added certificate from PrivacyCA.pem");
        } catch(Exception ex) {
            log.warn("Cannot load PrivacyCA.pem", ex);            
        }
        X509Certificate[] cas = pcaList.toArray(new X509Certificate[0]);
        return cas;
    }
    
    @Override
    public Set<Rule> loadTrustRulesForAssetTag(MwAssetTagCertificate atagCert, TblHosts host) {
        return reader.loadMatchesRulesForAssetTag(atagCert, host);
    }

}
