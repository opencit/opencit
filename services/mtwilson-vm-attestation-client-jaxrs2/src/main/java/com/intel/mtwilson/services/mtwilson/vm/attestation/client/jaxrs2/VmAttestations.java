/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.services.mtwilson.vm.attestation.client.jaxrs2;

import com.intel.dcsg.cpg.crypto.SimpleKeystore;
import com.intel.mtwilson.api.ApiException;
import com.intel.mtwilson.jaxrs2.client.MtWilsonClient;
import com.intel.mtwilson.as.rest.v2.model.HostAttestation;
import com.intel.mtwilson.as.rest.v2.model.VMAttestation;
import com.intel.mtwilson.as.rest.v2.model.VMAttestationCollection;
import com.intel.mtwilson.as.rest.v2.model.VMAttestationFilterCriteria;
import com.intel.mtwilson.jaxrs2.mediatype.CryptoMediaType;
import com.intel.mtwilson.saml.TrustAssertion;
import java.io.File;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Properties;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ssbangal
 */
public class VmAttestations extends MtWilsonClient {
    
    Logger log = LoggerFactory.getLogger(getClass().getName());
    Properties properties = null;

    public VmAttestations(URL url) throws Exception{
        super(url);
    }

    public VmAttestations(Properties properties) throws Exception {
        super(properties);
        this.properties = properties;
    }
       
    /**
     * Forces a complete attestation cycle for the specified VM running on the specified host and returns back the detailed attestation report.
     * Optionally the user can also request the host attestation report also to be included in the final report.
     * The accept content type header should be set to "Accept: application/json".<br>
     * @param obj VMAttestation object with the UUID of the host for which the attestation has to be done. 
     * @return VMAttestation object with the detailed trust report. 
     * @since Mt.Wilson 3.0
     * @mtwRequiresPermissions vm_attestations:create
     * @mtwContentTypeReturned JSON
     * @mtwMethodType POST
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/host-attestations
     * Input: {"host_name":"194.168.1.2","vm_instance_id":"14e03157-0935-442f-b4d6-1622154468e4","include_host_report":true} 
     * Output: 
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *   VmAttestations client = new VmAttestations(My.configuration().getClientProperties());
     *   VMAttestation attestation = new VMAttestation();
     *   attestation.setHostName("194.168.1.2");
     *   attestation.setVmInstanceId("14e03157-0935-442f-b4d6-1622154468e4");
     *   attestation.setIncludeHostReport(true);
     *   VMAttestation createVMAttestation = client.createVMAttestation(attestation);
     * </pre>
     */    
    public VMAttestation createVMAttestation(VMAttestation obj) {
        log.debug("target: {}", getTarget().getUri().toString());
        VMAttestation result = getTarget().path("vm-attestations").request(MediaType.APPLICATION_JSON).post(Entity.json(obj), VMAttestation.class);
        return result;
    }

    /**
     * Forces a complete attestation cycle for the specified VM running on the specified host and returns back the SAML assertion.
     * 
     * The accept content type header should be set to "Accept: application/samlassertion+xml".
     * @param obj HostAttestation object with the UUID of the host for which the attestation has to be done. 
     * @return String having the SAML assertion that was just created. 
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions vm_attestations:create
     * @mtwContentTypeReturned SAML
     * @mtwMethodType POST
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8443/mtwilson/v2/vm-attestations
     * Input: {"host_name":"194.168.1.2","vm_instance_id":"14e03157-0935-442f-b4d6-1622154468e4"} 
     * Output (SAML): 
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *   VmAttestations client = new VmAttestations(My.configuration().getClientProperties());
     *   VMAttestation attestation = new VMAttestation();
     *   attestation.setHostName("194.168.1.2");
     *   attestation.setVmInstanceId("14e03157-0935-442f-b4d6-1622154468e4");
     *   String hostSaml = client.createVMAttestationSaml(attestation);
     * </pre>
     */    
    public String createVMAttestationSaml(VMAttestation obj) {
        log.debug("target: {}", getTarget().getUri().toString());
        String samlAssertion = getTarget().path("vm-attestations").request(CryptoMediaType.APPLICATION_SAML).post(Entity.json(obj), String.class);
        return samlAssertion;
    }
    
