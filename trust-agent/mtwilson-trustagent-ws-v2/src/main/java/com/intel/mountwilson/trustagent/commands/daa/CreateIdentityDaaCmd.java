package com.intel.mountwilson.trustagent.commands.daa;



import com.intel.mountwilson.common.CommandUtil;
import com.intel.mountwilson.common.ErrorCode;
import com.intel.mountwilson.common.ICommand;
import com.intel.mountwilson.common.TAException;
import com.intel.mountwilson.trustagent.data.TADataContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
public class CreateIdentityDaaCmd implements ICommand {

    Logger log = LoggerFactory.getLogger(getClass().getName());
    private TADataContext context;

    public CreateIdentityDaaCmd(TADataContext context) {
        this.context = context;
    }

    @Override
    public void execute() throws TAException {
        try {
            // create the AIK blob and certificate
//            CommandUtil.runCommand(String.format("identity INTEL %s %s", context.getAikBlobFileName(), context.getAikCertFileName() )); // safe; no arguments involved in this command line
//            log.log(Level.INFO, "Created AIK Blob and AIK Certificate");
            
            // extract the EK
            String ekCertFileName = CommandUtil.doubleQuoteEscapeShellArgument(context.getEKCertFileName());
            CommandUtil.runCommand(String.format("getcert %s", ekCertFileName)); // safe; no arguments involved in this command line
            log.info( "Extracted EK Certificate");
	
            // prepare the AIK for the DAA challenge
            CommandUtil.runCommand(String.format("aikpublish %s %s", ekCertFileName,
                    CommandUtil.doubleQuoteEscapeShellArgument(context.getAikCertFileName()),
                    CommandUtil.doubleQuoteEscapeShellArgument(context.getAikBlobFileName()))); // safe; no arguments involved in this command line
            log.info( "Created AIK Blob and AIK Certificate for DAA");

            // read the AIK certificate
            context.setAIKCertificate(CommandUtil.readCertificate(context.getAikCertFileName()));
            log.debug("AIK Certificate Read to memory - {}", context.getAikCertFileName());

        } catch (Exception e) {
            throw new TAException(ErrorCode.COMMAND_ERROR, "Error while creating identity for DAA: "+e.toString());
        }
    }

}
