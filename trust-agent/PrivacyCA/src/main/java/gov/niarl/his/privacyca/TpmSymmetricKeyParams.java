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

/**
 * <p>This class is for the TCG's TPM_SYMMETRIC_KEY_PARMS structure.</p>
 * @author schawki
 *
 */
public class TpmSymmetricKeyParams implements TpmKeySubParams{
	private int keyLength = 0;
	private int blockSize = 0;
	private byte[] iv;
	
	public TpmSymmetricKeyParams() {}
	/**
	 * Create a new TpmSymmetricKeyParams by extracting from a byte stream.
	 * 
	 * @param source The ByteArrayInputStream from which to extract the TpmSymmetricKey.
	 * @param length The number of bytes to extract
	 * @throws TpmUtils.TpmUnsignedConversionException
	 * @throws TpmUtils.TpmBytestreamResouceException
	 */
	public TpmSymmetricKeyParams(ByteArrayInputStream source, int length)
			throws TpmUtils.TpmUnsignedConversionException, 
			TpmUtils.TpmBytestreamResouceException {
		if (length > 0) {
			keyLength = TpmUtils.getUINT32(source);
			blockSize = TpmUtils.getUINT32(source);
			int temp = TpmUtils.getUINT32(source);
			iv = TpmUtils.getBytes(source, temp);
		}
	}
	/**
	 * @return Key length in bytes.
	 */
	public int getKeyLength() {
		return keyLength;
	}
	/**
	 * Set a new key length.
	 * 
	 * @param newValue New key length in bytes.
	 */
	public void setKeyLength(int newValue) {
		keyLength = newValue;
	}
	/**
	 * @return Encryption block size in bytes.
	 */
	public int getValueData() {
		return blockSize;
	}
	/**
	 * Set encryption block size.
	 * 
	 * @param newValue New block size in bytes.
	 */
	public void setValueData(int newValue) {
		blockSize = newValue;
	}
	/**
	 * @return Initialization vector.
	 */
	public byte [] getByteData() {
		return iv;
	}
	/**
	 * Set initialization vector.
	 * 
	 * @param newValue New initialization vector.
	 */
	public void setByteData(byte [] newValue) {
		iv = newValue;
	}
	/**
	 * @return Human readable report of TpmSymmetricKeyParams.
	 */
	public String toString() {
		String returnVal = "";
		returnVal += "TpmSymmetricKeyParams:\n";
		returnVal += " keyLength: " + Integer.toString(keyLength) + "\n";
		returnVal += " blockSize: " + Integer.toString(blockSize) + "\n";
		returnVal += " iv: " + TpmUtils.byteArrayToString(iv, 16);
		return returnVal;
	}
	/**
	 * @return Serialized byte array form of TpmSymmetricKeyParams.
	 * @throws TpmUtils.TpmUnsignedConversionException
	 */
	public byte [] toByteArray()
			throws TpmUtils.TpmUnsignedConversionException {
		byte [] keyLngth = TpmUtils.intToByteArray(keyLength);
		byte [] blkSize = TpmUtils.intToByteArray(blockSize);
		int ivLength = 0;
		if (iv != null)
			ivLength = iv.length;
		byte [] size = TpmUtils.intToByteArray(ivLength);
		int x = keyLngth.length + blkSize.length + size.length + ivLength;
		byte [] returnArray = new byte[x];
		
		System.arraycopy(keyLngth, 0, returnArray, 0, keyLngth.length);
		System.arraycopy(blkSize, 0, returnArray, keyLngth.length, blkSize.length);
		System.arraycopy(size, 0, returnArray, keyLngth.length + blkSize.length, size.length);
		if (iv != null)
			System.arraycopy(iv, 0, returnArray, keyLngth.length + blkSize.length + size.length, iv.length);
		return returnArray;
	}
}
