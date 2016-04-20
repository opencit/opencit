/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package test.tls;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intel.dcsg.cpg.extensions.Extensions;
import com.intel.mtwilson.ApiClient;
import com.intel.mtwilson.agent.VendorHostAgentFactory;
import com.intel.mtwilson.agent.vmware.VmwareHostAgentFactory;
import com.intel.mtwilson.api.ClientException;
import com.intel.mtwilson.as.rest.v2.model.WhitelistConfigurationData;
import com.intel.mtwilson.datatypes.TxtHostRecord;
import com.intel.mtwilson.datatypes.HostConfigData;
import com.intel.mtwilson.datatypes.HostConfigData;
import com.intel.mtwilson.datatypes.HostConfigDataList;
import com.intel.mtwilson.datatypes.HostConfigResponseList;
import com.intel.mtwilson.datatypes.HostResponse;
import com.intel.mtwilson.datatypes.TxtHost;
import com.intel.mtwilson.datatypes.TxtHostRecord;
import com.intel.mtwilson.datatypes.TxtHostRecordList;
import com.intel.mtwilson.model.Hostname;
import com.intel.mtwilson.ms.MSComponentFactory;
import com.intel.mtwilson.test.RemoteIntegrationTest;
import com.intel.mtwilson.tls.policy.TlsPolicyChoice;
import com.intel.mtwilson.tls.policy.TlsPolicyDescriptor;
import com.intel.mtwilson.tls.policy.creator.impl.InsecureTlsPolicyCreator;
import com.intel.mtwilson.tls.policy.factory.TlsPolicyCreator;
import com.intel.mtwilson.tls.policy.factory.TlsPolicyFactory;
import com.intel.mtwilson.tls.policy.factory.impl.TblHostsTlsPolicyFactory;
import com.intel.mtwilson.tls.policy.factory.impl.TxtHostRecordTlsPolicyFactory;
import java.util.List;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;

/**
 * Test all v1 host registration and update APIs that require a TLS polic.
 * 
 * <pre>
 * **** ATTESTATION SERVICE ****   /AttestationService/resources
 * HostResponse addHost(TxtHost host)     POST /hosts
 * HostResponse updateHost(TxtHost host)   PUT /hosts
 * HostResponse registerHostByFindingMLE(TxtHostRecord hostObj)    POST /hosts/mle
 * HostConfigResponseList addHosts(TxtHostRecordList hostRecords)  POST /hosts/bulk
 * HostConfigResponseList updateHosts(TxtHostRecordList hostRecords)   PUT /hosts/bulk
 * HostResponse deleteHost(Hostname hostname)     DELETE /hosts
 * **** MANAGEMENT SERVICE ****   /ManagementService/resources
 * boolean registerHost(TxtHostRecord hostObj)     POST /host
 * boolean registerHost(HostConfigData hostConfigObj)   POST /host/custom
 * boolean configureWhiteList(TxtHostRecord hostObj)    POST /host/whitelist
 * boolean configureWhiteList(HostConfigData hostConfigObj)   POST /host/whitelist/custom
 * HostConfigResponseList registerHosts(TxtHostRecordList hostRecords)   POST /host/bulk
 * HostConfigResponseList registerHosts(HostConfigDataList hostRecords)   POST /host/bulk/custom
 * </pre>
 * 
 * @author jbuhacoff
 */
