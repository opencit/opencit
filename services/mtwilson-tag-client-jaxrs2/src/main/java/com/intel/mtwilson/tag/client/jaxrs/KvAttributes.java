/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.client.jaxrs;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.jaxrs2.client.MtWilsonClient;
import com.intel.mtwilson.tag.model.KvAttribute;
import com.intel.mtwilson.tag.model.KvAttributeCollection;
import com.intel.mtwilson.tag.model.KvAttributeFilterCriteria;
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
public class KvAttributes extends MtWilsonClient {
        
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(KvAttributes.class);

    public KvAttributes(URL url) throws Exception{
        super(url);
    }

    public KvAttributes(Properties properties) throws Exception {
        super(properties);
    }
    
    /**
     * Creates a new key value pair. If the key-value pair already exists in the system, an appropriate
     * message would be returned back to the caller. The user can specify the ID, which should be a valid
     * UUID to be used as the primary key. If not specified, a new UUID would be automatically generated.
     * After successful execution the caller would be returned back the new object created.
     * @param obj KvAttribute (Key-Value Attribute) object that needs to be created. 
     * @return Created KvAttribute object.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions tag_kv_attributes:create
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType POST
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/tag-kv-attributes
     * Input: {"name":"department","value":"finance"}
     * Output: {"id":"97e1a998-0a9b-4004-bacc-5158c0288e00","name":"department","value":"finance"}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  KvAttributes client = new KvAttributes(My.configuration().getClientProperties());
     *  KvAttribute kvAttrib = new KvAttribute();
     *  kvAttrib.setName("department");
     *  kvAttrib.setValue("finance");        
     *  kvAttrib = client.createKvAttribute(kvAttrib);
     * </pre>
     */
    public KvAttribute createKvAttribute(KvAttribute obj) {
        log.debug("target: {}", getTarget().getUri().toString());
        KvAttribute kvAttrObj = getTarget().path("tag-kv-attributes").request().accept(MediaType.APPLICATION_JSON).post(Entity.json(obj), KvAttribute.class);
        return kvAttrObj;
    }

    /**
     * Deletes the key value pair with the specified ID. 
     * @param uuid UUID of the key value pair that has to be deleted.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions tag_kv_attributes:delete
     * @mtwContentTypeReturned N/A
     * @mtwMethodType DELETE
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/tag-kv-attributes/a6544ff4-6dc7-4c74-82be-578592e7e3ba
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  KvAttributes client = new KvAttributes(My.configuration().getClientProperties());
     *  client.deleteRole("31741556-f5c7-4eb6-a713-338a23e43b93");
     * </pre>
     */
    public void deleteKvAttribute(UUID uuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("id", uuid);
        Response kvObj = getTarget().path("tag-kv-attributes/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).delete();
        if( !kvObj.getStatusInfo().getFamily().equals(Family.SUCCESSFUL)) {
            throw new WebApplicationException("Delete key-value attribute failed");
        }
    }
    
    /**
     * Deletes the key value pairs matching the specified search criteria. 
     * @param criteria KvAttributeFilterCriteria object specifying the search criteria. Possible search options include 
     * nameEqualTo, nameContains, valueEqualTo and valueContains.
     * @return N/A
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions tag_kv_attributes:delete,search
     * @mtwContentTypeReturned N/A
     * @mtwMethodType DELETE
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/tag-kv-attributes?nameEqualTo=country
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  KvAttributes client = new KvAttributes(My.configuration().getClientProperties());
     *  KvAttributeFilterCriteria criteria = new KvAttributeFilterCriteria();
     *  criteria.nameEqualTo = "country";
     *  client.deleteKvAttribute(criteria);
     * </pre>
     */
    public void deleteKvAttribute(KvAttributeFilterCriteria criteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        Response obj = getTargetPathWithQueryParams("tag-kv-attributes", criteria).request(MediaType.APPLICATION_JSON).delete();
        
        if( !obj.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
            throw new WebApplicationException("Delete KvAttribute failed");
        }
    }
    
    /**
     * Allows the user to edit an existing key value pair. Note that only the value of the key-value
     * pair can be edited. The user has to specify the ID on the query string and the value to be updated
     * in the body. If the specified ID does not exist in the system, appropriate error would be returned
     * back to the caller. 
     * @param obj KvAttribute object having the value that needs to be updated. 
     * @return Updated KvAttribute object.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions tag_kv_attributes:store
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType PUT
     * @mtwSampleRestCall
     * https://server.com:8181/mtwilson/v2/tag-kv-attributes/97e1a998-0a9b-4004-bacc-5158c0288e00
     * Input: {"value":"HR"}
     * Output: {"id":"97e1a998-0a9b-4004-bacc-5158c0288e00","value":"HR"}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  KvAttributes client = new KvAttributes(My.configuration().getClientProperties());
     *  KvAttribute kvAttrib = new KvAttribute();
     *  kvAttrib.setId(UUID.valueOf("97e1a998-0a9b-4004-bacc-5158c0288e00"));
     *  kvAttrib.setValue("HR");        
     *  kvAttrib = client.editKvAttribute(kvAttrib);
     * </pre>
     */
    public KvAttribute editKvAttribute(KvAttribute obj) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("id", obj.getId().toString());
        KvAttribute updatedKvObj = getTarget().path("tag-kv-attributes/{id}").resolveTemplates(map).request().accept(MediaType.APPLICATION_JSON).put(Entity.json(obj), KvAttribute.class);
        return updatedKvObj;
    }
    
