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
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
     * Creates a new TPM password entry for the host. The 
     * @param TpmPassword object that needs to be created. ID that should be specified to create the
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
     * Output: {"id":"07217f9c-f625-4c5a-a538-73f1880abdda","password":"Password"}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  TpmPasswords client = new TpmPasswords(My.configuration().getClientProperties());
     *  TpmPassword kvObj = new TpmPassword();
     *  role.setName("Intel");
     *  role.setDescription("Intel OEM");
     *  Role createRole = client.createRole(role);
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
     * @return N/A
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
     *  client.deleteTpmPassword("31741556-f5c7-4eb6-a713-338a23e43b93");
     * </pre>
     */
    public void deleteTpmPassword(UUID uuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("id", uuid);
        Response obj = getTarget().path("host-tpm-passwords/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).delete();
    }
    
    /**
     * Allows the user to update the TPM password for the specified ID, which is the
     * host's hardware UUID. 
     * @param role - TpmPassword object having the value that needs to be updated. 
     * @return Updated TpmPassword object.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions tpm_passwords:store
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType PUT
     * @mtwSampleRestCall
     * https://server.com:8181/mtwilson/v2/host-tpm-passwords/07217f9c-f625-4c5a-a538-73f1880abdda
     * <p>
     * <i>Sample Input</i><br>
     * "password":"Password123"
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  KvAttributes client = new KvAttributes(My.configuration().getClientProperties());
     *  Role role = new Role();
     *  role.setId(UUID.valueOf("31741556-f5c7-4eb6-a713-338a23e43b93"));
     *  role.setDescription("Intel OEM updated");
     *  role = client.editRole(role);
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
     * @param uuid - UUID of the host tpm password that needs to be retrieved
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
     *  KvAttributes client = new KvAttributes(My.configuration().getClientProperties());
     *  Role retrieveRole = client.retrieveRole("31741556-f5c7-4eb6-a713-338a23e43b93");
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
     * Retrieves the TPM password based on the search criteria specified. 
     * @param TpmPasswordFilterCriteria object specifying the filter criteria. The 
     * only search option currently supported is the ID.  
     * @return TpmPasswordCollection object with the list of all the TpmPassword objects matching the specified filter criteria
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions tpm_passwords:search
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/host-tpm-passwords?id=07217f9c-f625-4c5a-a538-73f1880abdda
     * Output: {"id":"07217f9c-f625-4c5a-a538-73f1880abdda","password":"Password"}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  KvAttributes client = new KvAttributes(My.configuration().getClientProperties());
     *  KvAttributeFilterCriteria criteria = new KvAttributeFilterCriteria();
     *  criteria.nameEqualTo = "country";
     *  KvAttributeCollection kvAttrs = client.searchKvAttributes(criteria);
     * </pre>
     */
    public TpmPasswordCollection searchTpmPasswords(TpmPasswordFilterCriteria criteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        TpmPasswordCollection objCollection = getTargetPathWithQueryParams("host-tpm-passwords", criteria).request(MediaType.APPLICATION_JSON).get(TpmPasswordCollection.class);
        return objCollection;
    }
    
    
}
