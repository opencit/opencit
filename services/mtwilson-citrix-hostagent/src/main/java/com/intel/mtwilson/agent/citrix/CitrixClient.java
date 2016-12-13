/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.agent.citrix;

//import java.lang.Process;
import com.intel.mountwilson.as.common.ASConfig;
import com.intel.mountwilson.as.common.ASException;
import com.intel.mountwilson.as.helper.CommandUtil;
import com.intel.mountwilson.ta.data.hostinfo.HostInfo;
import com.intel.mtwilson.datatypes.ConnectionString;
import com.intel.mtwilson.i18n.ErrorCode;
import com.intel.mtwilson.model.Pcr;
import com.intel.mtwilson.model.PcrIndex;
import com.intel.dcsg.cpg.crypto.Sha1Digest;
import com.intel.dcsg.cpg.io.Platform;
import com.intel.dcsg.cpg.tls.policy.TlsConnection;
import com.intel.dcsg.cpg.tls.policy.TlsUtil;
import com.intel.mtwilson.Folders;
import com.intel.mtwilson.model.Nonce;
import com.intel.mtwilson.model.PcrSha1;
import com.intel.mtwilson.util.exec.EscapeUtil;
import com.xensource.xenapi.APIVersion;
import com.xensource.xenapi.Connection;
import com.xensource.xenapi.Host;
import com.xensource.xenapi.Session;
import com.xensource.xenapi.Types.BadServerResponse;
import com.xensource.xenapi.Types.XenAPIException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.configuration.Configuration;
import org.apache.xmlrpc.XmlRpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author stdalex
 */
public class CitrixClient {

    private transient Logger log = LoggerFactory.getLogger(getClass());
    String hostIpAddress;
    int port;
    String userName;
    String password;
    String connectionString;
//    private String aikverifyhome;
    private String aikverifyhomeData;
    private String aikverifyhomeBin;
    private String opensslCmd;
    private String aikverifyCmd;
    private TlsConnection tlsConnection;
    private Pattern pcrNumberPattern = Pattern.compile("[0-9]|[0-1][0-9]|2[0-3]"); // integer 0-23 with optional zero-padding (00, 01, ...)
    private Pattern pcrValuePattern = Pattern.compile("[0-9a-fA-F]{40}"); // 40-character hex string
    private String pcrNumberUntaint = "[^0-9]";
    private String pcrValueUntaint = "[^0-9a-fA-F]";
    private String AIKCert = null;
    protected Connection connection;

    public CitrixClient(TlsConnection tlsConnection) {
        this.tlsConnection = tlsConnection;
        this.connectionString = tlsConnection.getURL().toExternalForm();
//        log.info("CitrixClient connectionString == " + connectionString);
        // connectionString == citrix:https://xenserver:port;username;password  or citrix:https://xenserver:port;u=username;p=password  or the same w/o the citrix prefix
        try {
            // We need to explicitly add citrix since both Intel and Citrix hosts have the same connection string format.
            if (!connectionString.toLowerCase().startsWith("citrix:"))
                connectionString = "citrix:" + connectionString; 
            ConnectionString.CitrixConnectionString citrixConnection = ConnectionString.CitrixConnectionString.forURL(connectionString);
            hostIpAddress = citrixConnection.getHost().toString();
            port = citrixConnection.getPort();
            userName = citrixConnection.getUsername();
            password = citrixConnection.getPassword();
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid Citrix Host URL", e); // NOTE: we are NOT providing the connection string in the error message because, since we can't parse it, we dn't know if there's a password in there. 
        }
        //log.info("stdalex-error citrixInit IP:" + hostIpAddress + " port:" + port + " user: " + userName + " pw:" + password);

        // check mtwilson 2.0 configuration first
        String binPath = Folders.features("aikqverify") + File.separator + "bin"; //.getBinPath();
        String varPath = Folders.features("aikqverify") + File.separator + "data";
        log.debug("binpath = {}", binPath);
        log.debug("varpath = {}", varPath);
        File bin = new File(binPath);
        File var = new File(varPath);
        if (bin.exists() && var.exists()) {
            aikverifyhomeBin = binPath;
            aikverifyhomeData = varPath;
            opensslCmd = aikverifyhomeBin + File.separator + (Platform.isUnix() ? "openssl.sh" : "openssl.bat"); //My.configuration().getConfiguration().getString("com.intel.mountwilson.as.openssl.cmd", "openssl.bat"));
            aikverifyCmd = aikverifyhomeBin + File.separator + (Platform.isUnix() ? "aikqverify" : "aikqverify.exe");
        } else {
            // mtwilson 1.2 configuration
            Configuration config = ASConfig.getConfiguration();
            String aikverifyhome = config.getString("com.intel.mountwilson.as.home", "C:/work/aikverifyhome");
            aikverifyhomeData = aikverifyhome + File.separator + "data";
            aikverifyhomeBin = aikverifyhome + File.separator + "bin";
            opensslCmd = aikverifyhomeBin + File.separator + config.getString("com.intel.mountwilson.as.openssl.cmd", "openssl.bat");
            aikverifyCmd = aikverifyhomeBin + File.separator + config.getString("com.intel.mountwilson.as.aikqverify.cmd", "aikqverify.exe");
        }


    }

