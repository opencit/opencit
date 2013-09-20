package com.intel.mountwilson.his.helper;



/*
 * 2012, U.S. Government, National Security Agency, National Information Assurance Research Laboratory
 * 
 * This is a work of the UNITED STATES GOVERNMENT and is not subject to copyright protection in the United States. Foreign copyrights may apply.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * 锟�Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer. 
 * 锟�Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution. 
 * 锟�Neither the name of the NATIONAL SECURITY AGENCY/NATIONAL INFORMATION ASSURANCE RESEARCH LABORATORY nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import com.intel.mtwilson.crypto.NopX509HostnameVerifier;
import gov.niarl.his.privacyca.IdentityOS;
import gov.niarl.his.privacyca.TpmIdentity;
import gov.niarl.his.privacyca.TpmIdentityRequest;
import gov.niarl.his.privacyca.TpmModule;
import gov.niarl.his.privacyca.TpmPubKey;
import gov.niarl.his.privacyca.TpmUtils;
import gov.niarl.his.webservices.hisPrivacyCAWebService2.IHisPrivacyCAWebService2;
import gov.niarl.his.webservices.hisPrivacyCAWebService2.client.HisPrivacyCAWebServices2ClientInvoker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.HashMap;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.cert.CertificateException;

import org.bouncycastle.util.encoders.Base64;

//import com.intel.mountwilson.as.common.ResourceFinder;
import com.intel.mtwilson.util.ResourceFinder;
import javax.net.ssl.HttpsURLConnection;


/**
 * <p>This is part 2 of 3 for fully provisioning HIS on a Windows client. This part provisions the identity key (AIK) and certificate (AIC) for a HIS client. 
 * <b>Prior to running this client, the HisTpmProvisioner must be run. Following this client, 
 * the HisRegisterIdentity must be run.</b></p>
 * 
 * <p>This class utilizes the TpmModuleJava class, which depends on the NTRU TCS Service running 
 * SOAP services on TCP port 30003. Because of the NTRU requirement, this class wil not work 
 * with Linux using TrouSerS.</p>
 * 
 * <p>This class utilizes a properties file. It looks for a file by the name of "HISprovisioner.properties" in the directory from which Java was invoked.</p>
 * The following values must be in the properties file:
 * <ul>
 * <li><b>TpmOwnerAuth</b> This must be a 40 digit (20 byte) hex code representing the owner auth data which was assigned during TPM provisioning. Owner auth is required for generating a new AIK.<br></li>
 * <li><b>HisIdentityLabel</b> This is the name (text string) that will appear as the Alternate Subject Name, as requested to the Privacy CA.<br></li>
 * <li><b>HisIdentityIndex</b> This is an integer (generally 1) that will be used for storing the AIK. The HIS Standalone will need to know this value.<br></li>
 * <li><b>HisIdentityAuth</b> This must be a 40 digit (20 byte) hex code representing the auth data to be assigned for the AIK. The HIS Standalone will need to know this value.<br></li>
 * <li><b>PrivacyCaCertFile</b> This must be the path to and name of the Privacy CA's certificate. The public key in this certificate will be what is used to encrypt the Identity Request, so it must match the key used by the Privacy CA.<br></li>
 * <li><b>PrivacyCaUrl</b> This is the entire path to the Privacy CA web service. This class assumes the use of HisPrivacyCAWebServices2.</li>
 * <li><b>TrustStore</b> This is the path to and name of the trust store used to encrypt the web service connection. This file must be provided by the web server hosting the Privacy CA.<br></li>
 * <li><b>ClientPath</b> This is the path for the HIS client installation. The AIC (aic.cer) file will be placed there.</li>
 * </ul>
 * 
 * <p>If there are problems with the properties file, context-specific errors will be shown, and an exit code of 99 will be returned.</p> 
 * <p>If the path specified by the ClientPath variable in the properties file cannot be created, an exit code of 2 will be returned.</p> 
 * <p>If there are any problems encountered when running the main function, the stack trace will be displayed and an exit code of 1 will be returned.</p> 
 * <p>If the program runs successfully, an error code of 0 will be returned.</p>
 * 
 * <p>This application assumes the the WELL KNOWN AUTH VALUE (20 bytes of zeros) is used as the 
 * SRK auth value! Though this is normal, a TPM which was owned using version 1 of the 
 * NIARL_TPM_Module or using some other low level tool may not be using this auth value.</p>
 * 
 * <p>Once the AIC is received from the Privacy CA, it is placed at <b>c:\his\aik.cer</b>.</p>
 * 
 * @author schawki
 *
 */
