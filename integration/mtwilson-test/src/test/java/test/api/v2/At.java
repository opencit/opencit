/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package test.api.v2;

import com.intel.dcsg.cpg.io.PropertiesUtil;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.My;
import com.intel.mtwilson.core.junit.Env;
import java.io.IOException;
import java.util.Properties;
import org.restlet.resource.ClientResource;

/**
 *
 * @author jbuhacoff
 */
public class At {

    public static String baseurl() throws IOException {
        Properties properties = PropertiesUtil.removePrefix(Env.getProperties("cit3-attestation"), "cit3.attestation.");
        return String.format("https://%s:8443/mtwilson/v2", properties.getProperty("host", "localhost"));
    }
    
    public static ClientResource userCertificates() throws IOException {
        return new ClientResource(baseurl() + "/user-certificates");
    }
    public static ClientResource userCertificates(UUID uuid) throws IOException {
        return new ClientResource(baseurl() + "/user-certificates/" + uuid);
    }
    
    public static ClientResource userCertificates(String anyUuidOidName) throws IOException {
        return new ClientResource(baseurl() + "/user-certificates/" + anyUuidOidName);
    }

    public static ClientResource manifestSignature() throws IOException {
        return new ClientResource(baseurl() + "/manifest-signature");
    }
    
    public static ClientResource testAddIntegers() throws IOException {
        return new ClientResource(baseurl() + "/rpc/add_integers");
    }
    public static ClientResource testAddIntegersAsync() throws IOException {
        return new ClientResource(baseurl() + "/rpc-async/add_integers");
    }

    public static ClientResource testRpcStatus(UUID id) throws IOException {
        return new ClientResource(baseurl() + "/rpcs/"+id);
    }
    public static ClientResource testRpcOutput(UUID id) throws IOException {
        return new ClientResource(baseurl() + "/rpcs/"+id+"/output");
    }

}
