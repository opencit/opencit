/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mountwilson.trustagent.commands;

import com.intel.mountwilson.common.CommandUtil;
import com.intel.mountwilson.common.ICommand;
import com.intel.mountwilson.trustagent.data.TADataContext;
import java.util.Date;
import org.apache.commons.lang.StringEscapeUtils;

/**
 *
 * @author dsmagadX
 */
public class BuildIdentityXMLCmd implements ICommand {

    private TADataContext context = null;

    public BuildIdentityXMLCmd(TADataContext context) {
        this.context = context;
    }

    @Override
    public void execute() {

        String responseXML =
                "<client_request> "
                + "<timestamp>" + new Date(System.currentTimeMillis()).toString() + "</timestamp>"
                + "<clientIp>" + StringEscapeUtils.escapeXml(CommandUtil.getHostIpAddress()) + "</clientIp>"
                + "<error_code>" + context.getErrorCode().getErrorCode() + "</error_code>"
                + "<error_message>" + StringEscapeUtils.escapeXml(context.getErrorCode().getMessage()) + "</error_message>"
                + "<aikcert>" + StringEscapeUtils.escapeXml(context.getAIKCertificate()) + "</aikcert>"
                + "</client_request>";

        context.setResponseXML(responseXML);
    }
}
