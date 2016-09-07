/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.attestation.client.jaxrs;

import com.intel.mtwilson.jaxrs2.client.MtWilsonClient;
import com.intel.mtwilson.as.rest.v2.model.RegisterHostsRpcInput;
import com.intel.mtwilson.as.rest.v2.model.RegisterHostsWithOptionsRpcInput;
import com.intel.mtwilson.datatypes.HostConfigResponse;
import com.intel.mtwilson.i18n.ErrorCode;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
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
     * Here we are assuming that the user already created the TlsPolicy to be used to connect to the host. Alternatively, the user
     * can also specify the TlsPolicy details using the TlsPolicyDescriptor without having to pre-configure the TlsPolicy. 
     * @param obj RegisterHostsRpcInput object with the list of all the hosts to be registered. Only the host name and the connection string
     * parameters are required for each of the hosts.
     * @return HostConfigResponse list having the list of hosts created and their associated status.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions hosts:create,hosts:store
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType POST
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8443/mtwilson/v2/rpc/register-hosts
     * Input: {"hosts":{"HostRecords":[{"host_name":"192.168.0.2","add_on_connection_string":"vmware:vmware:https://192.168.0.1:443/sdk;admin;pwd",
     *          "tls_policy_choice":{"tls_policy_id":"e1a527b5-2020-49c1-83be-6bd8bf641258"}}]}}
     * Output: {"hosts":{"HostRecords":[{"host_name":"192.168.0.2","bios_name":"Intel_Corporation","bios_version":"01.00.0060",
     *          "bios_oem":"Intel Corporation","vmm_name":"Intel_Thurley_VMware_ESXi","vmm_version":"5.1.0-799733","vmm_osname":
     *          "VMware_ESXi","vmm_osversion":"5.1.0","add_on_connection_string":"vmware:https://192.168.0.1:443/sdk;admin;pwd",
     *          "processor_info":"Westmere","tls_policy_choice":{"tls_policy_id":"e1a527b5-2020-49c1-83be-6bd8bf641258"}}]},
     *          "result":{"host_records":[{"host_name":"192.168.0.2","status":"true","error_message":"","error_code":"OK"}]}}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  Properties prop = My.configuration().getClientProperties();
     *  RegisterHosts client = new RegisterHosts(prop);
     *  TxtHostRecord host = new TxtHostRecord();
     *  host.HostName = "192.168.0.2";
     *  host.AddOn_Connection_String = "vmware:https://192.168.0.1:443/sdk;admin;pwd";
     *  TlsPolicyChoice tlsPolicyChoice = new TlsPolicyChoice();
     *  tlsPolicyChoice.setTlsPolicyId("e1a527b5-2020-49c1-83be-6bd8bf641258");
     *  gkvHost.tlsPolicyChoice = tlsPolicyChoice;
     *  TxtHostRecordList hostList = new TxtHostRecordList();
     *  hostList.getHostRecords().add(host);
     *  RegisterHostsRpcInput rpcInput = new RegisterHostsRpcInput();
     *  rpcInput.setHosts(hostList);
     *  List<HostConfigResponse> rpcOutput = client.registerHosts(rpcInput);
     * </pre>
     */
    public List<HostConfigResponse> registerHosts(RegisterHostsRpcInput obj) {
        List<HostConfigResponse> hostRecords = new ArrayList<>();
        log.debug("target: {}", getTarget().getUri().toString());
        Object result = getTarget().path("rpc/register-hosts").request().accept(MediaType.APPLICATION_JSON).post(Entity.json(obj), Object.class);
        if (result.getClass().equals(LinkedHashMap.class)) {
            LinkedHashMap resultMap = (LinkedHashMap)result;
            if (resultMap.containsKey("result")) {
                LinkedHashMap outputHashMap = (LinkedHashMap) resultMap.get("result");
                hostRecords = convertToHostConfigResponseList((List<LinkedHashMap>) outputHashMap.get("host_records"));
            }
        }
        return hostRecords;
    }

    /**
     * Registers the list of  hosts specified with the system using the customized options. This function allows the user to specify
     * the white list (Mle) that needs to be associated with the host by specifying the white list target. The system support 3 options 
     * for white list target.[GLOBAL: The white list is applicable for hosts from any OEM running the same version of the OS/Hypervisor. 
     * OEM: The  white list is applicable for the hosts from a specific OEM running the same version of the OS/Hypervisor. 
     * HOST: The white list is valid only for specific host.]. <br>
     * White list has to be configured with the White List target that the host would be using. If the
     * MLE & associated white lists do not exist, then appropriate error would be returned back to the caller.
     * @param obj RegisterHostsWithOptionsRpcInput object with the list of all the hosts to be registered. Only the host name and the connection string
     * parameters are required for each of the hosts. For each of the hosts, the user can specify whether it has to be associated with which BIOS Mle
     * (BIOS_OEM,BIOS_GLOBAL,or BIOS_HOST) and which VMM Mle (VMM_OEM, VMM_GLOBAL or VMM_HOST).
     * Here we are assuming that the user already created the TlsPolicy to be used to connect to the host. Alternatively, the user
     * can also specify the TlsPolicy details using the TlsPolicyDescriptor without having to pre-configure the TlsPolicy. 
     * @return HostConfigResponse list having the list of hosts created and their associated status.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions hosts:create,hosts:store
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType POST
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8443/mtwilson/v2/rpc/register-hosts-with-options
     * Input: {"hosts":{"HostRecords":[{"bios_white_list_target":"BIOS_OEM","vmm_white_list_target":"VMM_OEM",
     *          "txt_host_record":{"host_name":"192.168.0.2","add_on_connection_string":"vmware:https://192.168.0.1:443/sdk;admin;pwd",
     *          "tls_policy_choice":{"tls_policy_id":"e1a527b5-2020-49c1-83be-6bd8bf641258"}}}]}}
     * 
     * Output: {"hosts":{"HostRecords":[{"add_bios_white_list":false,"add_vmm_white_list":false,"bios_white_list_target":"BIOS_OEM",
     *          "vmm_white_list_target":"VMM_OEM","bios_pcrs":"","vmm_pcrs":"","host_location":"","register_host":false,
     *          "txt_host_record":{"host_name":"192.168.0.2","bios_name":"Intel_Corporation","bios_version":"01.00.0060",
     *          "bios_oem":"Intel Corporation","vmm_name":"Intel_Thurley_VMware_ESXi","vmm_version":"5.1.0-799733","vmm_osname":"VMware_ESXi",
     *          "vmm_osversion":"5.1.0","add_on_connection_string":"vmware:https://192.168.0.1:443/sdk;admin;pwd",
     *          "processor_info":"Westmere","tls_policy_choice":{"tls_policy_id":"e1a527b5-2020-49c1-83be-6bd8bf641258"}},
     *          "overwrite_whitelist":false}]},
     *          "result":{"host_records":[{"host_name":"192.168.0.2","status":"true","error_message":"","error_code":"OK"}]}}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  Properties prop = My.configuration().getClientProperties();
     *  RegisterHosts client = new RegisterHosts(prop);
     *  TxtHostRecord host = new TxtHostRecord();
     *  host.HostName = "192.168.0.2";
     *  host.AddOn_Connection_String = "vmware:https://192.168.0.1:443/sdk;admin;pwd";
     *  TlsPolicyChoice tlsPolicyChoice = new TlsPolicyChoice();
     *  tlsPolicyChoice.setTlsPolicyId("e1a527b5-2020-49c1-83be-6bd8bf641258");
     *  gkvHost.tlsPolicyChoice = tlsPolicyChoice;
     *  HostConfigData config = new HostConfigData();
     *  config.setBiosWLTarget(HostWhiteListTarget.BIOS_OEM);
     *  config.setVmmWLTarget(HostWhiteListTarget.VMM_OEM);
     *  config.setTxtHostRecord(host);
     *  HostConfigDataList hostConfigDataList = new HostConfigDataList();
     *  hostConfigDataList.getHostRecords().add(config);
     *  RegisterHostsWithOptionsRpcInput rpcInput = new RegisterHostsWithOptionsRpcInput();
     *  rpcInput.setHosts(hostConfigDataList);
     *  List<HostConfigResponse> rpcOutput = client.registerHostsWithOptions(rpcInput);
     * </pre>
     */
    public List<HostConfigResponse> registerHostsWithOptions(RegisterHostsWithOptionsRpcInput obj) {
        List<HostConfigResponse> hostRecords = new ArrayList<>();
        log.debug("target: {}", getTarget().getUri().toString());
        Object result = getTarget().path("rpc/register-hosts-with-options").request().accept(MediaType.APPLICATION_JSON).post(Entity.json(obj), Object.class);
        if (result.getClass().equals(LinkedHashMap.class)) {
            LinkedHashMap resultMap = (LinkedHashMap)result;
            if (resultMap.containsKey("result")) {
                LinkedHashMap outputHashMap = (LinkedHashMap) resultMap.get("result");
                hostRecords = convertToHostConfigResponseList((List<LinkedHashMap>) outputHashMap.get("host_records"));
            }
        }
        return hostRecords;
    }
    
    private List<HostConfigResponse> convertToHostConfigResponseList (List<LinkedHashMap> inputList) {
        List<HostConfigResponse> hostRecords = new ArrayList<>();
        for(int i=0; i< inputList.size(); i++) {
            HostConfigResponse hcr = new HostConfigResponse();
            hcr.setHostName((String)inputList.get(i).get("host_name"));
            hcr.setStatus((String)inputList.get(i).get("status"));
            hcr.setErrorMessage((String)inputList.get(i).get("error_message"));
            ErrorCode ec = ErrorCode.valueOf((String)inputList.get(i).get("error_code"));
            hcr.setErrorCode(ec);
            log.debug("Processing the register host response for {}", hcr.getHostName());
            hostRecords.add(hcr);
        }        
        return hostRecords;
    }
}
