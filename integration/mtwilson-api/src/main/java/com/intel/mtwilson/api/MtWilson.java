/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.api;

import com.intel.mountwilson.as.hostmanifestreport.data.HostManifestReportType;
import com.intel.mountwilson.as.hostmanifestreport.data.ManifestType;
import com.intel.mountwilson.as.hosttrustreport.data.HostsTrustReportType;
import com.intel.mtwilson.datatypes.ApiClientCreateRequest;
import com.intel.mtwilson.datatypes.ApiClientInfo;
import com.intel.mtwilson.datatypes.ApiClientSearchCriteria;
import com.intel.mtwilson.datatypes.ApiClientUpdateRequest;
import com.intel.mtwilson.datatypes.AssetTagCertCreateRequest;
import com.intel.mtwilson.datatypes.AssetTagCertRevokeRequest;
import com.intel.mtwilson.datatypes.AttestationReport;
import com.intel.mtwilson.datatypes.AuditLogEntry;
import com.intel.mtwilson.datatypes.AuditLogSearchCriteria;
import com.intel.mtwilson.datatypes.BulkHostTrustResponse;
import com.intel.mtwilson.datatypes.CaInfo;
import com.intel.mtwilson.datatypes.HostConfigData;
import com.intel.mtwilson.datatypes.HostConfigDataList;
import com.intel.mtwilson.datatypes.HostConfigResponseList;
import com.intel.mtwilson.datatypes.HostLocation;
import com.intel.mtwilson.datatypes.HostResponse;
import com.intel.mtwilson.datatypes.HostTrustResponse;
import com.intel.mtwilson.datatypes.MLESearchCriteria;
import com.intel.mtwilson.datatypes.MleData;
import com.intel.mtwilson.datatypes.MleSource;
import com.intel.mtwilson.datatypes.ModuleWhiteList;
import com.intel.mtwilson.datatypes.OemData;
import com.intel.mtwilson.datatypes.OpenStackHostTrustLevelReport;
import com.intel.mtwilson.datatypes.OsData;
import com.intel.mtwilson.datatypes.PCRWhiteList;
import com.intel.mtwilson.datatypes.Role;
import com.intel.mtwilson.datatypes.TxtHost;
import com.intel.mtwilson.datatypes.TxtHostRecord;
import com.intel.mtwilson.datatypes.TxtHostRecordList;
import com.intel.mtwilson.datatypes.xml.HostTrustXmlResponse;
import com.intel.mtwilson.model.Hostname;
import com.intel.mtwilson.model.Sha1Digest;
import java.io.IOException;
import java.net.MalformedURLException;
import java.security.SignatureException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Set;
import javax.xml.bind.JAXBException;

/**
 *
 * @author jbuhacoff
 */
public interface MtWilson {
    // ATTESTATION SERVICE
    
    /**
     * Reserved for the future release. This API is currently not supported.
     * @param hostname
     * @return
     * @throws IOException
     * @throws ApiException
     * @throws SignatureException 
     */
    HostLocation getHostLocation(Hostname hostname) throws IOException, ApiException, SignatureException;
    
    /**
     * Reserved for the future release. This API is currently not supported.
     * @param hostLocObj
     * @return
     * @throws IOException
     * @throws ApiException
     * @throws SignatureException 
     */
    boolean addHostLocation(HostLocation hostLocObj) throws IOException, ApiException, SignatureException;

    /**
     * Retrieves the current trust status of the host by going through a complete attestation cycle. Attestation cycle
     * includes communicating with the host, retrieving the latest BIOS and Hypervisor measurements and verifying
     * the same. Since the status is not retrieved from the cache, the performance would be relatively slower than
     * the getTrustForMultipleHosts(Set<Hostname> hostnames, boolean forceVerify) function where the user 
     * has the option to specify whether to retrieve the trust status from the cache or not. <br>
     * For users who want to ensure that the trust status indeed came from Mt.Wilson, they need to use 
     * {@link GetSamlForHost} function.
     * <p>
     * <i><u>Roles needed:</u></i>Attestation/Report
     * <p>
     * <i><u>Content type returned:</u></i>JSON
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: GET</i><br>
     * https://192.168.1.101:8181/AttestationService/resources/hosts/trust?hostName=10.1.70.126
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * {"hostname":"10.1.70.126","trust":{"bios":true,"vmm":false,"location":false}}
     * <p>
     * <i><u>Sample Java API Call:</u></i><br>
     * HostTrustResponse hostTrust = apiClientObj.getHostTrust(new Hostname("10.1.70.126"));
     * System.out.println("BIOS trust status: " + hostTrust.trust.bios + " Hypervisor trust status" + hostTrust.trust.vmm);
     * <p>
     * 
     * @param hostname {@link Hostname} Name of the host for which we need to get the trust status.
     * @return {@link HostTrustResponse} Trust status of BIOS and Hypervisor. Note that the location if for future use.
     * @throws IOException
     * @throws ApiException All the errors from the Mt.Wilson system would be thrown as ApiException. Users can access the 
     * error code and error message for details.
     * @throws SignatureException 
     * @since MTW 1.0 Enterprise/1.2 Opensource
     * 
     */
    HostTrustResponse getHostTrust(Hostname hostname) throws IOException, ApiException, SignatureException;

    /**
     * Registers the host specified with the system.
     * <p>
     * <i><u>Pre-requisite:</u></i>As part of registration, a host has to be associated with both BIOS and 
     * VMM/Hypervisor MLEs. So, these MLEs have to be configured before host registration.
     * <p>
     * <i><u>Roles needed:</u></i>Attestation
     * <p>
     * <i><u>Input content type:</u></i>Application/JSON
     * <p>
     * <i><u>Output content type:</u></i>Application/JSON
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: POST</i><br>
     * https://192.168.1.101:8181/AttestationService/resources/host<br>
     * <p>
     * <i>Sample Input</i><br>
     * {"HostName":"192.168.1.201","IPAddress":"192.168.1.201","Port":9999,"BIOS_Name":"Intel_Corp.","BIOS_Version":"T060","BIOS_Oem":"Intel Corp.","VMM_Name":"Intel_Thurley_Xen","VMM_Version":"11-4.1.0","VMM_OSName":"SUSE_LINUX","VMM_OSVersion":"11","AddOn_Connection_String":"intel:https://192.168.1.201:9999","Description":"","Email":"","Location":null,"AIK_Certificate":null,"AIK_PublicKey":null,"AIK_SHA1":null,"Processor_Info":null}
     * {"BIOS_WhiteList_Target":"BIOS_OEM","VMM_WhiteList_Target":"VMM_OEM", "TXT_Host_Record":{"HostName":"192.168.201","AddOn_Connection_String":"intel:https://192.168.1.201:9999"}} <br>
     * Also, the user can specify just the required parameters as shown in the example below.<br>
     * {"HostName":"192.168.1.201","BIOS_Name":"Intel_Corp.","BIOS_Version":"T060","BIOS_Oem":"Intel Corp.","VMM_Name":"Intel_Thurley_Xen","VMM_Version":"11-4.1.0","VMM_OSName":"SUSE_LINUX","VMM_OSVersion":"11","AddOn_Connection_String":"intel:https://192.168.1.201:9999"}
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * {"error_code":"OK","error_message":"OK"}<br>
     * <p>
     * <i><u>Sample Java API Call:</u></i><br>
            TxtHostRecord hostObj = new TxtHostRecord();<br>
            hostObj.HostName = "192.168.1.201";<br>
            hostObj.AddOn_Connection_String = "intel:https://192.168.1.201:9999";<br>
            hostObj.BIOS_Name = "Intel_Corp.";<br>
            hostObj.BIOS_Version = "T060";<br>
            hostObj.BIOS_Oem = "Intel Corp.";<br>
            hostObj.VMM_Name = "Intel_Thurley_Xen";<br>
            hostObj.VMM_Version = "11-4.1.0";<br>
            hostObj.VMM_OSName = "SUSE_LINUX";<br>
            hostObj.VMM_OSVersion = "11";<br>
            HostResponse addHost = apiClientObj.addHost(new TxtHost(hostObj));
     * <p>
     * 
     * @param host {@link TxtHost} object with the details of the host to be registered. The required parameters that specify
     * the host details are the HostName and AddOn_Connection_String [Open Source Hosts: intel:https://192.168.1.201:9999, Citrix XenServer: 
     * citrix:https://192.168.1.202:443/;root;pwd, VMware ESXi:vmware:https://192.168.1.222:443/sdk;Admin;password]. To associate 
     * the host with the MLEs, BIOS_Name, BIOS_Version, BIOS_OEM, VMM_Name, VMM_Version, 
     * VMM_OSName & VMM_OSVersion have to be specified. All other parameters are optional.
     * @return {@link HostResponse} with the details of the status. 
     * @throws IOException
     * @throws ApiException If there are any errors during the execution this exception would be returned to the caller.
     * The caller can use the getErrorCode() and getMessage() functions to retrieve the exception details.
     * @throws SignatureException
     * @see MtWilson#registerHost(com.intel.mtwilson.datatypes.TxtHostRecord) registerHost function for automation of host registration.
     * @since MTW 1.0 Enterprise/1.2 Opensource
     */    
    HostResponse addHost(TxtHost host) throws IOException, ApiException, SignatureException, MalformedURLException;
    
    /**
     * Registers the list of hosts specified. 
     * <p>
     * <i><u>Pre-requisite:</u></i>As part of registration, a host has to be associated with both BIOS and 
     * VMM/Hypervisor MLEs. So, these MLEs have to be configured before host registration.
     * <p>
     * <i><u>Roles needed:</u></i>Attestation
     * <p>
     * <i><u>Input content type:</u></i>Application/JSON
     * <p>
     * <i><u>Output content type:</u></i>Application/JSON
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: POST</i><br>
     * https://192.168.1.101:8181/AttestationService/resources/hosts/bulk<br>
     * <p>
     * <i>Sample Input</i><br>
     * {"HostRecords":[{"HostName":"192.168.1.201","BIOS_Name":"Intel_Corp.","BIOS_Version":"T060","BIOS_Oem":"Intel Corp.",
     * "VMM_Name":"Intel_Thurley_Xen","VMM_Version":"11-4.1.0","VMM_OSName":"SUSE_LINUX","VMM_OSVersion":"11","AddOn_Connection_String":"intel:https://192.168.1.201:9999"},
     * {"HostName":"192.168.1.202","BIOS_Name":"Intel_Corporation","BIOS_Version":"0060","BIOS_Oem":"Intel Corporation",
     * "VMM_Name":"Intel_Thurley_VMware_ESXi","VMM_Version":"5.1.0-799733","VMM_OSName":"VMware_ESXi","VMM_OSVersion":"5.1.0","AddOn_Connection_String":"vmware:https://192.168.1.222:443/sdk;Administrator;P@ssw0rd"}]}
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * {"HostRecords":[{"Host_Name":"192.168.1.201","Status":"true","Error_Message":"","Error_Code":"OK"},{"Host_Name":"192.168.1.202","Status":"true","Error_Message":"","Error_Code":"OK"}]}<br>
     * If in case there are any errors,<br>
     * {"HostRecords":[{"Host_Name":"192.168.1.201","Status":"false","Error_Message":"Host '192.168.1.201' already exists.' already exists.","Error_Code":"AS_HOST_EXISTS"},{"Host_Name":"192.168.1.202","Status":"true","Error_Message":"","Error_Code":"OK"}]}
     * <p>
     * <i><u>Sample Java API Call:</u></i><br>
            TxtHostRecord hostObj = new TxtHostRecord();<br>
            hostObj.HostName = "192.168.1.201";<br>
            hostObj.AddOn_Connection_String = "intel:https://192.168.1.201:9999";<br>
            hostObj.BIOS_Name = "Intel_Corp.";<br>
            hostObj.BIOS_Version = "T060";<br>
            hostObj.BIOS_Oem = "Intel Corp.";<br>
            hostObj.VMM_Name = "Intel_Thurley_Xen";<br>
            hostObj.VMM_Version = "11-4.1.0";<br>
            hostObj.VMM_OSName = "SUSE_LINUX";<br>
            hostObj.VMM_OSVersion = "11";<br>
            TxtHostRecordList hosts = new TxtHostRecordList();
            hosts.getHostRecords().add(hostObj);
            HostConfigResponseList addHosts = apiClientObj.addHosts(hosts);
     * <p>
     * 
     * @param hostRecords {@link TxtHostRecordList} object with the details of the hosts to be registered. For each of the hosts
     *  the HostName and AddOn_Connection_String [Open Source Hosts: intel:https://192.168.1.201:9999, Citrix XenServer: 
     * citrix:https://192.168.1.202:443/;root;pwd, VMware ESXi:vmware:https://192.168.1.222:443/sdk;Admin;password] are
     * required. To associate the host with the MLEs, BIOS_Name, BIOS_Version, BIOS_OEM, VMM_Name, VMM_Version, 
     * VMM_OSName & VMM_OSVersion have to be specified. All other parameters are optional.
     * @return {@link HostConfigResponseList} with the details of the status for all the hosts. 
     * @throws IOException
     * @throws ApiException If there are any errors during the execution this exception would be returned to the caller.
     * The caller can use the getErrorCode() and getMessage() functions to retrieve the exception details.
     * @throws SignatureException
     * @see MtWilson#registerHosts(com.intel.mtwilson.datatypes.TxtHostRecordList) registerHosts function for automation of host registration.
     * @since MTW 1.2 Enterprise
     */    
    HostConfigResponseList addHosts(TxtHostRecordList hostRecords) throws IOException, ApiException, SignatureException;

    /**
     * 
     * Retrieves the current trust status of the host by going through a complete attestation cycle. Attestation cycle
     * includes communicating with the host, retrieving the latest BIOS and Hypervisor measurements and verifying
     * the same. Since the status is not retrieved from the cache, the performance would be relatively slower than
     * the getTrustForMultipleHosts(Set<Hostname> hostnames, boolean forceVerify) function where the user 
     * has the option to specify whether to retrieve the trust status from the cache or not. <br>
     * For users who want to ensure that the trust status indeed came from Mt.Wilson, they need to use 
     * {@link GetSamlForHost} function.
     * <p>
     * <i><u>Roles needed:</u></i>Attestation/Report
     * <p>
     * <i><u>Content type returned:</u></i>JSON
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: GET</i>
     * https://192.168.1.101:8181/AttestationService/resources/hosts/aik-0de3710ee2f658a382f2531213233024175a63dd/trust.json
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * {"hostname":"192.168.0.201","trust":{"bios":true,"vmm":false,"location":false}}
     * <p>
     * <i><u>Sample Java API Call:</u></i><br>
     * HostTrustResponse hostTrust = apiClientObj.getHostTrustByAik(new Sha1Digest("0de3710ee2f658a382f2531213233024175a63dd"));
     * System.out.println("BIOS trust status: " + hostTrust.trust.bios + " Hypervisor trust status" + hostTrust.trust.vmm);
     * <p>
     * 
     * @param aikSha1 {@link Sha1Digest} The SHA1 of the AIK Public Key (not certificate) of the host
     * @return {@link HostTrustResponse} Trust status of BIOS and Hypervisor. Note that the location if for future use.
     * @throws IOException
     * @throws ApiException All the errors from the Mt.Wilson system would be thrown as ApiException. Users can access the 
     * error code and error message for details.
     * @throws SignatureException 
     * @since MTW 1.0 Enterprise
     */
    HostTrustResponse getHostTrustByAik(Sha1Digest aikSha1) throws IOException, ApiException, SignatureException;
    
    /**
     * Retrieves an X509 certificate for an RSA key that is sealed to the host's trusted platform configuration.
     * This can be used to authenticate the host in an SSL connection or to send encrypted data to the host
     * where the host can only receive the data if it is currently in the trusted configuration indicated by
     * the certificate.
     * 
     * <p>
     * <i><u>Roles needed:</u></i>Attestation/Report
     * <p>
     * <i><u>Content type returned:</u></i>JSON
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: GET</i>
     * https://192.168.1.101:8181/AttestationService/resources/hosts/aik-0de3710ee2f658a382f2531213233024175a63dd/trustcert.x509
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * application/octet-stream DER-encoded (binary) X509 certificate
     * <p>
     * <i><u>Sample Java API Call:</u></i><br>
     * X509Certificate hostTrustCert = apiClientObj.getCurrentTrustCertificateByAik(new Sha1Digest("0de3710ee2f658a382f2531213233024175a63dd"));
     * <p>
     * 
     * @param aikSha1 {@link Sha1Digest} The SHA1 of the AIK Public Key (not certificate) of the host
     * @return {@link X509Certificate}
     * @throws IOException
     * @throws ApiException
     * @throws SignatureException 
     * @since MW1.2
     */
//    X509Certificate getCurrentTrustCertificateByAik(Sha1Digest aikSha1) throws IOException, ApiException, SignatureException;


    /**
     * Updates the host specified that is already registered  with the system. Except for the host name, user can update
     * all the other information for the host.
     * <p>
     * <i><u>Pre-requisite:</u></i>As part of registration, a host has to be associated with both BIOS and 
     * VMM/Hypervisor MLEs. So, these MLEs have to be configured before host registration.
     * <p>
     * <i><u>Roles needed:</u></i>Attestation
     * <p>
     * <i><u>Input content type:</u></i>Application/JSON
     * <p>
     * <i><u>Output content type:</u></i>Application/JSON
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: PUT</i><br>
     * https://192.168.1.101:8181/AttestationService/resources/host<br>
	 * <p>
     * <i>Sample Input</i><br>
     * {"HostName":"192.168.1.201","BIOS_Name":"Intel_Corp.","BIOS_Version":"T060","BIOS_Oem":"Intel Corp.","VMM_Name":"Intel_Thurley_Xen",
     * "VMM_Version":"11-4.1.0","VMM_OSName":"SUSE_LINUX","VMM_OSVersion":"11","AddOn_Connection_String":"intel:https://10.1.71.169:9999",
     * "Description":"Updated the host"} 
     * <i><u>Sample Output:</u></i><br>
     * {"error_code":"OK","error_message":"OK"}<br>
     * <p>
     * <i><u>Sample Java API Call:</u></i><br>
            TxtHostRecord hostObj = new TxtHostRecord();<br>
            hostObj.HostName = "192.168.1.201";<br>
            hostObj.AddOn_Connection_String = "intel:https://192.168.1.201:9999";<br>
            hostObj.BIOS_Name = "Intel_Corp.";<br>
            hostObj.BIOS_Version = "T060";<br>
            hostObj.BIOS_Oem = "Intel Corp.";<br>
            hostObj.VMM_Name = "Intel_Thurley_Xen";<br>
            hostObj.VMM_Version = "11-4.1.0";<br>
            hostObj.VMM_OSName = "SUSE_LINUX";<br>
            hostObj.VMM_OSVersion = "11";<br>
            hostObj.Description = "Updated the host";
            HostResponse updateHost = apiClientObj.updateHost(new TxtHost(hostObj));
     * <p>
     * 
     * @param host {@link TxtHost} object with the details of the host to be updated. For the host user has to specify the
     *  HostName and AddOn_Connection_String [Open Source Hosts: intel:https://192.168.1.201:9999, Citrix XenServer: 
     * citrix:https://192.168.1.202:443/;root;pwd, VMware ESXi:vmware:https://192.168.1.222:443/sdk;Admin;password]. To associate 
     * the host with the MLEs, BIOS_Name, BIOS_Version, BIOS_OEM, VMM_Name, VMM_Version, 
     * VMM_OSName & VMM_OSVersion have to be specified. All other parameters are optional.
     * @return {@link HostResponse} with the details of the status. 
     * @throws IOException
     * @throws ApiException If there are any errors during the execution this exception would be returned to the caller.
     * The caller can use the getErrorCode() and getMessage() functions to retrieve the exception details.
     * @throws SignatureException
     * @see MtWilson#registerHost(com.intel.mtwilson.datatypes.TxtHostRecord) registerHost function for automation of host updates.
     * @since MTW 1.0 Enterprise/1.2 Opensource
     */    
    HostResponse updateHost(TxtHost host) throws IOException, ApiException, SignatureException, MalformedURLException;

