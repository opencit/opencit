package com.intel.mtwilson.trustagent.vrtmclient;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.intel.mtwilson.trustagent.vrtmclient.xml.MethodResponse;
import com.intel.mtwilson.trustagent.vrtmclient.xml.Param;
import com.intel.mtwilson.trustagent.vrtmclient.xml.Value;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import javax.xml.bind.DatatypeConverter;

public class RPClient {
        private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RPClient.class);
	private InetSocketAddress rpcoreEndpoint;
        private Socket rpSock;  

        public InetSocketAddress getRpcoreEndpoint() {
            return rpcoreEndpoint;
        }

        public void setRpcoreEndpoint(String hostName, int port) {
            this.rpcoreEndpoint = new InetSocketAddress(hostName, port);
        }

        public Socket getRpSock() {
            return rpSock;
        }

        public void setRpSock() {
            this.rpSock = new Socket();
        }

	public RPClient(String hostName, int port){
		this.rpcoreEndpoint = new InetSocketAddress(hostName, port);
		this.rpSock = new Socket();
	}
	
	public TCBuffer send(TCBuffer outTCBuffer) throws IOException {
		if(!rpSock.isConnected()){
			rpSock.connect(rpcoreEndpoint);
		}
		ByteArrayOutputStream rpOutStream = new ByteArrayOutputStream();
		/*String rpId = String.valueOf(outTCBuffer.getRpId());
		byte[] arr = rpId.getBytes();
		rpOutStream.write(arr);
		rpOutStream.write('\0');*/

		OutputStream rpcoreOut = rpSock.getOutputStream();
		rpOutStream.writeTo(rpcoreOut);
		rpOutStream.flush();
		rpcoreOut.flush();
		
		outTCBuffer.serializeTCBuffer(rpcoreOut);
		
		InputStream rpcoreIn = rpSock.getInputStream();
		TCBuffer inTCBuffer = new TCBuffer();
		inTCBuffer.deSerializeTCBuffer(rpcoreIn);
		
		return inTCBuffer;
	}
	
	public void close(){
		try{
			rpSock.shutdownOutput();
			rpSock.shutdownInput();
			rpSock.close();
		}catch(Exception e){
                    log.error("Error during socket close.", e);
		}
	}
        
        // get the vm status from vrtm
        public boolean getVmStatus(String vmInstanceId) throws IOException {
            boolean retStatus = false;
            
            //Format xml request payload 
            String xmlRPCBlob=  "<?xml version='1.0'?>" 
                            + "<methodCall>"
                            + "<methodName>get_verification_status</methodName>"
                            + 	"<params>"
                            +		"<param>"
                            +			"<value><string>%s</string></value>"
                            +		"</param>"
                            +	"</params>"
                            + "</methodCall>";
            // Replace the %s of xmlRPCBlob by VMUUID, rpcore accept all method input arguments in base64 format
            String base64InputArgument = String.format(xmlRPCBlob, DatatypeConverter.printBase64Binary((vmInstanceId).getBytes()));
            log.debug("Sending {}", base64InputArgument);

            TCBuffer tcBuffer = Factory.newTCBuffer(RPCCall.IS_VM_VERIFIED);	// Formuate tcbuffer structure
            if (tcBuffer != null) {
                tcBuffer.setRPCPayload(base64InputArgument.getBytes());

                TCBuffer resultTcb = send(tcBuffer);    // send tcBuffer to rpcore 
                /* Sample Output:
                 <?xml version='1.0'?>
                    <methodResponse>
                        <params>
                            <param>
                                <value><string>MQ==</string></value>
                            </param>
                        </params>
                    </methodResponse>
                //decode MQ== to get vm status
                */

                if (resultTcb.getRPCPayloadSize() != 0) {
                    String xml = resultTcb.getRPCPayload();
                    log.debug("Method response: {}", xml);

                    XmlMapper mapper = new XmlMapper();
                    MethodResponse response = mapper.readValue(xml, MethodResponse.class);
                    Param param[] = response.getParams();
                    Value value = param[0].getValue();
                    byte[] retBytes = DatatypeConverter.parseBase64Binary(value.getString());
                    String retValue = new String(retBytes, "UTF-8");

                    log.debug("vrtm return value: {}", retValue);  
                    if (retValue.equals("1"))
                        retStatus = true;
                }
            }
            
            return retStatus;
        }
        
        public String getVMAttestationReportPath(String vmInstanceId, String nonce) throws IOException {
            
            //Format xml request payload 
            String xmlRPCBlob=  "<?xml version='1.0'?>" 
                            + "<methodCall>"
                            + "<methodName>get_verification_status</methodName>"
                            + 	"<params>"
                            +		"<param>"
                            +			"<value><string>%s</string></value>"
                            +		"</param>"
                            +		"<param>"
                            +			"<value><string>%s</string></value>"
                            +		"</param>"
                            +	"</params>"
                            + "</methodCall>";
            String base64InputArgument = String.format(xmlRPCBlob, DatatypeConverter.printBase64Binary((vmInstanceId).getBytes()), 
                    DatatypeConverter.printBase64Binary((nonce).getBytes()));
            log.debug("Sending {}", base64InputArgument);

            TCBuffer tcBuffer = Factory.newTCBuffer(RPCCall.GET_VM_ATTESTATION_REPORT_PATH);	// Formuate tcbuffer structure
            if (tcBuffer != null) {
                tcBuffer.setRPCPayload(base64InputArgument.getBytes());

                TCBuffer resultTcb = send(tcBuffer);    // send tcBuffer to rpcore 

                if (resultTcb.getRPCPayloadSize() != 0) {
                    String xml = resultTcb.getRPCPayload();
                    log.debug("Method response: {}", xml);

                    XmlMapper mapper = new XmlMapper();
                    MethodResponse response = mapper.readValue(xml, MethodResponse.class);
                    Param param[] = response.getParams();
                    Value value = param[0].getValue();
                    byte[] retBytes = DatatypeConverter.parseBase64Binary(value.getString());
                    String retValue = new String(retBytes, "UTF-8");

                    log.debug("vrtm return value: {}", retValue);  
                    return retValue;
                } else {
                    return null;
                }
            }
            return null;
        }
}
