package com.intel.mountwilson.trustagent.commands;

import com.intel.mountwilson.common.CommandUtil;
import com.intel.mountwilson.common.ICommand;
import com.intel.mountwilson.trustagent.data.TADataContext;
import java.util.Date;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dsmagadX
 */
public class BuildQuoteXMLCmd implements ICommand {
    Logger log = LoggerFactory.getLogger(getClass().getName());
    private TADataContext context = null;

    public BuildQuoteXMLCmd(TADataContext context) {
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
                + "<quote>" + new String(Base64.encodeBase64(context.getTpmQuote())) + "</quote>"
                +  "<eventLog>" + context.getModules() + "</eventLog>" //To add the module information into the response.
                + "</client_request>";

        log.debug("Final content that is being sent back to the AS is : " + responseXML);
        context.setResponseXML(responseXML);
    }
}
