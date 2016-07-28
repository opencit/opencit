/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.datatypes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.intel.dcsg.cpg.validation.RegexPatterns;
import com.intel.dcsg.cpg.validation.Regex;
import com.intel.dcsg.cpg.validation.Validator;
import com.intel.mtwilson.validators.ConnectionStringValidator;
import java.net.MalformedURLException;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.intel.mtwilson.tls.policy.TlsPolicyChoice;
import java.util.HashMap;
import java.util.Map;
import org.apache.shiro.util.StringUtils;
//import org.codehaus.jackson.annotate.JsonIgnoreProperties;
//import org.codehaus.jackson.annotate.JsonProperty;
//import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * Extended TxtHostRecord supported by Mt Wilson 2.0 
 * This extended record contains additional fields which Mt Wilson 1.x clients
 * will not send and do not expect to receive (they may throw an error upon
 * receiving unexpected fields). Therefore the new fields can only be used by Mt Wilson
 * 2.x clients using v1 APIs when sending requests. Mt Wilson 1.x and 2.x will
 * always reply with the original TxtHostRecord fields from any v1 APIs in order to
 * preserve backward compatibility with Mt Wilson 1.x clients.
 * 
 * In order to fully utilize the capability represented by the new fields, 
 * clients should use v2 APIs. 
 * 
 * A data transfer object. The TxtHostRecord object is validated on construction
 * so to make it easier to create a TxtHost object, you can put all the data
 * (unvalidated) into a TxtHostRecord and then use it to construct a TxtHost.
 * @author jbuhacoff
 */
public class TxtHostRecord {
    
    @JsonProperty    
    @Regex(RegexPatterns.IPADDR_FQDN)
    public String HostName;
    @JsonProperty
    @Regex(RegexPatterns.IPADDR_FQDN)
    public String IPAddress;
    @JsonProperty
//    @Regex(RegexPatterns.PORT) // regex can only be tested against String variables ;   
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
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
    public String Hardware_Uuid;
    @JsonProperty
    public String TpmVersion;
    @JsonProperty
    public String PcrBanks;
    /**
     * @since 2.0
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
    @JsonProperty
    public TlsPolicyChoice tlsPolicyChoice;
    
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
        tlsPolicyChoice = input.getTlsPolicyChoice();
        TpmVersion = input.getTpmVersion();
        PcrBanks = input.getPcrBanks();
    }    



    public TxtHostRecord(TxtHostRecord host) {
        this.AIK_Certificate = host.AIK_Certificate;
        this.AIK_PublicKey = host.AIK_PublicKey;
        this.AIK_SHA1 = host.AIK_SHA1;
        this.AddOn_Connection_String = host.AddOn_Connection_String;
        this.BIOS_Name = host.BIOS_Name;
        this.BIOS_Oem = host.BIOS_Oem;
        this.BIOS_Version = host.BIOS_Version;
        this.Description = host.Description;
        this.Email = host.Email;
        this.Hardware_Uuid = host.Hardware_Uuid;
        this.HostName = host.HostName;
        this.IPAddress = host.IPAddress;
        this.Location = host.Location;
        this.Port = host.Port;
        this.Processor_Info = host.Processor_Info;
        this.VMM_Name = host.VMM_Name;
        this.VMM_OSName = host.VMM_OSName;
        this.VMM_OSVersion = host.VMM_OSVersion;
        this.VMM_Version = host.VMM_Version;
        this.tlsPolicyChoice = host.tlsPolicyChoice;
        this.TpmVersion = host.TpmVersion;
        this.PcrBanks = host.PcrBanks;

    }    
    
    @JsonIgnore
    public String getBestPcrAlgorithmBank() {
        if(PcrBanks != null && PcrBanks.length() > 0)
            return selectBestSinglePcrBank(this.PcrBanks);
        else 
            return "SHA1";
    }
    
    private String selectBestSinglePcrBank(String availableBanks) {        
        String[] banks = StringUtils.split(availableBanks, ' ');
        Map<String, Integer> rankings = new HashMap<>();
        rankings.put("SHA1", 0);
        rankings.put("SHA256", 1);
        rankings.put("SHA384", 2);
        rankings.put("SHA512", 3);
        
        String best = banks[0];
        for(String b : banks) {
            if(rankings.get(b) > rankings.get(best)) {
                best = b;
            }
        }        
        return best;
    }


}
