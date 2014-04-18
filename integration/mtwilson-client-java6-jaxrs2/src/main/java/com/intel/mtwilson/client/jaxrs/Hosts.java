/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.client.jaxrs;

import com.intel.mtwilson.as.rest.v2.model.Host;
import com.intel.mtwilson.as.rest.v2.model.HostCollection;
import com.intel.mtwilson.as.rest.v2.model.HostFilterCriteria;
import java.net.URL;
import java.util.HashMap;
import java.util.Properties;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <code>Hosts</code> is the class used for creation, updation and deletion of Hosts in the Mt.Wilson system.
 * @author ssbangal
 */
public class Hosts extends MtWilsonClient {
    
    Logger log = LoggerFactory.getLogger(getClass().getName());

    public Hosts(URL url) throws Exception{
        super(url);
    }

    /**
     * Constructor with properties object. 
     * @param properties - <code> Properties </code> object to initialize the <code>Hosts</code> with Mt.Wilson properties 
     * Use <code>MyConfiguration.getClientProperties()</code> to get the Properties to use for initialization
     * @throws Exception 
     * 
     * <i><u>Sample Java API call :</u></i><br>
     * {@code
     *  Properties prop = My.configuration().getClientProperties();
     *  Hosts hosts = new Hosts(prop);
     * }
    */
    public Hosts(Properties properties) throws Exception {
        super(properties);
    }
    
    /**
     * Searches for the hosts with the specified criteria.
     * @param criteria - <code> HostFilterCriteria</code> object that specifies the search criteria.
     * The possible search options include nameEqualTo, nameContains and descriptionContains.
     * @return <code> HostCollection</code>, a list of Hosts that match the filter/search criteria.
     *  * @return <code> Usercollection </code>, a list of users that satisfy the search criteria
     *  <p>
     * <i><u>Roles Needed:</u></i> TOCHECK?
     * <p>
     * <i><u>Content type returned:</u></i>JSON/XML/YAML<br>
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: GET</i><br>
     * https://10.1.71.234:8181/mtwilson/v2/hosts?nameContains=10
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * hosts: 
     * [1]0:  
     * {
     *  id: "2d026d64-ec08-4406-8a2d-3f90f2addd5e"
     *  name: "10.1.71.57"
     *  connection_url: "vmware:https://10.1.71.58:443/sdk;administrator@vsphere.local;P@ssw0rd"
     *  bios_mle_uuid: "a4f855a9-4307-470d-8662-24ca23dd88ef"
     *  vmm_mle_uuid: "d322f307-ab22-41f5-9b07-b99ef4b85b91"
     * }
     * 
     * <i><u>Sample Java API call :</u></i><br>
     * {@code
     *   Properties prop = My.configuration().getClientProperties();
     *   Hosts hosts = new Hosts(prop);
     *   HostFilterCriteria criteria = new HostFilterCriteria();
     *   criteria.nameContains = "10";
     *   HostCollection objCollection = hosts.searchHosts(criteria);
     * }
     */
    public HostCollection searchHosts(HostFilterCriteria criteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        HostCollection objCollection = getTargetPathWithQueryParams("hosts", criteria).request(MediaType.APPLICATION_JSON).get(HostCollection.class);
        return objCollection;
    }
    
