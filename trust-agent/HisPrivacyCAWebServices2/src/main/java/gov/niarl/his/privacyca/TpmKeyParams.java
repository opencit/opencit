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

/**
 * <p>This class is for the TCG's TPM_KEY_PARMS structure. Several other TCG values are defined here, as it seems appropriate to do so at the time.</p>
 * @author schawki
 *
 */
public class TpmKeyParams {
	public static final int TPM_ALG_RSA = 0x1;
	public static final int TPM_ALG_DES = 0x2;
	public static final int TPM_ALG_3DES = 0x3;
	public static final int TPM_ALG_SHA = 0x4;
	public static final int TPM_ALG_HMAC = 0x5;
	public static final int TPM_ALG_AES = 0x6;
	public static final int TPM_ALG_AES128 = TPM_ALG_AES;
	public static final int TPM_ALG_MGF1 = 0x7;
	public static final int TPM_ALG_AES192 = 0x8;
	public static final int TPM_ALG_AES256 = 0x9;
	public static final int TPM_ALG_XOR = 0xa;
	public static final short TPM_ES_NONE = 0x1;
	public static final short TPM_ES_RSAESPKCSv15 = 0x2;
	public static final short TPM_ES_RSAESOAEP_SHA1_MGF1 = 0x3;
	public static final short TPM_ES_SYM_CNT = 0x4;
	public static final short TPM_ES_SYM_OFB = 0x5;
	public static final short TPM_ES_SYM_CBC_PKCS5PAD = 0xff;
	public static final short TPM_SS_NONE = 0x1;
	public static final short TPM_SS_RSASAPKCS1v15_SHA1 = 2;
	public static final short TPM_SS_RSASAPKCS1v15_DER = 3;
	public static final short TPM_SS_RSASAPKCS1v15_INFO = 4;
	
	private int algorithmId;
	private short encScheme;
	private short sigScheme;
	private TpmKeySubParams subParams = null;
	private boolean TrouSerSmode = false;
	