    /**
     * Updates the list of hosts specified. Whenever the host is updated with a BIOS or OS patch/upgrade, it has to be re-registered
     * or updated with the updated MLEs. Otherwise the attestation will fail.
     * <p>
     * <i><u>Roles needed:</u></i>Attestation
     * <p>
     * <i><u>Input content type:</u></i>Application/JSON
     * <p>
     * <i><u>Output content type:</u></i>Application/JSON
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: PUT</i><br>
     * https://192.168.1.101:8181/AttestationService/resources/hosts/bulk<br>
	 * <p>
     * <i>Sample Input</i><br>
     * {"HostRecords":[{"HostName":"192.168.1.201","BIOS_Name":"Intel_Corp.","BIOS_Version":"T060","BIOS_Oem":"Intel Corp.",
     * "VMM_Name":"Intel_Thurley_Xen","VMM_Version":"11-4.1.0","VMM_OSName":"SUSE_LINUX","VMM_OSVersion":"11","AddOn_Connection_String":"intel:https://192.168.1.201:9999","Description":"Host updated"},
     * {"HostName":"192.168.1.202","BIOS_Name":"Intel_Corporation","BIOS_Version":"0060","BIOS_Oem":"Intel Corporation",
     * "VMM_Name":"Intel_Thurley_VMware_ESXi","VMM_Version":"5.1.0-799733","VMM_OSName":"VMware_ESXi","VMM_OSVersion":"5.1.0","AddOn_Connection_String":"vmware:https://192.168.1.222:443/sdk;Administrator;P@ssw0rd","Description":"Host updated"}]}
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * {"HostRecords":[{"Host_Name":"192.168.1.201","Status":"true","Error_Message":"","Error_Code":"OK"},{"Host_Name":"192.168.1.202","Status":"true","Error_Message":"","Error_Code":"OK"}]}<br>
     * <p>
     * <i><u>Sample Java API Call:</u></i><br>
            TxtHostRecord hostObj = new TxtHostRecord();<br>
            hostObj.HostName = "192.168.1.201";<br>
            hostObj.AddOn_Connection_String = "intel:https://192.168.1.201:9999";<br>
            hostObj.BIOS_Name = "Intel_Corp.";<br>
            hostObj.BIOS_Version = "T060";<br>
            hostObj.BIOS_Oem = "Intel Corp.";<br>
            hostObj.VMM_Name = "Intel_Thurley_Xen";<br>
            hostObj.VMM_Version = "11-4.1.0";<br>
            hostObj.VMM_OSName = "SUSE_LINUX";<br>
            hostObj.VMM_OSVersion = "11";<br>
            hostObj.Description = "Updated the host";
            TxtHostRecordList hosts = new TxtHostRecordList();
            hosts.getHostRecords().add(hostObj);
            HostConfigResponseList addHosts = apiClientObj.updateHosts(hosts);
     * <p>
     * 
     * @param hostRecords {@link TxtHostRecordList} object with the details of the hosts to be updated. For each of the hosts
     *  the HostName and AddOn_Connection_String [Open Source Hosts: intel:https://192.168.1.201:9999, Citrix XenServer: 
     * citrix:https://192.168.1.202:443/;root;pwd, VMware ESXi:vmware:https://192.168.1.222:443/sdk;Admin;password] are
     * required. To associate the host with the MLEs, BIOS_Name, BIOS_Version, BIOS_OEM, VMM_Name, VMM_Version, 
     * VMM_OSName & VMM_OSVersion have to be specified. All other parameters are optional.
     * @return {@link HostConfigResponseList} with the status for all the hosts. 
     * @throws IOException
     * @throws ApiException If there are any errors during the execution this exception would be returned to the caller.
     * The caller can use the getErrorCode() and getMessage() functions to retrieve the exception details.
     * @throws SignatureException
     * @see MtWilson#registerHosts(com.intel.mtwilson.datatypes.TxtHostRecordList) registerHosts function for automation of host registration.
     * @since MTW 1.2 Enterprise 
     */    
    HostConfigResponseList updateHosts(TxtHostRecordList hostRecords) throws IOException, ApiException, SignatureException;
    
    /**
     * Deletes the host specified from the system. 
     * <p>
     * <i><u>Roles needed:</u></i>Attestation
     * <p>
     * <i><u>Output content type:</u></i>Application/JSON
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: DELETE</i><br>
     * https://192.168.1.101:8181/AttestationService/resources/host?hostName=192.168.0.201<br>
     * <i><u>Sample Output:</u></i><br>
     * {"error_code":"OK","error_message":"OK"}<br>
     * <p>
     * <i><u>Sample Java API Call:</u></i><br>
     *             HostResponse deleteHost = apiClientObj.deleteHost(new Hostname("192.168.0.201"));
     * <p>
     * 
     * @param hostname {@link Hostname} to be deleted from the system. 
     * @return {@link HostResponse} with the details of the status. 
     * @throws IOException
     * @throws ApiException If there are any errors during the execution this exception would be returned to the caller.
     * The caller can use the getErrorCode() and getMessage() functions to retrieve the exception details.
     * @throws SignatureException
     * @since MTW 1.0 Enterprise/1.2 Opensource
     */        
    HostResponse deleteHost(Hostname hostname) throws IOException, ApiException, SignatureException;

    /**
     * Retrieves the list of hosts matching search criteria. Currently only search on the name is
     * supported. Empty search criteria retrieves all the hosts configured in the system.
     * <p>
     * <i><u>Roles needed:</u></i>Attestation/Report/Security
     * <p>
     * <i><u>Output content type:</u></i>Application/JSON
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: GET</i><br>
     * https://192.168.1.101:8181/AttestationService/resources/hosts?searchCriteria=201<br>
     * <p>
     * <i><u>Sample Output:</u></i><br>
     *[{"HostName":"192.168.1.201","IPAddress":"192.168.1.201","Port":9999,"BIOS_Name":"Intel_Corp.","BIOS_Version":"T060","BIOS_Oem":"Intel Corp.","VMM_Name":"Intel_Thurley_Xen","VMM_Version":"11-4.1.0","VMM_OSName":"SUSE_LINUX","VMM_OSVersion":"11","AddOn_Connection_String":"intel:https://192.168.1.201:9999","Description":null,"Email":null,"Location":null,"AIK_Certificate":null,"AIK_PublicKey":null,"AIK_SHA1":null,"Processor_Info":null}]
     * <p>
     * <i><u>Sample Java API Call:</u></i><br>
     * List<TxtHostRecord> queryForHosts = apiClientObj.queryForHosts("201");
     * <p>
     * @param searchCriteria search criteria specified by the user. Search criteria applies just for the host name.
     * @return List of {@link TxtHostRecord} objects matching the search criteria.
     * @throws IOException
     * @throws ApiException If there are any errors during the execution this exception would be returned to the caller.
     * The caller can use the getErrorCode() and getMessage() functions to retrieve the exception details.
     * @throws SignatureException 
     * @since MTW 1.0 Enterprise/1.2 Opensource
     */
    List<TxtHostRecord> queryForHosts(String searchCriteria) throws IOException, ApiException, SignatureException;
    
       /**
     * Retrieves the list of hosts matching search criteria. Currently only search on the name is
     * supported. Empty search criteria retrieves all the hosts configured in the system.
     * <p>
     * <i><u>Roles needed:</u></i>Attestation/Report/Security
     * <p>
     * <i><u>Output content type:</u></i>Application/JSON
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: GET</i><br>
     * https://192.168.1.101:8181/AttestationService/resources/hosts?searchCriteria=201&includeHardwareUuid=true<br>
     * <p>
     * <i><u>Sample Output:</u></i><br>
     *[{"HostName":"192.168.1.201","IPAddress":"192.168.1.201","Port":9999,"BIOS_Name":"Intel_Corp.","BIOS_Version":"T060","BIOS_Oem":"Intel Corp.","VMM_Name":"Intel_Thurley_Xen","VMM_Version":"11-4.1.0","VMM_OSName":"SUSE_LINUX","VMM_OSVersion":"11","AddOn_Connection_String":"intel:https://192.168.1.201:9999","Description":null,"Email":null,"Location":null,"AIK_Certificate":null,"AIK_PublicKey":null,"AIK_SHA1":null,"Processor_Info":null}]
     * <p>
     * <i><u>Sample Java API Call:</u></i><br>
     * List<TxtHostRecord> queryForHosts = apiClientObj.queryForHosts("201");
     * <p>
     * @param searchCriteria search criteria specified by the user. Search criteria applies just for the host name.
     * @parma includeHardwareUuid if set to true, api will include hardware_uuid field in txtHostRecord response, this will break backwards 1.2 compatabiltiy
     * @return List of {@link TxtHostRecord} objects matching the search criteria.
     * @throws IOException
     * @throws ApiException If there are any errors during the execution this exception would be returned to the caller.
     * The caller can use the getErrorCode() and getMessage() functions to retrieve the exception details.
     * @throws SignatureException 
     * @since MTW 1.0 Enterprise/1.2 Opensource
     */
    List<TxtHostRecord> queryForHosts(String searchCriteria,boolean includeHardwareUuid) throws IOException, ApiException, SignatureException;

    /**
     * Returns the host record with new features such as hardware uuid and tls policy.
     * 
     * @param searchCriteria
     * @return
     * @throws IOException
     * @throws ApiException
     * @throws SignatureException 
     * @since MTW 2.0
     */
    List<TxtHostRecord> queryForHosts2(String searchCriteria) throws IOException, ApiException, SignatureException;
    
    /**
     * Retrives the trust status of the list of hosts specified. This API is added for the OpenStack integration.
     * <p>
     * <i><u>Roles needed:</u></i>Attestation/Report
     * <p>
     * <i><u>Input content type:</u></i>Application/JSON
     * <p>
     * <i><u>Output content type:</u></i>Application/JSON
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: POST</i><br>
     * https://192.168.1.101:8181/AttestationService/resources/PollHosts<br>
     * <p>
     * <i>Sample Input</i><br>
     * {"hosts":["192.168.1.201","192.168.1.202"]}
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * {"hosts":[{"timestamp":"Wed Jun 5 02:06:16 2013","host_name":"192.168.1.201","trust_lvl":"trusted"},{"timestamp":"Wed Jun 5 02:06:16 2013","host_name":"192.168.1.201","trust_lvl":"untrusted"}]}<br>
     * <p>
     * <i><u>Sample Java API Call:</u></i><br>
            List<Hostname> hostObjects = new ArrayList<Hostname>();
            hostObjects.add(new Hostname("192.168.1.201"));
            hostObjects.add(new Hostname("192.168.1.202"));
            OpenStackHostTrustLevelReport pollHosts = apiClientObj.pollHosts(hostObjects);
     * <p>
     * 
     * @param hostnames List of {@link Hostname} hosts for which the trust attestation need to be performed.
     * @return {@link OpenStackHostTrustLevelReport} with the attestation status for each of the hosts specified.
     * @throws IOException
     * @throws ApiException If there are any errors during the execution this exception would be returned to the caller.
     * The caller can use the getErrorCode() and getMessage() functions to retrieve the exception details.
     * @throws SignatureException
     * @since MTW 1.0 Enterprise/1.2 Opensource
     */    
    OpenStackHostTrustLevelReport pollHosts(List<Hostname> hostnames) throws IOException, ApiException, SignatureException;

    /**
     * Retrieves the last 5 attestation results for the hosts specified. The API provides only the BIOS and they Hypervisor 
     * attestation status. If the user needs details on any of the failures (is present), then the getAttestationFailureReport can
     * be used.
     * <p>
     * <i><u>Roles needed:</u></i>Attestation/Report
     * <p>
     * <i><u>Input content type:</u></i>Text/Plain
     * <p>
     * <i><u>Output content type:</u></i>Application/XML
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: GET</i>
     * https://192.168.1.101:8181/AttestationService/resources/hosts/reports/trust?hostNames=192.168.1.201,192.168.1.126
     * <p>
     * <i><u>Sample Output:</u></i><br>
     *{@code <?xml version="1.0" encoding="UTF-8" standalone="yes"?><hosts_trust_report>
     * <Host Host_Name="192.168.1.201" MLE_Info="BIOS:Dell_Inc.-6.3.0,VMM:Dell_xen:6.1.0-4.1.3" Trust_Status="1" Verified_On="2013-05-24T00:12:02.014-07:00"/>
     * <Host Host_Name="192.168.1.201" MLE_Info="BIOS:Dell_Inc.-6.3.0,VMM:Dell_xen:6.1.0-4.1.3" Trust_Status="1" Verified_On="2013-05-23T23:49:31.559-07:00"/>
     * <Host Host_Name="192.168.1.201" MLE_Info="BIOS:Dell_Inc.-6.3.0,VMM:Dell_xen:6.1.0-4.1.3" Trust_Status="1" Verified_On="2013-05-23T23:37:49.450-07:00"/>
     * <Host Host_Name="192.168.1.201" MLE_Info= "BIOS:Dell_Inc.-6.3.0,VMM:Dell_xen:6.1.0-4.1.3" Trust_Status="1" Verified_On="2013-05-23T20:30:29.896-07:00"/>
     * <Host Host_Name="192.168.1.201" MLE_Info="BIOS:Dell_Inc.-6.3.0,VMM:Dell_xen:6.1.0-4.1.3" Trust_Status="1" Verified_On= "2013-05-23T10:18:17.908-07:00"/>
     * <Host Host_Name="192.168.1.126" MLE_Info="BIOS:Dell_Inc.-1.2.6,VMM:Dell_xen:6.1.0-4.1.3" Trust_Status="0" Verified_On="2013-05-24T00:11:56.761-07:00"/>
     * <Host Host_Name="192.168.1.126" MLE_Info="BIOS:Dell_Inc.-1.2.6,VMM:Dell_xen:6.1.0-4.1.3" Trust_Status="0" Verified_On="2013-05-23T23:49:26.189-07:00"/>
     * <Host Host_Name="192.168.1.126" MLE_Info="BIOS:Dell_Inc.-1.2.6,VMM:Dell_xen:6.1.0-4.1.3" Trust_Status="0" Verified_On="2013-05-23T23:37:44.091-07:00"/>
     * <Host Host_Name="192.168.1.126" MLE_Info="BIOS:Dell_Inc.-1.2.6,VMM:Dell_xen:6.1.0-4.1.3" Trust_Status="0" Verified_On="2013-05-23T20:30:17.958-07:00"/>
     * <Host Host_Name="192.168.1.126" MLE_Info="BIOS:Dell_Inc.-1.2.6,VMM:Dell_xen:6.1.0-4.1.3" Trust_Status="0" Verified_On="2013-05-23T18:59:09.997-07:00"/>
     * </hosts_trust_report>}
     * <p>
     * <i><u>Sample Java API Call:</u></i><br>
            List<Hostname> hostNames = new ArrayList<Hostname>();<br>
            hostNames.add(new Hostname("10.1.70.126"));<br>            
            HostsTrustReportType hostTrustReport = apiClientObj.getHostTrustReport(hostNames);<br>
            for(HostType hostType: hostTrustReport.getHost()) {<br>
                System.out.println(hostType.getHostName() + ":" + hostType.getTrustStatus());<br>
            }<br>
     * <p>
     * @param hostnames  {@link Hostname} List of the host names for which we need the trust report
     * @return {@link HostsTrustReportType} List of {@link HostType} objects with the details of the last 5 attestations.
     * @throws IOException
     * @throws ApiException
     * @throws SignatureException
     * @throws JAXBException
     * @since MTW 1.0 Enterprise/1.2 Opensource
     */
    HostsTrustReportType getHostTrustReport (List<Hostname> hostnames) throws IOException, ApiException, SignatureException, JAXBException;

    /**
     * Retrieves the details of the different PCRs that were verified and the verification status of all those PCRs for the last
     * successful attestation. Successful attestation includes both trusted or untrusted state of the host. Note that for 
     * Hypervisors supporting module level attestation, this report does not provide the details of all the modules that get 
     * extended to PCRs.  It provides a high level overview of the PCRs that were verified.
     * <p>
     * <i><u>Roles needed:</u></i>Attestation/Report
     * <p>
     * <i><u>Input content type:</u></i>Text/Plain
     * <p>
     * <i><u>Output content type:</u></i>Application/XML
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: GET</i>
     * https://192.168.1.101:8181/AttestationService/resources/hosts/reports/manifest?hostName=192.168.1.201
     * <p>
     * <i><u>Sample Output:</u></i><br>
     *{@code <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
     * <host_manifest_report>
     * <Host Name="10.1.71.201">
     * <Manifest TrustStatus="1" Name="17" Value="145f2174745ee28d6e0ef08ffb97d3b5e91276fd" Verified_On="2013-05-24T00:12:02.014-07:00"/>
     * <Manifest TrustStatus="1" Name="0" Value="0eb4fdf9ae6b9eb548655e28f0e8551c02122478" Verified_On="2013-05-24T00:12:02.014-07:00"/>
     * <Manifest TrustStatus="1" Name="18" Value="8cbd66606433c8b860de392efb30d76990a3b1ed" Verified_On="2013-05-24T00:12:02.014-07:00"/>
     * </Host>
     * </host_manifest_report>}
     * <p>
     * <i><u>Sample Java API Call:</u></i><br>
            HostManifestReportType hostManifestReport = apiClientObj.getHostManifestReport(new Hostname("10.1.71.201")); <br>
            System.out.println("Manifest report for:" + hostManifestReport.getHost().getName());<br>
            for(ManifestType hostMeasurements: hostManifestReport.getHost().getManifest()) {<br>
                System.out.println("Name:" + hostMeasurements.getName() + ":" + hostMeasurements.getTrustStatus());<br>
            }<br>
     * <p>
     * @param hostName  {@link Hostname} Name of the host for which we need the manifest/pcr report.
     * @return {@link HostManifestReportType} List of {@link ManifestType} for each of the PCRs verified for the host.
     * @throws IOException
     * @throws ApiException
     * @throws SignatureException
     * @throws JAXBException
     * @since MTW 1.0 Enterprise/1.2 Opensource
     */
    HostManifestReportType getHostManifestReport (Hostname hostname) throws IOException, ApiException, SignatureException, JAXBException;


    String getSamlForHostByAik(Sha1Digest aikSha1, boolean forceVerify) throws IOException, ApiException, SignatureException;
    
    /**
     * Retrieves the trust status of the host specified as a SAML assertion, which can be verified by caller. The 
     * SAML assertion containing the trust status of the host is signed by Mt.Wilson using its private key.
     * The API Client library provides helper functions to verify SAML assertion using Mt.Wilson's public key and extract
     * the contents of the assertion, if it is valid as shown in the example.
     * <p>
     * The REST API provides the option to the caller as to whether the user wants the trust status from the cache
     * (if valid) or force a complete attestation cycle. There is a different Java API getSamlForHost(Hostname hostname,
     * boolean forceVerify)where in the user can specify this  option. 
     * <p>
     * <i><u>Prerequisite:</u></i>The caller should have the downloaded the public key of Mt.Wilson
     * <p>
     * <i><u>Roles needed:</u></i>Attestation/Report
     * <p>
     * <i><u>Output content type:</u></i>Application/samlassertion+xml
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: GET</i>
     * https://192.168.1.101:8181/AttestationService/resources/saml/assertions/host?hostName=192.168.1.126&force_verify=false
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * {@code <?xml version="1.0" encoding="UTF-8"?><saml2:Assertion xmlns:saml2="urn:oasis:names:tc:SAML:2.0:assertion" 
     * ID="HostTrustAssertion" IssueInstant="2013-05-23T10:40:14.768Z" Version="2.0"><saml2:Issuer>https://192.168.0.101:8181
     * </saml2:Issuer><Signature xmlns="http://www.w3.org/2000/09/xmldsig#"><SignedInfo>
     * <CanonicalizationMethod Algorithm="http://www.w3.org/TR/2001/REC-xml-c14n-20010315#WithComments"/>
     * <SignatureMethod Algorithm="http://www.w3.org/2000/09/xmldsig#rsa-sha1"/><Reference URI="#HostTrustAssertion">
     * <Transforms><Transform Algorithm="http://www.w3.org/2000/09/xmldsig#enveloped-signature"/></Transforms>
     * <DigestMethod Algorithm="http://www.w3.org/2000/09/xmldsig#sha1"/><DigestValue>sxh6au9jxNgKfexWpolwigiIGuQ=
     * </DigestValue></Reference></SignedInfo><SignatureValue>kEfAQwUw2YeoDxwxx9WBM/LIp/YTDTZHm....
     * sHx05JCDGMITIRUuatCrNePaz0H/fDZYfSCGhQ==</SignatureValue><KeyInfo><X509Data>
     * <X509Certificate>MIIDVDCCAjygAwIBAgIEUZyfaTANBgkqhkiG9w0BAQUFADBsMQswCQYDVQQG......dw==</X509Certificate>
     * </X509Data></KeyInfo></Signature><saml2:Subject><saml2:NameID Format="urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified">
     * 198.168.0.126</saml2:NameID><saml2:SubjectConfirmation Method="urn:oasis:names:tc:SAML:2.0:cm:sender-vouches">
     * <saml2:NameID Format="urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified">AttestationService-0.5.4</saml2:NameID>
     * <saml2:SubjectConfirmationData Address="127.0.0.1" NotBefore="2013-05-23T10:40:14.817Z" NotOnOrAfter="2013-05-23T11:40:14.817Z"/>
     * </saml2:SubjectConfirmation></saml2:Subject><saml2:AttributeStatement><saml2:Attribute Name="Trusted">
     * <saml2:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
     * xsi:type="xs:anyType">false</saml2:AttributeValue></saml2:Attribute><saml2:Attribute Name="Trusted_BIOS">
     * <saml2:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
     * xsi:type="xs:anyType">true</saml2:AttributeValue></saml2:Attribute><saml2:Attribute Name="BIOS_Name">
     * <saml2:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
     * xsi:type="xs:string">Dell_Inc.</saml2:AttributeValue></saml2:Attribute><saml2:Attribute Name="BIOS_Version">
     * <saml2:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
     * xsi:type="xs:string">1.2.6</saml2:AttributeValue></saml2:Attribute><saml2:Attribute Name="BIOS_OEM">
     * <saml2:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
     * xsi:type="xs:string">Dell Inc.</saml2:AttributeValue></saml2:Attribute><saml2:Attribute Name="Trusted_VMM"><saml2:AttributeValue 
     * xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:anyType">
     * false</saml2:AttributeValue></saml2:Attribute><saml2:Attribute Name="Trusted_Location"><saml2:AttributeValue xmlns:xs=
     * "http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:anyType">false
     * </saml2:AttributeValue></saml2:Attribute><saml2:Attribute Name="AIK_PublicKey"><saml2:AttributeValue xmlns:xs=
     * "http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string">
     * -----BEGIN PUBLIC KEY-----&#13;
     * MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAvNEz3+TStAAndHTc1qwTNGvZYyB7DD1F&#13;
     * shQf+mbQUGJ9HccOXNn5oHB7fWQjODjlDrYyCs7FclSMTLxA3lHX98QWeWHL2O8t8qrJQQEUWZIT&#13;
     * mr/ddiNJOOvMeYF0K5if4m84vjgx/pTwwAVyU0YoMMXPnRozO8o7zSyRsH4jixALDugrsveEjLQI&#13;
     * /cIEFvNjqlhyfumHyJKywNkMH1oJ4e/f89FkpeDV694lsLs1jguuLLnvroXYJ5Uzeos+F0Pj1zFD&#13;
     * UvhWrjVwxsUfAxS85uFGTUm6EEl9XiKwi+mgg8ODrY5dh3uE2yKB2T1Qj8BfK55zB8cYbORSsm6/&#13;
     * f6BiBwIDAQAB&#13;
     * -----END PUBLIC KEY-----</saml2:AttributeValue></saml2:Attribute><saml2:Attribute Name="AIK_SHA1"><saml2:AttributeValue 
     * xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
     * xsi:type="xs:string">511d52f47b79378f124df6abf32e8addfdccd077</saml2:AttributeValue></saml2:Attribute>
     * </saml2:AttributeStatement></saml2:Assertion> }
     * <p>
     * <i><u>Sample Java API Call:</u></i><br>
          String samlForHost = apiClientObj.getSamlForHost(new Hostname("198.168.0.126"));<br>
            TrustAssertion trustAssertion = apiClientObj.verifyTrustAssertion(samlForHost);<br>
            if (trustAssertion.isValid()) {<br>
                for (String attr : trustAssertion.getAttributeNames()) {<br>
                   System.out.println("Signed attribute: " + attr + ":" + trustAssertion.getStringAttribute(attr));<br>
                }<br>
            }<br>
     * <p>
     * @param hostname {@link Hostname} Name of the host for which we need to get the trust status.
     * @return SAML assertion as a string.
     * @throws IOException
     * @throws ApiException
     * @throws SignatureException 
     * @since MTW 1.0 Enterprise
     */
    String getSamlForHost(Hostname hostname) throws IOException, ApiException, SignatureException;

