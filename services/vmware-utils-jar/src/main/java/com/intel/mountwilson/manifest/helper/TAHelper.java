package com.intel.mountwilson.manifest.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import javax.persistence.EntityManagerFactory;
import javax.xml.bind.JAXBException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.IOUtils;

import com.intel.mountwilson.as.common.ASConfig;
import com.intel.mountwilson.as.common.ASException;
import com.intel.mountwilson.as.helper.CommandUtil;
import com.intel.mountwilson.as.helper.TrustAgentSecureClient;
import com.intel.mountwilson.manifest.data.PcrManifest;
import com.intel.mountwilson.ta.data.ClientRequestType;
import com.intel.mountwilson.ta.data.daa.response.DaaResponse;
import com.intel.mtwilson.datatypes.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dsmagadx
 */
public class TAHelper {
    private Logger log = LoggerFactory.getLogger(getClass());

    private String aikverifyhome;
    private String aikverifyhomeData;
    private String aikverifyhomeBin;
    private String opensslCmd;
    private String aikverifyCmd;
    
    private Pattern pcrNumberPattern = Pattern.compile("[0-9]|[0-1][0-9]|2[0-3]"); // integer 0-23 with optional zero-padding (00, 01, ...)
    private Pattern pcrValuePattern = Pattern.compile("[0-9a-fA-F]{40}"); // 40-character hex string
    private String pcrNumberUntaint = "[^0-9]";
    private String pcrValueUntaint = "[^0-9a-fA-F]";
	private EntityManagerFactory entityManagerFactory;
    
