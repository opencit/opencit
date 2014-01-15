/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.datatypes;

import com.intel.dcsg.cpg.validation.Regex;
import java.net.MalformedURLException;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * A data transfer object. The TxtHost object is validated on construction
 * so to make it easier to create a TxtHost object, you can put all the data
 * (unvalidated) into a TxtHostRecord and then use it to construct a TxtHost.
 * @author jbuhacoff
 */
//@JsonIgnoreProperties(ignoreUnknown = true)
public class TxtHostRecord {
    @JsonProperty    
    @Regex(RegExAnnotation.IPADDR_FQDN)
    public String HostName;
    @JsonProperty
    @Regex(RegExAnnotation.IPADDR_FQDN)
    public String IPAddress;
    @JsonProperty
    @Regex(RegExAnnotation.PORT)
    public Integer Port;
    @JsonProperty
    public String BIOS_Name;
    @JsonProperty
    public String BIOS_Version;
    @JsonProperty
    public String BIOS_Oem;
    @JsonProperty
    public String VMM_Name;
    @JsonProperty
    public String VMM_Version;
    @JsonProperty
    public String VMM_OSName;
    @JsonProperty
    public String VMM_OSVersion;
    @JsonProperty
    @Regex(RegExAnnotation.ADDON_CONNECTION_STRING)
    public String AddOn_Connection_String;
    @JsonProperty
    public String Description;
    @JsonProperty
    @Regex(RegExAnnotation.EMAIL)
    public String Email;
    @JsonProperty
    public String Location;
    @JsonProperty
    public String AIK_Certificate;
    @JsonProperty
    public String AIK_PublicKey;
    @JsonProperty
    public String AIK_SHA1;
    @JsonProperty
    public String Processor_Info;    
    
    public TxtHostRecord() {
        
    }
    
    public TxtHostRecord(TxtHost input) throws MalformedURLException {
        HostName = input.getHostName().toString();
        if (input.getHostName() != null)
            IPAddress = input.getHostName().toString();
        else
            IPAddress = "";
        Port = input.getPort();
        BIOS_Name = input.getBios().getName();
        BIOS_Version = input.getBios().getVersion();
        BIOS_Oem = input.getBios().getOem();
        VMM_Name = input.getVmm().getName();
        VMM_Version = input.getVmm().getVersion();
        VMM_OSName = input.getVmm().getOsName();
        VMM_OSVersion = input.getVmm().getOsVersion();
        AddOn_Connection_String = input.getAddOn_Connection_String();
        Description = input.getDescription();
        Email = input.getEmail();
        Location = input.getLocation();
        AIK_Certificate = input.getAikCertificate();
        AIK_PublicKey = input.getAikPublicKey();
        AIK_SHA1 = input.getAikSha1();
    }
}