    /**
     * Retrieves the trust status of the host specified as a SAML assertion, which can be verified by caller. Here the
     * user has the option of forcing a attestation/verification cycle or retrieve the SAML assertion from the cache if
     * valid. Retrieving from the cache provides better performance. If in case the cache is invalid/expired, the system
     * will automatically force a verification cycle and provides the user with the latest trust status. <br>
     * SAML assertion containing the trust status of the host is signed by Mt.Wilson using its private key.
     * The API Client library provides helper functions to verify SAML assertion using Mt.Wilson's public key and extract
     * the contents of the assertion, if it is valid as shown in the example.
     * <p>
     * <i><u>Prerequisite:</u></i>The caller should have the downloaded the public key of Mt.Wilson
     * <p>
     * <i><u>Roles needed:</u></i>Attestation/Report
     * <p>
     * <i><u>Output content type:</u></i>Application/samlassertion+xml
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: GET</i>
     * https://192.168.1.101:8181/AttestationService/resources/saml/assertions/host?hostName=192.168.1.126&force_verify=true
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * {@code <?xml version="1.0" encoding="UTF-8"?><saml2:Assertion xmlns:saml2="urn:oasis:names:tc:SAML:2.0:assertion" 
     * ID="HostTrustAssertion" IssueInstant="2013-05-23T10:40:14.768Z" Version="2.0"><saml2:Issuer>https://192.168.0.101:8181
     * </saml2:Issuer><Signature xmlns="http://www.w3.org/2000/09/xmldsig#"><SignedInfo>
     * <CanonicalizationMethod Algorithm="http://www.w3.org/TR/2001/REC-xml-c14n-20010315#WithComments"/>
     * <SignatureMethod Algorithm="http://www.w3.org/2000/09/xmldsig#rsa-sha1"/><Reference URI="#HostTrustAssertion">
     * <Transforms><Transform Algorithm="http://www.w3.org/2000/09/xmldsig#enveloped-signature"/></Transforms>
     * <DigestMethod Algorithm="http://www.w3.org/2000/09/xmldsig#sha1"/><DigestValue>sxh6au9jxNgKfexWpolwigiIGuQ=
     * </DigestValue></Reference></SignedInfo><SignatureValue>kEfAQwUw2YeoDxwxx9WBM/LIp/YTDTZHm....
     * sHx05JCDGMITIRUuatCrNePaz0H/fDZYfSCGhQ==</SignatureValue><KeyInfo><X509Data>
     * <X509Certificate>MIIDVDCCAjygAwIBAgIEUZyfaTANBgkqhkiG9w0BAQUFADBsMQswCQYDVQQG......dw==</X509Certificate>
     * </X509Data></KeyInfo></Signature><saml2:Subject><saml2:NameID Format="urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified">
     * 198.168.0.126</saml2:NameID><saml2:SubjectConfirmation Method="urn:oasis:names:tc:SAML:2.0:cm:sender-vouches">
     * <saml2:NameID Format="urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified">AttestationService-0.5.4</saml2:NameID>
     * <saml2:SubjectConfirmationData Address="127.0.0.1" NotBefore="2013-05-23T10:40:14.817Z" NotOnOrAfter="2013-05-23T11:40:14.817Z"/>
     * </saml2:SubjectConfirmation></saml2:Subject><saml2:AttributeStatement><saml2:Attribute Name="Trusted">
     * <saml2:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
     * xsi:type="xs:anyType">false</saml2:AttributeValue></saml2:Attribute><saml2:Attribute Name="Trusted_BIOS">
     * <saml2:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
     * xsi:type="xs:anyType">true</saml2:AttributeValue></saml2:Attribute><saml2:Attribute Name="BIOS_Name">
     * <saml2:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
     * xsi:type="xs:string">Dell_Inc.</saml2:AttributeValue></saml2:Attribute><saml2:Attribute Name="BIOS_Version">
     * <saml2:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
     * xsi:type="xs:string">1.2.6</saml2:AttributeValue></saml2:Attribute><saml2:Attribute Name="BIOS_OEM">
     * <saml2:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
     * xsi:type="xs:string">Dell Inc.</saml2:AttributeValue></saml2:Attribute><saml2:Attribute Name="Trusted_VMM"><saml2:AttributeValue 
     * xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:anyType">
     * false</saml2:AttributeValue></saml2:Attribute><saml2:Attribute Name="Trusted_Location"><saml2:AttributeValue xmlns:xs=
     * "http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:anyType">false
     * </saml2:AttributeValue></saml2:Attribute><saml2:Attribute Name="AIK_PublicKey"><saml2:AttributeValue xmlns:xs=
     * "http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string">
     * -----BEGIN PUBLIC KEY-----&#13;
     * MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAvNEz3+TStAAndHTc1qwTNGvZYyB7DD1F&#13;
     * shQf+mbQUGJ9HccOXNn5oHB7fWQjODjlDrYyCs7FclSMTLxA3lHX98QWeWHL2O8t8qrJQQEUWZIT&#13;
     * mr/ddiNJOOvMeYF0K5if4m84vjgx/pTwwAVyU0YoMMXPnRozO8o7zSyRsH4jixALDugrsveEjLQI&#13;
     * /cIEFvNjqlhyfumHyJKywNkMH1oJ4e/f89FkpeDV694lsLs1jguuLLnvroXYJ5Uzeos+F0Pj1zFD&#13;
     * UvhWrjVwxsUfAxS85uFGTUm6EEl9XiKwi+mgg8ODrY5dh3uE2yKB2T1Qj8BfK55zB8cYbORSsm6/&#13;
     * f6BiBwIDAQAB&#13;
     * -----END PUBLIC KEY-----</saml2:AttributeValue></saml2:Attribute><saml2:Attribute Name="AIK_SHA1"><saml2:AttributeValue 
     * xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
     * xsi:type="xs:string">511d52f47b79378f124df6abf32e8addfdccd077</saml2:AttributeValue></saml2:Attribute>
     * </saml2:AttributeStatement></saml2:Assertion> }
     * <p>
     * <i><u>Sample Java API Call:</u></i><br>
          String samlForHost = apiClientObj.getSamlForHost(new Hostname("198.168.0.126"), true);<br>
            TrustAssertion trustAssertion = apiClientObj.verifyTrustAssertion(samlForHost);<br>
            if (trustAssertion.isValid()) {<br>
                for (String attr : trustAssertion.getAttributeNames()) {<br>
                   System.out.println("Signed attribute: " + attr + ":" + trustAssertion.getStringAttribute(attr));<br>
                }<br>
            }<br>
     * <p>
     * @param hostname {@link Hostname} Name of the host for which we need to get the trust status.
     * @param forceVerify boolean flag indicating whether the system should retrieve the status from the cache or
     * force a complete verification cycle.
     * @return Signed SAML assertion represented as a string.
     * @throws IOException
     * @throws ApiException
     * @throws SignatureException 
     * @since MTW 1.0 Enterprise
     */
    String getSamlForHost(Hostname hostname, boolean forceVerify) throws IOException, ApiException, SignatureException;

    
    /**
     * Provides attestation/trust status for the list of hosts specified. Here the user has the option either to retrieve the 
     * trust status from the cache (if valid) or force a complete verification/attestation cycle. The trust status for all the
     * hosts are sent as signed SAML assertions.
     * 
     * The API Client library provides helper functions to verify SAML assertion using Mt.Wilson's public key and extract
     * the contents of the assertion, if it is valid as shown in the example.
     * <p>
     * <i><u>Prerequisite:</u></i>The caller should have the downloaded the public key of Mt.Wilson
     * <p>
     * <i><u>Roles needed:</u></i>Attestation/Report
     * <p>
     * <i><u>Output content type:</u></i>Application/XML
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: GET</i>
     * https://192.168.1.101:8181/AttestationService/resources/hosts/bulk/trust/saml?hosts=192.168.0.126,192.168.0.201&force_verify=false
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * {@code <Hosts><Host><Name>192.168.0.126</Name><ErrorCode>OK</ErrorCode><Assertion>
     * 
     * <?xml version="1.0" encoding="UTF-8"?><saml2:Assertion xmlns:saml2="urn:oasis:names:tc:SAML:2.0:assertion" 
     * ID="HostTrustAssertion" IssueInstant="2013-05-23T10:40:14.768Z" Version="2.0"><saml2:Issuer>https://192.168.0.101:8181
     * </saml2:Issuer><Signature xmlns="http://www.w3.org/2000/09/xmldsig#"><SignedInfo>
     * <CanonicalizationMethod Algorithm="http://www.w3.org/TR/2001/REC-xml-c14n-20010315#WithComments"/>
     * <SignatureMethod Algorithm="http://www.w3.org/2000/09/xmldsig#rsa-sha1"/><Reference URI="#HostTrustAssertion">
     * <Transforms><Transform Algorithm="http://www.w3.org/2000/09/xmldsig#enveloped-signature"/></Transforms>
     * <DigestMethod Algorithm="http://www.w3.org/2000/09/xmldsig#sha1"/><DigestValue>sxh6au9jxNgKfexWpolwigiIGuQ=
     * </DigestValue></Reference></SignedInfo><SignatureValue>kEfAQwUw2YeoDxwxx9WBM/LIp/YTDTZHm....
     * sHx05JCDGMITIRUuatCrNePaz0H/fDZYfSCGhQ==</SignatureValue><KeyInfo><X509Data>
     * <X509Certificate>MIIDVDCCAjygAwIBAgIEUZyfaTANBgkqhkiG9w0BAQUFADBsMQswCQYDVQQG......dw==</X509Certificate>
     * </X509Data></KeyInfo></Signature><saml2:Subject><saml2:NameID Format="urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified">
     * 198.168.0.126</saml2:NameID><saml2:SubjectConfirmation Method="urn:oasis:names:tc:SAML:2.0:cm:sender-vouches">
     * <saml2:NameID Format="urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified">AttestationService-0.5.4</saml2:NameID>
     * <saml2:SubjectConfirmationData Address="127.0.0.1" NotBefore="2013-05-23T10:40:14.817Z" NotOnOrAfter="2013-05-23T11:40:14.817Z"/>
     * </saml2:SubjectConfirmation></saml2:Subject><saml2:AttributeStatement><saml2:Attribute Name="Trusted">
     * <saml2:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
     * xsi:type="xs:anyType">false</saml2:AttributeValue></saml2:Attribute><saml2:Attribute Name="Trusted_BIOS">
     * <saml2:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
     * xsi:type="xs:anyType">true</saml2:AttributeValue></saml2:Attribute><saml2:Attribute Name="BIOS_Name">
     * <saml2:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
     * xsi:type="xs:string">Dell_Inc.</saml2:AttributeValue></saml2:Attribute><saml2:Attribute Name="BIOS_Version">
     * <saml2:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
     * xsi:type="xs:string">1.2.6</saml2:AttributeValue></saml2:Attribute><saml2:Attribute Name="BIOS_OEM">
     * <saml2:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
     * xsi:type="xs:string">Dell Inc.</saml2:AttributeValue></saml2:Attribute><saml2:Attribute Name="Trusted_VMM"><saml2:AttributeValue 
     * xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:anyType">
     * false</saml2:AttributeValue></saml2:Attribute><saml2:Attribute Name="Trusted_Location"><saml2:AttributeValue xmlns:xs=
     * "http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:anyType">false
     * </saml2:AttributeValue></saml2:Attribute><saml2:Attribute Name="AIK_PublicKey"><saml2:AttributeValue xmlns:xs=
     * "http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string">
     * -----BEGIN PUBLIC KEY-----&#13;
     * MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAvNEz3+TStAAndHTc1qwTNGvZYyB7DD1F&#13;
     * shQf+mbQUGJ9HccOXNn5oHB7fWQjODjlDrYyCs7FclSMTLxA3lHX98QWeWHL2O8t8qrJQQEUWZIT&#13;
     * mr/ddiNJOOvMeYF0K5if4m84vjgx/pTwwAVyU0YoMMXPnRozO8o7zSyRsH4jixALDugrsveEjLQI&#13;
     * /cIEFvNjqlhyfumHyJKywNkMH1oJ4e/f89FkpeDV694lsLs1jguuLLnvroXYJ5Uzeos+F0Pj1zFD&#13;
     * UvhWrjVwxsUfAxS85uFGTUm6EEl9XiKwi+mgg8ODrY5dh3uE2yKB2T1Qj8BfK55zB8cYbORSsm6/&#13;
     * f6BiBwIDAQAB&#13;
     * -----END PUBLIC KEY-----</saml2:AttributeValue></saml2:Attribute><saml2:Attribute Name="AIK_SHA1"><saml2:AttributeValue 
     * xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
     * xsi:type="xs:string">511d52f47b79378f124df6abf32e8addfdccd077</saml2:AttributeValue></saml2:Attribute>
     * </saml2:AttributeStatement></saml2:Assertion>]></Assertion></Host>
     * 
     * <Host>.........</Host>
     * </Hosts> }
     * <p>
     * <i><u>Sample Java API Call:</u></i><br>
            Set<Hostname> hostList = new HashSet<Hostname>();<br>
            hostList.add(new Hostname("192.168.0.126"));<br>
            hostList.add(new Hostname("192.168.0.201"));<br>
            List<HostTrustXmlResponse> hostTrustSAMLResponses = apiClientObj.getSamlForMultipleHosts(hostList, true);<br>
            for(HostTrustXmlResponse hostResponse: hostTrustSAMLResponses) {<br>
                String saml = hostResponse.getAssertion();<br>
                TrustAssertion trustAssertion = apiClientObj.verifyTrustAssertion(saml);<br>
                if (trustAssertion.isValid()) {<br>
                    for (String attr : trustAssertion.getAttributeNames()) {<br>
                        System.out.println("Signed attribute: " + attr + ":" + trustAssertion.getStringAttribute(attr));<br>
                    }<br>
                }<br>
            }<br>
     * <p>
     * @param hostnames {@link Hostname} List of host names for which we need to get the trust status.
     * @param forceVerify boolean flag indicating whether the system should retrieve the status from the cache or
     * force a complete verification cycle.
     * @return List of SAML responses {@link HostTrustXmlResponse} for each of the hosts specified.
     * @throws IOException
     * @throws ApiException
     * @throws SignatureException 
     * @see  HostTrustXmlResponse
     * @since MTW 1.0 Enterprise
    */
    List<HostTrustXmlResponse> getSamlForMultipleHosts(Set<Hostname> hostnames, boolean forceVerify) throws IOException, ApiException, SignatureException;

    /**
     * Provides the trust status of all the hosts specified in plain text (Non-SAML).  This function provides the option for the user
     * to decide whether to retrieve the trust status of the hosts either from cache or by going through the complete attestation
     * verification cycle. At least one host has to be specified in the set. If not, exception would be thrown to the user.
     * 
     * For users who want to ensure that the trust status indeed came from Mt.Wilson, they need to use 
     * {@link GetSamlForMultipleHosts} function which provides the attestation result as a signed SAML assertion..
     * <p>
     * <i><u>Roles needed:</u></i>Attestation/Report
     * <p>
     * <i><u>Output content type:</u></i>Application/JSON
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: GET</i>
     * https://192.168.1.101:8181/AttestationService/resources/hosts/bulk/trust?hosts=198.162.0.126,192.16.0.126.201&force_verify=false
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * {"Hosts":[{"error_code":"OK","error_message":"OK","vmm_status":1,"bios_status":1,"host_name":"198.162.0.201"},{"error_code":"OK","error_message":"OK","vmm_status":0,"bios_status":1,"host_name":"198.162.0.126"}]}
     * <p>
     * <i><u>Sample Java API Call:</u></i><br>
            Set<Hostname> hostList = new HashSet<Hostname>(); <br>
            hostList.add(new Hostname("198.162.0.126"));<br>
            hostList.add(new Hostname("198.162.0.201"));<br>
            BulkHostTrustResponse trustForMultipleHosts = apiClientObj.getTrustForMultipleHosts(hostList, true);<br>
            for(HostTrust hostTrust : trustForMultipleHosts.getHosts()) {<br>
                if (hostTrust.getErrorCodeEnum() == ErrorCode.OK) {<br>
                    System.out.println("HostName:" + hostTrust.getIpAddress() + " BIOS Status:" + hostTrust.getBiosStatus() + " VMM Status:" + hostTrust.getVmmStatus());<br>
                } else {<br>
                    System.out.println("HostName:" + hostTrust.getIpAddress() + " Error Message:" + hostTrust.getErrorMessage());<br>
                }<br>                   
            }<br>
     * <p>
     * @param hostnames {@link Hostname} List of host names for which we need to get the trust status.
     * @param forceVerify boolean flag indicating whether the system should retrieve the status from the cache or
     * @return {@link BulkHostTrustResponse} List of {@link  HostTrust} HostTrust responses for each host specified.
     * @throws IOException
     * @throws ApiException
     * @throws SignatureException 
     * @since MTW 1.0 Enterprise
     */
    BulkHostTrustResponse getTrustForMultipleHosts(Set<Hostname> hostnames, boolean forceVerify) throws IOException, ApiException, SignatureException;


