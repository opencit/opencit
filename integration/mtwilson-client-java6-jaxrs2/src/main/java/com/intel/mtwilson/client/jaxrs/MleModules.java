/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.client.jaxrs;

import com.intel.mtwilson.as.rest.v2.model.MleModule;
import com.intel.mtwilson.as.rest.v2.model.MleModuleCollection;
import com.intel.mtwilson.as.rest.v2.model.MleModuleFilterCriteria;
import java.net.URL;
import java.util.HashMap;
import java.util.Properties;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *<code> MleModules </code> used to create, update, delete, search and retrieve MleModules from the system.
 * @author ssbangal
 */
public class MleModules extends MtWilsonClient {
    
    Logger log = LoggerFactory.getLogger(getClass().getName());

    public MleModules(URL url) throws Exception{
        super(url);
    }

     /**
     * Constructor to create the <code> MleModules </code> object.
     * @param properties <code> Properties </code> object to initialize the <code>MleModules</code> with Mt.Wilson properties 
     * Use <code>MyConfiguration.getClientProperties()</code> to get the Properties to use for initialization
     * @throws Exception 
     * 
     * <i><u>Sample Java API call :</u></i><br>
     * {@code
     *   MleModules client = new MleModules(My.configuration().getClientProperties());
     * }
     */
    public MleModules(Properties properties) throws Exception {
        super(properties);
    }
    
    
     /**
     * Search for MLEPcrs that match a specified Filter criteria.
     * @param criteria <code> MleModuleFilterCriteria </code> used to specify the parameters of search. 
     *  criteria can be one of indexEqualTo, valueEqualTo and id
     * @return <code> MleModuleCollection</code>, list of the MlePcr's that match the specified collection.
     * 
     * The search always returns back a collection.
     * <p>
     * <i><u>Roles Needed:</u></i> TOCHECK?
     * <p>
     * <i><u>Content type returned:</u></i>JSON/XML/YAML<br>
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: GET</i><br>
     * https://10.1.71.234:8181/mtwilson/v2/mles/31021a8a-de64-4c5f-b314-8d3f077a55e5/modules?id=51bd39cc-24af-4780-91e5-d9bcfe13ec6f
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * {
     *   mle_modules: [1]
     *   0:  {
     *       id: "51bd39cc-24af-4780-91e5-d9bcfe13ec6f"
     *       mle_uuid: "51bd39cc-24af-4780-91e5-d9bcfe13ec6f"
     *       module_name: "componentName.20_New one"
     *       event_name: "Vim25Api.HostTpmSoftwareComponentEventDetails"
     *       extended_to_pcr: "19"
     *       package_name: "PackageName is so and so"
     *       package_vendor: "VMware"
     *       description: "API UPdate test"
     *   }
     *  }
     * <i><u>Sample Java API call :</u></i><br>
     * {@code
     *      MleModules client = new MleModules(My.configuration().getClientProperties());
     *      MleModuleFilterCriteria criteria = new MleModuleFilterCriteria();
     *      criteria.mleUuid = UUID.valueOf("66e999af-e9eb-43cc-9cbf-dcb73af1963b");     *     
     *      MleModuleCollection searchMleModules = client.searchMleModules(criteria);
     * }
     */
    public MleModuleCollection searchMleModules(MleModuleFilterCriteria criteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("mle_id", criteria.mleUuid);
        MleModuleCollection objCollection = getTargetPathWithQueryParams("mles/{mle_id}/modules", criteria)
                .resolveTemplates(map).request(MediaType.APPLICATION_JSON).get(MleModuleCollection.class);
        return objCollection;
    }
    
