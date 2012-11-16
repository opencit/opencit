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

import javax.crypto.spec.*;
import java.io.*;

/**
 * <p>This class is for the TCG's TPM_SYMMETRIC_KEY structure.</p>
 * @author schawki
 *
 */
public class TpmSymmetricKey {
	private byte[] keyBlob;
	private int algorithmId;
	private short encScheme;
		
	public TpmSymmetricKey() {
		keyBlob = TpmUtils.hexStringToByteArray("");
	}
	/**
	 * Instantiate a new TpmSymmetricKey from a byte array.
	 * 
	 * @param blob The key bytes to use.
	 * @throws TpmUtils.TpmUnsignedConversionException
	 * @throws TpmUtils.TpmBytestreamResouceException
	 */
	public TpmSymmetricKey(byte [] blob)
			throws TpmUtils.TpmUnsignedConversionException, 
			TpmUtils.TpmBytestreamResouceException {
		ByteArrayInputStream bs = new ByteArrayInputStream(blob);
		algorithmId = TpmUtils.getUINT32(bs);
		encScheme = TpmUtils.getUINT16(bs);
		short temp = TpmUtils.getUINT16(bs);
		keyBlob = TpmUtils.getBytes(bs, (int)temp);
	}
	/**
	 * 
	 * @return The secret key in the form of a SecretKeySpec.
	 */
	public SecretKeySpec getSecretKey() {
		String algorithm = this.getAlgorithmStr();
		SecretKeySpec symKey = new SecretKeySpec(keyBlob, algorithm);
		return symKey;
	}
	/**
	 * Set the secret key with a SecretKeySpec.
	 * 
	 * @param newKeySpec New secret key.
	 */
	public void setSecretKey(SecretKeySpec newKeySpec) {
		keyBlob = newKeySpec.getEncoded();
	}
	/**
	 * 
	 * @return The algorithm ID defined for this symmetric key.
	 */
	public int getAlgorithmId() {
		return algorithmId;
	}
	/**
	 * Set the algorithm ID for this symmetric key.
	 * 
	 * @param newAlgId New algorithm ID.
	 */
	public void setAlgorithmId(int newAlgId) {
		algorithmId = newAlgId;
	}
	/**
	 * 
	 * @return Human-readable report of the TpmSymmetricKey.
	 */
	public String getAlgorithmStr(){
		String returnVal = "";
		switch (algorithmId){
		case TpmKeyParams.TPM_ALG_DES:
			returnVal = "DES";
			break;
		case TpmKeyParams.TPM_ALG_3DES:
			returnVal = "DESede";
			break;
		case TpmKeyParams.TPM_ALG_AES:
		case TpmKeyParams.TPM_ALG_AES192:
		case TpmKeyParams.TPM_ALG_AES256:
			returnVal = "AES";
			break;
		default:
			returnVal = "Error";
			break;
		}
		return returnVal;
	}
	/**
	 * 
	 * @return The set encryption scheme.
	 */
	public short getEncScheme() {
		return encScheme;
	}
	/**
	 * Set the encryption scheme.
	 * 
	 * @param newEncScheme New encryption scheme.
	 */
	public void setEncScheme(short newEncScheme) {
		encScheme = newEncScheme;
	}
	/**
	 * 
	 * @return The encryption scheme as a string.
	 */
	public String getEncSchemeStr(){
		String returnVal = "";
		switch (encScheme){
		case TpmKeyParams.TPM_ES_NONE:
			returnVal = "NONE/NoPadding";
			break;
		case TpmKeyParams.TPM_ES_SYM_CBC_PKCS5PAD:
			returnVal = "CBC/PKCS5Padding";
			break;
		default:
			returnVal = "Error";
			break;
		}
		return returnVal;
	}
	/**
	 * 
	 * @return Symmetric key blob, as a byte array.
	 */
	public byte [] getKeyBlob() {
		return keyBlob;
	}
	/**
	 * Set a new key symmetric key by byte blob.
	 * 
	 * @param newKeyBlob New key.
	 */
	public void setKeyBlob(byte [] newKeyBlob) {
		keyBlob = newKeyBlob;
	}
	/**
	 * Serialize the TpmSymmetricKey structure.
	 * 
	 * @return Byte array form of TpmSymmetricKey.
	 * @throws TpmUtils.TpmUnsignedConversionException
	 */
	public byte [] toByteArray() throws TpmUtils.TpmUnsignedConversionException {
		byte[] algoId = TpmUtils.intToByteArray(algorithmId);
		byte[] encSchm = TpmUtils.shortToByteArray(encScheme);
		byte[] size = TpmUtils.shortToByteArray((short)keyBlob.length);
		int x = algoId.length + encSchm.length + size.length + keyBlob.length; //calculate # of bytes in structure
		byte [] returnArray = new byte[x];
		System.arraycopy(algoId, 0, returnArray, 0, algoId.length);
		System.arraycopy(encSchm, 0, returnArray, algoId.length, encSchm.length);
		System.arraycopy(size, 0, returnArray, algoId.length + encSchm.length, size.length);
		System.arraycopy(keyBlob, 0, returnArray, algoId.length + encSchm.length + size.length, keyBlob.length);
		return returnArray;
	}
}