    private TAHelper(){
    	
    }
    public TAHelper(EntityManagerFactory entityManagerFactory) {
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
                log.error( String.format("Invalid service configuration: Cannot find %s", filename ));
                foundAllRequiredFiles = false;
            }
        }
        if( !foundAllRequiredFiles ) {
            throw new ASException(ErrorCode.AS_CONFIGURATION_ERROR, "Cannot find aikverify files");
        }
        
        // we must be able to write to the data folder in order to save certificates, nones, public keys, etc.
        File datafolder = new File(aikverifyhomeData);
        if( !datafolder.canWrite() ) {
            throw new ASException(ErrorCode.AS_CONFIGURATION_ERROR, String.format(" Cannot write to %s", aikverifyhomeData));            
        }
        
        this.setEntityManagerFactory(entityManagerFactory);
    }

    // DAA challenge
    public void verifyAikWithDaa(String hostIpAddress, int port) {
        
        try {
            TrustAgentSecureClient client = new TrustAgentSecureClient(hostIpAddress, port);
            String sessionId = generateSessionId();

            // request AIK certificate and CA chain (the AIK Proof File)
            System.out.println("DAA requesting AIK proof");
            String aikproof = client.getAIKCertificate(); // <identity_request></identity_request>
            FileOutputStream outAikProof = new FileOutputStream(new File(getDaaAikProofFileName(sessionId)));
            IOUtils.write(aikproof, outAikProof);
            IOUtils.closeQuietly(outAikProof);
            
            // TODO: verify issuer chain for the certificate so we can attest to the hardware if we recognize the manufacturer
            
            // create DAA challenge secret
            SecureRandom random = new SecureRandom();
            byte[] secret = new byte[20];
            random.nextBytes(secret);
            FileOutputStream outSecret = new FileOutputStream(new File(getDaaSecretFileName(sessionId)));
            IOUtils.write(secret, outSecret);
            IOUtils.closeQuietly(outSecret);
            
            // encrypt DAA challenge secret using AIK public key so only TPM can read it
            CommandUtil.runCommand(String.format("aikchallenge %s %s %s %s", 
                    getDaaSecretFileName(sessionId), 
                    getDaaAikProofFileName(sessionId), 
                    getDaaChallengeFileName(sessionId), 
                    getRSAPubkeyFileName(sessionId)), false, "Aik Challenge");
            
            // send DAA challenge to Trust Agent and validate the response
            FileInputStream in = new FileInputStream(new File(getDaaChallengeFileName(sessionId)));
            String challenge = IOUtils.toString(in);
            IOUtils.closeQuietly(in);
            DaaResponse response = client.sendDaaChallenge(challenge);
            byte[] responseContentDecoded = Base64.decodeBase64(response.getContent());
            if( responseContentDecoded.length != secret.length ) {
                throw new ASException(ErrorCode.AS_TRUST_AGENT_DAA_ERROR, "Incorrect challenge response");                
            }
            for(int i=0; i<secret.length; i++) {
                if( responseContentDecoded[i] != secret[i] ) {
                    throw new ASException(ErrorCode.AS_TRUST_AGENT_DAA_ERROR, "Incorrect challenge response");                        
                }
            }
           
            // TODO: Trust Agent is validated so now save the AIK certificate and RSA public key in the DATABASE ... 
            
        } catch (KeyManagementException ex) {
            log.error("Cannot verify AIK: "+ex.getMessage(), ex);
        } catch (UnknownHostException ex) {
            log.error("Cannot verify AIK: "+ex.getMessage(), ex);
        } catch (JAXBException ex) {
            log.error("Cannot verify AIK: "+ex.getMessage(), ex);
        } catch (IOException ex) {
            log.error("Cannot verify AIK: "+ex.getMessage(), ex);
        } catch (NoSuchAlgorithmException ex) {
            log.error("Cannot verify AIK: "+ex.getMessage(), ex);
        } catch (ASException ex) {
            throw ex;
        }
    }
    
    public HashMap<String, PcrManifest> getQuoteInformationForHost(String hostIpAddress, String pcrList, String name, int port) {

          try {
            TrustAgentSecureClient client = new TrustAgentSecureClient(hostIpAddress, port);

            String nonce = generateNonce();

            String sessionId = generateSessionId();

            ClientRequestType clientRequestType = client.getQuote(nonce, pcrList);
            log.info( "got response from server ["+hostIpAddress+"] "+clientRequestType);
            String aikCertificate = clientRequestType.getAikcert();
            
            log.info( "extracted aik cert from response: "+aikCertificate);
            
            String quote = clientRequestType.getQuote();

            log.info( "extracted quote from response: "+quote);
            saveCertificate(aikCertificate, sessionId);
            
            log.info( "saved certificate with session id: "+sessionId);
            
            saveQuote(quote, sessionId);

            log.info( "saved quote with session id: "+sessionId);
            
            saveNonce(nonce,sessionId);
            
            log.info( "saved nonce with session id: "+sessionId);
            
            createRSAKeyFile(sessionId);

            log.info( "created RSA key file for session id: "+sessionId);
            
            HashMap<String, PcrManifest> pcrMap = verifyQuoteAndGetPcr(sessionId);
            
            log.info( "Got PCR map");
            //log.log(Level.INFO, "PCR map = "+pcrMap); // need to untaint this first
            
            return pcrMap;
            
        } catch (ASException e) {
            throw e;
        } catch(UnknownHostException e) {
            throw new ASException(e,ErrorCode.AS_HOST_COMMUNICATION_ERROR, hostIpAddress);
        }  catch (Exception e) {
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

            log.info( "Nonce Generated {}", nonce);
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


            log.info( "Session Id Generated [{}]", sessionId);

        

        return sessionId;

    }
    
    // for DAA
    private String getDaaAikProofFileName(String sessionId) {
        return "daaaikproof_"+sessionId+".data";
    }
    private String getDaaSecretFileName(String sessionId) {
        return "daasecret_"+sessionId+".data";
    }
    private String getDaaChallengeFileName(String sessionId) {
        return "daachallenge_"+sessionId+".data";
    }
    /*
    private String getDaaResponseFileName(String sessionId) {
        return "daaresponse_"+sessionId+".data";
    }
    */

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
                    log.error(String.format("Cannot close file %s in [%s]: %s", fileName, aikverifyhomeData, ex.getMessage()), ex);
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
        log.info( "RSA Key Command {}", command);
        CommandUtil.runCommand(command, false, "CreateRsaKey" );
        //log.log(Level.INFO, "Result - {0} ", result);
    }

    private String getRSAPubkeyFileName(String sessionId) {
        return "rsapubkey_" + sessionId + ".key";
    }

    private HashMap<String,PcrManifest> verifyQuoteAndGetPcr(String sessionId) {
        HashMap<String,PcrManifest> pcrMp = new HashMap<String,PcrManifest>();
        log.info( "verifyQuoteAndGetPcr for session {}",sessionId);
        String command = String.format("%s -c %s %s %s",aikverifyCmd, aikverifyhomeData + File.separator+getNonceFileName( sessionId),
                aikverifyhomeData + File.separator+getRSAPubkeyFileName(sessionId),aikverifyhomeData + File.separator+getQuoteFileName(sessionId)); 
        
        log.info( "Command: {}",command);
        List<String> result = CommandUtil.runCommand(command,true,"VerifyQuote");
        
        // Sample output from command:
        //  1 3a3f780f11a4b49969fcaa80cd6e3957c33b2275
        //  17 bfc3ffd7940e9281a3ebfdfa4e0412869a3f55d8
        //log.log(Level.INFO, "Result - {0} ", result); // need to untaint this first
        
        //List<String> pcrs = getPcrsList(); // replaced with regular expression that checks 0-23
        
        for(String pcrString: result){
            String[] parts = pcrString.trim().split(" ");
            if( parts.length == 2 ) {
                String pcrNumber = parts[0].trim().replaceAll(pcrNumberUntaint, "").replaceAll("\n", "");
                String pcrValue = parts[1].trim().replaceAll(pcrValueUntaint, "").replaceAll("\n", "");
                boolean validPcrNumber = pcrNumberPattern.matcher(pcrNumber).matches();
                boolean validPcrValue = pcrValuePattern.matcher(pcrValue).matches();
                if( validPcrNumber && validPcrValue ) {
                	log.info("Result PCR "+pcrNumber+": "+pcrValue);
                	pcrMp.put(pcrNumber, new PcrManifest(Integer.parseInt(pcrNumber),pcrValue));            	
                }            	
            }
            else {
            	log.warn( "Result PCR invalid");
            }
            /*
            if(pcrs.contains(parts[0].trim()))
            	pcrMp.put(parts[0].trim(), new PcrManifest(Integer.parseInt(parts[0]),parts[1]));
            */
        }
        
        return pcrMp;
        
    }
    
	public EntityManagerFactory getEntityManagerFactory() {
		return entityManagerFactory;
	}
	
	public void setEntityManagerFactory(EntityManagerFactory entityManagerFactory) {
		this.entityManagerFactory = entityManagerFactory;
	}

    /*
	private List<String> getPcrsList() {
		List<String> pcrs = new ArrayList<String>() ;
		
		for(int i = 0 ; i< 24 ; i++)
			pcrs.add(String.valueOf(i));
		
		return pcrs;
	}
	*/

    
    
    
    
    
    
}