    /**
     * Retrieves the host with the specified UUID.
     * @param uuid - UUID of the Host to be retrieved. UUID has to be non- null
     * @return <code>Host</code> retrieved from the system with the specified UUID.
     *   * <p>
     * <i><u>Roles Needed:</u></i> TO CHECK?
     * <p>
     * <i><u>Content type returned:</u></i>JSON/XML/YAML
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: GET</i><br>
     *    https://10.1.71.234:8181/mtwilson/v2/hosts/2d026d64-ec08-4406-8a2d-3f90f2addd5e
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * {
     *  id: "2d026d64-ec08-4406-8a2d-3f90f2addd5e"
     *  name: "10.1.71.57"
     *  connection_url: "vmware:https://10.1.71.58:443/sdk;administrator@vsphere.local;P@ssw0rd"
     *  bios_mle_uuid: "a4f855a9-4307-470d-8662-24ca23dd88ef"
     *  vmm_mle_uuid: "d322f307-ab22-41f5-9b07-b99ef4b85b91"
     * }
     * 
     * <i><u>Sample Java API call :</u></i><br>
     * {@code
     *   Properties prop = My.configuration().getClientProperties();
     *   Hosts hosts = new Hosts(prop);
     *   Host retrieveHost = hosts.retrieveHost("6d0bbcf9-b662-4d59-bc71-7b360afeb94a");
     * }
    */
    public Host retrieveHost(String uuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("id", uuid);
        Host obj = getTarget().path("hosts/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).get(Host.class);
        return obj;
    }

    /**
     * Creates the specified host
     * @param obj - <code> Host</code> object with the values to be created in the system.
     * @return <code>Host</code> created in the system.
     * <i><u>Roles Needed:</u></i> TO CHECK?
     * <p>
     * <i><u>Content type returned:</u></i>JSON/XML/YAML
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: POST</i><br>
     *    https://10.1.71.234:8181/mtwilson/v2/hosts/
     * <p>
     * <i><u>Sample Input:</u></i> <br>
     * {"name":"10.1.71.175","connection_url":"https://10.1.71.162:443/sdk;Administrator;intel123!","bios_mle_uuid":"b14e5039-373d-4743-aa65-1e24c23dd249","vmm_mle_uuid":"3a4503a1-1632-433f-bca7-5655ccbafec4"} 
     * <i><u>Sample Output:</u></i><br>
     * {
     *  id: "2d026d64-ec08-4406-8a2d-3f90f2addd5e"
     *  name: "10.1.71.57"
     *  connection_url: "vmware:https://10.1.71.58:443/sdk;administrator@vsphere.local;P@ssw0rd"
     *  bios_mle_uuid: "a4f855a9-4307-470d-8662-24ca23dd88ef"
     *  vmm_mle_uuid: "d322f307-ab22-41f5-9b07-b99ef4b85b91"
     * }
     * 
     * <i><u>Sample Java API call :</u></i><br>
     * {@code
     *   Properties prop = My.configuration().getClientProperties();
     *   Hosts hosts = new Hosts(prop);
     *   Host obj = new Host();
     *   obj.setName("10.1.71.175");
     *   obj.setConnectionUrl("https://10.1.71.162:443/sdk;Administrator;intel123!");
     *   obj.setBiosMleUuid("b14e5039-373d-4743-aa65-1e24c23dd249");
     *   obj.setVmmMleUuid("3a4503a1-1632-433f-bca7-5655ccbafec4");
     *   Host createHost = hosts.createHost(obj);
     * }
     */
    public Host createHost(Host obj) {
        log.debug("target: {}", getTarget().getUri().toString());
        Host newObj = getTarget().path("hosts").request().accept(MediaType.APPLICATION_JSON).post(Entity.json(obj), Host.class);
        return newObj;
    }

    /**
     * Updates the host with the updated attributes
     * @param obj - <code> Host </code> object with the values to be updated in the system. 
     * "id" of the Host is used as a key to fetch the object and it has to be a non-null value.
     * @return Updated <code>Host</code> object.
     * <i><u>Roles Needed:</u></i> TO CHECK?
     * <p>
     * <i><u>Content type returned:</u></i>JSON/XML/YAML
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: PUT</i><br>
     *    https://10.1.71.234:8181/mtwilson/v2/hosts/2d026d64-ec08-4406-8a2d-3f90f2addd5e
     * <p>
     * <i><u>Sample Input:</u> </i><br>
     * {"name":"10.1.71.175","connection_url":"https://10.1.71.162:443/sdk;Administrator;intel123!","bios_mle_uuid":"b14e5039-373d-4743-aa65-1e24c23dd249","vmm_mle_uuid":"3a4503a1-1632-433f-bca7-5655ccbafec4"}
     * <i><u>Sample Output:</u></i><br>
     * {
     *  id: "2d026d64-ec08-4406-8a2d-3f90f2addd5e"
     *  name: "10.1.71.57"
     *  connection_url: "vmware:https://10.1.71.58:443/sdk;administrator@vsphere.local;P@ssw0rd"
     *  bios_mle_uuid: "a4f855a9-4307-470d-8662-24ca23dd88ef"
     *  vmm_mle_uuid: "d322f307-ab22-41f5-9b07-b99ef4b85b91"
     * }
     * 
     * <i><u>Sample Java API call :</u></i><br>
     * {@code
     *   Properties prop = My.configuration().getClientProperties();
     *   Hosts hosts = new Hosts(prop);    
     *   Host obj = new Host();
     *   obj.setId(UUID.valueOf("6d0bbcf9-b662-4d59-bc71-7b360afeb94a"));
     *   obj.setDescription("Updated the host");
     *   Host editHost = hosts.editHost(obj
     * }
     */
    public Host editHost(Host obj) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("id", obj.getId().toString());
        Host newObj = getTarget().path("hosts/{id}").resolveTemplates(map).request().accept(MediaType.APPLICATION_JSON).put(Entity.json(obj), Host.class);
        return newObj;
    }

    /**
     * Deletes the Host with the specified uuid.
     * @param uuid - UUID of the host to be deleted from the system. The UUID has to be a non-null value.
     *   * <i><u>Roles Needed:</u></i> TO CHECK
     * <p>
     * <i><u>Content type returned:</u></i>JSON/XML/YAML
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: DELETE</i><br>
     * https://10.1.71.234:8181/mtwilson/v2/hosts/2d026d64-ec08-4406-8a2d-3f90f2addd5e
     * <p>
     * <i><u>Sample Output: NA </u></i><br>
     * <i><u>Sample Java API call :</u></i><br>
     * {@code
     *   Properties prop = My.configuration().getClientProperties();
     *   Hosts hosts = new Hosts(prop);  
     *   hosts.deleteHost("6d0bbcf9-b662-4d59-bc71-7b360afeb94a");
     * }
     */
    public void deleteHost(String uuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("id", uuid);
        Response obj = getTarget().path("hosts/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).delete();
        log.debug(obj.toString());
    }
    
}
