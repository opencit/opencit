/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.business.trust.worker;

import com.intel.mtwilson.datatypes.HostTrust;
import java.util.concurrent.Future;

import javax.ejb.Local;

/**
 *
 * @author dsmagadX
 */
@Local
public interface AsyncTrustWorkerLocal{
    public Future<String> getTrustSaml(String host, boolean forceVerify);

    public Future<HostTrust> getTrustJson(String host, boolean forceVerify);

}
