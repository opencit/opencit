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

import javax.crypto.Cipher;
import java.security.interfaces.*;
import java.security.spec.*;
import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.*;

import java.io.*;

/**
 * <p>The TpmIdentityRequest class is based on the TPM_IDENTITY_REQUEST structure and includes 
 * associated methods. An identity request is generally created by running the TSS function 
 * <b>Tspi_TPM_CollateIdentityRequest</b>. The request contains two parts: a symmetrically 
 * encrypted identity proof (TPM_IDENTITY_PROOF); and an asymmetrically encrypted portion 
 * containing the key used to encrypt the symmetric portion. This class contains 
 * functionality to decode and decrypt the identity request and return an identity proof. 
 * There is also functionality to create a new request, given an identity proof and the key 
 * needed to encrypt it.</p>
 * <p>Different implementations of the TSS create slightly different formats of identity requests. 
 * The two version of TSS researched in the development of this class were NTru's CTSS v1.2.1.29 
 * and IBM's TrouSerS v</p>
 * 
 * @author schawki
 *
 */
public class TpmIdentityRequest {
	private byte[] asymBlob;
	private byte[] symBlob;
	private TpmKeyParams asymAlgorithm;
	private TpmKeyParams symAlgorithm;
	private boolean TrousersModeIV = false;
	private boolean TrousersModeSymkeyEncscheme = false;
	private boolean TrousersModeBlankOeap = false;
	
	public byte[] getAsymBlob(){
		return asymBlob;
	}
	public byte[] getSymBlob(){
		return symBlob;
	}
	
	/**
	 * Get a copy of the flag used to indicate the placement of the initialization vector used for this request.
	 * 
	 * @return <b>True</b> indicates that the placement is the first part of the symmetrically encrypted blob; <b>false</b> indicates that the placement is within the TPM_SYMMETRIC_KEY_PARMS portion of the TPM_KEY_PARMS structure used to describe the symmetric key usage.
	 */
	public boolean getIVmode(){
		return TrousersModeIV;
	}
	/**
	 * Set the flag used to dictate the placement of the initialization vector for this request.
	 * 
	 * @param newMode <b>True</b> indicates that the placement be the first part of the symmetrically encrypted blob; <b>false</b> indicates that the placement be within the TPM_SYMMETRIC_KEY_PARMS portion of the TPM_KEY_PARMS structure used to describe the symmetric key usage.
	 */
	public void setIVmode(boolean newMode) {
		TrousersModeIV = newMode;
	}
	/**
	 * Get a status of the flag that indicates the usage of the encryption mode used for symmetric encryption. Based on observation, all TSS implementations use 
	 * 
	 * @return TRUE if TrouSerS use of TPM_ES_NONE is used; FALSE if consistent with the specification.
	 */
	public boolean getSymkeyEncscheme() {
		return TrousersModeSymkeyEncscheme;
	}
	/**
	 * Set the status of the TrouSerS encryption scheme flag.
	 * 
	 * @param newScheme Set to TRUE to emulate TrouSerS' use of TPM_ES_NONE; set to FALSE to comply with the specification.
	 */
	public void setSymkeyEncscheme(boolean newScheme) {
		TrousersModeSymkeyEncscheme = newScheme;
	}
	/**
	 * Get the status of the TrouSerS OEAP flag. TrouSerS (at least in the version available through Yum) uses a blank OEAP password when performing asymmetric encryption. The password should be "TCPA".
	 * 
	 * @return TRUE if TrouSerS use of a blank OEAP password is in use; FALSE if the correct password is used.
	 */
	public boolean getOeapMode () {
		return TrousersModeBlankOeap;
	}
	/**
	 * Set the status of the TrouSerS OEAP flag.
	 * 
	 * @param newMode Set to TRUE to emulate TrouSerS use of a blank OEAP password; set to FALSE to comply with the specification.
	 */
	public void getOeapMode(boolean newMode) {
		TrousersModeBlankOeap = newMode;
	}
	
