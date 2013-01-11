package com.intel.mountwilson.as.helper;

/**
 *
 * @author dsmagadX
 */

//import com.intel.mountwilson.as.common.*;
import com.intel.mountwilson.as.common.ASConfig;
import com.intel.mountwilson.as.common.ASException;
import com.intel.mountwilson.ta.data.ClientRequestType;

import com.intel.mountwilson.ta.data.quoterequest.QuoteRequest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.bind.Unmarshaller;
import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import com.intel.mtwilson.datatypes.*;
import com.intel.mountwilson.ta.data.daa.response.DaaResponse;
import com.intel.mountwilson.ta.data.hostinfo.HostInfo;
import com.intel.mtwilson.tls.TlsConnection;
import com.intel.mtwilson.tls.TlsPolicy;

import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;

public class TrustAgentSecureClient {
    public static final int DEFAULT_TRUST_AGENT_PORT = 9999;
    public static final String TA_ERROR_CODE = "error_code";
    public static final String TA_ERROR_MESSAGE = "error_message";
    private final Logger log = LoggerFactory.getLogger(getClass());
    private String serverHostname = null;
    private int serverPort = 0;
    private byte[] data;
    private TlsPolicy tlsPolicy;
    
    private static int TIME_OUT = ASConfig.getTrustAgentTimeOutinMilliSecs();

    // Bug #497 commenting out  these two constructors because now Tls Policy is a required argument.
    // XXX this constructor is only used from the unit test class, that's why it allows setting data -  so unit test can mock response from trust agent
    /*
    public TrustAgentSecureClient(String serverHostname, int serverPort, byte[] data) {
        this(serverHostname, serverPort);
        if( data != null ) {
            this.data = Arrays.copyOf(data, data.length);
        }
    }

    public TrustAgentSecureClient(String hostName, int port) {
        this.serverHostname = hostName;
        this.serverPort = port;
        log.info("Connecting to Trust Agent at '{}'", hostName+":"+port);
    } */
    
    public TrustAgentSecureClient(TlsConnection tlsConnection) {
        tlsPolicy = tlsConnection.getTlsPolicy();
        parseConnectionString(tlsConnection.getConnectionString());
    }

    // XXX the ipaddress:port format is also parsed somewhere else in the codebase... need to consolidate here.
    private void parseConnectionString(String connectionString) {
        if( connectionString.startsWith("https") ) {  // format  https://ipAddressOrHostname:port
            try {
                URL url = new URL(connectionString);
                serverHostname = url.getHost();
                serverPort = url.getPort();
                if( serverPort == -1 ) {
                    serverPort = DEFAULT_TRUST_AGENT_PORT;
                }
                return;
            }
            catch(MalformedURLException e) {
                throw new IllegalArgumentException("Invalid Trust Agent connection string: "+connectionString, e);
            }
        }
        if( connectionString.contains(":") ) {
            try {
                String[] parts = connectionString.split(":");
                serverHostname = parts[0];
                serverPort = Integer.valueOf(parts[1]);
            }
            catch(Exception e) {
                throw new IllegalArgumentException("Invalid Trust Agent connection string: "+connectionString, e);
            }
        }
        throw new IllegalArgumentException("Unrecognized Trust Agent connection string format: "+connectionString);
    }
    
    /*
    // XXX this constructor is not used anywhere
    public TrustAgentSecureClient(IPAddress serverIPAddress, int serverPort, byte[] data) { // datatype.IPAddress
        this(serverIPAddress, serverPort);
        if( data != null ) {
            this.data = Arrays.copyOf(data, data.length);
        }
    }

    // XXX this constructor is not used anywhere
    public TrustAgentSecureClient(IPAddress serverIPAddress, int serverPort) { // datatype.IPAddress
        this(serverIPAddress.toString(), serverPort);
    }
    */

    
    private byte[] sendRequestWithSSLSocket() throws NoSuchAlgorithmException, NoSuchAlgorithmException, KeyManagementException, UnknownHostException, IOException {
        log.trace( "Opening connection to {} port {}", new String[]{serverHostname, String.valueOf(serverPort)});
        
        if( data == null ) {
        	throw new IllegalArgumentException("Attempted to send request without data");
        }

        SSLSocketFactory sslsocketfactory = getSSLContext().getSocketFactory();
        SSLSocket sock = (SSLSocket) sslsocketfactory.createSocket();
        
        try {            
            sock.connect(new InetSocketAddress(serverHostname,serverPort), TIME_OUT);
            InputStream sockInput = sock.getInputStream();
            OutputStream sockOutput = sock.getOutputStream();

            log.info("About to start reading/writing to/from socket.");

            byte[] buf = new byte[5000];
                sockOutput.write(data, 0, data.length);
            int bytes_read = sockInput.read(buf);
            log.info( "Received {} bytes to server and received them back again, msg = {}", 
                    new Object[]{bytes_read, StringUtils.replace(new String(buf), "\n", "\n  ")});

            return buf;
        }
        catch(SocketTimeoutException e){
            throw new ASException(e,ErrorCode.AS_TRUST_AGENT_CONNNECT_TIMED_OUT,serverHostname,serverPort,(TIME_OUT/1000));           
        }
        finally {
            sock.close();        
        }
    }
    
