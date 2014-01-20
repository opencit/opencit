/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.intel.mtwilson.atag.resource;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.intel.dcsg.cpg.net.InternetAddress;
import com.intel.mtwilson.atag.dao.Derby;
import com.intel.mtwilson.atag.resource.CertificateResource.CertificateActionName;
import java.sql.SQLException;
import org.restlet.data.Status;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author stdalex
 */
public class AutomationResource extends ServerResource {
    private Logger log = LoggerFactory.getLogger(getClass());
    
    @Override
    protected void doInit() throws ResourceException {
        super.doInit();
    }


    @Override
    protected void doRelease() throws ResourceException {
        super.doRelease();
    }
    
    @Get("txt")
    public String existingCertificateContent() {
        return "It worked!";
    }
    
    public static enum AutomationActionName {
        GETUUID;
        @Override
        public String toString() {
            return name().toLowerCase();
        }
        @JsonCreator
        public static AutomationActionName valueOfText(String text) {
            for(AutomationActionName action : AutomationActionName.values()) {
                if( action.name().equalsIgnoreCase(text)) { return action; }
            }
            throw new IllegalArgumentException("Unknown action");
        }
    }
    
    public static abstract class AutomationAction /*extends ObjectModel*/ {
        private AutomationActionName name;
       
        public AutomationAction(AutomationActionName name) {
            this.name = name;
        } 
    }       
    
    public static class AutomationUUIDAction extends AutomationAction {
        public InternetAddress host;
        public int port;
        // for citrix:
        public String username;
        public String password;
        public String UUID;
        public AutomationUUIDAction() {
            super(AutomationActionName.GETUUID);   
        }
        public void setUUID(String UUID) {
            this.UUID = UUID;
        }
        
        public String getUUID() {
            return this.UUID;
        }
        
        public void setHost(InternetAddress host) {
            this.host = host;
        }

        public InternetAddress getHost() {
            return host;
        }

        public void setPort(int port) {
            this.port = port;
        }
        
        public int getPort() {
            return port;
        }
        
        public String getUsername() {
            return username;
        }
        public void setUsername(String username) {
            this.username = username;
        }
        
        public void setPassword(String password) {
            this.password = password;
        }

        public String getPassword() {
            return password;
        }
    }
    
    @JsonInclude(Include.NON_NULL)
    public static class AutomationActionChoice {
        public AutomationUUIDAction uuid;
    }
    
    @Post("json:json")
    public AutomationActionChoice actionAutomation(AutomationActionChoice actionChoice) {
        log.debug("made it into actionAutomation!");
        if(actionChoice.uuid != null) {
            AutomationActionChoice result = new AutomationActionChoice();
            result.uuid = actionChoice.uuid;
            result.uuid.setUUID("F4B17194-CAE7-11DF-B40B-001517FA9844");
            return result;
        }
        setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
        return null;
    }
}
