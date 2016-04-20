/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.client.jaxrs;

import com.intel.mtwilson.attestation.client.jaxrs.HostTagCertificates;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.My;
import com.intel.mtwilson.as.rest.v2.model.TagCertificate;
import com.intel.mtwilson.as.rest.v2.model.TagCertificateCollection;
import com.intel.mtwilson.as.rest.v2.model.TagCertificateFilterCriteria;
import java.security.cert.CertificateException;
import org.apache.commons.codec.binary.Base64;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author ssbangal
 */
public class HostTagCertificateTest {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HostTagCertificateTest.class);

    private static HostTagCertificates client = null;
    
    @BeforeClass
    public static void init() throws Exception {
        client = new HostTagCertificates(My.configuration().getClientProperties());
    }
    
    @Test
    public void testSearchCollection() {
        TagCertificateFilterCriteria criteria = new TagCertificateFilterCriteria();
        criteria.hostUuid = UUID.valueOf("064866ea-620d-11e0-b1a9-001e671043c4");
        TagCertificateCollection objCollection = client.searchHostTagCertificates(criteria);
        for(TagCertificate obj : objCollection.getTagCertificates()) {
            log.debug("TagCertificate name {}", obj.getId().toString());
        }
    }
    
    @Test
    public void testCreate() {
        TagCertificate obj = new TagCertificate();
        String attrCert = "MIICGzCCAQMCAQEwH6EdpBswGTEXMBUGAWkEEK3AjNJLBUBSvVDG4bbdZsmgXzBdpFswWTEQMA4GA1UEAwwHQXR0ciBDQTEMMAoGA1UECwwDQ1BHMQ0wCwYDVQQLDAREQ1NHMQ4wDAYDVQQKDAVJbnRlbDELMAkGA1UECAwCQ0ExCzAJBgNVBAYTAlVTMA0GCSqGSIb3DQEBCwUAAgEBMCIYDzIwMTMwODI4MDMwODU2WhgPMjAxMzA5MjgwMzA4NTZaMEYwFAYMKwYBBAGGjR8BAQEBMQQMAlVTMBQGDCsGAQQBho0fAgICAjEEDAJDQTAYBgwrBgEEAYaNHwMDAwMxCAwGRm9sc29tMA0GCSqGSIb3DQEBCwUAA4IBAQA6EpPzMArxcoqy+oReAEAgr9fKi9pLt45eQd4svGnu0qfKnPrUiEJxedOULUd+O8aPs7sBYE3yS1lAHzAhS0BuTPvYLh4kYl5xftjl0KzCqgXJSHbCe/FcZmjj0CYt/avzxXslYguJicUqDnn7/I8Mr00qOx4AahJd8dbsTT0LGnX4vgD5d7AP9B27Ul5BqIdm1r3sg87adgltsHjz7GCgOIfNoCUYWGc11ERPlhTZq+qoRpGyxXi0LgbvQeMBX36V446WUrt3fG5ezlN4vOduOjEkWqGnjf32VYEdP34TsOCmD3bYzBB5HC1fDn7PLiuupkVPrWQ1stm+OD0cu0ii";
        obj.setCertificate((Base64.decodeBase64(attrCert.getBytes())));
        TagCertificate createTagCertificate = client.createHostTagCertificate(obj);
        log.debug("New TagCertificate created with UUID {}.", createTagCertificate.getId().toString());
    }
    
    @Test
    public void testRetrieve() throws CertificateException {
        TagCertificate retrieveTagCertificate = client.retrieveHostTagCertificate("6d0bbcf9-b662-4d59-bc71-7b360afeb94a");
        byte[] certBytes = Base64.decodeBase64(retrieveTagCertificate.getCertificate());
    }

    @Test
    public void testDelete() {
        client.deleteHostTagCertificate("6d0bbcf9-b662-4d59-bc71-7b360afeb94a");
        log.debug("Revoked the asset tag certificate successfully");
    }
    
}