    /**
     * Deletes the VM attestation report with the specifiied ID cached in the system. 
     * @param uuid - UUID of the cached VM attestation to be deleted from the system. 
     * @since Mt.Wilson 3.0
     * @mtwRequiresPermissions host_attestations:delete
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType DELETE
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8443/mtwilson/v2/vm-attestations/32923691-9847-4493-86ee-3036a4f24940
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *   VmAttestations client = new VmAttestations(My.configuration().getClientProperties());
     *   client.deleteVMAttestation("32923691-9847-4493-86ee-3036a4f24940");
     * </pre>
     */
    public void deleteVMAttestation(String uuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("id", uuid);
        Response obj = getTarget().path("vm-attestations/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).delete();
        log.debug(obj.toString());
    }

    /**
     * Deletes the attestation results for the Virtual Machine (VM) matching the specified criteria. 
     * @param criteria VMAttestationFilterCriteria object that specifies the search criteria.
     * The possible search options include one of the following
     * - VM attestation ID - A specific VM attestation report would be retrieved 
     * - VM instance ID - UUID of the VM instance - All reports for the specified VM instance would be retrieved.
     * - Host Name or IP address - Retrieves reports for all the VMs that were/are running on the specified host.
     * 
     * For both VM instance and Host name search criteria, user can additionally specify the below criteria.
     * - numberOfDays - Specifies the number of days back from the current date for which the attestations are needed. Ex: vmInstanceIdEqualTo=14e03157-0935-442f-b4d6-1622154468e4&numberOfDays=5
     * - fromDate & toDate - Specifies the date range for which the attestations are needed. Currently the following ISO 8601 date formats are supported
     *     -- date. Ex: vmInstanceIdEqualTo=14e03157-0935-442f-b4d6-1622154468e4&fromDate=2015-05-01&toDate=2015-06-01
     *     -- date+time. Ex: hostNameEqualTo=192.168.0.2&fromDate=2015-04-05T00:00Z&toDate=2015-06-05T00:00Z
     *     -- date+time+zone. Ex: hostNameEqualTo=192.168.0.2&fromDate=2015-04-05T12:30-02:00&toDate=2015-06-05T12:30-02:00
     * 
     * Note that when the fromDate and toDate options are specified, the output includes the attestations from the fromDate upto the toDate but not including the
     * attestations from the toDate.
     * 
     * By default the last 10 attestation results would be returned back. The user can change this by additionally specifying the limit criteria (limit=5).
     * 
     * @since CIT 3.0
     * @mtwRequiresPermissions vm_attestations:search,retrieve
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType DELETE
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8443/mtwilson/v2/vm-attestations?vmInstanceIdEqualTo=14e03157-0935-442f-b4d6-1622154468e4&limit=2
     * https://server.com:8443/mtwilson/v2/vm-attestations?vmInstanceIdEqualTo=14e03157-0935-442f-b4d6-1622154468e4&numberOfDays=5
     * https://server.com:8443/mtwilson/v2/vm-attestations?hostNameEqualTo=192.168.0.2&fromDate=2015-05-01&toDate=2015-06-01
     * 
     * Output: 
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *   VmAttestations client = new VmAttestations(My.configuration().getClientProperties());
     *   VMAttestationFilterCriteria criteria = new VMAttestationFilterCriteria();
     *   criteria.vmInstanceIdEqualTo = "14e03157-0935-442f-b4d6-1622154468e4";
     *   criteria.numberOfDays = 2;
     *   client.deleteVMAttestation(criteria);
     * </pre>
     */    
    public void deleteVMAttestation(VMAttestationFilterCriteria criteria) {        
        log.debug("target: {}", getTarget().getUri().toString());
        Response obj = getTargetPathWithQueryParams("vm-attestations", criteria).request(MediaType.APPLICATION_JSON).delete();        
        if( !obj.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
            throw new WebApplicationException("Delete VM attestation failed");
        }        
    }
    
    /**
     * This functionality is not supported.
     */
    public HostAttestation editHostAttestation(HostAttestation obj) {
        throw new UnsupportedOperationException("Not supported yet.");    
    }

    /**
     * Retrieves the VM attestation report with the specifiied ID (UUID). Note that this is the UUID of the attestation that was created for the
     * VM and cached in the system. 
     * @param uuid - UUID of the cached attestation. 
     * @return VMAttestation object with the attestation report.
     * @since Mt.Wilson 3.0
     * @mtwRequiresPermissions host_attestations:retrieve
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/vm-attestations/32923691-9847-4493-86ee-3036a4f24940
     * Output: 
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *   VmAttestations client = new VmAttestations(My.configuration().getClientProperties());
     *   VMAttestation obj = client.retrieveVMAttestation("32923691-9847-4493-86ee-3036a4f24940");
     * </pre>
    */    
    public VMAttestation retrieveVMAttestation(String uuid) {
        log.debug("target: {}", getTarget().getUri().toString());
        HashMap<String,Object> map = new HashMap<>();
        map.put("id", uuid);
        VMAttestation obj = getTarget().path("vm-attestations/{id}").resolveTemplates(map).request(MediaType.APPLICATION_JSON).get(VMAttestation.class);
        return obj;
    }
    
    /**
     * Searches for the attestation results for the Virtual Machine (VM) with the specified criteria. Complete attestation report would be returned back to the caller for the
     * VMs matching the search criteria. If during the actual request, the host attestation report was also requested, then that would also be included.
     * @param criteria VMAttestationFilterCriteria object that specifies the search criteria.
     * The possible search options include one of the following
     * - VM attestation ID - A specific VM attestation report would be retrieved 
     * - VM instance ID - UUID of the VM instance - All reports for the specified VM instance would be retrieved.
     * - Host Name or IP address - Retrieves reports for all the VMs that were/are running on the specified host.
     * 
     * For both VM instance and Host name search criteria, user can additionally specify the below criteria.
     * - numberOfDays - Specifies the number of days back from the current date for which the attestations are needed. Ex: vmInstanceIdEqualTo=14e03157-0935-442f-b4d6-1622154468e4&numberOfDays=5
     * - fromDate & toDate - Specifies the date range for which the attestations are needed. Currently the following ISO 8601 date formats are supported
     *     -- date. Ex: vmInstanceIdEqualTo=14e03157-0935-442f-b4d6-1622154468e4&fromDate=2015-05-01&toDate=2015-06-01
     *     -- date+time. Ex: hostNameEqualTo=192.168.0.2&fromDate=2015-04-05T00:00Z&toDate=2015-06-05T00:00Z
     *     -- date+time+zone. Ex: hostNameEqualTo=192.168.0.2&fromDate=2015-04-05T12:30-02:00&toDate=2015-06-05T12:30-02:00
     * 
     * Note that when the fromDate and toDate options are specified, the output includes the attestations from the fromDate upto the toDate but not including the
     * attestations from the toDate.
     * 
     * By default the last 10 attestation results would be returned back. The user can change this by additionally specifying the limit criteria (limit=5).
     * 
     * @return VMAttestationCollection object with a list of VM attestations matching the filter criteria. 
     * @since CIT 3.0
     * @mtwRequiresPermissions vm_attestations:search
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8443/mtwilson/v2/vm-attestations?vmInstanceIdEqualTo=14e03157-0935-442f-b4d6-1622154468e4&limit=2
     * https://server.com:8443/mtwilson/v2/vm-attestations?vmInstanceIdEqualTo=14e03157-0935-442f-b4d6-1622154468e4&numberOfDays=5
     * https://server.com:8443/mtwilson/v2/vm-attestations?hostNameEqualTo=192.168.0.2&fromDate=2015-05-01&toDate=2015-06-01
     * 
     * Output: 
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *   VmAttestations client = new VmAttestations(My.configuration().getClientProperties());
     *   VMAttestationFilterCriteria criteria = new VMAttestationFilterCriteria();
     *   criteria.vmInstanceIdEqualTo = "14e03157-0935-442f-b4d6-1622154468e4";
     *   criteria.numberOfDays = 2;
     *   VMAttestationCollection objCollection = client.searchVmAttestations(criteria);
     * </pre>
     */    
    public VMAttestationCollection searchVMAttestations(VMAttestationFilterCriteria criteria) {        
        log.debug("target: {}", getTarget().getUri().toString());
        VMAttestationCollection objCollection = getTargetPathWithQueryParams("vm-attestations", criteria).request(MediaType.APPLICATION_JSON).get(VMAttestationCollection.class);
        return objCollection;
    }

    /**
     * Verifies the signature of the retrieved SAML assertion using the SAML certificate stored in the user keystore created during user registration.
     * This functionality is available for the Api library users only.
     * @param saml SAML assertion.
     * @return TrustAssertion object having the status of verification.
     * @since Mt.Wilson 3.0
     * @mtwSampleApiCall
     * <pre>
     *   VmAttestations client = new VmAttestations(My.configuration().getClientProperties());
     *   VMAttestationFilterCriteria criteria = new VMAttestationFilterCriteria();
     *   criteria.vmInstanceIdEqualTo = "14e03157-0935-442f-b4d6-1622154468e4";
     *   criteria.numberOfDays = 2;
     *   VMAttestationCollection objCollection = client.searchVmAttestations(criteria);
     *   for (VMAttestation obj : objCollection.getVMAttestations()) {
     *       TrustAssertion verifyTrustAssertion = client.verifyTrustAssertion(obj.getVmSaml());
     *   }
     * 
     * </pre>
     */        
    public TrustAssertion verifyTrustAssertion(String saml) throws KeyManagementException, ApiException, KeyStoreException, NoSuchAlgorithmException, UnrecoverableEntryException, CertificateEncodingException {
        String mtwilsonApiKeystore = properties.getProperty("mtwilson.api.keystore");
        String mtwilsonApiKeystorePassword = properties.getProperty("mtwilson.api.keystore.password");
        
        if (properties == null || mtwilsonApiKeystore == null || mtwilsonApiKeystore.isEmpty()
                || mtwilsonApiKeystorePassword == null || mtwilsonApiKeystorePassword.isEmpty()) {
            return null;
        }
        SimpleKeystore keystore = new SimpleKeystore(new File(mtwilsonApiKeystore), mtwilsonApiKeystorePassword);
        X509Certificate[] trustedSamlCertificates;
        try {
            trustedSamlCertificates = keystore.getTrustedCertificates(SimpleKeystore.SAML);
        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableEntryException | CertificateEncodingException e) {
            throw e;
        } catch (Exception e) {
            throw e;
        }
        TrustAssertion trustAssertion = new TrustAssertion(trustedSamlCertificates, saml);
        return trustAssertion;
    }
}
