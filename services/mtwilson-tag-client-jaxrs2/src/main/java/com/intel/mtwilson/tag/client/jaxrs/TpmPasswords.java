/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.client.jaxrs;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.jaxrs2.client.MtWilsonClient;
import com.intel.mtwilson.tag.model.TpmPassword;
import com.intel.mtwilson.tag.model.TpmPasswordCollection;
import com.intel.mtwilson.tag.model.TpmPasswordFilterCriteria;
import java.net.URL;
import java.util.HashMap;
import java.util.Properties;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;

/**
 *
 * @author ssbangal
 */
public class TpmPasswords extends MtWilsonClient {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Selections.class);

    public TpmPasswords(URL url) throws Exception{
        super(url);
    }

    public TpmPasswords(Properties properties) throws Exception {
        super(properties);
    }    
    /**
     * Creates a new TPM password entry for the host.  
     * @param obj - TpmPassword object that needs to be created. ID that should be specified to create the
     * entry should be the host's hardware UUID. The hardware UUID can be obtained by running the
     * dmidecode command.
     * @return Created TpmPassword object.
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
     *  obj = client.createTpmPassword(obj);
     * </pre>
     */
    public TpmPassword createTpmPassword(TpmPassword obj) {
        log.debug("target: {}", getTarget().getUri().toString());
        TpmPassword createdObj = getTarget().path("host-tpm-passwords").request().accept(MediaType.APPLICATION_JSON).post(Entity.json(obj), TpmPassword.class);
        return createdObj;
    }

    /**
     * Deletes the TPM password entry for the specified ID. 
     * @param uuid - UUID of the host tpm password entry that has to be deleted.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions tpm_passwords:delete
     * @mtwContentTypeReturned N/A
     * @mtwMethodType DELETE
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/host-tpm-passwords/07217f9c-f625-4c5a-a538-73f1880abdda
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  TpmPasswords client = new TpmPasswords(My.configuration().getClientProperties());
     *  client.deleteTpmPassword(UUID.valueOf("07217f9c-f625-4c5a-a538-73f1880abdda"));
     * </pre>
     */
    public void deleteTpmPassword(UUID uuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("id", uuid);
        Response obj = getTarget().path("host-tpm-passwords/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).delete();
        if( !obj.getStatusInfo().getFamily().equals(Family.SUCCESSFUL)) {
            throw new WebApplicationException("Delete selection failed");
        }
    }
    
    /**
     * Deletes the list of TPM password entries based on the search criteria specified. 
     * @param criteria - TpmPasswordFilterCriteria object specifying the filter criteria. The 
     * only search option currently supported is the ID.
     * @return N/A
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions tpm_passwords:delete,search
     * @mtwContentTypeReturned N/A
     * @mtwMethodType DELETE
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/host-tpm-passwords?id=07217f9c-f625-4c5a-a538-73f1880abdda
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  TpmPasswords client = new TpmPasswords(My.configuration().getClientProperties());
     *  TpmPasswordFilterCriteria criteria = new TpmPasswordFilterCriteria();
     *  criteria.id = UUID.valueOf("07217f9c-f625-4c5a-a538-73f1880abdda");
     *  client.deleteTpmPassword(criteria);
     * </pre>
     */
    public void deleteTpmPassword(TpmPasswordFilterCriteria criteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        Response obj = getTargetPathWithQueryParams("host-tpm-passwords", criteria).request(MediaType.APPLICATION_JSON).delete();
        
        if( !obj.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
            throw new WebApplicationException("Delete TpmPassword failed");
        }
    }
    
    /**
     * Allows the user to update the TPM password for the specified ID, which is the
     * host's hardware UUID. 
     * @param obj TpmPassword object having the value that needs to be updated. 
     * @return Updated TpmPassword object.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions tpm_passwords:store
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType PUT
     * @mtwSampleRestCall
     * https://server.com:8181/mtwilson/v2/host-tpm-passwords/07217f9c-f625-4c5a-a538-73f1880abdda
     * Input: {"password":"P@ssword123"}
     * Output: {"id":"07217f9c-f625-4c5a-a538-73f1880abdda","etag":"8b393a8404a47817ed94317171c90a6d1d326b6b",
     * "modified_on":1401306205577,"password":"P@ssword123"}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  TpmPasswords client = new TpmPasswords(My.configuration().getClientProperties());
     *  TpmPassword obj = new TpmPassword();
     *  obj.setId("07217f9c-f625-4c5a-a538-73f1880abdda");
     *  obj.setPassword("P@ssword123");
     *  obj = obj.editTpmPassword(obj);
     * </pre>
     */
    public TpmPassword editTpmPassword(TpmPassword obj) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("id", obj.getId().toString());
        TpmPassword updatedObj = getTarget().path("host-tpm-passwords/{id}").resolveTemplates(map).request().accept(MediaType.APPLICATION_JSON).put(Entity.json(obj), TpmPassword.class);
        return updatedObj;
    }

    /**
     * Retrieves the TPM password value for the specified ID. 
     * @param uuid - UUID of the host for which the tpm password needs to be retrieved
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
    public TpmPassword retrieveTpmPassword(UUID uuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("id", uuid);
        TpmPassword obj = getTarget().path("host-tpm-passwords/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).get(TpmPassword.class);
        return obj;
    }

    /**
     * Retrieves the TPM password based on the search criteria specified. Note that the output does not include the password. The
     * user need to have the tpm_passwords:retrieve permission and call into the retrieve method to get the password.
     * @param criteria - TpmPasswordFilterCriteria object specifying the filter criteria. The 
     * only search option currently supported is the ID.  
     * @return TpmPasswordCollection object with the list of all the TpmPassword objects matching the specified filter criteria. Since
     * the criteria that is currently supported is just ID, the collection would always have either 0 or 1 entry.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions tpm_passwords:search
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/host-tpm-passwords?id=07217f9c-f625-4c5a-a538-73f1880abdda
     * Output: {"tpm_passwords":[{"id":"07217f9c-f625-4c5a-a538-73f1880abdda"}]}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  TpmPasswords client = new TpmPasswords(My.configuration().getClientProperties());
     *  TpmPasswordFilterCriteria criteria = new TpmPasswordFilterCriteria();
     *  criteria.id = UUID.valueOf("07217f9c-f625-4c5a-a538-73f1880abdda");
     *  TpmPasswordCollection objCollection = client.searchTpmPasswords(criteria);
     * </pre>
     */
    public TpmPasswordCollection searchTpmPasswords(TpmPasswordFilterCriteria criteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        TpmPasswordCollection objCollection = getTargetPathWithQueryParams("host-tpm-passwords", criteria).request(MediaType.APPLICATION_JSON).get(TpmPasswordCollection.class);
        return objCollection;
    }
    
    
}
