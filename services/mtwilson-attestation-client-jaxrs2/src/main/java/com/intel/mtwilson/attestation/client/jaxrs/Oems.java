/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.attestation.client.jaxrs;

import com.intel.mtwilson.jaxrs2.client.MtWilsonClient;
import com.intel.mtwilson.as.rest.v2.model.Oem;
import com.intel.mtwilson.as.rest.v2.model.OemCollection;
import com.intel.mtwilson.as.rest.v2.model.OemFilterCriteria;
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
 * <code>Oems</code> is the class that allows creation , deletion  and updates to OEM objects.
 * 
 * @author ssbangal
 */
public class Oems extends MtWilsonClient {
    
    Logger log = LoggerFactory.getLogger(getClass().getName());

    public Oems(URL url) throws Exception{
        super(url);
    }

    public Oems(Properties properties) throws Exception {
        super(properties);
    }
    
     /**
     * Creates an Oem object with the specified parameters. This would be used to associate with BIOS MLE[Measured Launch Environment] during
     * the creation of BIOS MLE. Only Name is the required parameter. Description field is optional.
     * @param oem - Oem object that needs to be created having the name and description. If the caller specifies the ID,
     * it has to be a valid UUID. If not provided, it would be automatically generated.
     * @return <code> Oem </code> that is created.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions oems:create
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType POST
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8443/mtwilson/v2/oems
     * Input: {"name":"Intel","description":"Intel OEM"}
     * Output: {"id":"31741556-f5c7-4eb6-a713-338a23e43b93","name":"Intel","description":"Intel OEM"}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  Properties prop = My.configuration().getClientProperties();
     *  Oems client = new Oems(prop);
     *  Oem oem = new Oem();
     *  oem.setName("Intel");
     *  oem.setDescription("Intel OEM");
     *  Oem createOem = client.createOem(oem);
     * </pre>
     */
    public Oem createOem(Oem oem) {
        log.debug("target: {}", getTarget().getUri().toString());
        Oem newOem = getTarget().path("oems").request().accept(MediaType.APPLICATION_JSON).post(Entity.json(oem), Oem.class);
        return newOem;
    }
    
    /**
     * Deletes the Oem with the specified UUID from the system. An OEM can be deleted only if it is not associated with any BIOS MLE.
     * @param uuid - UUID of the Oem that has to be deleted.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions oems:delete
     * @mtwContentTypeReturned N/A
     * @mtwMethodType DELETE
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8443/mtwilson/v2/oems/31741556-f5c7-4eb6-a713-338a23e43b93
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  Properties prop = My.configuration().getClientProperties(); 
     *  Oems client = new Oems(prop);
     *  client.deleteOem("31741556-f5c7-4eb6-a713-338a23e43b93");
     * </pre>
     */
    public void deleteOem(String uuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("id", uuid);
        Response obj = getTarget().path("oems/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).delete();
        if( !obj.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
            throw new WebApplicationException("Delete OEM failed");
        }
    }

    /**
     * Deletes the Oem(s) matching the specified search criteria. 
     * @param criteria OemFilterCriteria object specifying the search criteria. The search options include
     * id, nameEqualTo and nameContains.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions oems:delete,search
     * @mtwContentTypeReturned N/A
     * @mtwMethodType DELETE
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/oems?nameContains=admin
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  Oems client = new Oems(My.configuration().getClientProperties());
     *  OemFilterCriteria criteria = new OemFilterCriteria();
     *  criteria.nameContains = "admin";
     *  client.deleteOem(criteria);
     * </pre>
     */
    public void deleteOem(OemFilterCriteria criteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        Response obj = getTargetPathWithQueryParams("oems", criteria).request(MediaType.APPLICATION_JSON).delete();
        if( !obj.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
            throw new WebApplicationException("Delete oem failed");
        }
    }
    
    /**
     * Updates the details of the Oem in the system. Only description of the Oem can be updated.
     * @param oem - Oem that needs to be updated.
     * @return <code> Oem </code> that is updated.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions oems:store
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType PUT
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8443/mtwilson/v2/oems/31741556-f5c7-4eb6-a713-338a23e43b93
     * Input: {"description":"Intel OEM updated"}
     * Output: {"id": "31741556-f5c7-4eb6-a713-338a23e43b93","description": "Intel OEM updated" }
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  Properties prop = My.configuration().getClientProperties();
     *  Oems client = new Oems(prop);  
     *  Oem oem = new Oem();
     *  oem.setId(UUID.valueOf("31741556-f5c7-4eb6-a713-338a23e43b93"));
     *  oem.setDescription("Intel OEM updated");
     *  oem = client.editOem(oem);
     * </pre>
     */
    public Oem editOem(Oem oem) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("id", oem.getId().toString());
        Oem newOem = getTarget().path("oems/{id}").resolveTemplates(map).request().accept(MediaType.APPLICATION_JSON).put(Entity.json(oem), Oem.class);
        return newOem;
    }
    
     /**
     * Retrieves the Oem object with the specified UUID
     * @param uuid - UUID of the Oem to be retrieved
     * @return <code> Oem </code> matching the specified UUID.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions oems:retrieve
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8443/mtwilson/v2/oems/31741556-f5c7-4eb6-a713-338a23e43b93
     * Output: {"id":"31741556-f5c7-4eb6-a713-338a23e43b93","name":"Intel","description":"Intel OEM"}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  Properties prop = My.configuration().getClientProperties();
     *  Oems client = new Oems(prop);
     *  Oem retrieveOem = client.retrieveOem("31741556-f5c7-4eb6-a713-338a23e43b93");
     * </pre>
     */
    public Oem retrieveOem(String uuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("id", uuid);
        Oem oem = getTarget().path("oems/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).get(Oem.class);
        return oem;
    }
    
    /**
     * Searches for the Oem's with the specified set of criteria
     * @param criteria OemFilterCriteria specifying the filter criteria.
     * The possible search options include id, nameEqualTo and nameContains. 
     * If in case the caller needs the list of all records, filter option can to be set to false. [Ex: /v2/oems?filter=false]
     * @return OemCollection having the list of Oems that meet the specified filter criteria
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions oems:search
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8443/mtwilson/v2/oems?nameContains=Intel
     * Output: {"oems":[{"id":"f310b4e3-1f9c-4687-be60-90260262afd9","name":"Intel Corporation","description":"Intel Corporation"}]}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  Properties prop = My.configuration().getClientProperties();
     *  Oems client = new Oems(prop);
     *  OemFilterCriteria criteria = new OemFilterCriteria();
     *  criteria.nameContains = "intel";
     *  OemCollection oems = client.searchOems(criteria);
     * </pre>
     */
    public OemCollection searchOems(OemFilterCriteria criteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        //OemCollection oems = getTarget().path("oems").queryParam("nameContains", name).request(MediaType.APPLICATION_JSON).get(OemCollection.class);
        OemCollection oems = getTargetPathWithQueryParams("oems", criteria).request(MediaType.APPLICATION_JSON).get(OemCollection.class);
        return oems;
    }
}
