/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.agent.citrix;

import java.net.MalformedURLException; 
import java.net.URL; 
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;  
import java.util.Set;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap; 
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;
import javax.net.ssl.HostnameVerifier;  
import javax.net.ssl.HttpsURLConnection;  
import javax.net.ssl.SSLContext;  
import javax.net.ssl.SSLSession;  
import javax.net.ssl.TrustManager;  
import javax.net.ssl.X509TrustManager; 
import javax.persistence.EntityManagerFactory;
import java.sql.Timestamp;
import java.util.Date;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.IOUtils;

import com.xensource.xenapi.APIVersion;
import com.xensource.xenapi.Connection;
import com.xensource.xenapi.Host;
import com.xensource.xenapi.Session;
import com.xensource.xenapi.Types.BadServerResponse;
import com.xensource.xenapi.Types.XenAPIException;


import com.intel.mountwilson.as.common.ASConfig;
import com.intel.mountwilson.as.common.ASException;
import com.intel.mountwilson.as.helper.CommandUtil;
import com.intel.mtwilson.datatypes.ErrorCode;
import com.intel.mountwilson.ta.data.hostinfo.HostInfo;
import com.intel.mtwilson.datatypes.ConnectionString;
import com.intel.mtwilson.model.Pcr;
import com.intel.mtwilson.model.PcrIndex;
import com.intel.mtwilson.model.PcrManifest;
import com.intel.mtwilson.model.Sha1Digest;
import com.intel.mtwilson.tls.TlsConnection;
import java.util.Arrays;
import java.util.logging.Level;
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
    
    private String aikverifyhome;
    private String aikverifyhomeData;
    private String aikverifyhomeBin;
    private String opensslCmd;
    private String aikverifyCmd;
    private TlsConnection tlsConnection;
    private Pattern pcrNumberPattern = Pattern.compile("[0-9]|[0-1][0-9]|2[0-3]"); // integer 0-23 with optional zero-padding (00, 01, ...)
    private Pattern pcrValuePattern = Pattern.compile("[0-9a-fA-F]{40}"); // 40-character hex string
    private String pcrNumberUntaint = "[^0-9]";
    private String pcrValueUntaint = "[^0-9a-fA-F]";
    
    protected static Connection connection;
	
    public CitrixClient(TlsConnection tlsConnection){
        this.tlsConnection = tlsConnection;
        this.connectionString = tlsConnection.getConnectionString();
        log.info("CitrixClient connectionString == " + connectionString);
        // connectionString == citrix:https://xenserver:port;username;password  or citrix:https://xenserver:port;u=username;p=password  or the same w/o the citrix prefix
        try {
            ConnectionString.CitrixConnectionString citrixConnection = ConnectionString.CitrixConnectionString.forURL(connectionString);
            hostIpAddress = citrixConnection.getHost().toString();
            port = citrixConnection.getPort();
            userName = citrixConnection.getUsername();
            password = citrixConnection.getPassword();
        }
        catch(MalformedURLException e) {
            throw new IllegalArgumentException("Invalid Citrix Host URL: "+connectionString, e);
        }
        //log.info("stdalex-error citrixInit IP:" + hostIpAddress + " port:" + port + " user: " + userName + " pw:" + password);
               
        Configuration config = ASConfig.getConfiguration();
        aikverifyhome = config.getString("com.intel.mountwilson.as.home", "C:/work/aikverifyhome");
        aikverifyhomeData = aikverifyhome+File.separator+"data";
        aikverifyhomeBin = aikverifyhome+File.separator+"bin";
        opensslCmd = aikverifyhomeBin + File.separator + config.getString("com.intel.mountwilson.as.openssl.cmd", "openssl.bat");
        aikverifyCmd = aikverifyhomeBin + File.separator + config.getString("com.intel.mountwilson.as.aikqverify.cmd", "aikqverify.exe");
        
        boolean foundAllRequiredFiles = true;
        String required[] = new String[] { aikverifyhome, opensslCmd, aikverifyCmd, aikverifyhomeData };
        for(String filename : required) {
            File file = new File(filename);
            if( !file.exists() ) {
                log.info( String.format("Invalid service configuration: Cannot find %s", filename ));
                foundAllRequiredFiles = false;
            }
        }
        if( !foundAllRequiredFiles ) {
            throw new ASException(ErrorCode.AS_CONFIGURATION_ERROR, "Cannot find aikverify files");
        }
        
        // we must be able to write to the data folder in order to save certificates, nones, public keys, etc.
        log.info("stdalex-error checking to see if we can write to " + aikverifyhomeData);
        File datafolder = new File(aikverifyhomeData);
        if( !datafolder.canWrite() ) {
            throw new ASException(ErrorCode.AS_CONFIGURATION_ERROR, String.format(" Cannot write to %s", aikverifyhomeData));            
        }    
    }
	
	
    
    private String removeTags(String xml) {
	
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
    }
    
    public class keys {
     public String tpmEndCert;
     public String tpmEndKeyPEM;
     public String tpmAttKeyPEM;
     public String tpmAttKeyTCPA;
     
     public keys() {}
    }
    
    public void connect() throws NoSuchAlgorithmException, KeyManagementException, BadServerResponse, XenAPIException, XmlRpcException, XmlRpcException {
            URL url = null; 
            try { 
               url = new URL("https://" + hostIpAddress + ":" + port); 
            }catch (MalformedURLException e) { 
               throw new ASException(e,ErrorCode.AS_HOST_COMMUNICATION_ERROR, hostIpAddress);
            } 
            
            TrustManager[] trustAllCerts = new TrustManager[] { tlsConnection.getTlsPolicy().getTrustManager() };
            
            // Install the all-trusting trust manager  
            SSLContext sc = SSLContext.getInstance("SSL");  
            // Create empty HostnameVerifier  
            HostnameVerifier hv = tlsConnection.getTlsPolicy().getHostnameVerifier();  
 
            sc.init(null, trustAllCerts, new java.security.SecureRandom());  
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());  
            HttpsURLConnection.setDefaultHostnameVerifier(hv); 
			
            connection = new Connection(url);
        
         Session.loginWithPassword(connection, userName, password, APIVersion.latest().toString());
            
    }
	
    public boolean isConnected() { return connection != null; }
    
    public void disconnect() throws BadServerResponse, XenAPIException, XmlRpcException {
        Session.logout(connection);
//        connection.dispose();
    }
    
    public HashMap<String, Pcr> getQuoteInformationForHost(String pcrList) {
          System.err.println("stdalex-error getQuoteInformationForHost pcrList == " + pcrList);
          try {
            
              if( !isConnected()) { connect(); }
              
            String nonce = generateNonce();
            String sessionId = generateSessionId();

            System.err.println("stdalex-error connecting with " + userName + " " + password);
            Session.loginWithPassword(connection, userName, password, APIVersion.latest().toString());
			
            System.err.println( "CitrixClient: connected to server ["+hostIpAddress+"]");	
			 
            Map<String, String> myMap = new HashMap<String, String>();
            Set<Host> hostList = Host.getAll(connection);
            Iterator iter = hostList.iterator();
            Host h = (Host)iter.next();
			
            String aik = h.callPlugin(connection,  "tpm","tpm_get_attestation_identity", myMap);
           
            int startP = aik.indexOf("<xentxt:TPM_Attestation_KEY_PEM>");
            int endP   = aik.indexOf("</xentxt:TPM_Attestation_KEY_PEM>");
            // 32 is the size of the opening tag  <xentxt:TPM_Attestation_KEY_PEM>
            String cert = aik.substring(startP + "<xentxt:TPM_Attestation_KEY_PEM>".length(),endP);
            System.err.println("aikCert == " + cert);
            
            keys key = new keys();
            
            key.tpmAttKeyPEM = cert;  // This is the actual value for AIK!!!!!

			
            String aikCertificate = key.tpmAttKeyPEM;
            
            System.err.println( "extracted aik cert from response: " + aikCertificate);
            
            myMap = new HashMap<String, String>();
            myMap.put("nonce",nonce);
            String quote = h.callPlugin(connection, "tpm", "tpm_get_quote", myMap);

            System.err.println("extracted quote from response: "+ quote);
            //saveFile(getCertFileName(sessionId), Base64.decodeBase64(aikCertificate));
            saveFile(getCertFileName(sessionId),aikCertificate.getBytes());
            System.err.println( "saved certificate with session id: "+sessionId);
            
            saveQuote(quote, sessionId);

            System.err.println( "saved quote with session id: "+sessionId);
            
            saveNonce(nonce,sessionId);
            
            System.err.println( "saved nonce with session id: "+sessionId);
            
            //createRSAKeyFile(sessionId);

           System.err.println( "created RSA key file for session id: "+sessionId);
            
            HashMap<String, Pcr> pcrMap = verifyQuoteAndGetPcr(sessionId, pcrList);
            
            System.err.println( "Got PCR map");
            //log.log(Level.INFO, "PCR map = "+pcrMap); // need to untaint this first
            
            return pcrMap;
            
        } catch (ASException e) {
            throw e;
        } catch(UnknownHostException e) {
            throw new ASException(e,ErrorCode.AS_HOST_COMMUNICATION_ERROR, hostIpAddress);
        }  catch (Exception e) {
            System.err.println("stdalex-error caught exception during login: " + e.toString() + " class: " + e.getClass());
            throw new ASException(e);
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

            log.info( "Nonce Generated " + nonce);
            return nonce;
        } catch (NoSuchAlgorithmException e) {
            throw new ASException(e);
        }
    }

    private String generateSessionId() throws NoSuchAlgorithmException  {
        
        // Create a secure random number generator
            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
            // Get 1024 random bits
            byte[] seed = new byte[1];
            sr.nextBytes(seed);

            sr = SecureRandom.getInstance("SHA1PRNG");
            sr.setSeed(seed);
            
            

            int nextInt = sr.nextInt();
            String sessionId = "" + ((nextInt < 0)?nextInt *-1 :nextInt); 


            log.info( "Session Id Generated [" + sessionId + "]");

        

        return sessionId;

    }
    
    private String getNonceFileName(String sessionId) {
        return "nonce_" + sessionId +".data";
    }

    private String getQuoteFileName(String sessionId) {
        return "quote_" + sessionId +".data";
    }

    private void saveCertificate(String aikCertificate, String sessionId) throws IOException  {
        if( aikCertificate.indexOf("-----BEGIN CERTIFICATE-----\n") < 0 && aikCertificate.indexOf("-----BEGIN CERTIFICATE-----") >= 0 ) {
            log.info( "adding newlines to certificate BEGIN tag");            
            aikCertificate = aikCertificate.replace("-----BEGIN CERTIFICATE-----", "-----BEGIN CERTIFICATE-----\n");
        }
        if( aikCertificate.indexOf("\n-----END CERTIFICATE-----") < 0 && aikCertificate.indexOf("-----END CERTIFICATE-----") >= 0 ) {
            log.info( "adding newlines to certificate END tag");            
            aikCertificate = aikCertificate.replace("-----END CERTIFICATE-----", "\n-----END CERTIFICATE-----");
        }

        saveFile(getCertFileName(sessionId), aikCertificate.getBytes());
    }

    private String getCertFileName(String sessionId) {
        return "aikcert_" + sessionId + ".cer";
    }

    private void saveFile(String fileName, byte[] contents) throws IOException  {
        FileOutputStream fileOutputStream = null;

        try {
            assert aikverifyhome != null;
            log.info( String.format("saving file %s to [%s]", fileName, aikverifyhomeData));
            fileOutputStream = new FileOutputStream(aikverifyhomeData + File.separator +fileName);
            assert fileOutputStream != null;
            assert contents != null;
            fileOutputStream.write(contents);
            fileOutputStream.flush();
        }
        catch(FileNotFoundException e) {
            log.info( String.format("cannot save to file %s in [%s]: %s", fileName, aikverifyhomeData, e.getMessage()));
            throw e;
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException ex) {
                    log.info(String.format("Cannot close file %s in [%s]: %s", fileName, aikverifyhomeData, ex.getMessage()));
                }
            }
        }


    }

    private void saveQuote(String quote, String sessionId) throws IOException  {
//          byte[] quoteBytes = new BASE64Decoder().decodeBuffer(quote);
        byte[] quoteBytes = Base64.decodeBase64(quote);
          saveFile(getQuoteFileName(sessionId), quoteBytes);
    }

    private void saveNonce(String nonce, String sessionId) throws IOException  {
//          byte[] nonceBytes = new BASE64Decoder().decodeBuffer(nonce);
        byte[] nonceBytes = Base64.decodeBase64(nonce);
          saveFile(getNonceFileName(sessionId), nonceBytes);
    }

    private void createRSAKeyFile(String sessionId)  {
        
        String command = String.format("%s %s %s",opensslCmd,aikverifyhomeData + File.separator + getCertFileName(sessionId),aikverifyhomeData + File.separator+getRSAPubkeyFileName(sessionId)); 
        log.info( "RSA Key Command " + command);
        CommandUtil.runCommand(command, false, "CreateRsaKey" );
        //log.log(Level.INFO, "Result - {0} ", result);
    }

    private String getRSAPubkeyFileName(String sessionId) {
        return "rsapubkey_" + sessionId + ".key";
    }

    private HashMap<String,Pcr> verifyQuoteAndGetPcr(String sessionId, String pcrList) {
        HashMap<String,Pcr> pcrMp = new HashMap<String,Pcr>();
        System.err.println( "verifyQuoteAndGetPcr for session " + sessionId);
        String command = String.format("%s -c %s %s %s",aikverifyCmd, aikverifyhomeData + File.separator+getNonceFileName( sessionId),
                aikverifyhomeData + File.separator+getCertFileName(sessionId),
                aikverifyhomeData + File.separator+getQuoteFileName(sessionId)); 
        
        System.err.println( "Command: " + command);
        List<String> result = CommandUtil.runCommand(command,true,"VerifyQuote");
        
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
        for(String pcrString: result){
            String[] parts = pcrString.trim().split(" ");
            if( parts.length == 2 ) {
                String pcrNumber = parts[0].trim().replaceAll(pcrNumberUntaint, "").replaceAll("\n", "");
                String pcrValue = parts[1].trim().replaceAll(pcrValueUntaint, "").replaceAll("\n", "");
                boolean validPcrNumber = pcrNumberPattern.matcher(pcrNumber).matches();
                boolean validPcrValue = pcrValuePattern.matcher(pcrValue).matches();
                if( validPcrNumber && validPcrValue ) {
                	System.err.println("Result PCR "+pcrNumber+": "+pcrValue);
                        if(pcrs.contains(pcrNumber)) 
                            pcrMp.put(pcrNumber, new Pcr(new PcrIndex(Integer.parseInt(pcrNumber)), new Sha1Digest(pcrValue)));
                                    //PcrManifest(Integer.parseInt(pcrNumber),pcrValue));            	
                }            	
            }
            else {
            	System.err.println( "Result PCR invalid");
            }
            /*
            if(pcrs.contains(parts[0].trim()))
            	pcrMp.put(parts[0].trim(), new PcrManifest(Integer.parseInt(parts[0]),parts[1]));
            */
        }
        
        return pcrMp;
        
    }
    
    
    public HostInfo getHostInfo() throws NoSuchAlgorithmException, KeyManagementException, MalformedURLException, BadServerResponse, XenAPIException, XenAPIException, XmlRpcException, Exception  {
        //log.info("stdalex-error getHostInfo IP:" + hostIpAddress + " port:" + port + " user: " + userName + " pw:" + password);
         HostInfo response = new HostInfo();
       
              if( !isConnected()) { connect(); }

			
       log.info( "stdalex-error CitrixClient: connected to server ["+hostIpAddress+"]");
			
			 
       Map<String, String> myMap = new HashMap<String, String>();
       Set<Host> hostList = Host.getAll(connection);
       Iterator iter = hostList.iterator();
       Host h = (Host)iter.next();
       
       response.setClientIp(hostIpAddress);
       
       Map<String, String> map = h.getSoftwareVersion(connection);
       response.setOsName(map.get("product_brand"));
       response.setOsVersion(map.get("product_version"));
       response.setVmmName("xen");
       response.setVmmVersion(map.get("xen"));
       
       map = h.getBiosStrings(connection);
       response.setBiosOem(map.get("bios-vendor"));
       response.setBiosVersion(map.get("bios-version"));
       
       java.util.Date date= new java.util.Date();
       response.setTimeStamp( new Timestamp(date.getTime()).toString());
       log.info("stdalex-error leaving getHostInfo");
       return response;
    }

    public String getAIKCertificate() throws NoSuchAlgorithmException, KeyManagementException, BadServerResponse, XenAPIException, XenAPIException, XmlRpcException, Exception {
        String resp = new String();
        log.info("stdalex-error getAIKCert IP:" + hostIpAddress + " port:" + port + " user: " + userName + " pw:" + password);
               
        if( !isConnected()) { connect(); }

       log.info( "stdalex-error CitrixClient: connected to server ["+hostIpAddress+"]");
			
			 
       Map<String, String> myMap = new HashMap<String, String>();
       Set<Host> hostList = Host.getAll(connection);
       Iterator iter = hostList.iterator();
       Host h = (Host)iter.next();
       
       String aik = h.callPlugin(connection,  "tpm","tpm_get_attestation_identity", myMap);
       
       int startP = aik.indexOf("<xentxt:TPM_Attestation_KEY_PEM>");
       int endP   = aik.indexOf("</xentxt:TPM_Attestation_KEY_PEM>");
       // 32 is the size of the opening tag  <xentxt:TPM_Attestation_KEY_PEM>
       String cert = aik.substring(startP + "<xentxt:TPM_Attestation_KEY_PEM>".length(),endP);
       System.err.println("aikCert == " + cert);
      
            
       keys key = new keys();
           
       key.tpmAttKeyPEM = cert;  // This is the actual value for AIK!!!!!

       
       //resp = new String( Base64.decodeBase64(key.tpmAttKeyPEM));
       resp = new String(key.tpmAttKeyPEM);
       
       log.info("stdalex-error getAIKCert: returning back: " + resp);
       return resp;
    }
}
