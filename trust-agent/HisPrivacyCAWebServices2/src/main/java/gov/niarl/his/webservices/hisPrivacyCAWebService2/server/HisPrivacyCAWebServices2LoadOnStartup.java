/*
 * 2012, U.S. Government, National Security Agency, National Information Assurance Research Laboratory
 *
 * This is a work of the UNITED STATES GOVERNMENT and is not subject to copyright protection in the United States. Foreign copyrights may apply.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * ??Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * ??Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * ??Neither the name of the NATIONAL SECURITY AGENCY/NATIONAL INFORMATION ASSURANCE RESEARCH LABORATORY nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package gov.niarl.his.webservices.hisPrivacyCAWebService2.server;

import com.intel.mtwilson.util.ResourceFinder;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.StringTokenizer;

import gov.niarl.his.privacyca.HisSetup;
import gov.niarl.his.privacyca.TpmUtils;
import gov.niarl.sal.webservices.hisWebService.client.HisWebServicesClientInvoker;
import gov.niarl.sal.webservices.hisWebServices.clientWsImport.HisEnrollmentWebService;

import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

/**
 * This class allows code to be run when Tomcat first runs the HisPrivacyCAWebServices2 webapp.
 *
 * @author schawki
 *
 */
public class HisPrivacyCAWebServices2LoadOnStartup extends HttpServlet{

	private static final long serialVersionUID = 1L;
        private static String homeFolder = "/etc/intel/cloudsecurity";

        static{
            try {
                String setUpFile = ResourceFinder.getFile("privacyca-client.properties").getAbsolutePath();
                homeFolder = setUpFile.substring(0,setUpFile.indexOf("privacyca-client.properties"));

            } catch (FileNotFoundException ex) {
                Logger.getLogger(HisPrivacyCAWebService2Impl.class.getName()).log(Level.SEVERE, "Error while getting setup.properties.", ex);
            }
        }


	@Override
	/**
	 * This function will always run upon startup of the web service. The focus of the startup
	 * routine is to determine if this is the very first time the web service has run. It does
	 * this by looking for a PrivacyCA.p12 file, the holder of the private key and associated
	 * CA certificate used for signing AICs. If this file exists, nothing more is done. If the
	 * file does not exist, new files are created using the system's random number generator.
	 *
	 * The list of files generated is:<br>
	 * - endorsement p12<br>
	 * - Privacy CA p12<br>
	 * - Privacy CA certificate<br>
	 * - Privacy CA properties<br>
	 * - HIS provisioner properties<br>
	 *
	 * The trust store is copied from within Tomcat's directory structure and placed with the generated files.
	 */
	public void init() throws ServletException {
		System.out.println("HisPrivacyCAWebServices2LoadOnStartup init()");
//		String tomcatPath = System.getProperty("catalina.base") + "/webapps/HisPrivacyCAWebServices2/";
		File test = new File(homeFolder + "PrivacyCA.p12");
		if(!test.exists()){
//			FileInputStream fis = null;
//			try{
				// The functionality from the HisSetup class should be rebuilt here to avoid this call.
				// Because this function is now only run from here, the HisSetup class is now considered deprecated.
				HisSetup.main(null);
//				System.setProperty("javax.net.ssl.trustStore", System.getProperty("catalina.base") + "/Certificate/TrustStore.jks");
//				KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
//				fis = new FileInputStream(System.getProperty("catalina.base") + "/Certificate/TrustStore.jks");
//				ks.load(fis, null);
//				Enumeration<String> certList = ks.aliases();
//				if (certList.hasMoreElements()){
//					X509Certificate serverCert = (X509Certificate)ks.getCertificate(certList.nextElement());
//					String certDN = serverCert.getSubjectX500Principal().getName("RFC1779");
//					StringTokenizer st = new StringTokenizer(certDN, ",");
//					String certCN = "";
//					while(st.hasMoreElements()){
//						String line = st.nextToken();
//						if(line.startsWith("CN")){
//							certCN = line.subSequence(line.indexOf("=") + 1, line.length()).toString();
//							break;
//						}
//					}
//					while(certCN.startsWith(" ")){
//						certCN = certCN.subSequence(1, certCN.length()).toString();
//					}

// DMAGADI :This is not required as we are not using full OAT solution
//					X509Certificate privCaCert = TpmUtils.certFromFile(tomcatPath + "ClientFiles/PrivacyCA.cer");
//					HisEnrollmentWebService hisEnrollmentWebService = HisWebServicesClientInvoker.getHisEnrollmentWebService("https://" + certCN + ":8443/HisWebServices");
//					hisEnrollmentWebService.enrollHisMachine("_PrivacyCA", TpmUtils.PEMencodeCert(privCaCert));
//				}

//			}catch(Exception e){
//				System.out.println(e.toString());
//			}
//
//			finally{
//				try {
//                    if (fis != null)
//                    	fis.close();
//				} catch (IOException e) {
//					e.printStackTrace();
//     			}
//			}
		}
		super.init();
	}
}