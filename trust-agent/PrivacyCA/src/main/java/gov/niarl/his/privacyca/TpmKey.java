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

import gov.niarl.his.privacyca.TpmUtils.TpmBytestreamResouceException;
import gov.niarl.his.privacyca.TpmUtils.TpmUnsignedConversionException;

import java.io.ByteArrayInputStream;

/**
 * <p>This class is for the TCG's TPM_KEY structure.</p>
 * @author schawki
 *
 */
public class TpmKey {
	private byte [] structVer = {(byte)0x01, (byte)0x01, (byte)0x00, (byte)0x00};
	private short tpmKeyUsage = 0;//UINT16
	private int tpmKeyFlags = 0;//UINT32
	private byte tpmAuthDataUsage = (byte)0x00;//BYTE (just 1)
	private TpmKeyParams keyParms= null;
	//pcrInfoSize UINT32
	private byte [] pcrInfo = null;
	//UINT32 size, see below
	private byte [] tpmStorePubkey = null; //actually TPM_STORE_PUBKEY, which is a UINT32 size plus key data, usually the modulus
	//encryptedDataSize UINT32
	private byte [] encryptedData = null;
	
	/**
	 * Create a new TpmKey with no default values.
	 */
	public TpmKey(){
		//
	}
	/**
	 * Create a new TpmKey by extracting values from a byte blob.
	 * @param blob Raw blob representing a TPM_KEY.
	 * @throws TpmBytestreamResouceException
	 * @throws TpmUnsignedConversionException
	 */
	public TpmKey(byte [] blob) 
			throws TpmBytestreamResouceException, 
			TpmUnsignedConversionException{
		ByteArrayInputStream bs = new ByteArrayInputStream(blob);
		structVer = TpmUtils.getBytes(bs, 4); //4 bytes
		tpmKeyUsage = TpmUtils.getUINT16(bs); //uint16
		tpmKeyFlags = TpmUtils.getUINT32(bs); //uint32
		tpmAuthDataUsage = TpmUtils.getBytes(bs, 1)[0]; //byte
		keyParms = new TpmKeyParams(bs); //TpmKeyParams
		int tempSize = TpmUtils.getUINT32(bs); //uint32
		pcrInfo = TpmUtils.getBytes(bs, tempSize); //<tempSize> bytes
		tempSize = TpmUtils.getUINT32(bs); //uint32
		tpmStorePubkey = TpmUtils.getBytes(bs, tempSize); //<tempSize> bytes
		tempSize = TpmUtils.getUINT32(bs); //uint32
		encryptedData = TpmUtils.getBytes(bs, tempSize); //<tempSize> bytes
	}
	/**
	 * Get the stored key modulus.
	 * @return
	 */
	public byte [] getModulus(){
		return tpmStorePubkey;
	}
	/**
	 * Set the TPM_STRUCT_VER, should always be 0x01010000.
	 * @param newStructVer
	 */
	public void setStructVer(byte [] newStructVer){
		structVer = newStructVer;
	}
	/**
	 * Get the TPM_STRUCT_VER.
	 * @return
	 */
	public byte [] getStructVer(){
		return structVer;
	}
	/**
	 * Set the key usage. See the TPM Main Specification Part 2: Structures, section 5.8 for detailed information about TPM_KEY_USAGE.
	 * @param newTpmKeyUsage
	 */
	public void setTpmKeyUsage(short newTpmKeyUsage){
		tpmKeyUsage = newTpmKeyUsage;
	}
	/**
	 * Get the key usage value.
	 * @return
	 */
	public short getTpmKeyUsage(){
		return tpmKeyUsage;
	}
	/**
	 * Set the key flags. See the TPM Main Specification Part 2: Structures, section 5.9 for detailed information about TPM_KEY_FLAGS.
	 * @param newTpmKeyFlags
	 */
	public void setTpmKeyFlags(int newTpmKeyFlags){
		tpmKeyFlags = newTpmKeyFlags;
	}
	/**
	 * Get the key flags.
	 * @return
	 */
	public int getTpmKeyFlags(){
		return tpmKeyFlags;
	}
	/**
	 * Set the key auth data usage. See the TPM Main Specification Part 2: Structures, section 5.9 for detailed information about TPM_AUTH_DATA_USAGE.
	 * @param newTpmAuthDataUsage
	 */
	public void setTpmAuthDataUsage(byte newTpmAuthDataUsage){
		tpmAuthDataUsage = newTpmAuthDataUsage;
	}
	/**
	 * Get the key auth data usage.
	 * @return
	 */
	public byte getTpmAuthDataUsage(){
		return tpmAuthDataUsage;
	}
	/**
	 * Set the TPM_KEY_PARMS using a TpmKeyParams object.
	 * @param newKeyParms
	 */
	public void setKeyParms(TpmKeyParams newKeyParms){
		keyParms = newKeyParms;
	}
	/**
	 * Get the TPM_KEY_PARMS for the key.
	 * @return
	 */
	public TpmKeyParams getKeyParms(){
		return keyParms;
	}
	/**
	 * Set the PCR info for the key. As per the spec, if the key is not bound by PCR info, this should be null (size 0). If the key is bound by PCR info, the newPcrInfo parameter should be set as the serialized form of TPM_PCR_INFO, as defined in section 8.3 of the TPM Main Part 2: Structures document.
	 * @param newPcrInfo
	 */
	public void setPcrInfo(byte [] newPcrInfo){
		pcrInfo = newPcrInfo;
	}
	/**
	 * Get the PCR info. If null, the key is not bound to PCR info.
	 * @return
	 */
	public byte [] getPcrInfo(){
		return pcrInfo;
	}
	/**
	 * Set the raw public key information. This is the modulus only, as the public exponent is stored in the TPM_KEY_PARMS section.
	 * @param newTpmStorePubkey
	 */
	public void setTpmStorePubkey(byte [] newTpmStorePubkey){
		tpmStorePubkey = newTpmStorePubkey;
	}
	/**
	 * Get the stored TPM_STORE_PUBKEY.
	 * @return
	 */
	public byte [] getTpmStorePubkey(){
		return tpmStorePubkey;
	}
	/**
	 * Set the encrypted data portion of the key. This should generally be provided by the TPM itself.
	 * @param newEncryptedData
	 */
	public void setEncryptedData(byte [] newEncryptedData){
		encryptedData = newEncryptedData;
	}
	/**
	 * Get the encrytped portion of the key.
	 * @return
	 */
	public byte [] getEncryptedData(){
		return encryptedData;
	}
	/**
	 * Serialize the TpmKey object in the form of a TPM_KEY structure.
	 * @return
	 * @throws TpmUnsignedConversionException
	 */
	public byte [] toByteArray() 
			throws TpmUnsignedConversionException{
		byte [] keyParmsBytes = keyParms.toByteArray();
		byte [] toReturn = new byte[4 + 2 + 4 + 1 + keyParmsBytes.length + 4 + pcrInfo.length + 4 + tpmStorePubkey.length + 4 + encryptedData.length];
		byte [] tempBytes = null;
		int copyPos = 0;
		//structver: 4
		System.arraycopy(structVer, 0, toReturn, copyPos, structVer.length);
		copyPos += structVer.length;
		//tpm key usage: uint16/short
		tempBytes = TpmUtils.shortToByteArray(tpmKeyUsage);
		System.arraycopy(tempBytes, 0, toReturn, copyPos, tempBytes.length);
		copyPos += tempBytes.length;
		//tpm key flags: uint32/int
		tempBytes = TpmUtils.intToByteArray(tpmKeyFlags);
		System.arraycopy(tempBytes, 0, toReturn, copyPos, tempBytes.length);
		copyPos += tempBytes.length;
		//tpm auth data usage: byte[1]
		tempBytes = new byte[1];
		tempBytes[0] = tpmAuthDataUsage;
		System.arraycopy(tempBytes, 0, toReturn, copyPos, tempBytes.length);
		copyPos += tempBytes.length;
		//tpm key parms: above
		System.arraycopy(keyParmsBytes, 0, toReturn, copyPos, keyParmsBytes.length);
		copyPos += keyParmsBytes.length;
		//size of pcr info
		tempBytes = TpmUtils.intToByteArray(pcrInfo.length);
		System.arraycopy(tempBytes, 0, toReturn, copyPos, tempBytes.length);
		copyPos += tempBytes.length;
		//pcr info
		System.arraycopy(pcrInfo, 0, toReturn, copyPos, pcrInfo.length);
		copyPos += pcrInfo.length;
		//size of modulus
		tempBytes = TpmUtils.intToByteArray(tpmStorePubkey.length);
		System.arraycopy(tempBytes, 0, toReturn, copyPos, tempBytes.length);
		copyPos += tempBytes.length;
		//modulus
		System.arraycopy(tpmStorePubkey, 0, toReturn, copyPos, tpmStorePubkey.length);
		copyPos += tpmStorePubkey.length;
		//enc data size
		tempBytes = TpmUtils.intToByteArray(encryptedData.length);
		System.arraycopy(tempBytes, 0, toReturn, copyPos, tempBytes.length);
		copyPos += tempBytes.length;
		//enc data
		System.arraycopy(encryptedData, 0, toReturn, copyPos, encryptedData.length);
		return toReturn;
	}
}
