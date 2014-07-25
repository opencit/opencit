/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.attestation.client.jaxrs;

import com.intel.mtwilson.jaxrs2.client.MtWilsonClient;
import com.intel.mtwilson.as.rest.v2.model.Os;
import com.intel.mtwilson.as.rest.v2.model.OsCollection;
import com.intel.mtwilson.as.rest.v2.model.OsFilterCriteria;
import java.net.URL;
import java.util.HashMap;
import java.util.Properties;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <code>Oss</code> is the class that allows creation , deletion  and updates to OS objects.
 * @author ssbangal
 */
public class Oss extends MtWilsonClient {
    
    Logger log = LoggerFactory.getLogger(getClass().getName());

    public Oss(URL url) throws Exception{
        super(url);
    }

    public Oss(Properties properties) throws Exception {
        super(properties);
    }
    
     /**
     * Creates an Os object with the specified parameters in the system. This would be used to associate with VMM/Hypervisor 
     * MLE[Measured Launch Environment] during its creation. Both OS name and version are required parameters. Description field is optional.
     * @param os - Os object that needs to be created. Name and Version have to be specified by the caller. If the caller specifies the ID,
     * it has to be a valid UUID. If not provided, it would be automatically generated.
     * @return <code> Os </code> that is created.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions oss:create
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType POST
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8443/mtwilson/v2/oss
     * Input: {"name":"TestOS 1","description":"","version":"1.2.3"}
     * Output: {"id":"e946ccec-4a55-4913-bdb6-5878c88a9e81","name":"TestOS1","version":"1.2.3","description":""}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  Oss client = new Oss(My.configuration().getClientProperties());
     *  Os newOs = new Os();
     *  newOs.setName("TestOS1");
     *  newOs.setVersion("1.2.3");
     *  newOs.setDescription("");
     *  newOs = client.createOs(newOs);
     * </pre>
     *      
    */
    public Os createOs(Os os) {
        log.debug("target: {}", getTarget().getUri().toString());
        Os newObj = getTarget().path("oss").request().accept(MediaType.APPLICATION_JSON).post(Entity.json(os), Os.class);
        return newObj;
    }

    /**
     * Deletes the Os with the specified UUID from the system. An OS can be deleted only if it is not associated with any MLEs.
     * @param uuid - UUID of the OS that has to be deleted.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions oss:delete
     * @mtwContentTypeReturned N/A
     * @mtwMethodType DELETE
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/oss/e946ccec-4a55-4913-bdb6-5878c88a9e81
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  Oss client = new Oss(My.configuration().getClientProperties());
     *  client.deleteOs("e946ccec-4a55-4913-bdb6-5878c88a9e81");
     * </pre>
     */
    public void deleteOs(String uuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("id", uuid);
        Response obj = getTarget().path("oss/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).delete();
        if( !obj.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
            throw new WebApplicationException("Delete OS failed");
        }
    }

    /**
     * Deletes the Os(s) matching the specified search criteria. 
     * @param criteria OsFilterCriteria object specifying the search criteria. The search options include
     * id, nameEqualTo and nameContains.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions oss:delete,search
     * @mtwContentTypeReturned N/A
     * @mtwMethodType DELETE
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/oss?nameContains=admin
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  Oss client = new Oss(My.configuration().getClientProperties());
     *  OsFilterCriteria criteria = new OsFilterCriteria();
     *  criteria.nameContains = "admin";
     *  client.deleteOs(criteria);
     * </pre>
     */
    public void deleteOs(OsFilterCriteria criteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        Response obj = getTargetPathWithQueryParams("oss", criteria).request(MediaType.APPLICATION_JSON).delete();
        if( !obj.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
            throw new WebApplicationException("Delete os failed");
        }
    }
    
    /**
     * Updates the details of the Os in the system. Only the description of the OS can be updated. 
     * @param obj - Os object that needs to be updated. The caller needs to provide the ID of the object along with the description.
     * @return <code> Os </code> that is updated.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions oss:store
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType PUT
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8443/mtwilson/v2/oss/e946ccec-4a55-4913-bdb6-5878c88a9e81
     * Input: {"description":"Added description"}
     * Output: {"id":"e946ccec-4a55-4913-bdb6-5878c88a9e81","description":"Added description"}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     * Oss client = new Oss(My.configuration().getClientProperties());
     * Os os = new Os();
     * os.setId(UUID.valueOf("e946ccec-4a55-4913-bdb6-5878c88a9e81"));
     * os.setDescription("Added description");
     * os = client.editOs(os);
     * </pre>     
     */
    public Os editOs(Os obj) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("id", obj.getId().toString());
        Os newObj = getTarget().path("oss/{id}").resolveTemplates(map).request().accept(MediaType.APPLICATION_JSON).put(Entity.json(obj), Os.class);
        return newObj;
    }
     
     /**
     * Retrieves the Os with the specified uuid
     * @param uuid - UUID of the Os to be retrieved
     * @return <code> Os </code> matching the specified UUID.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions oss:retrieve
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/oss/e946ccec-4a55-4913-bdb6-5878c88a9e81
     * Output: {"id":"e946ccec-4a55-4913-bdb6-5878c88a9e81","name":"TestOS1","version":"1.2.3","description":"Added description"}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     * Properties prop = My.configuration().getClientProperties();
     * Oss client = new Oss(prop);  
     * Os retrieveOs = client.retrieveOs("e946ccec-4a55-4913-bdb6-5878c88a9e81");
     * </pre>
     */
    public Os retrieveOs(String uuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("id", uuid);
        Os obj = getTarget().path("oss/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).get(Os.class);
        return obj;
    }

    /**
     * Searches for the OS's with the specified set of criteria
     * @param criteria OsFilterCriteria object specifying the filter criteria.
     * The possible search options include id, nameEqualTo and nameContains. 
     * If in case the caller needs the list of all records, filter option can to be set to false. [Ex: /v2/oss?filter=false]
     * @return OsCollection having the list of Oss that meets the filter criteria
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions oss:search
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/oss?nameContains=VMWare
     * Output: {"oss":[{"id":"2ffa07cf-ca9f-11e3-8449-005056b5643f","name":"VMWare","version":"5.0"}]}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     * Properties prop = My.configuration().getClientProperties();
     * Oss client = new Oss(prop);  
     * OsFilterCriteria criteria = new OsFilterCriteria();
     * criteria.nameContains = "VMWare";
     * OsCollection oss = client.searchOss(criteria);
     * </pre>
     */
    public OsCollection searchOss(OsFilterCriteria criteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        OsCollection objCollection = getTargetPathWithQueryParams("oss", criteria).request(MediaType.APPLICATION_JSON).get(OsCollection.class);
        return objCollection;
    }
    
}