    /**
     * Retrieves the MLE Module with the specified mle id and Module ID
     * @param mleUuid - UUID of the MLE To be retrieved
     * @param uuid - UUID of the module to be retrieved.     * 
     * 
     * <p>
     * <i><u>Roles Needed:</u></i> TO CHECK?
     * <p>
     * <i><u>Content type returned:</u></i>JSON/XML/YAML
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: GET</i><br>
     * https://10.1.71.234:8181/mtwilson/v2/mles/31021a8a-de64-4c5f-b314-8d3f077a55e5/modules/51bd39cc-24af-4780-91e5-d9bcfe13ec6f
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * {
     *       id: "51bd39cc-24af-4780-91e5-d9bcfe13ec6f"
     *       mle_uuid: "51bd39cc-24af-4780-91e5-d9bcfe13ec6f"
     *       module_name: "componentName.20_New one"
     *       event_name: "Vim25Api.HostTpmSoftwareComponentEventDetails"
     *       extended_to_pcr: "19"
     *       package_name: "PackageName is so and so"
     *       package_vendor: "VMware"
     *       description: "API UPdate test"
     *  }
     * @return <code> MleModule </code> that is retrieved from the backend
     *  
     * <i><u>Sample Java API call :</u></i><br>
     * {@code
     *      MleModules client = new MleModules(My.configuration().getClientProperties());
     *      MleModule obj = client.retrieveMleModule("66e999af-e9eb-43cc-9cbf-dcb73af1963b", "0a863b84-e65b-4a23-b281-545d0f4afaf8");
     * }
     */
    public MleModule retrieveMleModule(String mleUuid, String uuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("mle_id", mleUuid);
        map.put("id", uuid);
        MleModule obj = getTarget().path("mles/{mle_id}/modules/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).get(MleModule.class);
        return obj;
    }

    /**
     * Creates the MLE Modules in the database.
     * @param obj - MLE Module to be created
     * @return - MLE Module post creation
     *  
     * <i><u>Roles Needed:</u></i> TOCHECK?
     * <p>
     * <i><u>Content type returned:</u></i>JSON/XML/YAML<br>
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: POST</i><br>
     * https://10.1.71.234:8181/mtwilson/v2/mles/31021a8a-de64-4c5f-b314-8d3f077a55e5/modules
     * <p>
     * <i>Sample Input</i><br>
     *	{"module_name":"20_New one","module_value":"CCCCAAAAE793491B1C6EA0FD8B46CD9F32E592FC","extended_to_pcr":"19","package_vendor":"VMware","package_name":"PackageName is so and so","event_name":"Vim25Api.HostTpmSoftwareComponentEventDetails","use_host_specific_digest":"false","description":"Another testing"}
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * {
     *   id: "51bd39cc-24af-4780-91e5-d9bcfe13ec6f"
     *   mle_uuid: "31021a8a-de64-4c5f-b314-8d3f077a55e5"
     *   module_name: "20_New one"
     *   module_value: "CCCCAAAAE793491B1C6EA0FD8B46CD9F32E592FC"
     *   event_name: "Vim25Api.HostTpmSoftwareComponentEventDetails"
     *   extended_to_pcr: "19"
     *   package_name: "PackageName is so and so"
     *   package_vendor: "VMware"
     *   use_host_specific_digest: false
     *   description: "Another testing"
     *   }
     * <i><u>Sample Java API call :</u></i><br>
     * {@code
     *      MleModules client = new MleModules(My.configuration().getClientProperties());
     *      MleModule obj = new MleModule();
     *      obj.setModuleName("20_sakljfaslf");
     *      obj.setModuleValue("CCCCCB19E793491B1C6EA0FD8B46CD9F32E592FC");
     *      obj.setMleUuid("66e999af-e9eb-43cc-9cbf-dcb73af1963b");
     *      obj.setEventName("Vim25Api.HostTpmSoftwareComponentEventDetails");
     *      obj.setExtendedToPCR("19");
     *      obj.setPackageName("net-bnx2");
     *      obj.setPackageVendor("VMware");
     *      obj.setPackageVersion("2.0.15g.v50.11-7vmw.510.0.0.799733");
     *      obj.setUseHostSpecificDigest(Boolean.FALSE);
     *      obj.setDescription("Testing");
     *      MleModule createMleModule = client.createMleModule(obj);
     * }
     */
    public MleModule createMleModule(MleModule obj) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("mle_id", obj.getMleUuid().toString());
        MleModule newObj = getTarget().path("mles/{mle_id}/modules").resolveTemplates(map)
                .request().accept(MediaType.APPLICATION_JSON).post(Entity.json(obj), MleModule.class);
        return newObj;
    }