    /**
     * Retrieves the key-value pair for the specified UUID.
     * @param uuid - UUID of the key-value pair to be retrieved
     * @return KvAttribute object matching the specified UUID.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions tag_kv_attributes:retrieve
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/tag-kv-attributes/97e1a998-0a9b-4004-bacc-5158c0288e00
     * Output: {"id":"97e1a998-0a9b-4004-bacc-5158c0288e00","name":"department","value":"HR"}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  KvAttributes client = new KvAttributes(My.configuration().getClientProperties());
     *  KvAttribute obj = client.retrieveKvAttribute(UUID.valueOf("31741556-f5c7-4eb6-a713-338a23e43b93"));
     * </pre>
     */
    public KvAttribute retrieveKvAttribute(UUID uuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("id", uuid);
        KvAttribute obj = getTarget().path("tag-kv-attributes/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).get(KvAttribute.class);
        return obj;
    }

    
    /**
     * Retrieves the key-value pairs based on the search criteria specified.  
     * @param criteria KvAttributeFilterCriteria object specifying the filter criteria. Possible search options include 
     * nameEqualTo, nameContains, valueEqualTo and valueContains.  If in case the caller needs the list of all 
     * the key value attributes, then the filter option has to be set to false. [Ex: /v2/tag-kv-attributes?filter=false]
     * @return KvAttributeCollection object with the list of all the KvAttribute objects matching the specified filter criteria
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions tag_kv_attributes:search
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/tag-kv-attributes?nameEqualTo=country
     * Output: {"kv_attributes":[{"id":"2676ee69-e42f-461b-824f-a6ec3d2c08f4","name":"country","value":"MX"},
     * {"id":"772c1358-feea-4827-bcf1-29cf3ca1a7d9","name":"country","value":"US"}]}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  KvAttributes client = new KvAttributes(My.configuration().getClientProperties());
     *  KvAttributeFilterCriteria criteria = new KvAttributeFilterCriteria();
     *  criteria.nameEqualTo = "country";
     *  KvAttributeCollection kvAttrs = client.searchKvAttributes(criteria);
     * </pre>
     */
    public KvAttributeCollection searchKvAttributes(KvAttributeFilterCriteria criteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        KvAttributeCollection kvattrs = getTargetPathWithQueryParams("tag-kv-attributes", criteria).request(MediaType.APPLICATION_JSON).get(KvAttributeCollection.class);
        return kvattrs;
    }    
}
