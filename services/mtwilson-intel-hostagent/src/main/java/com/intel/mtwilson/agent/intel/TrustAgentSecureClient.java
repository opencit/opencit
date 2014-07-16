/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.agent.intel;

/**
 * @author dsmagadX
 */

//import com.intel.mountwilson.as.common.*;
import com.intel.mtwilson.i18n.ErrorCode;
import com.intel.mountwilson.as.common.ASConfig;
import com.intel.mountwilson.as.common.ASException;
import com.intel.mountwilson.ta.data.ClientRequestType;
import com.intel.mountwilson.ta.data.daa.response.DaaResponse;
import com.intel.mountwilson.ta.data.hostinfo.HostInfo;
import com.intel.mountwilson.ta.data.quoterequest.QuoteRequest;
import com.intel.mtwilson.datatypes.*;
import com.intel.dcsg.cpg.tls.policy.TlsConnection;
import com.intel.dcsg.cpg.tls.policy.TlsPolicy;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.xml.bind.JAXBException;
import javax.xml.bind.PropertyException;
import javax.xml.stream.XMLStreamException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.intel.dcsg.cpg.xml.JAXB;;

public class TrustAgentSecureClient {
    public static final int DEFAULT_TRUST_AGENT_PORT = 9999;
    public static final String TA_ERROR_CODE = "error_code";
    public static final String TA_ERROR_MESSAGE = "error_message";
    private final Logger log = LoggerFactory.getLogger(getClass());
    private String serverHostname = null;
    private int serverPort = 0;
    private byte[] data;
    private TlsPolicy tlsPolicy;
    JAXB jaxb = new JAXB();
    
    private static int TIME_OUT = ASConfig.getTrustAgentTimeOutinMilliSecs();

