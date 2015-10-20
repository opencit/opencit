/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.intel.mountwilson.trustagent.commands;

import com.intel.mountwilson.common.CommandUtil;
import com.intel.mountwilson.common.ICommand;
import com.intel.mountwilson.common.TAException;
import com.intel.mountwilson.trustagent.data.TADataContext;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.intel.dcsg.cpg.crypto.RandomUtil;
import com.intel.mtwilson.codec.HexUtil;
import com.intel.mountwilson.common.CommandResult;
import com.intel.mountwilson.common.TAConfig;
import java.io.File;
import java.security.SecureRandom;
import org.apache.commons.codec.binary.Hex;



/**
 *
 * @author stdalex
 */
public class SetAssetTag implements ICommand{
   Logger log = LoggerFactory.getLogger(getClass().getName());
    private TADataContext context;
    private final static String index = "0x40000010";
    private SecureRandom random;
    
    public SetAssetTag(TADataContext context) {
        this.context = context;
        this.random = new SecureRandom();
    }
        
    @Override
    public void execute() throws TAException{
        try {
            log.debug("SetAssetTag execute");
            //String password = "ffffffffffffffffffffffffffffffffffffffff";  //No longer needed, read it from props file in createIndex()
            String tpmNvramPass = generateRandomPass();
            
            log.debug("SetAssetTag generated nvram password {}", tpmNvramPass);
            //create the index if needed
            boolean iExists = indexExists();
            if(iExists){  // if it exists we need to get the password from the service for the nvram
                log.debug("Index exists. Releasing index...");
                releaseIndex();
                log.debug("Creating new index...");
                createIndex(tpmNvramPass);
            }else{ // generate random password 
                // Just use the same password right now for testing
                // password =  generateRandomPass();
                log.debug("Index does not exist. creating it...");
                createIndex(tpmNvramPass);
            }
            //log.debug("using password " + password + " for index");
            //now index is created, write value to it
            String filename = writeHashToFile();  // store the hash as a binary file
            
            if(!writeHashToNvram(filename, tpmNvramPass)) {
                // need some type of exception here
                log.error("Error writing hash to NVRAM");
            }
            
            //last thing is, if we generated a new password, we need to register it
            if(!iExists) {
                registerPassword();
            }
            
            context.setResponseXML("<set_asset_tag><response>true</response></set_asset_tag>");
            
        } catch (Exception ex) {
            log.error(ex.getMessage());
            context.setResponseXML("<set_asset_tag><response>false</response><error>"+ex.getMessage() + "</error></set_asset_tag>");
        }
    }
    
    private void registerPassword() {
        // get uuid from context.getHostUUID() and call the asset tag service
        // and associate password with context.getHostUUID()
    }
    
    private boolean writeHashToNvram(String filename, String NvramPassword) throws TAException, IOException {
        try {
            //String tpmOwnerPass = TAConfig.getConfiguration().getString("tpm.owner.secret");
            //String tpmNvramPass = TAConfig.getConfiguration().getString("TpmNvramAuth");
            if (!HexUtil.isHex(NvramPassword)) {
                log.error("NvramPassword is not in hex format: {}", NvramPassword);
                throw new IllegalArgumentException(String.format("NvramPassword is not in hex format: %s", NvramPassword));
            }
            log.debug("running command tpm_nvwrite -x -i " + index + " -pXXXX -f "+filename);
            String[] variables = { "NvramPassword=" + NvramPassword };
            CommandUtil.runCommand("tpm_nvwrite -x -t -i " + index + " -pNvramPassword -f "+filename, variables);
            // now delete the temporary hash file
            File file = new File(filename);
            file.delete();
        }catch(TAException ex) {
                log.error("error writing to nvram, " + ex.getMessage() );
                throw ex;
        }
        return true;
    }
    
    private String getRandomHexString(int length) {
        byte[] data = new byte[length/2];
        random.nextBytes(data);
        return Hex.encodeHexString(data);
    }
    
    /**
     * Returns the file name containing the hash
     * @return
     * @throws TAException
     * @throws IOException 
     */
    private String writeHashToFile() throws TAException, IOException {
        try {
            String assetTagHash = context.getAssetTagHash();
            if (!HexUtil.isHex(assetTagHash)) {
                log.error("assetTagHash is not in hex format: {}", assetTagHash);
                throw new IllegalArgumentException(String.format("assetTagHash is not in hex format: %s", assetTagHash));
            }
            // hex2bin must be in the PATH, which could be /usr/local/bin or it could be /opt/trustagent/bin ; do not hardcode full path here
            String filename = String.format("/tmp/tagent_tag_%s", getRandomHexString(8));
            CommandUtil.runCommand(String.format("hex2bin %s %s", assetTagHash, filename));
            return filename;
        }catch(TAException ex) {
                log.error("error writing to nvram, " + ex.getMessage() );
                throw ex;
        }        
    }
    
    private boolean createIndex(String NvramPassword) throws TAException, IOException {
        try {
            String tpmOwnerPass = TAConfig.getConfiguration().getString("tpm.owner.secret");
            if (!HexUtil.isHex(NvramPassword)) {
                log.error("NvramPassword is not in hex format: {}", NvramPassword);
                throw new IllegalArgumentException(String.format("NvramPassword is not in hex format: %s", NvramPassword));
            }
            if (!HexUtil.isHex(tpmOwnerPass)) {
                log.error("tpmOwnerPass is not in hex format: {}", tpmOwnerPass);
                throw new IllegalArgumentException(String.format("tpmOwnerPass is not in hex format: %s", tpmOwnerPass));
            }
            //String tpmNvramPass = TAConfig.getConfiguration().getString("TpmNvramAuth");
            log.debug("running command tpm_nvdefine -i " + index + " -s 0x14 -x -aXXXX -oXXXX --permissions=AUTHWRITE");
            String[] variables = { "tpmOwnerPass=" + tpmOwnerPass, "NvramPassword=" + NvramPassword };
            CommandUtil.runCommand("tpm_nvdefine -i " + index + " -s 0x14 -x -t -aNvramPassword -otpmOwnerPass --permissions=AUTHWRITE", variables);
        }catch(TAException ex) {
                log.error("error writing to nvram, " + ex.getMessage() );
                throw ex;
        }
        return true;
    }
    
    private boolean releaseIndex() throws TAException, IOException {
        try {
            String tpmOwnerPass = TAConfig.getConfiguration().getString("tpm.owner.secret");
            if (!HexUtil.isHex(tpmOwnerPass)) {
                log.error("tpmOwnerPass is not in hex format: {}", tpmOwnerPass);
                throw new IllegalArgumentException(String.format("tpmOwnerPass is not in hex format: %s", tpmOwnerPass));
            }
            log.debug("running command tpm_nvrelease -x -t -i " + index + " -oXXXX");
            String[] variables = { "tpmOwnerPass=" + tpmOwnerPass };
            CommandUtil.runCommand("tpm_nvrelease -x -t -i " + index + " -otpmOwnerPass", variables);
        }catch(TAException ex) {
                log.error("error releasing nvram index, " + ex.getMessage() );
                throw ex;
        }
        return true;
    }
    
    private String generateRandomPass() {
       return RandomUtil.randomHexString(20);
    }
    
    private boolean indexExists() throws TAException, IOException {     
        try {
            CommandResult result = CommandUtil.runCommand("tpm_nvinfo -i " + index);
            if (result != null && result.getStdout() != null) {
                if(result.getStdout().contains("NVRAM index")) 
                    return true;
            }
        }catch(TAException ex) {
                log.error("error writing to nvram, " + ex.getMessage() );
                throw ex;
        }
        return false;
    }
}
