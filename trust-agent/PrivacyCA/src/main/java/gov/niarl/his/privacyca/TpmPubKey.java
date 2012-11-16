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

import java.io.*;
import java.security.*;
import java.security.spec.*;
import java.security.interfaces.*;
import java.math.*;

/**
 * <p>This class is for the TCG's TPM_PUBKEY structure.</p>
 * @author schawki
 *
 */
public class TpmPubKey {
	private TpmKeyParams algorithm;
	private byte [] key; //just the modulus!!
	
	public TpmPubKey() {}
	/**
	 * Create new TPM_PUBKEY by specifying the public key, encryption scheme and signature scheme.
	 * 
	 * @param pubKey The RSA public key structure, containing the modulus and public exponent.
	 * @param encScheme The defined encryption scheme value.
	 * @param sigscheme The defined signature scheme value.
	 */
	public TpmPubKey(RSAPublicKey pubKey, int encScheme, int sigscheme) {
		BigInteger keyBI = pubKey.getModulus();
		key = keyBI.toByteArray();
		if ((key.length == 257)&&(key[0]==0x00)) {
			byte [] newKey = new byte[256];
			for (int i = 0; i < 256; i++)
				newKey[i] = key[i+1];
			key = newKey;
		}
		algorithm = new TpmKeyParams();
		algorithm.setAlgorithmId(1);
		algorithm.setEncScheme((short)encScheme);
		algorithm.setSigScheme((short)sigscheme);
		algorithm.setSubParams(new TpmRsaKeyParams());
		algorithm.getSubParams().setByteData(pubKey.getPublicExponent().toByteArray());
		algorithm.getSubParams().setValueData(2); //numPrimes
		algorithm.getSubParams().setKeyLength(2048);
	}
	/**
	 * Create a TPM_PUBKEY using the specified modulus. An default encryption scheme of 1 (TPM_ES_NONE) and signature scheme 
	 * of 2 (TPM_SS_RSASSAPKCS1v15_SHA1) will be used.
	 * 
	 * @param newKey The new modulus in the form of a byte array.
	 */
	public TpmPubKey(byte [] newKey) {
		key = newKey;
		algorithm = new TpmKeyParams();
		algorithm.setAlgorithmId(1);
		algorithm.setEncScheme((short)1);
		algorithm.setSigScheme((short)2);
		algorithm.setSubParams(new TpmRsaKeyParams());
		byte [] pubExp = {0x01, 0x00, 0x01};
		algorithm.getSubParams().setByteData(pubExp);
		algorithm.getSubParams().setValueData(2); //numPrimes
		algorithm.getSubParams().setKeyLength(2048);
	}
	/**
	 * Create a new TPM_PUBKEY by extracting it from a byte stream.
	 * 
	 * @param source The ByteArrayInputStream from which to extract the TPM_PUBKEY.
	 * @throws TpmUtils.TpmUnsignedConversionException
	 * @throws TpmUtils.TpmBytestreamResouceException
	 */
	public TpmPubKey(ByteArrayInputStream source) throws TpmUtils.TpmUnsignedConversionException, TpmUtils.TpmBytestreamResouceException {
		algorithm = new TpmKeyParams(source);
		int storeKeyLength = TpmUtils.getUINT32(source);
		key = TpmUtils.getBytes(source, storeKeyLength);
	}
	/**
	 * Get the RSA key modulus.
	 * 
	 * @return The modulus as a byte array.
	 */
	public byte [] getKeybytes() {
		return key;
	}
	/**
	 * Return an RSAPublicKey structure representing the key stored in this TPM_PUBKEY.
	 * 
	 * @return The RSAPublicKey.
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 */
	public RSAPublicKey getKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
		byte [] pubExp = new byte[3];
		pubExp[0] = (byte)(0x01 & 0xff);
//		pubExp[1] = (byte)(0x00 & 0xff);
		pubExp[1] = (byte)(0x00);
		pubExp[2] = (byte)(0x01 & 0xff);
		return TpmUtils.makePubKey(key, pubExp);
	}
	/**
	 * Return a human-readable report of the TPM_PUBKEY.
	 * 
	 * @return The key report.
	 */
	public String toString() {
		String returnVal = "";
		returnVal += "TpmPubKey:\n";
		returnVal += " " + algorithm.toString() + "\n";
		returnVal += " key:\n" + TpmUtils.byteArrayToString(key, 16);
		return returnVal;
	}
	/**
	 * 
	 * @return The serialized TPM_PUBKEY.
	 * @throws TpmUtils.TpmUnsignedConversionException
	 */
	public byte [] toByteArray() throws TpmUtils.TpmUnsignedConversionException {
		byte[] algo = algorithm.toByteArray();
		byte[] keySize = TpmUtils.intToByteArray(key.length);
		int x = algo.length + keySize.length + key.length;
		byte [] returnArray = new byte[x];
		System.arraycopy(algo, 0, returnArray, 0, algo.length);
		System.arraycopy(keySize, 0, returnArray, algo.length, keySize.length);
		System.arraycopy(key, 0, returnArray, algo.length + keySize.length, key.length);
		return returnArray;
	}
}
