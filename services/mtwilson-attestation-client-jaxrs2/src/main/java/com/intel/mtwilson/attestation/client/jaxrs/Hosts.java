/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.attestation.client.jaxrs;

import com.intel.dcsg.cpg.tls.policy.TlsConnection;
import com.intel.mtwilson.jaxrs2.client.MtWilsonClient;
import com.intel.mtwilson.as.rest.v2.model.Host;
import com.intel.mtwilson.as.rest.v2.model.HostCollection;
import com.intel.mtwilson.as.rest.v2.model.HostFilterCriteria;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import javax.ws.rs.WebApplicationException;
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

    public Hosts(Properties properties) throws Exception {
        super(properties);
    }
    
    public Hosts(Properties properties, TlsConnection tlsConnection) throws Exception {
        super(properties, tlsConnection);
    }
    
    /**
     * Registers the specified host with the system. As part of registration, a host has to be associated with both BIOS and 
     * VMM/Hypervisor MLEs. So, these MLEs have to be configured before host registration. Also the TLS policy that should be used
     * to communicate with the host has to be specified.
     * @param obj Host object with the details of the host to be registered. The required parameters that specify
     * the host details are the host_name and add_on_connection_string [Open Source Hosts: intel:https://192.168.1.201:1443, Citrix XenServer: 
     * citrix:https://192.168.1.202:443/;root;pwd, VMware ESXi:vmware:https://192.168.1.222:443/sdk;Admin;password]. To associate 
     * the host with the MLEs, both the OEM and OS UUIDs have to be specified. Also a valid TLS policy has to be specified for
     * communicating with the host. All other parameters are optional.
     * @return Host created in the system.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions hosts:create
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType POST
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/hosts/
     * Input: {"name":"192.168.0.2","connection_url":"https://192.168.0.1:443/sdk;admin;pwd",
     *          "bios_mle_uuid":"7e90c088-c9c7-486f-9480-9cd0a7a3b977","vmm_mle_uuid":"fb2cb173-5e19-446b-9161-aa7368c5c882",
     *          "tls_policy_id":"e1a527b5-2020-49c1-83be-6bd8bf641258"}'
     * 
     * Output: {"id":"2fbe5090-7eb7-4af5-aefd-afa5cd70bf4d","name":"192.168.0.2","connection_url":"https://192.168.0.1:443/sdk;admin;pwd",
     *          "bios_mle_uuid":"7e90c088-c9c7-486f-9480-9cd0a7a3b977","vmm_mle_uuid":"fb2cb173-5e19-446b-9161-aa7368c5c882",
     *          "tls_policy_id":"e1a527b5-2020-49c1-83be-6bd8bf641258"}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *   Properties prop = My.configuration().getClientProperties();
     *   Hosts client = new Hosts(prop);
     *   Host obj = new Host();
     *   obj.setName("192.168.0.2");
     *   obj.setConnectionUrl("https://192.168.0.1:443/sdk;admin;pwd");
     *   obj.setBiosMleUuid("7e90c088-c9c7-486f-9480-9cd0a7a3b977");
     *   obj.setVmmMleUuid("fb2cb173-5e19-446b-9161-aa7368c5c882");
     *   obj.setTlsPolicyId("e1a527b5-2020-49c1-83be-6bd8bf641258");
     *   Host createHost = client.createHost(obj);
     * </pre>
     */
    public Host createHost(Host obj) {
        log.debug("target: {}", getTarget().getUri().toString());
        Host newObj = getTarget().path("hosts").request().accept(MediaType.APPLICATION_JSON).post(Entity.json(obj), Host.class);
        return newObj;
    }

    /**
     * Deletes the host with the specified UUID.
     * @param uuid - UUID of the host to be deleted from the system. 
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions hosts:delete
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType DELETE
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/hosts/e43424ca-9e00-4cb9-b038-9259d0307888
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *   Properties prop = My.configuration().getClientProperties();
     *   Hosts client = new Hosts(prop);  
     *   client.deleteHost("e43424ca-9e00-4cb9-b038-9259d0307888");
     * </pre>
     */
    public void deleteHost(String uuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("id", uuid);
        Response obj = getTarget().path("hosts/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).delete();
        log.debug(obj.toString());
    }

    /**
     * Deletes the hosts matching the specified filter criteria.
     * @param criteria HostFilterCriteria object specifying the search criteria. Search options supported
     * include id, nameEqualTo, nameContains and descriptionContains.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions hosts:delete,search
     * @mtwContentTypeReturned N/A
     * @mtwMethodType DELETE
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/hosts?nameContains=192
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *   Hosts client = new Hosts(My.configuration().getClientProperties());
     *   HostFilterCriteria criteria = new HostFilterCriteria();
     *   criteria.nameContains = "192";
     *  client.deleteHost(criteria);
     * </pre>
     */
    public void deleteHost(HostFilterCriteria criteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        Response obj = getTargetPathWithQueryParams("hosts", criteria).request(MediaType.APPLICATION_JSON).delete();
        if( !obj.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
            throw new WebApplicationException("Delete hosts failed");
        }
    }
    
    /**
     * Updates the host with the specified attributes. Except for the host name, all other attributes can be updated.
     * @param obj Host object with the values to be updated. 
     * @return Updated <code>Host</code> object.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions hosts:store
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType PUT
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/hosts/e43424ca-9e00-4cb9-b038-9259d0307888
     * Input: {"name":"192.168.0.2","connection_url":"https://192.168.0.1:443/sdk;admin;pwd","bios_mle_uuid":"823a4ae6-b8cd-4c14-b89b-2a3be2d13985",
     *          "vmm_mle_uuid":"98101211-b617-4f59-8132-a5d05360acd6","tls_policy_id":"e1a527b5-2020-49c1-83be-6bd8bf641258"}
     * Output: {"id":"e43424ca-9e00-4cb9-b038-9259d0307888","name":"192.168.0.2",
     *          "connection_url":"https://192.168.0.1:443/sdk;admin;pwd","bios_mle_uuid":"823a4ae6-b8cd-4c14-b89b-2a3be2d13985",
     *          "vmm_mle_uuid":"98101211-b617-4f59-8132-a5d05360acd6","tls_policy_id":"e1a527b5-2020-49c1-83be-6bd8bf641258"}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *   Properties prop = My.configuration().getClientProperties();
     *   Hosts client = new Hosts(prop);    
     *   Host obj = new Host();
     *   obj.setId(UUID.valueOf("e43424ca-9e00-4cb9-b038-9259d0307888"));
     *   obj.setName("192.168.0.2");
     *   obj.setConnectionUrl("https://192.168.0.1:443/sdk;admin;pwd");
     *   obj.setBiosMleUuid("823a4ae6-b8cd-4c14-b89b-2a3be2d13985"); // updating the BIOS
     *   obj.setVmmMleUuid("98101211-b617-4f59-8132-a5d05360acd6");
     *   obj.setTlsPolicyId("e1a527b5-2020-49c1-83be-6bd8bf641258");
     *   Host editHost = client.editHost(obj);
     * </pre>
     */
    public Host editHost(Host obj) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("id", obj.getId().toString());
        Host newObj = getTarget().path("hosts/{id}").resolveTemplates(map).request().accept(MediaType.APPLICATION_JSON).put(Entity.json(obj), Host.class);
        return newObj;
    }

    /**
     * Retrieves the details of the host with the specified UUID.
     * @param uuid - UUID of the Host to be retrieved. 
     * @return Host retrieved from the system with the specified UUID.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions hosts:retrieve
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/hosts/2d026d64-ec08-4406-8a2d-3f90f2addd5e
     * Output: {"id":"e43424ca-9e00-4cb9-b038-9259d0307888","name":"192.168.0.2", "connection_url":"https://192.168.0.1:443/sdk;admin;pwd",
     * "bios_mle_uuid":"823a4ae6-b8cd-4c14-b89b-2a3be2d13985", "vmm_mle_uuid":"98101211-b617-4f59-8132-a5d05360acd6"}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *   Properties prop = My.configuration().getClientProperties();
     *   Hosts hosts = new Hosts(prop);
     *   Host retrieveHost = hosts.retrieveHost("6d0bbcf9-b662-4d59-bc71-7b360afeb94a");
     * </pre>
    */
    public Host retrieveHost(String uuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("id", uuid);
        Host obj = getTarget().path("hosts/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).get(Host.class);
        return obj;
    }

    /**
     * Searches for the hosts with the specified criteria.
     * @param criteria HostFilterCriteria object that specifies the search criteria.
     * The possible search options include id, nameEqualTo, nameContains and descriptionContains.
     * If in case the caller needs the list of all records, filter option can to be set to false. [Ex: /hosts?filter=false]
     * @return HostCollection object with a list of Hosts that match the filter criteria.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions hosts:search
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/hosts?nameContains=192
     * Output: {"hosts":[{"id":"de07c08a-7fc6-4c07-be08-0ecb2f803681","name":"192.168.0.2", "connection_url":"https://192.168.0.1:443/sdk;admin;pwd",
     * "bios_mle_uuid":"823a4ae6-b8cd-4c14-b89b-2a3be2d13985","vmm_mle_uuid":"45c03402-e33d-4b54-9893-de3bbd1f1681"}]}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *   Properties prop = My.configuration().getClientProperties();
     *   Hosts client = new Hosts(prop);
     *   HostFilterCriteria criteria = new HostFilterCriteria();
     *   criteria.nameContains = "192";
     *   HostCollection objCollection = client.searchHosts(criteria);
     * </pre>
     */
    public HostCollection searchHosts(HostFilterCriteria criteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        HostCollection objCollection = getTargetPathWithQueryParams("hosts", criteria).request(MediaType.APPLICATION_JSON).get(HostCollection.class);
        return objCollection;
    }
    
    public void preRegisterHostDetails(List<String> hostNames, String userName, String password) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("host_names", hostNames);
        map.put("user_name", userName);
        map.put("password", password);
        Response obj = getTarget().path("rpc/store-host-pre-registration-details")
                .request().accept(MediaType.APPLICATION_JSON).post(Entity.json(map));
        
        if( !obj.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
            throw new WebApplicationException("Pre-registration of host details failed");
        }
    }
}
