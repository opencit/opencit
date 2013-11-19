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
import java.io.File;
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
        
        
        File aikCertFile = new File(context.getAikCertFileName());
        
        if(aikCertFile.exists()){
            log.debug( "AIK Certificate To Read - {}", context.getAikCertFileName());

            context.setAIKCertificate(CommandUtil.readCertificate(context.getAikCertFileName())); // this file name is configured ;it is NOT user input

            log.debug("AIK Certificate Read to memory - {}", context.getAikCertFileName());
        }else{
            throw new TAException(ErrorCode.CERT_MISSING,"Aik Certificate file is missing.");
        } 
    }


}
