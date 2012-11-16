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

/**
 * <p>This class was created to hole all return values for running collateIdentityRequest.</p>
 * 
 * @author schawki
 *
 */
public class TpmIdentity {
	private byte [] identityRequestBytes = null;
	private byte [] aikModulus = null;
	private byte [] aikKeyBytes = null;
	/**
	 * Create a new TpmIdentity with no data set.
	 * 
	 */
	public TpmIdentity(){
		// default constructor
	}
	/**
	 * Create a new TpmIdentity, setting the Identity Request, AIK modulus, and AIK key blob.
	 * 
	 * @param idReq The Identity Request, as returned from CollateIdentityRequest, in raw byte form. 
	 * @param aikMod The AIK modulus.
	 * @param aikBlob The AIK in the form of a TPM_KEY.
	 */
	public TpmIdentity(byte[] idReq, byte [] aikMod, byte [] aikBlob){
		identityRequestBytes = idReq;
		aikModulus = aikMod;
		aikKeyBytes = aikBlob;
	}
	/**
	 * Set the Identity Request.
	 * 
	 * @param idReq Identity Request in raw byte form.
	 */
	public void setIdentityRequest(byte [] idReq){
		identityRequestBytes = idReq;
	}
	/**
	 * Set the AIK modulus.
	 * 
	 * @param aikMod AIK modulus in byte form.
	 */
	public void setAikModulus(byte [] aikMod){
		aikModulus = aikMod;
	}
	/**
	 * Set the AIK key blob in raw byte form.
	 * 
	 * @param aikBlob
	 */
	public void setAikBlob(byte [] aikBlob){
		aikKeyBytes = aikBlob;
	}
	/**
	 * Get the stored Identity Request.
	 * 
	 * @return Stored Identity Request in raw bytes, or null
	 */
	public byte [] getIdentityRequest(){
		return identityRequestBytes;
	}
	/**
	 * Get the stored AIK modulus.
	 * 
	 * @return Stored AIK modulus in raw byte form, or null.
	 */
	public byte [] getAikModulus(){
		return aikModulus;
	}
	/**
	 * Get the store AIK key blob.
	 * 
	 * @return Stored AIK key blob in raw byte form, or null.
	 */
	public byte [] getAikBlob(){
		return aikKeyBytes;
	}
}
