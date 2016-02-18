/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.attestation.client.jaxrs;

import com.intel.mtwilson.jaxrs2.client.MtWilsonClient;
import com.intel.mtwilson.as.rest.v2.model.Mle;
import com.intel.mtwilson.as.rest.v2.model.MleCollection;
import com.intel.mtwilson.as.rest.v2.model.MleFilterCriteria;
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
 * <code> Mle </code> is the class used to create, update, delete, search and retreive MLE's .
 * @author ssbangal
 */
public class Mles extends MtWilsonClient {
    
    Logger log = LoggerFactory.getLogger(getClass().getName());

    public Mles(URL url) throws Exception{
        super(url);
    }

     /**
     * Constructor to create the <code> Mles </code> object.
     * @param properties <code> Properties </code> object to initialize the <code>Mles</code> with Mt.Wilson properties 
     * Use <code>MyConfiguration.getClientProperties()</code> to get the Properties to use for initialization
     * @throws Exception 
     * 
     */
    public Mles(Properties properties) throws Exception {
        super(properties);
    }
    
     /**
     * Creates a new MLE (Measured Launch Environment]. MLEs can be either BIOS or OS/Hypervisor. MLEs define what the 
     * good known values/white list values/finger print should be. When these MLEs are associated with the hosts for attestation, then
     * the measured values from the host are compared against the good known values defined for the MLEs. If they match then
     * that component {BIOS or OS/Hypervisor} is trusted. <br>
     * Hosts are always associated with both BIOS and OS/Hypervisors MLEs. If both the MLEs evaluate to trusted state, then the host
     * is trusted. If either one is untrusted, the the overall trust status of the host is untrusted. <br>
     * Instead of creating the OS/OEM & MLEs manually, users can opt to use the automation APIs.
     * @mtwPreRequisite For creating BIOS MLE, the OEM has to be created first since BIOS is always associated with the OEM. 
     * For OS/Hypervisor MLE, the OS on which the hypervisor would be installed should be configured first. In case of VMware ESXi
     * and Citrix XenServer there are no separation between the OS & Hypervisor components. They are the same. But Open Source hypervisors
     * like Xen & KVM can be installed on Ubuntu/RHEL & SUSE. <br>
     * Currently on Xen & KVM installed on Ubuntu, RHEL and SUSE are supported in the system.
     * @param obj MLE object that needs to be created. For creating BIOS MLEs, user has to specify the Name, Version, Attestation_Type as 
     * PCR [Defines how the verification of the measurements are done. Possible options are PCR & MODULE], MLE_Type as BIOS
     * [Possible options are BIOS and VMM], optional description, list of ManifestData for each of the BIOS PCRs to be 
     * verified[Valid BIOS PCR names are 0, 1, 2, 3, 4 & 5] and UUID of the OEM that needs to be associated with the MLE. If the user 
     * wants to set the white list values for the PCRs at a later point of time, then can do so and
     * during the creation of MLEs set them to empty strings. <br>
     * For creating VMM MLEs, user has to specify the Name, Version, Attestation_Type as either PCR or Module [For VMware ESXi,
     * Open Source Xen & KVM it is Module. For Citrix XenServer it is PCR], MLE_Type as VMM, optional description, list of 
     * ManifestData for each of the VMM PCRs to be verified [Valid VMM PCRs names are 17, 18, 19 & 20. PCR 20 is valid only
     * for VMware ESXi. Currently only PCR 19 provides module level information. So, user has to call createMleModules
     * method to configure the modules that gets extended to PCR 19 [During the creation of MLE, PCR 19 should be set to empty string 
     * for MODULE Attestation_Type], and UUID of the OS that needs to be associated with the MLE. 
     * @return Mle object created.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions mles:create
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType POST
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8443/mtwilson/v2/mles
     * Input: {"name":"vmmmle","version":"1.2.3","description":"Test","attestation_type":"MODULE","mle_type":"VMM","os_uuid":"2ffa05bd-ca9f-11e3-8449-005056b5643f","mle_manifests":[{"name": "18", "value": "BDC83B19E793491B1C6EA0FD8B46CD9F32E592FC"}]}
     * Output: {"id":"4804cb83-5319-423f-8944-c687145dd5eb","name":"vmmmle","version":"1.2.3","attestation_type":"MODULE","mle_type":"VMM","description":"Test","os_uuid":"2ffa05bd-ca9f-11e3-8449-005056b5643f","mle_manifests":[{"name":"18","value":"BDC83B19E793491B1C6EA0FD8B46CD9F32E592FC"}]}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     * Properties prop = My.configuration().getClientProperties();
     * Oss osClient = new Oss(prop);
     * Mles mleClient = new Mles(prop);
     * UUID osUuid = null;
     * OsFilterCriteria osCriteria = new OsFilterCriteria();
     * osCriteria.nameContains = "VMWare";
     * OsCollection oss = osClient.searchOss(osCriteria);
     *  for (Os os : oss.getOss()) {
     *      osUuid = os.getId();
     *  }
     *  
     *  Mle vmmMle = new Mle();
     *  vmmMle.setName("vmmmle");
     *  vmmMle.setVersion("1.2.3");
     *  vmmMle.setAttestationType(Mle.AttestationType.MODULE);
     *  vmmMle.setMleType(Mle.MleType.VMM);
     *  vmmMle.setOsUuid(osUuid.toString());
     *  vmmMle.setSource("192.168.0.1"); // host from which the white lists are extracted

     *  List<ManifestData> vmmPcrs = new ArrayList<>();
     *  vmmPcrs.add(new ManifestData("18", "BDC83B19E793491B1C6EA0FD8B46CD9F32E592FC");
     *  vmmPcrs.add(new ManifestData("19", "");
     *  vmmMle.setMleManifests(vmmPcrs);
     *  vmmMle = mleClient.createMle(vmmMle);
     *      
     */
    public Mle createMle(Mle obj) {
        log.debug("target: {}", getTarget().getUri().toString());
        Mle newObj = getTarget().path("mles").request().accept(MediaType.APPLICATION_JSON).post(Entity.json(obj), Mle.class);
        return newObj;
    }