    /**
     * @deprecated Not being used by anyone and this functionality need not be provided to end users.
     * @param hostname
     * @return
     * @throws IOException
     * @throws ApiException
     * @throws SignatureException 
     */
    // this method is used only by OpenSourceVMMHelper which is being replaced by IntelHostAgent; also the service implementation of this method only supports hosts with trust agents (even though vmware hosts also have their own attestation report)
   // String getHostAttestationReport(Hostname hostname) throws IOException, ApiException, SignatureException;
    
    
    /**
     * Retrieves the list of PCRs & Modules that failed attestation/verification. 
     * <p>
     * <i><u>Roles needed:</u></i>Attestation/Report
     * <p>
     * <i><u>Input content type:</u></i>Text/Plain
     * <p>
     * <i><u>Output content type:</u></i>Application/JSON
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: GET</i><br>
     * https://192.168.1.101:8181/AttestationService/resources/hosts/reports/attestationreport?hostName=192.168.0.201&failure_only=true<br>
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * {"PcrLogReport":[{"TrustStatus":0,"Name":19,"Value":"D99569B64D6F6C3D2C41865882CF1F6935282891","Verified_On":1370426370303,"WhitelistValue":"","ModuleLogs":[{"TrustStatus":0,"ComponentName":"componentName.esx-xserver-5.1.0-0.0.799733","Value":"","WhitelistValue":"AAAA79E9D9F4473E9F201A884450F51BC80B0980"}]}]}<br>
     * If all the PCRs & Modules matches, empty PcrLogReport would be returned back to the caller <br>
     * {"PcrLogReport":[]}
     * <p>
     * <i><u>Sample Java API Call:</u></i><br>
            AttestationReport attestationFailureReport = apiClientObj.getAttestationFailureReport(new Hostname("192.168.0.201"));
     * <p>
     * @param hostname  Name of the host for which the failure attestation report is needed. 
     * @return AttestationReport having the details of the PCR/Module that failed attestation/verification. 
     * @throws IOException
     * @throws ApiException If there are any errors during the execution this exception would be returned to the caller.
     * The caller can use the getErrorCode() and getMessage() functions to retrieve the exception details.
     * @throws SignatureException 
     * @since MTW 1.0 Enterprise/1.2 Opensource
     */
    AttestationReport getAttestationFailureReport(Hostname hostname)throws IOException, ApiException, SignatureException;
    
    
    /**
     * Retrieves the complete attestation report for the host. The attestation report includes all the PCRs and Modules and their
     * attestation/verification status. 
     * <p>
     * <i><u>Roles needed:</u></i>Attestation/Report
     * <p>
     * <i><u>Input content type:</u></i>Text/Plain
     * <p>
     * <i><u>Output content type:</u></i>Application/JSON
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: GET</i><br>
     * https://192.168.1.101:8181/AttestationService/resources/hosts/reports/attestationreport?hostName=192.168.0.201<br>
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * {"PcrLogReport":[{"TrustStatus":0,"Name":19,"Value":"D99569B64D6F6C3D2C41865882CF1F6935282891","Verified_On":1370426370303,"WhitelistValue":"",
     * "ModuleLogs":[{"TrustStatus":1,"ComponentName":"componentName.scsi-bnx2i-1.9.1d.v50.1-5vmw.510.0.0.799733","Value":"A996DEA4F36E065E8FC789799858BAE6A97FBD6F","WhitelistValue":"A996DEA4F36E065E8FC789799858BAE6A97FBD6F"},
     * {"TrustStatus":1,"ComponentName":"componentName.block-cciss-3.6.14-10vmw.510.0.0.799733","Value":"C9AAEEBD0CCC5A3CEE5E580AEC5D33BC4F7F2A5C","WhitelistValue":"C9AAEEBD0CCC5A3CEE5E580AEC5D33BC4F7F2A5C"}]},
     * {"TrustStatus":1,"Name":17,"Value":"496C8530D2B4BA6A6F3901455C8C240BBB482D85","Verified_On":1370426370303,"WhitelistValue":"496C8530D2B4BA6A6F3901455C8C240BBB482D85","ModuleLogs":[]},
     * {"TrustStatus":1,"Name":0,"Value":"5E724D834FEC48C62D523D95D08884DCAC7F4F98","Verified_On":1370426370303,"WhitelistValue":"5E724D834FEC48C62D523D95D08884DCAC7F4F98","ModuleLogs":[]},
     * {"TrustStatus":1,"Name":18,"Value":"F6FD306D2FA33E21C69CA598330B64DF1ED0D002","Verified_On":1370426370303,"WhitelistValue":"F6FD306D2FA33E21C69CA598330B64DF1ED0D002","ModuleLogs":[]},
     * {"TrustStatus":1,"Name":20,"Value":"7F824EA48E5D50A4B236152223206B00620BC74B","Verified_On":1370426370303,"WhitelistValue":"7F824EA48E5D50A4B236152223206B00620BC74B","ModuleLogs":[]}]}
     * <p>
     * <i><u>Sample Java API Call:</u></i><br>
            AttestationReport attestationReport = apiClientObj.getAttestationReport(new Hostname("10.1.71.155"));
     * <p>
     * @param hostname  Name of the host for which the attestation report is needed. 
     * @return AttestationReport having the details of all the PCR/Modules which were attested/verified. 
     * @throws IOException
     * @throws ApiException If there are any errors during the execution this exception would be returned to the caller.
     * The caller can use the getErrorCode() and getMessage() functions to retrieve the exception details.
     * @throws SignatureException 
     * @since MTW 1.0 Enterprise/1.2 Opensource
     */
    AttestationReport getAttestationReport(Hostname hostname) throws IOException, ApiException, SignatureException;

    /**
     * Reserved for the future release. This API is currently not supported.
     * @param hostname
     * @return
     * @throws IOException
     * @throws ApiException
     * @throws SignatureException 
     */
    X509Certificate getTlsCertificateForTrustedHost(Hostname hostname) throws IOException, ApiException, SignatureException;;

    /**
     * 
     * @param aTagObj
     * @return
     * @throws IOException
     * @throws ApiException
     * @throws SignatureException 
     */
    boolean importAssetTagCertificate(AssetTagCertCreateRequest aTagObj) throws IOException, ApiException, SignatureException;
    
    /**
     * 
     * @param aTagObj
     * @return
     * @throws IOException
     * @throws ApiException
     * @throws SignatureException 
     */
    boolean revokeAssetTagCertificate(AssetTagCertRevokeRequest aTagObj) throws IOException, ApiException, SignatureException;
    
    // WHITELIST SERVICE
    
    /**
     * Creates a new MLE (Measured Launch Environment]. MLEs can be either BIOS or OS/Hypervisor. MLEs define what the 
     * good known values/white list values/finger print should be. When these MLEs are associated with the hosts for attestation, then
     * the measured values from the host are compared against the good known values defined for the MLEs. If they match then
     * that component {BIOS or OS/Hypervisor} is trusted. <br>
     * Hosts are always associated with both BIOS and OS/Hypervisors MLEs. If both the MLEs evaluate to trusted state, then the host
     * is trusted. If either one is untrusted, the the overall trust status of the host is untrusted. <br>
     * Instead of creating the OS/OEM & MLEs manually, users can opt to use the automation APIs {@link ManagementService}.
     * <p>
     * <i><u>Pre-requisite:</u></i>For creating BIOS MLE, the OEM has to be created first since BIOS is always associated with the OEM. 
     * For OS/Hypervisor MLE, the OS on which the hypervisor would be installed should be configured first. In case of VMware ESXi
     * and Citrix XenServer there are no separation between the OS & Hypervisor components. They are the same. But Open Source hypervisors
     * like Xen & KVM can be installed on Ubuntu/RHEL & SUSE. <br>
     * Currently on Xen & KVM installed on Ubuntu, RHEL and SUSE are supported in the system.
     * <p>
     * <i><u>Roles needed:</u></i>Whitelist
     * <p>
     * <i><u>Input content type:</u></i>Application/JSON
     * <p>
     * <i><u>Output content type:</u></i>Text/Plain
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: POST</i><br>
     * https://192.168.1.101:8181/WLMService/resources/mles<br>
     * <i>Sample Input for BIOS MLE.</i><br>
     * {"Name":"Intel_BIOS_MLE","Version":"T060","Attestation_Type": "PCR","MLE_Type":"BIOS","Description":"","MLE_Manifests": [{"Name": "0",  "Value": "ADC83B19E793491B1C6EA0FD8B46CD9F32E592FC"}],"OsName":"","OsVersion":"","OemName":"INTEL"} <br>
     * Note: For BIOS MLE, the OsName and OsVersion fields can be either ignored or set to empty string.<br><br>
     * <i>Sample Input for VMM/Hypervisor MLE.</i><br>
     * {"Name":"Xen_MLE","Version":"4.1.0","Attestation_Type": "MODULE","MLE_Type":"VMM","Description":"","MLE_Manifests": [{"Name": "17",  "Value": "ADC83B19E793491B1C6EA0FD8B46CD9F32E592FC"}, {"Name": "18",  "Value": "ADC83B19E793491B1C6EA0FD8B46CD9F32E592FC"}, {"Name": "19",  "Value": ""}],"OsName":"UBUNTU","OsVersion":"11.10","OemName":""}<br>
     * Note: For VMM MLE, the OemName field can be either ignored or set to empty string.
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * true<br>
     * <p>
     * <i><u>Sample Java API Call:</u></i><br>
     *     // For creating BIOS MLE<br>
            List<ManifestData> mleWhiteList = new ArrayList<ManifestData>(); <br>
            mleWhiteList.add(new ManifestData("0", "ADC83B19E793491B1C6EA0FD8B46CD9F32E592FC"));<br>
            MleData mleObj = new MleData("Intel_BIOS_MLE", "T060", MleData.MleType.BIOS, MleData.AttestationType.PCR, mleWhiteList, "", "", "", "INTEL");<br>
            boolean addMLE = apiClientObj.addMLE(mleObj);<br><br>
            // For creation VMM/Hypervisor MLE<br>
            List<ManifestData> mleVMMWhiteList = new ArrayList<ManifestData>();<br>
            mleVMMWhiteList.add(new ManifestData("17", "ADC83B19E793491B1C6EA0FD8B46CD9F32E592FC"));<br>
            mleVMMWhiteList.add(new ManifestData("18", "ADC83B19E793491B1C6EA0FD8B46CD9F32E592FC"));<br>
            mleVMMWhiteList.add(new ManifestData("19", ""));<br>
            MleData mleObj = new MleData("Xen_MLE", "4.1.0", MleData.MleType.VMM, MleData.AttestationType.MODULE, mleVMMWhiteList, "", "UBUNTU", "11.10", "");<br>
            boolean addMLE = apiClientObj.addMLE(mleObj);<br>            
     * <p>
     * 
     * @param mle {@link MleData} object. For creating BIOS MLEs, user has to specify the Name, Version, Attestation_Type as 
     * PCR [Defines how the verification of the measurements are done. Possible options are PCR & MODULE], MLE_Type as BIOS
     * [Possible options are BIOS and VMM], optional description, list of {@link ManifestData} for each of the BIOS PCRs to be 
     * verified[Valid BIOS PCR names are 0, 1, 2, 3, 4 & 5] and OemName. For BIOS MLE, the OsName and OsVersion should be
     * set to empty strings. If the user wants to set the white list values for the PCRs at a later point of time, then can do so and
     * during the creation of MLEs set them to empty strings. <br>
     * For creating VMM MLEs, user has to specify the Name, Version, Attestation_Type as either PCR or Module [For VMware ESXi,
     * Open Source Xen & KVM it is Module. For Citrix XenServer it is PCR], MLE_Type as VMM, optional description, list of 
     * {@link ManifestData} for each of the VMM PCRs to be verified[Valid VMM PCRs names are 17, 18, 19 & 20. PCR 20 is valid only
     * for VMware ESXi. Currently only PCR 19 provides module level information. So, user has to call {@link addModuleWhiteList}
     * method to configure the modules that gets extended to PCR 19. During the creation of MLE, PCR 19 should be set to empty string 
     * for MODULE Attestation_Type], OsName and OsVersion. OemName is not needed for the creation of VMM MLEs and  
     * can be set to empty strings.
     * @return boolean value indicating whether the request was executed successfully or not. 
     * @throws IOException
     * @throws ApiException If there are any errors during the execution this exception would be returned to the caller.
     * The caller can use the getErrorCode() and getMessage() functions to retrieve the exception details.
     * @throws SignatureException 
     * @since MTW 1.0 Enterprise/1.2 Opensource
     */
    boolean addMLE(MleData mle) throws IOException, ApiException, SignatureException;
    
    
    /**
     * Updates  an existing MLE (Measured Launch Environment]. Only the description and the white lists can be updated. <br>
     * Instead of updating the OS/OEM & MLEs manually, users can opt to use the automation APIs {@link ManagementService}.
     * <p>
     * <i><u>Roles needed:</u></i>Whitelist
     * <p>
     * <i><u>Input content type:</u></i>Application/JSON
     * <p>
     * <i><u>Output content type:</u></i>Text/Plain
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: PUT</i><br>
     * https://192.168.1.101:8181/WLMService/resources/mles<br>
     * <i>Sample Input for BIOS MLE.</i><br>
     * {"Name":"Intel_BIOS_MLE","Version":"T060","Attestation_Type": "PCR","MLE_Type":"BIOS","Description":"Updated","MLE_Manifests": [{"Name": "0",  "Value": "BBC83B19E793491B1C6EA0FD8B46CD9F32E592FC"}],"OsName":"","OsVersion":"","OemName":"INTEL"} <br>
     * <i>Sample Input for VMM/Hypervisor MLE.</i><br>
     * {"Name":"Xen_MLE","Version":"4.1.0","Attestation_Type": "MODULE","MLE_Type":"VMM","Description":"Updated","MLE_Manifests": [{"Name": "17",  "Value": "BBC83B19E793491B1C6EA0FD8B46CD9F32E592FC"}, {"Name": "18",  "Value": "BBC83B19E793491B1C6EA0FD8B46CD9F32E592FC"}, {"Name": "19",  "Value": ""}],"OsName":"UBUNTU","OsVersion":"11.10","OemName":""}<br>
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * true<br>
     * <p>
     * <i><u>Sample Java API Call:</u></i><br>
     * HostTrustResponse hostTrust = apiClientObj.getHostTrust(new Hostname("10.1.70.126"));<br>
     * System.out.println("BIOS trust status: " + hostTrust.trust.bios + " Hypervisor trust status" + hostTrust.trust.vmm);
     * <p>
     * 
     * @param mle {@link MleData} object. For updating BIOS MLEs, user has to specify the Name, Version, Attestation_Type 
     * MLE_Type, optional description,  list of {@link ManifestData} for each of the BIOS PCRs to be verified[Valid BIOS PCR 
     * names are 0, 1, 2, 3, 4 & 5] and OemName.  For BIOS MLE, the OsName and OsVersion should be set to empty strings.  <br>
     * For updating VMM MLEs, user has to specify the Name, Version, Attestation_Type, MLE_Type as VMM, optional description, list of 
     * {@link ManifestData} for each of the VMM PCRs to be verified[Valid VMM PCRs names are 17, 18, 19 & 20. PCR 20 is valid only
     * for VMware ESXi. Currently only PCR 19 provides module level information. So, user has to call {@link addModuleWhiteList}
     * method to update the modules that gets extended to PCR 19. During the updation of MLE, PCR 19 should be set to empty string
     * for MODULE Attestation_Type],
     * OsName and OsVersion. OemName is not needed for the creation of VMM MLEs and  can be set to empty strings.
     * @return boolean value indicating whether the request was executed successfully or not. 
     * @throws IOException
     * @throws ApiException If there are any errors during the execution this exception would be returned to the caller.
     * The caller can use the getErrorCode() and getMessage() functions to retrieve the exception details.
     * @throws SignatureException 
     * @since MTW 1.0 Enterprise/1.2 Opensource
     */
    boolean updateMLE(MleData mle) throws IOException, ApiException, SignatureException;

    /**
     * Retrieves the list of MLEs configured in the system that matches the search criteria. Currently only search on the name is
     * supported. Note that this won't retrieve the details of all the associated white lists. To retrieve the white lists the 
     * user has to call the {@link getMLEManifest} function.
     * <p>
     * <i><u>Roles needed:</u></i>Whitelist
     * <p>
     * <i><u>Output content type:</u></i>Application/JSON
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: GET</i><br>
     * https://192.168.1.101:8181/WLMService/resources/mles?searchCriteria=Intel<br>
     * To retrieve all the configured MLEs<br>
     * https://192.168.1.101:8181/WLMService/resources/mles
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * [{"Name":"Intel_BIOS_MLE","Version":"T060","Attestation_Type":"PCR","MLE_Type":"BIOS","Description":"Updated","OsName":null,"OsVersion":null,"OemName":"INTEL","MLE_Manifests":null}]
     * <p>
     * <i><u>Sample Java API Call:</u></i><br>
     * List<MleData> searchMLE = apiClientObj.searchMLE("Intel"); <br>
            for (MleData mleObj:searchMLE){<br>
                System.out.println(mleObj.getName());<br>
            }<br>
     * <p>
     * @param name search criteria specified by the user
     * @return List of {@link MleData} objects matching the search criteria.
     * @throws IOException
     * @throws ApiException If there are any errors during the execution this exception would be returned to the caller.
     * The caller can use the getErrorCode() and getMessage() functions to retrieve the exception details.
     * @throws SignatureException 
     * @since MTW 1.0 Enterprise/1.2 Opensource
     */
    List<MleData> searchMLE(String name) throws IOException, ApiException, SignatureException;

    /**
     * Retrieves the details of the MLE specified along with the associated white list. Unlike the search function, this
     * would always return a single MLE object satisfying the specified search criteria. The search criteria includes name,
     * version, oemName(for BIOS MLEs), osName/osVersion (for VMM/Hypervisor MLEs). 
     * <p>
     * <i><u>Roles needed:</u></i>Whitelist
     * <p>
     * <i><u>Output content type:</u></i>Application/JSON
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: GET</i><br>
     * https://10.1.71.95:8443/WLMService/resources/mles/manifest?mleName=Xen_MLE&mleVersion=4.1.0&osName=UBUNTU&osVersion=11.10<br>
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * {"Name":"Xen_MLE","Version":"4.1.0","Attestation_Type":"MODULE","MLE_Type":"VMM","Description":"MLE Updated","OsName":"UBUNTU","OsVersion":"11.10","OemName":null,"MLE_Manifests":[{"Name":"19","Value":""},{"Name":"17","Value":"BBC83B19E793491B1C6EA0FD8B46CD9F32E592FC"},{"Name":"18","Value":"BBC83B19E793491B1C6EA0FD8B46CD9F32E592FC"}]}
     * <p>
     * <i><u>Sample Java API Call:</u></i><br>
     *       MLESearchCriteria mleSearchObj = new MLESearchCriteria();<br>
            mleSearchObj.mleName = "Xen_MLE";<br>
            mleSearchObj.mleVersion = "4.1.0";<br>
            mleSearchObj.osName = "UBUNTU";<br>
            mleSearchObj.osVersion = "11.10";<br>
            mleSearchObj.oemName = "";<br>
            MleData mleManifest = apiClientObj.getMLEManifest(mleSearchObj);<br>
     * <p>
     * 
     * @param criteria specified in {@link MLESearchCriteria} object. For BIOS MLEs user has to specify the name, version,
     * and the oemName. The osName and osVersion has to be set to empty strings. For VMM MLEs, user has to specify the 
     * name, version, osName and osVersion. The oemName has to be set to empty string.
     * @return {@link MleData} object matching the search criteria along with all the associated white lists.
     * @throws IOException 
     * @throws ApiException If there are any errors during the execution this exception would be returned to the caller.
     * The caller can use the getErrorCode() and getMessage() functions to retrieve the exception details.
     * @throws SignatureException 
     * @since MTW 1.0 Enterprise/1.2 Opensource
     */    
    MleData getMLEManifest(MLESearchCriteria criteria) throws IOException, ApiException, SignatureException;

    /**
     * Deletes the MLE matching the specified search criteria.  The search criteria includes name,
     * version, oemName(for BIOS MLEs), osName/osVersion (for VMM/Hypervisor MLEs). 
     * <p>
     * <i><u>Roles needed:</u></i>Whitelist
     * <p>
     * <i><u>Output content type:</u></i>Application/JSON
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: DELETE</i><br>
     * https://10.1.71.95:8443/WLMService/resources/mles?mleName=Xen_MLE&mleVersion=4.1.0&osName=UBUNTU&osVersion=11.10<br>
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * true <br>
     * If the MLE that the user is trying to delete does not exist in the system, the user would be sent back an appropriate error.<br>
     * {"error_code":"WS_MLE_DOES_NOT_EXIST","error_message":"MLE 'Xen_MLE' of version '4.1.0' is not configured in the system."}
     * <p>
     * <i><u>Sample Java API Call:</u></i><br>
            MLESearchCriteria mleSearchObj = new MLESearchCriteria();<br>
            mleSearchObj.mleName = "Intel_BIOS_MLE";<br>
            mleSearchObj.mleVersion = "T060";<br>
            mleSearchObj.osName = "";<br>
            mleSearchObj.osVersion = "";<br>
            mleSearchObj.oemName = "INTEL";<br>
            boolean deleteMLE = apiClientObj.deleteMLE(mleSearchObj);
     * <p>
     * 
     * @param criteria specified in {@link MLESearchCriteria} object. For BIOS MLEs user has to specify the name, version,
     * and the oemName. The osName and osVersion has to be set to empty strings. For VMM MLEs, user has to specify the 
     * name, version, osName and osVersion. The oemName has to be set to empty string.
     * @return boolean indicating whether function executed successfully or not.
     * @throws IOException 
     * @throws ApiException If there are any errors during the execution this exception would be returned to the caller.
     * The caller can use the getErrorCode() and getMessage() functions to retrieve the exception details.
     * @throws SignatureException 
     * @since MTW 1.0 Enterprise/1.2 Opensource
     */    
    boolean deleteMLE(MLESearchCriteria criteria) throws IOException, ApiException, SignatureException;

