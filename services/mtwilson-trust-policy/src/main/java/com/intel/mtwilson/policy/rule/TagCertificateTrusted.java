/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.policy.rule;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.intel.mtwilson.policy.BaseRule;
import com.intel.mtwilson.policy.HostReport;
import com.intel.mtwilson.policy.RuleResult;
import com.intel.mtwilson.policy.fault.TagCertificateExpired;
import com.intel.mtwilson.policy.fault.TagCertificateMissing;
import com.intel.mtwilson.policy.fault.TagCertificateNotTrusted;
import com.intel.mtwilson.policy.fault.TagCertificateNotYetValid;
import com.intel.mtwilson.tag.model.X509AttributeCertificate;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Date;

/**
 *
 * @author jbuhacoff
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown=true)
public class TagCertificateTrusted extends BaseRule {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TagCertificateTrusted.class);
    private X509Certificate[] trustedAuthorityCerts;

    protected TagCertificateTrusted() { } // for desearializing jackson
    
    public TagCertificateTrusted(X509Certificate[] trustedAuthorityCerts) {
        this.trustedAuthorityCerts = trustedAuthorityCerts;
    }

    @Override
    public RuleResult apply(HostReport hostReport) {
        RuleResult report = new RuleResult(this);
        if (hostReport.tagCertificate == null) {
            log.debug("Tag certificate is NULL");
            report.fault(new TagCertificateMissing());
        } else {
            Date today = new Date();
            boolean validCaSignature = false;
            for (int i = 0; i < trustedAuthorityCerts.length && !validCaSignature; i++) {
                X509Certificate ca = trustedAuthorityCerts[i];
                try {
                    if (hostReport.tagCertificate.getIssuer().equalsIgnoreCase(ca.getIssuerX500Principal().getName())) {
                        if (hostReport.tagCertificate.isValid(ca)) {
                            // NOTE:  CA certificate must be valid for the start date and end date of the tag certificate's validity - we don't let a CA generate certs for a period when the CA itself is expired.
                            //        if this rule is too strict in practice we can remove it
                            log.debug("Verifying CA start date : {} with tag certificate start date : {}", ca.getNotBefore(), hostReport.tagCertificate.getNotBefore());
                            ca.checkValidity(hostReport.tagCertificate.getNotBefore());
                            log.debug("Verifying CA end date : {} with tag certificate end date : {}", ca.getNotAfter(), hostReport.tagCertificate.getNotAfter());
                            ca.checkValidity(hostReport.tagCertificate.getNotAfter());
                            validCaSignature = true;
                        } else {
                            log.debug("TagCertificate is not valid");
                        }
                    } else {
                        log.debug("Issuer name mismatch : {} vs {}", hostReport.tagCertificate.getIssuer(), ca.getIssuerX500Principal().getName());
                    }
                } catch (Exception e) { //CertificateExpiredException | CertificateNotYetValidException e) {
                    log.debug("Failed to verify tag signature with CA: {}", e.getMessage()); // suppressing because maybe another cert in the list is a valid signer
                }
            }
            if (!validCaSignature) {
                log.debug("Adding fault for invalid tagcertificate");
                report.fault(new TagCertificateNotTrusted());
            } else {
                // we found a trusted ca and validated the tag certificate; now check the validity period of the tag certificate
                if (today.before(hostReport.tagCertificate.getNotBefore())) {
                    log.debug("Adding fault for tagCertificate not yet valid");
                    report.fault(new TagCertificateNotYetValid(hostReport.tagCertificate.getNotBefore()));

                }
                if (today.after(hostReport.tagCertificate.getNotAfter())) {
                    log.debug("Adding fault for tagCertificate already expired");
                    report.fault(new TagCertificateExpired(hostReport.tagCertificate.getNotAfter()));
                }
            }
        }
        return report;
    }

    @Override
    public String toString() {
        return "AIK certificate is signed by trusted authority";
    }
}
