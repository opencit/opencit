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

//import gov.niarl.sal.webservices.hisWebService.client.HisWebServicesClientInvoker;
//import gov.niarl.sal.webservices.hisWebServices.clientWsImport.HisEnrollmentWebService;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.Properties;

/**
 * <p>This is part 3 of 3 for fully provisioning HIS on a Windows client. This class will register an identity provisioned using HisIdentityProvisioner and register that identity with an appraiser.</p>
 * <p>This class utilizes a properties file. It looks for a file by the name of "HISprovisioner.properties" in the directory from which Java was invoked.</p>
 * The following values must be in the properties file:
 * <ul>
 * <li><b>HisRegistrationUrl</b> This is the entire path to the registration service.</li>
 * <li><b>TrustStore</b> This is the name of the trust store used to encrypt the web service connection. This file must be provided by the web server hosting the appraiser and placed into the path that the provisioner is run from ("./").</li>
 * <li><b>ClientPath</b> This is the path for the HIS client installation. The AIC (aic.cer) must be in this directory for registration to take place.</li>
 * </ul>

 * @author schawki
 *
 */
public class HisRegisterIdentity {

	/**
	 * Entry point for the class
	 */
    /*
	public static void main(String[] args){
		final String HIS_REGISTRATION_URL = "HisRegistrationUrl";
		final String TRUST_STORE = "TrustStore";
		final String CLIENT_PATH = "ClientPath";
		
		String HisRegistrationUrl = "";
		String TrustStore = "";
		String ClientPath = "";
		
		String propertiesFileName = "./OATprovisioner.properties";
		
		FileInputStream PropertyFile = null;
		try {
			PropertyFile = new FileInputStream(propertiesFileName);
			Properties HisProvisionerProperties = new Properties();
			HisProvisionerProperties.load(PropertyFile);
			
			HisRegistrationUrl = HisProvisionerProperties.getProperty(HIS_REGISTRATION_URL, "");
			TrustStore = HisProvisionerProperties.getProperty(TRUST_STORE, "TrustStore.jks");
			ClientPath = HisProvisionerProperties.getProperty(CLIENT_PATH, "");
		} catch (FileNotFoundException e) {
			System.out.println("Error finding HIS Provisioner properties file (HISprovisionier.properties)");
		} catch (IOException e) {
			System.out.println("Error loading HIS Provisioner properties file (HISprovisionier.properties)");
		}
		finally{
			if (PropertyFile != null)
				try {
					PropertyFile.close();
				} catch (IOException e) {
					e.printStackTrace();
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
		if(ClientPath.length() == 0){
			errorString += " - \"ClientPath\" value must be the path that will be used for installing the HIS Client\n";
			hasErrors = true;
		}
		if(hasErrors){
			System.out.println(errorString);
			System.exit(99);
			return;
		}
		System.setProperty("javax.net.ssl.trustStore", "./" + TrustStore);
		
		System.out.print("Registering identity with server...");
		try{
			X509Certificate aikCert = TpmUtils.certFromFile(ClientPath + "/aik.cer");
			
			HisEnrollmentWebService hisEnrollmentWebService = HisWebServicesClientInvoker.getHisEnrollmentWebService(HisRegistrationUrl);
			hisEnrollmentWebService.enrollHisMachine(TpmUtils.getHostname(), TpmUtils.PEMencodeCert(aikCert));
		} catch (Exception e){
			System.out.println("FAILED");
			e.printStackTrace();
			System.exit(1);
		}
		System.out.println("DONE");
		System.exit(0);
		return;
	}*/
}