    public void init() {
        boolean foundAllRequiredFiles = true;
        String required[] = new String[]{opensslCmd, aikverifyCmd, aikverifyhomeData};
        for (String filename : required) {
            File file = new File(filename);
            if (!file.exists()) {
                log.debug(String.format("Invalid service configuration: Cannot find %s", filename));
                foundAllRequiredFiles = false;
            }
        }
        if (!foundAllRequiredFiles) {
            throw new ASException(ErrorCode.AS_CONFIGURATION_ERROR, "Cannot find aikverify files");
        }

        // we must be able to write to the data folder in order to save certificates, nones, public keys, etc.
        //log.info("stdalex-error checking to see if we can write to " + aikverifyhomeData);
        File datafolder = new File(aikverifyhomeData);
        if (!datafolder.canWrite()) {
            throw new ASException(ErrorCode.AS_CONFIGURATION_ERROR, String.format(" Cannot write to %s", aikverifyhomeData));
        }

    }

    // Commenting the below function since it is not being used and klocwork is throwing a warning    
    /*private String removeTags(String xml) {
	
     String resp = "";  
     int i = 0;
     for(; i < xml.length(); i++) {
     if(xml.charAt(i) == '>') {
     i++;
     break;
     }
     }
     for(;i < xml.length(); i++) {
     if(xml.charAt(i) == '<'){
     break;
     }
     resp += xml.charAt(i);
     }
     return resp;
     }*/
    public class keys {

        public String tpmEndCert;
        public String tpmEndKeyPEM;
        public String tpmAttKeyPEM;
        public String tpmAttKeyTCPA;

        public keys() {
        }
    }

    public void connect() throws NoSuchAlgorithmException, KeyManagementException, BadServerResponse, XenAPIException, XmlRpcException, XmlRpcException {
        URL url;
        try {
            url = new URL("https://" + hostIpAddress + ":" + port);
        } catch (MalformedURLException e) {
            throw new ASException(e, ErrorCode.AS_HOST_COMMUNICATION_ERROR, hostIpAddress);
        }


//        TrustManager[] trustAllCerts = new TrustManager[]{tlsConnection.getTlsPolicy().getTrustManager()};

        log.debug("Connecting to Citrix with ProtocolSelector: {}", tlsConnection.getTlsPolicy().getProtocolSelector().preferred());
//        SSLContext sc = SSLContext.getInstance(tlsConnection.getTlsPolicy().getProtocolSelector().preferred()); // issue #871 ssl protocol should be configurable;   was hardcoded to "SSL" before

//        sc.init(null, trustAllCerts, new java.security.SecureRandom());
//        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory()); 
//        HttpsURLConnection.setDefaultHostnameVerifier(tlsConnection.getTlsPolicy().getHostnameVerifier());
        // it would be better to use TlsConnection's openConnection directly but the URL is used from Citrix code so we try to affect it by setting the default policies
        TlsUtil.setHttpsURLConnectionDefaults(tlsConnection);

        connection = new Connection(url);

        Session.loginWithPassword(connection, userName, password, APIVersion.latest().toString());

    }

