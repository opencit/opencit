/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.attestation.client.jaxrs;

import com.intel.mtwilson.jaxrs2.client.MtWilsonClient;
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
     * Creates a new TPM password entry for the host.  
     * @param hostHardwareId Hardware UUID of the host, which can be obtained by running the dmidecode command.
     * @param tpmOwnerSecretHex TPM Owner password.
     * @return etag for the created password
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions tpm_passwords:create
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType POST
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/host-tpm-passwords
     * Input: {"id":"07217f9c-f625-4c5a-a538-73f1880abdda","password":"Password"}
     * Output: {"id":"07217f9c-f625-4c5a-a538-73f1880abdda","etag":"52bfe4be78b4f7e83afcc516311450dd18d89e8c",
     * "modified_on":1401305674274,"password":"Password"}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  TpmPasswords client = new TpmPasswords(My.configuration().getClientProperties());
     *  TpmPassword obj = new TpmPassword();
     *  obj.setId("07217f9c-f625-4c5a-a538-73f1880abdda");
     *  obj.setPassword("Password");
     *  obj = client.storeTpmPassword(obj);
     * </pre>
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


    /**
     * Retrieves the TPM password based on the search criteria specified. Note that the output does not include the password. The
     * user need to have the tpm_passwords:retrieve permission and call into the retrieve method to get the password.
     * @param criteria - TpmPasswordFilterCriteria object specifying the filter criteria. The 
     * only search option currently supported is the id, which is the hardware UUID of the host.  
     * @return TpmPassword object matching the specified filter criteria.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions tpm_passwords:search
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/host-tpm-passwords?id=07217f9c-f625-4c5a-a538-73f1880abdda
     * Output: {"id":"07217f9c-f625-4c5a-a538-73f1880abdda"}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  TpmPasswords client = new TpmPasswords(My.configuration().getClientProperties());
     *  TpmPasswordFilterCriteria criteria = new TpmPasswordFilterCriteria();
     *  criteria.id = UUID.valueOf("07217f9c-f625-4c5a-a538-73f1880abdda");
     *  TpmPassword obj = client.searchTpmPasswords(criteria);
     * </pre>
     */
    public TpmPassword searchTpmPasswords(TpmPasswordFilterCriteria criteria) {
        TpmPasswordCollection collection = getTargetPathWithQueryParams("/host-tpm-passwords", criteria).request(MediaType.APPLICATION_JSON).get(TpmPasswordCollection.class);
        if( collection.getTpmPasswords().isEmpty() ) {
            return null;
        }
        return collection.getTpmPasswords().get(0);
    }

    /**
     * Retrieves the TPM password value for the specified host. 
     * @param uuid - Hardware UUID of the host for which the tpm password needs to be retrieved
     * @return TpmPassword object matching the specified UUID.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions tpm_passwords:retrieve
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/host-tpm-passwords/07217f9c-f625-4c5a-a538-73f1880abdda
     * Output: {"id":"07217f9c-f625-4c5a-a538-73f1880abdda","password":"Password"}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  TpmPasswords client = new TpmPasswords(My.configuration().getClientProperties());
     *  TpmPassword obj = client.retrieveTpmPassword(UUID.valueOf("07217f9c-f625-4c5a-a538-73f1880abdda"));
     * </pre>
     */    
    public TpmPassword retrieveTpmPassword(UUID hardwareUuid) {
        HashMap<String,Object> map = new HashMap<>();
        map.put("id", hardwareUuid.toString());
        TpmPassword tpmPassword = getTarget().path("/host-tpm-passwords/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).get(TpmPassword.class);
        return tpmPassword; // may be null
    }

}
