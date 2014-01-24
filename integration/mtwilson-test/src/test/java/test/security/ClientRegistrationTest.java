/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package test.security;

import com.intel.dcsg.cpg.crypto.CryptographyException;
import com.intel.mtwilson.My;
import com.intel.mtwilson.MyConfiguration;
import com.intel.mtwilson.api.ClientFactory;
import com.intel.dcsg.cpg.crypto.RsaCredentialX509;
import com.intel.dcsg.cpg.crypto.SimpleKeystore;
import com.intel.dcsg.cpg.io.ByteArrayResource;
import com.intel.dcsg.cpg.io.FileResource;
import com.intel.mtwilson.ms.controller.ApiClientX509JpaController;
import com.intel.mtwilson.ms.controller.exceptions.IllegalOrphanException;
import com.intel.mtwilson.ms.controller.exceptions.MSDataException;
import com.intel.mtwilson.ms.controller.exceptions.NonexistentEntityException;
import com.intel.mtwilson.ms.data.ApiClientX509;
import com.intel.dcsg.cpg.tls.policy.impl.InsecureTlsPolicy;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateEncodingException;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class ClientRegistrationTest {
    @Test
    public void testRegisterClient() throws IOException, FileNotFoundException, KeyStoreException, NoSuchAlgorithmException, UnrecoverableEntryException, CertificateEncodingException, IllegalOrphanException, NonexistentEntityException, MSDataException, CryptographyException {
//        My.client().r
        ByteArrayResource keystoreResource = new ByteArrayResource();
        MyConfiguration config = My.configuration(); // new MyConfiguration();
        // create and register a new api client
        SimpleKeystore keystore = ClientFactory.createUserInResource(
                keystoreResource,
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
