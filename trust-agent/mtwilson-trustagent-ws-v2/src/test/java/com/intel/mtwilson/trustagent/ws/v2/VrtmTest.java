/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.trustagent.ws.v2;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;
import com.intel.mtwilson.trustagent.model.VMAttestationRequest;
import com.intel.mtwilson.trustagent.model.VMAttestationResponse;
import com.intel.mtwilson.trustagent.vrtmclient.xml.MethodResponse;
import com.intel.mtwilson.trustagent.vrtmclient.xml.Param;
import com.intel.mtwilson.trustagent.vrtmclient.xml.Value;
import javax.xml.bind.DatatypeConverter;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author hxia5
 */
public class VrtmTest {
    
    public VrtmTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }
    
    @Test
    public void testmapper() throws Exception {
        //String xml = "<methodResponse><params><param><value><string>haidongcaca</string></value></param></params></methodResponse>";
        // define  the java class represenging xml response from vrtm since it is a simple return
        String xml= "<?xml version='1.0'?>"
            + "<methodResponse>"
            +     "<params>"
            +        "<param>"
            +           "<value><string>MQ==</string></value>"
            +        "</param>"
            +     "</params>"
            + "</methodResponse>";
        
        System.out.println("Method response: " + xml);

        XmlMapper mapper = new XmlMapper();
        MethodResponse response = mapper.readValue(xml, MethodResponse.class);
        Param param[] = response.getParams();
        Value value = param[0].getValue();
        byte[] bytes = DatatypeConverter.parseBase64Binary(value.getString());
        System.out.println("return vm status: " + new String(bytes, "UTF-8"));
        System.out.println("response write back to xml: " + mapper.writeValueAsString(response));
    }
}