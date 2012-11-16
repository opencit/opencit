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

import java.security.*;
import java.security.spec.*;
import java.security.interfaces.*;
import javax.crypto.*;
import javax.crypto.spec.*;

/**
 * <p>This class is the Java version of the C-style structure TPM_ASYM_CA_CONTENTS, 
 * as specified by the TCG. It contains all of the member variables specified by 
 * the TCG, and any applicable functions. This structure is normally created by 
 * the Privacy CA, is encrypted using a TPM's public EK, and holds the symmetric 
 * key needed to decrypt a paired TpmSymCaAttestation.</p>
 * 
 * @author schawki
 *
 */
public class TpmAsymCaContents {
	private TpmSymmetricKey symKey = null;
	private byte [] tpmDigest = null;
	private byte [] encrypted = null;
	public TpmAsymCaContents(){}
	/**
	 * Set the TpmSymmetricKey member data.
	 * 
	 * @param newKey
	 */
	public void setSymmetricKey(TpmSymmetricKey newKey) {
		symKey = newKey;
	}
	/**
	 * Set the TPM digest. This is required before the TpmAsymCaContents structure can be encrypted.
	 * 
	 * @param aik The AIK in the form of a TpmPubKeu.
	 * @throws NoSuchAlgorithmException Thrown if the MessageDigest class doesn't know what "SHA-1" means.
	 * @throws TpmUtils.TpmUnsignedConversionException Thrown if there is a problem converting the AIK to a byte array.
	 */
	public void setDigest(TpmPubKey aik) 
			throws NoSuchAlgorithmException, 
			TpmUtils.TpmUnsignedConversionException {
		MessageDigest md;
		md = MessageDigest.getInstance("SHA-1");
		md.update(aik.toByteArray());
		tpmDigest = md.digest();
	}
	/**
	 * Encrypt the TpmAsymCaContents for return to the TPM. The symmetric key and digest must be set for this function to run without Exception.
	 * 
	 * @param ekPubKey The EK public key, extracted form the EK certificate included in the identity request/proof.
	 * @throws NoSuchPaddingException Encryption error.
	 * @throws NoSuchAlgorithmException Encryption error.
	 * @throws InvalidKeyException Encryption error.
	 * @throws InvalidAlgorithmParameterException Encryption error.
	 * @throws IllegalBlockSizeException Encryption error.
	 * @throws BadPaddingException Encryption error.
	 * @throws TpmUtils.TpmUnsignedConversionException Error in converting structures to byte arrays (bad data, most likely).
	 * @throws PrivacyCaException All required prerequisites were not met.
	 */
	public void encrypt(RSAPublicKey ekPubKey, boolean TrousersModeBlankOeap) // use the TPM's EK
			throws NoSuchPaddingException, 
			NoSuchAlgorithmException, 
			InvalidKeyException, 
			InvalidAlgorithmParameterException, 
			IllegalBlockSizeException, 
			BadPaddingException, 
			TpmUtils.TpmUnsignedConversionException,
			PrivacyCaException { 
		OAEPParameterSpec oaepSpec;
		if (!TrousersModeBlankOeap)
			oaepSpec = new OAEPParameterSpec("Sha1", "MGF1", MGF1ParameterSpec.SHA1, new PSource.PSpecified("TCPA".getBytes()));
		else
			oaepSpec = new OAEPParameterSpec("Sha1", "MGF1", MGF1ParameterSpec.SHA1, new PSource.PSpecified("".getBytes()));
		Cipher asymCipher = Cipher.getInstance("RSA/ECB/OAEPWithSha1AndMGF1Padding");
		asymCipher.init(Cipher.PUBLIC_KEY, ekPubKey, oaepSpec);
		byte[] newbytes = this.toPlaintextByteArray();
		asymCipher.update(newbytes);
		encrypted = asymCipher.doFinal();
	}
	/**
	 * Get the encrypted TpmAsymCaContents as a byte array suitable for delivery to the TPM with TPM_ActivateIdentity.
	 * 
	 * @return A byte array form of the TpmAsymCaContents.
	 * @throws PrivacyCaException Throws if the structure is not ready to be sent back to the TPM.
	 */
	public byte [] toByteArray()
			throws PrivacyCaException {
		if (encrypted == null) {
			throw new PrivacyCaException("Cannot access encrypted TpmAsymCaContents until encryption process has been run.");
		}
		return encrypted;
	}
	/**
	 * Return a byte array of the plaintext structure suitable for encryption. All prerequisites must be met, 
	 * which are just populating all of the private member variables.
	 * 
	 * @return the plaintext byte array.
	 * @throws TpmUtils.TpmUnsignedConversionException Thrown if there is a problem in assembling the array.
	 * @throws PrivacyCaException If the prereqs are not met.
	 */
	private byte [] toPlaintextByteArray()
			throws TpmUtils.TpmUnsignedConversionException,
			PrivacyCaException {
		if (symKey == null) { 
			throw new PrivacyCaException("Cannot convert TpmAsymCaContents to byte array until TpmSymmetricKey is set.");
		}
		if (tpmDigest == null) {
			throw new PrivacyCaException("Cannot convert TpmAsymCaContents to byte array until TPM digest has been created.");
		}
		byte [] symKeyBytes = symKey.toByteArray();
		byte [] returnArray = new byte[symKeyBytes.length + tpmDigest.length];
		System.arraycopy(symKeyBytes, 0, returnArray, 0, symKeyBytes.length);
		System.arraycopy(tpmDigest, 0, returnArray, symKeyBytes.length, tpmDigest.length);
		return returnArray;
	}
}
