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


//import gov.niarl.his.privacyca.TpmUtils.TpmBytestreamResouceException;
//import gov.niarl.his.privacyca.TpmUtils.TpmUnsignedConversionException;

//import java.io.ByteArrayInputStream;
import java.security.*;
import java.security.interfaces.*;
import java.util.*;

/**
 * This class was created to test the functionality the TPM Module quote routine. Although there may not be a production need for this function,
 * it does contain a routine that can determine the validity of a quote and signature by recreating the quote using nonce, and raw PCR values, 
 * then verify using the public key in the AIK certificate.
 * 
 * @author schawki
 *
 */
public class TpmIntegrityReport {
	private Vector<PCR> PCRlist = new Vector<PCR>();
	private byte[] quote;
	private byte[] signature;
	/**
	 * Create a new TpmIntegrityReport, ready to accept data.
	 */
	public TpmIntegrityReport() {
		PCRlist.trimToSize();
	}
	/**
	 * Add a PCR value (these should be added in the order that they were requested from the TPM).
	 * 
	 * @param newPCRbytes The new PCR value as a 20-byte array.
	 */
	public void addPCR(byte[] newPCRbytes) {
		PCR newPcr = new PCR(newPCRbytes);
		PCRlist.add(newPcr);
	}
	/**
	 * Add a PCR value (these should be added in the order that they were requested from the TPM).
	 * 
	 * @param newPCR The new PCR vale as a PCR structure.
	 */
	public void addPCR(PCR newPCR) {
		PCRlist.add(newPCR);
	}
	/**
	 * Set the quote.
	 * 
	 * @param newQuote The 48 byte quote.
	 */
	public void setQuote(byte[] newQuote) {
		quote = newQuote;
	}
	/**
	 * Set the quote signature.
	 * 
	 * @param newSig The 256 byte signature. 
	 */
	public void setSignature(byte[] newSig) {
		signature = newSig;
	}
	public static class TpmIntegrityReportException extends Exception {
		private static final long serialVersionUID = 0;
		public TpmIntegrityReportException(String msg) {
			super(msg);
		}
	}
	private int countbits(byte[] mask){
		int count = 0;
		int pcrnumber = 0;
		//count the '1' bits in each byte
		for(int i = 0; i < mask.length; i++){
			//count the '1' bits in each byte
			int bitMask = 0x000100;
			for(int j = 0; j < 8; j++){
				bitMask = bitMask/2;
				if ((bitMask & mask[i])==bitMask){
					count++;
					//System.out.println("PCR" + pcrnumber + " is set"); //for testing
				}
				pcrnumber++;
			}
		}
		return count;
	}
	/**
	 * Check the integrity report for both consistency and validity. The Quote is reconstructed using the PCR 
	 * values and nonce that should have been used by the TPM to generate the original quote. Also, validate 
	 * the signature using the AIK certificate of the TPM that generated the report. If either the consistency 
	 * or validity fails, <b>false</b> is returned.
	 * 
	 * @param aik The AIK public key from the AIK certificate.
	 * @param nonce The nonce passed to the TPM.
	 * @param pcrBitMask The PRC bitmask that defines the registers requested.
	 * @return <b>True</b> if both consistency and validity tests pass, <b>false</b> otherwise.
	 * @throws TpmIntegrityReportException Thrown if the bitmask is not 3 bytes long.
	 * @throws TpmUtils.TpmUnsignedConversionException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 * @throws SignatureException
	 */
	public boolean checkSignature(RSAPublicKey aik, byte[] nonce, byte[] pcrBitMask, boolean printResults) 
			throws TpmIntegrityReportException, 
				TpmUtils.TpmUnsignedConversionException, 
				NoSuchAlgorithmException, 
				InvalidKeyException, 
				SignatureException {
		//check to see that the PCRs match the hash in the quote
		PCRlist.trimToSize();
		//System.out.println(countbits(pcrBitMask));
		int bitcount = countbits(pcrBitMask);
		byte[] PCRlongList = new byte[20 * bitcount];
		//byte[] PCRlongList = new byte[20 * PCRlist.size()]; //TODO: should we check to see that the number of PCRs requested (per the mask) matches those provided via the PCRlist?
		// add PCRs to the long list
		int count = 0;
		/*while (!PCRlist.isEmpty()) {
			PCR tempPCR = (PCR)PCRlist.remove(0);
			System.arraycopy(tempPCR.getBytes(), 0, PCRlongList, (20 * count), 20);
			count++;
		}*/
		for (int i = 0; i < bitcount; i++) {
			PCR tempPCR = (PCR)PCRlist.remove(0);
			System.arraycopy(tempPCR.getBytes(), 0, PCRlongList, (20 * count), 20);
			count++;
		}
		//if (pcrBitMask.length != 3) throw new TpmIntegrityReportException("PCR bit mask must be 24 bits (3 byte) long.");
		short bitMaskLength = (short)pcrBitMask.length;
		byte[] bitMaskLengthBytes = TpmUtils.shortToByteArray(bitMaskLength);
		byte[] pcrSelect = new byte[bitMaskLengthBytes.length + pcrBitMask.length]; //should be 5
		System.arraycopy(bitMaskLengthBytes, 0, pcrSelect, 0, bitMaskLengthBytes.length);
		System.arraycopy(pcrBitMask, 0, pcrSelect, bitMaskLengthBytes.length, pcrBitMask.length);
		byte[] listSize = TpmUtils.intToByteArray(PCRlongList.length);
		byte[] pcrComposite = new byte[pcrSelect.length + listSize.length + PCRlongList.length];
		System.arraycopy(pcrSelect, 0, pcrComposite, 0, pcrSelect.length);
		System.arraycopy(listSize, 0, pcrComposite, pcrSelect.length, listSize.length);
		System.arraycopy(PCRlongList, 0, pcrComposite, pcrSelect.length + listSize.length, PCRlongList.length);
		// perform hash
		MessageDigest md = MessageDigest.getInstance("SHA1");
		md.update(pcrComposite);
		byte [] newQuote = md.digest();
		// compare
		boolean pcrsMatch = true;
		for (int i = 0; i < 20; i++) {
			if (newQuote[i] != quote[i+8]) pcrsMatch = false;
		}
		//compare nonces
		boolean noncesMatch = true;
		for (int i = 0; i < 20; i++) {
			if (nonce[i] != quote[i+28]) noncesMatch = false;
		}
		//check to see that the quote is verified by the signature
		Signature sig = Signature.getInstance("SHA1withRSA");
		sig.initVerify(aik);
		sig.update(quote);
		boolean sigCheck = sig.verify(signature);
		//boolean printResults = true;
		if(printResults){
			if(pcrsMatch)
				System.out.println("PCR Composite reconstruction: PASS");
			else{
				System.out.println("PCR Composite reconstruction: FAIL");
				byte [] receivedQuote = new byte [20];
				System.arraycopy(quote, 8, receivedQuote, 0, 20);
				System.out.println(" Received: " + TpmUtils.byteArrayToHexString(receivedQuote) + "; Expected: " + TpmUtils.byteArrayToHexString(newQuote));
			}
			if(noncesMatch)
				System.out.println("Nonce check: PASS");
			else
				System.out.println("Nonce check: FAIL");
			if(sigCheck)
				System.out.println("Signature check: PASS");
			else
				System.out.println("Signature check: FAIL");
		}
		return (pcrsMatch && noncesMatch && sigCheck);
	}
	/**
	 * This holds a single PCR value.
	 * @author schawki
	 *
	 */
	public static class PCR {
		private byte[] value;
		public PCR(byte[] newValue) {
			value = newValue;
		}
		public byte[] getBytes() {
			return value;
		}
		public void setBytes(byte[] newValue) {
			value = newValue;
		}
	}
}
