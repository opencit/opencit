/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package api.as;

import org.junit.Test;
import com.intel.mtwilson.ApiCommand;
import com.intel.mtwilson.api.ApiException;
import com.intel.mtwilson.api.ClientException;
import com.intel.dcsg.cpg.crypto.CryptographyException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author jbuhacoff
 */
public class CommandTest {
//    @Test
    public void testGetLocation() throws IOException, KeyManagementException, NoSuchAlgorithmException, GeneralSecurityException, ApiException, CryptographyException, ClientException {
        ApiCommand.main(new String[] { "GetHostLocation", "10.1.71.103" });
    }
}