public class CreateIdentity  {
	private static Logger log = LoggerFactory.getLogger(CreateIdentity.class);
	/**
	 * Entry point into the program. See class description for required properties file elements.
	 * 
	 */
	public static void createIdentity() throws PrivacyCAException{
		//Properties file
		// Define properties file strings
		final String OWNER_AUTH = "TpmOwnerAuth";
		final String HIS_IDENTITY_LABEL = "HisIdentityLabel";
		final String HIS_IDENTITY_INDEX = "HisIdentityIndex";
		final String HIS_IDENTITY_AUTH = "HisIdentityAuth";
		final String PRIVACY_CA_CERT = "PrivacyCaCertFile";
		final String PRIVACY_CA_URL = "PrivacyCaUrl";
		final String TRUST_STORE = "TrustStore";
		final String CLIENT_PATH = "ClientPath";
		
		// Instantiate variables to be set by properties file
		byte [] TpmOwnerAuth = null;
		String HisIdentityLabel = "";
		int HisIdentityIndex = 0;
		byte [] HisIdentityAuth = null;
		String PrivacyCaCertFile = "";
		String PrivacyCaUrl = "";
		String TrustStore = "";
		String ClientPath = "";

		// Set properties file name
		String homeFolder = ""; 
		

		// Read the properties file, setting any defaults where it makes sense
		FileInputStream PropertyFile = null;
		try {
			File propFile = ResourceFinder.getFile("hisprovisioner.properties");
			PropertyFile = new FileInputStream(propFile);
			Properties HisProvisionerProperties = new Properties();
			HisProvisionerProperties.load(PropertyFile);
			
			
			homeFolder = propFile.getAbsolutePath();
			homeFolder = homeFolder.substring(0,homeFolder.indexOf("hisprovisioner.properties"));
			
			log.info("Home folder : " + homeFolder);

			TpmOwnerAuth = TpmUtils.hexStringToByteArray(HisProvisionerProperties.getProperty(OWNER_AUTH));
			HisIdentityLabel = HisProvisionerProperties.getProperty(HIS_IDENTITY_LABEL, "");
			HisIdentityIndex = Integer.parseInt(HisProvisionerProperties.getProperty(HIS_IDENTITY_INDEX, "0"));
			HisIdentityAuth = TpmUtils.hexStringToByteArray(HisProvisionerProperties.getProperty(HIS_IDENTITY_AUTH, ""));
			
			
			PrivacyCaCertFile = HisProvisionerProperties.getProperty(PRIVACY_CA_CERT, "");
			PrivacyCaUrl = HisProvisionerProperties.getProperty(PRIVACY_CA_URL, "");
//			TrustStore = HisProvisionerProperties.getProperty(TRUST_STORE, "TrustStore.jks");
			ClientPath = HisProvisionerProperties.getProperty(CLIENT_PATH, "");
		} catch (FileNotFoundException e) { // If the properties file is not found, display error
			throw new PrivacyCAException("Error finding HIS Provisioner properties file (HISprovisionier.properties); using defaults.",e);
		} catch (IOException e) { // If propertied file cannot be read, display error
			throw new PrivacyCAException("Error loading HIS Provisioner properties file (HISprovisionier.properties); using defaults.");
		}
		catch (NumberFormatException e) {
			throw new PrivacyCAException(e);
		}
		finally{
			if (PropertyFile != null)
				try {
					PropertyFile.close();
				} catch (IOException e) {
					log.error("Error while closing the property file " , e);
				}
		}
		// Check to see if any of the values were not populated with acceptable values
		String errorString = "Properties file \"" + homeFolder  + "hisprovisioner.properties contains errors:\n";
		
		boolean hasErrors = false;
		if(TpmOwnerAuth ==null ||  TpmOwnerAuth.length != 20){
			errorString += " - \"TpmOwnerAuth\" value must be a 40 hexidecimal digit (20 byte) value representing the TPM owner authentication\n";
			hasErrors = true;
		}
		if(HisIdentityLabel.length() == 0){
			errorString += " - \"HisIdentityLabel\" value must be the subject name for the AIK certificate\n";
			hasErrors = true;
		}
		if(HisIdentityIndex == 0){
			errorString += " - \"HisIdentityIndex\" value must be the index for AIK storage\n";
			hasErrors = true;
		}
		if(HisIdentityAuth ==null || HisIdentityAuth.length != 20){
			errorString += " - \"HisIdentityAuth\" value must be a 40 hexidecimal digit (20 byte) value representing the AIK authentication\n";
			hasErrors = true;
		}
		if(PrivacyCaCertFile.length() == 0){
			errorString += " - \"PrivacyCaCertFile\" value must be the name of the Privacy CA certificate file\n";
			hasErrors = true;
		}
		if(PrivacyCaUrl.length() == 0){
			errorString += " - \"PrivacyCaUrl\" value must be the name of the URL of the Privacy CA web service\n";
			hasErrors = true;
		}
//		if(TrustStore.length() == 0){
//			errorString += " - \"TrustStore\" value must be the name of the trust store for using the registration web service\n";
//			hasErrors = true;
//		}
		if(ClientPath.length() == 0){
			errorString += " - \"ClientPath\" value must be the path that will be used for installing the HIS Client\n";
			hasErrors = true;
		}
		// If there were errors that prevent the rest of the class from running, display the error specifics and exit with an error code.
		if(hasErrors){
//			System.out.println(errorString);
			throw new PrivacyCAException(errorString);
		}
		
		//System.out.println("Trust store to use :" + System.getProperty("javax.net.ssl.trustStore"));
		
		//System.setProperty("javax.net.ssl.trustStore", "./" + TrustStore);
		
                /*
                // looks like this is already being done somewhere else:
                // check if an identity already exists; if so, do not attempt to create it. if administrator wants to create a new identity, the existing identity must first be deleted from disk.  this version of trust agent supports only a single identity.
		File aikcertFile = new File(homeFolder + ClientPath + File.separator+"aikcert.cer");
                if( aikcertFile.exists() && aikcertFile.isFile() && aikcertFile.canRead() ) {
                    log.info("Identity already exists");
                    return;
                }
                */

		//Provision an identity for HIS
		log.info("Performing HIS identity provisioning...");
		
		FileOutputStream pcaFileOut = null;
		try{
			byte[] srkAuth = TpmUtils.hexStringToByteArray("0000000000000000000000000000000000000000");
			boolean requiresAuthSha = false;
			byte[] ownerAuthRaw = TpmOwnerAuth;
			byte[] keyAuthRaw = HisIdentityAuth;
			byte[] srkAuthRaw = srkAuth;
			if (requiresAuthSha){
				ownerAuthRaw = TpmUtils.sha1hash(TpmOwnerAuth);
				keyAuthRaw = TpmUtils.sha1hash(HisIdentityAuth);
				srkAuthRaw = TpmUtils.sha1hash(srkAuth);
			}
			
			
			X509Certificate pcaCert = TpmUtils.certFromFile(homeFolder + PrivacyCaCertFile);
			boolean shortcut = true;
			
			byte[] ekCert = TpmModule.getCredential(TpmOwnerAuth, "EC");
			TpmIdentityRequest encryptedEkCert = new TpmIdentityRequest(ekCert, (RSAPublicKey)pcaCert.getPublicKey(), false); 
			
	
			TpmIdentity newId = TpmModule.collateIdentityRequest(TpmOwnerAuth, HisIdentityAuth, HisIdentityLabel, new TpmPubKey((RSAPublicKey)pcaCert.getPublicKey(), 3, 1).toByteArray(), HisIdentityIndex, (X509Certificate)null, !shortcut);
			
                        
//                        HttpsURLConnection.setDefaultHostnameVerifier(new NopX509HostnameVerifier()); // XXX TODO Bug #497 need to allow caller to specify a TlsPolicy // disabled for testing issue #541
            System.err.println("Create Identity... Calling into HisPriv first time. using url = " + PrivacyCaUrl);
			IHisPrivacyCAWebService2 hisPrivacyCAWebService2 = HisPrivacyCAWebServices2ClientInvoker.getHisPrivacyCAWebService2(PrivacyCaUrl);
            System.err.println("Create Identity... Got HisPrivCA ref, making request ize of msg = " + encryptedEkCert.toByteArray().length);
			byte[] encrypted1 = hisPrivacyCAWebService2.identityRequestGetChallenge(newId.getIdentityRequest(), encryptedEkCert.toByteArray());
			if(encrypted1.length == 1){
				throw new PrivacyCAException("Identity request was rejected by Privacy CA in phase 1 of process");
			}
			//TpmKey aik = new TpmKey(newId.getAikBlob());
			
			int os = IdentityOS.osType();//return os type. win:0; linux:1; other:-1
			
			byte[] asym1 = new byte[256];
			System.arraycopy(encrypted1, 0, asym1, 0, asym1.length);
			byte[] sym1 = new byte[encrypted1.length - 256];
			System.arraycopy(encrypted1, 256, sym1, 0, sym1.length);
			byte[] decrypted1;
			if (os==1){//linux
				//decrypted1 = TpmModule.activateIdentity(ownerAuthRaw, keyAuthRaw, asym1, sym1, HisIdentityIndex);
				HashMap<String, byte[]> results = TpmModule.activateIdentity2(ownerAuthRaw, keyAuthRaw,asym1, sym1, HisIdentityIndex);

				decrypted1 = results.get("aikcert");
			}else
				//decrypted1 = TpmModuleJava.ActivateIdentity(asym1, sym1, aik, keyAuthRaw, srkAuthRaw, ownerAuthRaw); //Comments  temporarily due to TSSCoreService.jar compiling issue 
				decrypted1 = TpmModule.activateIdentity(ownerAuthRaw, keyAuthRaw, asym1, sym1, HisIdentityIndex);
			
			TpmIdentityRequest encryptedChallenge = new TpmIdentityRequest(decrypted1, (RSAPublicKey)pcaCert.getPublicKey(), false);
            System.err.println("Create Identity... Calling into HisPriv second time, size of msg = " + encryptedChallenge.toByteArray().length);
			byte[] encrypted2 = hisPrivacyCAWebService2.identityRequestSubmitResponse(encryptedChallenge.toByteArray());
			if(encrypted2.length == 1){
				log.warn("Identity request was rejected by Privacy CA in phase 2 of process");
				throw new Exception("Identity request was rejected by Privacy CA in phase 2 of process");
			}
			byte[] asym2 = new byte[256];
			System.arraycopy(encrypted2, 0, asym2, 0, asym2.length);
			byte[] sym2 = new byte[encrypted2.length - 256];
			System.arraycopy(encrypted2, 256, sym2, 0, sym2.length);
			byte[] decrypted2;
			byte[] aikblob;
			if (os==1){//linux
				HashMap<String, byte[]> results = TpmModule.activateIdentity2(ownerAuthRaw, keyAuthRaw,asym2, sym2, HisIdentityIndex);
				System.out.println(results);

				decrypted2 = results.get("aikcert");
				aikblob = results.get("aikblob");
				log.debug(homeFolder + ClientPath + "/aikcert.cer >>>");
				writecert(homeFolder + ClientPath, decrypted2,"/aikcert.cer");
                                log.debug(homeFolder + ClientPath + "/aikcert.cer <<<");
				writeFile(homeFolder + ClientPath, aikblob,"/aikblob.dat");
				
				
			}else{
				//decrypted1 = TpmModuleJava.ActivateIdentity(asym1, sym1, aik, keyAuthRaw, srkAuthRaw, ownerAuthRaw); 
				//decrypted2 = TpmModuleJava.ActivateIdentity(asym2, sym2, aik, keyAuthRaw, srkAuthRaw, ownerAuthRaw);//Comments  temporarily due to TSSCoreService.jar compiling issue 
				decrypted2 = TpmModule.activateIdentity(ownerAuthRaw, keyAuthRaw,asym2, sym2, HisIdentityIndex);
				writecert(homeFolder + ClientPath, decrypted2,"/aikcert.cer");
			}
			
			
		}catch(Exception e){
			throw new PrivacyCAException("FAILED: " + e.getMessage(),e);
		}
		finally{
			if (pcaFileOut != null)
				try {
					pcaFileOut.close();
				} catch (IOException e) {
					log.error("Error while closing pcaFileOut",e);
				}
		}
	
		log.info("DONE");
		
	}

