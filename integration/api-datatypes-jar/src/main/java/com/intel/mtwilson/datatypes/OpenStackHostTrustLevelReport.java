package com.intel.mtwilson.datatypes;

import java.util.HashMap;
import java.util.Map;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 *
 * @author dsmagadx
 */
public class OpenStackHostTrustLevelReport
{
    @JsonProperty("count") public int count;
    @JsonProperty("hosts") public Map<Hostname,HostTrustLevel1String>pollHosts = new HashMap<Hostname, HostTrustLevel1String>(); // datatype.Hostname

}
