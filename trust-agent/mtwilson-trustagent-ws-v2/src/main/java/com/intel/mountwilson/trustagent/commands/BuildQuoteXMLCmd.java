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
            //Adding new function to insert inside Quote the IMA measurements
            tpmQuoteResponse.SimpleSnapshotObject.addAll(readValuesFromIma());
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
    
        public ArrayList<Objects> readValuesFromIma () {
    	String[] ima_parts = new String[5];
    	String[] parts = new String[2];
    	ArrayList<Objects> list = new ArrayList<>();
		String s;
		try {
            //Opening IMA runtime measurement list
			FileReader ima = new FileReader("/sys/kernel/security/ima/ascii_runtime_measurements");
			BufferedReader row = new BufferedReader(ima);
			while (true) {
				s = row.readLine();
				if (s==null)
					break;
				if (!s.startsWith(defaultPCRnumber))
					continue;

				ima_parts=s.split(" ");
				Objects value = new Objects();
				value.setHash(ima_parts[template_hash]);
				value.setPcrindex(defaultPCRnumber);
				value.setEventtype(defaultEventType);
                //Managing both types of IMA templates (ima and ima-ng)
				if (ima_parts[3].startsWith("sha1") || ima_parts[3].startsWith("sha256")) {
					String filedata_hash[] = new String[2];
					filedata_hash = ima_parts[3].split(":");
					byte[] data_value = filedata_hash[1].getBytes("UTF-8");
					value.setEventdigest(Base64.encodeBase64String(data_value));
				} else {
					byte[] data_value = ima_parts[filedata_hash].getBytes("UTF-8");
					value.setEventdigest(Base64.encodeBase64String(data_value));
				}
				byte[] data_name = ima_parts[filename_hint].getBytes("UTF-8");
				value.setEventdata(Base64.encodeBase64String(data_name));

				list.add(value);
			}
		
			row.close();
			ima.close();
			return list;
			
		} catch (IOException e) {
            //If IMA is not available the list will contain only the item shown below
			Objects value = new Objects();
			value.setPcrindex(defaultPCRnumber);
			value.setHash("not available");
			value.setEventtype(defaultEventType);
			value.setEventdata("not available");
			value.setEventdigest("not available");
			list.add(value);
    	return list;
		}	
    }
}
