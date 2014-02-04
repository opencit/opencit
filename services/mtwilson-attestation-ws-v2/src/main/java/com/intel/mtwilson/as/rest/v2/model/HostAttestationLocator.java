/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.model;

import com.intel.mtwilson.jersey.Locator;
import javax.ws.rs.PathParam;

/**
 *
 * @author ssbangal
 */
public class HostAttestationLocator implements Locator<HostAttestation> {

    @PathParam("aik")
    public String aik;
    
    @Override
    public void copyTo(HostAttestation item) {
        item.setAikFingerPrint(aik);
    }
    
}
