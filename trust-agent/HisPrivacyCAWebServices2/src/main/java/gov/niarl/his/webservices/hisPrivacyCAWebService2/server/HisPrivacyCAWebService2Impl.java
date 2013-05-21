/*
 * 2012, U.S. Government, National Security Agency, National Information Assurance Research Laboratory
 *
 * This is a work of the UNITED STATES GOVERNMENT and is not subject to copyright protection in the United States. Foreign copyrights may apply.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * ?Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * ?Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * ?Neither the name of the NATIONAL SECURITY AGENCY/NATIONAL INFORMATION ASSURANCE RESEARCH LABORATORY nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package gov.niarl.his.webservices.hisPrivacyCAWebService2.server;

import com.intel.mtwilson.My;
import com.intel.mtwilson.util.ResourceFinder;
import java.io.*;
import java.security.*;
import java.security.cert.*;
import java.security.interfaces.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.*;

import gov.niarl.his.privacyca.*;
import gov.niarl.his.privacyca.TpmUtils.TpmUnsignedConversionException;
import gov.niarl.his.webservices.hisPrivacyCAWebService2.IHisPrivacyCAWebService2;

public class HisPrivacyCAWebService2Impl implements IHisPrivacyCAWebService2 {

	private byte[] identityRequestChallenge = null;
	private RSAPrivateKey caPrivKey = null;
	private X509Certificate caPubCert = null;
	private int validityDays = 0;
	private boolean propFileLoaded = false;
	private Hashtable<Principal, RSAPublicKey> endorsementCerts;
	private X509Certificate ekCert = null;
	TpmIdentityProof idProof = null;
        private  static String homeFolder = "/etc/opt/intel/cloudsecurity/";
        static{
            try {
                String setUpFile = ResourceFinder.getFile("privacyca-client.properties").getAbsolutePath();
                homeFolder = setUpFile.substring(0,setUpFile.indexOf("privacyca-client.properties"));

            } catch (FileNotFoundException ex) {
                Logger.getLogger(HisPrivacyCAWebService2Impl.class.getName()).log(Level.SEVERE, "Error while getting setup.properties.", ex);
            }
        }

	public byte[] identityRequestGetChallenge(byte[] identityRequest, byte[] endorsementCertificate) {
		try {
			if(!propFileLoaded)
				propFileLoaded = readPropertiesFile();
			//decrypt identityRequest and endorsementCertificate
			TpmIdentityRequest idReq = new TpmIdentityRequest(identityRequest);
			idProof = idReq.decrypt(caPrivKey);
			TpmIdentityRequest tempEC = new TpmIdentityRequest(endorsementCertificate);
			ekCert = TpmUtils.certFromBytes(tempEC.decryptRaw(caPrivKey));
			//check out the endorsement certificate
			//if the cert is good, issue challenge; if not return dud
			try{
				if (prepEndorsementCaHashMap())
					ekCert.verify(endorsementCerts.get(ekCert.getIssuerDN()));
				this.identityRequestChallenge = TpmUtils.createRandomBytes(32);
				System.out.println("Endorsement Certificate passed validity check");
			} catch (SignatureException se){
				this.identityRequestChallenge = TpmUtils.hexStringToByteArray("00");
				System.out.println("Endorsement Certificate did not pass validity check");
			}
			//check the rest of the identity proof
			if(!idProof.checkValidity((RSAPublicKey)caPubCert.getPublicKey())){
				this.identityRequestChallenge = TpmUtils.hexStringToByteArray("00");
				System.out.println("Identity Request did not pass validity check");
			}
			//encrypt the challenge and return
			System.out.println("Phase 1 details:");
			System.out.println(" AIK blob: " + TpmUtils.byteArrayToHexString(idProof.getAik().toByteArray()));
			System.out.println(" challenge: " + TpmUtils.byteArrayToHexString(this.identityRequestChallenge));
			byte[] toReturn = createReturn(idProof.getAik(), (RSAPublicKey)ekCert.getPublicKey(), this.identityRequestChallenge);
			System.out.println(" toReturn: " + TpmUtils.byteArrayToHexString(toReturn));
			return toReturn;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public byte[] identityRequestSubmitResponse(byte[] identityRequestResponseToChallenge) {
		try{
			if(!propFileLoaded)
				propFileLoaded = readPropertiesFile();

			//decrypt response
			TpmIdentityRequest returnedIR = new TpmIdentityRequest(identityRequestResponseToChallenge);
			byte[] returned = returnedIR.decryptRaw(caPrivKey);
			//compare decrypted response to challenge
			//if match, create AIC; else create failure code
			byte[] preReturn = null;
			if (TpmUtils.compareByteArrays(returned, this.identityRequestChallenge)){
				preReturn = TpmUtils.makeCert(idProof, caPrivKey, caPubCert, validityDays, 0).getEncoded();
			}else{
				preReturn = TpmUtils.hexStringToByteArray("00");
			}
			//encrypt response and return
			return createReturn(idProof.getAik(), (RSAPublicKey)ekCert.getPublicKey(), preReturn);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	private boolean readPropertiesFile ()
			throws UnrecoverableKeyException,
			KeyStoreException,
			NoSuchAlgorithmException,
			CertificateException,
			IOException,
			javax.security.cert.CertificateException {
		final String P12_FILE_NAME = "P12filename";
		final String P12_PASSWORD = "P12password";
		final String PRIVCA_CERT_VALIDITYDAYS = "PrivCaCertValiditydays";
		String P12filename = null;
		String P12password = null;
		int PrivCaCertValiditydays = 0;
//		String filePath = System.getProperty("catalina.base") + "/webapps/HisPrivacyCAWebServices2/";
		// String propertiesFileName =  "PrivacyCA.properties";
		InputStream PropertyFile = null;



		try
                        {
                        System.out.println("Location " + homeFolder);

			//PropertyFile = new FileInputStream(homeFolder +  propertiesFileName);
			//Properties PrivacyCaProperties = new Properties();
			//PrivacyCaProperties.load(PropertyFile);
			P12filename = My.configuration().getConfiguration().getString(P12_FILE_NAME, null);
			P12password = My.configuration().getConfiguration().getString(P12_PASSWORD, null);
			PrivCaCertValiditydays = My.configuration().getConfiguration().getInt(PRIVCA_CERT_VALIDITYDAYS, 0);
                    //Integer.parseInt(PrivacyCaProperties.getProperty(PRIVCA_CERT_VALIDITYDAYS, "0"));
		} catch (FileNotFoundException e) {
			System.out.println("Error finding Privacy CA properties file: cannot continue. Please place properties file in: home folder." + homeFolder);
			System.out.println(e.toString());
			return false;
		} catch (IOException e) {
			System.out.println("Error loading Privacy CA properties file: cannot continue." + homeFolder);
			return false;
		}
		finally{
			 try {
                 if (PropertyFile != null)
                	 PropertyFile.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		//check to see if defaults are in use
		boolean parameterMissing = false;
		if (P12filename == null){
			System.out.println("Parameter \"P12filename\" missing from properties file: cannot continue.");
			parameterMissing = true;
		}
		if (P12password == null){
			System.out.println("Parameter \"P12password\" missing from properties file: cannot continue.");
			parameterMissing = true;
		}
		if (PrivCaCertValiditydays == 0){
			System.out.println("Parameter \"PrivcaCertValiditydays\" missing from properties file: cannot continue.");
			parameterMissing = true;
		}
		if (parameterMissing){
			return false;
		}


		caPrivKey = TpmUtils.privKeyFromP12(homeFolder +P12filename, P12password);
		caPubCert = TpmUtils.certFromP12(homeFolder + P12filename, P12password);
		validityDays = PrivCaCertValiditydays;
		return true;
	}
	private boolean prepEndorsementCaHashMap() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, javax.security.cert.CertificateException{

		File endorsementCaDir = new File(homeFolder + "/CaCerts".toLowerCase());
		String[] certList = endorsementCaDir.list();
		if(certList == null){
			System.out.println("Problem reading CaCerts directory: "+endorsementCaDir.getAbsolutePath());
			return false;
		}
		endorsementCerts = new Hashtable<Principal, RSAPublicKey>();
		for(int i = 0; i < certList.length; i++){
			X509Certificate tempCert = TpmUtils.certFromFile(endorsementCaDir + "/" + certList[i]);
			endorsementCerts.put((Principal)tempCert.getSubjectDN(), (RSAPublicKey)tempCert.getPublicKey());
		}
		return true;
	}
	private static byte[] createReturn(TpmPubKey aik, RSAPublicKey pubEk, byte[] challengeRaw) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, TpmUnsignedConversionException, IOException{
		byte [] key = TpmUtils.createRandomBytes(16);
		byte [] iv = TpmUtils.createRandomBytes(16);
		byte [] encryptedBlob = TpmUtils.concat(iv, TpmUtils.TCGSymEncrypt(challengeRaw, key, iv));
		byte [] credSize = TpmUtils.intToByteArray(encryptedBlob.length);

		TpmSymmetricKey symKey = new TpmSymmetricKey();
		symKey.setKeyBlob(key);
		symKey.setAlgorithmId(TpmKeyParams.TPM_ALG_AES);
		symKey.setEncScheme(TpmKeyParams.TPM_ES_SYM_CBC_PKCS5PAD);
		TpmKeyParams keyParms = new TpmKeyParams();
		keyParms.setAlgorithmId(TpmKeyParams.TPM_ALG_AES);
		keyParms.setEncScheme(TpmKeyParams.TPM_ES_NONE);
		keyParms.setSigScheme((short)0);
		keyParms.setSubParams(null);
		keyParms.setTrouSerSmode(true);

		byte [] asymBlob = TpmUtils.TCGAsymEncrypt(TpmUtils.concat(symKey.toByteArray(), TpmUtils.sha1hash(aik.toByteArray())), pubEk);
		byte [] symBlob = TpmUtils.concat(TpmUtils.concat(credSize, keyParms.toByteArray()), encryptedBlob);
		return TpmUtils.concat(asymBlob, symBlob);
	}
}