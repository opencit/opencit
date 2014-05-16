/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.client.jaxrs;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.jaxrs2.client.MtWilsonClient;
import com.intel.mtwilson.tag.model.Configuration;
import com.intel.mtwilson.tag.model.ConfigurationCollection;
import com.intel.mtwilson.tag.model.ConfigurationFilterCriteria;
import com.intel.mtwilson.tag.model.Configuration;
import com.intel.mtwilson.tag.model.ConfigurationCollection;
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
public class Configurations extends MtWilsonClient {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Selections.class);

    public Configurations(URL url) throws Exception{
        super(url);
    }

    public Configurations(Properties properties) throws Exception {
        super(properties);
    }    

   
    /**
     * Creates a new configuration item using the list of properties specified.
     * @param Configuration object that needs to be created. 
     * @return Created Configuration object.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions configurations:create
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType POST
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/configurations
     * Input: {"name":"backup","content":{"allowTagsInCertificateRequests":"true","approveAllCertificateRequests":"true",
     * "allowAutomaticTagSelection":"true","automaticTagSelectionName":"d16034fe-1f3f-4648-a305-4b7f90aa213f"}}
     * Output: {"id":"1230ead0-7ce4-4eac-a68f-e249052ea9aa","name":"backup",
     * "content":{"allowTagsInCertificateRequests":"true","approveAllCertificateRequests":"true",
     * "allowAutomaticTagSelection":"true","automaticTagSelectionName":"d16034fe-1f3f-4648-a305-4b7f90aa213f"}}
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
    public Configuration createConfiguration(Configuration obj) {
        log.debug("target: {}", getTarget().getUri().toString());
        Configuration createdObj = getTarget().path("configurations").request().accept(MediaType.APPLICATION_JSON).post(Entity.json(obj), Configuration.class);
        return createdObj;
    }

    /**
     * Deletes the configuration details with the specified ID. 
     * @param UUID of the configuration entry that has to be deleted.
     * @return N/A
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions configurations:delete
     * @mtwContentTypeReturned N/A
     * @mtwMethodType DELETE
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/configurations/1230ead0-7ce4-4eac-a68f-e249052ea9aa
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  TpmPasswords client = new TpmPasswords(My.configuration().getClientProperties());
     *  client.deleteTpmPassword("31741556-f5c7-4eb6-a713-338a23e43b93");
     * </pre>
     */
    public void deleteConfiguration(UUID uuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("id", uuid);
        Response obj = getTarget().path("configurations/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).delete();
    }
    
    /**
     * Allows the user to edit the properties of the specified configuration item.
     * @param Configuration object having the data that needs to be updated.
     * @return Updated Configuration object.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions tpm_passwords:store
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType PUT
     * @mtwSampleRestCall
     * https://server.com:8181/mtwilson/v2/configurations/1230ead0-7ce4-4eac-a68f-e249052ea9aa
     * Input: {"name":"backup","content":{"allowTagsInCertificateRequests":"false","approveAllCertificateRequests":"true",
     * "allowAutomaticTagSelection":"false","automaticTagSelectionName":"d16034fe-1f3f-4648-a305-4b7f90aa213f"}}
     * Output: {"id":"1230ead0-7ce4-4eac-a68f-e249052ea9aa","name":"backup",
     * "content":{"allowTagsInCertificateRequests":"false","approveAllCertificateRequests":"true",
     * "allowAutomaticTagSelection":"false","automaticTagSelectionName":"d16034fe-1f3f-4648-a305-4b7f90aa213f"}}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  Configurations client = new Configurations(My.configuration().getClientProperties());
     *  Role role = new Role();
     *  role.setId(UUID.valueOf("31741556-f5c7-4eb6-a713-338a23e43b93"));
     *  role.setDescription("Intel OEM updated");
     *  role = client.editRole(role);
     * </pre>
     */
    public Configuration editConfiguration(Configuration obj) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("id", obj.getId().toString());
        Configuration updatedObj = getTarget().path("configurations/{id}").resolveTemplates(map).request().accept(MediaType.APPLICATION_JSON).put(Entity.json(obj), Configuration.class);
        return updatedObj;
    }


    /**
     * Retrieves the details of the configuration with for the specified ID. Note
     * that the ID should be a valid UUID.
     * @param uuid - UUID of the host tpm password that needs to be retrieved
     * @return Configuration object matching the specified UUID.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions tpm_passwords:retrieve
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * https://server.com:8181/mtwilson/v2/configurations/5330ead0-7ce4-4eac-a68f-e249052ea9aa
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * {"id":"5330ead0-7ce4-4eac-a68f-e249052ea9aa","name":"main",
     * "content":{"allowTagsInCertificateRequests":"true","approveAllCertificateRequests":"true",
     * "allowAutomaticTagSelection":"true","automaticTagSelectionName":"d16034fe-1f3f-4648-a305-4b7f90aa213f"}}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  Configurations client = new Configurations(My.configuration().getClientProperties());
     *  Role retrieveRole = client.retrieveRole("31741556-f5c7-4eb6-a713-338a23e43b93");
     * </pre>
     */
    public Configuration retrieveConfiguration(UUID uuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("id", uuid);
        Configuration obj = getTarget().path("configurations/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).get(Configuration.class);
        return obj;
    }

    /**
     * Retrieves the details of the configuration based on the search criteria specified.  
     * @param ConfigurationFilterCriteria object specifying the filter criteria. The 
     * possible search options include nameEqualTo and nameContains.  
     * @return ConfigurationCollection object with the list of all the Configuration objects matching the specified filter criteria
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions tpm_passwords:search
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/configurations?nameEqualTo=main
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * {"configurations":[{"id":"5330ead0-7ce4-4eac-a68f-e249052ea9aa","name":"main",
     * "content":{"allowTagsInCertificateRequests":"true","approveAllCertificateRequests":"true","allowAutomaticTagSelection":"true",
     * "automaticTagSelectionName":"d16034fe-1f3f-4648-a305-4b7f90aa213f"}}]}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  Configurations client = new Configurations(My.configuration().getClientProperties());
     *  KvAttributeFilterCriteria criteria = new KvAttributeFilterCriteria();
     *  criteria.nameEqualTo = "country";
     *  KvAttributeCollection kvAttrs = client.searchConfigurations(criteria);
     * </pre>
     */
    public ConfigurationCollection searchConfigurations(ConfigurationFilterCriteria criteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        ConfigurationCollection objCollection = getTargetPathWithQueryParams("configurations", criteria).request(MediaType.APPLICATION_JSON).get(ConfigurationCollection.class);
        return objCollection;
    }
    
}