     /**
     * Deletes the Mle with the specified UUID from the system.
     * @param uuid - UUID of the Mle that has to be deleted.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions mles:delete
     * @mtwContentTypeReturned N/A
     * @mtwMethodType DELETE
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8443/mtwilson/v2/mles/4804cb83-5319-423f-8944-c687145dd5eb
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  Properties prop = My.configuration().getClientProperties(); 
     *  Mles client = new Mles(prop);
     *  client.deleteMles("4804cb83-5319-423f-8944-c687145dd5eb");
     * </pre>
     */
    public void deleteMle(String uuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("id", uuid);
        Response obj = getTarget().path("mles/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).delete();
        if( !obj.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
            throw new WebApplicationException("Delete MLE failed");
        }
    }
    
    /**
     * Deletes the Mle(s) matching the specified search criteria. 
     * @param criteria MleFilterCriteria object specifying the search criteria. Search options supported
     * include id, nameEqualTo, nameContains, osUuid and oemUuid.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions mles:delete,search
     * @mtwContentTypeReturned N/A
     * @mtwMethodType DELETE
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/mles?nameContains=mle
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  Mles client = new Mles(My.configuration().getClientProperties());
     *  MleFilterCriteria criteria = new MleFilterCriteria();
     *  criteria.nameContains = "mle";
     *  client.deleteMle(criteria);
     * </pre>
     */
    public void deleteMle(MleFilterCriteria criteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        Response obj = getTargetPathWithQueryParams("mles", criteria).request(MediaType.APPLICATION_JSON).delete();
        if( !obj.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
            throw new WebApplicationException("Delete mle failed");
        }
    }
    
    /**
     * Updates the MLE in the system. Only the description can be updated. For updating the whitelist values the caller
     * has to use either use the MlePcrs/MleModules resources.
     * Instead of updating the OS/OEM & MLEs manually, users can opt to use the RPC automation APIs.
     * @param obj MLE object having the details that needs to be updated.
     * @return Updated <code> Mle </code> object.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions mles:store
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType PUT
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8443/mtwilson/v2/mles/4804cb83-5319-423f-8944-c687145dd5eb
     * Input: {"description":"Added description"}
     * Output: {"id":"4804cb83-5319-423f-8944-c687145dd5eb","description":"Updated description"}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     * Properties prop = My.configuration().getClientProperties();
     * Mles client = new Mles(prop);
     * Mle vmmMle = new Mle();
     * vmmMle.setId(UUID.valueOf("4804cb83-5319-423f-8944-c687145dd5eb"));
     * vmmMle.setDescription("Added description");
     * vmmMle = client.editOs(os);

     */
    public Mle editMle(Mle obj) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("id", obj.getId().toString());
        Mle newObj = getTarget().path("mles/{id}").resolveTemplates(map).request().accept(MediaType.APPLICATION_JSON).put(Entity.json(obj), Mle.class);
        return newObj;
    }    

    /**
     * Retrieves the Mle with the specified UUID from the system.
     * @param uuid - UUID of the MLE to be retrieved
     * @return Mle matching the specified UUID.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions mles:retrieve
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8443/mtwilson/v2/mles/4804cb83-5319-423f-8944-c687145dd5eb
     * Output: {"id":"4804cb83-5319-423f-8944-c687145dd5eb","name":"vmmmle","version":"1.2.3","attestation_type":"MODULE",
     * "mle_type":"VMM","description":"Updated description","os_uuid":"2ffa05bd-ca9f-11e3-8449-005056b5643f"}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  Properties prop = My.configuration().getClientProperties();
     *  Mles client = new Mles(prop);
     *  Mle retrieveMle = client.retrieveMle("4804cb83-5319-423f-8944-c687145dd5eb");
     * </pre>
     */
    public Mle retrieveMle(String uuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("id", uuid);
        Mle obj = getTarget().path("mles/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).get(Mle.class);
        return obj;
    }
    
    /**
     * Searches for Mles matching the specified filter criteria.
     * @param criteria MleFilterCriteria object specifying the filter criteria. Search options supported
     * include id, nameEqualTo, nameContains, osUuid, oemUuid & mleType.
     * If in case the caller needs the list of all records, filter option can to be set to false. [Ex: /v2/mles?filter=false]
     * @return MleCollection having the list of the Mles that match the specified criteria.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions mles:search
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8443/mtwilson/v2/mles?nameContains=mle
     * Output: {"mles":[{"id":"4804cb83-5319-423f-8944-c687145dd5eb","name":"vmmmle","version":"1.2.3","attestation_type":"MODULE","mle_type":"VMM",
     * "description":"Updated description","os_uuid":"2ffa05bd-ca9f-11e3-8449-005056b5643f"}]}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     * Properties prop = My.configuration().getClientProperties();
     * Mles client = new Mles(prop);
     * MleFilterCriteria criteria = new MleFilterCriteria();
     * criteria.nameContains = "mle";
     * MleCollection mles = client.searchMles(criteria);
     * </pre>
     */
    public MleCollection searchMles(MleFilterCriteria criteria) {
        log.debug("target: {}", getTarget().getUri().toString());
        MleCollection objCollection = getTargetPathWithQueryParams("mles", criteria).request(MediaType.APPLICATION_JSON).get(MleCollection.class);
        return objCollection;
    }
    
}