public class V1HostRegistrationWithTlsPolicyTest extends RemoteIntegrationTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(V1HostRegistrationWithTlsPolicyTest.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    
    @BeforeClass
    public static void registerTlsPolicy() {
        Extensions.register(TlsPolicyFactory.class, TblHostsTlsPolicyFactory.class);
        Extensions.register(TlsPolicyFactory.class, TxtHostRecordTlsPolicyFactory.class);
        Extensions.register(TlsPolicyCreator.class, InsecureTlsPolicyCreator.class);
        Extensions.register(VendorHostAgentFactory.class, VmwareHostAgentFactory.class);
    }
    
    @AfterClass
    public static void deleteRegisteredHost() {
        try {
            ApiClient client = new ApiClient(testProperties);
            List<TxtHostRecord> hosts = client.queryForHosts("10.1.71.173");
            if( hosts == null || hosts.isEmpty() ) { return; }
            client.deleteHost(new Hostname("10.1.71.173"));
        }
        catch(Exception e) {
            log.error("cannot delete host", e);
        }
    }
    
    private TxtHostRecord getTxtHostRecord() {
        TxtHostRecord host = new TxtHostRecord();
        host.HostName = "10.1.71.173";
        host.AddOn_Connection_String =  "vmware:https://10.1.71.162:443/sdk;Administrator;intel123!;h=10.1.71.173";
        TlsPolicyChoice insecureChoice = new TlsPolicyChoice();
        insecureChoice.setTlsPolicyDescriptor(new TlsPolicyDescriptor());
        insecureChoice.getTlsPolicyDescriptor().setPolicyType("INSECURE");
        host.tlsPolicyChoice = insecureChoice;
        return host;
    }
    
    /**
     * TODO:
     * this registratio n request requires bios name and version as input; only do it after tha tinformation is more readaily available
     * 
     * 
     * Request (wire):
2014-07-05 00:16:40,108 DEBUG [main] o.a.h.wire [Wire.java:72]  >> "POST /mtwilson/v1/AttestationService/resources/hosts HTTP/1.1[\r][\n]"
2014-07-05 00:16:40,109 DEBUG [main] o.a.h.wire [Wire.java:72]  >> "Accept-Language: en-US;q=1, en;q=0.9[\r][\n]"
2014-07-05 00:16:40,109 DEBUG [main] o.a.h.wire [Wire.java:72]  >> "Date: Sat, 5 Jul 2014 00:16:32 PDT[\r][\n]"
2014-07-05 00:16:40,110 DEBUG [main] o.a.h.wire [Wire.java:72]  >> "Authorization: X509 fingerprint="AMK+09fsEr66j/EoT1IN6k9ppCMtVpzb+lwE3PNn7bA=", headers="X-Nonce,Date", algorithm="SHA256withRSA", signature="iWxMLFCOFsNaw+NT/zVK0WrV8iLiGFl59wePk+qdsIm0JRXQb/bh7uI3y7/mwoUbLow8vceTLd40sAQ0D1nfZskKzxJb9//1nZWRKk8IotNVVHzgoHEenAb6CGf6EjtBmyMAZO8FCLc7IkNf4fYWCL2OVOBHKUnU7NDpAIrqaR1iN1kACfVHZ6g8X79EoB3axiK7Ge5Xl3RRslUgDOcmC3AWnAncm9DK7uqvt4xUWAjb8Se9ZozbWEIVsexoNbiBm8piqKUYyLmGLuLtZyQeCLDec7RehU5lvaq4jyWhZqPjKGCHTUmGfIbJvq9udUZzlIHuc8jme0FzNHkHoghzXA=="[\r][\n]"
2014-07-05 00:16:40,110 DEBUG [main] o.a.h.wire [Wire.java:72]  >> "X-Nonce: AAABRwVhqvBjvK5c14uKHazhM1FM+yD6[\r][\n]"
2014-07-05 00:16:40,110 DEBUG [main] o.a.h.wire [Wire.java:72]  >> "Content-Length: 422[\r][\n]"
2014-07-05 00:16:40,111 DEBUG [main] o.a.h.wire [Wire.java:72]  >> "Content-Type: application/json; charset=UTF-8[\r][\n]"
2014-07-05 00:16:40,111 DEBUG [main] o.a.h.wire [Wire.java:72]  >> "Host: 10.1.71.56:8443[\r][\n]"
2014-07-05 00:16:40,111 DEBUG [main] o.a.h.wire [Wire.java:72]  >> "Connection: Keep-Alive[\r][\n]"
2014-07-05 00:16:40,112 DEBUG [main] o.a.h.wire [Wire.java:72]  >> "[\r][\n]"
2014-07-05 00:16:40,115 DEBUG [main] o.a.h.wire [Wire.java:86]  >> "{"HostName":"10.1.71.173","IPAddress":"10.1.71.173","Port":null,"BIOS_Name":null,"BIOS_Version":null,"BIOS_Oem":null,"VMM_Name":null,"VMM_Version":null,"VMM_OSName":null,"VMM_OSVersion":null,"AddOn_Connection_String":"vmware:https://10.1.71.162:443/sdk;Administrator;intel123!;h=10.1.71.173","Description":null,"Email":null,"Location":null,"AIK_Certificate":null,"AIK_PublicKey":null,"AIK_SHA1":null,"Processor_Info":null}"
     * 
     * 
     * Request when using original v1 TxtHostRecord:
     * <pre>
     * {"HostName":"10.1.71.173","IPAddress":"10.1.71.173","Port":null,"BIOS_Name":null,"BIOS_Version":null,"BIOS_Oem":null,"VMM_Name":null,"VMM_Version":null,"VMM_OSName":null,"VMM_OSVersion":null,"AddOn_Connection_String":"vmware:https://10.1.71.162:443/sdk;Administrator;intel123!;h=10.1.71.173","Description":null,"Email":null,"Location":null,"AIK_Certificate":null,"AIK_PublicKey":null,"AIK_SHA1":null,"Processor_Info":null}
     * </pre>
     * 
     * Request when using extended v1 TxtHostRecord2:
     * <pre>
     * {"HostName":"10.1.71.173","IPAddress":null,"Port":null,"BIOS_Name":null,"BIOS_Version":null,"BIOS_Oem":null,"VMM_Name":null,"VMM_Version":null,"VMM_OSName":null,"VMM_OSVersion":null,"AddOn_Connection_String":"vmware:https://10.1.71.162:443/sdk;Administrator;intel123!;h=10.1.71.173","Description":null,"Email":null,"Location":null,"AIK_Certificate":null,"AIK_PublicKey":null,"AIK_SHA1":null,"Processor_Info":null,"tlsPolicyChoice":{"tlsPolicyId":null,"tlsPolicyDescriptor":{"policyType":"INSECURE","ciphers":null,"protocols":null,"protection":null,"data":null,"meta":null}}}
     * </pre>
     * 
     * @throws Exception 
     */
    @Test
    public void testRegisterViaPostHosts() throws Exception {
        log.debug("POST /v1/AttestationService/resources/hosts");
        TxtHostRecord txtHostRecord = getTxtHostRecord();
        ApiClient client = new ApiClient(testProperties);
        TxtHost txtHost = new TxtHost(txtHostRecord);
        HostResponse response = client.addHost(txtHost);
        log.debug("response: {}", mapper.writeValueAsString(response));
    }
    
    /**
     * Request with TxtHostRecord:
     * <pre>
     * POST https://10.1.71.56:8443/mtwilson/v1/ManagementService/resources/host
     * "{"HostName":"10.1.71.173","IPAddress":null,"Port":null,"BIOS_Name":null,"BIOS_Version":null,"BIOS_Oem":null,"VMM_Name":null,"VMM_Version":null,"VMM_OSName":null,"VMM_OSVersion":null,"AddOn_Connection_String":"vmware:https://10.1.71.162:443/sdk;Administrator;intel123!;h=10.1.71.173","Description":null,"Email":null,"Location":null,"AIK_Certificate":null,"AIK_PublicKey":null,"AIK_SHA1":null,"Processor_Info":null,"tlsPolicyChoice":{"tlsPolicyId":"INSECURE","tlsPolicyDescriptor":null}}"
     * </pre>
     * 
     * Request with TxtHostRecord2 (notice presence of tlsPolicyChoice field):
     * <pre>
     * POST https://10.1.71.56:8443/mtwilson/v1/ManagementService/resources/host
     * {"HostName":"10.1.71.173","IPAddress":null,"Port":null,"BIOS_Name":null,"BIOS_Version":null,"BIOS_Oem":null,"VMM_Name":null,"VMM_Version":null,"VMM_OSName":null,"VMM_OSVersion":null,"AddOn_Connection_String":"vmware:https://10.1.71.162:443/sdk;Administrator;intel123!;h=10.1.71.173","Description":null,"Email":null,"Location":null,"AIK_Certificate":null,"AIK_PublicKey":null,"AIK_SHA1":null,"Processor_Info":null,"tlsPolicyChoice":{"tlsPolicyId":"INSECURE","tlsPolicyDescriptor":null}}
     * </pre>
     * 
     * @throws Exception 
     */
    @Test
    public void testRegisterViaPostHost() throws Exception {
        log.debug("POST /v1/ManagementService/resources/host");
        TxtHostRecord txtHostRecord = getTxtHostRecord(); // actually ExtendedTxtHostRecord with a tlsPolicyChoice field
        ApiClient client = new ApiClient(testProperties);
        boolean response = client.registerHost(txtHostRecord);
        log.debug("response: {}", mapper.writeValueAsString(response));
    }
    
    /**
     * This is the same as mtwilson-portal / whitelist / import from trusted host
     * with the host registration option enabled.
     * 
     * Example request:
     * <pre>
     * POST /mtwilson/v1/ManagementService/resources/host/whitelist/custom
     * {"Add_BIOS_WhiteList":true,"Add_VMM_WhiteList":true,"BIOS_WhiteList_Target":"BIOS_OEM","VMM_WhiteList_Target":"VMM_OEM","BIOS_PCRS":"0,17","VMM_PCRS":"18,19,20","Host_Location":"","Register_Host":true,"Host_VMM_Type":null,"TXT_Host_Record":{"HostName":"10.1.71.173","IPAddress":null,"Port":null,"BIOS_Name":null,"BIOS_Version":null,"BIOS_Oem":null,"VMM_Name":null,"VMM_Version":null,"VMM_OSName":null,"VMM_OSVersion":null,"AddOn_Connection_String":"vmware:https://10.1.71.162:443/sdk;Administrator;intel123!;h=10.1.71.173","Description":null,"Email":null,"Location":null,"AIK_Certificate":null,"AIK_PublicKey":null,"AIK_SHA1":null,"Processor_Info":null,"tlsPolicyChoice":{"tlsPolicyId":null,"tlsPolicyDescriptor":{"policyType":"INSECURE","ciphers":null,"protocols":null,"protection":null,"data":null,"meta":null}}},"Overwrite_Whitelist":true}'
     * </pre>
     * 
     * @throws Exception 
     */
    @Test
    public void testRegisterAndWhitelistViaPostWhitelistCustom() throws Exception {
        log.debug("POST /v1/ManagementService/resources/whitelist/custom");
        TxtHostRecord txtHostRecord = getTxtHostRecord(); // actually ExtendedTxtHostRecord with a tlsPolicyChoice field        
        HostConfigData request = new HostConfigData();
        request.setBiosWhiteList(true);
        request.setVmmWhiteList(true);
        request.setBiosPCRs("0,17");
        request.setVmmPCRs("18,19,20");
        request.setRegisterHost(true);
        request.setOverWriteWhiteList(true);
        request.setTxtHostRecord(txtHostRecord);
        
        try {
            ObjectMapper mapper = new ObjectMapper();
            log.debug("testRegisterAndWhitelistViaPostWhitelistCustom: {}", mapper.writeValueAsString(request));
            HostConfigData check = mapper.readValue(mapper.writeValueAsString(request), HostConfigData.class);
            log.debug("testRegisterAndWhitelistViaPostWhitelistCustom check: {}", mapper.writeValueAsString(check));
        } catch (Exception e) {
            log.warn("Cannot write debug log", e);
        }
        
        ApiClient client = new ApiClient(testProperties);
        boolean response = client.configureWhiteList(request);
        log.debug("response: {}", mapper.writeValueAsString(response));
        
    }
    
    // same as testRegisterAndWhitelistViaPostWhitelistCustom  but calling
    // business logic locally instead of using the api client
    @Test
    public void testLocalRegisterAndWhitelistViaPostWhitelistCustom() throws Exception {
        TxtHostRecord txtHostRecord = getTxtHostRecord(); // actually ExtendedTxtHostRecord with a tlsPolicyChoice field        
        HostConfigData request = new HostConfigData();
        request.setBiosWhiteList(true);
        request.setVmmWhiteList(true);
        request.setBiosPCRs("0,17");
        request.setVmmPCRs("18,19,20");
        request.setRegisterHost(true);
        request.setOverWriteWhiteList(true);
        request.setTxtHostRecord(txtHostRecord);
        // equivalent to the /whitelist/custom api method
        WhitelistConfigurationData wlConfigData = new WhitelistConfigurationData(request);
        boolean result = MSComponentFactory.getHostBO().configureWhiteListFromCustomData(wlConfigData);
        log.debug("success? {}", result);
    }
}
