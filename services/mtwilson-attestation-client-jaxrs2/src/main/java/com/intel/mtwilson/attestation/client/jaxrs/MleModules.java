/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.attestation.client.jaxrs;

import com.intel.mtwilson.jaxrs2.client.MtWilsonClient;
import com.intel.mtwilson.as.rest.v2.model.MleModule;
import com.intel.mtwilson.as.rest.v2.model.MleModuleCollection;
import com.intel.mtwilson.as.rest.v2.model.MleModuleFilterCriteria;
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
 *<code> MleModules </code> used to create, update, delete, search and retrieve MleModules from the system.
 * @author ssbangal
 */
public class MleModules extends MtWilsonClient {
    
    Logger log = LoggerFactory.getLogger(getClass().getName());

    public MleModules(URL url) throws Exception{
        super(url);
    }

    public MleModules(Properties properties) throws Exception {
        super(properties);
    }
    
    /**
     * Creates a new module white list for the Mle specified. Currently VMware ESXi and OpenSource Xen/KVM support
     * module based attestation. When the Mle is created, for hypervisors supporting MODULE
     * based attestation, PCR 19 would be set to empty. Using this API all the modules that get extended to PCR 19 should 
     * be configured. Since Module based attestation is supported only for PCR 19, it is not applicable for BIOS type MLEs. <br>
     * Creation of Module white lists could be automated using the RPC automation APIs .
     * @param obj - MleModule object specifying the Module details and the Mle for which it has to be associated.
     * For creating Module whitelists user has to specify the Name, Version, OsUUID (UUID of the OS that needs to be associated), ComponentName, DigestValue,
     * EventName, ExtendedToPCR & UseHostSpecificDigest have to be specified. The PackageName, PackageVendor, PackageVersion, Description are optional.
     * The UseHostSpecificDigest flag has to be set only for modules that vary across hosts (each host will have a unique value).
     * The possible values for EventName include
     * Vim25Api.HostTpmSoftwareComponentEventDetails,Vim25Api.HostTpmOptionEventDetails,Vim25Api.HostTpmBootSecurityOptionEventDetails,
     * Vim25Api.HostTpmCommandEventDetails & OpenSource.EventName.
     * Note that the first 4 event types are for ESXi host type. For the linux hosts, the last event name needs to be used.
     * @return - Created MleModule object.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions mle_modules:create
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType POST
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/mles/9a16973b-5b17-49a8-b508-3f5436c8f944/modules
     * Input: {"module_name":"New Module 1","module_value":"CCCCAAAAE793491B1C6EA0FD8B46CD9F32E592FC","extended_to_pcr":"19",
     * "package_vendor":"VMware","package_name":"PackageName","event_name":"Vim25Api.HostTpmSoftwareComponentEventDetails",
     * "use_host_specific_digest":"false","description":"Module addition testing"}
     * Output: {"id":"f4b25e23-9114-46f1-b0cb-8e2654514f5d","mle_uuid":"9a16973b-5b17-49a8-b508-3f5436c8f944","module_name":"New Module 1",
     * "module_value":"CCCCAAAAE793491B1C6EA0FD8B46CD9F32E592FC","event_name":"Vim25Api.HostTpmSoftwareComponentEventDetails",
     * "extended_to_pcr":"19","package_name":"PackageName","package_vendor":"VMware","use_host_specific_digest":false,"description":"Module addition testing"}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *      MleModules client = new MleModules(My.configuration().getClientProperties());
     *      MleModule obj = new MleModule();
     *      obj.setModuleName("New Module 1");
     *      obj.setModuleValue("CCCCCB19E793491B1C6EA0FD8B46CD9F32E592FC");
     *      obj.setMleUuid("9a16973b-5b17-49a8-b508-3f5436c8f944");
     *      obj.setEventName("Vim25Api.HostTpmSoftwareComponentEventDetails");
     *      obj.setExtendedToPCR("19");
     *      obj.setPackageName("PackageName");
     *      obj.setPackageVendor("VMware");
     *      obj.setUseHostSpecificDigest(Boolean.FALSE);
     *      obj.setDescription("Module addition testing");
     *      MleModule createMleModule = client.createMleModule(obj);
     * </pre>
     */
    public MleModule createMleModule(MleModule obj) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("mle_id", obj.getMleUuid());
        MleModule newObj = getTarget().path("mles/{mle_id}/modules").resolveTemplates(map)
                .request().accept(MediaType.APPLICATION_JSON).post(Entity.json(obj), MleModule.class);
        return newObj;
    }
    
     /**
     * Deletes the specified module white list for the Mle.
     * @param mleUuid - UUID of the Mle for which the module whitelist has to be deleted.
     * @param uuid - UUID Of the Mle Module to be deleted
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions mle_modules:delete
     * @mtwContentTypeReturned N/A
     * @mtwMethodType DELETE
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/mles/9a16973b-5b17-49a8-b508-3f5436c8f944/modules/f4b25e23-9114-46f1-b0cb-8e2654514f5d
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *      MleModules client = new MleModules(My.configuration().getClientProperties());
     *      client.deleteMleModule("66e999af-e9eb-43cc-9cbf-dcb73af1963b", "5ae636d0-e748-4d30-9660-f797956d4bb7");             * 
     * </pre>
     */
    public void deleteMleModule(String mleUuid, String uuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("mle_id", mleUuid);
        map.put("id", uuid); 
        Response obj = getTarget().path("mles/{mle_id}/modules/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).delete();
        log.debug(obj.toString());
    }
    
    /**
     * Deletes the Module white lists of the specified MLE using the filter criteria.
     * @param criteria MleModuleFilterCriteria object specifying the search criteria. Search options supported
     * include id, nameContains, and valueEqualTo.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions mle_modules:delete,search
     * @mtwContentTypeReturned N/A
     * @mtwMethodType DELETE
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/mles/9a16973b-5b17-49a8-b508-3f5436c8f944/modules?nameContains=New
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *   MleModules client = new MleModules(My.configuration().getClientProperties());
     *   MleModuleFilterCriteria criteria = new MleModuleFilterCriteria();
     *   criteria.mleUuid = UUID.valueOf("9a16973b-5b17-49a8-b508-3f5436c8f944");
     *   criteria.nameContains = "New";
     *  client.deleteMleModule(criteria);
     * </pre>
     */
    public void deleteMleModule(MleModuleFilterCriteria criteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("mle_id", criteria.mleUuid);
        Response obj = getTargetPathWithQueryParams("mles/{mle_id}/modules", criteria).request(MediaType.APPLICATION_JSON).delete();
        if( !obj.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
            throw new WebApplicationException("Delete Mle modules failed");
        }
    }
    
    /**
     * Updates the module white list for the Mle specified. Only digest value and description fields are allowed to be updated..
     * @param obj - MleModule to be updated
     * @return - Updated MleModule object
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions mle_modules:store
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType PUT
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/mles/9a16973b-5b17-49a8-b508-3f5436c8f944/modules/f4b25e23-9114-46f1-b0cb-8e2654514f5d
     * Input: {"description":"Module update testing"}
     * Output: {"id":"f4b25e23-9114-46f1-b0cb-8e2654514f5d","mle_uuid":"9a16973b-5b17-49a8-b508-3f5436c8f944","description":"Module update testing"}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *      MleModules client = new MleModules(My.configuration().getClientProperties()); 
     *      MleModule obj = new MleModule();
     *      obj.setMleUuid("9a16973b-5b17-49a8-b508-3f5436c8f944");
     *      obj.setId(UUID.valueOf("f4b25e23-9114-46f1-b0cb-8e2654514f5d"));
     *      obj.setDescription("Module update testing");
     *      MleModule newObj = client.editMleModule(obj);
     *  }
     */
    public MleModule editMleModule(MleModule obj) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("mle_id", obj.getMleUuid());
        map.put("id", obj.getId().toString()); 
        MleModule newObj = getTarget().path("mles/{mle_id}/modules/{id}").resolveTemplates(map).request().accept(MediaType.APPLICATION_JSON).put(Entity.json(obj), MleModule.class);
        return newObj;
    }
    
    /**
     * Retrieves the details of the specified MleModule
     * @param mleUuid - UUID of the associated Mle
     * @param uuid - UUID of the module to be retrieved.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions mle_modules:retrieve
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/mles/9a16973b-5b17-49a8-b508-3f5436c8f944/modules/f4b25e23-9114-46f1-b0cb-8e2654514f5d
     * Output: {"id":"f4b25e23-9114-46f1-b0cb-8e2654514f5d","mle_uuid":"f4b25e23-9114-46f1-b0cb-8e2654514f5d",
     * "module_name":"componentName.New Module 1","event_name":"Vim25Api.HostTpmSoftwareComponentEventDetails",
     * "extended_to_pcr":"19","package_name":"PackageName","package_vendor":"VMware","description":"Module update testing"}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *      MleModules client = new MleModules(My.configuration().getClientProperties());
     *      MleModule obj = client.retrieveMleModule("9a16973b-5b17-49a8-b508-3f5436c8f944", "f4b25e23-9114-46f1-b0cb-8e2654514f5d");
     * </pre>
     */
    public MleModule retrieveMleModule(String mleUuid, String uuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("mle_id", mleUuid);
        map.put("id", uuid);
        MleModule obj = getTarget().path("mles/{mle_id}/modules/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).get(MleModule.class);
        return obj;
    }
    
     /**
     * Searches for the Module whitelist satisfying the specified filter criteria. 
     * @param criteria MleModuleFilterCriteria  object specifying the filter criteria. Currently supported
     * search options include id, nameContains, and valueEqualTo.
     * If in case the caller needs the list of all records, filter option can to be set to false. [Ex: /modules?filter=false]
     * @return <code> MleModuleCollection</code> having the list of MleModules matching the specified criteria.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions mle_modules:search
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/mles/9a16973b-5b17-49a8-b508-3f5436c8f944/modules?nameContains=New
     * Output: {"mle_modules":[{"id":"f4b25e23-9114-46f1-b0cb-8e2654514f5d","mle_uuid":"9a16973b-5b17-49a8-b508-3f5436c8f944",
     * "module_name":"componentName.New Module 1","event_name":"Vim25Api.HostTpmSoftwareComponentEventDetails",
     * "extended_to_pcr":"19","package_name":"PackageName","package_vendor":"VMware","description":"Module update testing"}]} 
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *   MleModules client = new MleModules(My.configuration().getClientProperties());
     *   MleModuleFilterCriteria criteria = new MleModuleFilterCriteria();
     *   criteria.mleUuid = UUID.valueOf("9a16973b-5b17-49a8-b508-3f5436c8f944");
     *   criteria.nameContains = "New";
     *   MleModuleCollection searchMleModules = client.searchMleModules(criteria);
     * </pre>
     */
    public MleModuleCollection searchMleModules(MleModuleFilterCriteria criteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("mle_id", criteria.mleUuid);
        MleModuleCollection objCollection = getTargetPathWithQueryParams("mles/{mle_id}/modules", criteria)
                .resolveTemplates(map).request(MediaType.APPLICATION_JSON).get(MleModuleCollection.class);
        return objCollection;
    }
    
}