    // Bug #497 commenting out  these two constructors because now Tls Policy is a required argument.
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
        parseConnectionString(tlsConnection.getURL().toExternalForm());
        log.debug("TrustAgentSecureClient  hostname({}) port({})", new Object[] {  serverHostname, serverPort }); // removed tlsConnection.getConnectionString(), to prevent leaking secrets
    }

    private void parseConnectionString(String connectionString) {
        if( connectionString.startsWith("https") ) {  // new format used starting with version 1.1 is URL:   https://ipAddressOrHostname:port
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
        if( connectionString.contains(":") ) { // format used from 0.5 Alpha to 1.0-RC2 
            try {
                String[] parts = connectionString.split(":");
                serverHostname = parts[0];
                serverPort = Integer.valueOf(parts[1]);
                return;
            }
            catch(Exception e) {
                throw new IllegalArgumentException("Invalid Trust Agent connection string: "+connectionString, e);
            }
        }
        throw new IllegalArgumentException("Unrecognized Trust Agent connection string format: "+connectionString);
    }
    
    /*
    public TrustAgentSecureClient(IPAddress serverIPAddress, int serverPort, byte[] data) { // datatype.IPAddress
        this(serverIPAddress, serverPort);
        if( data != null ) {
            this.data = Arrays.copyOf(data, data.length);
        }
    }

    public TrustAgentSecureClient(IPAddress serverIPAddress, int serverPort) { // datatype.IPAddress
        this(serverIPAddress.toString(), serverPort);
    }
    */

    
    private byte[] sendRequestWithSSLSocket() throws NoSuchAlgorithmException, NoSuchAlgorithmException, KeyManagementException, UnknownHostException, IOException {
        log.trace( "Opening connection to {} port {}", serverHostname, String.valueOf(serverPort));
        
        if( data == null ) {
        	throw new IllegalArgumentException("Attempted to send request without data");
        }

//        HttpsURLConnection.setDefaultHostnameVerifier(tlsPolicy.getHostnameVerifier());
//        SSLSocketFactory sslsocketfactory = getSSLContext().getSocketFactory();
//        SSLSocket sock = (SSLSocket) sslsocketfactory.createSocket();
        TlsConnection tlsConnection = new TlsConnection(new URL("https://"+serverHostname+":"+serverPort), tlsPolicy); 
        
        try(SSLSocket sock = tlsConnection.connect()) {            
//            sock.connect(new InetSocketAddress(serverHostname,serverPort), TIME_OUT);
            InputStream sockInput = sock.getInputStream();
            OutputStream sockOutput = sock.getOutputStream();

            log.info("About to start reading/writing to/from socket.");
            log.debug("Writing: {}", new String(data));
            byte[] buf = new byte[5000];
                sockOutput.write(data, 0, data.length);
            int bytes_read = sockInput.read(buf);
            log.debug( "Received " + bytes_read + " bytes to server and received them back again, msg = " +StringUtils.replace(new String(buf), "\n", "\n  "));
            return buf;
        }
        catch(SocketTimeoutException e){
            throw new ASException(e,ErrorCode.AS_TRUST_AGENT_CONNNECT_TIMED_OUT,serverHostname,serverPort,(TIME_OUT/1000));           
        }
    }
    
    public DaaResponse sendDaaChallenge(String challenge) throws NoSuchAlgorithmException, KeyManagementException, UnknownHostException, JAXBException, IOException, XMLStreamException {
        this.data = challenge.getBytes();
        byte buf[] = sendRequestWithSSLSocket();
        // bug #1038 use secure xml parsing settings, encapsulated in cpg-xml JAXB utility
        DaaResponse response = jaxb.read(new String(buf).trim(), DaaResponse.class);
//        JAXBContext jc = JAXBContext.newInstance("com.intel.mountwilson.ta.data.daa.response");
//        Unmarshaller u = jc.createUnmarshaller();
//        JAXBElement po =  (JAXBElement) u.unmarshal(new StringReader(new String(buf).trim()));
//        DaaResponse response = (DaaResponse)po.getValue();
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
    public ClientRequestType sendQuoteRequest() throws UnknownHostException, IOException, JAXBException, KeyManagementException, NoSuchAlgorithmException, XMLStreamException  {


            byte buf[] = sendRequestWithSSLSocket();

            log.info("Unmarshalling to Jaxb object.");
            
        // bug #1038 use secure xml parsing settings, encapsulated in cpg-xml JAXB utility
            ClientRequestType response = jaxb.read(new String(buf).trim(), ClientRequestType.class);
            
            assert response != null;
            
            checkQuoteError(response);

            log.info("Done reading/writing to/from socket, closing socket.");
            return response;

    }

        /*
    private SSLContext getSSLContext() throws NoSuchAlgorithmException, KeyManagementException {
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
        log.debug("Connecting to trust agent with ProtocolSelector: {}", tlsPolicy.getProtocolSelector().preferred());
    	SSLContext ctx = SSLContext.getInstance(tlsPolicy.getProtocolSelector().preferred()); // bug #871 ssl policy should be configurable; was hardcoded to "SSL"
        ctx.init(null, new javax.net.ssl.TrustManager[]{ tlsPolicy.getTrustManager() }, null);
        return ctx;
    }
        */

    /**
     * Example request:
     * <identity_request></identity_request>
     * Example response:
     * <client_request> <timestamp>Tue Apr 09 15:45:02 PDT 2013</timestamp><clientIp>fe80:0:0:0:21e:67ff:fe10:4460%4</clientIp><error_code>0</error_code><error_message>OK</error_message><aikcert>-----BEGIN CERTIFICATE-----MIICuzCCAaOgAwIBAgIGAT3w+RgtMA0GCSqGSIb3DQEBBQUAMBkxFzAVBgNVBAMMDkhJU19Qcml2
  YWN5X0NBMB4XDTEzMDQwOTIyNDQ0OVoXDTIzMDQwOTIyNDQ0OVowADCCASIwDQYJKoZIhvcNAQEB
  BQADggEPADCCAQoCggEBAMtggj+vxoG7fSTrM8yAjZZm4v4SbOcou1BW+HeRcVt1VU4/nWkTJdk/
  etHtNI2qady/qalIIKkHvRjG4IStubaKtS9t8cJhzkFoI9dWYLAfYNdIrDe4IB8fFWgwSB9nAbHC
  E9KL5yWs+7ImRND/4HYRPHShtwm+aH2SvZdvHi0ZCDzentlhZaQQ3T71upWUPi7fnBnFy4L1xzO6
  AR2aPT27D06LdSh+21ofWl/Lh28VQM3hcqul+Yyhc+DdKjMhqOUPJOOmS4y/erip8HH+YgIg7OqX
  PrXdqrW9h5IWl+5T/AYayQ82Y2WAtnYa2rHa+GU55p8pM1Ohp88FDbNaSv0CAwEAAaMiMCAwHgYD
  VR0RAQH/BBQwEoEQSElTIElkZW50aXR5IEtleTANBgkqhkiG9w0BAQUFAAOCAQEAQ6F2PGx26D6P
  53Enht4Zt7cOnohZwIkdu6Jl6wUKLcWpMn6x1Nqdi5Q0++qFW+vqoBQA44ldSsfQ7t3rqwr59Qrb
  HtTIW/+bWTUjXO0OiQlFR87adaxbNlPQlPSApQiLvH2K5PFctnMd4A/TAaH9KtbDimR8obCt4y5S
  xUH54DrZMayGd5OWCUtH9bypgLBg8QvAyuRP7eszu5z75Nv61JNjQxDNUyFMe2iIqMPV9Gs1nsYV
  3kWLYF/gxYjRc/3iGDUTJez9pinZrx/Ddq3q6nQs/EHGZj5G7Z0S261Pofobo+KBRHP31+kEVczi
  Bhc/7BfnYNKiGYzNZh4atiMrhg==
  -----END CERTIFICATE-----</aikcert></client_request>                                                                  
     * @return 
     */
    public String getAIKCertificate() {
        try {

            log.info("Sending Generate Identity");
            byte[] identityInput = "<identity_request></identity_request>".getBytes();
            this.data = identityInput;

            ClientRequestType response = sendQuoteRequest();

            String certificate = response.getAikcert();
            
            return certificate;
        }catch(ASException ase){
            throw ase;
        }catch(UnknownHostException e) {
            throw new ASException(e,ErrorCode.AS_HOST_COMMUNICATION_ERROR, this.serverHostname);
        }catch (Exception e) {
            throw new ASException(e);
        }
    }
    
    public ClientRequestType getQuote(String nonce, String pcrList) throws PropertyException, JAXBException, UnknownHostException, IOException, KeyManagementException, NoSuchAlgorithmException, XMLStreamException {
        QuoteRequest quoteRequest = new QuoteRequest();
        quoteRequest.setPcrList(pcrList);
        quoteRequest.setNonce(nonce);
        this.data = getXml(quoteRequest).getBytes();
        ClientRequestType clientRequestType = sendQuoteRequest();
        log.info("Got quote from server");
        return clientRequestType;
    }

    private String getXml(QuoteRequest quoteRequest) throws PropertyException, JAXBException {
        String quoteRequestXml = jaxb.write(quoteRequest);
        log.debug("Quote request XML {}", quoteRequestXml);
        return quoteRequestXml;
    }
    
    

    private void checkQuoteError(ClientRequestType response) {
        int errorCode = response.getErrorCode();
        
        log.error(String.format("Trust Agent Error %d [%s]: %s", response.getErrorCode(), response.getClientIp(), response.getErrorMessage()));
        if (errorCode != 0) {
            throw new ASException(ErrorCode.AS_TRUST_AGENT_ERROR, response.getErrorCode(),response.getErrorMessage());
        }

    }

    /**
     * How to test this:  
     * 
     * openssl s_client -connect 10.1.71.169:9999
     * <host_info></host_info>
     * 
     * Example response:
     * <host_info><timeStamp>Tue Apr 09 15:43:47 PDT 2013</timeStamp><clientIp>fe80:0:0:0:21e:67ff:fe10:4460%4</clientIp><errorCode>0</errorCode><errorMessage>OK</errorMessage><osName>SUSE LINUX</osName><osVersion> 11</osVersion><biosOem>Intel Corp.</biosOem><biosVersion> S5500.86B.01.00.T060.070620121139</biosVersion><vmmName>Xen</vmmName><vmmVersion>4.1.0</vmmVersion></host_info>                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               
     * 
     * 
     * @return 
     */
    public HostInfo getHostInfo() {
        this.data = "<host_info></host_info>".getBytes();
        HostInfo response;
		try {
			byte buf[] = sendRequestWithSSLSocket();
            log.debug("TrustAgent response: {}", new String(buf).trim());
        // bug #1038 use secure xml parsing settings, encapsulated in cpg-xml JAXB utility
                        response = jaxb.read(new String(buf).trim(), HostInfo.class);
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
        }
        catch(XMLStreamException e) {
            throw new ASException(e,ErrorCode.AS_TRUST_AGENT_INVALID_RESPONSE, e.toString());            
        }
        /*catch(Exception e) {
            throw new ASException(e);
        }*/
       
        int errorCode = response.getErrorCode();
        log.error(String.format("Trust Agent Error %d [%s]: %s", response.getErrorCode(), response.getClientIp(), response.getErrorMessage()));
        if (errorCode != 0) {
            throw new ASException(ErrorCode.AS_TRUST_AGENT_ERROR, errorCode,response.getErrorMessage());
        }
       return response;
    }

     /**
     * How to test this:  
     * 
     * openssl s_client -connect 10.1.71.169:9999
     * <set_asset_tag><asset_tag_hash>aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa</asset_tag_hash><asset_tag_uuid>aaaa-aaa-aaa-aaa</asset_tag_uuid></set_asset_tag>
     * 
     * Example response:
     * <response>true</response>
     * 
     * 
     * @return 
     */
    public boolean setAssetTag(String assetTagHash, String uuid) {
        String xml = "<set_asset_tag><asset_tag_hash>" + assetTagHash + "</asset_tag_hash><asset_tag_uuid>" + uuid +"</asset_tag_uuid></set_asset_tag>";
        this.data = xml.getBytes();
        
	try {
	    byte buf[] = sendRequestWithSSLSocket();
            log.debug("TrustAgent response: {}", new String(buf));
        // bug #1038 use secure xml parsing settings, encapsulated in cpg-xml JAXB utility
	    // response = jaxb.read(new String(buf).trim(), HostInfo.class);
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
        }
     
        return true;
    }
    
    
    
}