    /**
     * Retrieves the list of all the OEMs configured in the system. 
     * <p>
     * <i><u>Roles needed:</u></i>Whitelist
     * <p>
     * <i><u>Output content type:</u></i>Application/JSON
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: GET</i><br>
     * https://192.168.1.101:8181/WLMService/resources/oem <br>
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * [{"Name":"GENERIC","Description":"Default Oem for testing"},{"Name":"Intel","Description":"Intel white boxes"}]
     * <p>
     * <i><u>Sample Java API Call:</u></i><br>
            List<OemData> listAllOEM = apiClientObj.listAllOEM();<br>
            for (OemData oemObj: listAllOEM) {<br>
                System.out.println(oemObj.getName());<br>
            }
     * <p>
     * @return List of {@link OemData} objects each having the name and description of the OEMs configured. 
     * @throws IOException
     * @throws ApiException If there are any errors during the execution this exception would be returned to the caller.
     * The caller can use the getErrorCode() and getMessage() functions to retrieve the exception details.
     * @throws SignatureException 
     * @since MTW 1.0 Enterprise/1.2 Opensource
     */    
    List<OemData> listAllOEM() throws IOException, ApiException, SignatureException;

    /**
     * Creates a new OEM in the system. This would be used to associate with BIOS MLE[Measured Launch Environment] during
     * the creation of BIOS MLE
     * Only Name is the required parameter. Description field is optional.
     * <p>
     * <i><u>Roles needed:</u></i>Whitelist
     * <p>
     * <i><u>Input content type:</u></i>Application/JSON
     * <p>
     * <i><u>Output content type:</u></i>Text/Plain
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: POST</i><br>
     * https://192.168.1.101:8181/WLMService/resources/oem <br>
     * <p>
     * <i>Sample Input</i><br>
     * {"Name":"INTEL","Description":"Intel OEM"}
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * true<br>
     * If the OEM you are trying to create already exists or any other exception occurs then the error details would be sent back to the caller <br>
     * {"error_code":"WS_OEM_ALREADY_EXISTS","error_message":"OEM 'INTEL' is already configured in the system."}
     * <p>
     * <i><u>Sample Java API Call:</u></i><br>
            OemData oemdata = new OemData("INTEL", "Intel OEM");<br>
            boolean result = apiClientObj.addOEM(oemdata);<br>
     * <p>
     * @param oem {@link OemData} object having the OEM name and description. Description field is optional.
     * @return boolean value indicating whether the request was executed successfully or not. 
     * @throws IOException
     * @throws ApiException If there are any errors during the execution like OEM already exists or so, this exception would be returned to the caller.
     * The caller can use the getErrorCode() and getMessage() functions to retrieve the exception details.
     * @throws SignatureException 
     * @since MTW 1.0 Enterprise/1.2 Opensource
     */
    boolean addOEM(OemData oem) throws IOException, ApiException, SignatureException;

    /**
     * Updates the existing OEM in the system. Only the description field is editable. If the OEM that the user wants to update
     * does not exist in the system, then appropriate error would be returned back to the caller. Users can use the listAllOEM to 
     * get the list of all the OEMs currently configured in the system and update the required one.
     * <p>
     * <i><u>Roles needed:</u></i>Whitelist
     * <p>
     * <i><u>Input content type:</u></i>Application/JSON
     * <p>
     * <i><u>Output content type:</u></i>Text/Plain
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: PUT</i><br>
     * https://192.168.1.101:8181/WLMService/resources/oem <br>
     * <p>
     * <i>Sample Input</i><br>
     * {"Name":"INTEL","Description":"Updated description"}
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * true<br>
     * If the OEM you are trying to update does not exist or any other exception occurs then the error details would be sent back to the caller <br>
     * {"error_code":"WS_OEM_DOES_NOT_EXIST","error_message":"OEM 'INTEL' is not configured in the system."}
     * <p>
     * <i><u>Sample Java API Call:</u></i><br>
            OemData oemdata = new OemData("INTEL", "Updated description");<br>
            boolean result = apiClientObj.addOEM(oemdata);<br>
     * <p>
     * @param oem {@link OemData} object having the OEM name and description. OEM name is required and description field is optional.
     * @return boolean value indicating whether the request was executed successfully or not. 
     * @throws IOException
     * @throws ApiException If there are any errors during the execution this exception would be returned to the caller.
     * The caller can use the getErrorCode() and getMessage() functions to retrieve the exception details.
     * @throws SignatureException 
     * @since MTW 1.0 Enterprise/1.2 Opensource
     */
    boolean updateOEM(OemData oem) throws IOException, ApiException, SignatureException;

    /**
     * Deletes an existing OEM in the system. An OEM can be deleted only if it is not associated with any BIOS MLE.
     * <p>
     * <i><u>Roles needed:</u></i>Whitelist
     * <p>
     * <i><u>Input content type:</u></i>Text/Plain
     * <p>
     * <i><u>Output content type:</u></i>Text/Plain
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: DELETE</i><br>
     * https://192.168.1.101:8181/WLMService/resources/oem?Name=INTEL <br>
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * true<br>
     * If the OEM the user is trying to update does not exist or any other exception occurs then the error details would be sent back to the caller <br>
     * {"error_code":"WS_OEM_DOES_NOT_EXIST","error_message":"OEM 'INTEL' is not configured in the system."}<br>
     * If the OEM that the user is trying to delete is currently associated with an MLE, then an exception with the below information would be
     * returned back. <br>
     * {"error_code":"WS_OEM_ASSOCIATION_EXISTS","error_message":"OEM 'INTEL' cannot be deleted as it is associated with 1 MLEs."}
     * <p>
     * <i><u>Sample Java API Call:</u></i><br>
            boolean deleteOEM = apiClientObj.deleteOEM("INTEL");
     * <p>
     * @param name Name of the OEM to be deleted.
     * @return boolean value indicating whether the request was executed successfully or not. 
     * @throws IOException
     * @throws ApiException If there are any errors during the execution this exception would be returned to the caller.
     * The caller can use the getErrorCode() and getMessage() functions to retrieve the exception details.
     * @throws SignatureException 
     * @since MTW 1.0 Enterprise/1.2 Opensource
     */    
    boolean deleteOEM(String name) throws IOException, ApiException, SignatureException;

    /**
     * Retrieves the list of all the OS configured in the system. 
     * <p>
     * <i><u>Roles needed:</u></i>Whitelist
     * <p>
     * <i><u>Output content type:</u></i>Application/JSON
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: GET</i><br>
     * https://192.168.1.101:8181/WLMService/resources/os <br>
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * [{"Name":"RHEL","Version":"6.1","Description":null},{"Name":"RHEL","Version":"6.2","Description":null},{"Name":"UBUNTU","Version":"11.10","Description":null}]
     * <p>
     * <i><u>Sample Java API Call:</u></i><br>
            List<OsData> listAllOS = apiClientObj.listAllOS(); <br>
            for (OsData osObj: listAllOS) {<br>
                System.out.println(osObj.getName() + ":" + osObj.getVersion());<br>
            }
     * <p>
     * @return List of {@link OsData} objects each having the details of the OS configuration. 
     * @throws IOException
     * @throws ApiException If there are any errors during the execution this exception would be returned to the caller.
     * The caller can use the getErrorCode() and getMessage() functions to retrieve the exception details.
     * @throws SignatureException 
     * @since MTW 1.0 Enterprise/1.2 Opensource
     */        
    List<OsData> listAllOS() throws IOException, ApiException, SignatureException;

    /**
     * Updates an existing Operating system (OS) in the system. Only the description of the OS can be updated.
     * <p>
     * <i><u>Roles needed:</u></i>Whitelist
     * <p>
     * <i><u>Input content type:</u></i>Application/JSON
     * <p>
     * <i><u>Output content type:</u></i>Text/Plain
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: PUT</i><br>
     * https://192.168.1.101:8181/WLMService/resources/os <br>
     * <i>Sample Input</i><br>
     * {"Name":"RHEL","Version":"6.1","Description":"RHEL Updated"}
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * true<br>
     * If the OS you are trying to update does not exist or any other exception occurs then the error details would be sent back to the caller <br>
     * {"error_code":"WS_OS_DOES_NOT_EXIST","error_message":"OS 'RHEL' of version '6.1' is not configured in the system."}
     * <p>
     * <i><u>Sample Java API Call:</u></i><br>
           boolean updateOS = apiClientObj.updateOS(new OsData("RHEL", "6.1", "RHEL Updated"));
     * <p>
     * @param os {@link OsData} object having the OS name, version and description. Note that the name and version specified should
     * match an existing OS in the system. Otherwise an exception would be thrown to the user.
     * @return boolean value indicating whether the request was executed successfully or not. 
     * @throws IOException
     * @throws ApiException If there are any errors during the execution like OS already exists or so, this exception would be returned to the caller.
     * The caller can use the getErrorCode() and getMessage() functions to retrieve the exception details.
     * @throws SignatureException 
     * @since MTW 1.0 Enterprise/1.2 Opensource
     */
    boolean updateOS(OsData os) throws IOException, ApiException, SignatureException;

    /**
     * Creates a new Operating system (OS) in the system. This would be used to associate with VMM/Hypervisor MLE[Measured Launch Environment] during
     * its creation. Both OS name and version are required parameters. Description field is optional.
     * <p>
     * <i><u>Roles needed:</u></i>Whitelist
     * <p>
     * <i><u>Input content type:</u></i>Application/JSON
     * <p>
     * <i><u>Output content type:</u></i>Text/Plain
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: POST</i><br>
     * https://192.168.1.101:8181/WLMService/resources/os <br>
     * <p>
     * <i>Sample Input</i><br>
     * {"Name":"RHEL","Version":"6.1","Description":"RHEL"}
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * true<br>
     * If the OS you are trying to create already exists or any other exception occurs then the error details would be sent back to the caller <br>
     * {"error_code":"WS_OS_ALREADY_EXISTS","error_message":"OS 'RHEL' of version '6.1' is already configured in the system."}
     * <p>
     * <i><u>Sample Java API Call:</u></i><br>
           boolean addOS = apiClientObj.addOS(new OsData("RHEL", "6.1", "RHEL OS"));
     * <p>
     * @param os {@link OsData} object having the OS name, version and description. Description field is optional.
     * @return boolean value indicating whether the request was executed successfully or not. 
     * @throws IOException
     * @throws ApiException If there are any errors during the execution like OS already exists or so, this exception would be returned to the caller.
     * The caller can use the getErrorCode() and getMessage() functions to retrieve the exception details.
     * @throws SignatureException 
     * @since MTW 1.0 Enterprise/1.2 Opensource
     */
    boolean addOS(OsData os) throws IOException, ApiException, SignatureException;

    /**
     * Deletes an existing OS in the system. Since the OS is uniquely identified by the combination of the name and version
     * both of them have to be specified in the OsData object. An OS can be deleted only if it is not associated with any
     * MLEs. Otherwise appropriate error would be returned back to the caller.
     * <p>
     * <i><u>Roles needed:</u></i>Whitelist
     * <p>
     * <i><u>Input content type:</u></i>Application/JSON
     * <p>
     * <i><u>Output content type:</u></i>Text/Plain
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: DELETE</i><br>
     * https://192.168.1.101:8181/WLMService/resources/os?Name=RHEL&Version=6.1 <br>
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * true<br>
     * If the OS you are trying to delete does not exist or any other exception occurs then the error details would be sent back to the caller <br>
     * {"error_code":"WS_OS_DOES_NOT_EXIST","error_message":"OS 'RHEL' of version '6.1' is not configured in the system."} <br>
     * If in case the OS is associated with an MLE, then the user will get the below error.<br>
     * {"error_code":"WS_OS_ASSOCIATION_EXISTS","error_message":"OS 'RHEL' of version '6.1' cannot be deleted as it is associated with 1 MLEs."}
     * <p>
     * <i><u>Sample Java API Call:</u></i><br>
            boolean deleteOS = apiClientObj.deleteOS(new OsData("RHEL", "6.1"));
     * <p>
     * @param os {@link OsData} object specifying the name and version of the OS to be deleted.
     * @return boolean value indicating whether the request was executed successfully or not. 
     * @throws IOException
     * @throws ApiException If there are any errors during the execution this exception would be returned to the caller.
     * The caller can use the getErrorCode() and getMessage() functions to retrieve the exception details.
     * @throws SignatureException 
     * @since MTW 1.0 Enterprise/1.2 Opensource
     */         
    boolean deleteOS(OsData os) throws IOException, ApiException, SignatureException;

    
    /**
     * Creates a new PCR white list for the MLE specified. When the MLE is created, PCR white list entries are also added. 
     * So, if the white list values need to be updated, then the user need to call (@link updatePCRWhiteList} function. <br>
     * Creation of PCR white lists could be automated using the APIs {@link MtWilson#configureWhiteList(com.intel.mtwilson.datatypes.TxtHostRecord)} or
     * {@link MtWilson#configureWhiteList(com.intel.mtwilson.datatypes.HostConfigData)}
     * <p>
     * <i><u>Roles needed:</u></i>Whitelist
     * <p>
     * <i><u>Input content type:</u></i>Application/JSON
     * <p>
     * <i><u>Output content type:</u></i>Text/Plain
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: POST</i><br>
     * https://10.1.71.95:8443/WLMService/resources/mles/whitelist/pcr<br>
     * <i>Sample Input.</i><br>
     * {"PCRName":"0","PCRDigest":"ACE7AB9D3582097C9BC739C9311D60B5B5F5603A","MLEName":"Intel_BIOS_MLE","MLEVersion":"T060","OSName":"","OSVersion":"","OEMName":"INTEL"}
     * <br>
     * Note: For BIOS MLE PCR updates, the OSName and OSVersion fields can be either ignored or set to empty string. 
     * For VMM MLEs, the OEMName can be ignored or set to empty string.<br>
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * true<br>
     * If the PCR the user is trying to create already existing, an appropriate error would be returned.<br>
     * {"error_code":"WS_PCR_WHITELIST_ALREADY_EXISTS","error_message":"White list for the PCR '0' is already configured in the system."}
     * <p>
     * <i><u>Sample Java API Call:</u></i><br>
     * boolean addPCRWhiteList = apiClientObj.addPCRWhiteList(new PCRWhiteList("0", "BBC83B19E793491B1C6EA0FD8B46CD9F32E592FC", "Intel_BIOS_MLE", "T060", "", "", "INTEL"));
     * <p>
     *
     * @param pcrObj {@link PCRWhiteList} object specifying the PCR and the MLE for which the PCR has to be created. 
     * For creating BIOS PCR whitelists user has to specify the PCRName [Valid PCRNames include 0, 1, 2, 3, 4 & 5], PCRDigest, 
     * MLEName, MLEVersion and OEMName. The OsName and OsVersion should be set to empty strings. 
     * For creating VMM PCR whitelists user has to specify the PCRName [Valid PCRNames include 17, 18, 19 & 20. 
     * For Hypervisors supporting module attestation (VMware ESXi, KVM & Xen), PCR 19 should be set to empty string.
     * The white list values that will get extended to PCR 19 should be configured using {@link addModuleWhiteList}.
     * For Citrix XenServer, PCR 19 will have an actual white list value], PCRDigest, MLEName, MLEVersion, OSName and
     * OSVersion. The OEMName should be set to empty string.
     * @return boolean value indicating whether the request was executed successfully or not.
     * @throws IOException
     * @throws ApiException If there are any errors during the execution this exception would be returned to the caller. 
     * The caller can use the getErrorCode() and getMessage() functions to retrieve the exception details.
     * @throws SignatureException
     * @since MTW 1.0 Enterprise/1.2 Opensource
     */
    boolean addPCRWhiteList(PCRWhiteList pcrObj) throws IOException, ApiException, SignatureException;
    
    /**
     * Updates the white list of the specified PCR for the MLE.  <br>
     * Updates to the PCR white lists could be automated using the APIs {@link MtWilson#configureWhiteList(com.intel.mtwilson.datatypes.TxtHostRecord)} or
     * {@link MtWilson#configureWhiteList(com.intel.mtwilson.datatypes.HostConfigData)}
     * <p>
     * <i><u>Roles needed:</u></i>Whitelist
     * <p>
     * <i><u>Input content type:</u></i>Application/JSON
     * <p>
     * <i><u>Output content type:</u></i>Text/Plain
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: PUT</i><br>
     * https://10.1.71.95:8443/WLMService/resources/mles/whitelist/pcr<br>
     * <i>Sample Input.</i><br>
     * {"PCRName":"0","PCRDigest":"CCAAAB9D3582097C9BC739C9311D60B5B5F5603A","MLEName":"Intel_BIOS_MLE","MLEVersion":"T060","OSName":"","OSVersion":"","OEMName":"INTEL"}
     * <br>
     * Note: For BIOS MLE PCR updates, the OSName and OSVersion fields can be either ignored or set to empty string. 
     * For VMM MLEs, the OEMName can be ignored or set to empty string.<br>
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * true<br>
     * <p>
     * <i><u>Sample Java API Call:</u></i><br>
     * boolean addPCRWhiteList = apiClientObj.updatePCRWhiteList(new PCRWhiteList("0", "AAFF3B19E793491B1C6EA0FD8B46CD9F32E592FC", "Intel_BIOS_MLE", "T060", "", "", "INTEL"));
     * <p>
     *
     * @param pcrObj {@link PCRWhiteList} object specifying the PCR and the MLE for which the PCR has to be updated. 
     * For updating BIOS PCR whitelists user has to specify the PCRName [Valid PCRNames include 0, 1, 2, 3, 4 & 5], PCRDigest, 
     * MLEName, MLEVersion and OEMName. The OsName and OsVersion should be set to empty strings. 
     * For updating VMM PCR whitelists user has to specify the PCRName [Valid PCRNames include 17, 18, 19 & 20. 
     * For Hypervisors supporting module attestation (VMware ESXi, KVM & Xen), PCR 19 should be set to empty string. 
     * The white list values that will get extended to PCR 19 should be updated using {@link updateModuleWhiteList}.
     * For Citrix XenServer, PCR 19 will have an actual white list value], PCRDigest, MLEName, MLEVersion, OSName and
     * OSVersion. The OEMName should be set to empty string.
     * @return boolean value indicating whether the request was executed successfully or not.
     * @throws IOException
     * @throws ApiException If there are any errors during the execution this exception would be returned to the caller. 
     * The caller can use the getErrorCode() and getMessage() functions to retrieve the exception details.
     * @throws SignatureException
     * @since MTW 1.0 Enterprise/1.2 Opensource
     */    
    boolean updatePCRWhiteList(PCRWhiteList pcrObj) throws IOException, ApiException, SignatureException;
    
    /**
     * Deletes the white list of the specified PCR for the MLE.  <br>
     * <p>
     * <i><u>Roles needed:</u></i>Whitelist
     * <p>
     * <i><u>Output content type:</u></i>Text/Plain
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: DELETE</i><br>
     * https://10.1.71.95:8443/WLMService/resources/mles/whitelist/pcr?pcrName=0&mleName=Intel_BIOS_MLE&mleVersion=T060&osName=&osVersion=&oemName=INTEL<br>
     * <br>
     * Note: For BIOS MLE PCR updates, the OSName and OSVersion fields can be either ignored or set to empty string. 
     * For VMM MLEs, the OEMName can be ignored or set to empty string.<br>
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * true<br>
     * <p>
     * <i><u>Sample Java API Call:</u></i><br>
     * boolean deletePCRWhiteList = apiClientObj.deletePCRWhiteList((new PCRWhiteList("0", "", "Intel_BIOS_MLE", "T060", "", "", "INTEL")));
     * <p>
     *
     * @param pcrObj {@link PCRWhiteList} object specifying the PCR and the MLE for which the PCR has to be deleted. 
     * For deleting BIOS PCR whitelists user has to specify the PCRName [Valid PCRNames include 0, 1, 2, 3, 4 & 5],  
     * MLEName, MLEVersion and OEMName. The OsName and OsVersion should be set to empty strings.  The
     * PCRDigest can be specified or set to empty string.
     * For creating VMM PCR whitelists user has to specify the PCRName [Valid PCRNames include 17, 18, 19 & 20. 
     * For Hypervisors supporting module attestation (VMware ESXi, KVM & Xen), PCR 19 should be set to empty string. 
     * The white list values that will get extended to PCR 19 should be updated using {@link updateModuleWhiteList}.
     * For Citrix XenServer, PCR 19 will have an actual white list value], MLEName, MLEVersion, OSName and
     * OSVersion. The OEMName should be set to empty string. The PCRDigest can be specified or set to empty string.
     * @return boolean value indicating whether the request was executed successfully or not.
     * @throws IOException
     * @throws ApiException If there are any errors during the execution this exception would be returned to the caller. 
     * The caller can use the getErrorCode() and getMessage() functions to retrieve the exception details.
     * @throws SignatureException
     * @since MTW 1.0 Enterprise/1.2 Opensource
     */    
    boolean deletePCRWhiteList(PCRWhiteList pcrObj) throws IOException, ApiException, SignatureException;
    
