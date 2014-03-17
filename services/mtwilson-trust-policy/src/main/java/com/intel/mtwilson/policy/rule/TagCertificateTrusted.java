/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.policy.rule;

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
public class TagCertificateTrusted extends BaseRule {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TagCertificateTrusted.class);
    private X509Certificate[] trustedAuthorityCerts;

    public TagCertificateTrusted(X509Certificate[] trustedAuthorityCerts) {
        this.trustedAuthorityCerts = trustedAuthorityCerts;
    }

    @Override
    public RuleResult apply(HostReport hostReport) {
        RuleResult report = new RuleResult(this);
        if (hostReport.tagCertificate == null) {
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
                            ca.checkValidity(hostReport.tagCertificate.getNotBefore());
                            ca.checkValidity(hostReport.tagCertificate.getNotAfter());
                            // TODO check if the privacy ca cert is self-signed... if it's not self-signed  we should check for a path leading to a known root ca in the root ca's file
                            validCaSignature = true;
                        }
                    }
                } catch (CertificateExpiredException | CertificateNotYetValidException e) {
                    log.debug("Failed to verify tag signature with CA: {}", e.getMessage()); // suppressing because maybe another cert in the list is a valid signer
                }
            }
            if (!validCaSignature) {
                report.fault(new TagCertificateNotTrusted());
            } else {
                // we found a trusted ca and validated the tag certificate; now check the validity period of the tag certificate
                if (today.before(hostReport.tagCertificate.getNotBefore())) {
                    report.fault(new TagCertificateNotYetValid(hostReport.tagCertificate.getNotBefore()));

                }
                if (today.after(hostReport.tagCertificate.getNotAfter())) {
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
