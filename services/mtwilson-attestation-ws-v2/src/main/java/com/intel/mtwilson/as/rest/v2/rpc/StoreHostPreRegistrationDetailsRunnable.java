/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.rest.v2.rpc;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.My;
import com.intel.mtwilson.as.data.MwHostPreRegistrationDetails;
import com.intel.mtwilson.launcher.ws.ext.RPC;
import com.intel.mtwilson.repository.RepositoryCreateException;
import java.util.Calendar;
import org.apache.shiro.authz.annotation.RequiresPermissions;

/**
 *
 * @author ssbangal
 */
@RPC("store-host-pre-registration-details")
@JacksonXmlRootElement(localName = "store_host_pre_registration_details")
public class StoreHostPreRegistrationDetailsRunnable implements Runnable {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(StoreHostPreRegistrationDetailsRunnable.class);
    
    private String hostName;
    private String userName;
    private String password;

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    
    @Override
    @RequiresPermissions({"store_host_pre_registration_details:create"})
    public void run() {
        try {
            if (hostName != null && !hostName.isEmpty() && userName != null && !userName.isEmpty()) {

                log.debug("About to store the pre-registration data for host {} with user name '{}' & password '{}'.", hostName, userName, password);

                MwHostPreRegistrationDetails host = new MwHostPreRegistrationDetails();
                host.setId(new UUID().toString());
                host.setName(hostName);
                host.setLogin(userName);
                host.setPassword(password);
                host.setCreatedTs(Calendar.getInstance().getTime());

                My.jpa().mwHostPreRegistrationDetails().create(host);
                log.debug("Successfully registered the host data");

            } else {
                throw new Exception("Invalid input specified or input value missing.");
            }
        } catch (Exception ex) {
            log.error("Error during pre-registration of host data.", ex);
            throw new RepositoryCreateException();
        }
    }    
}