    public DaaResponse sendDaaChallenge(String challenge) throws NoSuchAlgorithmException, KeyManagementException, UnknownHostException, JAXBException, IOException {
        this.data = challenge.getBytes();
        byte buf[] = sendRequestWithSSLSocket();
        JAXBContext jc = JAXBContext.newInstance("com.intel.mountwilson.ta.data.daa.response");
        Unmarshaller u = jc.createUnmarshaller();
        JAXBElement po =  (JAXBElement) u.unmarshal(new StringReader(new String(buf).trim()));
        DaaResponse response = (DaaResponse)po.getValue();
        return response;
    }
    
    /**
     * 
     * @return an object representing the RESPONSE from the Trust Agent
     * @throws UnknownHostException if the IP address of the host could not be determined from local hosts file or DNS
     * @throws IOException if there was an error connecting to the host, such as it is not reachable on the network or it dropped the connection
     * @throws JAXBException when the response from the host cannot be interpreted properly
     * @throws NoSuchAlgorithmException 
     * @throws KeyManagementException 
     */
    public ClientRequestType sendQuoteRequest() throws UnknownHostException, IOException, JAXBException, KeyManagementException, NoSuchAlgorithmException  {


        try {
            byte buf[] = sendRequestWithSSLSocket();

            log.info("Unmarshalling to Jaxb object.");
            
            JAXBContext jc = JAXBContext.newInstance("com.intel.mountwilson.ta.data");
            assert jc != null;
            Unmarshaller u = jc.createUnmarshaller();
            assert u != null;
            assert new String(buf) != null;
            JAXBElement po =  (JAXBElement) u.unmarshal(new StringReader(new String(buf).trim()));
            
            assert po != null;
            
            ClientRequestType response = (ClientRequestType)po.getValue();
            
            assert response != null;
            
            checkQuoteError(response);

            log.info("Done reading/writing to/from socket, closing socket.");
            return response;
        } finally {
        }

    }

    private SSLContext getSSLContext() throws NoSuchAlgorithmException, KeyManagementException {
        /*
        javax.net.ssl.TrustManager x509 = new javax.net.ssl.X509TrustManager() {

            @Override
            public void checkClientTrusted(java.security.cert.X509Certificate[] arg0, String arg1) throws java.security.cert.CertificateException {
                log.info("checkClientTrusted. String argument: "+arg1);
                for(java.security.cert.X509Certificate cert : arg0) {
                    log.info("Certificate:");
                    log.info("  Subject: "+cert.getSubjectX500Principal().getName());
                    log.info("  Issued by: "+cert.getIssuerX500Principal().getName());
                    cert.checkValidity();
                }
                return;
            }

            @Override
            public void checkServerTrusted(java.security.cert.X509Certificate[] arg0, String arg1) throws java.security.cert.CertificateException {
                log.info("checkServerTrusted. String argument: "+arg1);
                for(java.security.cert.X509Certificate cert : arg0) {
                    log.info("Certificate:");
                    log.info("  Subject: "+cert.getSubjectX500Principal().getName());
                    log.info("  Issued by: "+cert.getIssuerX500Principal().getName());
                    cert.checkValidity();
                }
                return;
            }

            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
               log.info("getAcceptedIssuers");
               return null;
            }
        };

    	SSLContext ctx = SSLContext.getInstance("SSL");
        ctx.init(null, new javax.net.ssl.TrustManager[]{x509}, null);
        */
    	SSLContext ctx = SSLContext.getInstance("SSL");
        ctx.init(null, new javax.net.ssl.TrustManager[]{ tlsPolicy.getTrustManager() }, null);
        return ctx;
    }

