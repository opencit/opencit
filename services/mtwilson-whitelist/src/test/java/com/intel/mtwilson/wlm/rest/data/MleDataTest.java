/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.wlm.rest.data;

import com.intel.mtwilson.datatypes.MleData;
import com.intel.mtwilson.datatypes.ManifestData;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
//import org.codehaus.jackson.map.ObjectMapper;
//import org.codehaus.jackson.JsonGenerationException;
//import org.codehaus.jackson.map.JsonMappingException;
import org.junit.*;
import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

/**
 *
 * @author jbuhacoff
 */
public class MleDataTest {
    private static ObjectMapper mapper = new ObjectMapper();

    /*
    @Test
    public void testMleTypeString() {
        MleData.MleType mleType = MleData.MleType.BIOS;
        assertEquals("BIOS", mleType.toString());
        assertEquals("BIOS", mleType.name());
    }
    * 
    */

    /*
    @Test
    public void testAttestationTypeString() {
        MleData.AttestationType aType = MleData.AttestationType.PCR;
        assertEquals("PCR", aType.toString());
        assertEquals("PCR", aType.name());
    }
    * 
    */


    /**
     * Sample serialized object.
     * {"Name":"OEM MLE A","Description":"OEM MLE","Attestation_Type":"PCR","MLE_Manifests":[{"Name":"1","Value":"abcdefghijklmnop"},{"Name":"2","Value":"jklmnopabcdefghi"}],"MLE_Type":"VMM","Version":"1.2.3"}
     * 
     * @throws JsonGenerationException
     * @throws JsonMappingException
     * @throws IOException
     */
    @Test
    public void writeJSON() throws JsonGenerationException,
            JsonMappingException, IOException {
        
        ArrayList<ManifestData> manifestList = new ArrayList<ManifestData>();
        manifestList.add(new ManifestData("1", "abcdefghijklmnop"));
        manifestList.add(new ManifestData("2", "jklmnopabcdefghi"));
        MleData mleData = new MleData(
                "OEM MLE A",
                "1.2.3",
                MleData.MleType.VMM,
                MleData.AttestationType.PCR,
                manifestList,
                "OME MLE", // description
                "Windows", // OS Name
                "7", // OS Version
                "Microsoft" // OEM Name
                );

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        mapper.writeValue(stream, mleData);

        String json = stream.toString();
        System.out.println(json);
        
        
/*
        assertEquals(2, with(json).getInt("count"));
        with(json).getString("pcrmask").equals("userName");
        Arrays.asList(with(json).getList("hosts")).containsAll(Arrays.asList(new String[] { "test-host-1", "ESX host 2" }));*/
    }
    
    /**
     * Sample serialized object.
     * {"Name":"OEM MLE A","Description":"OEM MLE","Attestation_Type":"PCR","MLE_Manifests":[{"Name":"1","Value":"abcdefghijklmnop"},{"Name":"2","Value":"jklmnopabcdefghi"}],"MLE_Type":"VMM","Version":"1.2.3"}
     *
     * @throws JsonGenerationException
     * @throws JsonMappingException
     * @throws IOException
     */
    @Test
    public void readJSON() throws JsonGenerationException,
            JsonMappingException, IOException {
        
    	InputStream in = getClass().getResourceAsStream("/VmmMle1.sample.json");
    	try {
    		MleData mleData = mapper.readValue(in, MleData.class);
            assertEquals("PCR", mleData.getAttestationType());
            assertEquals("VMM MLE", mleData.getDescription());
            assertEquals("VMM", mleData.getMleType());
            assertEquals("abcdefghijklmnop", mleData.getManifestList().get(0).getValue());
            assertEquals("jklmnopabcdefghi", mleData.getManifestList().get(1).getValue());
    	}
    	finally {
    		in.close();
    	}
//        MleData mleData = new MleData(mleDataRecord);

    }
/*
    @Test
    public void writeMleDataRecordToXml() throws javax.xml.bind.JAXBException, JsonGenerationException,
            JsonMappingException, IOException {
        JAXBContext jc = JAXBContext.newInstance(MleDataRecord.class);
        MleDataRecord mleData = mapper.readValue(getClass().getResourceAsStream("VmmMle1.sample.json"), MleDataRecord.class);
        Marshaller m = jc.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        m.marshal(mleData, System.out);
    }*/

    
    /**
     * Disabled this test because the MleData class does not have the @XmlRootElement annotation on the class 
     * @throws javax.xml.bind.JAXBException
     * @throws JsonGenerationException
     * @throws JsonMappingException
     * @throws IOException 
     */
//    @Test
    public void writeMleDataToXml() throws javax.xml.bind.JAXBException, JsonGenerationException,
            JsonMappingException, IOException {
        JAXBContext jc = JAXBContext.newInstance(MleData.class);
        InputStream in = getClass().getResourceAsStream("/VmmMle1.sample.json");
        try {
        	MleData mleData = mapper.readValue(in, MleData.class);
            Marshaller m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            m.marshal(mleData, System.out);
        }
        finally {
        	in.close();
        }
    }
}
