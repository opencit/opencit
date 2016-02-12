/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mountwilson.trustagent.commands;

import com.intel.mtwilson.codec.HexUtil;
import com.intel.mountwilson.common.CommandUtil;
import com.intel.mountwilson.common.ErrorCode;
import com.intel.mountwilson.common.ICommand;
import com.intel.mountwilson.common.TAException;
import com.intel.mountwilson.trustagent.data.TADataContext;
import com.intel.mtwilson.Folders;
import com.intel.mtwilson.util.exec.EscapeUtil;
import java.io.File;
import java.util.regex.Pattern;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dsmagadX
 */


public class GenerateQuoteCmd implements ICommand {
    Logger log = LoggerFactory.getLogger(getClass().getName());
    private Pattern PCR_LIST_SSV = Pattern.compile("^[0-9][0-9 ]*$");
    
    private TADataContext context;

    
    public GenerateQuoteCmd(TADataContext context) {
        this.context = context;
    }
    
    protected static byte[] hexStringToByteArray(String s) {
        int len = s.length();
            
        
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }
    
    public static String byteArrayToHexString(byte[] b) {
        StringBuffer sb = new StringBuffer();
        String returnStr = "";
        for (int i = 0; i < b.length; i++) {
                String singleByte = Integer.toHexString(b[i] & 0xff);
                if (singleByte.length() != 2) singleByte = "0" + singleByte;
//			returnStr += singleByte;
                returnStr = sb.append(singleByte).toString();
        }
        return returnStr;
    }
    
    @Override
    public void execute() throws TAException {
        String identityAuthKey = context.getIdentityAuthKey();
        String selectedPcrs = context.getSelectedPCRs();
        
        if (!HexUtil.isHex(identityAuthKey)) {
            log.error("Aik secret password is not in hex format: {}", identityAuthKey);
            throw new IllegalArgumentException(String.format("Aik secret password is not in hex format."));
        }
        if (!PCR_LIST_SSV.matcher(selectedPcrs).matches()) {
            log.error("Selected PCRs do not match correct format: {}", selectedPcrs);
            throw new IllegalArgumentException(String.format("Selected PCRs do not match correct format."));
        }
                
        String osName = context.getOsName();
        String commandLine = "";
        String keyName = "HIS_Identity_Key";
        byte[] nonce = Base64.decodeBase64(context.getNonce());

        if (osName.toLowerCase().contains("windows")) {
            
            // In the case of Windows, we import the AIK first
            String aikOpaqueFile = Folders.configuration() + File.separator + "aik.opaque";
            log.debug("AikOpaqueFile: " + aikOpaqueFile);

            String aikImportCmdLine = String.format("tpmtool.exe importaik \"%s\" HIS_Identity_Key", aikOpaqueFile);
            log.debug("cmd to run: ", aikImportCmdLine);
            try {
                CommandUtil.runCommand(aikImportCmdLine);
            }catch (Exception e) {
                throw new TAException(ErrorCode.COMMAND_ERROR, "Error while importing AIK" ,e);
            }
            
            // format is: "tpmtool.exe [aik name] {attestation file} {nonce} {aikauth}" 
            commandLine = String.format("tpmtool.exe aikquote %s %s %s", // skip the authkey for now
                byteArrayToHexString(keyName.getBytes()),
                byteArrayToHexString(context.getQuoteFileName().getBytes()),
                byteArrayToHexString(nonce));
            /*
            commandLine = String.format("tpmtool.exe %s %s %s %s",
                byteArrayToHexString(keyName.getBytes()),
                EscapeUtil.doubleQuoteEscapeShellArgument(context.getQuoteFileName()),
                byteArrayToHexString(nonce),
                identityAuthKey);
            */
        } else {
            commandLine = String.format("aikquote -p %s -c %s %s %s %s",
                identityAuthKey,
                EscapeUtil.doubleQuoteEscapeShellArgument(context.getNonceFileName()),
                EscapeUtil.doubleQuoteEscapeShellArgument(context.getAikBlobFileName()),
                selectedPcrs,
                EscapeUtil.doubleQuoteEscapeShellArgument(context.getQuoteFileName())); // these are configured (trusted), they are NOT user input, but if that changes you can do CommandArg.escapeFilename(...)
        }
        
        //debug
        log.debug("Command Line to be executed: " + commandLine);
        
        try {
            CommandUtil.runCommand(commandLine);
			log.debug("Create the quote {} ",
					context.getQuoteFileName());
			context.setTpmQuote(CommandUtil.readfile(context.getQuoteFileName()));
		}catch (Exception e) {
			throw new TAException(ErrorCode.COMMAND_ERROR, "Error while generating quote" ,e);
		}

    }
    
}