    public boolean isConnected() {
        return connection != null;
    }

    public void disconnect() throws BadServerResponse, XenAPIException, XmlRpcException {
        Session.logout(connection);
//        connection.dispose();
    }

    /**
     * This is a Citrix-specific API, not implemented by vmware hosts ; trust
     * agent will implement it when it's merged with provisioning agent from the
     * asset tag branch
     *
     * @param tag
     */
    public void setAssetTag(Sha1Digest tag) throws BadServerResponse, XenAPIException, XmlRpcException, NoSuchAlgorithmException, KeyManagementException {
        if (!isConnected()) {
            connect();
        }
        Set<Host> hostList = Host.getAll(connection);
        Iterator iter = hostList.iterator();
        // hasNext() will always be valid otherwise we will get an exception from the getAll method. So, we not need
        // to throw an exception if the hasNext is false.
        Host h = null;
        if (iter.hasNext()) {
            h = (Host) iter.next();
        }
        if (h == null) {
            throw new IllegalStateException("Cannot find Citrix Xen host");
        }

        Map<String, String> myMap = new HashMap<>();
        log.debug("sending the following to the xenserver: " + tag.toBase64());
        myMap.put("tag", Base64.encodeBase64String(tag.toByteArray()));


        //toByteArray()
        String retval = h.callPlugin(connection, "tpm", "tpm_set_asset_tag", myMap);
        log.debug("xenapi returned: {}", retval);

    }

    public HashMap<String, Pcr> getQuoteInformationForHost(String pcrList) {
        return getQuoteInformationForHost(pcrList, null);
    }
    