	/**
	 * Given an algorithm, return the text string. Useful for debugging and logging.
	 * @param alg
	 * @return
	 */
	public static String algToString(int alg) {
		String returnVal = "";
		switch (alg) {
		case TPM_ALG_RSA:
			returnVal = "TPM_ALG_RSA";
			break;
		case TPM_ALG_DES:
			returnVal = "TPM_ALG_DES";
			break;
		case TPM_ALG_3DES:
			returnVal = "TPM_ALG_3DES";
			break;
		case TPM_ALG_SHA:
			returnVal = "TPM_ALG_SHA";
			break;
		case TPM_ALG_HMAC:
			returnVal = "TPM_ALG_HMAC";
			break;
		case TPM_ALG_AES:
			returnVal = "TPM_ALG_AES/TPM_ALG_AES128";
			break;
		case TPM_ALG_MGF1:
			returnVal = "TPM_ALG_MGF1";
			break;
		case TPM_ALG_AES192:
			returnVal = "TPM_ALG_AES192";
			break;
		case TPM_ALG_AES256:
			returnVal = "TPM_ALG_AES256";
			break;
		case TPM_ALG_XOR:
			returnVal = "TPM_ALG_XOR";
			break;
		default:
			returnVal = "UNKNOWN ALG VALUE! (" + Integer.toString(alg) + ")";
			break;
		}
		return returnVal;
	}
	/**
	 * Given an encryption scheme, return the string. Useful for debugging and logging.
	 * @param es
	 * @param TrouSerSmode
	 * @return
	 */
	public static String esToString(short es, boolean TrouSerSmode) {
		String returnVal = "";
		switch (es) {
		case TPM_ES_NONE:
			 returnVal = "TPM_ES_NONE";
			 if (TrouSerSmode)
				 returnVal += " (should be TPM_ES_SYM_CBC_PKCS5PAD)";
			 break;
		case TPM_ES_RSAESPKCSv15:
			 returnVal = "TPM_ES_RSAESPKCSv15";
			 break;
		case TPM_ES_RSAESOAEP_SHA1_MGF1:
			 returnVal = "TPM_ES_RSAESOAEP_SHA1_MGF1";
			 break;
		case TPM_ES_SYM_CNT:
			 returnVal = "TPM_ES_SYM_CNT";
			 break;
		case TPM_ES_SYM_OFB:
			 returnVal = "TPM_ES_SYM_OFB";
			 break;
		case TPM_ES_SYM_CBC_PKCS5PAD:
			 returnVal = "TPM_ES_SYM_CBC_PKCS5PAD";
			 break;
		default:
			returnVal = "UNKNOWN ENCSCHEME VALUE! (" + Short.toString(es) + ")";
			break;
		}
		return returnVal;
	}
	/**
	 * Given a signature scheme, return the string. Useful for debugging and logging.
	 * @param ss
	 * @return
	 */
	public static String ssToString(short ss) {
		String returnVal = "";
		switch (ss) {
		case TPM_SS_NONE:
			returnVal = "TPM_SS_NONE";
			break;
		case TPM_SS_RSASAPKCS1v15_SHA1:
			returnVal = "TPM_SS_RSASAPKCS1v15_SHA1";
			break;
		case TPM_SS_RSASAPKCS1v15_DER:
			returnVal = "TPM_SS_RSASAPKCS1v15_DER";
			break;
		case TPM_SS_RSASAPKCS1v15_INFO:
			returnVal = "TPM_SS_RSASAPKCS1v15_INFO";
			break;
		default:
			returnVal = "UNKNOWN SIGSCHEME VALUE! (" + Short.toString(ss) + ")";
			break;
		}
		return returnVal;
	}
	/**
	 * Create a new TpmKeyParms with no default values set.
	 */
	public TpmKeyParams(){}
	/**
	 * Create a new TpmKeyParams by extracting values from a ByteArrayInputStream.
	 * 
	 * @param source The InputStream.
	 * @throws TpmUtils.TpmUnsignedConversionException
	 * @throws TpmUtils.TpmBytestreamResouceException
	 */
	public TpmKeyParams(ByteArrayInputStream source) 
			throws TpmUtils.TpmUnsignedConversionException, 
			TpmUtils.TpmBytestreamResouceException {
		algorithmId = TpmUtils.getUINT32(source);
		encScheme = TpmUtils.getUINT16(source);
		sigScheme = TpmUtils.getUINT16(source);
		int subParamSize = TpmUtils.getUINT32(source);
		if ((algorithmId != TPM_ALG_RSA) && (subParamSize == 0)) {
			TrouSerSmode = true;
		}
		switch (algorithmId) {
		case TPM_ALG_RSA:
			subParams = new TpmRsaKeyParams(source, subParamSize);
			break;
		case TPM_ALG_DES:
		case TPM_ALG_3DES:
		case TPM_ALG_AES: //same as TPM_ALG_AES128; most likely to occur
		case TPM_ALG_AES192:
		case TPM_ALG_AES256:
			subParams = new TpmSymmetricKeyParams(source, subParamSize);
			break;
		default: //?could be an issue if something other than RSA or a DES/AES variant?
			if (subParamSize > 0)
				TpmUtils.getBytes(source, subParamSize); //just throw it away -- what else to do with it??
			break;
		}
	}
	/**
	 * Manually set TrouSerS mode. <b>True</b> means that the symmetric IV will appear at the beginning of a symmetrically 
	 * encrypted blob, while <b>false</b> means that it will appear within a SymmetricKeyParams structure. This value is 
	 * used when determining the format of the byte arrays that are passed back from the Privacy CA. By default the Privacy 
	 * CA will set this value based on the format of the incoming identity request.
	 * 
	 * @param state
	 */
	public void setTrouSerSmode(boolean state) {
		TrouSerSmode = state;
	}
	/**
	 * 
	 * @return The current state of TrouSerS mode for this key params structure.
	 */
	public boolean getTrouSerSmode() {
		return TrouSerSmode;
	}
	/**
	 * Assemble the KeyParams structure into a byte array.
	 * 
	 * @return The byte array.
	 * @throws TpmUtils.TpmUnsignedConversionException
	 */
	public byte [] toByteArray() 
			throws TpmUtils.TpmUnsignedConversionException {
		byte [] algoId = TpmUtils.intToByteArray(algorithmId);
		byte [] encSchm = TpmUtils.shortToByteArray(encScheme);
		byte [] sigSchm = TpmUtils.shortToByteArray(sigScheme);
		byte [] size;
		byte [] subParms;
		int x;
		if (TrouSerSmode && (algorithmId != 1)) {
			size = TpmUtils.intToByteArray(0);
			subParms = null;
			x = algoId.length + encSchm.length + sigSchm.length + size.length;
		}
		else {
			if (subParams != null) {
				subParms = subParams.toByteArray();
				size = TpmUtils.intToByteArray(subParms.length);
				x = algoId.length + encSchm.length + sigSchm.length + size.length + subParms.length;
			}
			else {
				subParms = null;
				size = TpmUtils.intToByteArray(0);
				x = algoId.length + encSchm.length + sigSchm.length + size.length;
			}
		}
		byte [] returnArray = new byte[x];
		System.arraycopy(algoId, 0, returnArray, 0, algoId.length);
		System.arraycopy(encSchm, 0, returnArray, algoId.length, encSchm.length);
		System.arraycopy(sigSchm, 0, returnArray, algoId.length + encSchm.length, sigSchm.length);
		System.arraycopy(size, 0, returnArray, algoId.length + encSchm.length + sigSchm.length, size.length);
		if (subParms != null) System.arraycopy(subParms, 0, returnArray, algoId.length + encSchm.length + sigSchm.length + size.length, subParms.length);
		return returnArray;
	}
	/**
	 * 
	 * @return A String representing a human-readable report of the Key Params.
	 */
	public String toString() {
		String returnVal = "";
		returnVal += "TpmKeyParams:\n";
		//returnVal += " algorithmId: " + Integer.toString(algorithmId) + "\n";
		//returnVal += " encScheme: " + Short.toString(encScheme) + "\n";
		//returnVal += " sigScheme: " + Short.toString(sigScheme) + "\n";
		returnVal += " algorithmId: " + TpmKeyParams.algToString(algorithmId) + "\n";
		returnVal += " encScheme: " + TpmKeyParams.esToString(encScheme, TrouSerSmode) + "\n";
		returnVal += " sigScheme: " + TpmKeyParams.ssToString(sigScheme) + "\n";
		returnVal += " subParameters:";//\n";
		if (TrouSerSmode) {
			returnVal += " (fabricated: TrouSerS-style IV placement)";
		}
		returnVal += "\n" + subParams.toString();
		return returnVal;
	}
	/**
	 * 
	 * @return The current algorithm ID value.
	 */
	public int getAlgorithmId() {
		return algorithmId;
	}
	/**
	 * Set a new algorithm ID value;
	 * 
	 * @param newAlgId The new value.
	 */
	public void setAlgorithmId(int newAlgId) {
		algorithmId = newAlgId;
	}
	/**
	 * 
	 * @return The current encryption scheme value.
	 */
	public short getEncScheme() {
		return encScheme;
	}
	/**
	 * Set a new encryption scheme value;
	 * 
	 * @param newEncScheme The new value.
	 */
	public void setEncScheme(short newEncScheme) {
		encScheme = newEncScheme;
	}
	/**
	 * 
	 * @return The current signature scheme.
	 */
	public short getSigScheme() {
		return sigScheme;
	}
	/**
	 * Set a new signature scheme value.
	 * 
	 * @param newSigScheme The new value.
	 */
	public void setSigScheme(short newSigScheme) {
		sigScheme = newSigScheme;
	}
	/**
	 * 
	 * @return The currently assigned TpmKeySubParams. Will be <b>null</b>, or a TpmRsaKeyParams or TpmSymmetricKeyParams structure.
	 */
	public TpmKeySubParams getSubParams() {
		return subParams;
	}
	/**
	 * Set a new TpmKeySubParams structure.
	 * 
	 * @param newSubParams The new TpmRsaKeyParams or TpmSymmetricKeyParams structure.
	 */
	public void setSubParams(TpmKeySubParams newSubParams) {
		subParams = newSubParams;
	}
}
