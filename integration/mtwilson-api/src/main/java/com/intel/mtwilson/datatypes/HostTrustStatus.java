package com.intel.mtwilson.datatypes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
//import org.codehaus.jackson.annotate.JsonProperty;

/**
 * @since 0.5.1
 * @author jbuhacoff
 */
public class HostTrustStatus
{
    @JsonProperty("bios") public boolean bios = false;
    @JsonProperty("vmm") public boolean vmm = false;
    @JsonProperty("location") public boolean location = false;
    
    @JsonIgnore
    @JsonProperty("asset_tag") public boolean asset_tag = false;
    
    public HostTrustStatus() {
        
    }
    
    public HostTrustStatus(HostTrustStatus copy) {
        this.bios = copy.bios;
        this.vmm = copy.vmm;
        this.location = copy.location;
        this.asset_tag = copy.asset_tag;
    }
}
