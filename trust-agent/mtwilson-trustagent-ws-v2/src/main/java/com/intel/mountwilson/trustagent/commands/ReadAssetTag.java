/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mountwilson.trustagent.commands;

import com.intel.mountwilson.common.CommandResult;
import com.intel.mountwilson.common.CommandUtil;
import com.intel.mountwilson.common.ICommand;
import com.intel.mountwilson.common.TAException;
import com.intel.mountwilson.trustagent.data.TADataContext;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hxia5
 */
@Deprecated
public class ReadAssetTag implements ICommand {

    Logger log = LoggerFactory.getLogger(getClass().getName());
    private TADataContext context;
    private final static String index = "0x40000010";
    
    public ReadAssetTag(TADataContext context) {
        this.context = context;
    }
        
    @Override
    public void execute() throws TAException{
        try {
            log.debug("ReadAssetTagWindows execute");
         
            boolean iExists = indexExists();
            if(iExists){  // if it exists we need to get the password from the service for the nvram
                log.debug("Asset Tag Index {} exists", index);
                readFromNvram();
            }else{ // generate random password 
                // Just use the same password right now for testing
                // password =  generateRandomPass();
                log.debug("Asset Tag Index does not exist.");
            }
        } catch (Exception ex) {
            log.error(ex.getMessage());
            context.setResponseXML("<set_asset_tag><response>false</response><error>"+ex.getMessage() + "</error></set_asset_tag>");
        }
    }
    
    private boolean readFromNvram() throws TAException, IOException {
        try {
            String cmd = "tpmtool.exe nvread " + index;
            log.debug("Running command: " + cmd);
            CommandResult cmdResult = CommandUtil.runCommand(cmd);
            
            // check if the cmd returns successfully. if so, set the assettag
            if (cmdResult.getExitcode() == 0) {
                log.debug("Provisioned Asset tag hash: {}", cmdResult.getStdout());
                context.setAssetTagHash(cmdResult.getStdout());
            } else {
                log.debug("Erro reading Asset tag");
                return false;
            }
        }catch(TAException ex) {
                log.error("error reading assetTag from nvram 0x40000010, " + ex.getMessage() );
                throw ex;
        }
        return true;
    }
    
    private boolean indexExists() throws TAException, IOException {     
        try {
            CommandResult result = CommandUtil.runCommand("tpmtool.exe nvinfo " + index);
            if (result != null && result.getStdout() != null) {
                if(result.getStdout().contains("NVRAM index")) 
                    return true;
            }
        }catch(TAException ex) {
                log.error("error reading nvram nvinfo 0x40000010, " + ex.getMessage() );
                throw ex;
        }
        return false;
    }
}
