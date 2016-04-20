/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.datatypes;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
//import org.codehaus.jackson.annotate.JsonProperty;

/**
 *
 * @author dsmagadx
 */
public class AttestationReport {
    private List<PcrLogReport> pcrLogs = new ArrayList<PcrLogReport>();
     
    @JsonProperty("PcrLogReport")
    public List<PcrLogReport> getPcrLogs() {
        return pcrLogs;
    }
    
}