    // XXX TODO  we need to return an X509Certificate here;   if the caller wants it in PEM format they can encode it.  returning a String is ambiguous and leaves open possibiility of parsing errors later. we should catch them here.
    public String getAIKCertificate() {
        try {

            log.info("Sending Generate Identity");
            byte[] identityInput = "<identity_request></identity_request>".getBytes();
            this.data = identityInput;

            ClientRequestType response = sendQuoteRequest();

            String certificate = response.getAikcert();
            
            // TODO:  ensure certificate is propertly formatted.  If missing a line after the header, insert it.  Or decode it, and re-encode as base-64 blob with no line endings.
            
            return certificate;
        }catch(ASException ase){
            throw ase;
        }catch(UnknownHostException e) {
            throw new ASException(e,ErrorCode.AS_HOST_COMMUNICATION_ERROR, this.serverHostname);
        }catch (Exception e) {
            throw new ASException(e);
        }
    }
    
    public ClientRequestType getQuote(String nonce, String pcrList) throws PropertyException, JAXBException, UnknownHostException, IOException, KeyManagementException, NoSuchAlgorithmException {
        QuoteRequest quoteRequest = new QuoteRequest();
        quoteRequest.setPcrList(pcrList);
        quoteRequest.setNonce(nonce);
        this.data = getXml(quoteRequest).getBytes();
        ClientRequestType clientRequestType = sendQuoteRequest();
        log.info("Got quote from server");
        return clientRequestType;
    }

    private String getXml(QuoteRequest quoteRequest) throws PropertyException, JAXBException {
        JAXBContext jc = JAXBContext.newInstance("com.intel.mountwilson.ta.data.quoterequest");
        Marshaller marshaller = jc.createMarshaller();
        java.io.StringWriter sw = new StringWriter();
         marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        marshaller.marshal(quoteRequest, sw);
        String quoteRequestXml =  sw.toString();
        log.info("Quote request XML {}", quoteRequestXml);
        return quoteRequestXml;
    }
    
    

    private void checkQuoteError(ClientRequestType response) {
        int errorCode = response.getErrorCode();
        
        log.warn(String.format("Trust Agent Error %d [%s]: %s", response.getErrorCode(), response.getClientIp(), response.getErrorMessage()));
        if (errorCode != 0) {
            throw new ASException(ErrorCode.AS_TRUST_AGENT_ERROR, response.getErrorCode(),response.getErrorMessage());
        }

    }

    public HostInfo getHostInfo()  {
        this.data = "<host_info></host_info>".getBytes();
        HostInfo response;
		try {
			byte buf[] = sendRequestWithSSLSocket();
			JAXBContext jc = JAXBContext
					.newInstance("com.intel.mountwilson.ta.data.hostinfo");
			Unmarshaller u = jc.createUnmarshaller();
//			JAXBElement<HostInfo> po = (JAXBElement<HostInfo>) u.unmarshal(new StringReader(
//					new String(buf).trim()));
			response = (HostInfo)  u.unmarshal(new StringReader(
					new String(buf).trim()));
        }catch(UnknownHostException e) {
            throw new ASException(e,ErrorCode.AS_HOST_COMMUNICATION_ERROR,this.serverHostname);
        }catch(NoRouteToHostException e) { // NoRouteToHostException is a subclass of IOException that may be thrown by the socket layer
            throw new ASException(e,ErrorCode.AS_HOST_COMMUNICATION_ERROR,this.serverHostname);
        }catch(IOException e) {
            throw new ASException(e,ErrorCode.AS_HOST_COMMUNICATION_ERROR,this.serverHostname);
        }catch(NoSuchAlgorithmException e) {
            throw new ASException(e,ErrorCode.TLS_COMMMUNICATION_ERROR,this.serverHostname, e.toString());
        }catch(KeyManagementException e) {
            throw new ASException(e,ErrorCode.TLS_COMMMUNICATION_ERROR,this.serverHostname, e.toString());
        } catch(JAXBException e) {
            throw new ASException(e,ErrorCode.AS_TRUST_AGENT_INVALID_RESPONSE, e.toString());
        }/*catch(Exception e) {
            throw new ASException(e);
        }*/
       
        int errorCode = response.getErrorCode();
        log.warn(String.format("Trust Agent Error %d [%s]: %s", response.getErrorCode(), response.getClientIp(), response.getErrorMessage()));
        if (errorCode != 0) {
            throw new ASException(ErrorCode.AS_TRUST_AGENT_ERROR, errorCode,response.getErrorMessage());
        }
       return response;
    }

    
}
