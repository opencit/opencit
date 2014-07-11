/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mountwilson.trustagent.commands;

import com.intel.mountwilson.common.CommandUtil;
import com.intel.mountwilson.common.ErrorCode;
import com.intel.mountwilson.common.ICommand;
import com.intel.mountwilson.common.TAException;
import com.intel.mountwilson.trustagent.data.TADataContext;
import com.intel.mtwilson.trustagent.TrustagentConfiguration;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author dsmagadX
 */
public class ReadIdentityCmd implements ICommand {

    Logger log = LoggerFactory.getLogger(getClass().getName());
    private TADataContext context;

    public ReadIdentityCmd(TADataContext context) {
        this.context = context;
    }

    @Override
    public void execute() throws TAException {
        
        /*
        File aikCertFile = new File(context.getAikCertFileName());
        
        if(aikCertFile.exists()){
            log.debug( "AIK Certificate To Read - {}", context.getAikCertFileName());

            context.setAIKCertificate(CommandUtil.readCertificate(context.getAikCertFileName())); // this file name is configured ;it is NOT user input

            log.debug("AIK Certificate Read to memory - {}", context.getAikCertFileName());
        }else{
            throw new TAException(ErrorCode.CERT_MISSING,"Aik Certificate file is missing.");
        } 
        */
        
        try {
            TrustagentConfiguration configuration = TrustagentConfiguration.loadConfiguration();
            File aikCertificateFile = configuration.getAikCertificateFile();
            if( !aikCertificateFile.exists() ) {
                log.error("Missing AIK certificate file: {}", aikCertificateFile.getAbsolutePath());
                throw new TAException(ErrorCode.CERT_MISSING,"Aik Certificate file is missing.");
            }
            String aikPem = FileUtils.readFileToString(aikCertificateFile);
            context.setAIKCertificate(aikPem);
        } catch (IOException ex) {
            throw new TAException(ErrorCode.CERT_MISSING, "Cannot load trustagent configuration");
        }
        
    }


}
