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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.security.cert.X509Certificate;
import java.util.Properties;

/**
 * <p>This is part 1 of 3 for fully provisioning HIS on a Windows client. This class does the initial provisioning of the TPM.</p>
 * This provisioning includes:
 * <ul>
 * <li>Taking ownership of the TPM</li>
 * <li>Creating an Endorsement Certificate</li>
 * <li>Storing the Endorsement Certificate in the TPM's NVRAM</li>
 * </ul>
 * 
 * <p>This class utilizes a properties file. It looks for a file by the name of "HISprovisioner.properties" in the directory from which Java was invoked.</p>
 * The following values must be in the properties file:<br>
 * <ul>
 * <li><b>TpmEndorsmentP12</b></li>
 * <li><b>EndorsementP12Pass</b></li>
 * <li><b>EcValidityDays</b></li>
 * <li><b>TpmOwnerAuth</b> This must be a 40 digit (20 byte) hex code representing the owner auth data to be assigned.</li>
 * </ul>
 * 
 * @author schawki
 * @deprecated Bug #947 this class isn't being used
 */
public class HisTpmProvisioner {

	/**
	 * Entry point into the program
	 */
	public static void main(String[] args){// throws InvalidKeyException, CertificateEncodingException, UnrecoverableKeyException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, NoSuchProviderException, KeyStoreException, CertificateException, IOException, javax.security.cert.CertificateException {
		//get properties file info
		final String EC_P12_FILE = "TpmEndorsmentP12";
		final String EC_P12_PASSWORD = "EndorsementP12Pass";
		final String EC_VALIDITY = "EcValidityDays";
		final String OWNER_AUTH = "TpmOwnerAuth";
		
		String TpmEndorsmentP12 = "";
		String EndorsementP12Pass = "";
		int EcValidityDays = 0;
		
		byte [] TpmOwnerAuth = null;

		String propertiesFileName = "./OATprovisioner.properties";

		FileInputStream PropertyFile = null;
		try {
			PropertyFile = new FileInputStream(propertiesFileName);
			Properties HisProvisionerProperties = new Properties();
			HisProvisionerProperties.load(PropertyFile);
			
			TpmEndorsmentP12 = HisProvisionerProperties.getProperty(EC_P12_FILE, "");
			EndorsementP12Pass = HisProvisionerProperties.getProperty(EC_P12_PASSWORD, "");
			EcValidityDays = Integer.parseInt(HisProvisionerProperties.getProperty(EC_VALIDITY, ""));
			TpmOwnerAuth = TpmUtils.hexStringToByteArray(HisProvisionerProperties.getProperty(OWNER_AUTH, ""));
		} catch (FileNotFoundException e) {
			System.out.println("Error finding HIS Provisioner properties file (HISprovisionier.properties)");
		} catch (IOException e) {
			System.out.println("Error loading HIS Provisioner properties file (HISprovisionier.properties)");
		}
		catch (NumberFormatException e) {
			e.printStackTrace();
		}
		finally{
			if (PropertyFile != null){
				try {
					PropertyFile.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		String errorString = "Properties file \"" + propertiesFileName + "\" contains errors:\n";
		boolean hasErrors = false;
		if(TpmEndorsmentP12.length() == 0){
			errorString += " - \"TpmEndorsmentP12\" value must be the name of a valid PKCS#12 file\n";
			hasErrors = true;
		}
		if(EndorsementP12Pass.length() == 0){
			errorString += " - \"EndorsementP12Pass\" value must be the password for the TpmEndorsementP12 file\n";
			hasErrors = true;
		}
		if(EcValidityDays == 0){
			errorString += " - \"EcValidityDays\" value must be the number of validity days for the Endorsement Credential\n";
			hasErrors = true;
		}
		if(TpmOwnerAuth ==null || TpmOwnerAuth.length != 20){
			errorString += " - \"TpmOwnerAuth\" value must be a 40 hexidecimal digit (20 byte) value representing the TPM owner auth\n";
			hasErrors = true;
		}
		if(hasErrors){
			System.out.println(errorString);
			System.exit(99);
			return;
		}
		//Provision the TPM
		System.out.print("Performing TPM provisioning...");
		try {
			X509Certificate cert = TpmUtils.certFromP12(TpmEndorsmentP12, EndorsementP12Pass);
			if (cert != null)
				TpmClient.provisionTpm(TpmOwnerAuth, TpmUtils.privKeyFromP12(TpmEndorsmentP12, EndorsementP12Pass), cert, EcValidityDays);
		}catch (TpmModule.TpmModuleException e){
			System.out.println("Caught a TPM Module exception: " + e.toString());
		}catch (Exception e){
			System.out.println("FAILED");
			e.printStackTrace();
			System.exit(1);
		}
		System.out.println("DONE");
		System.exit(0);
		return;
	}

}
