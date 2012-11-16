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
 * <p>This class is for the TCG's TPM_RSA_KEY_PARMS structure. It extends the TpmKeySubParams interface.</p>
 * @author schawki
 *
 */
public class TpmRsaKeyParams implements TpmKeySubParams{
	public int keyLength;
	public int numPrimes; //number of primes
	public byte[] exponent; //exponent
	
	public TpmRsaKeyParams() {}
	/**
	 * Create a new RSAKeyParams by extracting it from a byte stream.
	 * 
	 * @param source The byte stream from which to extract.
	 * @param length The length of the RSA key params (just used to see if over 0).
	 * @throws TpmUtils.TpmUnsignedConversionException
	 * @throws TpmUtils.TpmBytestreamResouceException
	 */
	public TpmRsaKeyParams(ByteArrayInputStream source, int length) throws TpmUtils.TpmUnsignedConversionException, TpmUtils.TpmBytestreamResouceException {
		if (length > 0) {
			keyLength = TpmUtils.getUINT32(source);
			numPrimes = TpmUtils.getUINT32(source);
			int temp = TpmUtils.getUINT32(source);
			exponent = TpmUtils.getBytes(source, temp);
		}
	}
	/**
	 * Serialize the structure.
	 * 
	 * @return The serialized RSA key params structure.
	 * @throws TpmUtils.TpmUnsignedConversionException 
	 */
	public byte[] toByteArray()
			throws TpmUtils.TpmUnsignedConversionException {
		byte [] keyLngth = TpmUtils.intToByteArray(keyLength);
		byte [] numPrms = TpmUtils.intToByteArray(numPrimes);
		byte [] size;
		byte [] exponentOut;
		if (defaultExponent()) {
			size = TpmUtils.intToByteArray(0);
			exponentOut = null;
		} else {
			size = TpmUtils.intToByteArray(exponent.length);
			exponentOut = exponent;
		}
		int x = keyLngth.length + numPrms.length + size.length;
		if (exponentOut != null) x += exponentOut.length;
		byte [] returnArray = new byte[x];
		System.arraycopy(keyLngth, 0, returnArray, 0, keyLngth.length);
		System.arraycopy(numPrms, 0, returnArray, keyLngth.length, numPrms.length);
		System.arraycopy(size, 0, returnArray, keyLngth.length + numPrms.length, size.length);
		if (exponentOut != null) System.arraycopy(exponentOut, 0, returnArray, keyLngth.length + numPrms.length + size.length, exponentOut.length);
		return returnArray;
	}
	/**
	 * Determine if the TCG-defined "default" public exponent is used for this key.
	 * 
	 * @return <b>True</b> if the exponent defined is 2^16 + 1 (65537 or 0x01 0x00 0x01).
	 */
	private boolean defaultExponent() {
		if (exponent == null) return true;
		byte [] defaultExp = {0x01, 0x00, 0x01};
		for (int i = 0; i < exponent.length; i++)
			if (exponent[i] != defaultExp[i])
				return false;
		return true;
	}
	/**
	 * @return The length of the RSA key.
	 */
	public int getKeyLength() {
		return keyLength;
	}
	/**
	 * Set the RSA key length.
	 * 
	 * @param newValue The new key length, in bits.
	 */
	public void setKeyLength(int newValue) {
		keyLength = newValue;
	}
	/**
	 * @return The number of primes.
	 */
	public int getValueData() {
		return numPrimes;
	}
	/**
	 * Set the number of primes for this RSA key params structure.
	 * 
	 * @param newValue The new number of primes.
	 */
	public void setValueData(int newValue) {
		numPrimes = newValue;
	}
	/**
	 * @return The exponent for this RSA key.
	 */
	public byte [] getByteData() {
		return exponent;
	}
	/**
	 * @param newValue Set the RSA exponent to this byte array.
	 */
	public void setByteData(byte [] newValue) {
		exponent = newValue;
	}
	/**
	 * Creates a human-readable report of the RSA key parameters structure.
	 * 
	 * @return The report.
	 */
	public String toString() {
		String returnVal = "";
		returnVal += "TpmRsaKeyParams:\n";
		returnVal += " keyLength: " + Integer.toString(keyLength) + "\n";
		returnVal += " numPrimes: " + Integer.toString(numPrimes) + "\n";
		returnVal += " exponent: " + TpmUtils.byteArrayToString(exponent, 16);
		return returnVal;
	}
}