    /**
     * Update/Edits the MLE Modules in the database.
     * @param obj - MLE Module to be updated
     * @return - MLE Module post updation
     *  
     * <i><u>Roles Needed:</u></i> TOCHECK?
     * <p>
     * <i><u>Content type returned:</u></i>JSON/XML/YAML<br>
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: PUT</i><br>
     * https://10.1.71.234:8181/mtwilson/v2/mles/31021a8a-de64-4c5f-b314-8d3f077a55e5/modules/51bd39cc-24af-4780-91e5-d9bcfe13ec6f
     * <p>
     * <i>Sample Input</i><br>
     *	{"module_name":"20_New Updated","description":"API UPdate test"}
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * {
     *   id: "51bd39cc-24af-4780-91e5-d9bcfe13ec6f"
     *   mle_uuid: "31021a8a-de64-4c5f-b314-8d3f077a55e5"
     *   module_name: "20_New Updated"
     *   description: "API UPdate test"
     * }
     *  
     * <i><u>Sample Java API call :</u></i><br>
     * {@code
     *      MleModules client = new MleModules(My.configuration().getClientProperties()); 
     *      MleModule obj = new MleModule();
     *      obj.setMleUuid("66e999af-e9eb-43cc-9cbf-dcb73af1963b");
     *      obj.setId(UUID.valueOf("5ae636d0-e748-4d30-9660-f797956d4bb7"));
     *      obj.setModuleValue("DDDDDB19E793491B1C6EA0FD8B46CD9F32E592FC");
     *      obj.setDescription("Updating desc");
     *      MleModule newObj = client.editMleModule(obj);
     *  }
     */
    public MleModule editMleModule(MleModule obj) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("mle_id", obj.getMleUuid().toString());
        map.put("id", obj.getId().toString()); 
        MleModule newObj = getTarget().path("mles/{mle_id}/modules/{id}").resolveTemplates(map).request().accept(MediaType.APPLICATION_JSON).put(Entity.json(obj), MleModule.class);
        return newObj;
    }

     /**
     * Deletes the MLE PCR with the specified UUID from the database
     * @param mleUuid - MLE UUID of the MLE module  that has to be deleted.
     * @param uuid - UUID Of the MLE MOdule to be deleted
     * <p>
     * <i><u>Roles Needed:</u></i> TO CHECK
     * <p>
     * <i><u>Content type returned:</u></i>JSON/XML/YAML
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: DELETE</i><br>
     * https://10.1.71.234:8181/mtwilson/v2/mles/31021a8a-de64-4c5f-b314-8d3f077a55e5/modules/51bd39cc-24af-4780-91e5-d9bcfe13ec6f
     * <p>
     * <i><u>Sample Output: NA </u></i><br>
     * 
     * <i><u>Sample Java API call :</u></i><br>
     * {@code
     *      MleModules client = new MleModules(My.configuration().getClientProperties());
     *      client.deleteMleModule("66e999af-e9eb-43cc-9cbf-dcb73af1963b", "5ae636d0-e748-4d30-9660-f797956d4bb7");             * 
     * }
     */
    public void deleteMleModule(String mleUuid, String uuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("mle_id", mleUuid);
        map.put("id", uuid); 
        Response obj = getTarget().path("mles/{mle_id}/modules/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).delete();
        log.debug(obj.toString());
    }
    
}
