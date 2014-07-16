/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.client.jaxrs;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.jaxrs2.client.MtWilsonClient;
import com.intel.mtwilson.tag.model.SelectionKvAttribute;
import com.intel.mtwilson.tag.model.SelectionKvAttributeCollection;
import com.intel.mtwilson.tag.model.SelectionKvAttributeFilterCriteria;
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
public class SelectionKvAttributes extends MtWilsonClient {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Selections.class);

    public SelectionKvAttributes(URL url) throws Exception{
        super(url);
    }

    public SelectionKvAttributes(Properties properties) throws Exception {
        super(properties);
    }
    
    /**
     * Creates a new mapping between the selection and the key-value pair specified. 
     * @param obj SelectionKvAttribute object that needs to be created. User needs to specify the selection name
     * and the UUID of the key-value (KvAttribute) object.
     * @return Created SelectionKvAttribute object.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions tag_selection_kv_attributes:create
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType POST
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/tag-selection-kv-attributes
     * Input: {"selection_name":"Test","kv_attribute_id":"a847262e-8afe-4020-b40c-ce89dacb2b60"}
     * Output: {"id":"3910e752-4645-4caf-b78c-098458710a1e","kv_attribute_id":"97e1a998-0a9b-4004-bacc-5158c0288e00","selection_name":"Test"}
     * @mtwSampleApiCall
     * <pre>
     *  SelectionKvAttributes client = new SelectionKvAttributes(My.configuration().getClientProperties());
     *  SelectionKvAttribute skvObj = new SelectionKvAttribute();
     *  skvObj.setSelectionName("Test");
     *  skvObj.setKvAttributeId("97e1a998-0a9b-4004-bacc-5158c0288e00");
     *  skvObj = client.createSelectionKvAttribute(skvObj);
     * </pre>
     */
    public SelectionKvAttribute createSelectionKvAttribute(SelectionKvAttribute obj) {
        log.debug("target: {}", getTarget().getUri().toString());
        SelectionKvAttribute createdObj = getTarget().path("tag-selection-kv-attributes")
                .request().accept(MediaType.APPLICATION_JSON).post(Entity.json(obj), SelectionKvAttribute.class);
        return createdObj;
    }

    /**
     * Deletes an existing mapping between the Selection and the key-value pair.
     * @param uuid - UUID of the mapping entry that has to be deleted.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions tag_selection_kv_attributes:delete
     * @mtwContentTypeReturned N/A
     * @mtwMethodType DELETE
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/tag-selection-kv-attributes/e404ee8a-b114-40cc-b75f-a99d82fc11d7
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  SelectionKvAttributes client = new SelectionKvAttributes(My.configuration().getClientProperties());
     *  client.deleteSelectionKvAttribute("e404ee8a-b114-40cc-b75f-a99d82fc11d7");
     * </pre>
     */
    public void deleteSelectionKvAttribute(UUID uuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("id", uuid);
        Response obj = getTarget().path("tag-selection-kv-attributes/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).delete();
        if( !obj.getStatusInfo().getFamily().equals(Family.SUCCESSFUL)) {
            throw new WebApplicationException("Delete selection key-value attribute failed");
        }
    }

    /**
     * Deletes the mappings between the selection and the associated key-value pairs based on the 
     * search criteria specified 
     * @param criteria SelectionKvAttributeFilterCriteria object specifying the search criteria. The possible search options 
     * include nameEqualTo, nameContains, attrNameEqualTo, attrNameContains, attrValueContains, and attrValueEqualTo. 
     * @return N/A
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions tag_selection_kv_attributes:delete,search
     * @mtwContentTypeReturned N/A
     * @mtwMethodType DELETE
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/tag-selection-kv-attributes?attrValueContains=Folsom
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  SelectionKvAttributes client = new SelectionKvAttributes(My.configuration().getClientProperties());
     *  SelectionKvAttributeFilterCriteria criteria = new SelectionKvAttributeFilterCriteria();
     *  criteria.attrValueContains = "Folsom";
     *  client.deleteSelectionKvAttribute(criteria);
     * </pre>
     */
    public void deleteSelectionKvAttribute(SelectionKvAttributeFilterCriteria criteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        Response obj = getTargetPathWithQueryParams("tag-selection-kv-attributes", criteria).request(MediaType.APPLICATION_JSON).delete();
        
        if( !obj.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
            throw new WebApplicationException("Delete SelectionKvAttribute failed");
        }
    }
    
    
    /**
     * Retrieves the details of the mapping between selection and the key value pair with the specified ID. 
     * @param uuid - UUID of the selection key-value mapping to be retrieved
     * @return SelectionKvAttribute object matching the specified UUID.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions tag_selection_kv_attributes:retrieve
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/tag-selection-kv-attributes/3910e752-4645-4caf-b78c-098458710a1e
     * Output: {"id":"3910e752-4645-4caf-b78c-098458710a1e","selection_id":"24246b8a-b039-4fb2-9e4c-cee56e0a5893",
     * "kv_attribute_id":"97e1a998-0a9b-4004-bacc-5158c0288e00"}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  SelectionKvAttributes client = new SelectionKvAttributes(My.configuration().getClientProperties());
     *  SelectionKvAttribute retrieveSelectionKvAttribute = client.retrieveSelectionKvAttribute(UUID.valueOf("3910e752-4645-4caf-b78c-098458710a1e"));
     * </pre>
     */
    public SelectionKvAttribute retrieveSelectionKvAttribute(UUID uuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("id", uuid);
        SelectionKvAttribute role = getTarget().path("tag-selection-kv-attributes/{id}")
                .resolveTemplates(map).request(MediaType.APPLICATION_JSON).get(SelectionKvAttribute.class);
        return role;
    }

    /**
     * Retrieves the list of mappings between the selection and the associated key-value pairs based on the 
     * search criteria specified.  
     * @param criteria SelectionKvAttributeFilterCriteria object specifying the filter criteria. The possible search options 
     * include nameEqualTo, nameContains, attrNameEqualTo, attrNameContains, attrValueContains, and attrValueEqualTo.  
     * User can retrieve all the selections by setting the filter criteria to false. By default this filter
     * criteria would be set to true. [Ex: /v2/tag-selection-kv-attributes?filter=false]
     * @return SelectionKvAttributeCollection object with the list of all the SelectionKvAttribute objects matching the specified filter criteria
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions tag_selection_kv_attributes:search
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/tag-selection-kv-attributes?attrValueContains=Folsom
     * Output: {"selection_kv_attribute_values":
     * [{"selection_id":"61116006-1cb8-40df-bb8f-f89e609e678b","kv_attribute_id":"061fbaf6-c5a6-4fce-9f69-1a68e65c1281",
     * "kv_attribute_name":"city","kv_attribute_value":"Hillsboro","selection_name":"other"}]}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  SelectionKvAttributes client = new SelectionKvAttributes(My.configuration().getClientProperties());
     *  SelectionKvAttributeFilterCriteria criteria = new SelectionKvAttributeFilterCriteria();
     *  criteria.attrValueContains = "Folsom";
     *  SelectionKvAttributeCollection objCollection = client.searchSelectionKvAttributes(criteria);
     * </pre>
     */
    public SelectionKvAttributeCollection searchSelectionKvAttributes(SelectionKvAttributeFilterCriteria criteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        SelectionKvAttributeCollection objCollection = getTargetPathWithQueryParams("tag-selection-kv-attributes", criteria)
                .request(MediaType.APPLICATION_JSON).get(SelectionKvAttributeCollection.class);
        return objCollection;
    }
}
