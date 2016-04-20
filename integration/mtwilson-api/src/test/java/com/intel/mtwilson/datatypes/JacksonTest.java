/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.datatypes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.tls.policy.TlsPolicyChoice;
import com.intel.mtwilson.tls.policy.TlsPolicyDescriptor;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class JacksonTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JacksonTest.class);

    
    @Test
    public void testWritePojo() throws Exception {
        ApiClientCreateRequest pojo = new ApiClientCreateRequest();
        pojo.setRoles(new String[] { "a", "b", "c" });
        pojo.setCertificate(new byte[] { 0, 1, 2, 3 });
        ObjectMapper mapper = new ObjectMapper();
        log.debug("pojo: {}", mapper.writeValueAsString(pojo));
        // output:  pojo: {"X509Certificate":"AAECAw==","Roles":["a","b","c"]}
    }
    
    @Test
    public void testWritePojoWithPropertyNamingStrategy() throws Exception {
        ApiClientCreateRequest pojo = new ApiClientCreateRequest();
        pojo.setRoles(new String[] { "a", "b", "c" });
        pojo.setCertificate(new byte[] { 0, 1, 2, 3 });
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(new PropertyNamingStrategy.LowerCaseWithUnderscoresStrategy());
        log.debug("pojo: {}", mapper.writeValueAsString(pojo));
        // output:  pojo: {"x509_certificate":"AAECAw==","roles":["a","b","c"]}
    }

    /**
     * Output:
     * <pre>
     * {"HostName":"localhost","IPAddress":null,"Port":null,"BIOS_Name":null,"BIOS_Version":null,"BIOS_Oem":null,"VMM_Name":null,"VMM_Version":null,"VMM_OSName":null,"VMM_OSVersion":null,"AddOn_Connection_String":null,"Description":null,"Email":null,"Location":null,"AIK_Certificate":null,"AIK_PublicKey":null,"AIK_SHA1":null,"Processor_Info":null}
     * </pre>
     * @throws Exception 
     */
    @Test
    public void testWriteTxtHostRecordWithoutTlsPolicy() throws Exception {
        TxtHostRecord r = new TxtHostRecord();
        r.HostName = "localhost";
        ObjectMapper mapper = new ObjectMapper();
        log.debug("without tls policy: {}", mapper.writeValueAsString(r));
        TxtHostRecord t = mapper.readValue(mapper.writeValueAsString(r), TxtHostRecord.class);
        log.debug("without tls policy after decoding: {}", mapper.writeValueAsString(t));
    }

     /**
     * Output same as in testWriteTxtHostRecordWithoutTlsPolicy because the
     * new TlsPolicyChoice field is ignored during serialization:
     * <pre>
     * {"HostName":"localhost","IPAddress":null,"Port":null,"BIOS_Name":null,"BIOS_Version":null,"BIOS_Oem":null,"VMM_Name":null,"VMM_Version":null,"VMM_OSName":null,"VMM_OSVersion":null,"AddOn_Connection_String":null,"Description":null,"Email":null,"Location":null,"AIK_Certificate":null,"AIK_PublicKey":null,"AIK_SHA1":null,"Processor_Info":null}
     * </pre>
     * @throws Exception 
     */
   @Test
    public void testWriteTxtHostRecordWithTlsPolicy() throws Exception {
        TxtHostRecord r = new TxtHostRecord();
        r.HostName = "localhost";
//        TlsPolicyChoice choice = new TlsPolicyChoice();
//        choice.setTlsPolicyId("INSECURE");
//        r.tlsPolicyChoice = choice;
        ObjectMapper mapper = new ObjectMapper();
        log.debug("with tls policy name: {}", mapper.writeValueAsString(r));
//        choice.setTlsPolicyId(new UUID().toString());
//        log.debug("with tls policy id: {}", mapper.writeValueAsString(r));
//        choice.setTlsPolicyDescriptor(new TlsPolicyDescriptor());
//       log.debug("with tls policy descriptor: {}", mapper.writeValueAsString(r));
         TxtHostRecord t = mapper.readValue(mapper.writeValueAsString(r), TxtHostRecord.class);
        log.debug("with tls policy after decoding: {}", mapper.writeValueAsString(t));
    }

     /**
     * Output same as in testWriteTxtHostRecordWithoutTlsPolicy because the
     * new TlsPolicyChoice field is ignored during serialization:
     * <pre>
     * {"HostName":"localhost","IPAddress":null,"Port":null,"BIOS_Name":null,"BIOS_Version":null,"BIOS_Oem":null,"VMM_Name":null,"VMM_Version":null,"VMM_OSName":null,"VMM_OSVersion":null,"AddOn_Connection_String":null,"Description":null,"Email":null,"Location":null,"AIK_Certificate":null,"AIK_PublicKey":null,"AIK_SHA1":null,"Processor_Info":null}
     * </pre>
     * @throws Exception 
     */
   @Test
    public void testWriteExtendedTxtHostRecordWithTlsPolicy() throws Exception {
        TxtHostRecord r = new TxtHostRecord();
        r.HostName = "localhost";
        TlsPolicyChoice choice = new TlsPolicyChoice();
        choice.setTlsPolicyId("INSECURE");
        r.tlsPolicyChoice = choice;
        ObjectMapper mapper = new ObjectMapper();
        log.debug("with tls policy name: {}", mapper.writeValueAsString(r));
        choice.setTlsPolicyId(new UUID().toString());
        log.debug("with tls policy id: {}", mapper.writeValueAsString(r));
        choice.setTlsPolicyDescriptor(new TlsPolicyDescriptor());
        choice.getTlsPolicyDescriptor().setPolicyType("INSECURE");
       log.debug("with tls policy descriptor: {}", mapper.writeValueAsString(r));
         TxtHostRecord t = mapper.readValue(mapper.writeValueAsString(r), TxtHostRecord.class);
        log.debug("with tls policy after decoding: {}", mapper.writeValueAsString(t));
    }
   
    @Test
    public void testReadTxtHostRecordWithTlsPolicy() throws Exception {
        String json = "{\"tlsPolicyChoice\":{\"tlsPolicyId\":\"INSECURE\"},\"HostName\":\"localhost\",\"IPAddress\":null,\"Port\":null,\"BIOS_Name\":null,\"BIOS_Version\":null,\"BIOS_Oem\":null,\"VMM_Name\":null,\"VMM_Version\":null,\"VMM_OSName\":null,\"VMM_OSVersion\":null,\"AddOn_Connection_String\":null,\"Description\":null,\"Email\":null,\"Location\":null,\"AIK_Certificate\":null,\"AIK_PublicKey\":null,\"AIK_SHA1\":null,\"Processor_Info\":null}";
        ObjectMapper mapper = new ObjectMapper();
        TxtHostRecord t = mapper.readValue(json, TxtHostRecord.class);
        log.debug("with tls policy after decoding: {}", mapper.writeValueAsString(t));
        log.debug("tls policy: {}", t.tlsPolicyChoice.getTlsPolicyId());
    }

}
