/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package test.api.v2;

import test.tag.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intel.dcsg.cpg.crypto.RandomUtil;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.performance.report.PerformanceInfo;
import com.intel.dcsg.cpg.performance.report.PerformanceUtil;
import com.intel.dcsg.cpg.tls.policy.impl.InsecureTlsPolicy;
import com.intel.mtwilson.attestation.client.jaxrs.Oems;
import com.intel.mtwilson.as.rest.v2.model.Oem;
import com.intel.mtwilson.as.rest.v2.model.OemCollection;
import com.intel.mtwilson.as.rest.v2.model.OemFilterCriteria;
import com.intel.mtwilson.test.RemoteIntegrationTest;
import java.util.List;
import org.junit.Test;

/**
 * The random strings appended to the OEM name are to make it unique so 
 * running the test repeatedly does not result in "oem already exists" 
 * errors from the server; alternative would be to look for and delete the
 * oem name that is going to be created before creating it
 * 
 * @author jbuhacoff
 */
public class OemTest extends RemoteIntegrationTest {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ProvisionCertificateTest.class);

    @Test
    public void testSearchOems() throws Exception {
        Oems client = new Oems(testProperties);
        OemCollection results = client.searchOems(new OemFilterCriteria());
        List<Oem> list = results.getOems();
        for (Oem oem : list) {
            log.debug("got oem {}", oem.getId().toString());
        }
    }

    /**
     * example output:
     * <pre>
     * created new oem {"id":"6236a572-5295-4dab-b1ee-205198455674","name":"test new oem"}
     * </pre>
     */
    @Test
    public void testCreateOem() throws Exception {
        Oem oem = new Oem();
        oem.setName(String.format("test new oem %s", RandomUtil.randomHexString(4)));
        log.debug("oem input: {}", mapper.writeValueAsString(oem));
        Oems client = new Oems(testProperties);
        Oem created = client.createOem(oem);
        log.debug("created new oem {}", mapper.writeValueAsString(created));
    }

    /**
     * example output when copyTo method of the OemLocator does NOT have the null check:
     * <pre>
     * oem input: {"id":"d607656e-f27d-4ac7-93f2-56bf6a25e7f7","name":"test new oem a2325a5a"}
     * created new oem {"id":"cd08bb8f-01c8-4049-89d2-9aeccc8475bc","name":"test new oem a2325a5a"}
     * </pre>
     * 
     * example output when copyTo method of the OemLocator is fixed WITH the null check:
     * <pre>
     * oem input: {"id":"804c661c-c47a-4475-99ff-dd5fc62cd767","name":"test new oem 7bc629fc"}
     * created new oem {"id":"804c661c-c47a-4475-99ff-dd5fc62cd767","name":"test new oem 7bc629fc"}
     * </pre>
     */
    @Test
    public void testCreateOemWithId() throws Exception {
        Oem oem = new Oem();
        oem.setId(new UUID());
        oem.setName(String.format("test new oem %s", RandomUtil.randomHexString(4)));
        log.debug("oem input: {}", mapper.writeValueAsString(oem));
        Oems client = new Oems(testProperties);
        Oem created = client.createOem(oem);
        log.debug("created new oem {}", mapper.writeValueAsString(created));
    }
    
}