    /**
     * Creates a new module white list for the MLE specified. Currently VMware ESXi and OpenSource Xen/KVM support
     * module based attestation. When the MLE is created, for hypervisors supporting MODULE
     * based attestation, PCR 19 would be set to empty. Using this API all the modules that get extended to PCR 19 should 
     * be configured. Since Module based attestation is supported only for PCR 19, it is not applicable for BIOS type MLEs. <br>
     * Creation of Module white lists could be automated using the APIs {@link MtWilson#configureWhiteList(com.intel.mtwilson.datatypes.TxtHostRecord)} or
     * {@link MtWilson#configureWhiteList(com.intel.mtwilson.datatypes.HostConfigData)}
     * <p>
     * <i><u>Roles needed:</u></i>Whitelist
     * <p>
     * <i><u>Input content type:</u></i>Application/JSON
     * <p>
     * <i><u>Output content type:</u></i>Text/Plain
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: POST</i><br>
     * https://10.1.71.95:8443/WLMService/resources/mles/whitelist/module<br>
     * <i>Sample Input.</i><br>
     * {"ComponentName":"esx-xserver-5.1.0-0.0.799733","DigestValue":"CBE879E9D9F4473E9F201A884450F51BC80B0980","EventName":"Vim25Api.HostTpmSoftwareComponentEventDetails",
     * "ExtendedToPCR":"19","PackageName":"esx-xserver","PackageVendor":"VMware","PackageVersion":"5.1.0-0.0.799733","UseHostSpecificDigest":false,"Description":"",
     * "MLEName":"Intel_VMware_ESXi","MLEVersion":"5.1.0-799733","OSName":"VMware_ESXi","OSVersion":"5.1.0","OEMName":""}<br>
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * true<br>
     * If the Module the user is trying to create already exists, an appropriate error would be returned.<br>
     * {"error_code":"WS_MODULE_WHITELIST_ALREADY_EXISTS","error_message":"White list for the module 'esx-xserver-5.1.0-0.0.799733' is already configured in the system.."}
     * <p>
     * <i><u>Sample Java API Call:</u></i><br>
            ModuleWhiteList moduleObj = new ModuleWhiteList("esx-xserver-5.1.0-0.0.799733", "CBE879E9D9F4473E9F201A884450F51BC80B0980", "Vim25Api.HostTpmSoftwareComponentEventDetails", 
                    "19", "esx-xserver", "VMware", "5.1.0-0.0.799733", Boolean.FALSE, "", "Intel_VMware_ESXi", "5.1.0-799733", "VMware_ESXi", "5.1.0", ""); <br>
            apiClientObj.addModuleWhiteList(moduleObj);
     * <p>
     *
     * @param moduleObj {@link ModuleWhiteList} object specifying the Module details and the MLE for which it has to be associated.
     * For creating Module whitelists user has to specify the MLEName, MLEVersion, OSName, OSVersion, ComponentName, DigestValue,
     * EventName, ExtendedToPCR & UseHostSpecificDigest have to be specified. The PackageName, PackageVendor, PackageVersion, Description are options.
     * Since module whitelists are not applicable for BIOS MLEs, OEMName has to be set to empty string. The UseHostSpecificDigest flag has
     * to be set only for modules that vary across hosts (each host will have a unique value).
     * @return boolean value indicating whether the request was executed successfully or not.
     * @throws IOException
     * @throws ApiException If there are any errors during the execution this exception would be returned to the caller. 
     * The caller can use the getErrorCode() and getMessage() functions to retrieve the exception details.
     * @throws SignatureException
     * @since MTW 1.0 Enterprise
     */    
    boolean addModuleWhiteList(ModuleWhiteList moduleObj) throws IOException, ApiException, SignatureException;
    
    /**
     * Updates the module white list for the MLE specified. Only digest value and description fields are allowed to be updated. <br>
     * Updates to the module white lists could be automated using the APIs {@link MtWilson#configureWhiteList(com.intel.mtwilson.datatypes.TxtHostRecord)} or
     * {@link MtWilson#configureWhiteList(com.intel.mtwilson.datatypes.HostConfigData)}
     * <p>
     * <i><u>Roles needed:</u></i>Whitelist
     * <p>
     * <i><u>Input content type:</u></i>Application/JSON
     * <p>
     * <i><u>Output content type:</u></i>Text/Plain
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: PUT</i><br>
     * https://10.1.71.95:8443/WLMService/resources/mles/whitelist/module<br>
     * <i>Sample Input.</i><br>
     * {"ComponentName":"esx-xserver-5.1.0-0.0.799733","DigestValue":"AAAA79E9D9F4473E9F201A884450F51BC80B0980","EventName":"Vim25Api.HostTpmSoftwareComponentEventDetails", 
     * "MLEName":"Intel_Thurley_VMware_ESXi","MLEVersion":"5.1.0-799733","OSName":"VMware_ESXi","OSVersion":"5.1.0","OEMName":""}<br>
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * true<br>
     * If the Module the user is trying to update does not exist, an appropriate error would be returned.<br>
     * {"error_code":"WS_MODULE_WHITELIST_DOES_NOT_EXIST","error_message":"White list for the module 'esx-xserver-5.1.0-0.0.799733' is not configured in the system.."}
     * <p>
     * <i><u>Sample Java API Call:</u></i><br>
            ModuleWhiteList moduleUpdateObj = new ModuleWhiteList("esx-xserver-5.1.0-0.0.799733", "DDEE79E9D9F4473E9F201A884450F51BC80B0980", "Vim25Api.HostTpmSoftwareComponentEventDetails", 
                    "", "", "", "", Boolean.FALSE, "", "Intel_Thurley_VMware_ESXi", "5.1.0-799733", "VMware_ESXi", "5.1.0", "");            
            apiClientObj.updateModuleWhiteList(moduleUpdateObj);
     * <p>
     *
     * @param moduleObj {@link ModuleWhiteList} object specifying the Module details and the MLE to which it is associated.
     * For updating Module whitelists user has to specify the MLEName, MLEVersion, OSName, OSVersion to uniquely identify the 
     * MLE and ComponentName, DigestValue & EventName to uniquely identify the Module Whitelist to be updated. 
     * Since module whitelists are not applicable for BIOS MLEs, OEMName has to be set to empty string. 
     * @return boolean value indicating whether the request was executed successfully or not.
     * @throws IOException
     * @throws ApiException If there are any errors during the execution this exception would be returned to the caller. 
     * The caller can use the getErrorCode() and getMessage() functions to retrieve the exception details.
     * @throws SignatureException
     * @since MTW 1.0 Enterprise
     */    
    boolean updateModuleWhiteList(ModuleWhiteList moduleObj) throws IOException, ApiException, SignatureException;
    
    /**
     * Deletes the module white list for the MLE specified. 
     * <p>
     * <i><u>Roles needed:</u></i>Whitelist
     * <p>
     * <i><u>Output content type:</u></i>Text/Plain
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: DELETE</i><br>
     * https://10.1.71.95:8443/WLMService/resources/mles/whitelist/module?ComponentName=esx-xserver-5.1.0-0.0.799733&EventName=Vim25Api.HostTpmSoftwareComponentEventDetails&MLEName=Intel_Thurley_VMware_ESXi&MLEVersion=5.1.0-799733&OSName=VMware_ESXi&OSVersion=5.1.0&OEMName=<br>
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * true<br>
     * If the Module the user is trying to delete does not exist, an appropriate error would be returned.<br>
     * {"error_code":"WS_MODULE_WHITELIST_DOES_NOT_EXIST","error_message":"White list for the module 'esx-xserver-5.1.0-0.0.799733' is not configured in the system.."}
     * <p>
     * <i><u>Sample Java API Call:</u></i><br>
     *             ModuleWhiteList moduleUpdateObj = new ModuleWhiteList("esx-xserver-5.1.0-0.0.799733", "", "Vim25Api.HostTpmSoftwareComponentEventDetails", 
                    "", "", "", "", Boolean.FALSE, "", "Intel_Thurley_VMware_ESXi", "5.1.0-799733", "VMware_ESXi", "5.1.0", ""); <br>           
                    boolean deleteModuleWhiteList = apiClientObj.deleteModuleWhiteList(moduleUpdateObj);
     * <p>
     *
     * @param moduleObj {@link ModuleWhiteList} object specifying the Module details and the MLE to which it is associated.
     * For deleting Module whitelists user has to specify the MLEName, MLEVersion, OSName, OSVersion to uniquely identify the 
     * MLE and ComponentName  & EventName to uniquely identify the Module Whitelist to be deleted. 
     * Since module whitelists are not applicable for BIOS MLEs, OEMName has to be set to empty string. 
     * @return boolean value indicating whether the request was executed successfully or not.
     * @throws IOException
     * @throws ApiException If there are any errors during the execution this exception would be returned to the caller. 
     * The caller can use the getErrorCode() and getMessage() functions to retrieve the exception details.
     * @throws SignatureException
     * @since MTW 1.0 Enterprise
     */    
    boolean deleteModuleWhiteList(ModuleWhiteList moduleObj) throws IOException, ApiException, SignatureException;    
    
    /**
     * Retrieves the list of all the module white lists associated with the MLEs
     * <p>
     * <i><u>Roles needed:</u></i>Whitelist
     * <p>
     * <i><u>Output content type:</u></i>Application/JSON
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: GET</i><br>
     * https://10.1.71.95:8443/WLMService/resources/mles/whitelist/module?mleName=Intel_Thurley_VMware_ESXi&mleVersion=5.1.0-799733&osName=VMware_ESXi&osVersion=5.1.0&oemName=<br>
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * [{"ComponentName":"commandLine.","DigestValue":"2213253CBDF7A0D8B3709E971E6BEB7EA6A81A92","EventName":"Vim25Api.HostTpmCommandEventDetails","ExtendedToPCR":"19","PackageName":"","PackageVendor":"","PackageVersion":"","UseHostSpecificDigest":true,"Description":"","MLEName":"Intel_Thurley_VMware_ESXi","MLEVersion":"5.1.0-799733","OSName":"VMware_ESXi","OSVersion":"5.1.0","OEMName":""},
     * {"ComponentName":"bootOptions.useropts.gz","DigestValue":"DA39A3EE5E6B4B0D3255BFEF95601890AFD80709","EventName":"Vim25Api.HostTpmOptionEventDetails","ExtendedToPCR":"19","PackageName":"","PackageVendor":"","PackageVersion":"","UseHostSpecificDigest":false,"Description":"","MLEName":"Intel_Thurley_VMware_ESXi","MLEVersion":"5.1.0-799733","OSName":"VMware_ESXi","OSVersion":"5.1.0","OEMName":""}]<br>
     * <p>
     * <i><u>Sample Java API Call:</u></i><br>
                List<ModuleWhiteList> listModuleWhiteListForMLE = apiClientObj.listModuleWhiteListForMLE("Intel_Thurley_VMware_ESXi", "5.1.0-799733", "VMware_ESXi", "5.1.0", "");
     * <p>
     *
     * @param mleName Name of the MLE for which the white lists need to be retrieved.
     * @param mleVersion Version of the MLE
     * @param osName Name of the OS associated with the MLE
     * @param osVersion Version of the OS associated with the MLE
     * @param oemName OEM name, which should be empty since Module white lists are always associated with VMM MLEs.
     * @return List of {@link ModuleWhiteList} objects.
     * @throws IOException
     * @throws ApiException If there are any errors during the execution this exception would be returned to the caller. 
     * The caller can use the getErrorCode() and getMessage() functions to retrieve the exception details.
     * @throws SignatureException
     * @since MTW 1.0 Enterprise
     */    
    List<ModuleWhiteList> listModuleWhiteListForMLE(String mleName, String mleVersion, 
            String osName, String osVersion, String oemName) throws IOException, ApiException, SignatureException;
    
    /**
     * Creates a mapping between the MLE and the name of the host from which the white lists associated with the MLE were retrieved.
     * This information is mainly for auditing and tracking purpose.
     * <p>
     * <i><u>Roles needed:</u></i>Whitelist
     * <p>
     * <i><u>Input content type:</u></i>Application/JSON
     * <p>
     * <i><u>Output content type:</u></i>Text/Plain
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: POST</i><br>
     * https://192.168.1.101:8181/WLMService/resources/mles/source<br>
     * <p>
     * <i>Sample Input.</i><br>
     * {"hostName":"192.168.1.202","mleData":{"Name":"Intel_BIOS_MLE","Version":"T060","MLE_Type":"BIOS","OsName":"","OsVersion":"","OemName":"INTEL"}} <br>
     * If a host is already mapped to the MLE, then an appropriate error would be returned back to the caller.<br>
     * {"error_code":"WS_MLE_SOURCE_MAPPING_ALREADY_EXISTS","error_message":"White list host mapping already exists for the MLE 'Intel_BIOS_MLE'."}
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * true<br>
     * <p>
     * <i><u>Sample Java API Call:</u></i><br>
            MleData mleBIOSObj = new MleData("Intel_BIOS_MLE", "T060", MleData.MleType.BIOS, MleData.AttestationType.PCR, null, "", "", "", "INTEL");
            MleSource mleSourceObj = new MleSource();
            mleSourceObj.setHostName("192.168.1.202");
            mleSourceObj.setMleData(mleBIOSObj);
            boolean addMleSource = apiClientObj.addMleSource(mleSourceObj);
     * <p>
     * 
     * @param mleSourceObj {@link MleSource} object specifying the name of the host and the details of the MLE object for
     * which the mapping should be added. For the MLE details user has to specify the Name, Version, Attestation_Type & MLE_Type.
     * For BIOS MLE type, OemName has to be specified and the OsName and OsVersion should be set to empty strings. For VMM
     * MLE type, the OemName has to be set to empty string and OsName/OsVersion have to be specified. 
     * @return boolean value indicating whether the request was executed successfully or not. 
     * @throws IOException
     * @throws ApiException If there are any errors during the execution this exception would be returned to the caller.
     * The caller can use the getErrorCode() and getMessage() functions to retrieve the exception details.
     * @throws SignatureException 
     * @since MTW 1.1 Enterprise/1.2 Opensource
     */    
    boolean addMleSource(MleSource mleSourceObj) throws IOException, ApiException, SignatureException;
    
    /**
     * Updates the host name  from which the white lists were retrieved for the specified MLE.
     * This is mainly for auditing purpose. This function need to be executed when the white lists have been updated from a 
     * different good known host.
     * <p>
     * <i><u>Roles needed:</u></i>Whitelist
     * <p>
     * <i><u>Input content type:</u></i>Application/JSON
     * <p>
     * <i><u>Output content type:</u></i>Text/Plain
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: PUT</i><br>
     * https://192.168.1.101:8181/WLMService/resources/mles/source<br>
     * <p>
     * <i>Sample Input.</i><br>
     * {"hostName":"192.168.1.203","mleData":{"Name":"Intel_BIOS_MLE","Version":"T060","MLE_Type":"BIOS","OsName":"","OsVersion":"","OemName":"INTEL"}} <br>
     * If a host is not mapped to the MLE, then an appropriate error would be returned back to the caller.<br>
     * {"error_code":"WS_MLE_SOURCE_MAPPING_DOES_NOT_EXIST","error_message":"White list host mapping does not exist for the MLE 'Intel_BIOS_MLE'."}
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * true<br>
     * <p>
     * <i><u>Sample Java API Call:</u></i><br>
            MleData mleBIOSObj = new MleData("Intel_BIOS_MLE", "T060", MleData.MleType.BIOS, MleData.AttestationType.PCR, null, "", "", "", "INTEL");
            MleSource mleSourceObj = new MleSource();
            mleSourceObj.setHostName("192.168.1.203");
            mleSourceObj.setMleData(mleBIOSObj);
            boolean updateMleSource = apiClientObj.updateMleSource(mleSourceObj);
     * <p>
     * 
     * @param mleSourceObj {@link MleSource} object specifying the name of the host and the details of the MLE object for
     * which the mapping should be updated. For the MLE details user has to specify the Name, Version, Attestation_Type & MLE_Type.
     * For BIOS MLE type, OemName has to be specified and the OsName and OsVersion should be set to empty strings. For VMM
     * MLE type, the OemName has to be set to empty string and OsName/OsVersion have to be specified. 
     * @return boolean value indicating whether the request was executed successfully or not. 
     * @throws IOException
     * @throws ApiException If there are any errors during the execution this exception would be returned to the caller.
     * The caller can use the getErrorCode() and getMessage() functions to retrieve the exception details.
     * @throws SignatureException 
     * @since MTW 1.1 Enterprise/1.2 Opensource
     */    
    boolean updateMleSource(MleSource mleSourceObj) throws IOException, ApiException, SignatureException;
    
    /**
     * Deletes the existing mapping between the MLE and the host from which the white lists for the MLE were retrieved.
     * <p>
     * <i><u>Roles needed:</u></i>Whitelist
     * <p>
     * <i><u>Input content type:</u></i>Application/JSON
     * <p>
     * <i><u>Output content type:</u></i>Text/Plain
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: DELETE</i><br>
     * https://192.168.1.101:8181/WLMService/resources/mles/source?mleName=Intel_BIOS_MLE&mleVersion=T060&osName=&osVersion=&oemName=INTEL<br>
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * true<br>
     * <p>
     * <i><u>Sample Java API Call:</u></i><br>
            MleData mleBIOSObj = new MleData("Intel_BIOS_MLE", "T060", MleData.MleType.BIOS, MleData.AttestationType.PCR, null, "", "", "", "INTEL");
            boolean deleteMleSource = apiClientObj.deleteMleSource(mleBIOSObj);
     * <p>
     * 
     * @param mleDataObj {@link MleData} object specifying the details of the MLE object for which the mapping should be deleted. 
     * For the MLE details user has to specify the Name, Version, Attestation_Type & MLE_Type.
     * For BIOS MLE type, OemName has to be specified and the OsName and OsVersion should be set to empty strings. For VMM
     * MLE type, the OemName has to be set to empty string and OsName/OsVersion have to be specified. 
     * @return boolean value indicating whether the request was executed successfully or not. 
     * @throws IOException
     * @throws ApiException If there are any errors during the execution this exception would be returned to the caller.
     * The caller can use the getErrorCode() and getMessage() functions to retrieve the exception details.
     * @throws SignatureException 
     * @since MTW 1.1 Enterprise/1.2 Opensource
     */    
    boolean deleteMleSource(MleData mleDataObj) throws IOException, ApiException, SignatureException;
    
    /**
     * Retrieves the host name from which the specified MLE's white list was configured. If in case the white list of the
     * MLE was configured manually then "Manually configured whitelist" string would be returned back to the caller.
     * <p>
     * <i><u>Roles needed:</u></i>Whitelist
     * <p>
     * <i><u>Output content type:</u></i>Text/Plain
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: GET</i><br>
     * https://192.168.1.101:8181/WLMService/resources/mles/source?mleName=Intel_BIOS_MLE&mleVersion=T060&osName=&osVersion=&oemName=INTEL<br>
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * 192.168.1.102<br>
     * <p>
     * <i><u>Sample Java API Call:</u></i><br>
            MleData mleBIOSObj = new MleData("Intel_BIOS_MLE", "T060", MleData.MleType.BIOS, MleData.AttestationType.PCR, null, "", "", "", "INTEL");
            String mleSource = apiClientObj.getMleSource(mleBIOSObj);
     * <p>
     * 
     * @param mleDataObj {@link MleData} object specifying the details of the MLE object for which the mapping should be retrieved. 
     * For the MLE details user has to specify the Name, Version, Attestation_Type & MLE_Type.
     * For BIOS MLE type, OemName has to be specified and the OsName and OsVersion should be set to empty strings. For VMM
     * MLE type, the OemName has to be set to empty string and OsName/OsVersion have to be specified. 
     * @return boolean value indicating whether the request was executed successfully or not. 
     * @throws IOException
     * @throws ApiException If there are any errors during the execution this exception would be returned to the caller.
     * The caller can use the getErrorCode() and getMessage() functions to retrieve the exception details.
     * @throws SignatureException 
     * @since MTW 1.1 Enterprise/1.2 Opensource
     */        
    String getMleSource(MleData mleDataObj) throws IOException, ApiException, SignatureException;    
    
    // MANAGEMENT SERVICE
    
