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
        return new ClientResource(baseurl() + "/v2/user-certificates");
    }
    public static ClientResource userCertificates(UUID uuid) {
        return new ClientResource(baseurl() + "/v2/user-certificates/" + uuid);
    }
    
    public static ClientResource userCertificates(String anyUuidOidName) {
        return new ClientResource(baseurl() + "/v2/user-certificates/" + anyUuidOidName);
    }

    public static ClientResource manifestSignature() {
        return new ClientResource(baseurl() + "/v2/manifest-signature");
    }
    
    public static ClientResource testAddIntegers() {
        return new ClientResource(baseurl() + "/v2/rpc/add_integers");
    }

    public static ClientResource testRpcStatus(UUID id) {
        return new ClientResource(baseurl() + "/v2/rpcs/"+id);
    }

}
