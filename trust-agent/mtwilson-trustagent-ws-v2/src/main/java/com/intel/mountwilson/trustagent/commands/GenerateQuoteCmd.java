/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mountwilson.trustagent.commands;

import com.intel.dcsg.cpg.io.HexUtil;
import com.intel.mountwilson.common.CommandUtil;
import com.intel.mountwilson.common.ErrorCode;
import com.intel.mountwilson.common.ICommand;
import com.intel.mountwilson.common.TAException;
import com.intel.mountwilson.trustagent.data.TADataContext;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dsmagadX
 */


public class GenerateQuoteCmd implements ICommand {
    Logger log = LoggerFactory.getLogger(getClass().getName());
    
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
    
    @Override
    public void execute() throws TAException {
        String identityAuthKey = context.getIdentityAuthKey();
        String nonceFileName = context.getNonceFileName();
        String aikBlobFileName = context.getAikBlobFileName();
        String selectedPcrs = context.getSelectedPCRs();
        String quoteFileName = context.getQuoteFileName();
        
        if (!HexUtil.isHex(identityAuthKey)) {
            log.error("identityAuthKey is not in hex format: {}", identityAuthKey);
            throw new IllegalArgumentException(String.format("identityAuthKey is not in hex format: %s", identityAuthKey));
        }
        if (!CommandUtil.containsSingleQuoteShellSpecialCharacters(nonceFileName)) {
            log.warn("Escaping special characters in nonceFileName: {}", nonceFileName);
            nonceFileName = CommandUtil.escapeShellArgument(nonceFileName);
        }
        if (!CommandUtil.containsSingleQuoteShellSpecialCharacters(aikBlobFileName)) {
            log.warn("Escaping special characters in aikBlobFileName: {}", aikBlobFileName);
            aikBlobFileName = CommandUtil.escapeShellArgument(aikBlobFileName);
        }
        if (!selectedPcrs.matches(Pattern.compile("^[0-9 ]*$").pattern())) {
            log.error("selectedPcrs do not match correct format: {}", selectedPcrs);
            throw new IllegalArgumentException(String.format("selectedPcrs do not match correct format: %s", selectedPcrs));
        }
        if (!CommandUtil.containsSingleQuoteShellSpecialCharacters(quoteFileName)) {
            log.warn("Escaping special characters in quoteFileName: {}", quoteFileName);
            quoteFileName = CommandUtil.escapeShellArgument(quoteFileName);
        }
        
        String commandLine = String.format("aikquote -p %s -c %s %s %s %s",
                identityAuthKey,
                nonceFileName,
                aikBlobFileName,
                selectedPcrs,
                quoteFileName); // these are configured (trusted), they are NOT user input, but if that changes you can do CommandArg.escapeFilename(...)
        

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
