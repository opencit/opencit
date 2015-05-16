/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package test.api.v2;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intel.dcsg.cpg.extensions.Extensions;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.agent.VendorHostAgentFactory;
import com.intel.mtwilson.agent.citrix.CitrixHostAgentFactory;
import com.intel.mtwilson.agent.intel.IntelHostAgentFactory;
import com.intel.mtwilson.agent.vmware.VmwareHostAgentFactory;
import com.intel.mtwilson.as.rest.v2.rpc.CreateWhiteListRunnable;
import com.intel.mtwilson.as.rest.v2.rpc.RegisterHostsRunnable;
import com.intel.mtwilson.datatypes.ManifestData;
import com.intel.mtwilson.datatypes.MleData;
import com.intel.mtwilson.datatypes.TxtHostRecord;
import com.intel.mtwilson.datatypes.TxtHostRecord;
import com.intel.mtwilson.datatypes.TxtHostRecordList;
import com.intel.mtwilson.datatypes.TxtHostRecordList;
import com.intel.mtwilson.jaxrs2.provider.JacksonObjectMapperProvider;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import com.intel.mtwilson.jackson.v2api.V2Module;

/**
 *
 * @author ssbangal
 */
public class WhiteListTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(WhiteListTest.class);
    
    @BeforeClass 
    public static void registerPluginsForTest() {
        Extensions.register(VendorHostAgentFactory.class, VmwareHostAgentFactory.class);
        Extensions.register(VendorHostAgentFactory.class, CitrixHostAgentFactory.class);
        Extensions.register(VendorHostAgentFactory.class, IntelHostAgentFactory.class);
    }
    
    @Test
    public void CreateWhiteListWithDefaultOptions() throws Exception {
        CreateWhiteListRunnable runObj = new CreateWhiteListRunnable();
        TxtHostRecord hostObj = new TxtHostRecord();
        hostObj.HostName = "10.1.71.175";
        hostObj.AddOn_Connection_String = "https://10.1.71.162:443/sdk;Administrator;intel123!";
//        hostObj.HostName = "10.1.71.91";
//        hostObj.AddOn_Connection_String = "http://10.1.71.91:443/;root;P@ssw0rd";
        
//        hostObj.HostName = "10.1.71.45";
//        hostObj.AddOn_Connection_String = "https://10.1.71.45:9999";
        runObj.setHost(hostObj);
        runObj.run();
    }
    
    @Test
    public void testRegisterHost() throws Exception {
        RegisterHostsRunnable runObj = new RegisterHostsRunnable();
        TxtHostRecord hostObj = new TxtHostRecord();
//        hostObj.HostName = "10.1.71.175";
//        hostObj.AddOn_Connection_String = "https://10.1.71.162:443/sdk;Administrator;intel123!";
        hostObj.HostName = "10.1.71.91";
        hostObj.AddOn_Connection_String = "http://10.1.71.91:443/;root;P@ssw0rd";
        TxtHostRecordList hosts = new TxtHostRecordList();
        hosts.getHostRecords().add(hostObj);
        runObj.setHosts(hosts);
        runObj.run();
        
    }
    
    @Test
    public void serializeMleDataTest() throws JsonProcessingException {
        ObjectMapper mapper = JacksonObjectMapperProvider.createDefaultMapper();
        ManifestData manifestItem = new ManifestData("moduleName", "aaaa");
        ArrayList<ManifestData> list = new ArrayList<>();
        list.add(manifestItem);
        MleData mle = new MleData();
        mle.setName("IntelTest");
        mle.setVersion("1.2.3.4");
        mle.setMleType("BIOS");
        mle.setOemName("EPSD");
        mle.setDescription("test json mapper");
        mle.setAttestationType("PCR");
        mle.setManifestList(list);
        log.debug("mledata: {}", mapper.writeValueAsString(mle));
        // {"Name":"IntelTest","Version":"1.2.3.4","Attestation_Type":"PCR","MLE_Type":"BIOS","Description":"test json mapper","OemName":"EPSD","MLE_Manifests":[{"Name":"moduleName","Value":"aaaa"}]}
        
    }

    @Test
    public void serializeMleDataMixinTest() throws JsonProcessingException {
        ObjectMapper mapper = JacksonObjectMapperProvider.createDefaultMapper();
         mapper.registerModule(new V2Module()); // forces v2 style property names overriding the v1 @PropertyName annotations
        mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, false);
        ManifestData manifestItem = new ManifestData("moduleName", "aaaa");
        ArrayList<ManifestData> list = new ArrayList<>();
        list.add(manifestItem);
        MleData mle = new MleData();
        mle.setName("IntelTest");
        mle.setVersion("1.2.3.4");
        mle.setMleType("BIOS");
        mle.setOemName("EPSD");
        mle.setDescription("test json mapper");
        mle.setAttestationType("PCR");
        mle.setManifestList(list);
        log.debug("mledata: {}", mapper.writeValueAsString(mle));
        // {"name":"IntelTest","version":"1.2.3.4","attestation_type":"PCR","mle_type":"BIOS","description":"test json mapper","oem_name":"EPSD","mle_manifests":[{"name":"moduleName","value":"aaaa"}]}
    }
    
    @Test(expected=Exception.class)
    public void deserializeMleDataTestFailsBecauseIncorrectManifestListKey() throws IOException {
        String json = "{\"name\":\"IntelTest\",\"version\":\"1.3.3.7\",\"description\":\"Test Bios MLE\",\"attestation_type\":\"PCR\",\"mle_type\":\"BIOS\",\"oem_uuid\":\"1dc1086a-f9cf-4f32-9552-c71d0a5361c5\",\"mle_manifests\":[{\"name\":\"0\",\"value\":\"13371337133713513101F04B88BCB7B79A8F250E\"}]}";
        ObjectMapper mapper = JacksonObjectMapperProvider.createDefaultMapper();
        MleData mle = mapper.readValue(json, MleData.class);
        assertNotNull(mle);
        assertNotNull(mle.getManifestList()); // REQUIRES MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES in order to work;  without that configuration we get an exception here:  "mle_manifests" does not map to setManifestList() because of @PropertyName("MLE_Manifests")
        assertEquals("13371337133713513101F04B88BCB7B79A8F250E",mle.getManifestList().get(0).getValue()); // ok
    }

    @Test(expected=Exception.class)
    public void deserializeMleDataTestFailsWithMethodNameMapping() throws IOException {
        String json = "{\"name\":\"IntelTest\",\"version\":\"1.3.3.7\",\"description\":\"Test Bios MLE\",\"attestation_type\":\"PCR\",\"mle_type\":\"BIOS\",\"oem_uuid\":\"1dc1086a-f9cf-4f32-9552-c71d0a5361c5\",\"manifest_list\":[{\"name\":\"0\",\"value\":\"13371337133713513101F04B88BCB7B79A8F250E\"}]}";
        ObjectMapper mapper = JacksonObjectMapperProvider.createDefaultMapper();
        MleData mle = mapper.readValue(json, MleData.class);
        assertNotNull(mle);
        assertNotNull(mle.getManifestList()); // exception here:  "manifest_list" does not map to setManifestList() because of @PropertyName("MLE_Manifests")
        assertEquals("13371337133713513101F04B88BCB7B79A8F250E",mle.getManifestList().get(0).getValue());
    }

    @Test
    public void deserializeMleDataTestWorksWithPropertyNameAnnotation() throws IOException {
        String json = "{\"name\":\"IntelTest\",\"version\":\"1.3.3.7\",\"description\":\"Test Bios MLE\",\"attestation_type\":\"PCR\",\"mle_type\":\"BIOS\",\"oem_uuid\":\"1dc1086a-f9cf-4f32-9552-c71d0a5361c5\",\"MLE_Manifests\":[{\"name\":\"0\",\"value\":\"13371337133713513101F04B88BCB7B79A8F250E\"}]}";
        ObjectMapper mapper = JacksonObjectMapperProvider.createDefaultMapper();
        MleData mle = mapper.readValue(json, MleData.class);
        assertNotNull(mle);
        assertNotNull(mle.getManifestList()); // ok because "MLE_Manifests" matches @PropertyName("MLE_Manifests")
        assertEquals("13371337133713513101F04B88BCB7B79A8F250E",mle.getManifestList().get(0).getValue()); // ok
    }
    
    @Test
    public void deserializeMleDataTestWorksWithMixin() throws IOException {
        String json = "{\"name\":\"IntelTest\",\"version\":\"1.3.3.7\",\"description\":\"Test Bios MLE\",\"attestation_type\":\"PCR\",\"mle_type\":\"BIOS\",\"oem_uuid\":\"1dc1086a-f9cf-4f32-9552-c71d0a5361c5\",\"mle_manifests\":[{\"name\":\"0\",\"value\":\"13371337133713513101F04B88BCB7B79A8F250E\"}]}";
        ObjectMapper mapper = JacksonObjectMapperProvider.createDefaultMapper();
         mapper.registerModule(new V2Module()); // forces v2 style property names overriding the v1 @PropertyName annotations
        mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, false);
       MleData mle = mapper.readValue(json, MleData.class);
        assertNotNull(mle);
        assertNotNull(mle.getManifestList()); // REQUIRES MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES in order to work;  without that configuration we get an exception here:  "mle_manifests" does not map to setManifestList() because of @PropertyName("MLE_Manifests")
        assertEquals("13371337133713513101F04B88BCB7B79A8F250E",mle.getManifestList().get(0).getValue()); // ok
    }
    
    
}
