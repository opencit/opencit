package com.intel.mountwilson.trustagent.commands.daa;



import com.intel.mountwilson.common.CommandUtil;
import com.intel.mountwilson.common.ErrorCode;
import com.intel.mountwilson.common.ICommand;
import com.intel.mountwilson.common.TAException;
import com.intel.mountwilson.trustagent.data.TADataContext;
import java.io.File;
import java.io.FileOutputStream;

import org.apache.commons.io.IOUtils;
import org.codehaus.plexus.util.IOUtil;
import org.eclipse.persistence.tools.file.FileUtil;
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
            // write challenge to file
        	FileOutputStream out = new FileOutputStream(new File(context.getDaaChallengeFileName()));
            IOUtil.copy(context.getDaaChallenge(), out);
            IOUtils.closeQuietly(out);
            
            // prepare response to challenge
            CommandUtil.runCommand(String.format("aikrespond %s %s %s", context.getAikBlobFileName(), context.getDaaChallengeFileName(), context.getDaaResponseFileName())); // safe; no arguments involved in this command line
            log.info( "Created response for DAA challenge");

            // read response and delete the response file
            context.setDaaResponse(CommandUtil.readfile(context.getDaaResponseFileName()));
            log.debug( "DAA Response read to memory - {}", context.getDaaResponseFileName());
            FileUtil.delete(new File(context.getDaaResponseFileName()));
            log.debug("DAA Response file deleted - {}", context.getDaaResponseFileName());
            
        } catch (Exception e) {
            throw new TAException(ErrorCode.COMMAND_ERROR, "Error while preparing DAA challenge response: "+e.toString());
        }
    }

}
