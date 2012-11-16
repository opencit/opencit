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

import gov.niarl.sal.webservices.hisWebService.client.*;
import gov.niarl.sal.webservices.hisWebServices.clientWsImport.*;

import java.io.*;
import java.security.cert.X509Certificate;
import java.util.Properties;

/**
 * @deprecated
 * <p>This class is to be used for registration of the Privacy CA to the HIS appraiser.</p>
 * <p>The HisPrivacyCAWebServices2 auto-enrolls itself. This can still be used with the first-generation Privacy CA.</p>
 * Properties file named "HISprovisioner.properties" is expected with the following values:
 * <ul>
 * <li><b>HisRegistrationUrl</b> The complete URL to the appraiser's web service.</li>
 * <li><b>TrustStore</b> The path and file name of the trust store, often named "TrustStore.jks."</li>
 * <li><b>PrivacyCaCertFile</b> The path to and file name of the Privacy CA's certificate, in X.509 form. This is usually a ".cer" or ".crt" file.</li>
 * </ul>
 * @author schawki
 *
 */
public class RegisterPrivacyCa {
	/**
	 * Entry point into the program.
	 */
	public static void main(String[] args) {
		final String HIS_REGISTRATION_URL = "HisRegistrationUrl";
		final String TRUST_STORE = "TrustStore";
		final String PRIVACY_CA_CERT = "PrivacyCaCertFile";
		
		String HisRegistrationUrl = "";
		String TrustStore = "";
		String PrivacyCaCertFile = "";
		
		String propertiesFileName = "./HISprovisioner.properties";
		
		FileInputStream PropertyFile = null;
		try {
			PropertyFile = new FileInputStream(propertiesFileName);
			Properties HisProvisionerProperties = new Properties();
			HisProvisionerProperties.load(PropertyFile);
			
			HisRegistrationUrl = HisProvisionerProperties.getProperty(HIS_REGISTRATION_URL, "");
			TrustStore = HisProvisionerProperties.getProperty(TRUST_STORE, "TrustStore.jks");
			PrivacyCaCertFile = HisProvisionerProperties.getProperty(PRIVACY_CA_CERT, "");
		} catch (FileNotFoundException e) {
			System.out.println("Error finding HIS Provisioner properties file (HISprovisionier.properties)");
		} catch (IOException e) {
			System.out.println("Error loading HIS Provisioner properties file (HISprovisionier.properties)");
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
		if(HisRegistrationUrl.length() == 0){
			errorString += " - \"HisRegistrationUrl\" value must be the URL of the HIS registrtion web service\n";
			hasErrors = true;
		}
		if(TrustStore.length() == 0){
			errorString += " - \"TrustStore\" value must be the name of the trust store for using the registration web service\n";
			hasErrors = true;
		}
		if(PrivacyCaCertFile.length() == 0){
			errorString += " - \"PrivacyCaCertFile\" value must be the name of the Privacy CA certificate file\n";
			hasErrors = true;
		}
		if(hasErrors){
			System.out.println(errorString);
			System.exit(99);
			return;
		}
		System.setProperty("javax.net.ssl.trustStore", "./" + TrustStore);
		
		System.out.print("Registering Privacy CA with server...");
		try{
			X509Certificate privCaCert = TpmUtils.certFromFile(PrivacyCaCertFile);
			
			HisEnrollmentWebService hisEnrollmentWebService = HisWebServicesClientInvoker.getHisEnrollmentWebService(HisRegistrationUrl);
			hisEnrollmentWebService.enrollHisMachine("_PrivacyCA", TpmUtils.PEMencodeCert(privCaCert));
		} catch (Exception e){
			System.out.println(e.toString());
			System.exit(1);
		}
		
		System.out.println("DONE");
		System.exit(0);
		return;
	}
}
