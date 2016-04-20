package com.intel.mtwilson.as.rest.data;

import com.intel.mountwilson.as.common.ASException;
import com.intel.mountwilson.as.common.ValidationException;
import com.intel.mtwilson.i18n.ErrorCode;
import com.intel.mtwilson.datatypes.OpenStackHostTrustLevelQuery;
import com.intel.mtwilson.datatypes.TxtHost;
import com.intel.mtwilson.datatypes.TxtHostRecord;
import com.intel.mtwilson.model.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
//import org.codehaus.jackson.JsonGenerationException;
//import org.codehaus.jackson.map.JsonMappingException;
//import org.codehaus.jackson.map.ObjectMapper;
import static org.junit.Assert.*;
import org.junit.Test;
/**
 * These tests verify that the data model serializes and de-serializes properly.
 *
 * @author jbuhacoff
 */
public class TxtHostTest {
    private static ObjectMapper mapper = new ObjectMapper();

    /**
     * Sample serialized object.
     * Before:
     * {"HostName":"Test host 1","IPAddress":"10.1.71.103","Port":9999,"BIOS_Name":"EPSD","BIOS_Version":60,"VMM_Name":"Some VMM","VMM_Version":1.1.1,AddOnConnectionString="http://example.server.com/connect/here",Description="a test record","Email":jonathanx.a.buhacoff@intel.com"}
     * After change:
     * {"hostName":"RHEL 62 KVM","port":9999,"description":"RHEL 62 KVM Integration ENV","addOn_Connection_String":"http://example.server.com:234/vcenter/","bios":{"name":"EPSD","version":"60"},"vmm":{"name":"ESX","version":"0.4.1"},"ipaddress":"10.1.71.103","email":null}
     * @throws JsonGenerationException
     * @throws JsonMappingException
     * @throws IOException
     */
    @Test
    public void writeJSON() throws JsonGenerationException,
            JsonMappingException, IOException {
        /*
        TxtHost host = new TxtHost(
            "RHEL 62 KVM", // String HostName,
            "10.1.71.103", // String IPAddress,
            9999, // Integer Port,
            "EPSD", // String BIOS_Name,
            "60", // String BIOS_Version,
            "ESX", // String VMM_Name,
            "0.4.1", // String VMM_Version,
            "http://example.server.com:234/vcenter/", // String AddOn_Connection_String,
            "RHEL 62 KVM Integration ENV", // String Description,
            null // String Email
            );
            * */
        TxtHostRecord hostinfo = new TxtHostRecord();
        hostinfo.HostName = "RHEL 62 KVM";
        hostinfo.IPAddress ="10.1.71.103";
        hostinfo.Port = 9999;
        hostinfo.BIOS_Name = "EPSD";
        hostinfo.BIOS_Version = "60";
        hostinfo.VMM_Name = "Xen";
        hostinfo.VMM_Version = "4.1.1";
        hostinfo.BIOS_Oem = "EPSD";
        hostinfo.AddOn_Connection_String = "http://example.server.com:234/vcenter/";
        hostinfo.Description = "RHEL 62 KVM Integration ENV";
        hostinfo.Email = null;
        hostinfo.VMM_OSName = "RHEL";
        hostinfo.VMM_OSVersion = "6.1";
        TxtHost host = new TxtHost(hostinfo);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        mapper.writeValue(stream, host);

        String json = stream.toString();
        System.out.println(json);
        
        
/*
        assertEquals(2, with(json).getInt("count"));
        with(json).getString("pcrmask").equals("userName");
        Arrays.asList(with(json).getList("hosts")).containsAll(Arrays.asList(new String[] { "test-host-1", "ESX host 2" }));*/
    }
    
    /**
     * Sample serialized object.
     * {"count":2,"pcrmask":"some pcr mask","hosts":["test-host-1","ESX host 2"]}
     *
     * @throws JsonGenerationException
     * @throws JsonMappingException
     * @throws IOException
     */
    @Test
    public void readJSON() throws JsonGenerationException,
            JsonMappingException, IOException {
        
    	InputStream in = getClass().getResourceAsStream("HostTrustInputTest.sample.json");
    	try {
	        OpenStackHostTrustLevelQuery obj = mapper.readValue(in, OpenStackHostTrustLevelQuery.class);
	
	        assertEquals(2, obj.count);
	        assertEquals("some pcr mask",obj.pcrMask);
	        assertEquals("test-host-1",obj.hosts[0].toString());
	        assertEquals("ESX host 2",obj.hosts[1].toString());
    	}
    	finally {
    		if(in != null) 
    			in.close();
    	}
    }

    
    @Test(expected=ValidationException.class) // was NullPointerException
    public void nullHostnameThrowsException() {  // datatype.Hostname
    	Hostname h = new Hostname(null);
    	System.err.println(h.toString());
    }

    @Test(expected=ValidationException.class) // was IllegalArgumentException
    public void emptyHostnameThrowsException() {  // datatype.Hostname
    	Hostname h = new Hostname("");
    	System.err.println(h.toString());
    }

    @Test(expected=ValidationException.class) // was IllegalArgumentException
    public void invalidHostnameThrowsException() {  // datatype.Hostname
    	Hostname h = new Hostname("invalid, hostname has comma in it");
    	System.err.println(h.toString());
    }

    @Test(expected=ASException.class)
    public void convertIllegalArgumentExceptionToASException() {
        try {
        	Hostname h = new Hostname("");            
        	System.err.println(h.toString());
        }
        catch(IllegalArgumentException e) {
            throw new ASException(ErrorCode.AS_MISSING_INPUT, e.getMessage());
        }
    }
}
