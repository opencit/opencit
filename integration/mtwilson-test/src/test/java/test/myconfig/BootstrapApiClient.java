/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package test.myconfig;

import com.intel.mtwilson.api.ApiException;
import com.intel.mtwilson.api.ClientException;
import com.intel.mtwilson.My;
import com.intel.mtwilson.MyConfiguration;
import com.intel.mtwilson.api.ClientFactory;
import com.intel.dcsg.cpg.crypto.CryptographyException;
import com.intel.dcsg.cpg.crypto.RsaCredentialX509;
import com.intel.dcsg.cpg.crypto.SimpleKeystore;
import com.intel.dcsg.cpg.io.FileResource;
import com.intel.mtwilson.ms.controller.ApiClientX509JpaController;
import com.intel.mtwilson.ms.data.ApiClientX509;
import com.intel.dcsg.cpg.tls.policy.impl.InsecureTlsPolicy;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class BootstrapApiClient {

    /**
     * You should first run testInitMyConfig if you haven't already.
     *
     * Creates a local user keystore and registers your new user with Mt Wilson.
     * Also automatically approves your new user so you can start using it right away
     * in your JUnit tests using My.client()
     *
     * @throws MalformedURLException
     * @throws IOException
     * @throws ApiException
     * @throws ClientException
     * @throws CryptographyException
     */
    @Test
    public void testCreateMyUser() throws Exception {
        MyConfiguration config = My.configuration(); // new MyConfiguration();
        File directory = config.getKeystoreDir();
        if (!directory.exists()) {
            directory.mkdirs();
        }
        // create and register a new api client
        SimpleKeystore keystore = ClientFactory.createUserInResource(
                new FileResource(config.getKeystoreFile()),
                config.getKeystoreUsername(),
                config.getKeystorePassword(),
                config.getMtWilsonURL(),
                new InsecureTlsPolicy(),
                config.getMtWilsonRoleArray()
                );
        // approve the new api client
        if( keystore == null ) { throw new IllegalArgumentException("Cannot create user in resource: "+config.getKeystoreFile().getAbsolutePath()); }
        RsaCredentialX509 rsaCredentialX509 = keystore.getRsaCredentialX509(config.getKeystoreUsername(), config.getKeystorePassword());
        ApiClientX509JpaController jpaController = My.jpa().mwApiClientX509();
        ApiClientX509 apiClient = jpaController.findApiClientX509ByFingerprint(rsaCredentialX509.identity());
        apiClient.setStatus("Approved");
        apiClient.setEnabled(true);
        jpaController.edit(apiClient);
    }
}
