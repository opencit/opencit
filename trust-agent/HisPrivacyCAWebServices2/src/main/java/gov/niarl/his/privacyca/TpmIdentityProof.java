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
import java.io.*;
import javax.security.cert.*;

/**
 * See TPM_IDENTITY_PROOF<p>
 * 
 * <p>The TpmIdentityProof is the plain-text incarnation of a TPM's request for an 
 * AIK credential. The data contained in the identity proof is used to verify the 
 * integrity of the request, and to construct an AIK credential.</p>
 * 
 * The TPM_IDENTITY_PROOF structure, as defined by the TCG, contains the following elements:
 * <ul>
 * <li><b>TPM_STRUCT_VER</b>, which is a byte array always equal to 0x01010000</li>
 * <li><b>AIK</b> in the form of a TPM_PUBKEY (TpmPubKey class)</li>
 * <li><b>Identity label</b> The requested name for the identity. This will (probably) be placed in the Subject Alternative Name field in the AIC by the Privacy CA, per specification.</li>
 * <li><b>Identity binding</b> This is the signature, made using the private AIK, of a hash made of the TPM_IDENTITY_CONTENTS structure (see note below).</li>
 * <li><b>Endorsement Certificate</b> (optional) This really should be a required item, but isn't.</li>
 * <li><b>Platform Certificate</b> (optional)</li>
 * <li><b>Conformance Certificate</b> (optional)</li>
 * </ul>
 * 
 * This class adds additional flags to clarify discrepancies that have been observed among TSS implementations. These flags include:
 * <ul>
 * <li><b>TrousersModeIV</b> (boolean) Though both NTRU and TrouSerS place the initialization vector for symmetrically encrypted data within, but at the beginning of, the encrypted data blob, the specification states that the IV should be recorded within the TPM_KEY_PARMS structure for the symmetric key. Setting this boolean to TRUE indicates the use of "TrouSerS-style placement.</li>
 * <li><b>TrousersModeSymkeyEncscheme</b> (boolean) TrouSerS (but not NTRU) incorrectly sets the symmetric encryption scheme to TPM_ES_NONE. The correct scheme to use is TPM_ES_SYM_CBC_PKCS5PAD. Both NTRU and TrouSerS <i>use</i> this scheme for encryption, but TrouSerS records it as otherwise.</li>
 * <li><b>TrousersModeBlankOeap</b> (boolean) TrouSerS incorrectly encrypts the asymmetric blob, as it uses a blank OEAP password. The password should be "TCPA".</li>
 * </ul>
 * 
 * <p>Developer note: Methods in this class are responsible for working with all aspects of the 
 * TPM_IDENTITY_CONTENTS structure. A future version of the TPM support code may include 
 * a TpmIdentityContents class, which would greatly simplify the code in this class.</p>
 * 
 * <p>This class can be used by a Privacy CA for parsing an incoming request, but 
 * it can also be used by a client for constructing a new request.</p>
 * 
 * @author schawki
 * @see TpmIdentityRequest
 */
