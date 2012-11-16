package com.intel.mtwilson.datatypes;

import javax.xml.datatype.XMLGregorianCalendar;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 *
 * @author dsmagadx
 */
public class HostTrustLevel1String
{
    @JsonProperty("trust_lvl") public String trustLevel ;
    @JsonProperty("timestamp") public String timestamp ;
    
}