    public HashMap<String, Pcr> getQuoteInformationForHost(String pcrList, Nonce challenge) {
        log.debug("getQuoteInformationForHost pcrList == " + pcrList);
        try {

            // We cannot reuse the connections across different calls since they are tied to a particular host.
            if (!isConnected()) {
                connect();
            }

            String nonce;
            if( challenge == null ) {
                nonce = generateNonce();
            }
            else {
                nonce = Base64.encodeBase64String(challenge.toByteArray());
            }
            
            String sessionId = generateSessionId();
            String aikCertificate = getAIKCertificate();

            // We do not need to connect again. So, commenting it out.
            // System.err.println("stdalex-error connecting with " + userName + " " + password);
            // Session.loginWithPassword(connection, userName, password, APIVersion.latest().toString());

            // System.err.println( "CitrixClient: connected to server ["+hostIpAddress+"]");	

            Set<Host> hostList = Host.getAll(connection);
            Iterator iter = hostList.iterator();
            // hasNext() will always be valid otherwise we will get an exception from the getAll method. So, we not need
            // to throw an exception if the hasNext is false.
            File f, q, n;
            Host h = null;
            if (iter.hasNext()) {
                h = (Host) iter.next();
            }
            if (h == null) {
                throw new IllegalStateException("Cannot find Citrix Xen host");
            }

            /*
             String aik = h.callPlugin(connection, "tpm", "tpm_get_attestation_identity", myMap);
             int startP = aik.indexOf("<xentxt:TPM_Attestation_KEY_PEM>");
             int endP = aik.indexOf("</xentxt:TPM_Attestation_KEY_PEM>");
             // 32 is the size of the opening tag  <xentxt:TPM_Attestation_KEY_PEM>
             String cert = aik.substring(startP + "<xentxt:TPM_Attestation_KEY_PEM>".length(), endP);
             log.debug("aikCert == " + cert);
             keys key = new keys();
             key.tpmAttKeyPEM = cert;  // This is the actual value for AIK!!!!!
             aikCertificate = key.tpmAttKeyPEM;
             */

            log.debug("extracted aik cert from response: " + aikCertificate);

            Map<String, String> myMap = new HashMap<>();
            myMap.put("nonce", nonce);

            long plugInCallStart = System.currentTimeMillis();
            String quote = h.callPlugin(connection, "tpm", "tpm_get_quote", myMap);
            long plugInCallStop = System.currentTimeMillis();
            log.debug("Citrix PlugIn call: TPM quote retrieval time " + (plugInCallStop - plugInCallStart) + " milliseconds");

            log.debug("extracted quote from response: " + quote);
            //saveFile(getCertFileName(sessionId), Base64.decodeBase64(aikCertificate));
            f = saveFile(getCertFileName(sessionId), aikCertificate.getBytes());
            log.debug("saved certificate with session id: " + sessionId);

            q = saveQuote(quote, sessionId);

            log.debug("saved quote with session id: " + sessionId);

            n = saveNonce(nonce, sessionId);

            log.debug("saved nonce with session id: " + sessionId);

            //createRSAKeyFile(sessionId);

            log.debug("created RSA key file for session id: " + sessionId);

            HashMap<String, Pcr> pcrMap = verifyQuoteAndGetPcr(sessionId, pcrList);

            log.info("Got PCR map");
            //log.log(Level.INFO, "PCR map = "+pcrMap); // need to untaint this first

            f.delete();
            q.delete();
            n.delete();
            return pcrMap;

        } catch (ASException e) {
            throw e;
//        } catch(UnknownHostException e) {
//            throw new ASException(e,ErrorCode.AS_HOST_COMMUNICATION_ERROR, hostIpAddress);
        } catch (Exception e) {
            log.debug("caught exception during login: " + e.toString() + " class: " + e.getClass());
            throw new ASException(e, ErrorCode.AS_CITRIX_ERROR, e.toString());
        }
    }

    public String generateNonce() {
        try {
            // Create a secure random number generator
            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
            // Get 1024 random bits
            byte[] bytes = new byte[16];
            sr.nextBytes(bytes);

//            nonce = new BASE64Encoder().encode( bytes);
            String nonce = Base64.encodeBase64String(bytes);

            log.debug("Nonce Generated " + nonce);
            return nonce;
        } catch (NoSuchAlgorithmException e) {
            throw new ASException(e);
        }
    }

    private String generateSessionId() throws NoSuchAlgorithmException {

        // Create a secure random number generator
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        // Get 1024 random bits
        byte[] seed = new byte[1];
        sr.nextBytes(seed);

        sr = SecureRandom.getInstance("SHA1PRNG");
        sr.setSeed(seed);



        int nextInt = sr.nextInt();
        String sessionId = "" + ((nextInt < 0) ? nextInt * -1 : nextInt);


        log.debug("Session Id Generated [" + sessionId + "]");



        return sessionId;

    }

    private String getNonceFileName(String sessionId) {
        return "nonce_" + sessionId + ".data";
    }

    private String getQuoteFileName(String sessionId) {
        return "quote_" + sessionId + ".data";
    }

    // Commenting the below function since it is not being used and klocwork is throwing a warning
    /*private void saveCertificate(String aikCertificate, String sessionId) throws IOException  {
     if( aikCertificate.indexOf("-----BEGIN CERTIFICATE-----\n") < 0 && aikCertificate.indexOf("-----BEGIN CERTIFICATE-----") >= 0 ) {
     log.debug( "adding newlines to certificate BEGIN tag");            
     aikCertificate = aikCertificate.replace("-----BEGIN CERTIFICATE-----", "-----BEGIN CERTIFICATE-----\n");
     }
     if( aikCertificate.indexOf("\n-----END CERTIFICATE-----") < 0 && aikCertificate.indexOf("-----END CERTIFICATE-----") >= 0 ) {
     log.debug( "adding newlines to certificate END tag");            
     aikCertificate = aikCertificate.replace("-----END CERTIFICATE-----", "\n-----END CERTIFICATE-----");
     }

     saveFile(getCertFileName(sessionId), aikCertificate.getBytes());
     }*/
    private String getCertFileName(String sessionId) {
        return "aikcert_" + sessionId + ".cer";
    }