 /**
     * Provides the list of all the existing users (API clients) having access to Mt.Wilson. The search criteria can be any one of the 
     * following:  enabledEqualTo, expiresAfter, expiresBefore, fingerprintEqualTo, issuerEqualTo, nameContains, 
     * nameEqualTo, serialNumberEqualTo & statusEqualTo. If no search criteria is specified, then all the users would be 
     * returned back to the caller.
     * <p>
     *<i><u>Roles needed:</u></i>Security
     * <p>
     * <i><u>Output content type:</u></i>Application/JSON
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: GET</i><br>
     * https://192.168.1.101:8181/ManagementService/resources/apiclient/search?statusEqualTo=APPROVED<br>
     *  https://192.168.1.101:8181/ManagementService/resources/apiclient/search?
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * [{"name":"CN=ManagementServiceAutomation,OU=Mt Wilson,O=Trusted Data Center,C=US","certificate":"MIIDSjCCA.....",
     * "fingerprint":"EiI/zmVY0vUr5Q6RdalS55mNcyZD7Pt5oFDRUmUOPkQ=","issuer":"CN=ManagementServiceAutomation, 
     * OU=Mt Wilson, O=Trusted Data Center, C=US","serialNumber":1093506352,"expires":1683687684000,"enabled":true,
     * "status":"Approved","roles":["Attestation","Security","Whitelist"],"comment":null}]
     * <p>
     *  <i><u>Sample Java API Call to retrieve the list of approved users:</u></i><br>
     * ApiClientSearchCriteria apiSearchObj = new ApiClientSearchCriteria();<br>
     * apiSearchObj.statusEqualTo = ApiClientStatus.APPROVED.toString(); // Alternatively apiSearchObj.statusEqualTo = "Approved";<br>
     * List<ApiClientInfo> searchApiClients = apiClientObj.searchApiClients(apiSearchObj);<br>
     * // To retrieve all the users<br>
     * ApiClientSearchCriteria apiSearchObj = new ApiClientSearchCriteria();<br>
     * List<ApiClientInfo> searchApiClients = api.searchApiClients(apiSearchObj);<br>
     * <p>
     * @param criteria  {@link ApiClientSearchCriteria} specifies the search criteria value to be used for finding the 
     * matching users.
     * @return {@link ApiClientInfo} List of all the existing users having access to Mt.Wilson matching the search criteria.
     * @throws IOException
     * @throws ApiException If there are any errors during the execution this exception would be returned to the caller. 
     * The caller can use the getErrorCode() and getMessage() functions to retrieve the exception details.
     * @throws SignatureException 
     * @since MTW 1.0 Enterprise
     */            
    List<ApiClientInfo> searchApiClients(ApiClientSearchCriteria criteria) throws IOException, ApiException, SignatureException;
    
    /**
     * Reserved for future use.  This API is currently not supported.
     * @param criteria
     * @return
     * @throws IOException
     * @throws ApiException
     * @throws SignatureException 
     */
    List<AuditLogEntry> searchAuditLog(AuditLogSearchCriteria criteria)  throws IOException, ApiException, SignatureException;

 /**
     * Provides the list of all the  users (API clients) whose access request has not been approved yet. 
     * <p>
     *<i><u>Roles needed:</u></i>Security
     * <p>
     * <i><u>Output content type:</u></i>Application/JSON
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: GET</i><br>
     * https://192.168.1.101:8181/ManagementService/resources/apiclient/search?statusEqualTo=Pending<br>
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * [{"name":"CN=testuser,OU=Mt Wilson,O=Trusted Data Center,C=US","certificate":"MIIDJ..."UY=",
     * ,"fingerprint":"e5/MoypC6GxC1Dh+nvKwOm7/Fj/O2av4MmZtx2MemUY=","issuer":"CN=testuser, OU=Mt Wilson, 
     * O=Trusted Data Center, C=US","serialNumber":1072669869, "expires":1684143856000,"enabled":false,
     * "status":"PENDING","roles":["Attestation","Whitelist"], "comment":null}]
     * <p>
     *  <i><u>Sample Java API Call:</u></i><br>
     * List<ApiClientInfo> searchApiClients = apiClientObj.listPendingAccessRequests();<br>
     * <p>
     * @return {@link ApiClientInfo} List of all the users whose access request is yet to be approved. If no pending
     * request exists, then empty list would be returned back.
     * @throws IOException
     * @throws ApiException If there are any errors during the execution this exception would be returned to the caller. 
     * The caller can use the getErrorCode() and getMessage() functions to retrieve the exception details.
     * @throws SignatureException 
     * @since MTW 1.0 Enterprise
     */            
    List<ApiClientInfo> listPendingAccessRequests() throws IOException, ApiException, SignatureException;

    
 /**
     * Retrieves the details of the user (API client) matching the specified finger print. Note that the fingerprint has to be Hex 
     * encoded when calling into the REST API directly. The java API accepts the byte array.
     * <p>
     *<i><u>Roles needed:</u></i>Security
     * <p>
     * <i><u>Output content type:</u></i>Application/JSON
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: GET</i><br>
     * https://192.168.1.101:8181/ManagementService/resources/apiclient/search?fingerprintEqualTo=3efcf2652ae8211bb870af071ab1bdf8270913da8225b7728e3f086993f732ec<br>
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * [{"name":"CN=testuser,OU=Mt Wilson,O=Trusted Data Center,C=US","certificate":"MIIDJ..."UY=",
     * ,"fingerprint":"e5/MoypC6GxC1Dh+nvKwOm7/Fj/O2av4MmZtx2MemUY=","issuer":"CN=testuser, OU=Mt Wilson, 
     * O=Trusted Data Center, C=US","serialNumber":1072669869, "expires":1684143856000,"enabled":false,
     * "status":"PENDING","roles":["Attestation","Whitelist"], "comment":null}]
     * <p>
     *  <i><u>Sample Java API Call:</u></i><br>
            String fingerprintHex = "3efcf2652ae8211bb870af071ab1bdf8270913da8225b7728e3f086993f732ec";<br>
            ApiClientInfo apiClientInfo = apiClientObj.getApiClientInfo(Hex.decodeHex(fingerprintHex.toCharArray()));
     * <p>
     * @param fingerprint of the user/api client for which the details are needed. 
     * @return {@link ApiClientInfo} containing the details of the user specified.
     * @throws IOException
     * @throws ApiException If there are any errors during the execution this exception would be returned to the caller. 
     * The caller can use the getErrorCode() and getMessage() functions to retrieve the exception details.
     * @throws SignatureException 
     * @see ApiClientInfo
     * @since MTW 1.0 Enterprise
     */                
    ApiClientInfo getApiClientInfo(byte[] fingerprint) throws IOException, ApiException, SignatureException;

    /**
     * Registers a new user (API Client) with they system. Note that registering does not provide the requested access. 
     * The administrator has to approve the request. The administrator has the ability to change the roles that the user
     * has requested for. <br>
     * A helper method {@link com.intel.mtwilson.KeystoreUtil.createUserInDirectory} has been provided for the user
     * registration. This method would automatically create the required certificate, registers the user and also downloads
     * the server's SSL, SAML, Root CA & PrivacyCA certificates into the local keystore.
     * <p>
     * <i><u>Roles needed:</u></i>Security
     * <p>
     * <i><u>Input content type:</u></i>Application/JSON
     * <p>
     * <i><u>Output content type:</u></i>Text/Plain
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: POST</i><br>
     * https://192.168.1.101:8181/ManagementService/resources/apiclient/<br>
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * true<br>
     * <p>
     * <i><u>Sample Java API Call:</u></i><br>
            File directory = new File(System.getProperty("user.home", "."));
            String username = "apiuser"; // you choose a username
            String password = "password"; // you choose a password
            URL server = new URL("https://192.168.1.101:8181"); // your Mt Wilson server
            String[] roles = new String[] { "Attestation", "Whitelist", "Security" };
            KeystoreUtil.createUserInDirectory(directory, username, password, server, roles);  
     * <p>
     * 
     * @param apiClient {@link ApiClientCreateRequest} object with the certificate created using a new RSAKeypair and the roles
     * need by the user.
     * @return boolean value indicating whether the request was executed successfully or not. 
     * @throws IOException
     * @throws ApiException If there are any errors during the execution this exception would be returned to the caller.
     * The caller can use the getErrorCode() and getMessage() functions to retrieve the exception details.
     * @throws SignatureException 
     * @since MTW 1.0 Enterprise
     */    
    boolean registerApiClient(ApiClientCreateRequest apiClient) throws IOException, ApiException, SignatureException;
    
    
    /**
     * Updates the access request of  user (API Client). The same API can be used for approving/denying the access. <br>
     * <p>
     * <i><u>Roles needed:</u></i>Security
     * <p>
     * <i><u>Input content type:</u></i>Application/JSON
     * <p>
     * <i><u>Output content type:</u></i>Text/Plain
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: PUT</i><br>
     * https://192.168.1.101:8181/ManagementService/resources/apiclient/<br>
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * true<br>
     * <p>
     * <i><u>Sample Java API Call:</u></i><br>
            String fingerPrintHex = "b8c79266fecaf8ec929016b873b65689a84697d510c68d471db6598ade9972f2";<br>
            ApiClientUpdateRequest updateRequest = new ApiClientUpdateRequest();<br>
            updateRequest.fingerprint = Hex.decodeHex(fingerPrintHex.toCharArray());<br>
            updateRequest.enabled = true;<br>
            updateRequest.status = ApiClientStatus.APPROVED.toString();
            updateRequest.roles = new String[] { "Attestation", "Whitelist", "Security" };<br>
            updateRequest.comment ="Access request approved on Jun 3rd.";<br>
            apiClientObj.updateApiClient(updateRequest);
     * <p>
     * 
     * @param info {@link ApiClientUpdateRequest} object with the fingerprint of the user for whom the details have to 
     * be updated. Except for the comment field all other fields are required for the update.
     * @return boolean value indicating whether the request was executed successfully or not. 
     * @throws IOException
     * @throws ApiException If there are any errors during the execution this exception would be returned to the caller.
     * The caller can use the getErrorCode() and getMessage() functions to retrieve the exception details.
     * @throws SignatureException 
     * @since MTW 1.0 Enterprise
     */    
    boolean updateApiClient(ApiClientUpdateRequest info) throws IOException, ApiException, SignatureException;
    
 /**
     * Deletes the details of the user (API client) matching the specified finger print. Note that the fingerprint has to be Hex 
     * encoded when calling into the REST API directly. The java API accepts the byte array.
     * <p>
     *<i><u>Roles needed:</u></i>Security
     * <p>
     * <i><u>Output content type:</u></i>Text/Plain
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: DELETE</i><br>
     * https://192.168.1.101:8181/ManagementService/resources/apiclient?fingerprint=3efcf2652ae8211bb870af071ab1bdf8270913da8225b7728e3f086993f732ec<br>
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * true
     * <p>
     *  <i><u>Sample Java API Call:</u></i><br>
            String fingerprintHex = "3efcf2652ae8211bb870af071ab1bdf8270913da8225b7728e3f086993f732ec";<br>
            boolean deleteApiClient = apiClientObj.deleteApiClient(Hex.decodeHex(fingerprintHex.toCharArray()));
     * <p>
     * @param fingerprint of the user/api client which has to be deleted.
     * @return boolean value indicating whether the request was executed successfully or not. 
     * @throws IOException
     * @throws ApiException If there are any errors during the execution this exception would be returned to the caller. 
     * The caller can use the getErrorCode() and getMessage() functions to retrieve the exception details.
     * @throws SignatureException 
     * @see ApiClientInfo
     * @since MTW 1.0 Enterprise
     */                    
    boolean deleteApiClient(byte[] fingerprint) throws IOException, ApiException, SignatureException;

    
    /**
     * Provides the list of roles that the user can request access for during the registration.  
     * <p>
     * <i><u>Roles needed:</u></i>Security
     * <p>
     * <i><u>Output content type:</u></i>Application/JSON
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: GET</i><br>
     * https://192.168.1.101:8181/ManagementService/resources/apiclient/availableRoles<br>
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * ["Security","Whitelist","Attestation","Report","Audit"]<br>
     * <p>
     * <i><u>Sample Java API Call:</u></i><br>
     *             Role[] listAvailableRoles = apiClientObj.listAvailableRoles();
     * <p>
     * 
     * @return List of {@link Role} supported by the system. 
     * @throws IOException
     * @throws ApiException If there are any errors during the execution this exception would be returned to the caller.
     * The caller can use the getErrorCode() and getMessage() functions to retrieve the exception details.
     * @throws SignatureException 
     * @since MTW 1.0 Enterprise
     */    
    Role[] listAvailableRoles() throws IOException, ApiException, SignatureException;
    
    /**
     * Registers the host specified with the system. By default the host will be associated with MLEs created with OEM as the 
     * White List target option. If the host is already registered in the system, then it will be updated (associated with correct MLEs)
     * to match the current version of the BIOS and OS/Hypervisor running on the host.
     * <p>
     * <i><u>Pre-requisite:</u></i>White list has to be configured with the White List target of OEM since this function uses
     * OEM as the white list target by default. If the user has created the White List with either GLOBAL or HOST Specific option
     * then the custom host registration function has to be used..
     * <p>
     * <i><u>Roles needed:</u></i>Attestation
     * <p>
     * <i><u>Input content type:</u></i>Application/JSON
     * <p>
     * <i><u>Output content type:</u></i>Text/Plain
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: POST</i><br>
     * https://192.168.1.101:8181/ManagementService/resources/host<br>
     * <i>Sample Input</i><br>
     * Open Source Hosts: {"HostName":"192.168.1.201","AddOn_Connection_String":"intel:https://192.168.1.201:9999"} <br>
     * Citrix XenServer Hosts: {"HostName":"192.168.1.202","AddOn_Connection_String":"citrix:https://192.168.1.202:443/;root;pwd"}<br>
     * VMware Hosts: {"HostName":"192.168.1.203","AddOn_Connection_String":"vmware:https://192.168.1.222:443/sdk;Admin;password"}<br>
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * true<br>
     * <p>
     * <i><u>Sample Java API Call:</u></i><br>
            TxtHostRecord hostObj = new TxtHostRecord();<br>
            hostObj.HostName = "192.168.1.201";<br>
            hostObj.AddOn_Connection_String = "intel:https://192.168.1.201:9999";<br>
            boolean registerHost = apiClientObj.registerHost(hostObj);
     * <p>
     * 
     * @param hostObj {@link TxtHostRecord} object with the details of the host to be registered.
     * Only the HostName and AddOn_Connection_String parameters are required. The AddOn_Connection_String should
     * be in the format specified above. For VMware hosts, the AddOn_Connection_String would have the DNS or IP Address
     * of the vCenter Server.
     * @return boolean value indicating whether the request was executed successfully or not. 
     * @throws IOException
     * @throws ApiException If there are any errors during the execution this exception would be returned to the caller.
     * The caller can use the getErrorCode() and getMessage() functions to retrieve the exception details.
     * @throws SignatureException 
     * @since MTW 1.0 Enterprise
     */    
    boolean registerHost(TxtHostRecord hostObj) throws IOException, ApiException, SignatureException;
    
    /**
     * Registers the host specified with the system using the customized options. This function allows the user to specify
     * the white list that needs to be associated with the host by specifying the white list target. The system support 3 options 
     * for white list target.[GLOBAL: The white list is applicable for hosts from any OEM running the same version of the OS/Hypervisor. 
     * OEM: The  white list is applicable for the hosts from a specific OEM running the same version of the OS/Hypervisor. 
     * HOST: The white list is valid only for specific host.]. If the user does not want to specify this option and want to go with
     * the default OEM white list, the the registerHost(TxtHostRecord) could be used.
     * <p>
     * <i><u>Pre-requisite:</u></i>White list has to be configured with the White List target that the host would be using. If the
     * MLE & associated white lists do not exist, then appropriate error would be returned back to the caller.
     * <p>
     * <i><u>Roles needed:</u></i>Attestation
     * <p>
     * <i><u>Input content type:</u></i>Application/JSON
     * <p>
     * <i><u>Output content type:</u></i>Text/Plain
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: POST</i><br>
     * https://192.168.1.101:8181/ManagementService/resources/host/custom<br>
     * <i>Sample Input</i><br>
     * {"BIOS_WhiteList_Target":"BIOS_OEM","VMM_WhiteList_Target":"VMM_OEM", "TXT_Host_Record":{"HostName":"192.168.201","AddOn_Connection_String":"intel:https://192.168.1.201:9999"}}
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * true<br>
     * If the MLE and associated white lists are not configured, then appropriate error would be returned back. In the below example, MLE was configured with OEM as the white list target but
     * when registering the host used GLOBAL option for which the MLE does not exist.<br>
     * {"error_code":"MS_VMM_MLE_NOT_FOUND","error_message":"VMM MLE Xen - 11-4.1.0' is not configured in the system. Please verify if the white list is properly configured."}
     * <p>
     * <i><u>Sample Java API Call:</u></i><br>
            TxtHostRecord hostObj = new TxtHostRecord();<br>
            hostObj.HostName = "192.168.1.201";<br>
            hostObj.AddOn_Connection_String = "intel:https://192.168.1.201:9999";<br>
            HostConfigData hostConfigObj = new HostConfigData();<br>
            hostConfigObj.setBiosWLTarget(HostWhiteListTarget.BIOS_OEM);<br>
            hostConfigObj.setVmmWLTarget(HostWhiteListTarget.VMM_OEM);<br>
            hostConfigObj.setTxtHostRecord(hostObj);<br>
            boolean registerHost = apiClientObj.registerHost(hostConfigObj);
     * <p>
     * 
     * @param hostConfigObj {@link HostConfigData} object with the details of the host to be registered
     * along with the customization options.The HostName and AddOn_Connection_String parameters for the TxtHostRecord
     * object are required. The user also has to specify which of the OEM/GLOBAL/HOST options for white list target has to
     * be used.
     * @return boolean value indicating whether the request was executed successfully or not. 
     * @throws IOException
     * @throws ApiException If there are any errors during the execution this exception would be returned to the caller.
     * The caller can use the getErrorCode() and getMessage() functions to retrieve the exception details.
     * @throws SignatureException 
     * @since MTW 1.0 Enterprise
     */    
    boolean registerHost(HostConfigData hostConfigObj) throws IOException, ApiException, SignatureException;
    
    /**
     * Configures the MLEs & associated white list/good known values using the host information provided. This API communicates
     * with the host, retrieves the details of the BIOS, OS/Hypervisor installed on that host and the associated white lists. Using
     * all the information retrieved from the host, OEM, OS and MLEs (both BIOS and VMM) are configured automatically. <br>
     * For Open Source (Xen/KVM) & Citrix XenServer hosts, PCRs 0, 17 & 18 are selected by default. For VMware ESXi hosts, PCRs
     * 0, 17, 18, 19 & 20 are selected by default. The default white list target for both BIOS and VMM would be set to OEM. <br>
     * If the user wants to change any of the default selections, then custom white list API should be used.
     * <p>
     * <i><u>Note:</u></i> If the white list is already configured, then executing this function again would update the current
     * white lists in the database.
     * <p>
     * <i><u>Roles needed:</u></i>Whitelist
     * <p>
     * <i><u>Input content type:</u></i>Application/JSON
     * <p>
     * <i><u>Output content type:</u></i>Text/Plain
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: POST</i><br>
     * https://192.168.1.101:8181/ManagementService/resources/host/whitelist<br>
     * <i>Sample Input.</i><br>
     * Open Source Hosts: {"HostName":"192.168.1.201","AddOn_Connection_String":"intel:https://192.168.1.201:9999"}<br>
     * Citrix XenServer Hosts: {"HostName":"192.168.1.202","AddOn_Connection_String":"citrix:https://192.168.1.202:443/;root;pwd"}<br>
     * VMware Hosts: {"HostName":"192.168.1.203","AddOn_Connection_String":"vmware:https://192.168.1.222:443/sdk;Admin;password"}<br>
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * true<br>
     * <p>
     * <i><u>Sample Java API Call:</u></i><br>
            TxtHostRecord hostObj = new TxtHostRecord();<br>
            hostObj.HostName = "192.168.1.201";<br>
            hostObj.AddOn_Connection_String = "intel:https://192.168.1.201:9999";<br>
            boolean configureWhiteList = apiClientObj.configureWhiteList(hostObj);<br>
     * <p>
     * 
     * @param hostObj {@link TxtHostRecord} object with the details of the host to be used for white list configuration.
     * Only the HostName and AddOn_Connection_String parameters are required. The AddOn_Connection_String should
     * be in the format specified above. For VMware hosts, the AddOn_Connection_String would have the DNS or IP Address
     * of the vCenter Server.
     * @return boolean value indicating whether the request was executed successfully or not. 
     * @throws IOException
     * @throws ApiException If there are any errors during the execution this exception would be returned to the caller.
     * The caller can use the getErrorCode() and getMessage() functions to retrieve the exception details.
     * @throws SignatureException 
     * @since MTW 1.0 Enterprise
     */    
    boolean configureWhiteList(TxtHostRecord hostObj) throws IOException, ApiException, SignatureException;
    
