/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.intel.mountwilson.trustagent.commands;

import com.intel.mountwilson.common.CommandUtil;
import com.intel.mountwilson.common.ErrorCode;
import com.intel.mountwilson.common.ICommand;
import com.intel.mountwilson.common.TAException;
import com.intel.mountwilson.trustagent.data.TADataContext;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.intel.dcsg.cpg.crypto.RandomUtil;
import org.apache.commons.lang.StringUtils;



/**
 *
 * @author stdalex
 */
public class SetAssetTag implements ICommand{
   Logger log = LoggerFactory.getLogger(getClass().getName());
    private TADataContext context;
    private final static String index ="0x40000010";
    
    public SetAssetTag(TADataContext context) {
        this.context = context;
    }
        
    @Override
    public void execute() throws TAException{
        try {
            
            String password = "";
            
            //create the index if needed
            boolean iExists = indexExists();
            if(iExists){  // if it exists we need to get the password from the service for the nvram
                
            }else{ // generate random password 
                password = generateRandomPass();
                createIndex(password);
            }
            
            //now index is created, write value to it
            writeHashToFile();  // store the hash as a binary file
            
            if(!writeHashToNvram(password)) {
                // need some type of exception here
            }
            
            //last thing is, if we generated a new password, we need to register it
            if(!iExists) {
                registerPassword(password);
            }
            
            context.setResponseXML("<response>true</response>");
            
        } catch (IOException ex) {
            log.error(ex.getMessage());
            throw new TAException(ErrorCode.ERROR,"error while trying to set asset tag",ex);
        }
    }
    
    private void registerPassword(String password) {
        // get uuid from context.getHostUUID() and call the asset tag service
        // and associate password with context.getHostUUID()
    }
    
    private boolean writeHashToNvram(String password) throws TAException, IOException {
        List<String> result;
        try {
            result = CommandUtil.runCommand("tpm_nvwrite -i " + index + " -p" + password + " -f /tmp/hash");
            String response = StringUtils.join(result,"\n");
        }catch(TAException ex) {
                log.error("error writing to nvram, " + ex.getMessage() );
        }
        return true;
    }
    
    private void writeHashToFile() throws TAException, IOException {
        try {
            List<String> result = CommandUtil.runCommand("echo " + context.getAssetTagHash() + " | hex2bin > /tmp/hash");
            String response = StringUtils.join(result,"\n");
        }catch(TAException ex) {
                log.error("error writing to nvram, " + ex.getMessage() );
        }        
    }
    
    private boolean createIndex(String password) throws TAException, IOException {
        List<String> result;
        try {
            result = CommandUtil.runCommand("tpm_nvdefine -i " + index + " -s 0x14 -a" + password + " --permissions=\"AUTHWRITE\"");
            String response = StringUtils.join(result,"\n");
        }catch(TAException ex) {
                log.error("error writing to nvram, " + ex.getMessage() );
        }
        return true;
    }
    
    private String generateRandomPass() {
       return RandomUtil.randomHexString(20);
    }
    
    private boolean indexExists() throws TAException, IOException {     
        List<String> result;
        try {
            result = CommandUtil.runCommand("tpm_nvinfo -i " + index);  
            String response = StringUtils.join(result,"\n");
            if(response.contains("NVRAM index")) 
                return true;
        }catch(TAException ex) {
                log.error("error writing to nvram, " + ex.getMessage() );
        }
        return false;
    }
}
