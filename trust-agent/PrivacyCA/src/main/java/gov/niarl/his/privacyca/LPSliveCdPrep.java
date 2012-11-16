/*
 * 2011, U.S. Government, National Security Agency, National Information Assurance Research Laboratory
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Properties;
import java.util.zip.CRC32;

/**
 * <p>This class is used to setup an LPS bootable CD. This should be run during the boot 
 * process. No unique data for any particular machine is stored on the LPS CD, as the CD 
 * may be used on many different machines. the HIS client requires that each machine 
 * have a unique machine name, which must be associated with a particular TPM. The client 
 * must have provisioned an AIK and registered the corresponding AIC with the HIS appraiser 
 * using the unique machine name. This is true for all HIS clients. LPS/bootable-CD HIS 
 * clients differ from traditional HIS clients in that the unique name is derived from the 
 * TPM's Endorsement Key (EK), and the AIK is stored in the TPM's NVRAM. The main function 
 * in this class is supposed to set the hostname on the system, extract the AIK from the 
 * TPM's NVRAM, and set that AIK for use by the normal HIS Standalone Client.</p>
 * 
 * <p>This class is designed to run even if the machine booting to the LPS CD has not been 
 * provisioned. However, if this is done the AIK will not be properly registered. The 
 * failure to load and register an AIK on the system will not be obvious to a user on the 
 * system. Even though the hostname could be derived without being provisioned, this 
 * program will set the hostname to "LPS-NoProvision" if the AIK could not be found. This 
 * should indicate to the user that the machine must be provisioned for HIS.</p>
 * 
 * <p>Registration can be performed by running the LPSprovisioner class.</p>
 * 
 * @author schawki
 *
 */
public class LPSliveCdPrep {
	/**
	 * Entry point into the program.
	 */
	public static void main(String[] args) {
		//TPM Provisioning Data
		final String OWNER_AUTH = "TpmOwnerAuth";

		//HIS Identity Provisioning Data
		final String HIS_IDENTITY_INDEX = "HisIdentityIndex";
		final String HIS_IDENTITY_AUTH = "HisIdentityAuth";

		//Properties variables with defaults:
		byte [] TpmOwnerAuth = TpmUtils.hexStringToByteArray("1111111111111111111111111111111111111111");
		int HisIdentityIndex = 1;
		byte [] HisIdentityAuth = TpmUtils.hexStringToByteArray("1111111111111111111111111111111111111111");

		String propertiesFileName = "./HISprovisioner.properties";

		FileInputStream PropertyFile = null;
		try {
			PropertyFile = new FileInputStream(propertiesFileName);
			Properties HisProvisionerProperties = new Properties();
			HisProvisionerProperties.load(PropertyFile);

			TpmOwnerAuth = TpmUtils.hexStringToByteArray(HisProvisionerProperties.getProperty(OWNER_AUTH, "1111111111111111111111111111111111111111"));
			HisIdentityIndex = Integer.parseInt(HisProvisionerProperties.getProperty(HIS_IDENTITY_INDEX, "1"));
			HisIdentityAuth = TpmUtils.hexStringToByteArray(HisProvisionerProperties.getProperty(HIS_IDENTITY_AUTH, "1111111111111111111111111111111111111111"));
		} catch (FileNotFoundException e) {
			System.out.println("Error finding HIS Provisioner properties file (HISprovisionier.properties); using defaults.");
		} catch (IOException e) {
			System.out.println("Error loading HIS Provisioner properties file (HISprovisionier.properties); using defaults.");
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

		try{
			// pull the AIK key blob from the TPM's NVRAM
			byte[] aikRawBlob = null;
			try {
				aikRawBlob = TpmModule.getCredential(TpmOwnerAuth, "PCC");
			}catch(Exception g){
				g.printStackTrace();
				//do nothing -- This is run on every boot of the LPS CD, including the first boot -- where there should be no AIK stored in the TPM anyway
			}
			
			String hostname = "";
			if (aikRawBlob != null){
				// set the AIK using the blob that should be stored in the TPM's NVRAM
				byte[] aikBlob = new byte[aikRawBlob.length - 4];
				System.arraycopy(aikRawBlob, 4, aikBlob, 0, aikBlob.length);
				try{
					TpmModule.clearKey("identity", HisIdentityAuth, HisIdentityIndex);
				}catch(Exception f){
					f.printStackTrace();
					//do nothing -- it may be that there is no key there to clear! (this is hopefully the case)
				}
				TpmModule.setKey("identity", HisIdentityAuth, aikBlob, HisIdentityIndex);
				
				//re-create the hostname in a standard way, by concatenating "LPS-" with the CRC32 of the public Endorsement Key
				CRC32 crc = new CRC32();
				crc.update(TpmModule.getEndorsementKeyModulus(TpmOwnerAuth, TpmOwnerAuth));
				hostname = "LPS-" + Long.toHexString(crc.getValue()).toUpperCase();
			} else {
				hostname = "LPS-NoProvision";
			}
			System.out.print(hostname); // dump to screen
			
			//set the hostname on the system
			Runtime.getRuntime().exec("hostname " + hostname);
			//place the hostname in the local hosts file, associated with the loopback adapter
			File hostfile = new File("/etc/hosts");
			FileOutputStream hostfileStream = new FileOutputStream(hostfile);
			try {
				OutputStreamWriter hfile = new OutputStreamWriter(hostfileStream);
				hfile.append("127.0.0.1\tlocalhost " + hostname + "\n");
				hfile.flush();
				hfile.close();
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			finally{
				if (hostfileStream != null)
					hostfileStream.close();
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
