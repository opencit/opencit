/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.client.jaxrs;

import com.intel.dcsg.cpg.configuration.Configuration;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.tls.policy.TlsConnection;
import com.intel.mtwilson.tag.model.TpmPassword;
import com.intel.mtwilson.tag.model.TpmPasswordCollection;
import com.intel.mtwilson.tag.model.TpmPasswordFilterCriteria;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ssbangal
 */
public class HostTpmPassword extends MtWilsonClient {
    
    Logger log = LoggerFactory.getLogger(getClass().getName());

    public HostTpmPassword(URL url)throws Exception {
        super(url);
    }

    public HostTpmPassword(Properties properties) throws Exception {
        super(properties);
    }
    public HostTpmPassword(Configuration configuration) throws Exception {
        super(configuration);
    }
    public HostTpmPassword(Properties configuration, TlsConnection tlsConnection) throws Exception {
        super(configuration, tlsConnection);
    }
    
    /**
     * 
     * @param hostHardwareId
     * @param tpmOwnerSecretHex
     * @return the etag for the updated password
     */
    public String storeTpmPassword(UUID hostHardwareId, String tpmOwnerSecretHex) {
        log.debug("target: {}", getTarget().getUri().toString());
        TpmPassword tpmPassword = new TpmPassword();
        tpmPassword.setId(hostHardwareId);
        tpmPassword.setPassword(tpmOwnerSecretHex);
        TpmPassword result = getTarget()
                .path("/host-tpm-passwords")
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .post(Entity.json(tpmPassword), TpmPassword.class);
        if( result.getEtag() != null ) {
            return result.getEtag();
        }
        return null;
    }

    /*
    public TpmPassword retrieveTpmPassword(TpmPasswordFilterCriteria criteria) {
        TpmPasswordCollection collection = getTargetPathWithQueryParams("/host-tpm-passwords", criteria).request(MediaType.APPLICATION_JSON).get(TpmPasswordCollection.class);
        if( collection.getTpmPasswords().isEmpty() ) {
            return null;
        }
        return collection.getTpmPasswords().get(0);
    }
    */

    public TpmPassword retrieveTpmPassword(UUID hardwareUuid) {
        HashMap<String,Object> map = new HashMap<>();
        map.put("id", hardwareUuid.toString());
        TpmPassword tpmPassword = getTarget().path("/host-tpm-passwords/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).get(TpmPassword.class);
        return tpmPassword; // may be null
    }

}
