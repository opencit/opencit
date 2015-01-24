/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package api.as;

import com.intel.mtwilson.ApiClient;
import com.intel.mtwilson.api.*;
import com.intel.dcsg.cpg.configuration.CommonsConfigurationUtil;
import com.intel.dcsg.cpg.crypto.SimpleKeystore;
import com.intel.mtwilson.datatypes.ApiClientCreateRequest;
import com.intel.mtwilson.datatypes.ApiClientInfo;
import com.intel.mtwilson.datatypes.ApiClientSearchCriteria;
import com.intel.mtwilson.datatypes.OemData;
import com.intel.mtwilson.datatypes.TxtHostRecord;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateEncodingException;
import java.util.List;
import org.apache.commons.configuration.Configuration;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
public class MSSearchTest {
    private static Logger log = LoggerFactory.getLogger(MSSearchTest.class);
    
    private static ApiClient c;
    private static Configuration config;
    
    @BeforeClass
    public static void setup() throws IOException, ClientException  {
        config = CommonsConfigurationUtil.fromResource("/mtwilson-0.5.2.properties");
        c = new ApiClient(config);
    }
    
    @Test
    public void testRegisterApiClient() throws KeyManagementException, CertificateEncodingException, FileNotFoundException, FileNotFoundException, FileNotFoundException, FileNotFoundException, FileNotFoundException, FileNotFoundException, FileNotFoundException, FileNotFoundException, FileNotFoundException, FileNotFoundException, FileNotFoundException, FileNotFoundException, FileNotFoundException, FileNotFoundException, FileNotFoundException, FileNotFoundException, ApiException, SignatureException, IOException, FileNotFoundException, CertificateEncodingException, UnrecoverableEntryException, KeyStoreException, Exception  {        
        SimpleKeystore keystore = new SimpleKeystore(new File(config.getString("mtwilson.api.keystore")), config.getString("mtwilson.api.keystore.password"));
        ApiClientCreateRequest apiClient = new ApiClientCreateRequest();
        apiClient.setCertificate(keystore.getRsaCredentialX509(config.getString("mtwilson.api.key.alias"), config.getString("mtwilson.api.key.password")).getCertificate().getEncoded());
        apiClient.setRoles(new String[] {"Security", "Attestation", "Whitelist", "Audit", "Report"});
        c.register(apiClient);
    }
    
    @Test
    public void testQueryHosts() throws IOException, ApiException, SignatureException  {
        List<TxtHostRecord> list = c.queryForHosts(".");
    }
    
    @Test
    public void testListOEM() throws IOException, IOException, ApiException, SignatureException, SignatureException {
        List<OemData> list;
        list = c.listAllOEM();
        list = c.listAllOEM();
        list = c.listAllOEM();
        list = c.listAllOEM();
    }
    
    @Test
    public void testSearchApiClient() throws IOException, ApiException, SignatureException {
        ApiClientSearchCriteria criteria = new ApiClientSearchCriteria();
        criteria.enabledEqualTo = true;
        List<ApiClientInfo> list = c.searchApiClients(criteria);
        if( list == null ) {
            System.out.println("No records found");
            return;
        }
        for(ApiClientInfo info : list) {
            System.out.println("Name: "+info.name+" Enabled: "+info.enabled+" Status: "+info.status);
        }
    }


}
