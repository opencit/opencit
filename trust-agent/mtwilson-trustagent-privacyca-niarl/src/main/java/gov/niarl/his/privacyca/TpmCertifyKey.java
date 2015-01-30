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
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;

/**
 * <p>This class is for the TCG's TPM_KEY structure.</p>
 * @author schawki
 *
 */
public class TpmCertifyKey {
        private static int TPM_SHA1_160_HASH_LEN = 20;
        private static int TPM_SHA1BASED_NONCE_LEN = TPM_SHA1_160_HASH_LEN;
	private byte [] structVer = {(byte)0x01, (byte)0x01, (byte)0x00, (byte)0x00};
	private short tpmKeyUsage = 0;//UINT16
	private int tpmKeyFlags = 0;//UINT32
	private byte tpmAuthDataUsage = (byte)0x00;//BYTE (just 1)
	private TpmKeyParams keyParms= null;
        private byte [] publicKeyDigest = null;
        private byte [] nonce = null;
        private byte parentPCRStatus = (byte) 0x00; // 0x00-false, 0x01-true
        private int pcrInfoSize = 0; //UINT32

        
	public TpmCertifyKey(){}
        
	public TpmCertifyKey(byte [] blob) throws TpmBytestreamResouceException, TpmUnsignedConversionException {
		ByteArrayInputStream bs = new ByteArrayInputStream(blob);
		structVer = TpmUtils.getBytes(bs, 4); 
		tpmKeyUsage = TpmUtils.getUINT16(bs); 
		tpmKeyFlags = TpmUtils.getUINT32(bs); 
		tpmAuthDataUsage = TpmUtils.getBytes(bs, 1)[0]; //byte
		keyParms = new TpmKeyParams(bs); //TpmKeyParams
                publicKeyDigest = TpmUtils.getBytes(bs, TPM_SHA1_160_HASH_LEN);
                nonce = TpmUtils.getBytes(bs, TPM_SHA1BASED_NONCE_LEN);
                parentPCRStatus = TpmUtils.getBytes(bs, 1)[0];
                pcrInfoSize = TpmUtils.getUINT32(bs);
	}
        
	public byte [] getPublicKey() {
		return publicKeyDigest;
	}

	public RSAPublicKey getKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
		byte [] pubExp = new byte[3];
		pubExp[0] = (byte)(0x01 & 0xff);
//		pubExp[1] = (byte)(0x00 & 0xff);
		pubExp[1] = (byte)(0x00);
		pubExp[2] = (byte)(0x01 & 0xff);
		return TpmUtils.makePubKey(publicKeyDigest, pubExp);
	}
        
        public String getPublicKeyAsString() {
            return TpmUtils.byteArrayToHexString(publicKeyDigest);            
        }
        
        public String getNonceAsString() {
            return TpmUtils.byteArrayToHexString(nonce);
        }
        
        public boolean getParentPCRStatus() {
            return (parentPCRStatus != 0);
        }
        
        public int getPcrInfoSize() {
            return pcrInfoSize;
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
}
