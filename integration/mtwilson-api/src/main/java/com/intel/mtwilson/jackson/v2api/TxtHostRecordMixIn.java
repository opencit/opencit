/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jackson.v2api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.intel.mtwilson.tls.policy.TlsPolicyChoice;

/**
 *
 * @author jbuhacoff
 */
public abstract class TxtHostRecordMixIn {

    @JsonProperty("host_name") public String HostName;
    @JsonProperty("ip_address") public String IPAddress;
    @JsonProperty("port") public Integer Port;
    @JsonProperty("bios_name") public String BIOS_Name;
    @JsonProperty("bios_version") public String BIOS_Version;
    @JsonProperty("bios_oem") public String BIOS_Oem;
    @JsonProperty("vmm_name") public String VMM_Name;
    @JsonProperty("vmm_version") public String VMM_Version;
    @JsonProperty("vmm_os_name") public String VMM_OSName;
    @JsonProperty("vmm_os_version") public String VMM_OSVersion;
    @JsonProperty("add_on_connection_string") public String AddOn_Connection_String;
    @JsonProperty("description") public String Description;
    @JsonProperty("email") public String Email;
    @JsonProperty("location") public String Location;
    @JsonProperty("aik_certificate") public String AIK_Certificate;
    @JsonProperty("aik_public_key") public String AIK_PublicKey;
    @JsonProperty("aik_sha1") public String AIK_SHA1;
    @JsonProperty("processor_info") public String Processor_Info;
    @JsonProperty("hardware_uuid") public String Hardware_Uuid;
    @JsonProperty("tls_policy_choice") public TlsPolicyChoice tlsPolicyChoice;
    
}
