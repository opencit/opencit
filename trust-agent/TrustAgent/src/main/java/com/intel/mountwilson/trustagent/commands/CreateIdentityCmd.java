/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mountwilson.trustagent.commands;

import com.intel.mountwilson.common.CommandUtil;
import com.intel.mountwilson.common.ErrorCode;
import com.intel.mountwilson.common.ICommand;
import com.intel.mountwilson.common.TAException;
import com.intel.mountwilson.his.helper.CreateIdentity;
import com.intel.mountwilson.trustagent.data.TADataContext;
import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dsmagadX
 */
public class CreateIdentityCmd implements ICommand {

    Logger log = LoggerFactory.getLogger(getClass().getName());
    private TADataContext context;

    public static void main(String[] args) throws TAException {
        TADataContext ctx = new TADataContext();
        CreateIdentityCmd cmd = new CreateIdentityCmd(ctx);
        cmd.execute();
    }

    public CreateIdentityCmd(TADataContext context) {
        this.context = context;
    }

    @Override
    public void execute() throws TAException {
        try {

            // Let us first check if the AIK is already created or not. If it already exists, then we do not need to create the AIK again.
            File aikCertFile = new File(context.getAikCertFileName());
            if (aikCertFile.exists()) {
                log.debug("AIK Certificate already exists at ", context.getAikCertFileName());
                log.info("New AIK certificate will not be created.");
            } else {
                // this will create the AIK in the configured folder
                CreateIdentity.createIdentity();

            }

            context.setAIKCertificate(CommandUtil.readCertificate(context.getAikCertFileName()));

            log.debug("AIK Certificate Read to memory - {}", context.getAikCertFileName());

        } catch (Exception e) {

            throw new TAException(ErrorCode.COMMAND_ERROR, "Error while creating identity.", e);
        }
    }
}
