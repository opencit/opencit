/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.datatypes;

import com.intel.dcsg.cpg.validation.RegexPatterns;
import com.intel.dcsg.cpg.validation.Regex;
import com.intel.dcsg.cpg.validation.Validator;
import com.intel.mtwilson.validators.ConnectionStringValidator;
import java.net.MalformedURLException;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
//import org.codehaus.jackson.annotate.JsonIgnoreProperties;
//import org.codehaus.jackson.annotate.JsonProperty;
//import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * A data transfer object. The TxtHost object is validated on construction
 * so to make it easier to create a TxtHost object, you can put all the data
 * (unvalidated) into a TxtHostRecord and then use it to construct a TxtHost.
 * @author jbuhacoff
 */
//@JsonIgnoreProperties(ignoreUnknown = true)
public class TxtHostRecord {
    @JsonProperty    
    @Regex(RegexPatterns.IPADDR_FQDN)
    public String HostName;
    @JsonProperty
    @Regex(RegexPatterns.IPADDR_FQDN)
    public String IPAddress;
    @JsonProperty
//    @Regex(RegexPatterns.PORT) // regex can only be tested against String variables ;   TODO  we need an integer range validator  annotation
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
//    @Regex(RegExAnnotation.ADDON_CONNECTION_STRING)
    @Validator(ConnectionStringValidator.class)
    public String AddOn_Connection_String;
    @JsonProperty
    public String Description;
    @JsonProperty
    @Regex(RegexPatterns.EMAIL)
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
    @JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
    public String Hardware_Uuid;
    
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
        Hardware_Uuid = null;
    }
}
