/*
 * 2012, U.S. Government, National Security Agency, National Information Assurance Research Laboratory
 *
 * This is a work of the UNITED STATES GOVERNMENT and is not subject to copyright protection in the United States. Foreign copyrights may apply.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * ?Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * ?Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * ?Neither the name of the NATIONAL SECURITY AGENCY/NATIONAL INFORMATION ASSURANCE RESEARCH LABORATORY nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package gov.niarl.his.privacyca;

import com.intel.mtwilson.util.ResourceFinder;
import java.io.*;
import java.security.cert.X509Certificate;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @deprecated
 *
 * This method will create all new files for a HIS deployment.
 *
 * The setup of the HisPrivacyCAWebServices2 Privacy CA replaces the functionality
 * of this class. It currently does this by <i>using</i> this class.
 *
 * @author schawki
 *
 */
public class HisSetup {

    /**
     * @param args
     */
    public static void main(String[] args) {
        /*
         * File needed to run: setup.properties
         * Files needed as output:
         *  - endorsement p12
         *  - Privacy CA p12
         *  - Privacy CA certificate
         *  - Privacy CA properties
         *  - HIS provisioner properties
         *
         *  Additional items needed (external):
         *  - trust store jks for web apps
         */

        // Read the properties file
		/*
         * PrivacyCaSubjectName = HIS_Privacy_CA
         * PrivacyCaFileName = PrivCA.p12
         * PrivacyCaPassword = replace
         * EndorsementCaSubjectame = Endorsement_CA_Rev_1
         * EndorsementCaFileName = EndorseCA.p12
         * EndorsementCaPassword = replace
         * HisRegistrationUrl = https://replace
         * PrivacyCaUrl = https://replace
         * CertValidityDays = 3652
         * PrivacyCaCertFileName = PricCa.cer
         * FileLocation = ./HIS_Setup
         */
        FileOutputStream fos = null;
        try {
            System.out.println("Reading properties file...");
            final String PRIVACY_CA_SUBJECT_NAME = "PrivacyCaSubjectName";
            //final String PRIVACY_CA_FILE_NAME = "PrivacyCaFileName";
            final String PRIVACY_CA_PASSWORD = "PrivacyCaPassword";
            final String ENDORSEMENT_CA_SUBJECT_NAME = "EndorsementCaSubjectName";
            //final String ENDORSEMENT_CA_FILE_NAME = "EndorsementCaFileName";
            final String ENDORSEMENT_CA_PASSWORD = "EndorsementCaPassword";
            final String HIS_REGISTRATION_URL = "HisRegistrationUrl";
            final String PRIVACY_CA_URL = "PrivacyCaUrl";
            final String CERT_VALIDITY_DAYS = "CertValidityDays";
            //final String PRIVACY_CA_CERTIFICATE_FILE_NAME = "PrivacyCaCertFileName";
            final String FILE_LOCATION = "FileLocation";
            final String CLIENT_PATH = "ClientPath";
            final String AIK_AUTH = "AikAuth";

            FileInputStream PropertyFile = null;
            String PrivacyCaSubjectName = "null";
            String PrivacyCaFileName = "PrivacyCA.p12";
            String PrivacyCaPassword = "null";
            String EndorsementCaSubjectName = "null";
            String EndorsementCaFileName = "endorsement.p12";
            String EndorsementCaPassword = "null";
            String HisRegistrationUrl = "null";
            String PrivacyCaUrl = "null";
            String CertValidityDays = "null";
            String PrivacyCaCertFileName = "PrivacyCA.cer";
            String EndorsementCaCertFileName = "EndorsementCA.cer";
            String fileLocation = "null";
            int ValidityDays;
            String clientPath = "";
            String AikAuth = "";

//			String tomcatPath = System.getProperty("catalina.base");
//			String tempPath = "";
//			if (tomcatPath != null){
//				tempPath = tomcatPath + "/webapps/HisPrivacyCAWebServices2/";
//			}

            try {
                PropertyFile = new FileInputStream(ResourceFinder.getFile("privacyca-client.properties"));
                Properties SetupProperties = new Properties();
                SetupProperties.load(PropertyFile);
                PrivacyCaSubjectName = SetupProperties.getProperty(PRIVACY_CA_SUBJECT_NAME, "null");
                //PrivacyCaFileName = SetupProperties.getProperty(PRIVACY_CA_FILE_NAME, "null");
                PrivacyCaPassword = SetupProperties.getProperty(PRIVACY_CA_PASSWORD, "null");
                EndorsementCaSubjectName = SetupProperties.getProperty(ENDORSEMENT_CA_SUBJECT_NAME, "null");
                //EndorsementCaFileName = SetupProperties.getProperty(ENDORSEMENT_CA_FILE_NAME, "null");
                EndorsementCaPassword = SetupProperties.getProperty(ENDORSEMENT_CA_PASSWORD, "null");
                //HisRegistrationUrl = SetupProperties.getProperty(HIS_REGISTRATION_URL, "null");
                PrivacyCaUrl = SetupProperties.getProperty(PRIVACY_CA_URL, "null");
                CertValidityDays = SetupProperties.getProperty(CERT_VALIDITY_DAYS, "null");
                //PrivacyCaCertFileName = SetupProperties.getProperty(PRIVACY_CA_CERTIFICATE_FILE_NAME, "null");
                String setUpFile = ResourceFinder.getFile("privacyca-client.properties").getAbsolutePath();
                fileLocation = setUpFile.substring(0, setUpFile.indexOf("privacyca-client.properties"));

                Logger.getLogger(HisSetup.class.getName()).info("Using File Location Home " + fileLocation);
                //FileLocation = SetupProperties.getProperty(FILE_LOCATION, "null");
                clientPath = SetupProperties.getProperty(CLIENT_PATH, "clientfiles");
                checkAndCreateDirectory(fileLocation, clientPath);

                AikAuth = SetupProperties.getProperty(AIK_AUTH, "1111111111111111111111111111111111111111");
            } catch (FileNotFoundException e) {
                System.out.println("Error finding setup.properties file. Setup cannot continue without the information in this file.");
                return;
            } catch (IOException e) {
                System.out.println("Error loading setup.properties file. Setup cannot continue without the information in this file.");
                return;
            } finally {
                if (PropertyFile != null) {
                    try {
                        PropertyFile.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            // Populate some strings if running from Tomcat
//			if (tomcatPath != null){
            // Look for TrustStore.jks in tomcatPath + "/Certificate"
//				KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
//				FileInputStream fis = new FileInputStream(ResourceFinder.getFile("TrustStore.jks"));
//
//				try {
//					ks.load(fis, null);
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//				finally{
//					if (fis != null){
//						fis.close();
//					}
//				}
//
//				Enumeration<String> certList = ks.aliases();
//				if (certList.hasMoreElements()){
//					X509Certificate test = (X509Certificate)ks.getCertificate(certList.nextElement());
//					String certDN = test.getSubjectX500Principal().getName("RFC1779");
//
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
//					// If there, it can be copied later, but now should be used to extract the url!
//					PrivacyCaUrl = "https://" + "hostname" + ":8181/HisPrivacyCAWebServices2";
////					HisRegistrationUrl = "https://" + certCN + ":8443/HisWebServices";
//				}

//			}

            //PrivacyCaUrl = "https://" + "privacyca.service.com" + ":8181/HisPrivacyCAWebServices2";


            // Continue processing properties file
            if (PrivacyCaSubjectName.equals("null")) {
                System.out.println("Error finding element \"PrivacyCaSubjectName\" in properties file. Setup cannot continue without this information.");
                return;
            }
            /*if (PrivacyCaFileName.equals("null")){
            System.out.println("Error finding element \"PrivacyCaFileName\" in properties file. Setup cannot continue without this information.");
            return;
            }*/
            if (PrivacyCaPassword.equals("null")) {
                System.out.println("Error finding element \"PrivacyCaPassword\" in properties file. Setup cannot continue without this information.");
                return;
            }
            if (EndorsementCaSubjectName.equals("null")) {
                System.out.println("Error finding element \"EndorsementCaSubjectName\" in properties file. Setup cannot continue without this information.");
                return;
            }
            /*if (EndorsementCaFileName.equals("null")){
            System.out.println("Error finding element \"EndorsementCaFileName\" in properties file. Setup cannot continue without this information.");
            return;
            }*/
            if (EndorsementCaPassword.equals("null")) {
                System.out.println("Error finding element \"EndorsementCaPassword\" in properties file. Setup cannot continue without this information.");
                return;
            }
//			if (HisRegistrationUrl.equals("null")){
//				System.out.println("Error finding element \"HisRegistrationUrl\" in properties file. Setup cannot continue without this information.");
//				return;
//			}
//			if (PrivacyCaUrl.equals("null")){
//				System.out.println("Error finding element \"PrivacyCaUrl\" in properties file. Setup cannot continue without this information.");
//				return;
//			}
            if (CertValidityDays.equals("null")) {
                System.out.println("Error finding element \"CertValidityDays\" in properties file. Setup cannot continue without this information.");
                return;
            }
            /*if (PrivacyCaCertFileName.equals("null")){
            System.out.println("Error finding element \"PrivacyCaCertFileName\" in properties file. Setup cannot continue without this information.");
            return;
            }*/
//			if (FileLocation.equals("null")){
//				System.out.println("Error finding element \"FileLocation\" in properties file. Setup cannot continue without this information.");
//				return;
//			}

            //create random passwords!
            if (PrivacyCaPassword.equals("***replace***")) {
                PrivacyCaPassword = TpmUtils.byteArrayToHexString(TpmUtils.createRandomBytes(16));
            }
            if (EndorsementCaPassword.equals("***replace***")) {
                EndorsementCaPassword = TpmUtils.byteArrayToHexString(TpmUtils.createRandomBytes(16));
            }
            String ecCaPath = "";
//			if (tomcatPath != null){
//				InputStream in = null;
//				OutputStream out = null;
//				try {
//					FileLocation = tomcatPath + "/webapps/HisPrivacyCAWebServices2/";
//					clientPath = "clientfiles";
            ecCaPath = "cacerts";
            checkAndCreateDirectory(fileLocation, ecCaPath);
            //copy the TrustStore:  tomcatPath + "/Certificate/TrustStore.jks"
//					in = new FileInputStream(ResourceFinder.getFile("TrustStore.jks"));
//					out = new FileOutputStream(new File(fileLocation + clientPath + "/TrustStore.jks"));
//					byte[] buf = new byte[1024];
//					int len;
//					while ((len = in.read(buf)) > 0)
//						out.write(buf, 0, len);
//					in.close();
//					out.close();

//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//				finally{
//					if (in != null){
//						try {
//							in.close();
//						} catch (Exception e2) {
//							if (out != null)
//								out.close();
//						}
//
//					}
//					if (out != null){
//						out.close();
//					}
//				}

//			}
//                        else {
//				tomcatPath = "";
//			}

            ValidityDays = Integer.parseInt(CertValidityDays);
            System.out.println("DONE");
            // Create the p12 files (2)
			/*
             * PrivacyCaSubjectName = HIS_Privacy_CA
             * PrivacyCaFileName = PrivacyCA.p12
             * PrivacyCaPassword = replace
             * EndorsementCaSubjectName = Endorsement_CA_Rev_1
             * EndorsementCaFileName = EndorseCA.p12
             * EndorsementCaPassword = replace
             * HisRegistrationUrl = https://replace
             * PrivacyCaUrl = https://replace
             * CertValidityDays = 3652
             * PrivacyCaCertFileName = PrivCa.cer
             * FileLocation = ./HIS_Setup
             */
            System.out.print("Creating p12 files...");
            TpmUtils.createCaP12(2048, PrivacyCaSubjectName, PrivacyCaPassword, fileLocation + "/" + PrivacyCaFileName, ValidityDays);
            TpmUtils.createCaP12(2048, EndorsementCaSubjectName, EndorsementCaPassword, fileLocation + clientPath + "/" + EndorsementCaFileName, ValidityDays);
            System.out.println("DONE");
            // Create the Privacy CA certificate file
            System.out.print("Creating Privacy CA certificate...");
            X509Certificate pcaCert = TpmUtils.certFromP12(fileLocation + "/" + PrivacyCaFileName, PrivacyCaPassword);
            FileOutputStream pcaFileOut = new FileOutputStream(new File(fileLocation + clientPath + "/" + PrivacyCaCertFileName));
            try {
                if (pcaCert != null) {
                    pcaFileOut.write(pcaCert.getEncoded());
                }
                pcaFileOut.flush();
                pcaFileOut.close();

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (pcaFileOut != null) {
                    pcaFileOut.close();
                }
            }
            System.out.println("DONE");

            // Create the Endorsement CA certificate file
            System.out.print("Creating Endorsement CA certificate...");
            X509Certificate ecCert = TpmUtils.certFromP12(fileLocation + clientPath + "/" + EndorsementCaFileName, EndorsementCaPassword);
            FileOutputStream ecFileOut = new FileOutputStream(new File(fileLocation + ecCaPath + "/" + EndorsementCaCertFileName));
            try {
                if (ecCert != null) {
                    ecFileOut.write(ecCert.getEncoded());
                }
                ecFileOut.flush();
                ecFileOut.close();

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (ecFileOut != null) {
                    ecFileOut.close();
                }
            }

            System.out.println("DONE");

            // Create the other properties files (HISprovisioner and PrivacyCA)
            System.out.print("Creating properties files...");
            String PrivacyCaPropertiesFile = "PrivacyCA.properties";
            String HisProvisionerPropertiesFile = "hisprovisioner.properties";
            //String HisStandalonePropertiesFile = "OAT.properties";

            // the PrivacyCA.properties file may already exist ; if so we update it with new properties
            Properties privacyCaProperties = new Properties();
            File privacyCaPropertiesFile = ResourceFinder.getFile(PrivacyCaPropertiesFile);
            String ClientFilesDownloadUsername = "", ClientFilesDownloadPassword = "";
        try {
            FileInputStream in = new FileInputStream(privacyCaPropertiesFile);
            privacyCaProperties.load(in);
            ClientFilesDownloadUsername = privacyCaProperties.getProperty("ClientFilesDownloadUsername");
            ClientFilesDownloadPassword = privacyCaProperties.getProperty("ClientFilesDownloadPassword");
            in.close();
        }
        catch(Exception e) {
            System.err.println("Error while loading PrivacyCA.properties: "+e.getMessage());
        }

        /*
             *
             */
            fos = new FileOutputStream(fileLocation + "/" + PrivacyCaPropertiesFile);
            /*
             * #Privacy CA Operation
             * P12filename = PrivacyCA.p12
             * P12password = ***replace***
             * PrivCaCertValiditydays = 3652
             * #Privacy CA Registration
             * HisRegistrationUrl = ***replace***
             * TrustStore = TrustStore.jks
             */
            String toWrite =
                    "#Privacy CA Operation\r\n"
                    + "P12filename = " + PrivacyCaFileName + "\r\n"
                    + "P12password = " + PrivacyCaPassword + "\r\n"
                    + "PrivCaCertValiditydays = " + CertValidityDays + "\r\n"
                    + "#Privacy CA Registration\r\n" 
                    + "ClientFilesDownloadUsername = "+ClientFilesDownloadUsername+"\r\n"
                    + "ClientFilesDownloadPassword = "+ClientFilesDownloadPassword+"\r\n";// +
//				"HisRegistrationUrl = " + HisRegistrationUrl + "\r\n" +
//				"TrustStore = TrustStore.jks";
            try {
                fos.write(toWrite.getBytes("US-ASCII"));
                fos.flush();
                fos.close();

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    fos.close();
                }
            }


            /*
             * File: HISprovisioner.properties
             * Used by: HisTpmProvisioner, HisIdentityProvisioner, HisRegisterIdentity
             */
            fos = new FileOutputStream(fileLocation + clientPath + "/" + HisProvisionerPropertiesFile);
            toWrite =
                    "#TPM Provisioning Data\r\n"
                    + "TpmEndorsmentP12 = " + EndorsementCaFileName + "\r\n"
                    + "EndorsementP12Pass = " + EndorsementCaPassword + "\r\n"
                    + "EcValidityDays = " + CertValidityDays + "\r\n"
                    + "TpmOwnerAuth = 1111111111111111111111111111111111111111\r\n"
                    + "#HIS Identity Provisioning Data\r\n"
                    + "HisIdentityLabel = HIS Identity Key\r\n"
                    + "HisIdentityIndex = 1\r\n"
                    + "HisIdentityAuth = " + AikAuth + "\r\n"
                    + "PrivacyCaCertFile = " + PrivacyCaCertFileName + "\r\n"
                    + "PrivacyCaUrl = " + PrivacyCaUrl + "\r\n"
                    + //				"#HisRegistrationUrl = " + HisRegistrationUrl + "\r\n" +
                    //				"TrustStore = TrustStore.jks\r\n" +
                    "NtruBypass = true\r\n"
                    + "ClientPath = " + "cert" + "\r\n";
            try {
                fos.write(toWrite.getBytes("US-ASCII"));
                fos.flush();
                fos.close();

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    fos.close();
                }
            }





            /*
             * File: HIS.properties
             * Used by: HIS Standalone (client reporter)
             */
//			fos = new FileOutputStream(fileLocation + clientPath + "/" + HisStandalonePropertiesFile);
//			toWrite =
//				"WebServiceUrl=" + HisRegistrationUrl + "\r\n" +
//				"KeyAuth=" + AikAuth + "\r\n" +
//				"KeyIndex=1\r\n" +
//				"TpmQuoteExecutableName=NIARL_TPM_Module.exe\r\n" +
//				"SplashImage=HIS07.jpg\r\n" +
//				"TrustStore=TrustStore.jks\r\n";
//
//			try {
//				fos.write(toWrite.getBytes("US-ASCII"));
//				fos.flush();
//				fos.close();
//
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//			finally{
//				if (fos != null)
//					fos.close();
//			}


            /*
             * File: install.bat
             * Used by: not a properties file, but... assembles a batch file for the client installer
             */
//			String WinClientPath = clientPath.replace("/", "\\");
//			fos = new FileOutputStream(fileLocation + clientPath + "/install.bat");
//			toWrite =
//				"rem DO NOT EDIT THIS FILE!\r\n" +
//				"rem This file is generated by the Privacy CA installation utility in Java\r\n" +
//				"call UninstallUSW.bat\r\n" +
//				"HIS-Standalone-Setup-v3.0a.exe /VERYSILENT /SUPPRESSMSGBOXES /LOG=\"tpminstall.log\" /DIR=\"" + clientPath + "/\"\r\n" +
//				"copy /Y OAT.properties \"" + WinClientPath  + "\\HIS.properties\"\r\n" +
//				"copy /Y trustStore.jks \"" + WinClientPath + "\\\"\r\n" +
//				"copy /Y NIARL_TPM_Module.exe \"" + WinClientPath + "\\\"\r\n" +
//				"rem cd \"HIS Provisioner\" \r\n" +
//				"call provisioner.bat\r\n" +
//				"cd \"" + WinClientPath + "\\service\\\"\r\n" +
//				"call \"replaceUSW.bat\"\r\n";
//			try {
//				fos.write(toWrite.getBytes("US-ASCII"));
//				fos.flush();
//				fos.close();
//
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//			finally{
//				if (fos != null)
//					fos.close();
//			}
//
//			System.out.println("DONE");

            new Zip().zipFiles(fileLocation + clientPath + "/", fileLocation + "clientfiles.zip");


        } catch (Exception e) {
            System.out.println(e.toString());
        }

    }

    private static void checkAndCreateDirectory(String fileLocation, String clientPath) {
        File file = new File(fileLocation + clientPath);

        if (!file.exists()) {
            if (file.mkdir()) {
                Logger.getLogger(HisSetup.class.getName()).log(Level.INFO, "Folder {0} didn not exist. Created directory ", clientPath);
            }
        }

    }

    public static class Zip {

        static final int BUFFER = 2048;

        public void zipFiles(String folder, String zipFileName) throws Exception {
            try {
                BufferedInputStream origin = null;
                FileOutputStream dest = new FileOutputStream(zipFileName);
                ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
                //out.setMethod(ZipOutputStream.DEFLATED);
                byte data[] = new byte[BUFFER];
                // get a list of files from current directory
                File f = new File(folder);
                String files[] = f.list();

                for (int i = 0; i < files.length; i++) {
                    System.out.println("Adding: " + folder + files[i]);
                    FileInputStream fi = new FileInputStream(folder + files[i]);
                    origin = new BufferedInputStream(fi, BUFFER);
                    ZipEntry entry = new ZipEntry(files[i]);
                    out.putNextEntry(entry);
                    int count;
                    while ((count = origin.read(data, 0,
                            BUFFER)) != -1) {
                        out.write(data, 0, count);
                    }
                    origin.close();
                }
                out.close();
            } catch (Exception e) {
                throw new Exception("Error while Zipping Client files.");
            }
        }
    }
}