    /**
     * Configures the MLEs & associated white list/good known values using the host information and the customization options
     * provided by the caller. This API communicates with the host, retrieves the details of the BIOS, OS/Hypervisor installed 
     * on that host and the associated white lists. Using all the information retrieved from the host and the customization options
     * provided by the caller, OEM, OS and MLEs (both BIOS and VMM) are configured automatically. <br>
     * Here the caller has the option to configure either or both BIOS and VMM MLEs with specific PCRs that the user is
     * interested in. Also, the white list target can be customized by choosing one of the 3 options supported [GLOBAL: 
     * The white list is applicable for hosts from any OEM running the same version of the OS/Hypervisor. OEM: The 
     * white list is applicable for the hosts from a specific OEM. HOST: The white list is valid only for specific host.] <br>
     * The user also gets to choose whether to register the host automatically after the configuration of the white list. Note
     * that the host can be registered successfully if both the BIOS and VMM MLEs have been configured.
     * <p>
     * <i><u>Note:</u></i> If the white list is already configured, then executing this function again would update the current
     * white lists in the database if the Overwrite_Whitelist flag is set to true. Otherwise, if the MLE already exists and the
     * white lists matches, new MLE will not be created. If in case the white list does not match (because of new modules, 
     * new tBoot version etc), then a new MLE will be created with a numeric extension (_001, 002).
     * <p>
     * <i><u>Roles needed:</u></i>Whitelist
     * <p>
     * <i><u>Input content type:</u></i>Application/JSON
     * <p>
     * <i><u>Output content type:</u></i>Text/Plain
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: POST</i><br>
     * https://192.168.1.101:8181/ManagementService/resources/host/whitelist/custom<br>
     * <i>Sample Input.</i><br>
     * {"Add_BIOS_WhiteList":true,"Add_VMM_WhiteList":true,"BIOS_WhiteList_Target":"BIOS_OEM","VMM_WhiteList_Target":"VMM_OEM",
     * "BIOS_PCRS":"0,17","VMM_PCRS":"18,19","Register_Host":true,"Overwrite_Whitelist":false,"TXT_Host_Record":{"HostName":"192.168.1.201","AddOn_Connection_String":"intel:https://192.168.1.201:9999"}}
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * true<br>
     * <p>
     * <i><u>Sample Java API Call:</u></i><br>
            TxtHostRecord hostObj = new TxtHostRecord();<br>
            hostObj.HostName = "192.168.1.201";<br>
            hostObj.AddOn_Connection_String = "intel:https://192.168.1.201:9999";<br>
            HostConfigData hostConfigObj = new HostConfigData();<br>
            hostConfigObj.setBiosWhiteList(true);<br>
            hostConfigObj.setVmmWhiteList(true);<br>
            hostConfigObj.setBiosPCRs("0");<br>
            hostConfigObj.setVmmPCRs("17,18");<br>
            hostConfigObj.setBiosWLTarget(HostWhiteListTarget.BIOS_OEM);<br>
            hostConfigObj.setVmmWLTarget(HostWhiteListTarget.VMM_OEM);<br>
            hostConfigObj.setRegisterHost(true);<br>
            hostConfigObj.setOverWriteWhiteList(false);<br>
            hostConfigObj.setTxtHostRecord(hostObj);<br>
            boolean configureWhiteList = apiClientObj.configureWhiteList(hostConfigObj);<br>
     * <p>
     * 
     * @param hostConfigObj {@link HostConfigData} object with the details of the host to be used for white list configuration
     * along with the customization options.The HostName and AddOn_Connection_String parameters for the TxtHostRecord
     * object are required. If the user choose to configure BIOS or VMM MLE, the the corresponding PCRs and the WhiteList
     * target also have to be configured. Registering the host after the white list configuration is optional.
     * @return boolean value indicating whether the request was executed successfully or not. 
     * @throws IOException
     * @throws ApiException If there are any errors during the execution this exception would be returned to the caller.
     * The caller can use the getErrorCode() and getMessage() functions to retrieve the exception details.
     * @throws SignatureException 
     * @since MTW 1.0 Enterprise
     */    
    boolean configureWhiteList(HostConfigData hostConfigObj) throws IOException, ApiException, SignatureException;

    /**
     * Retrieves server's SAML certificate and any root certificates if available. This would be used to verify the signed
     * SAML assertion sent by the system. 
     * <p>
     * <i><u>Roles needed:</u></i>No roles needed.
     * <p>
     * <i><u>Output content type:</u></i>Text/Plain
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: GET</i><br>
     * https://192.168.1.101:8181/ManagementService/resources/ca/certificate/saml/current <br>
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * -----BEGIN CERTIFICATE-----
     * MIIDVDCCAjygAwIBAgIEUZyfaTANBgkqhkiG9w0BAQUFA...........EwJV
     * -----END CERTIFICATE-----<br>
     * <p>
     * <i><u>Sample Java API Call:</u></i><br>
            Set<X509Certificate> samlCertificates = apiClientObj.getSamlCertificates();
     * <p>
     * @return Set of X509Certificates containing server's SAML certificate and any root certificates if available. 
     * @throws IOException
     * @throws ApiException If there are any errors during the execution this exception would be returned to the caller.
     * The caller can use the getErrorCode() and getMessage() functions to retrieve the exception details.
     * @throws SignatureException 
     * @since MW 1.0
     * @deprecated As of MTW 1.2 Enterprise replaced by {@link ManagementService#getSamlCertificates() }
     */    
    X509Certificate getSamlCertificate() throws IOException, ApiException, SignatureException;

    /**
     * Retrieves the system's root CA certificate(s). 
     * <p>
     * <i><u>Roles needed:</u></i>No roles needed.
     * <p>
     * <i><u>Output content type:</u></i>Text/Plain
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: GET</i><br>
     * https://192.168.1.101:8181/ManagementService/resources/ca/certificate/rootca/current <br>
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * -----BEGIN CERTIFICATE-----
     * MIIDVDCCAjygAwIBAgIEUZyfaTANBgkqhkiG9w0BAQUFA...........EwJV
     * -----END CERTIFICATE-----<br>
     * If the certificate does not exist, an appropriate error would be returned back.<br>
     * {"error_code":"SYSTEM_ERROR","error_message":"System error: Mt Wilson Root CA certificate file is not found"}
     * <p>
     * <i><u>Sample Java API Call:</u></i><br>
            Set<X509Certificate> rootCaCertificates = apiClientObj.getRootCaCertificates();
     * <p>
     * @return Set of X509Certificates containing server's root CA certificates. 
     * @throws IOException
     * @throws ApiException If there are any errors during the execution this exception would be returned to the caller.
     * The caller can use the getErrorCode() and getMessage() functions to retrieve the exception details.
     * @throws SignatureException 
     * @since MTW 1.1 Enterprise
     */    
    Set<X509Certificate> getRootCaCertificates() throws IOException, ApiException, SignatureException;

    /**
     * Retrieves all the PrivacyCA certificates. 
     * <p>
     * <i><u>Roles needed:</u></i>No roles needed.
     * <p>
     * <i><u>Output content type:</u></i>Text/Plain
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: GET</i><br>
     * https://192.168.1.101:8181/ManagementService/resources/ca/certificate/privacyca/current <br>
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * -----BEGIN CERTIFICATE-----
     * MIIDVDCCAjygAwIBAgIEUZyfaTANBgkqhkiG9w0BAQUFA...........EwJV
     * -----END CERTIFICATE-----<br>
     * <p>
     * <i><u>Sample Java API Call:</u></i><br>
            Set<X509Certificate> privacyCaCertificates = apiClientObj.getPrivacyCaCertificates();
     * <p>
     * @return Set of X509Certificates containing server's PrivacyCA certificates. 
     * @throws IOException
     * @throws ApiException If there are any errors during the execution this exception would be returned to the caller.
     * The caller can use the getErrorCode() and getMessage() functions to retrieve the exception details.
     * @throws SignatureException 
     * @since MTW 1.1 Enterprise
     */    
    Set<X509Certificate> getPrivacyCaCertificates() throws IOException, ApiException, SignatureException;
    
    /**
     * Retrieves server's SAML certificate and any root certificates if available. This would be used to verify the signed
     * SAML assertion sent by the system. 
     * <p>
     * <i><u>Roles needed:</u></i>No roles needed.
     * <p>
     * <i><u>Output content type:</u></i>Text/Plain
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: GET</i><br>
     * https://192.168.1.101:8181/ManagementService/resources/ca/certificate/saml/current <br>
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * -----BEGIN CERTIFICATE-----
     * MIIDVDCCAjygAwIBAgIEUZyfaTANBgkqhkiG9w0BAQUFA...........EwJV
     * -----END CERTIFICATE-----<br>
     * <p>
     * <i><u>Sample Java API Call:</u></i><br>
            Set<X509Certificate> samlCertificates = apiClientObj.getSamlCertificates();
     * <p>
     * @return Set of X509Certificates containing server's SAML certificate and any root certificates if available. 
     * @throws IOException
     * @throws ApiException If there are any errors during the execution this exception would be returned to the caller.
     * The caller can use the getErrorCode() and getMessage() functions to retrieve the exception details.
     * @throws SignatureException 
     * @since MTW 1.1 Enterprise
     */    
    Set<X509Certificate> getSamlCertificates() throws IOException, ApiException, SignatureException;
    
    /**
     * Retrieves server's TLS (SSL) certificate and any root certificates if available.  
     * <p>
     * <i><u>Roles needed:</u></i>No roles needed.
     * <p>
     * <i><u>Output content type:</u></i>Text/Plain
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: GET</i><br>
     * https://192.168.1.101:8181/ManagementService/resources/ca/certificate/tls/current <br>
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * -----BEGIN CERTIFICATE-----
     * MIIDVDCCAjygAwIBAgIEUZyfaTANBgkqhkiG9w0BAQUFA...........EwJV
     * -----END CERTIFICATE-----<br>
     * If the server does not have an SSL Certificate setup then appropriate error would be sent back to the caller <br>
     * {"error_code":"SYSTEM_ERROR","error_message":"System error: Server SSL certificate file is not found"}
     * <p>
     * <i><u>Sample Java API Call:</u></i><br>
            Set<X509Certificate> tlsCertificates = apiClientObj.getTlsCertificates();
     * <p>
     * @return Set of X509Certificates containing server's SSL and any root certificates if available. 
     * @throws IOException
     * @throws ApiException If there are any errors during the execution this exception would be returned to the caller.
     * The caller can use the getErrorCode() and getMessage() functions to retrieve the exception details.
     * @throws SignatureException 
     * @since MTW 1.1 Enterprise
     */    
    Set<X509Certificate> getTlsCertificates() throws IOException, ApiException, SignatureException;

    /**
     * Reserved for future release.  This API is currently not supported.
     * @return
     * @throws IOException
     * @throws ApiException
     * @throws SignatureException 
     */
    //CaInfo getCaStatus() throws IOException, ApiException, SignatureException;
    
    /**
     * Sets up the password for the CA in the configuration file. 
     * <p>
     * <i><u>Roles needed:</u></i>Security
     * <p>
     * <i><u>Input content type:</u></i>Application/JSON
     * <p>
     * <i><u>Output content type:</u></i>Text/Plain
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: POST</i><br>
     * https://192.168.1.101:8181/ManagementService/resources/ca/enable <br>
     * <i>Sample Input</i><br>
     * {"newSaltedPasswordString":"INTEL"}
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * true
     * <p>
     * <i><u>Sample Java API Call:</u></i><br>
            apiClientObj.enableCaWithPassword("INTEL");
     * <p>
     * @throws IOException
     * @throws ApiException If there are any errors during the execution this exception would be returned to the caller.
     * The caller can use the getErrorCode() and getMessage() functions to retrieve the exception details.
     * @throws SignatureException 
     * @since MW 1.1 
     */   
        // Commenting this function for now as it is not being used
    // void enableCaWithPassword(String newPassword) throws IOException, ApiException, SignatureException;
    
    
    /**
     * Disables/removes the password for the CA in the configuration file. 
     * <p>
     * <i><u>Roles needed:</u></i>Security
     * <p>
     * <i><u>Output content type:</u></i>Text/Plain
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: POST</i><br>
     * https://192.168.1.101:8181/ManagementService/resources/ca/disable <br>
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * true
     * <p>
     * <i><u>Sample Java API Call:</u></i><br>
            apiClientObj.disableCa();
     * <p>
     * @throws IOException
     * @throws ApiException If there are any errors during the execution this exception would be returned to the caller.
     * The caller can use the getErrorCode() and getMessage() functions to retrieve the exception details.
     * @throws SignatureException 
     * @since MW 1.1 
     */    
    // Commenting this function for now as it is not being used
    //void disableCa() throws IOException, ApiException, SignatureException;
    
    /**
     * Registers the list of hosts specified with the system. By default all the hosts will be associated with MLEs created with OEM as the 
     * White List target option. If the host is already registered in the system, then it will be updated (associated with correct MLEs)
     * to match the current version of the BIOS and OS/Hypervisor running on the host. <br>
     * If any of the hosts specified have already been registered, the function will update the host association with MLEs to match
     * the current version of the BIOS & OS/Hypervisor running on the host.
     * <p>
     * <i><u>Pre-requisite:</u></i>White list has to be configured with the White List target of OEM since this function uses
     * OEM as the white list target by default. If the user has created the White List with either GLOBAL or HOST Specific option
     * then the custom host registration function has to be used or else an error would be returned back to the caller.
     * <p>
     * <i><u>Roles needed:</u></i>Attestation
     * <p>
     * <i><u>Input content type:</u></i>Application/JSON
     * <p>
     * <i><u>Output content type:</u></i>Application/JSON
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: POST</i><br>
     * https://192.168.1.101:8181/ManagementService/resources/host/bulk<br>
     * <i>Sample Input</i><br>
     * {"HostRecords":[{"HostName":"192.168.1.201","AddOn_Connection_String":"vmware:https://192.168.1.222:443/sdk;Administrator;password"},{"HostName":"192.168.1.202","AddOn_Connection_String":"vmware:https://192.168.1.222:443/sdk;Administrator;password"}]}
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * {"HostRecords":[{"Host_Name":"192.168.1.201","Status":"true","Error_Message":"","Error_Code":"OK"},{"Host_Name":"192.168.1.202","Status":"true","Error_Message":"","Error_Code":"OK"}]}<br>
     * <p>
     * <i><u>Sample Java API Call:</u></i><br>
            TxtHostRecord hostObj = new TxtHostRecord();<br>
            hostObj.HostName = "192.168.1.203";<br>
            hostObj.AddOn_Connection_String = "intel:https://192.168.1.203:9999";<br>
            
            TxtHostRecord hostObj2 = new TxtHostRecord();<br>
            hostObj2.HostName = "192.168.1.204";<br>
            hostObj2.AddOn_Connection_String = "intel:https://192.168.1.204:9999";<br>

            TxtHostRecordList hostList = new TxtHostRecordList();<br>
            hostList.getHostRecords().add(hostObj);<br>
            hostList.getHostRecords().add(hostObj2);<br>
            HostConfigResponseList registerHosts = apiClientObj.registerHosts(hostList);<br>
            for (HostConfigResponse hcr: registerHosts.getHostRecords()) {<br>
                System.out.println(hcr.getHostName() + ":" + hcr.getStatus() + ":" + hcr.getErrorCode().toString());<br>
            }<br>
     * <p>
     * 
     * @param hostRecords {@link TxtHostRecordList} object with the list of all the hosts to be registered.
     * Only the HostName and AddOn_Connection_String parameters are required for each of the hosts. The AddOn_Connection_String should
     * be in the format specified in the registerHosts method. For VMware hosts, the AddOn_Connection_String would have the DNS or IP Address
     * of the vCenter Server.
     * @return {@link HostConfigResponseList } having {@link HostConfigResponse} for each of the hosts that the user wanted to be registered .
     * @throws IOException
     * @throws ApiException If there are any errors during the execution this exception would be returned to the caller.
     * The caller can use the getErrorCode() and getMessage() functions to retrieve the exception details.
     * @throws SignatureException 
     * @since MTW 1.2 Enterprise
     */        
    HostConfigResponseList registerHosts(TxtHostRecordList hostRecords) throws IOException, ApiException, SignatureException;
    
    /**
     * Registers the list of  hosts specified with the system using the customized options. This function allows the user to specify
     * the white list (MLE) that needs to be associated with the host by specifying the white list target. The system support 3 options 
     * for white list target.[GLOBAL: The white list is applicable for hosts from any OEM running the same version of the OS/Hypervisor. 
     * OEM: The  white list is applicable for the hosts from a specific OEM running the same version of the OS/Hypervisor. 
     * HOST: The white list is valid only for specific host.]. If the user does not want to specify this option and want to go with
     * the default OEM white list, the the registerHosts(TxtHostRecordList) could be used.
     * <p>
     * <i><u>Pre-requisite:</u></i>White list has to be configured with the White List target that the host would be using. If the
     * MLE & associated white lists do not exist, then appropriate error would be returned back to the caller.
     * <p>
     * <i><u>Roles needed:</u></i>Attestation
     * <p>
     * <i><u>Input content type:</u></i>Application/JSON
     * <p>
     * <i><u>Output content type:</u></i>Application/JSON
     * <p>
     * <i><u>Sample REST API call :</u></i><br>
     * <i>Method Type: POST</i><br>
     * https://192.168.1.101:8181/ManagementService/resources/host/bulk/custom<br>
     * <i>Sample Input</i><br>
     * {"HostRecords":[{"BIOS_WhiteList_Target":"BIOS_OEM","VMM_WhiteList_Target":"VMM_OEM", "TXT_Host_Record":{"HostName":"192.168.1.201","AddOn_Connection_String":"intel:https://192.168.1.201:9999"}},
     * {"BIOS_WhiteList_Target":"BIOS_OEM","VMM_WhiteList_Target":"VMM_OEM", "TXT_Host_Record":{"HostName":"192.168.1.202","AddOn_Connection_String":"intel:https://192.168.1.202:9999"}}]}
     * <p>
     * <i><u>Sample Output:</u></i><br>
     * {"HostRecords":[{"Host_Name":"192.168.1.201","Status":"false","Error_Message":"VMM MLE Xen - 11-4.1.0' is not configured in the system. Please verify if the white list is properly configured.[MS_VMM_MLE_NOT_FOUND]","Error_Code":null},
     * {"Host_Name":"192.168.1.202","Status":"true","Error_Message":"","Error_Code":"OK"}]}
     * <p>
     * <i><u>Sample Java API Call:</u></i><br>
            TxtHostRecord hostObj = new TxtHostRecord();<br>
            hostObj.HostName = "192.168.1.201";<br>
            hostObj.AddOn_Connection_String = "intel:https://192.168.1.201:9999";<br>
            HostConfigData hostConfigObj = new HostConfigData();<br>
            hostConfigObj.setBiosWLTarget(HostWhiteListTarget.BIOS_OEM);<br>
            hostConfigObj.setVmmWLTarget(HostWhiteListTarget.VMM_OEM);<br>
            hostConfigObj.setTxtHostRecord(hostObj);<br>
            <br>
            TxtHostRecord hostObj2 = new TxtHostRecord();<br>
            hostObj2.HostName = "192.168.1.202";<br>
            hostObj2.AddOn_Connection_String = "intel:https://192.168.1.202:9999";<br>
            HostConfigData hostConfigObj2 = new HostConfigData();<br>
            hostConfigObj2.setBiosWLTarget(HostWhiteListTarget.BIOS_OEM);<br>
            hostConfigObj2.setVmmWLTarget(HostWhiteListTarget.VMM_GLOBAL);<br>
            hostConfigObj2.setTxtHostRecord(hostObj);<br>
            <br>
            HostConfigDataList hosts = new HostConfigDataList();<br>
            hosts.getHostRecords().add(hostConfigObj);<br>
            hosts.getHostRecords().add(hostConfigObj2);<br>
            HostConfigResponseList registerHosts = apiClientObj.registerHosts(hosts);<br>
            for (HostConfigResponse hcr: registerHosts.getHostRecords()) {<br>
                System.out.println(hcr.getHostName() + ":" + hcr.getStatus());<br>
            }            <br>
     * <p>
     * 
     * @param hostRecords {@link HostConfigDataList} object with the list of all the hosts to be registered
     * along with the customization options.The HostName and AddOn_Connection_String parameters for the TxtHostRecord
     * object are required. The user also has to specify which of the OEM/GLOBAL/HOST options for white list target has to
     * be used.
     * @return {@link HostConfigResponseList } having {@link HostConfigResponse} for each of the hosts that the user wanted to be registered. 
     * @throws IOException
     * @throws ApiException If there are any errors during the execution this exception would be returned to the caller.
     * The caller can use the getErrorCode() and getMessage() functions to retrieve the exception details.
     * @throws SignatureException 
     * @since MTW 1.2 Enterprise
     */        
    HostConfigResponseList registerHosts(HostConfigDataList hostRecords) throws IOException, ApiException, SignatureException;
    
}
