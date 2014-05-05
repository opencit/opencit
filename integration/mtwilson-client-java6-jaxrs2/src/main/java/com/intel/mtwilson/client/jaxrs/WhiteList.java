/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.client.jaxrs;

import com.intel.mtwilson.client.jaxrs.common.MtWilsonClient;
import com.intel.mtwilson.as.rest.v2.model.CreateWhiteListRpcInput;
import com.intel.mtwilson.as.rest.v2.model.CreateWhiteListWithOptionsRpcInput;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Properties;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WhiteList extends MtWilsonClient {
    
    Logger log = LoggerFactory.getLogger(getClass().getName());

    public WhiteList(URL url) throws Exception{
        super(url);
    }

    public WhiteList(Properties properties) throws Exception {
        super(properties);
    }
    
     /**
     * Configures the MLEs & associated white list/good known values using the host information provided. This API communicates
     * with the host, retrieves the details of the BIOS, OS/Hypervisor installed on that host and the associated white lists. Using
     * all the information retrieved from the host, OEM, OS and MLEs (both BIOS and VMM) are configured automatically. <br>
     * For Open Source (Xen/KVM) & Citrix XenServer hosts, PCRs 0, 17 & 18 are selected by default. For VMware ESXi hosts, PCRs
     * 0, 17, 18, 19 & 20 are selected by default. The default white list target for both BIOS and VMM would be set to OEM. <br>
     * If the user wants to change any of the default selections, then custom white list API should be used.     
     * @param oem - Oem object that needs to be created having the name and description. If the caller specifies the ID,
     * it has to be a valid UUID. If not provided, it would be automatically generated.
     * @return <code> CreateWhiteListRpcInput </code> that is created.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions oems:create,oss:create,mles:create,mle_pcrs:create,mle_pcrs:store,mle_modules:create,mle_sources:create
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType POST
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8443/mtwilson/v2/rpc/create-whitelist
     * Input: {"host":{"host_name":"192.168.0.2","add_on_connection_string":"vmware:https://192.168.0.1:443/sdk;admin;pwd"}}
     * Output: {"host":{"host_name":"10.1.71.155","ipaddress":null,"port":null,"bios_name":"Intel_Corporation",
     * "bios_version":"01.00.0060","bios_oem":"Intel Corporation","vmm_name":"Intel_Thurley_VMware_ESXi",
     * "vmm_version":"5.1.0-799733","vmm_osname":"VMware_ESXi","vmm_osversion":"5.1.0",
     * "add_on_connection_string":"vmware:https://10.1.71.87:443/sdk;Administrator;P@ssw0rd","description":null,
     * "email":null,"location":null,"aik_certificate":null,"aik_public_key":null,"aik_sha1":null,"processor_info":"Westmere"},
     * "result":"true"}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  Properties prop = My.configuration().getClientProperties();
     *  WhiteList client = new WhiteList(prop);
     *  TxtHostRecord gkvHost = new TxtHostRecord();
     *  gkvHost.HostName = "192.168.0.10";
     *  gkvHost.AddOn_Connection_String = "vmware:https://192.168.0.1:443/sdk;admin;pwd";
     *  CreateWhiteListRpcInput rpcInput = new CreateWhiteListRpcInput();
     *  rpcInput.setHost(gkvHost);        
     *  LinkedHashMap result = client.createWhitelist(rpcInput);
     * </pre>
     */
    public LinkedHashMap createWhitelist(CreateWhiteListRpcInput obj) {
        log.debug("target: {}", getTarget().getUri().toString());
        Object result = getTarget().path("rpc/create-whitelist").request().accept(MediaType.APPLICATION_JSON).post(Entity.json(obj), Object.class);
        if (result.getClass().equals(LinkedHashMap.class))
            return ((LinkedHashMap)(result));
        else 
            return null;
    }

    /**
     * Configures the Mles & associated white list/good known values using the host information and the customization options
     * provided by the caller. This API communicates with the host, retrieves the details of the BIOS, OS/Hypervisor installed 
     * on that host and the associated white lists. Using all the information retrieved from the host and the customization options
     * provided by the caller, OEM, OS and Mles (both BIOS and VMM) are configured automatically. <br>
     * Here the caller has the option to configure either or both BIOS and VMM MLEs with specific PCRs that the user is
     * interested in. Also, the white list target can be customized by choosing one of the 3 options supported [GLOBAL: 
     * The white list is applicable for hosts from any OEM running the same version of the OS/Hypervisor. OEM: The 
     * white list is applicable for the hosts from a specific OEM. HOST: The white list is valid only for specific host.] <br>
     * The user also gets to choose whether to register the host automatically after the configuration of the white list. Note
     * that the host can be registered successfully if both the BIOS and VMM MLEs have been configured.<br>
     * If the white list is already configured, then executing this function again would update the current
     * white lists in the database if the Overwrite_Whitelist flag is set to true. Otherwise, if the MLE already exists and the
     * white lists matches, new MLE will not be created. If in case the white list does not match (because of new modules, 
     * new tBoot version etc), then a new MLE will be created with a numeric extension (_001, 002).
     * @param obj
     * @return <code> CreateWhiteListRpcInput </code> that is created.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions oems:create,oss:create,mles:create,mle_pcrs:create,mle_pcrs:store,mle_modules:create,mle_sources:create
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType POST
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8443/mtwilson/v2/rpc/create-whitelist-with-options
     * Input: {"config":{"add_bios_white_list":true,"add_vmm_white_list":true,"bios_white_list_target":"BIOS_OEM",
     * "vmm_white_list_target":"VMM_OEM","bios_pcrs":"0","vmm_pcrs":"18,19,20","register_host":true,"overwrite_whitelist":false,
     * "txt_host_record":{"host_name":"192.168.0.2","add_on_connection_string":"vmware:https://192.168.0.1:443/sdk;admin;pwd"}}}
     * 
     * Output: {"config":{"add_bios_white_list":true,"add_vmm_white_list":true,"bios_white_list_target":"BIOS_OEM",
     * "vmm_white_list_target":"VMM_OEM","bios_pcrs":"0","vmm_pcrs":"18,19,20","host_location":"",
     * "register_host":true,"host_vmm_type":null,"txt_host_record":{"host_name":"192.168.0.2","ipaddress":null,
     * "port":null,"bios_name":"Intel_Corporation","bios_version":"01.00.0060","bios_oem":"Intel Corporation",
     * "vmm_name":"Intel_Thurley_VMware_ESXi","vmm_version":"5.1.0-799733","vmm_osname":"VMware_ESXi",
     * "vmm_osversion":"5.1.0","add_on_connection_string":"vmware:https://192.168.0.1:443/sdk;admin;pwd",
     * "description":null,"email":null,"location":null,"aik_certificate":null,"aik_public_key":null,
     * "aik_sha1":null,"processor_info":"Westmere"},"overwrite_whitelist":false},"result":"true"}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  Properties prop = My.configuration().getClientProperties();
     *  WhiteList client = new WhiteList(prop);
     *  TxtHostRecord gkvHost = new TxtHostRecord();
     *  gkvHost.HostName = "192.168.0.10";
     *  gkvHost.AddOn_Connection_String = "vmware:https://192.168.0.1:443/sdk;admin;pwd";
     *  HostConfigData config = new HostConfigData();
     *  config.setRegisterHost(true);
     *  config.setTxtHostRecord(gkvHost);
     *  CreateWhiteListWithOptionsRpcInput rpcInput = new CreateWhiteListWithOptionsRpcInput();
     *  rpcInput.setConfig(config);        
     *  LinkedHashMap rpcOutput = client.createWhitelistWithOptions(rpcInput);
     * </pre>
     */
    public LinkedHashMap createWhitelistWithOptions(CreateWhiteListWithOptionsRpcInput obj) {
        log.debug("target: {}", getTarget().getUri().toString());
        Object result = getTarget().path("rpc/create-whitelist").request().accept(MediaType.APPLICATION_JSON).post(Entity.json(obj), Object.class);
        if (result.getClass().equals(LinkedHashMap.class))
            return ((LinkedHashMap)(result));
        else 
            return null;
    }
    
}