    private File saveFile(String fileName, byte[] contents) throws IOException {
        File file = new File(aikverifyhomeData + File.separator + fileName);
        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            log.debug(String.format("saving file %s to [%s]", fileName, aikverifyhomeData));
            assert contents != null;
            fileOutputStream.write(contents);
            fileOutputStream.flush();
            return file;
        } catch (FileNotFoundException e) {
            log.warn(String.format("cannot save to file %s in [%s]: %s", fileName, aikverifyhomeData, e.getMessage()));
            throw e;
        }
    }

    private File saveQuote(String quote, String sessionId) throws IOException {
//          byte[] quoteBytes = new BASE64Decoder().decodeBuffer(quote);
        byte[] quoteBytes = Base64.decodeBase64(quote);
        File q = saveFile(getQuoteFileName(sessionId), quoteBytes);
        return q;
    }

    private File saveNonce(String nonce, String sessionId) throws IOException {
//          byte[] nonceBytes = new BASE64Decoder().decodeBuffer(nonce);
        byte[] nonceBytes = Base64.decodeBase64(nonce);
        File n = saveFile(getNonceFileName(sessionId), nonceBytes);
        return n;
    }

    // Commenting the below function since it is not being used and klocwork is throwing a warning
    /*private void createRSAKeyFile(String sessionId)  {
        
     String command = String.format("%s %s %s",opensslCmd,aikverifyhomeData + File.separator + getCertFileName(sessionId),aikverifyhomeData + File.separator+getRSAPubkeyFileName(sessionId)); 
     log.debug( "RSA Key Command " + command);
     CommandUtil.runCommand(command, false, "CreateRsaKey" );
     //log.log(Level.INFO, "Result - {0} ", result);
     } */

    /*private String getRSAPubkeyFileName(String sessionId) {
     return "rsapubkey_" + sessionId + ".key";
     }*/
    private HashMap<String, Pcr> verifyQuoteAndGetPcr(String sessionId, String pcrList) {
        HashMap<String, Pcr> pcrMp = new HashMap<String, Pcr>();
        log.debug("verifyQuoteAndGetPcr for session " + sessionId);
        String command = String.format("%s -c %s %s %s",
                EscapeUtil.doubleQuoteEscapeShellArgument(aikverifyCmd),
                EscapeUtil.doubleQuoteEscapeShellArgument(aikverifyhomeData + File.separator + getNonceFileName(sessionId)),
                EscapeUtil.doubleQuoteEscapeShellArgument(aikverifyhomeData + File.separator + getCertFileName(sessionId)),
                EscapeUtil.doubleQuoteEscapeShellArgument(aikverifyhomeData + File.separator + getQuoteFileName(sessionId)));
        log.debug("Command: " + command);
        List<String> result = CommandUtil.runCommand(command, true, "VerifyQuote");

        // Sample output from command:
        //  1 3a3f780f11a4b49969fcaa80cd6e3957c33b2275
        //  17 bfc3ffd7940e9281a3ebfdfa4e0412869a3f55d8
        //log.log(Level.INFO, "Result - {0} ", result); // need to untaint this first

        // String pcrList = "0,1,2,3,17,18,19";
        List<String> pcrs = Arrays.asList(pcrList.split(","));
        //for(int i = 0; i < 25; i++) {
        //     if(pcrs.contains(String.valueOf(i)))
        //         System.out.println(i);
        //}
        for (String pcrString : result) {
            String[] parts = pcrString.trim().split(" ");
            if (parts.length == 2) {
                String pcrNumber = parts[0].trim().replaceAll(pcrNumberUntaint, "").replaceAll("\n", "");
                String pcrValue = parts[1].trim().replaceAll(pcrValueUntaint, "").replaceAll("\n", "");
                boolean validPcrNumber = pcrNumberPattern.matcher(pcrNumber).matches();
                boolean validPcrValue = pcrValuePattern.matcher(pcrValue).matches();
                if (validPcrNumber && validPcrValue) {
                    log.debug("Result PCR " + pcrNumber + ": " + pcrValue);
                    if (pcrs.contains(pcrNumber)) {
                        pcrMp.put(pcrNumber, new PcrSha1(new PcrIndex(Integer.parseInt(pcrNumber)), pcrValue));
                    }
                    //PcrManifest(Integer.parseInt(pcrNumber),pcrValue));            	
                }
            } else {
                log.info("Result PCR invalid");
            }
            /*
             if(pcrs.contains(parts[0].trim()))
             pcrMp.put(parts[0].trim(), new PcrManifest(Integer.parseInt(parts[0]),parts[1]));
             */
        }

        return pcrMp;

    }

    public HostInfo getHostInfo() throws NoSuchAlgorithmException, KeyManagementException, MalformedURLException, BadServerResponse, XenAPIException, XmlRpcException {
        //log.info("stdalex-error getHostInfo IP:" + hostIpAddress + " port:" + port + " user: " + userName + " pw:" + password);
        HostInfo response = new HostInfo();

        if (!isConnected()) {
            connect();
        }

        log.debug("CitrixClient: connected to server [" + hostIpAddress + "]");


        // Map<String, String> myMap = new HashMap<String, String>();
        Set<Host> hostList = Host.getAll(connection);
        Iterator iter = hostList.iterator();
        // hasNext() will always be valid otherwise we will get an exception from the getAll method. So, we not need
        // to throw an exception if the hasNext is false.
        Host h = null;
        if (iter.hasNext()) {
            h = (Host) iter.next();
        }
        if (h == null) {
            throw new IllegalStateException("Cannot find Citrix Xen host");
        }

        response.setClientIp(hostIpAddress);

        Map<String, String> map = h.getSoftwareVersion(connection);
        response.setOsName(map.get("product_brand"));
        response.setOsVersion(map.get("product_version"));
        response.setVmmName("xen");
        response.setVmmVersion(map.get("xen"));

        map = h.getBiosStrings(connection);
        response.setBiosOem(map.get("bios-vendor"));
        response.setBiosVersion(map.get("bios-version"));

        map = h.getCpuInfo(connection);
        int stepping = Integer.parseInt(map.get("stepping"));
        int model = Integer.parseInt(map.get("model"));
        int family = Integer.parseInt(map.get("family"));
        // EAX register contents is used for defining CPU ID and as well as family/model/stepping
        // 0-3 bits : Stepping
        // 4-7 bits: Model #
        // 8-11 bits: Family code
        // 12 & 13: Processor type, which will always be zero
        // 14 & 15: Reserved
        // 16 to 19: Extended model
        // Below is the sample of the data got from the Citrix API
        // Model: 45, Stepping:7 and Family: 6
        // Mapping it to the EAX register we would get
        // 0-3 bits: 7
        // 4-7 bits: D (Actually 45 would be 2D. So, we would put D in 4-7 bits and 2 in 16-19 bits
        // 8-11 bits: 6
        //12-15 bits: 0
        // 16-19 bits: 2
        // 20-31 bits: Extended family and reserved, which will be 0
        // So, the final content would be : 000206D7
        // On reversing individual bytes, we would get D7 06 02 00
        String modelInfo = Integer.toHexString(model);
        String processorInfo = modelInfo.charAt(1) + Integer.toHexString(stepping) + " " + "0" + Integer.toHexString(family) + " " + "0" + modelInfo.charAt(0);
        processorInfo = processorInfo.trim().toUpperCase();
        response.setProcessorInfo(processorInfo);
        java.util.Date date = new java.util.Date();
        response.setTimeStamp(new Timestamp(date.getTime()).toString());
//       log.trace("stdalex-error leaving getHostInfo");

        return response;
    }

    public String getSystemUUID() throws NoSuchAlgorithmException, KeyManagementException, XenAPIException, BadServerResponse, XmlRpcException {

        if (!isConnected()) {
            connect();
        }

        log.debug("CitrixClient getSystemUUID: connected to server [" + hostIpAddress + "]");

        Map<String, String> myMap = new HashMap<String, String>();
        Set<Host> hostList = Host.getAll(connection);
        Iterator iter = hostList.iterator();
        // hasNext() will always be valid otherwise we will get an exception from the getAll method. So, we not need
        // to throw an exception if the hasNext is false.
        Host h = null;
        if (iter.hasNext()) {
            h = (Host) iter.next();
        }
        if (h == null) {
            throw new IllegalStateException("Cannot find Citrix Xen host");
        }

        String aik = h.callPlugin(connection, "tpm", "tpm_get_attestation_identity", myMap);

        int startP = aik.indexOf("<xentxt:System_UUID>");
        int endP = aik.indexOf("</xentxt:System_UUID>");
        // 32 is the size of the opening tag  <xentxt:TPM_Attestation_KEY_PEM>
        String systemUUID = aik.substring(startP + "<xentxt:System_UUID>".length(), endP);
        log.debug("systemUUID == " + systemUUID);



        String resp = systemUUID.toLowerCase();

        // log.trace("stdalex-error getAIKCert: returning back: " + resp);
        return resp;
    }

    public String getAIKCertificate() throws NoSuchAlgorithmException, KeyManagementException, BadServerResponse, XenAPIException, XmlRpcException {
//        log.info("stdalex-error getAIKCert IP:" + hostIpAddress + " port:" + port + " user: " + userName + " pw:" + password); // removed to prevent leaking secrets

        //log.debug("CitrixClient: AIKCert: " + AIKCert);
        long startTime = System.currentTimeMillis();

        if (AIKCert != null) {
            log.debug("CitrixClient: AIKCert already generated: " + AIKCert);
            return AIKCert;
        } else {
            if (!isConnected()) {
                connect();
            }
            log.debug("CitrixClient: generating AIKCert");


            Map<String, String> myMap = new HashMap<String, String>();
            Set<Host> hostList = Host.getAll(connection);
            Iterator iter = hostList.iterator();
            // hasNext() will always be valid otherwise we will get an exception from the getAll method. So, we not need
            // to throw an exception if the hasNext is false.
            Host h = null;
            if (iter.hasNext()) {
                h = (Host) iter.next();
            }
            if (h == null) {
                throw new IllegalStateException("Cannot find Citrix Xen host");
            }

            log.debug("TIMETAKEN: get host list: {}", System.currentTimeMillis() - startTime);
            startTime = System.currentTimeMillis();

            String aik = h.callPlugin(connection, "tpm", "tpm_get_attestation_identity", myMap);
            log.debug("TIMETAKEN: citrix api: {}", System.currentTimeMillis() - startTime);

            int startP = aik.indexOf("<xentxt:TPM_Attestation_KEY_PEM>");
            int endP = aik.indexOf("</xentxt:TPM_Attestation_KEY_PEM>");
            // 32 is the size of the opening tag  <xentxt:TPM_Attestation_KEY_PEM>
            String cert = aik.substring(startP + "<xentxt:TPM_Attestation_KEY_PEM>".length(), endP);
            log.debug("aikCert == " + cert);

            keys key = new keys();

            key.tpmAttKeyPEM = cert;  // This is the actual value for AIK!!!!!

            //resp = new String( Base64.decodeBase64(key.tpmAttKeyPEM));
            String resp = key.tpmAttKeyPEM;//new String(key.tpmAttKeyPEM);

//       log.trace("stdalex-error getAIKCert: returning back: " + resp);
            AIKCert = resp;
            return AIKCert;
        }
    }
}
