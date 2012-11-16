/*
 * 2011, U.S. Government, National Security Agency, National Information Assurance Research Laboratory
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

package gov.niarl.his.privacyca;

import gov.niarl.his.privacyca.TpmModule.TpmModuleException;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.util.logging.Logger;

/**
 * <p>This class provides macro-level client provisioning functions, such
 * as taking ownership, provisioning the EC, and creating a new identity key.
 * All functions are statically called.</p>
 *
 * @author schawki
 *
 */
public class TpmClient {
	/**
	 * This function combines taking ownership and creating an endorsement key (EK) certificate (EC). This function essentially serves as the Certificate Authority for the EC.
	 *
	 * @param ownerAuth 20-byte owner auth string, needed to retrieve the EK.
	 * @param caPrivKey The RSA private key needed to sign the EC.
	 * @param caCert The CA certificate associated with the caPrivKey.
	 * @param ecValidDays The number of days before EC expires. This should be about the same time as the expected lifetime/use of the computer holding the TPM.
	 * @throws IOException Thrown for a number of reasons dealing with communication to the TPM, creation of random numbers, and file access.
	 * @throws TpmModuleException Thrown if there is a problem communicating to the TPM through the TPM Module.
	 * @throws InvalidKeyException Error when creating the EC.
	 * @throws CertificateEncodingException Error when creating the EC.
	 * @throws NoSuchAlgorithmException Error when creating the EC.
	 * @throws InvalidKeySpecException Error when creating the EC.
	 * @throws SignatureException Error when creating the EC.
	 * @throws NoSuchProviderException Error using the BouncyCastle provider, needed for creation of the certificate.
	 */
	public static void provisionTpm(byte [] ownerAuth, RSAPrivateKey caPrivKey, X509Certificate caCert, int ecValidDays)
			throws IOException,
			TpmModuleException,
			InvalidKeyException,
			CertificateEncodingException,
			NoSuchAlgorithmException,
			InvalidKeySpecException,
			SignatureException,
			NoSuchProviderException{
		/*
		 * The following actions must be performed during the TPM Provisioning process:
		 * 1. Take ownership of the TPM
		 * 		- owner auth
		 * 2. Create an Endorsement Certificate (EC)
		 * 		- public EK
		 * 			- owner auth (should already have from above)
		 * 		- private key and cert for CA to create new cert
		 * 		- validity period of EC cert
		 * 3. Store the newly created EC in the TPM's NV-RAM
		 */
		// Take Ownership
		byte [] nonce = TpmUtils.createRandomBytes(20);
		try {
			TpmModule.takeOwnership(ownerAuth, nonce);
		} catch (TpmModuleException e){
			if(e.toString().contains(".takeOwnership returned nonzero error: 4")){
				Logger.getLogger(TpmClient.class.getName()).info("Ownership is already taken : " );
				return;
			}
			else
				throw e;
		}
		// Create Endorsement Certificate
		nonce = TpmUtils.createRandomBytes(20);
		try {
			byte [] pubEkMod = TpmModule.getEndorsementKeyModulus(ownerAuth, nonce);
			X509Certificate ekCert = TpmUtils.makeEkCert(pubEkMod, caPrivKey, caCert, ecValidDays);
			TpmModule.setCredential(ownerAuth, "EC", ekCert.getEncoded());
		} catch (TpmModuleException e){
			Logger.getLogger(TpmClient.class.getName()).info("Error getting PubEK: " + e.toString());
			throw e;
		}
		// Store the new EC in NV-RAM
	}
	/**
	 * @deprecated
	 * Creates a new AIK and contacts a Privacy CA for an AIC.
	 *
	 * @param ownerAuth 20-byte owner auth, needed to do CollateIdentityRequest.
	 * @param pcaCert X.509 certificate for the Privacy CA, used to extract the public key.
	 * @param aikIndex Index to store the AIK key blob.
	 * @param keyAuth 20-byte auth data to assign to the new AIK.
	 * @param idLabel String to use in request for AIC. If the Privacy CA accepts the string, it sill be used as the SubjectAlternativeName in the AIC.
	 * @param pcaUrl The URL for the PrivacyCA web service. This does not work for HisPrivacyCAWebServices2.
	 * @param shortcut Must be TRUE (due flaws in the TSS implementations).
	 * @return The AIK in X.509 certificate format.
	 * @throws IOException
	 * @throws TpmModuleException
	 * @throws TpmUnsignedConversionException
	 * @throws CertificateException
	 * @throws javax.security.cert.CertificateException
	 */
//	public static X509Certificate provisionIdentity(byte [] ownerAuth, X509Certificate pcaCert, int aikIndex, byte [] keyAuth, String idLabel, String pcaUrl, boolean shortcut)
//			throws IOException,
//			TpmModuleException,
//			TpmUnsignedConversionException,
//			CertificateException,
//			javax.security.cert.CertificateException{
//		/*
//		 * The following actions must be performed during the Identity Provisioning process:
//		 * 1. Perform a CollateIdentity
//		 * 2. Contact the Privacy CA (must know if performing full procedure)
//		 * 3. Perform ActivateIdentity (if doing full procedure)
//		 */
//		// CollateIdentity
//		TpmIdentity newId = TpmModule.collateIdentityRequest(ownerAuth, keyAuth, idLabel, new TpmPubKey((RSAPublicKey)pcaCert.getPublicKey(), 3, 1).toByteArray(), aikIndex, (X509Certificate)null, !shortcut);
//		X509Certificate toReturn = null;
//		if (shortcut){
//			//System.out.println("Shortcut");
//			HisPrivacyCAWebService hisPrivacyCAWebService = HisPrivacyCAWebServicesClientInvoker.getHisPrivacyCAWebService(pcaUrl);
//			//System.out.println("\n" + TpmUtils.byteArrayToHexString(newId.getIdentityRequest()) + "\n");
//			byte [] tempBytes = hisPrivacyCAWebService.partialIdentityRequest(newId.getIdentityRequest());
//			//System.out.println("Privacy CA response: " + TpmUtils.byteArrayToHexString(tempBytes));
//			toReturn = TpmUtils.certFromBytes(tempBytes);
//		} else {
//			System.out.println("Identity Request: " + TpmUtils.byteArrayToHexString(newId.getIdentityRequest()));
//
//			//System.out.println("NO Shortcut");
//			HisPrivacyCAWebService hisPrivacyCAWebService = HisPrivacyCAWebServicesClientInvoker.getHisPrivacyCAWebService(pcaUrl);
//			EncryptedCAResponse pcaResponse = hisPrivacyCAWebService.identityRequest(newId.getIdentityRequest());
//			toReturn = TpmUtils.certFromBytes(TpmModule.activateIdentity(ownerAuth, keyAuth, pcaResponse.getEncryptedKey(), pcaResponse.getEncryptedCert(), aikIndex));
//		}
//		return toReturn;
//	}
}
