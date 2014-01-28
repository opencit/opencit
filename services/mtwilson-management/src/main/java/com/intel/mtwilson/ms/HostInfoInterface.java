/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.ms;

import com.intel.mtwilson.datatypes.TxtHostRecord;

/**
 *
 * @author ssbangal
 */
public interface HostInfoInterface {

    TxtHostRecord getHostDetails(TxtHostRecord hostObj) throws Exception;
    
    String getHostAttestationReport(TxtHostRecord hostObj, String pcrList) throws Exception;
}