	private static void writecert(String clientPath, byte[] decrypted,
			String fileName) throws CertificateException, IOException {
			log.info("writing file " + clientPath + fileName);
			javax.security.cert.X509Certificate cert = 
					javax.security.cert.X509Certificate.getInstance(decrypted);
			
			FileOutputStream pcaFileOut;
			
			File outPath = new File(clientPath);
			File outFile = new File(clientPath + fileName);
			if(!outPath.isDirectory()){
				if(!outPath.mkdirs()){
					System.out.println("Failed to create client installation path!");
					System.exit(5);
				}
			}
			pcaFileOut = new FileOutputStream(outFile);
			pcaFileOut.write("-----BEGIN CERTIFICATE-----\n".getBytes());
                        String code = Base64.encode(cert.getEncoded()).toString();
                        int line = 0;
                        int remaining = code.length();
                        while(remaining > 76){
                        pcaFileOut.write((code.substring(line*76, line*76+76)+"\n").getBytes());
                        line++;
                        remaining -= 76;
                        }    
                        
                        pcaFileOut.write(code.substring(line*76,line*76+remaining).getBytes());
			pcaFileOut.write("\n-----END CERTIFICATE-----\n".getBytes());
			pcaFileOut.flush();
			pcaFileOut.close();
		
	}

	private static void writeFile(String ClientPath,
			byte[] decrypted2, String fileName) throws FileNotFoundException, IOException, PrivacyCAException {
		
		log.info("writing file " + ClientPath + fileName);
		
		FileOutputStream pcaFileOut;
		File outPath = new File(ClientPath);
		File outFile = new File(ClientPath + fileName);
		if(!outPath.isDirectory()){
			if(!outPath.mkdirs()){
				log.warn("Failed to create client installation path!");
				throw new PrivacyCAException("Failed to create client installation path!");
			}
		}
		pcaFileOut = new FileOutputStream(outFile);
		pcaFileOut.write(decrypted2);
		pcaFileOut.flush();
		pcaFileOut.close();
	}
}


