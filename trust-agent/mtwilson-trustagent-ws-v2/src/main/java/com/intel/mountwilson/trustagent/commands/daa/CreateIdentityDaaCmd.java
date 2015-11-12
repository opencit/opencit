package com.intel.mountwilson.trustagent.commands.daa;

import com.intel.dcsg.cpg.io.FileResource;
import com.intel.mountwilson.common.ErrorCode;
import com.intel.mountwilson.common.ICommand;
import com.intel.mountwilson.common.TAException;
import com.intel.mountwilson.trustagent.data.TADataContext;
import com.intel.mtwilson.util.exec.EscapeUtil;
import com.intel.mtwilson.util.exec.ExecUtil;
import com.intel.mtwilson.util.exec.Result;
import java.io.File;
import java.io.InputStream;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.io.IOUtils;

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
            String ekCertFileName = EscapeUtil.doubleQuoteEscapeShellArgument(context.getEKCertFileName());
            CommandLine command1 = new CommandLine("/opt/trustagent/bin/getcert");  // safe; no arguments involved in this command line
            command1.addArgument(ekCertFileName);
            Result result1 = ExecUtil.execute(command1);
            if (result1.getExitCode() != 0) {
                log.error("Error running command [{}]: {}", command1.getExecutable(), result1.getStderr());
                throw new TAException(ErrorCode.ERROR, result1.getStderr());
            }
            log.debug("command stdout: {}", result1.getStdout());
            log.info( "Extracted EK Certificate");
	
            // prepare the AIK for the DAA challenge
            CommandLine command2 = new CommandLine("/opt/trustagent/bin/aikpublish"); // safe; no arguments involved in this command line
            command2.addArgument(ekCertFileName);
            command2.addArgument(EscapeUtil.doubleQuoteEscapeShellArgument(context.getAikCertFileName()));
            //command2.addArgument(EscapeUtil.doubleQuoteEscapeShellArgument(context.getAikBlobFileName()));
            Result result2 = ExecUtil.execute(command2);
            if (result2.getExitCode() != 0) {
                log.error("Error running command [{}]: {}", command2.getExecutable(), result2.getStderr());
                throw new TAException(ErrorCode.ERROR, result2.getStderr());
            }
            log.debug("command stdout: {}", result2.getStdout());
            
            log.info( "Created AIK Blob and AIK Certificate for DAA");

            // read the AIK certificate
            try (InputStream in = new FileResource(new File(context.getAikCertFileName())).getInputStream()) {
                context.setAIKCertificate(IOUtils.toString(in));
            }
            log.debug("AIK Certificate Read to memory - {}", context.getAikCertFileName());

        } catch (Exception e) {
            throw new TAException(ErrorCode.COMMAND_ERROR, "Error while creating identity for DAA: "+e.toString());
        }
    }

}
