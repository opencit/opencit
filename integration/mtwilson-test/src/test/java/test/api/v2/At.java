/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package test.api.v2;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.My;
import java.io.IOException;
import org.restlet.resource.ClientResource;

/**
 *
 * @author jbuhacoff
 */
public class At {

    public static String baseurl() {
        try {
            return My.configuration().getMtWilsonURL().toExternalForm();
        }
        catch(IOException e) {
            return "";
        }
    }
    
    public static ClientResource userCertificates() {
        return new ClientResource(baseurl() + "/user-certificates");
    }
    public static ClientResource userCertificates(UUID uuid) {
        return new ClientResource(baseurl() + "/user-certificates/" + uuid);
    }
    
    public static ClientResource userCertificates(String anyUuidOidName) {
        return new ClientResource(baseurl() + "/user-certificates/" + anyUuidOidName);
    }

    public static ClientResource manifestSignature() {
        return new ClientResource(baseurl() + "/manifest-signature");
    }
    
    public static ClientResource testAddIntegers() {
        return new ClientResource(baseurl() + "/rpc/add_integers");
    }
    public static ClientResource testAddIntegersAsync() {
        return new ClientResource(baseurl() + "/rpc-async/add_integers");
    }

    public static ClientResource testRpcStatus(UUID id) {
        return new ClientResource(baseurl() + "/rpcs/"+id);
    }
    public static ClientResource testRpcOutput(UUID id) {
        return new ClientResource(baseurl() + "/rpcs/"+id+"/output");
    }

}
