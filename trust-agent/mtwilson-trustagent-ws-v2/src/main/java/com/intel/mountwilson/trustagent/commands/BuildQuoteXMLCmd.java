package com.intel.mountwilson.trustagent.commands;

import com.intel.dcsg.cpg.x509.X509Util;
import com.intel.mountwilson.common.CommandUtil;
import com.intel.mountwilson.common.ICommand;
import com.intel.mountwilson.common.TAException;
import com.intel.mountwilson.trustagent.data.TADataContext;
import com.intel.mountwilson.common.ErrorCode;
import com.intel.mtwilson.trustagent.model.TpmQuoteResponse;
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
        try {
            TpmQuoteResponse tpmQuoteResponse = new TpmQuoteResponse();
            tpmQuoteResponse.timestamp = System.currentTimeMillis();
            tpmQuoteResponse.clientIp = CommandUtil.getHostIpAddress();
            tpmQuoteResponse.errorCode = String.valueOf(context.getErrorCode().getErrorCode());
            tpmQuoteResponse.errorMessage = context.getErrorCode().getMessage();
            tpmQuoteResponse.aik = X509Util.decodePemCertificate(context.getAIKCertificate());
            tpmQuoteResponse.quote = context.getTpmQuote();
            tpmQuoteResponse.eventLog = context.getModules(); //base64-encoded  xml which the caller will interpret
            tpmQuoteResponse.tcbMeasurement = context.getTcbMeasurement();
            tpmQuoteResponse.selectedPcrBanks = context.getSelectedPcrBanks();
            context.setTpmQuoteResponse(tpmQuoteResponse);
        }
        catch(Exception e) {
//            throw new TAException(ErrorCode.ERROR, "Cannot generate tpm quote response", e);
            throw new RuntimeException("Cannot generate tpm quote response", e);
        }
        /*
        String responseXML =
                "<tpm_quote_response> "
                + "<timestamp>" + new Date(System.currentTimeMillis()).toString() + "</timestamp>"
                + "<client_ip>" + StringEscapeUtils.escapeXml(CommandUtil.getHostIpAddress()) + "</client_ip>"
                + "<error_code>" + context.getErrorCode().getErrorCode() + "</error_code>"
                + "<error_message>" + StringEscapeUtils.escapeXml(context.getErrorCode().getMessage()) + "</error_message>"
                + "<aikcert>" + StringEscapeUtils.escapeXml(context.getAIKCertificate()) + "</aikcert>"
                + "<quote>" + new String(Base64.encodeBase64(context.getTpmQuote())) + "</quote>"
                +  "<event_log>" + context.getModules() + "</event_log>" //To add the module information into the response.
                + "</tpm_quote_response>";
        log.debug("Final content that is being sent back to the AS is : " + responseXML);
            context.setResponseXML(responseXML);
*/
    }
}
