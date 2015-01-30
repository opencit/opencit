/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.attestation.client.jaxrs;

import com.intel.mtwilson.as.rest.v2.model.CreateWhiteListRpcInput;
import com.intel.mtwilson.jaxrs2.client.MtWilsonClient;
import com.intel.mtwilson.as.rest.v2.model.Host;
import com.intel.mtwilson.as.rest.v2.model.HostCollection;
import com.intel.mtwilson.as.rest.v2.model.HostFilterCriteria;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
public class HostTpmKeys extends MtWilsonClient {
    
    Logger log = LoggerFactory.getLogger(getClass().getName());

    public HostTpmKeys(URL url) throws Exception{
        super(url);
    }

    public HostTpmKeys(Properties properties) throws Exception {
        super(properties);
    }
    
     /**
     * Configures the MLEs & associated white list/good known values using the host information provided. This API communicates
     * with the host, retrieves the details of the BIOS, OS/Hypervisor installed on that host and the associated white lists. Using
     * all the information retrieved from the host, OEM, OS and MLEs (both BIOS and VMM) are configured automatically. <br>
     * For Open Source (Xen/KVM) & Citrix XenServer hosts, PCRs 0, 17 & 18 are selected by default. For VMware ESXi hosts, PCRs
     * 0, 17, 18, 19 & 20 are selected by default. The default white list target for both BIOS and VMM would be set to OEM. <br>
     * If the user wants to change any of the default selections, then custom white list API should be used.   
     * Here we are assuming that the user already created the TlsPolicy to be used to connect to the host. Alternatively, the user
     * can also specify the TlsPolicy details using the TlsPolicyDescriptor without having to pre-configure the TlsPolicy. 
     * @param obj CreateWhiteListRpcInput object having the IP address and the connection string of the host from which the white list has
     * to be created.
     * @return boolean value indicating whether the whitelist was successfully created or not.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions oems:create,oss:create,mles:create,mle_pcrs:create,mle_pcrs:store,mle_modules:create,mle_sources:create
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType POST
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8443/mtwilson/v2/rpc/create-whitelist
     * Input: {"host":{"host_name":"192.168.0.2","add_on_connection_string":"vmware:https://192.168.0.1:443/sdk;admin;pwd",
     *          "tls_policy_choice":{"tls_policy_id":"e1a527b5-2020-49c1-83be-6bd8bf641258"}}}
     * Output: {"host":{"host_name":"192.168.0.2","bios_name":"Intel_Corporation","bios_version":"01.00.0060","bios_oem":"Intel Corporation",
     *          "vmm_name":"Intel_Thurley_VMware_ESXi","vmm_version":"5.1.0-799733","vmm_osname":"VMware_ESXi","vmm_osversion":"5.1.0",
     *          "add_on_connection_string":"vmware:https://192.168.0.1:443/sdk;admin;password","processor_info":"Westmere",
     *          "tls_policy_choice":{"tls_policy_id":"e1a527b5-2020-49c1-83be-6bd8bf641258"}},"result":"true"}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  WhiteList client = new WhiteList(My.configuration().getClientProperties());
     *  TxtHostRecord gkvHost = new TxtHostRecord();
     *  gkvHost.HostName = "192.168.0.2";
     *  gkvHost.AddOn_Connection_String = "vmware:https://192.168.0.1:443/sdk;admin;pwd";
     *  TlsPolicyChoice tlsPolicyChoice = new TlsPolicyChoice();
     *  tlsPolicyChoice.setTlsPolicyId("e1a527b5-2020-49c1-83be-6bd8bf641258");
     *  gkvHost.tlsPolicyChoice = tlsPolicyChoice;
     *  CreateWhiteListRpcInput rpcInput = new CreateWhiteListRpcInput();
     *  rpcInput.setHost(gkvHost);        
     *  boolean result = client.createWhitelist(rpcInput);
     * </pre>
     */
    public String createBindingKeyCertificate(String tcgCertificateAsHexString) {
        log.debug("target: {}", getTarget().getUri().toString());
        String pemCertificate = "";
        Object result = getTarget().path("rpc/certify-host-binding-key").request().accept(MediaType.APPLICATION_JSON).post(Entity.json(tcgCertificateAsHexString), Object.class);
        if (result.getClass().equals(LinkedHashMap.class)) {
            LinkedHashMap resultMap = (LinkedHashMap)result;
            if (resultMap.containsKey("result")) {
                pemCertificate = resultMap.get("result").toString().trim();
                log.debug("Result of certifying host binding key is {}.", pemCertificate);
            }
        }
        return pemCertificate;
    }
    
}