public class TpmIdentityProof {
	private byte [] structVer;
	private TpmPubKey Aik;
	private byte [] idLabelBytes;
	private byte [] idBindingBytes;
	private byte [] ekCredBytes;
	private byte [] platformCredBytes;
	private byte [] conformCredBytes;
	private boolean TrousersModeIV = false;
	private boolean TrousersModeSymkeyEncscheme = false;
	private boolean TrousersModeBlankOeap = false;
	/**
	 * Get the TrousersModeIV status. A value of TRUE indicates that the IV placement is inside, and at the beginning of, the symmetrically encrypted blob and not in the symmetric key parameters. This will almost always be set at TRUE.
	 * 
	 * @return The status of the flag.
	 */
	public boolean getIVmode(){
		return TrousersModeIV;
	}
	/**
	 * Set the TrousersModeIV status. A value of TRUE indicates that the IV placement is inside, and at the beginning of, the symmetrically encrypted blob and not in the symmetric key parameters. This will almost always be set at TRUE.
	 * 
	 * @param newMode Set to TRUE for greatest compatibility; set to FALSE for compliance to specification.
	 */
	public void setIVmode(boolean newMode) {
		TrousersModeIV = newMode;
	}
	/**
	 * Get the TrousersModeSymkeyEncscheme status. A value of TRUE indicates that the identity proof was constructed by TrouSerS, and is not compliant with the specification.
	 * 
	 * @return The status of the flag.
	 */
	public boolean getSymkeyEncscheme() {
		return TrousersModeSymkeyEncscheme;
	}
	/**
	 * Set the TrousersModeSymkeyEncscheme flag. This <i>should</i> always be set to FALSE.
	 * 
	 * @param newScheme Set to TRUE to emulate an identity proof created by TrouSerS; set to FALSE to comply with the specification.
	 */
	public void setSymkeyEncscheme(boolean newScheme) {
		TrousersModeSymkeyEncscheme = newScheme;
	}
	/**
	 * Get the TrousersModeBlankOeap flag. A value of TRUE indicates that the identity proof was constructed by TrouSerS, and is not compliant with the specification.
	 * 
	 * @return The status of the flag.
	 */
	public boolean getOeapMode () {
		return TrousersModeBlankOeap;
	}
	/**
	 * Set the TrousersModeBlankOeap flag. A value of TRUE indicates that the identity proof was constructed by TrouSerS, and is not compliant with the specification.
	 * 
	 * @param newMode Set to TRUE to emulate an identity proof constructed by TrouSerS; set to FALSE to comply with the specification.
	 */
	public void getOeapMode(boolean newMode) {
		TrousersModeBlankOeap = newMode;
	}
	/**
	 * Create new TpmIdentityProof object by parsing the decrypted data from a 
	 * TPM_IDENTITY_REQ. Information gathered while parsing and decrypting the 
	 * Identity Request is needed to properly parse the Ideneity Proof. Also, 
	 * that information may be useful when crafting the response to the client.
	 * 
	 * @param blob The decrypted form of the sym blob from the Identity Request.
	 * @param IV Set to TRUE if the IV was placed at the beginning of the symblob of the Identity Request instead of in the key parameters. 
	 * @param symKey Set to TRUE if the symmetric encryption scheme was set to TPM_ES_NONE in the Identity Request.
	 * @param oaep Set to TRUE if the OAEP password was blank.
	 * @throws PrivacyCaException 
	 * @throws TpmUtils.TpmUnsignedConversionException
	 * @throws TpmUtils.TpmBytestreamResouceException
	 */
	public TpmIdentityProof(byte[] blob, boolean IV, boolean symKey, boolean oaep) 
			throws PrivacyCaException, 
			TpmUtils.TpmUnsignedConversionException, 
			TpmUtils.TpmBytestreamResouceException {
		TrousersModeIV = IV;
		TrousersModeSymkeyEncscheme = symKey;
		TrousersModeBlankOeap = oaep;
		ByteArrayInputStream bs = new ByteArrayInputStream(blob);
		structVer = new byte[4];
		structVer = TpmUtils.getBytes(bs, 4);
		int labelSize = TpmUtils.getUINT32(bs);
		int identBindingSize = TpmUtils.getUINT32(bs);
		int ekCredSize = TpmUtils.getUINT32(bs);
		//This is where things go wrong with no EK certificate!!
		//if (ekCredSize == 0) throw new PrivacyCaException("PrivacyCaException: Error parsing TPM_IDENTITY_PROOF: there is no endorsement credential.");
		int platformCredSize = TpmUtils.getUINT32(bs);
		int conformCredSize = TpmUtils.getUINT32(bs);
		Aik = new TpmPubKey(bs);
		idLabelBytes = TpmUtils.getBytes(bs, labelSize);
		idBindingBytes = TpmUtils.getBytes(bs, identBindingSize);
		ekCredBytes = TpmUtils.getBytes(bs, ekCredSize);
		platformCredBytes = TpmUtils.getBytes(bs, platformCredSize);
		conformCredBytes = TpmUtils.getBytes(bs, conformCredSize);
	}
	/**
	 * Create a new TpmIdentityProof by supplying all of the necessary elements to construct one from scratch.
	 * 
	 * @param idLabel The requested Identity Label. This is usually assigned as the Subject Alternative Name in the AIC. Supply as ASCII string in byte array.
	 * @param idBinding The identity binding is supplied from the TPM by running TPM_MakeIdentity (available via TCS, not TSP). Should be 256 byte (signature made using 2048 bit AIK).
	 * @param AIK The AIK in TpmPubKey form. This should also be supplied from the TPM using TPM_MakeIdentity. 
	 * @param ekCertBytes (Optional) Endorsement Certificate (EC), usually as an X.509 certificate, as a byte array. Null is acceptable.
	 * @param platformCertBytes (Optional) Platform Certificate, usually as an X.509 certificate, as a byte array. Null is acceptable.
	 * @param conformanceCertBytes (Optional) Conformance Certificate, usually as an X.509 certificate, as a byte array. Null is acceptable.
	 * @param IV TrouSerS IV placement mode flag. Recommended setting is TRUE.
	 * @param symKey TrouSerS symmetric encryption scheme flag. Recommended setting is FALSE.
	 * @param oaep TrouSerS use of blank OAEP password flag. Recommended setting is FALSE.
	 */
	public TpmIdentityProof(byte [] idLabel, byte [] idBinding, TpmPubKey AIK, byte [] ekCertBytes, byte [] platformCertBytes, byte [] conformanceCertBytes, boolean IV, boolean symKey, boolean oaep) {
		TrousersModeIV = IV;
		TrousersModeSymkeyEncscheme = symKey;
		TrousersModeBlankOeap = oaep;
		byte [] temp = {(byte)0x01, (byte)0x01, (byte)0x00, (byte)0x00};
		structVer = temp;
		Aik = AIK;
		idLabelBytes = idLabel;
		idBindingBytes = idBinding;
		ekCredBytes = ekCertBytes;
		platformCredBytes = platformCertBytes;
		conformCredBytes = conformanceCertBytes;
	}
	/**
	 * Get the Identity Proof in the form of a byte array.
	 * 
	 * @return Entire Identity Proof in the form of a byte array.
	 * @throws TpmUtils.TpmUnsignedConversionException Thrown if there are any out-of-bounds problems converting from (signed) Java long to UINT32.
	 */
	public byte [] toByteArray() 
			throws TpmUtils.TpmUnsignedConversionException {
		// Get byte elements of the proof
	    //byte [] structVer - already exists in final form
		byte [] labelSize = TpmUtils.intToByteArray(idLabelBytes.length); 
		byte [] idBindingSize = TpmUtils.intToByteArray(idBindingBytes.length);
		byte [] endorsementSize = TpmUtils.intToByteArray(ekCredBytes.length);
		byte [] platformSize = TpmUtils.intToByteArray(ekCredBytes.length);
		byte [] conformanceSize = TpmUtils.intToByteArray(ekCredBytes.length);
		byte [] identityKey = Aik.toByteArray();
		//byte [] idLabelBytes - already exists in final form
		//byte [] idBindingBytes - already exists in final form
		 //byte [] ekCredBytes - already exists in final form
		//byte [] platformCredBytes - already exists in final form
		 //byte [] conformCredBytes - already exists in final form
		 // Assemble the return array
		 byte [] toReturn = new byte[structVer.length + labelSize.length + idBindingSize.length + endorsementSize.length + platformSize.length + conformanceSize.length + 
		  		                            identityKey.length + idLabelBytes.length + idBindingBytes.length + ekCredBytes.length + platformCredBytes.length + conformCredBytes.length];
		int copyOffset = 0;
		System.arraycopy(structVer, 0, toReturn, copyOffset, structVer.length);
		copyOffset += structVer.length;
		System.arraycopy(labelSize, 0, toReturn, copyOffset, labelSize.length);
		copyOffset += labelSize.length;
		System.arraycopy(idBindingSize, 0, toReturn, copyOffset, idBindingSize.length);
		copyOffset += idBindingSize.length;
		System.arraycopy(endorsementSize, 0, toReturn, copyOffset, endorsementSize.length);
		copyOffset += endorsementSize.length;
		System.arraycopy(platformSize, 0, toReturn, copyOffset, platformSize.length);
		copyOffset += platformSize.length;
		System.arraycopy(conformanceSize, 0, toReturn, copyOffset, conformanceSize.length);
		copyOffset += conformanceSize.length;
		System.arraycopy(identityKey, 0, toReturn, copyOffset, identityKey.length);
		copyOffset += identityKey.length;
		System.arraycopy(idLabelBytes, 0, toReturn, copyOffset, idLabelBytes.length);
		copyOffset += idLabelBytes.length;
		System.arraycopy(idBindingBytes, 0, toReturn, copyOffset, idBindingBytes.length);
		copyOffset += idBindingBytes.length;
		System.arraycopy(ekCredBytes, 0, toReturn, copyOffset, ekCredBytes.length);
		copyOffset += ekCredBytes.length;
		System.arraycopy(platformCredBytes, 0, toReturn, copyOffset, platformCredBytes.length);
		copyOffset += platformCredBytes.length;
		System.arraycopy(conformCredBytes, 0, toReturn, copyOffset, conformCredBytes.length);
		return toReturn;
	}
	/**
	 * Display the parsed contents of the request in a form suitable for display on console or in log file. This is intended to be used when troubleshooting.
	 * 
	 * @return Multi-line human readable breakdown of identity proof contents
	 */
	public String toString() {
		String returnVal = "";
		returnVal += "TpmIdentityProof:\n";
		returnVal += " StructVer: " + TpmUtils.byteArrayToString(structVer, 16) + "\n";
		returnVal += " Aik:\n" + Aik.toString() + "\n";
		String junk = new String(idLabelBytes);
		returnVal += " idLabel:\n" + junk + "\n";
		returnVal += " idBinding:\n" + TpmUtils.byteArrayToString(idBindingBytes, 16) + "\n";
		returnVal += " ekCred:\n" + TpmUtils.byteArrayToString(ekCredBytes, 16) + "\n";
		returnVal += " platformCred:\n" + TpmUtils.byteArrayToString(platformCredBytes, 16) + "\n";
		returnVal += " conformCred:\n" + TpmUtils.byteArrayToString(conformCredBytes, 16) + "\n";
		return returnVal;
	}
	/**
	 * Use the identity binding (the signature value of the TPM_IDENTITY_CONTENTS structure) to determine the validity of the request.<p>
	 * The TPM_IDENTITY_CONTENTS structure contains the public AIK and a hash of the idLabel and the Privacy CA's public key.<p>
	 * 
	 * @param caPubKey The Privacy CA's public key.
	 * @return <b>True</b> if the identity binding checks out, <b>false</b> if it does not.
	 * @throws NoSuchAlgorithmException
	 * @throws TpmUtils.TpmUnsignedConversionException
	 * @throws InvalidKeyException
	 * @throws InvalidKeySpecException
	 * @throws SignatureException
	 */
	public boolean checkValidity(RSAPublicKey caPubKey) 
			throws NoSuchAlgorithmException, 
			TpmUtils.TpmUnsignedConversionException, 
			InvalidKeyException, 
			InvalidKeySpecException, 
			SignatureException {
		TpmPubKey pca = new TpmPubKey(caPubKey, 3, 1);
		MessageDigest md = MessageDigest.getInstance("SHA1");
		byte [] pcaBytes = pca.toByteArray();
		byte [] chosenId = new byte[idLabelBytes.length + pcaBytes.length];
		System.arraycopy(idLabelBytes, 0, chosenId, 0, idLabelBytes.length);
		System.arraycopy(pcaBytes, 0, chosenId, idLabelBytes.length, pcaBytes.length);
		md.update(chosenId);
		byte [] chosenIdHash = md.digest();
		byte [] tpmMakeIdOrd = TpmUtils.intToByteArray(0x79);
		byte [] aikPubKey = Aik.toByteArray();
		//Structver in new (NTru) requests is appearing as 01 02 04 1E. Strange. (9/8/2009).
		//To compensate, using a shim copy of structver populated with the correct values.
		//
		//In this case, the problem is that the structver included as cleartext in the identity proof is
		//placed by the TSS, but the identity binding is created by the TPM independently. If one value of
		//structver is used during the creation of the identity binding signature but a different structver
		//is used when performing verification, the result will always be a failure to verify.
		byte [] thisStructVer = structVer;
		byte [] traditionalStructVer = {(byte)0x01, (byte)0x01, (byte)0x00, (byte)0x00};
		thisStructVer = traditionalStructVer;
		byte [] identityContents = new byte[thisStructVer.length + tpmMakeIdOrd.length + chosenIdHash.length + aikPubKey.length];
		System.arraycopy(thisStructVer, 0, identityContents, 0, thisStructVer.length);
		System.arraycopy(tpmMakeIdOrd, 0, identityContents, thisStructVer.length, tpmMakeIdOrd.length);
		System.arraycopy(chosenIdHash, 0, identityContents, thisStructVer.length + tpmMakeIdOrd.length, chosenIdHash.length);
		System.arraycopy(aikPubKey, 0, identityContents, thisStructVer.length + tpmMakeIdOrd.length + chosenIdHash.length, aikPubKey.length);
		Signature sig = Signature.getInstance("SHA1withRSA");
		sig.initVerify(Aik.getKey());
		sig.update(identityContents);
		boolean bindingCheck = sig.verify(idBindingBytes);
		//if (bindingCheck) System.out.println("bindingCheck is TRUE"); else System.out.println("bindingCheck is FALSE");
		return bindingCheck;
	}
	/**
	 * Get the AIK stored in the request.
	 * 
	 * @return The AIK as a TpmPubKey.
	 */
	public TpmPubKey getAik() {
		return Aik;
	}
	/**
	 * Get the TPM_STRUCT_VER. Should always be 0x01010000.
	 * 
	 * @return The four-byte TpmStructVer.
	 */
	public byte [] getVer() {
		return structVer;
	}
	/**
	 * Get the Identity Label string (in ASCII byte array) as stored in the Identity Proof.
	 * 
	 * @return The identity label as a byte string (ASCII).
	 */
	public byte [] getIdLableBytes() {
		return idLabelBytes;
	}
	/**
	 * Return the Identity Binding. It should be the signature of the Identity Contents made using the AIK. Should always be 256 bytes.
	 * 
	 * @return The identity binding, as extracted directly from the identity proof.
	 */
	public byte [] getIdBindingBytes() {
		return idBindingBytes;
	}
	/**
	 * EC stored in the Identity Proof, if present, in the form of raw bytes.
	 * 
	 * @return The X509 Endorsement Key Certificate as a byte array. This must be present to complete the Privacy CA process.
	 */
	public byte [] getEkCredBytes() {
		return ekCredBytes;
	}
	/**
	 * EC stored in the Identity Proof, if present, in the form of an X509Certificate object.
	 * 
	 * @return The EK certificate as a java X509Certificate.
	 * @throws CertificateException
	 */
	public X509Certificate getEkCred() 
			throws CertificateException {
		return X509Certificate.getInstance(ekCredBytes);
	}
	/**
	 * PC stored in the Identity Proof, if present, in the form of raw bytes.
	 * 
	 * @return The Platform Certificate as a byte array. May be <b>null</b>.
	 */
	public byte [] getPlatformCredBytes() {
		return platformCredBytes;
	}
	/**
	 * PC stored in the Identity Proof, if present, in the form of an X509Certificate object.
	 * 
	 * @return The Platform Certificate as a Java X509Certificate. If not present, will throw exception.
	 * @throws CertificateException
	 * @throws java.security.cert.CertificateException
	 * @throws java.security.cert.CertificateEncodingException
	 */
	public X509Certificate getPlatformCred() 
			throws CertificateException, 
			java.security.cert.CertificateException, 
			java.security.cert.CertificateEncodingException {
		return getCertFromBytes(platformCredBytes);
	}
	/**
	 * CC stored in the Identity Proof, if present, in the form of raw bytes.
	 * 
	 * @return The Conformance Credential as a byte array. May be <b>null</b>.
	 */
	public byte [] getConformCredBytes() {
		return conformCredBytes;
	}
	/**
	 * CC stored in the Identity Proof, if present, in the form of an X509Certificate object.
	 * 
	 * @return The Conformance Credential as a Java X509Certificate. If not present, will throw exception.
	 * @throws CertificateException
	 * @throws java.security.cert.CertificateException
	 * @throws java.security.cert.CertificateEncodingException
	 */
	public X509Certificate getConformCred() 
			throws CertificateException, 
			java.security.cert.CertificateException, 
			java.security.cert.CertificateEncodingException {
		return getCertFromBytes(conformCredBytes);
	}
	/**
	 * Convert from a byte array to a Java X509 Certificate. By default, all of the Privacy CA functions use 
	 * javax.security.cert.X509Certificate. The conversion process requires the creation of a 
	 * java.security.cert.X509Certificate. Although these two are identitical in structure, their member 
	 * functions are different, and are seen by Java as different. This function performs the conversion process
	 * between the two.
	 * 
	 * @param certBytes The byte array to convert.
	 * @return A javax.security.cert.X509Certificate.
	 * @throws CertificateException
	 * @throws java.security.cert.CertificateException
	 * @throws java.security.cert.CertificateEncodingException
	 */
	private X509Certificate getCertFromBytes(byte [] certBytes) 
			throws CertificateException, 
			java.security.cert.CertificateException, 
			java.security.cert.CertificateEncodingException {
		ByteArrayInputStream bs = new ByteArrayInputStream(certBytes);
		java.security.cert.CertificateFactory cf = java.security.cert.CertificateFactory.getInstance("X.509");
		java.security.cert.X509Certificate cert = (java.security.cert.X509Certificate)cf.generateCertificate(bs);
		X509Certificate xcert = javax.security.cert.X509Certificate.getInstance(cert.getEncoded());
		return xcert;
	}
}
