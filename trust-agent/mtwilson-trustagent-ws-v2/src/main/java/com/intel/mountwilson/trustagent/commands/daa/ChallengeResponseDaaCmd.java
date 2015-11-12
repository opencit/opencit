package com.intel.mountwilson.trustagent.commands.daa;



import com.intel.dcsg.cpg.io.FileResource;
import com.intel.mountwilson.common.ErrorCode;
import com.intel.mountwilson.common.ICommand;
import com.intel.mountwilson.common.TAException;
import com.intel.mountwilson.trustagent.data.TADataContext;
import com.intel.mtwilson.util.exec.EscapeUtil;
import com.intel.mtwilson.util.exec.ExecUtil;
import com.intel.mtwilson.util.exec.Result;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
public class ChallengeResponseDaaCmd implements ICommand {

    Logger log = LoggerFactory.getLogger(getClass().getName());
    private TADataContext context;

    public ChallengeResponseDaaCmd(TADataContext context) {
        this.context = context;
    }

    @Override
    public void execute() throws TAException {
        try {
            try (FileOutputStream out = new FileOutputStream(new File(context.getDaaChallengeFileName()))) {
                IOUtils.copy(new ByteArrayInputStream(context.getDaaChallenge()), out);
            }
            // prepare response to challenge
            CommandLine command = new CommandLine("/opt/trustagent/bin/aikrespond");  // safe; no arguments involved in this command line
            command.addArgument(EscapeUtil.doubleQuoteEscapeShellArgument(context.getAikBlobFileName()));
            command.addArgument(EscapeUtil.doubleQuoteEscapeShellArgument(context.getDaaChallengeFileName()));
            command.addArgument(EscapeUtil.doubleQuoteEscapeShellArgument(context.getDaaResponseFileName()));
            Result result = ExecUtil.execute(command);
            if (result.getExitCode() != 0) {
                log.error("Error running command [{}]: {}", command.getExecutable(), result.getStderr());
                throw new TAException(ErrorCode.ERROR, result.getStderr());
            }
            log.debug("command stdout: {}", result.getStdout());
            log.info( "Created response for DAA challenge");

            // read response and delete the response file
            try (InputStream in = new FileResource(new File(context.getDaaResponseFileName())).getInputStream()) {
                context.setDaaResponse(IOUtils.toByteArray(in));
            }
            log.debug( "DAA Response read to memory - {}", context.getDaaResponseFileName());
            (new File(context.getDaaResponseFileName())).delete();
            log.debug("DAA Response file deleted - {}", context.getDaaResponseFileName());
            
        } catch (IOException | TAException e) {
            throw new TAException(ErrorCode.COMMAND_ERROR, "Error while preparing DAA challenge response: "+e.toString());
        }
    }

}
