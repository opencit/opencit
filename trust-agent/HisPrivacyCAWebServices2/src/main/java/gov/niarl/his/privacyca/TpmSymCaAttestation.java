/*
 * 2012, U.S. Government, National Security Agency, National Information Assurance Research Laboratory
 * 
 * This is a work of the UNITED STATES GOVERNMENT and is not subject to copyright protection in the United States. Foreign copyrights may apply.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * �Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer. 
 * �Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution. 
 * �Neither the name of the NATIONAL SECURITY AGENCY/NATIONAL INFORMATION ASSURANCE RESEARCH LABORATORY nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package gov.niarl.his.privacyca;

import java.io.ByteArrayInputStream;
import java.security.cert.*;
//import java.util.*;
import java.io.*;
import java.security.*;
import javax.crypto.*;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * <p>This class is for the TCG's TPM_SYM_CA_ATTESTATION structure.</p>
 * @author schawki
 *
 */
public class TpmSymCaAttestation {
	private TpmKeyParams algorithm;
	private byte [] plainAikCred = null;
	private byte [] encAikCred;
	
	public TpmSymCaAttestation(){}
	/**
	 * Set the AIK certificate to be encrypted.
	 * 
	 * @param cred The AIK certificate in byte array form.
	 */
	public void setAikCredential(byte [] cred) {
		plainAikCred = cred;
	}
	/**
	 * Set the AIK certificate to be encrypted.
	 * 
	 * @param cred The AIK certificate in X509Certificate form.
	 * @throws CertificateEncodingException
	 */
	public void setAikCredential(X509Certificate cred) throws CertificateEncodingException {
		plainAikCred = cred.getEncoded();
	}
	/**
	 * @return The stored AIK certificate in X509Certificate form.
	 * @throws CertificateException
	 */
	public X509Certificate getAikCredential()
			throws CertificateException {
		ByteArrayInputStream bs = new ByteArrayInputStream(plainAikCred);
		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		return (X509Certificate)cf.generateCertificate(bs);
	}
	/**
	 * Encrypt the stored certificate using the specified options. A random key and IV are created.
	 * 
	 * @param algMode Not used. This parameter is left here to maintain compatibility. This value is hard coded to TPM_ALG_AES.
	 * @param encScheme Not used. This parameter is left here to maintain compatibility. This value is hard coded to TPM_ES_CBC_PKCS5PAD.
	 * @param TrousersModeIV Set to TRUE to use a TrouSerS-style initialization vector placement.
	 * @param TrousersModeSymkeyEncscheme Set to TRUE to set the encryption scheme to TPM_ES_NONE. (Not all versions of TrouSerS use this scheme, but the only ones available in RedHat Yum repositories do.)
	 * @return The symmetric key used to encrypt the certificate in the form of a TpmSymmetricKey. The encrypted certificate itself is retrieved in raw byte format by running the toByteArray() function.
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidAlgorithmParameterException
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws PrivacyCaException
	 */
	public TpmSymmetricKey encrypt(int algMode, short encScheme, boolean TrousersModeIV, boolean TrousersModeSymkeyEncscheme)
			throws IOException, 
			NoSuchAlgorithmException, 
			NoSuchPaddingException, 
			InvalidAlgorithmParameterException, 
			InvalidKeyException, 
			IllegalBlockSizeException, 
			BadPaddingException,
			PrivacyCaException {
		if (plainAikCred == null) {
			throw new PrivacyCaException("TpmSymCaAttestation: Must store certificate prior to encrypting.");
		}
		algorithm = new TpmKeyParams();
		algorithm.setAlgorithmId(TpmKeyParams.TPM_ALG_AES);
		algorithm.setEncScheme(TpmKeyParams.TPM_ES_SYM_CBC_PKCS5PAD);
		algorithm.setSigScheme(TpmKeyParams.TPM_SS_NONE);
		algorithm.setSubParams(new TpmSymmetricKeyParams());
		algorithm.getSubParams().setKeyLength(128);
		algorithm.getSubParams().setValueData(16);
		//create a random IV (16 byte)
		byte [] newIv = TpmUtils.createRandomBytes(16);
		algorithm.getSubParams().setByteData(newIv); //copy IV into params
		algorithm.setTrouSerSmode(TrousersModeIV); //set trousers mode
		//create a random key for AES (128 bit = 16 byte)
		byte [] newKey = TpmUtils.createRandomBytes(16);
		//System.out.println("The symmetric key is " + newKey.length + " bytes long");
		//encrypt
		Cipher symCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		IvParameterSpec ivSpec = new IvParameterSpec(newIv);
		SecretKeySpec symKey = new SecretKeySpec(newKey, "AES");
		symCipher.init(Cipher.ENCRYPT_MODE, symKey, ivSpec);
		encAikCred = symCipher.doFinal(plainAikCred);
		if (TrousersModeIV) { //copy iv to from of encrypted portion
			byte [] temp = new byte[newIv.length + encAikCred.length];
			System.arraycopy(newIv, 0, temp, 0, newIv.length);
			System.arraycopy(encAikCred, 0, temp, newIv.length, encAikCred.length);
			encAikCred = temp;
		}
		else {
			algorithm.setSubParams(new TpmSymmetricKeyParams());
			algorithm.getSubParams().setByteData(newIv);
			algorithm.getSubParams().setValueData(16);
			algorithm.getSubParams().setKeyLength(128);
		}
		//set the Symkey for return
		TpmSymmetricKey encryptKey = new TpmSymmetricKey();
		encryptKey.setAlgorithmId(TpmKeyParams.TPM_ALG_AES);
		encryptKey.setEncScheme(TpmKeyParams.TPM_ES_SYM_CBC_PKCS5PAD);
		encryptKey.setSecretKey(symKey);
		if (TrousersModeSymkeyEncscheme)
			encryptKey.setEncScheme(TpmKeyParams.TPM_ES_NONE);
		return encryptKey;
	}
	/**
	 * Serialize the structure.
	 * 
	 * @return A byte array form of the TpmSymCaAttestation structure.
	 * @throws TpmUtils.TpmUnsignedConversionException
	 */
	public byte [] toByteArray()
			throws TpmUtils.TpmUnsignedConversionException {
		byte [] credSize = TpmUtils.intToByteArray(encAikCred.length); //credSize.length must be 4!
		byte [] tempAlgo = algorithm.toByteArray();
		int x = credSize.length + tempAlgo.length + encAikCred.length;
		byte [] returnArray = new byte[x];
		System.arraycopy(credSize, 0, returnArray, 0, credSize.length);
		System.arraycopy(tempAlgo, 0, returnArray, credSize.length, tempAlgo.length);
		System.arraycopy(encAikCred, 0, returnArray, credSize.length + tempAlgo.length, encAikCred.length);
		return returnArray;
	}
}