	/**
	 * Create a new TpmIdentityRequest object by initializing with a byte blob from the output of 
	 * Tspi_TPM_CollateIdentityRequest.
	 *  
	 * @param blob The byte blob form of the identity request.
	 * @throws TpmUtils.TpmUnsignedConversionException
	 * @throws TpmUtils.TpmBytestreamResouceException
	 * @throws PrivacyCaException
	 */
	public TpmIdentityRequest(byte[] blob)
			throws TpmUtils.TpmUnsignedConversionException, 
			TpmUtils.TpmBytestreamResouceException,
			PrivacyCaException {
		ByteArrayInputStream bs = new ByteArrayInputStream(blob);
		int asymSize = TpmUtils.getUINT32(bs);
		int symSize = TpmUtils.getUINT32(bs);
		asymAlgorithm = new TpmKeyParams(bs);
		symAlgorithm = new TpmKeyParams(bs);
		TrousersModeIV = symAlgorithm.getTrouSerSmode();
		asymBlob = TpmUtils.getBytes(bs, asymSize);
		symBlob = TpmUtils.getBytes(bs, symSize);
		findIv();
	}
	/**
	 * Create a new TpmIdentityRequest by supplying a TpmIdentityProof and the Privacy CA's public key. A symmetric key and IV will be randomly created.
	 * 
	 * @param newIdProof A TpmIdentityProof object
	 * @param caKey The Privacy CA's private key
	 * @throws IOException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws InvalidKeyException
	 * @throws InvalidAlgorithmParameterException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws TpmUtils.TpmUnsignedConversionException
	 */
	public TpmIdentityRequest(TpmIdentityProof newIdProof, RSAPublicKey caKey)
			throws IOException,
			IllegalBlockSizeException,
			BadPaddingException,
			InvalidKeyException,
			InvalidAlgorithmParameterException,
			NoSuchAlgorithmException,
			NoSuchPaddingException,
			TpmUtils.TpmUnsignedConversionException {
		this(newIdProof, caKey, TpmUtils.createRandomBytes(16), TpmUtils.createRandomBytes(16));
	}
	/**
	 * Create a new TpmIdentityRequest by supplying a TpmIdentityProof, the Privacy CA's public key, a symmetric key, and an IV.
	 * 
	 * @param newIdProof A TpmIdentityProof object
	 * @param caKey The Privacy CA's private key
	 * @param key Symmetric key to use for encrypting the request (will itself be encrypted using the Privacy CA public key)
	 * @param iv Initialization Vector to be used for symmetric encryption
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws InvalidKeyException
	 * @throws InvalidAlgorithmParameterException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws TpmUtils.TpmUnsignedConversionException
	 */
	public TpmIdentityRequest(TpmIdentityProof newIdProof, RSAPublicKey caKey, byte [] key, byte [] iv)
			throws IllegalBlockSizeException,
			BadPaddingException,
			InvalidKeyException,
			InvalidAlgorithmParameterException,
			NoSuchAlgorithmException,
			NoSuchPaddingException,
			TpmUtils.TpmUnsignedConversionException {
		this(newIdProof, caKey, createDefaultAsymAlgorithm(), createDefaultSymAlgorithm(iv), key);
	}
	/**
	 * Create a new TpmIdentityRequest by supplying a TpmIdentityProof, the Privacy CA's public key, a symmetric key, and an IV.
	 * 
	 * @param newIdProof A TpmIdentityProof object
	 * @param caKey The Privacy CA's private key
	 * @param newAsymAlgorithm Asymmetric encryption information in the form of a TpmKeyParams object
	 * @param newSymAlgorithm Symmetric encryption information in the form of a TpmKeyParams object
	 * @throws IOException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws InvalidKeyException
	 * @throws InvalidAlgorithmParameterException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws TpmUtils.TpmUnsignedConversionException
	 */
	public TpmIdentityRequest(TpmIdentityProof newIdProof, RSAPublicKey caKey, TpmKeyParams newAsymAlgorithm, TpmKeyParams newSymAlgorithm)
			throws IOException,
			IllegalBlockSizeException,
			BadPaddingException,
			InvalidKeyException,
			InvalidAlgorithmParameterException,
			NoSuchAlgorithmException,
			NoSuchPaddingException,
			TpmUtils.TpmUnsignedConversionException {
		this(newIdProof, caKey, newAsymAlgorithm, newSymAlgorithm, TpmUtils.createRandomBytes(16));
	}
	public TpmIdentityRequest(TpmIdentityProof newIdProof, RSAPublicKey caKey, TpmKeyParams newAsymAlgorithm, TpmKeyParams newSymAlgorithm, byte [] key)
			throws IllegalBlockSizeException,
			BadPaddingException,
			InvalidKeyException,
			InvalidAlgorithmParameterException,
			NoSuchAlgorithmException,
			NoSuchPaddingException,
			TpmUtils.TpmUnsignedConversionException {
		TrousersModeIV = newIdProof.getIVmode();
		asymAlgorithm = newAsymAlgorithm;
		symAlgorithm = newSymAlgorithm;
		symAlgorithm.setTrouSerSmode(TrousersModeIV);
		TrousersModeSymkeyEncscheme = newIdProof.getSymkeyEncscheme();
		TrousersModeBlankOeap = newIdProof.getOeapMode();
		encryptAsym(encryptSym(newIdProof.toByteArray(), key, symAlgorithm.getSubParams().getByteData()), caKey);
		if (TrousersModeIV) {
			//symAlgorithm.setSubParams(null); //taken care of by TpmKeyParams
			byte [] newSymblob = new byte[symAlgorithm.getSubParams().getByteData().length + symBlob.length];
			System.arraycopy(symAlgorithm.getSubParams().getByteData(), 0, newSymblob, 0, symAlgorithm.getSubParams().getByteData().length);
			System.arraycopy(symBlob, 0, newSymblob, symAlgorithm.getSubParams().getByteData().length, symBlob.length);
			symBlob = newSymblob;
		}
	}
	/**
	 * Create a new Identity Request using an arbitrary byte blob as an Identity Proof and random AES 256 key and IV. This function is intended to be used to wrap data in the form of an Identity Request that may not be an Identity Proof. An example of this may be an Endorsement Credential.
	 * 
	 * @param newIdProof Arbitrary byte blob to take the position of an Identity Proof.
	 * @param caKey Privacy CA's public key
	 * @param TrouSerS <b>true</b> if request should be structured like one TrouSerS would create, <b>false</b> if request should be structured like one NTRU would create.
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws InvalidKeyException
	 * @throws InvalidAlgorithmParameterException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws TpmUtils.TpmUnsignedConversionException
	 * @throws IOException
	 */
	public TpmIdentityRequest(byte[] newIdProof, RSAPublicKey caKey, boolean TrouSerS)
			throws IllegalBlockSizeException,
			BadPaddingException,
			InvalidKeyException,
			InvalidAlgorithmParameterException,
			NoSuchAlgorithmException,
			NoSuchPaddingException,
			TpmUtils.TpmUnsignedConversionException, IOException {
		TrousersModeIV = true;
		asymAlgorithm = createDefaultAsymAlgorithm();
		symAlgorithm = createDefaultSymAlgorithm(TpmUtils.createRandomBytes(16));
		symAlgorithm.setTrouSerSmode(TrousersModeIV);
		if(TrouSerS){
			TrousersModeSymkeyEncscheme = true;
			TrousersModeBlankOeap = true;
		}else{
			TrousersModeSymkeyEncscheme = false;
			TrousersModeBlankOeap = false;
		}
		encryptAsym(encryptSym(newIdProof, TpmUtils.createRandomBytes(16), symAlgorithm.getSubParams().getByteData()), caKey);
		if (TrousersModeIV) {
			byte [] newSymblob = new byte[symAlgorithm.getSubParams().getByteData().length + symBlob.length];
			System.arraycopy(symAlgorithm.getSubParams().getByteData(), 0, newSymblob, 0, symAlgorithm.getSubParams().getByteData().length);
			System.arraycopy(symBlob, 0, newSymblob, symAlgorithm.getSubParams().getByteData().length, symBlob.length);
			symBlob = newSymblob;
		}
	}
	/**
	 * Create a default TpmKeyParams for the asym portion of the request.
	 * 
	 * @return
	 */
	private static TpmKeyParams createDefaultAsymAlgorithm() {
		TpmKeyParams toReturn = new TpmKeyParams();
		toReturn.setAlgorithmId(TpmKeyParams.TPM_ALG_RSA);//1
		toReturn.setEncScheme((short)TpmKeyParams.TPM_ES_RSAESOAEP_SHA1_MGF1);//3
		toReturn.setSigScheme((short)TpmKeyParams.TPM_SS_NONE);//1
		TpmRsaKeyParams newRsaKeyParams = new TpmRsaKeyParams();
		newRsaKeyParams.setKeyLength(2048);
		newRsaKeyParams.setValueData(2);
		newRsaKeyParams.setByteData(null);
		toReturn.setSubParams(newRsaKeyParams);
		return toReturn;
	}
	/**
	 * Create a default TpmKeyParams for the sym portion of the request. This will include the IV, as per the specification, but this IV will most likely be moved to a TrouSerS-like location when the request is encrypted.
	 * 
	 * @param iv 128-bit (16 byte) initialization vector
	 * @return
	 */
	private static TpmKeyParams createDefaultSymAlgorithm(byte [] iv) {
		TpmKeyParams toReturn = new TpmKeyParams();
		toReturn.setAlgorithmId(TpmKeyParams.TPM_ALG_AES);//6
		toReturn.setEncScheme((short)TpmKeyParams.TPM_ES_SYM_CBC_PKCS5PAD);//255=FF
		toReturn.setSigScheme((short)TpmKeyParams.TPM_SS_NONE);//1
		TpmSymmetricKeyParams newSymmetricKeyParams = new TpmSymmetricKeyParams();
		newSymmetricKeyParams.setKeyLength(128);
		newSymmetricKeyParams.setValueData(128);
		newSymmetricKeyParams.setByteData(iv);
		toReturn.setSubParams(newSymmetricKeyParams);
		return toReturn;
	}
	/**
	 * Encrypt the TpmIdentityProof using a specified key and IV; return the TpmSymmetricKey object. The encrypted portion will be stored in this object's symblob variable.
	 * 
	 * @param proof This is the TpmIdentityProof as a byte array (or anything else to encrypt as if it is an identity proof -- useful for sending encrypted data to a Privacy CA outside of the specification).
	 * @param key 128-bit (16 byte) AES key
	 * @param iv 128-bit (16 byte) initialization vector
	 * @return TpmSymmetricKey containing the used key
	 * @throws NoSuchPaddingException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidAlgorithmParameterException
	 * @throws InvalidKeyException
	 * @throws BadPaddingException
	 * @throws IllegalBlockSizeException
	 */
	private TpmSymmetricKey encryptSym(byte [] proof, byte [] key, byte [] iv)
			throws NoSuchPaddingException, 
			NoSuchAlgorithmException, 
			InvalidAlgorithmParameterException, 
			InvalidKeyException, 
			BadPaddingException, 
			IllegalBlockSizeException{
		//encrypt
		Cipher symCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		IvParameterSpec ivSpec = new IvParameterSpec(iv);
		SecretKeySpec symKey = new SecretKeySpec(key, "AES");
		symCipher.init(Cipher.ENCRYPT_MODE, symKey, ivSpec);
		symBlob = symCipher.doFinal(proof);
		//set the TpmSymmetricKey for return
		TpmSymmetricKey encryptKey = new TpmSymmetricKey();
		encryptKey.setAlgorithmId(TpmKeyParams.TPM_ALG_AES);
		encryptKey.setEncScheme(TpmKeyParams.TPM_ES_SYM_CBC_PKCS5PAD);
		encryptKey.setSecretKey(symKey);
		if (TrousersModeSymkeyEncscheme) {
			encryptKey.setEncScheme(TpmKeyParams.TPM_ES_NONE);
		}
		return encryptKey;
	}
	/**
	 * Encrypt a TpmSymmetricKey, as returned from the private encryptSym() function, and store in the asymblob variable for this object.
	 * 
	 * @param symKey The TpmSymmetricKey as returned from encryptSym()
	 * @param caKey The Privacy CA's public key
	 * @throws NoSuchPaddingException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidAlgorithmParameterException
	 * @throws InvalidKeyException
	 * @throws TpmUtils.TpmUnsignedConversionException
	 * @throws BadPaddingException
	 * @throws IllegalBlockSizeException
	 */
	private void encryptAsym(TpmSymmetricKey symKey, RSAPublicKey caKey)
			throws NoSuchPaddingException,
			NoSuchAlgorithmException,
			InvalidAlgorithmParameterException,
			InvalidKeyException,
			TpmUtils.TpmUnsignedConversionException,
			BadPaddingException,
			IllegalBlockSizeException {
		OAEPParameterSpec oaepSpec;
		if (TrousersModeBlankOeap)
			oaepSpec = new OAEPParameterSpec("Sha1", "MGF1", MGF1ParameterSpec.SHA1, new PSource.PSpecified("".getBytes()));
		else
			oaepSpec = new OAEPParameterSpec("Sha1", "MGF1", MGF1ParameterSpec.SHA1, new PSource.PSpecified("TCPA".getBytes()));
		Cipher asymCipher = Cipher.getInstance("RSA/ECB/OAEPWithSha1AndMGF1Padding");
		asymCipher.init(Cipher.PUBLIC_KEY, caKey, oaepSpec);
		asymCipher.update(symKey.toByteArray());
		asymBlob = asymCipher.doFinal();
	}
	/**
	 * Dump the Identity Request as a byte array in the form that it can be sent to a Privacy CA (or as it came from the client, assembled by a TSS)
	 * 
	 * @return Byte array containing the Identity Request
	 * @throws TpmUtils.TpmUnsignedConversionException
	 */
	public byte [] toByteArray()
			throws TpmUtils.TpmUnsignedConversionException {
		byte [] asymSize = TpmUtils.intToByteArray(asymBlob.length);
		byte [] symSize = TpmUtils.intToByteArray(symBlob.length);
		byte [] asymAlgorithmBytes = asymAlgorithm.toByteArray();
		byte [] symAlgorithmBytes = symAlgorithm.toByteArray();
		byte [] toReturn = new byte[asymSize.length + symSize.length + asymAlgorithmBytes.length + symAlgorithmBytes.length + asymBlob.length + symBlob.length];
		System.arraycopy(asymSize, 0, toReturn, 0, asymSize.length);
		System.arraycopy(symSize, 0, toReturn, asymSize.length, symSize.length);
		System.arraycopy(asymAlgorithmBytes, 0, toReturn, asymSize.length + symSize.length, asymAlgorithmBytes.length);
		System.arraycopy(symAlgorithmBytes, 0, toReturn, asymSize.length + symSize.length + asymAlgorithmBytes.length, symAlgorithmBytes.length);
		System.arraycopy(asymBlob, 0, toReturn, asymSize.length + symSize.length + asymAlgorithmBytes.length + symAlgorithmBytes.length, asymBlob.length);
		System.arraycopy(symBlob, 0, toReturn, asymSize.length + symSize.length + asymAlgorithmBytes.length + symAlgorithmBytes.length + asymBlob.length, symBlob.length);
		return toReturn;
	}
	/**
	 * 
	 * @return The asym key_parms.
	 */
	public TpmKeyParams getAsymKeyParams() {
		return asymAlgorithm;
	}
	/**
	 * 
	 * @return The sym algorithm key_parms.
	 */
	public TpmKeyParams getSymKeyParams() {
		return symAlgorithm;
	}
	/**
	 * 
	 * @return A textual report of the contents of the identity request.
	 */
	public String toString() {
		String returnVal = "";
		returnVal += "TpmIdentityRequest:\n";
		returnVal += " asymAlgorithm:";
		if (TrousersModeBlankOeap)
			returnVal += " (blank OAEP parameter)";
		returnVal += "\n" + asymAlgorithm.toString() + "\n";
		returnVal += " symAlgorithm:";
		if (TrousersModeSymkeyEncscheme)
			returnVal += " (bad symmetric enc-scheme)";
		returnVal += "\n" + symAlgorithm.toString() + "\n";
		returnVal += " asymBlob:\n" + TpmUtils.byteArrayToString(asymBlob, 16) + "\n";
		returnVal += " symBlob:\n" + TpmUtils.byteArrayToString(symBlob, 16);
		return returnVal;
	}
	/**
	 * Decrypt the asymmetric portion of the request to get the key needed to decrypt the symmetric portion.
	 * 
	 * @param privCaKey The Privacy CA's private key.
	 * @return
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws BadPaddingException
	 * @throws TpmUtils.TpmUnsignedConversionException
	 * @throws NoSuchAlgorithmException
	 * @throws IllegalBlockSizeException
	 * @throws InvalidAlgorithmParameterException
	 * @throws TpmUtils.TpmBytestreamResouceException
	 */
	private TpmSymmetricKey decryptAsym(RSAPrivateKey privCaKey)
			throws NoSuchPaddingException, 
			InvalidKeyException, 
			BadPaddingException, 
			TpmUtils.TpmUnsignedConversionException,
			NoSuchAlgorithmException, 
			IllegalBlockSizeException, 
			InvalidAlgorithmParameterException, 
			TpmUtils.TpmBytestreamResouceException,
			PrivacyCaException {
		TpmSymmetricKey symKey = new TpmSymmetricKey();
		switch (asymAlgorithm.getEncScheme()) {
		case 0x3: // <-- most likely with NTRU, TrouSerS
			Cipher asymCipher = Cipher.getInstance("RSA/ECB/OAEPWithSha1AndMGF1Padding");
			OAEPParameterSpec oaepSpec = new OAEPParameterSpec("Sha1", "MGF1", MGF1ParameterSpec.SHA1, new PSource.PSpecified("TCPA".getBytes()));
			asymCipher.init(Cipher.PRIVATE_KEY, privCaKey, oaepSpec);
			asymCipher.update(asymBlob);
			byte[] temparray = null;
			try {
				temparray = asymCipher.doFinal();
			} catch (BadPaddingException e) { //<- TrouSerS does not use an OAEP parameter string of "TCPA", per 1.1b spec. This results in a BadPaddingException -- try again without!
				oaepSpec = new OAEPParameterSpec("Sha1", "MGF1", MGF1ParameterSpec.SHA1, new PSource.PSpecified("".getBytes()));
				asymCipher.init(Cipher.PRIVATE_KEY, privCaKey, oaepSpec);
				asymCipher.update(asymBlob);
				temparray = asymCipher.doFinal();
				TrousersModeBlankOeap = true;
			}
			if (temparray == null)
				throw new PrivacyCaException("Unable to decrypt asym blob from incoming request.");
			symKey = new TpmSymmetricKey(temparray);
			break;
		default:
			asymCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			asymCipher.init(Cipher.DECRYPT_MODE, privCaKey);
			symKey = new TpmSymmetricKey(asymCipher.doFinal(asymBlob));
			break;
		}
		if ((symKey.getAlgorithmId() == TpmKeyParams.TPM_ALG_AES) && (symKey.getEncScheme() == TpmKeyParams.TPM_ES_NONE)) {
			TrousersModeSymkeyEncscheme = true;
			symKey.setEncScheme(TpmKeyParams.TPM_ES_SYM_CBC_PKCS5PAD);
		}
		return symKey;
	}
	/**
	 * Decrypt the symmetric portion of the request to get the identity proof.
	 * 
	 * @param symKey The output of decryptAsym.
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidAlgorithmParameterException
	 * @throws TpmUtils.TpmUnsignedConversionException
	 * @throws InvalidKeyException
	 * @throws BadPaddingException
	 * @throws IllegalBlockSizeException
	 * @throws PrivacyCaException
	 * @throws TpmUtils.TpmBytestreamResouceException
	 */
	private TpmIdentityProof decryptSym(TpmSymmetricKey symKey)
			throws NoSuchAlgorithmException, 
			NoSuchPaddingException, 
			InvalidAlgorithmParameterException, 
			TpmUtils.TpmUnsignedConversionException,
			InvalidKeyException, 
			BadPaddingException, 
			IllegalBlockSizeException, 
			PrivacyCaException, 
			TpmUtils.TpmBytestreamResouceException {
		String instance = symKey.getAlgorithmStr() + "/" + symKey.getEncSchemeStr();
		//System.out.println("Instance: " + instance);
		Cipher symCipher = Cipher.getInstance(instance);
		IvParameterSpec ivSpec = new IvParameterSpec(symAlgorithm.getSubParams().getByteData());
		symCipher.init(Cipher.DECRYPT_MODE, symKey.getSecretKey(), ivSpec);
		TpmIdentityProof identProof = new TpmIdentityProof(symCipher.doFinal(symBlob), TrousersModeIV, TrousersModeSymkeyEncscheme, TrousersModeBlankOeap);
		return identProof;
	}
	/**
	 * If the byte blob captured as an Identity Request was not an encrypted Identity Proof (which could be done to transfer data from client to Privacy CA outside of the specification), then this method of decrypting the symblob may be preferable.
	 * 
	 * @param symKey The TpmSymmetricKey as returned from decryptAsym()
	 * @return Decrypted byte blob
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidAlgorithmParameterException
	 * @throws TpmUtils.TpmUnsignedConversionException
	 * @throws InvalidKeyException
	 * @throws BadPaddingException
	 * @throws IllegalBlockSizeException
	 * @throws PrivacyCaException
	 * @throws TpmUtils.TpmBytestreamResouceException
	 */
	private byte[] decryptSymRaw(TpmSymmetricKey symKey)
			throws NoSuchAlgorithmException, 
			NoSuchPaddingException, 
			InvalidAlgorithmParameterException, 
			TpmUtils.TpmUnsignedConversionException,
			InvalidKeyException, 
			BadPaddingException, 
			IllegalBlockSizeException, 
			PrivacyCaException, 
			TpmUtils.TpmBytestreamResouceException {
		String instance = symKey.getAlgorithmStr() + "/" + symKey.getEncSchemeStr();
		//System.out.println("Instance: " + instance);
		Cipher symCipher = Cipher.getInstance(instance);
		IvParameterSpec ivSpec = new IvParameterSpec(symAlgorithm.getSubParams().getByteData());
		symCipher.init(Cipher.DECRYPT_MODE, symKey.getSecretKey(), ivSpec);
		return symCipher.doFinal(symBlob);
	}
	/**
	 * Decrypt the identity request to get the identity proof.
	 * 
	 * @param privCaKey The Privacy CA's private key.
	 * @return An identity proof.
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws InvalidAlgorithmParameterException
	 * @throws TpmUtils.TpmUnsignedConversionException
	 * @throws NoSuchAlgorithmException
	 * @throws BadPaddingException
	 * @throws NoSuchPaddingException
	 * @throws PrivacyCaException
	 * @throws TpmUtils.TpmBytestreamResouceException
	 */
	public TpmIdentityProof decrypt(RSAPrivateKey privCaKey)
			throws InvalidKeyException, 
			IllegalBlockSizeException, 
			InvalidAlgorithmParameterException, 
			TpmUtils.TpmUnsignedConversionException, 
			NoSuchAlgorithmException, 
			BadPaddingException, 
			NoSuchPaddingException, 
			PrivacyCaException, 
			TpmUtils.TpmBytestreamResouceException {
		TpmSymmetricKey tempKey = decryptAsym(privCaKey);
		return decryptSym(tempKey);
	}
	/**
	 * Decrypts the Identity Request, and DOES NOT assume the contents are an Identity Proof. 
	 * 
	 * @param privCaKey Privacy CA's private key
	 * @return Raw byte blob
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws InvalidAlgorithmParameterException
	 * @throws TpmUtils.TpmUnsignedConversionException
	 * @throws NoSuchAlgorithmException
	 * @throws BadPaddingException
	 * @throws NoSuchPaddingException
	 * @throws PrivacyCaException
	 * @throws TpmUtils.TpmBytestreamResouceException
	 */
	public byte[] decryptRaw(RSAPrivateKey privCaKey)
			throws InvalidKeyException, 
			IllegalBlockSizeException, 
			InvalidAlgorithmParameterException, 
			TpmUtils.TpmUnsignedConversionException, 
			NoSuchAlgorithmException, 
			BadPaddingException, 
			NoSuchPaddingException, 
			PrivacyCaException, 
			TpmUtils.TpmBytestreamResouceException {
		TpmSymmetricKey tempKey = decryptAsym(privCaKey);
		return decryptSymRaw(tempKey);
	}
	/**
	 * This must be run to properly identity the location of the symmetric encryption Initialization Vector. If TrouSerS-style
	 * formatting is used on the request, the IV is not in the symmetric key parameters, but rather at the head of the 
	 * symmetrically encrypted blob. In order to decrypt consistently, regardless of the format use, this procedure will adjust 
	 * from TrouSerS-style to 1.2 spec compliant style. A flag is set when parsing (in the constructor) so that the particular
	 * style is recorded. This will be used when constructing a response. 
	 * 
	 * @throws PrivacyCaException
	 */
	private void findIv() 
			throws PrivacyCaException { //must be called at this level to have access to symBlob
		//Because TrouSerS-style might be in effect, we may have to find the IV and adjust the symBlob
		//Also, based on this populate other values (keyLength, blockSize)
		if (symAlgorithm.getSubParams().getValueData() == 0) {
			TrousersModeIV = true;
			//This indicates TrouSerS mode is active.
			//Set the key length and block size to the value for the symmetric algorithm
			switch (symAlgorithm.getAlgorithmId()) {
			case 0x2: //TPM_ALG_DES
				symAlgorithm.getSubParams().setKeyLength(56);
				symAlgorithm.getSubParams().setValueData(64); //set block size, assume in bits (not specified in TCG documentation)
				break;
			case 0x3: //TPM_ALG_3DES
				symAlgorithm.getSubParams().setKeyLength(192);
				symAlgorithm.getSubParams().setValueData(64); //set block size, assume in bits (not specified in TCG documentation)
				break;
			case 0x6: //TPM_ALG_AES/AES128* most likely to occur
				symAlgorithm.getSubParams().setKeyLength(128);
				symAlgorithm.getSubParams().setValueData(128); //set block size, assume in bits (not specified in TCG documentation)
				break;
			case 0x8: //TPM_ALG_AES192
				symAlgorithm.getSubParams().setKeyLength(192);
				symAlgorithm.getSubParams().setValueData(128); //set block size, assume in bits (not specified in TCG documentation)
				break;
			case 0x9: //TPM_ALG_AES256
				symAlgorithm.getSubParams().setKeyLength(256);
				symAlgorithm.getSubParams().setValueData(128); //set block size, assume in bits (not specified in TCG documentation)
				break;
			default:
				throw new PrivacyCaException("Unexpected symmetric algorithm ID: " + Integer.toHexString(symAlgorithm.getAlgorithmId()));
			}
			//snag the first (blocksize) bits from the symBlob
			byte [] newIv = new byte[symAlgorithm.getSubParams().getValueData() / 8];
			System.arraycopy(symBlob, 0, newIv, 0, newIv.length);
			symAlgorithm.getSubParams().setByteData(newIv);
			byte [] newSymBlob = new byte[symBlob.length - newIv.length];
			System.arraycopy(symBlob, newIv.length, newSymBlob, 0, newSymBlob.length);
			symBlob = newSymBlob;
		}
	}
}
