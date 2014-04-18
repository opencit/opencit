/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package test.shiro;

import com.intel.dcsg.cpg.crypto.RsaCredential;
import com.intel.dcsg.cpg.crypto.SimpleKeystore;
import com.intel.dcsg.cpg.io.ByteArrayResource;
import com.intel.mtwilson.ApiClient;
import com.intel.mtwilson.My;
import com.intel.mtwilson.MyFilesystem;
import com.intel.mtwilson.datatypes.OemData;
import com.intel.mtwilson.datatypes.TxtHostRecord;
import com.intel.mtwilson.ms.data.MwPortalUser;
import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class CertificateLoginTest2 {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CertificateLoginTest2.class);
    private static String username = "admin";
    private static String password;
    private static byte[] keystoreBytes;
    private static SimpleKeystore keystore;
    private static URL url;
    private static ApiClient client;
    
    /**
     * To avoid having to re-register a test client with the
     * system being tested, we look for a local file 
     * mtwilson/configuration/private/password.txt
     * which has the admin password, use the information in local
     * mtwilson/configuration/mtwilson.properties to connect
     * to the remote system's database, open the admin keystore
     * using the locally stored password, and then use that
     * keystore with the api client.
     */
    @BeforeClass
    public static void getUserKeystore() throws Exception {
        password = FileUtils.readFileToString(new File(MyFilesystem.getApplicationFilesystem().getConfigurationPath() + File.separator + "private" + File.separator + "password.txt"));
        MwPortalUser user = My.jpa().mwPortalUser().findMwPortalUserByUserName(username);
        keystoreBytes = user.getKeystore();
        keystore = new SimpleKeystore(new ByteArrayResource(keystoreBytes), password);
        RsaCredential credential = keystore.getRsaCredentialX509(username, password);
        url = My.configuration().getMtWilsonURL();
        Properties configuration = new Properties();
        configuration.setProperty("mtwilson.api.ssl.policy", "INSECURE"); 
        client = new ApiClient(url, credential, configuration);
    }
    
    @Test
    public void testCertificateLogin() throws Exception {
//        List<OemData> oems = client.listAllOEM();
//        log.debug("oems: {}", oems);
        // now something with a query string
        List<TxtHostRecord> hosts = client.queryForHosts("");
        log.debug("hosts: {}", hosts);
    }
}
