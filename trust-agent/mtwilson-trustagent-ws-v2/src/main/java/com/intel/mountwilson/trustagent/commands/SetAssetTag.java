/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.intel.mountwilson.trustagent.commands;

import com.intel.mountwilson.common.ICommand;
import com.intel.mountwilson.common.TAException;
import com.intel.mountwilson.trustagent.data.TADataContext;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.intel.dcsg.cpg.crypto.RandomUtil;
import com.intel.mtwilson.codec.HexUtil;
import com.intel.mountwilson.common.ErrorCode;
import com.intel.mountwilson.common.TAConfig;
import com.intel.mtwilson.util.exec.EscapeUtil;
import com.intel.mtwilson.util.exec.ExecUtil;
import com.intel.mtwilson.util.exec.Result;
import java.io.File;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.exec.CommandLine;



/**
 *
 * @author stdalex
 */
@Deprecated
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
            log.debug("running command tpm_nvwrite -x -i " + index + " -pXXXX -f " + filename);
            Map<String, String> variables = new HashMap<>();
            variables.put("NvramPassword", NvramPassword);
            CommandLine command = new CommandLine("/opt/trustagent/bin/tpm_nvwrite");
            command.addArgument("-x");
            command.addArgument("-t");
            command.addArgument("-pNvramPassword");
            command.addArgument(String.format("-i %s", index), false);
            command.addArgument("-f");
            command.addArgument(EscapeUtil.doubleQuoteEscapeShellArgument(filename));
            Result result = ExecUtil.execute(command, variables);
            if (result.getExitCode() != 0) {
                log.error("Error running command [{}]: {}", command.getExecutable(), result.getStderr());
                throw new TAException(ErrorCode.ERROR, result.getStderr());
            }
            log.debug("command stdout: {}", result.getStdout());

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
            String filename = EscapeUtil.doubleQuoteEscapeShellArgument(String.format("/tmp/tagent_tag_%s", getRandomHexString(8)));
            CommandLine command = new CommandLine("/opt/trustagent/bin/hex2bin");
            command.addArgument(assetTagHash);
            command.addArgument(filename);
            Result result = ExecUtil.execute(command);
            if (result.getExitCode() != 0) {
                log.error("Error running command [{}]: {}", command.getExecutable(), result.getStderr());
                throw new TAException(ErrorCode.ERROR, result.getStderr());
            }
            log.debug("command stdout: {}", result.getStdout());
            
            return filename;
        }catch(TAException ex) {
                log.error("error writing hash to file, " + ex.getMessage() );
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
            Map<String, String> variables = new HashMap<>();
            variables.put("tpmOwnerPass", tpmOwnerPass);
            variables.put("NvramPassword", NvramPassword);
            CommandLine command = new CommandLine("/opt/trustagent/bin/tpm_nvdefine");
            command.addArgument("-x");
            command.addArgument("-t");
            command.addArgument("-aNvramPassword");
            command.addArgument("-otpmOwnerPass");
            command.addArgument("--permissions=AUTHWRITE");
            command.addArgument("-s 0x14", false);
            command.addArgument(String.format("-i %s", index), false);
            Result result = ExecUtil.execute(command, variables);
            if (result.getExitCode() != 0) {
                log.error("Error running command [{}]: {}", command.getExecutable(), result.getStderr());
                throw new TAException(ErrorCode.ERROR, result.getStderr());
            }
            log.debug("command stdout: {}", result.getStdout());
        }catch(TAException ex) {
                log.error("error creating nvram index, " + ex.getMessage() );
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
            Map<String, String> variables = new HashMap<>();
            variables.put("tpmOwnerPass", tpmOwnerPass);
            CommandLine command = new CommandLine("/opt/trustagent/bin/tpm_nvrelease");
            command.addArgument("-x");
            command.addArgument("-t");
            command.addArgument("-otpmOwnerPass");
            command.addArgument(String.format("-i %s", index), false);
            Result result = ExecUtil.execute(command, variables);
            if (result.getExitCode() != 0) {
                log.error("Error running command [{}]: {}", command.getExecutable(), result.getStderr());
                throw new TAException(ErrorCode.ERROR, result.getStderr());
            }
            log.debug("command stdout: {}", result.getStdout());
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
            CommandLine command = new CommandLine("/opt/trustagent/bin/tpm_nvinfo");
            command.addArgument(String.format("-i %s", index), false);
            Result result = ExecUtil.execute(command);
            if (result.getExitCode() != 0) {
                log.error("Error running command [{}]: {}", command.getExecutable(), result.getStderr());
                throw new TAException(ErrorCode.ERROR, result.getStderr());
            }
            log.debug("command stdout: {}", result.getStdout());
            
            if (result.getStdout() != null && result.getStdout().contains("NVRAM index"))
                return true;
        }catch(TAException ex) {
                log.error("error checking if nvram index exists, " + ex.getMessage() );
                throw ex;
        }
        return false;
    }
}
