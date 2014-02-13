package com.intel.mtwilson.datatypes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonProperty;
//import org.codehaus.jackson.annotate.JsonProperty;

/**
 *
 * @author dsmagadx
 */
public class OpenStackHostTrustLevelReport
{
   @JsonProperty("hosts") public ArrayList<HostTrustLevel1String>pollHosts = new ArrayList<HostTrustLevel1String>();

}
