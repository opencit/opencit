/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package test.tag;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.performance.report.PerformanceInfo;
import com.intel.dcsg.cpg.performance.report.PerformanceUtil;
import com.intel.dcsg.cpg.tls.policy.impl.InsecureTlsPolicy;
import com.intel.mtwilson.as.rest.v2.model.TagCertificate;
import com.intel.mtwilson.as.rest.v2.model.TagCertificateCollection;
import com.intel.mtwilson.as.rest.v2.model.TagCertificateFilterCriteria;
import com.intel.mtwilson.attestation.client.jaxrs.HostTagCertificates;
import com.intel.mtwilson.tag.client.jaxrs.TagManagementClient;
import com.intel.mtwilson.tag.model.Certificate;
import com.intel.mtwilson.test.RemoteIntegrationTest;
import java.util.List;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class ProvisionCertificateTest extends RemoteIntegrationTest {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ProvisionCertificateTest.class);

    @Test
    public void testSearchCertificates() throws Exception {
        HostTagCertificates client = new HostTagCertificates(testProperties);
        TagCertificateCollection results = client.searchHostTagCertificates(new TagCertificateFilterCriteria());
        List<TagCertificate> list = results.getTagCertificates();
        for (TagCertificate tagcert : list) {
            log.debug("got cert {}", tagcert.getId().toString());
        }
    }

    @Test
    public void testProvisionCertificate() throws Exception {
        UUID fakeHostUuid = new UUID();
        String selectionXml = "<selections xmlns=\"urn:mtwilson-tag-selection\">\n"
                + "<default><selection>\n"
                + "<attribute oid=\"2.5.4.789.1\">\n"
                + "<text>country=CA</text>\n"
                + "</attribute>\n"
                + "</selection></default>\n"
                + "</selections>";

        TagManagementClient client = new TagManagementClient(testProperties);
        Certificate certificate = client.createOneXml(fakeHostUuid, selectionXml);
        log.debug("got certificate {} bytes for uuid {}", certificate.getCertificate().length, fakeHostUuid.toString());
    }
    
    @Test
    public void testAnyCachedCertificate() throws Exception {
        // first create a certificate
        UUID fakeHostUuid = new UUID();
        String selectionXml = "<selections xmlns=\"urn:mtwilson-tag-selection\">\n"
                + "<default><selection>\n"
                + "<attribute oid=\"2.5.4.789.1\">\n"
                + "<text>country=CA</text>\n"
                + "</attribute>\n"
                + "</selection></default>\n"
                + "</selections>";

        TagManagementClient client = new TagManagementClient(testProperties);
        Certificate certificate = client.createOneXml(fakeHostUuid, selectionXml);
        log.debug("got certificate {} bytes for uuid {}", certificate.getCertificate().length, fakeHostUuid.toString());
        // now try to get the same one from the cache
        String getCachedCertXml = "<selections xmlns=\"urn:mtwilson-tag-selection\">\n"
                + "<options><cache mode=\"on\"/></options>\n"
                + "</selections>";
        Certificate cachedCertificate = client.createOneXml(fakeHostUuid, getCachedCertXml);
        log.debug("got certificate {} bytes for uuid {}", cachedCertificate.getCertificate().length, fakeHostUuid.toString());
        // result: no matching selection
    }

    @Test
    public void testCachedCertificateWithSelectedAttributes() throws Exception {
        // first create a certificate
        UUID fakeHostUuid = new UUID();
        String selectionXml = "<selections xmlns=\"urn:mtwilson-tag-selection\">\n"
                + "<default><selection>\n"
                + "<attribute oid=\"2.5.4.789.1\">\n"
                + "<text>country=CA</text>\n"
                + "</attribute>\n"
                + "</selection></default>\n"
                + "</selections>";

        TagManagementClient client = new TagManagementClient(testProperties);
        Certificate certificate = client.createOneXml(fakeHostUuid, selectionXml);
        log.debug("got certificate {} bytes for uuid {}", certificate.getCertificate().length, fakeHostUuid.toString());
        // now try to get the same one from the cache
        String getCachedCertXml = "<selections xmlns=\"urn:mtwilson-tag-selection\">\n"
                + "<options><cache mode=\"on\"/></options>\n"
                + "<default><selection>\n"
                + "<attribute oid=\"2.5.4.789.1\">\n"
                + "<text>country=CA</text>\n"
                + "</attribute>\n"
                + "</selection></default>\n"
                + "</selections>";
        Certificate cachedCertificate = client.createOneXml(fakeHostUuid, getCachedCertXml);
        log.debug("got certificate {} bytes for uuid {}", cachedCertificate.getCertificate().length, fakeHostUuid.toString());
        // result: no matching selection
    }
    

    @Test
    public void testProvisionMultipleCertificatesSequential() throws Exception {
        for (int i = 0; i < 10; i++) {
            testProvisionCertificate();
        }
    }

    @Test
    public void testProvisionMultipleCertificatesConcurrent() throws Exception {
        int max = 1000;
        Runnable[] runnables = new Runnable[max];
        for (int i = 0; i < max; i++) {
            runnables[i] = new Runnable() {
                @Override
                public void run() {
                    try {
                        testProvisionCertificate();
                    } catch (Exception e) {
                        log.error("failed to provision certificate", e);
                    }
                }
            };
        }
        PerformanceInfo info = PerformanceUtil.measureMultipleConcurrentTasks(120, runnables);
//        ObjectMapper mapper = new ObjectMapper();
        log.debug("min {} max {} average {} for {} attempts", info.getMin(), info.getMax(), info.getAverage(), info.getData().length);
    }
}
