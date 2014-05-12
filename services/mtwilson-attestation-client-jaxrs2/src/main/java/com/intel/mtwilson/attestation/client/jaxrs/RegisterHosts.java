/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.attestation.client.jaxrs;

import com.intel.mtwilson.client.jaxrs.common.MtWilsonClient;
import com.intel.mtwilson.as.rest.v2.model.RegisterHostsRpcInput;
import com.intel.mtwilson.as.rest.v2.model.RegisterHostsWithOptionsRpcInput;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Properties;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegisterHosts extends MtWilsonClient {
    
    Logger log = LoggerFactory.getLogger(getClass().getName());

    public RegisterHosts(URL url) throws Exception{
        super(url);
    }

    public RegisterHosts(Properties properties) throws Exception {
        super(properties);
    }
    
     /**
     * Registers the list of hosts specified with the system. By default all the hosts will be associated with Mles created with 
     * BIOS_OEM & VMM_OEM as the White List target option. If any of the hosts specified have already been registered, the function
     * will update the host association with MLEs to match the current version of the BIOS & OS/Hypervisor running on the host. So, 
     * this RPC function can be used for both new registration and updating the host in the system.<br>
     * White list has to be configured with the White List target of OEM since this function uses
     * OEM as the white list target by default. If the user has created the White List with either GLOBAL or HOST Specific option
     * then the custom host registration function has to be used or else an error would be returned back to the caller.
     * @param RegisterHostsRpcInput object with the list of all the hosts to be registered. Only the host name and the connection string
     * parameters are required for each of the hosts.
     * @return Hashmap having the list of hosts created and their associated status.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions hosts:create,hosts:store
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType POST
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8443/mtwilson/v2/rpc/register-hosts
     * Input: {"hosts":{"host_records":[{"host_name":"192.168.0.2","add_on_connection_string":"vmware:vmware:https://192.168.0.1:443/sdk;admin;pwd"}]}}
     * Output: {"hosts":{"host_records":[{"host_name":"192.168.0.2","ipaddress":null,"port":null,
     * "bios_name":"Intel_Corporation","bios_version":"01.00.0060","bios_oem":"Intel Corporation",
     * "vmm_name":"Intel_Thurley_VMware_ESXi","vmm_version":"5.1.0-799733","vmm_osname":"VMware_ESXi",
     * "vmm_osversion":"5.1.0","add_on_connection_string":"vmware:https://192.168.0.1:443/sdk;admin;pwd",
     * "description":null,"email":null,"location":null,"aik_certificate":null,"aik_public_key":null,
     * "aik_sha1":null,"processor_info":"Westmere"}]},
     * "result":{"host_records":[{"host_name":"192.168.0.2","status":"true","error_message":"","error_code":"OK"}]}}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  Properties prop = My.configuration().getClientProperties();
     *  RegisterHosts client = new RegisterHosts(prop);
     *  TxtHostRecord host = new TxtHostRecord();
     *  host.HostName = "192.168.0.10";
     *  host.AddOn_Connection_String = "vmware:https://192.168.0.1:443/sdk;admin;pwd";
     *  TxtHostRecordList hostList = new TxtHostRecordList();
     *  hostList.getHostRecords().add(host);
     *  RegisterHostsRpcInput rpcInput = new RegisterHostsRpcInput();
     *  rpcInput.setHosts(hostList);
     *  LinkedHashMap rpcOutput = client.registerHosts(rpcInput);
     * </pre>
     */
    public LinkedHashMap registerHosts(RegisterHostsRpcInput obj) {
        log.debug("target: {}", getTarget().getUri().toString());
        Object result = getTarget().path("rpc/register-hosts").request().accept(MediaType.APPLICATION_JSON).post(Entity.json(obj), Object.class);
        if (result.getClass().equals(LinkedHashMap.class))
            return ((LinkedHashMap)(result));
        else 
            return null;
    }

    /**
     * Registers the list of  hosts specified with the system using the customized options. This function allows the user to specify
     * the white list (Mle) that needs to be associated with the host by specifying the white list target. The system support 3 options 
     * for white list target.[GLOBAL: The white list is applicable for hosts from any OEM running the same version of the OS/Hypervisor. 
     * OEM: The  white list is applicable for the hosts from a specific OEM running the same version of the OS/Hypervisor. 
     * HOST: The white list is valid only for specific host.]. <br>
     * White list has to be configured with the White List target that the host would be using. If the
     * MLE & associated white lists do not exist, then appropriate error would be returned back to the caller.
     * @param RegisterHostsWithOptionsRpcInput object with the list of all the hosts to be registered. Only the host name and the connection string
     * parameters are required for each of the hosts. For each of the hosts, the user can specify whether it has to be associated with which BIOS Mle
     * (BIOS_OEM,BIOS_GLOBAL,or BIOS_HOST) and which VMM Mle (VMM_OEM, VMM_GLOBAL or VMM_HOST).
     * @return Hashmap having the list of hosts created and their associated status.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions hosts:create,hosts:store
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType POST
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8443/mtwilson/v2/rpc/create-whitelist-with-options
     * Input: {"hosts":{"host_records":[{"bios_white_list_target":"BIOS_OEM","vmm_white_list_target":"VMM_OEM",
     * "txt_host_record":{"host_name":"10.1.71.155","add_on_connection_string":"vmware:https://10.1.71.87:443/sdk;Administrator;P@ssw0rd"}}]}}
     * 
     * Output: {"hosts":{"host_records":[{"add_bios_white_list":false,"add_vmm_white_list":false,"bios_white_list_target":"BIOS_OEM",
     * "vmm_white_list_target":"VMM_OEM","bios_pcrs":"","vmm_pcrs":"","host_location":"","register_host":false,"host_vmm_type":null,
     * "txt_host_record":{"host_name":"10.1.71.155","ipaddress":null,"port":null,"bios_name":"Intel_Corporation","bios_version":"01.00.0060",
     * "bios_oem":"Intel Corporation","vmm_name":"Intel_Thurley_VMware_ESXi","vmm_version":"5.1.0-799733","vmm_osname":"VMware_ESXi",
     * "vmm_osversion":"5.1.0","add_on_connection_string":"vmware:https://10.1.71.87:443/sdk;Administrator;P@ssw0rd","description":null,
     * "email":null,"location":null,"aik_certificate":null,"aik_public_key":null,"aik_sha1":null,"processor_info":"Westmere"},
     * "overwrite_whitelist":false}]},
     * "result":{"host_records":[{"host_name":"10.1.71.155","status":"true","error_message":"","error_code":"OK"}]}}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  Properties prop = My.configuration().getClientProperties();
     *  RegisterHosts client = new RegisterHosts(prop);
     *  TxtHostRecord host = new TxtHostRecord();
     *  host.HostName = "192.168.0.10";
     *  host.AddOn_Connection_String = "vmware:https://192.168.0.1:443/sdk;admin;pwd";
     *  HostConfigData config = new HostConfigData();
     *  config.setBiosWLTarget(HostWhiteListTarget.BIOS_OEM);
     *  config.setVmmWLTarget(HostWhiteListTarget.VMM_OEM);
     *  config.setTxtHostRecord(host);
     *  HostConfigDataList hostConfigDataList = new HostConfigDataList();
     *  hostConfigDataList.getHostRecords().add(config);
     *  RegisterHostsWithOptionsRpcInput rpcInput = new RegisterHostsWithOptionsRpcInput();
     *  rpcInput.setHosts(hostConfigDataList);
     *  LinkedHashMap rpcOutput = client.registerHostsWithOptions(rpcInput);
     * </pre>
     */
    public LinkedHashMap registerHostsWithOptions(RegisterHostsWithOptionsRpcInput obj) {
        log.debug("target: {}", getTarget().getUri().toString());
        Object result = getTarget().path("rpc/register-hosts-with-options").request().accept(MediaType.APPLICATION_JSON).post(Entity.json(obj), Object.class);
        if (result.getClass().equals(LinkedHashMap.class))
            return ((LinkedHashMap)(result));
        else 
            return null;
    }
    
}
