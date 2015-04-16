/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package api.as;

import com.intel.dcsg.cpg.crypto.CryptographyException;
import com.intel.dcsg.cpg.crypto.RsaCredentialX509;
import com.intel.dcsg.cpg.crypto.SimpleKeystore;
import com.intel.dcsg.cpg.io.ByteArrayResource;
import com.intel.mtwilson.ApiClient;
import com.intel.mtwilson.api.*;
import com.intel.mtwilson.datatypes.*;
import com.intel.dcsg.cpg.configuration.CommonsConfigurationUtil;
import com.intel.mtwilson.model.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateEncodingException;
import java.util.List;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
public class ASHostTest {
    private static Logger log = LoggerFactory.getLogger(ASHostTest.class);
    
    private static ApiClient c;
    @BeforeClass
    public static void setup() throws IOException, ClientException  {
        c = new ApiClient(CommonsConfigurationUtil.fromPropertiesFile(new File("C:/Intel/CloudSecurity/RSATool.properties")));
    }
    
    //@Test
    public void testGetHostTrust() throws IOException, ApiException, SignatureException {
        HostTrustResponse response = c.getHostTrust(new Hostname("10.1.71.145")); // 10.1.71.103
        System.out.println("host: "+response.hostname+" bios: "+response.trust.bios+" vmm: "+response.trust.vmm+" location: "+response.trust.location);
    }


    //@Test
    public void testGetHostLocation() throws IOException, SignatureException {
        HostLocation location;
        try {
            //location = c.GetHostLocation(new Hostname("10.1.71.103")); // will throw api exception because host is not in db
            location = c.getHostLocation(new Hostname("10.1.71.145")); // will throw api exception because host is not in db
            System.out.println("location: "+location.location);
        } catch (ApiException e) {
            log.error(e.getMessage()+" ["+String.valueOf(e.getErrorCode())+"]");
        }
    }

    //@Test
    public void testListAllOS() throws IOException, ApiException, SignatureException {
        List<OsData> list = c.listAllOS();
        for(OsData os : list) {
            System.out.println("OS Name: "+os.getName()+" Version: "+os.getVersion());
        }
    }
    
    //@Test
    public void testQueryHosts()throws IOException, ApiException, SignatureException {
        List<TxtHostRecord> hostList = c.queryForHosts("192.168.98.98");
        for(TxtHostRecord hostObj : hostList){
            System.out.println(hostObj.HostName);
        }
    } 
    
    @Test
    public void testConfigureWhiteList()throws IOException, ApiException, SignatureException {
        //c.deleteHost(new Hostname("10.1.71.154"));
        HostConfigData wlObj = new HostConfigData();
        TxtHostRecord gkvHost = new TxtHostRecord();
        gkvHost.HostName = "10.1.71.154";
        gkvHost.AddOn_Connection_String = "https://10.1.71.87:443/sdk;Administrator;P@ssw0rd";
        wlObj.setTxtHostRecord(gkvHost);
        wlObj.setBiosPCRs("0,17");
        wlObj.setVmmPCRs("18,19,20");
        wlObj.setBiosWhiteList(true);
        wlObj.setVmmWhiteList(true);
        wlObj.setBiosWLTarget(HostWhiteListTarget.BIOS_HOST);
        wlObj.setVmmWLTarget(HostWhiteListTarget.BIOS_HOST);
        wlObj.setRegisterHost(true);
        boolean result = c.configureWhiteList(wlObj);
        System.out.println(result);        
    }
    
    @Test
    public void testRegisterWhiteList()throws IOException, ApiException, SignatureException {
        //c.deleteHost(new Hostname("10.1.71.154"));
        HostConfigData wlObj = new HostConfigData();
        TxtHostRecord gkvHost = new TxtHostRecord();
        gkvHost.HostName = "10.1.71.154";
        gkvHost.AddOn_Connection_String = "https://10.1.71.87:443/sdk;Administrator;P@ssw0rd";
        wlObj.setTxtHostRecord(gkvHost);
        wlObj.setVmmWLTarget(HostWhiteListTarget.VMM_GLOBAL);
        boolean result = c.registerHost(wlObj);
        System.out.println(result);        
    }         
    
    @Test
    public void testAddHostLocation()throws IOException, ApiException, SignatureException {
        HostLocation hlObj = new HostLocation("Folsom", "7C07CFF3C83882A7BE74A2C7869CD5991E6F166F");
        boolean result = c.addHostLocation(hlObj);
        System.out.println(result);        
    }
}
