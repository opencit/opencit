/*
 * 2012, U.S. Government, National Security Agency, National Information Assurance Research Laboratory
 * 
 * This is a work of the UNITED STATES GOVERNMENT and is not subject to copyright protection in the United States. Foreign copyrights may apply.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * 锟�Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer. 
 * 锟�Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution. 
 * 锟�Neither the name of the NATIONAL SECURITY AGENCY/NATIONAL INFORMATION ASSURANCE RESEARCH LABORATORY nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package gov.niarl.his.privacyca;

import com.intel.mtwilson.MyFilesystem;
import gov.niarl.his.privacyca.TpmUtils.TpmBytestreamResouceException;
import gov.niarl.his.privacyca.TpmUtils.TpmUnsignedConversionException;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;
import java.security.cert.*;

//import com.intel.mountwilson.as.common.ResourceFinder;
import com.intel.mtwilson.util.ResourceFinder;

/**
 * <p>The TpmModule class is a Java front end for C++ utilities that work directly with the TSS for interfacing with the TPM.</p>
 * 
 * @author schawki
 *
 */

//TODO: 
//TODO: 
//TODO: Add Javadoc for the public functions
//TODO: This is probably the most usable class of everything in this package, so this is more important than normal
//TODO: Make sure to describe the use of the properties file
//TODO: 
//TODO: 
public class TpmModule {
	/**
	 * 
	 * @author schawki
	 *
	 */
	public static class TpmModuleException extends Exception {
		private static final long serialVersionUID = 0;
		/**
		 * 
		 * @param msg
		 */
		public TpmModuleException(String msg) {
			super(msg);
		}
	}
	/**
	 * 
	 * @author schawki
	 *
	 */
	private static class commandLineResult {
		private int returnCode = 0;
		private String [] results = null;
		/**
		 * 
		 * @param newReturnCode
		 * @param numResults
		 */
		public commandLineResult(int newReturnCode, int numResults){
			returnCode = newReturnCode;
			results = new String[numResults];
		}
		/**
		 * 
		 * @return
		 */
		public int getReturnCode(){
			return returnCode;
		}
		/**
		 * 
		 * @param index
		 * @param result
		 * @throws IllegalArgumentException
		 */
		public void setResult(int index, String result)
				throws IllegalArgumentException {
			if (index + 1 > results.length)
				throw new IllegalArgumentException("Array index out of bounds.");
			results[index] = result;
		}
		/**
		 * 
		 * @return
		 */
		public int getResultCount(){
			return results.length;
		}
		/**
		 * 
		 * @param index
		 * @return
		 * @throws IllegalArgumentException
		 */
		public String getResult(int index)
				throws IllegalArgumentException {
			if (index + 1 > results.length)
				throw new IllegalArgumentException("Array index out of bounds.");
			return results[index];
		}
		
	}
	/**
	 * 
	 * @param mode
	 * @param args
	 * @param returnCount
	 * @param useTrousersMode
	 * @return
	 * @throws IOException
	 */
	private static commandLineResult executeVer2Command(int mode, String args, int returnCount, boolean useTrousersMode)
			throws IOException {
        
		int returnCode = 0;
//		final String new_TPM_MODULE_EXE_PATH = "TpmModuleExePath";
//		final String new_EXE_NAME = "ExeName";
		final String new_TROUSERS_MODE = "TrousersMode";
		final String DEBUG_MODE = "DebugMode";
		FileInputStream PropertyFile = null;
		final String newTpmModuleExePath = MyFilesystem.getApplicationFilesystem().getBootstrapFilesystem().getBinPath(); // "./exe";
		final String newExeName = "NIARL_TPM_Module";
		String newTrousersMode = "False";
		String debugMode = "False";
		String propertiesFileName = ResourceFinder.getLocation("TPMModule.properties");
		Logger.getAnonymousLogger().info("Reading properties file" + propertiesFileName);
		try {
			PropertyFile = new FileInputStream(ResourceFinder.getFile("TPMModule.properties"));
			Properties TpmModuleProperties = new Properties();
			TpmModuleProperties.load(PropertyFile);
//			newTpmModuleExePath = TpmModuleProperties.getProperty(new_TPM_MODULE_EXE_PATH, ".\\exe");
//			newExeName = TpmModuleProperties.getProperty(new_EXE_NAME, "NIARL_TPM_Module");
			newTrousersMode = TpmModuleProperties.getProperty(new_TROUSERS_MODE, "False");
			debugMode = TpmModuleProperties.getProperty(DEBUG_MODE, "False");
		} catch (FileNotFoundException e) {
			System.out.println("Error finding TPM Module properties file; using defaults.");
		} catch (IOException e) {
			System.out.println("Error loading TPM Module properties file; using defaults.");
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
		boolean TrousersMode = false;
		if (newTrousersMode.toLowerCase().equals("true"))
			TrousersMode = true;
		boolean DebugMode = false;
		if (debugMode.toLowerCase().equals("true"))
			DebugMode = true;
		String commandLine = newTpmModuleExePath + File.separator + newExeName + " -mode " + mode + " " + args;
		if (TrousersMode && useTrousersMode)
			commandLine += " -trousers";
		if (DebugMode) System.out.println("\"" + commandLine + "\"");
		Process p = Runtime.getRuntime().exec(commandLine);
		String line = "";
		if (returnCount != 0){
			BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String newLine = "";
			try {
				while ((newLine = input.readLine()) != null) {
					line = newLine;
				}
				input.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			finally{
				if (input != null)
					input.close();
			}
			
		}
		if (DebugMode) System.out.println("\"" + line + "\"");
		
		//do a loop to wait for an exit value
		
		boolean isRunning;
		int timeout = 10000; //300000; //any need to add this to the properties file?
		int countToTimeout = 0;
		do {
			countToTimeout++;
			isRunning = false;
			try {
				returnCode = p.exitValue();
			}catch(IllegalThreadStateException e1) {
				isRunning = true;
				try {
					Thread.sleep(1);
					//if (countToTimeout%5000 == 0) System.out.print(".");
				} catch (InterruptedException e2) {
					isRunning = false;
				}
			}
		} while(isRunning && (countToTimeout < timeout));
		if (countToTimeout == timeout){
			System.out.println("Timeout reached");
			p.destroy();
		}
		
		returnCode = p.exitValue();
		commandLineResult toReturn = new commandLineResult(returnCode, returnCount);
		if ((returnCode == 0)&&(returnCount != 0)) {
			StringTokenizer st = new StringTokenizer(line);
			for (int i = 0; i < returnCount; i++) {
				toReturn.setResult(i, st.nextToken());
			}
		}
		return toReturn;
	}
	/**
	 * 
	 * @param ownerAuth
	 * @param nonce
	 * @throws IOException
	 * @throws TpmModuleException
	 */
	public static void takeOwnership(byte [] ownerAuth, byte [] nonce)
			throws IOException, 
			TpmModuleException {
		/*
		 * Take Ownership
		 * NIARL_TPM_Module -mode 1 -owner_auth <40 char hex blob> -nonce <40 char hex blob>
		 * return: no return ***
		 */
		String argument = "-owner_auth " + TpmUtils.byteArrayToHexString(ownerAuth) + " -nonce " + TpmUtils.byteArrayToHexString(nonce);
		commandLineResult result = executeVer2Command(1, argument, 0, false);
		if (result.getReturnCode() != 0) throw new TpmModuleException("TpmModule.takeOwnership returned nonzero error: " + result.getReturnCode() + "(" + ")");
		return;
	}
	/**
	 * 
	 * @param ownerAuth
	 * @throws IOException
	 * @throws TpmModuleException
	 */
	public static void clearOwnership(byte [] ownerAuth)
			throws IOException, 
			TpmModuleException {
		/*
		 * Clear Ownership
		 * NIARL_TPM_Module -mode 2 -owner_auth <40 char hex blob>
		 * return: no return ***
		 */
		String argument = "-owner_auth " + TpmUtils.byteArrayToHexString(ownerAuth);
		commandLineResult result = executeVer2Command(2, argument, 0, false);
		if (result.getReturnCode() != 0) throw new TpmModuleException("TpmModule.clearOwnership returned nonzero error: " + result.getReturnCode() + "(" + ")");
		return;
	}
	/**
	 * 
	 * @param ownerAuth
	 * @param keyAuth
	 * @param keyLabel
	 * @param pcaPubKeyBlob
	 * @param keyIndex
	 * @param endorsmentCredential
	 * @param useECinNvram
	 * @return
	 * @throws IOException
	 * @throws TpmModuleException
	 * @throws CertificateEncodingException
	 */
	public static TpmIdentity collateIdentityRequest(byte [] ownerAuth, byte [] keyAuth, String keyLabel, byte [] pcaPubKeyBlob, int keyIndex, X509Certificate endorsmentCredential, boolean useECinNvram)
			throws IOException, 
			TpmModuleException, 
			CertificateEncodingException {
		/*
		 * Collate Identity Request
		 * NIARL_TPM_Module -mode 3 -owner_auth <40 char hex blob> -key_auth <40 char hex blob> -key_label <hex string in ASCII> -pcak <public key blob for Privacy CA> -key_index <integer index> [-ec_blob <hex blob of endorsement credential> -ec_nvram -trousers]
		 * return: <identity request> <aik modulus> <aik complete key blob>
		 */
		String argument = "-owner_auth " + TpmUtils.byteArrayToHexString(ownerAuth)
						+ " -key_auth " + TpmUtils.byteArrayToHexString(keyAuth) 
						+ " -key_label " + TpmUtils.byteArrayToHexString(keyLabel.getBytes()) 
						+ " -pcak " + TpmUtils.byteArrayToHexString(pcaPubKeyBlob) 
						+ " -key_index " + keyIndex;
		if (endorsmentCredential != null)
			argument += " -ec_blob " + TpmUtils.byteArrayToHexString(endorsmentCredential.getEncoded());
		if (useECinNvram)
			argument += " -ec_nvram";
		// TROUSERS MODE OPTIONAL
		commandLineResult result = executeVer2Command(3, argument, 3, true);
		if (result.getReturnCode() != 0) throw new TpmModuleException("TpmModule.collateIdentityRequest returned nonzero error: " + result.getReturnCode() + "(" + ")");
		byte [] identityRequest = TpmUtils.hexStringToByteArray(result.getResult(0));
		byte [] aikModulus = TpmUtils.hexStringToByteArray(result.getResult(1));
		byte [] aikKeyBlob = TpmUtils.hexStringToByteArray(result.getResult(2));
		TpmIdentity toReturn = new TpmIdentity(identityRequest, aikModulus, aikKeyBlob);
		return toReturn;
	}
	/**
	 * 
	 * @param ownerAuth
	 * @param keyAuth
	 * @param asymCaContents
	 * @param symCaAttestation
	 * @param keyIndex
	 * @return
	 * @throws IOException
	 * @throws TpmModuleException
	 */
	public static byte [] activateIdentity(byte [] ownerAuth, byte [] keyAuth, byte [] asymCaContents, byte [] symCaAttestation, int keyIndex)
			throws IOException, 
			TpmModuleException {
		/*
		 * Activate Identity
		 * NIARL_TPM_Module -mode 4 -owner_auth <40 char hex blob> -key_auth <40 char hex blob> -asym <> -sym <> -key_index <integer index>
		 * return: <aik certificate>
		 */
		String argument = "-owner_auth " + TpmUtils.byteArrayToHexString(ownerAuth) 
						+ " -key_auth " + TpmUtils.byteArrayToHexString(keyAuth) 
						+ " -asym " + TpmUtils.byteArrayToHexString(asymCaContents) 
						+ " -sym " + TpmUtils.byteArrayToHexString(symCaAttestation) 
						+ " -key_index " + keyIndex;
		commandLineResult result = executeVer2Command(4, argument, 1, false);
		if (result.getReturnCode() != 0) throw new TpmModuleException("TpmModule.activateIdentity returned nonzero error: " + result.getReturnCode() + "(" + ")");
		byte [] identityCredential = TpmUtils.hexStringToByteArray(result.getResult(0));
		return identityCredential;
	}
	/**
	 * Added by DMAGADI 
	 * 
	 * @param ownerAuth
	 * @param keyAuth
	 * @param asymCaContents
	 * @param symCaAttestation
	 * @param keyIndex
	 * @return
	 * @throws IOException
	 * @throws TpmModuleException
	 */
	public static HashMap<String,byte[]> activateIdentity2(byte [] ownerAuth, byte [] keyAuth, byte [] asymCaContents, byte [] symCaAttestation, int keyIndex)
			throws IOException, 
			TpmModuleException {
		/*
		 * Activate Identity
		 * NIARL_TPM_Module -mode 4 -owner_auth <40 char hex blob> -key_auth <40 char hex blob> -asym <> -sym <> -key_index <integer index>
		 * return: <aik certificate>
		 */
		String argument = "-owner_auth " + TpmUtils.byteArrayToHexString(ownerAuth) 
						+ " -key_auth " + TpmUtils.byteArrayToHexString(keyAuth) 
						+ " -asym " + TpmUtils.byteArrayToHexString(asymCaContents) 
						+ " -sym " + TpmUtils.byteArrayToHexString(symCaAttestation) 
						+ " -key_index " + keyIndex;
		commandLineResult result = executeVer2Command(4, argument, 2, false);
		if (result.getReturnCode() != 0) throw new TpmModuleException("TpmModule.activateIdentity returned nonzero error: " + result.getReturnCode() + "(" + ")");
		HashMap<String,byte[]> results = new HashMap<String, byte[]>(); 
		results.put("aikcert", TpmUtils.hexStringToByteArray(result.getResult(0)));
		results.put("aikblob", TpmUtils.hexStringToByteArray(result.getResult(1)));
		return results;
	}

	
	
	
	
	/**
	 * 
	 * @param keyAuth
	 * @param nonce
	 * @param mask
	 * @param keyIndex
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IOException
	 * @throws TpmModuleException
	 */
	public static TpmIntegrityReport quote(byte [] keyAuth, byte [] nonce, byte [] mask, int keyIndex)
			throws IllegalArgumentException,
			IOException, 
			TpmModuleException {
		/*
		 * Quote
		 * NIARL_TPM_Module -mode 5 -key_auth <40 char hex blob> -nonce <40 char hex blob> -mask <> -key_index <integer index>
		 * return: <quote> <signature>
		 */
		//check the mask
		if ((mask.length > 3)||(mask.length == 0)) {
			throw new IllegalArgumentException ("TpmModule.quote: Mask must be between 1 and 3 bytes in length.");
		}
		int pcrCount = 0;
		for (int i = 0; i < mask.length; i++) {
			for (int j = 0; j < 8; j++) {
				if ((0x80>>j&mask[i])==(0x80>>j))
					pcrCount++; //count the bits!
			}
		}
		String argument = "-key_auth " + TpmUtils.byteArrayToHexString(keyAuth) 
						+ " -nonce " + TpmUtils.byteArrayToHexString(nonce) 
						+ " -mask " + TpmUtils.byteArrayToHexString(mask) 
						+ " -key_index " + keyIndex;
		commandLineResult result = executeVer2Command(5, argument, pcrCount + 3, false);
		// command line app should return <pcrCount> pcrs, (1 nonce,) 1 quote, and 1 signature
		if (result.getReturnCode() != 0) throw new TpmModuleException("TpmModule.quote returned nonzero error: " + result.getReturnCode() + "(" + ")");
		if (result.getResultCount() != pcrCount + 3) throw new TpmModuleException("TpmModule.quote returned the wrong number of results.");
		TpmIntegrityReport toReturn = new TpmIntegrityReport();
		for (int i = 0; i < pcrCount; i++)
			toReturn.addPCR(TpmUtils.hexStringToByteArray(result.getResult(i)));
		toReturn.setQuote(TpmUtils.hexStringToByteArray(result.getResult(pcrCount + 1)));
		//skip getting the nonce
		toReturn.setSignature(TpmUtils.hexStringToByteArray(result.getResult(pcrCount + 2)));
		return toReturn;
	}
	/**
	 * 
	 * @param ownerAuth
	 * @param resetData
	 * @param nonce
	 * @param keyIndex
	 * @return
	 * @throws IOException
	 * @throws TpmModuleException
	 */
	public static byte [] createRevocableEndorsementKey(byte [] ownerAuth, byte [] resetData, byte [] nonce, int keyIndex)
			throws IOException, 
			TpmModuleException {
		/*
		 * Create Revocable Endorsement Key
		 * NIARL_TPM_Module -mode 6 -owner_auth <40 char hex blob> -reset <> -nonce <40 char hex blob> -key_index <integer index>
		 * return: <? validation data>
		 * 
		 * What is the key index for??
		 */
		String argument = "-owner_auth " + TpmUtils.byteArrayToHexString(ownerAuth) 
						+ " -reset " + TpmUtils.byteArrayToHexString(resetData) 
						+ " -nonce " + TpmUtils.byteArrayToHexString(nonce) 
						+ " -key_index " + keyIndex;
		commandLineResult result = executeVer2Command(6, argument, 1, false);
		if (result.getReturnCode() != 0) throw new TpmModuleException("TpmModule.createRevocableEndorsementKey returned nonzero error: " + result.getReturnCode() + "(" + ")");
		return TpmUtils.hexStringToByteArray(result.getResult(0));
	}
	/**
	 * 
	 * @param ownerAuth
	 * @param resetData
	 * @throws IOException
	 * @throws TpmModuleException
	 */
	public static void revokeRevocableEndorsementKey(byte [] ownerAuth, byte [] resetData)
			throws IOException, 
			TpmModuleException {
		/*
		 * Revoke Revocable Endorsement Key
		 * NIARL_TPM_Module -mode 7 -owner_auth <40 char hex blob> -reset <>
		 * return: no return ***
		 */
		String argument = "-owner_auth " + TpmUtils.byteArrayToHexString(ownerAuth) 
						+ " -reset " + TpmUtils.byteArrayToHexString(resetData);
		commandLineResult result = executeVer2Command(7, argument, 0, false);
		if (result.getReturnCode() != 0) throw new TpmModuleException("TpmModule.revokeRevocableEndorsementKey returned nonzero error: " + result.getReturnCode() + "(" + ")");
		return;
	}
	/**
	 * 
	 * @param keyType
	 * @param keyAuth
	 * @param keyIndex
	 * @return
	 * @throws IOException
	 * @throws TpmModuleException
	 * @throws TpmBytestreamResouceException
	 * @throws TpmUnsignedConversionException
	 */
	public static TpmKey createKey(String keyType, byte [] keyAuth, int keyIndex)
			throws IOException, 
			TpmModuleException, 
			TpmBytestreamResouceException, 
			TpmUnsignedConversionException {
		/*
		 * Create Key (sign or bind)
		 * NIARL_TPM_Module -mode 8 -key_type <"sign" | "bind"> -key_auth <40 char hex blob> -key_index <integer index>
		 * return: <modulus> <key blob>
		 */
		if (!(keyType.equals("sign") || keyType.equals("bind"))) throw new TpmModuleException("TpmModule.createKey: key type parameter must be \"sign\" or \"bind\".");
		String argument = "-key_type " + keyType + " -key_auth " + TpmUtils.byteArrayToHexString(keyAuth) + " -key_index " + keyIndex;
		commandLineResult result = executeVer2Command(8, argument, 2, false);
		if (result.getReturnCode() != 0) throw new TpmModuleException("TpmModule.createKey returned nonzero error: " + result.getReturnCode() + "(" + ")");
		byte [] tempArray = TpmUtils.hexStringToByteArray(result.getResult(0)); //modulus - discard in favor of blob
		tempArray = TpmUtils.hexStringToByteArray(result.getResult(1)); //modulus - discard in favor of blob
		TpmKey toReturn = new TpmKey(tempArray);
		return toReturn;
	}
	/**
	 * 
	 * @param keyType
	 * @param keyAuth
	 * @param keyBlob
	 * @param keyIndex
	 * @throws IOException
	 * @throws TpmModuleException
	 */
	public static void setKey(String keyType, byte [] keyAuth, byte [] keyBlob, int keyIndex)
			throws IOException, 
			TpmModuleException {
		/*
		 * Set Key (sign, bind, or identity)
		 * NIARL_TPM_Module -mode 9 -key_type <"sign" | "bind" | "identity"> -key_auth <40 char hex blob> -key_blob <complete key blob> -key_index <integer index>
		 * return: no return ***
		 */
		if (!(keyType.equals("sign") || keyType.equals("bind") || keyType.equals("identity"))) throw new TpmModuleException("TpmModule.setKey: key type parameter must be \"sign\", \"bind\", or \"identity\".");
		String argument = "-key_type " + keyType + " -key_auth " + TpmUtils.byteArrayToHexString(keyAuth) + " -key_blob " + TpmUtils.byteArrayToHexString(keyBlob) + " -key_index " + keyIndex;
		commandLineResult result = executeVer2Command(9, argument, 0, false);
		if (result.getReturnCode() != 0) throw new TpmModuleException("TpmModule.setKey returned nonzero error: " + result.getReturnCode() + "(" + ")");
		return;
	}
	/**
	 * 
	 * @param keyType
	 * @param keyIndex
	 * @param keyAuth
	 * @return
	 * @throws IOException
	 * @throws TpmModuleException
	 * @throws TpmBytestreamResouceException
	 * @throws TpmUnsignedConversionException
	 */
	public static TpmKey getKey(String keyType, int keyIndex, byte [] keyAuth)
			throws IOException, 
			TpmModuleException, 
			TpmBytestreamResouceException, 
			TpmUnsignedConversionException {
		/*
		 * Get Key (sign, bind, or identity) *
		 * NIARL_TPM_Module -mode 10 -key_type <"sign" | "bind" | "identity"> -key_index <integer index> -key_auth <40 char hex blob>
		 * return: <modulus> <key blob>
		 */
		if (!(keyType.equals("sign") || keyType.equals("bind") || keyType.equals("identity"))) throw new TpmModuleException("TpmModule.getKey: key type parameter must be \"sign\", \"bind\", or \"identity\".");
		String argument = "-key_type " + keyType + " -key_index " + keyIndex + " -key_auth " + TpmUtils.byteArrayToHexString(keyAuth);
		commandLineResult result = executeVer2Command(10, argument, 2, false);
		if (result.getReturnCode() != 0) throw new TpmModuleException("TpmModule.getKey returned nonzero error: " + result.getReturnCode() + "(" + ")");
		byte [] tempArray = TpmUtils.hexStringToByteArray(result.getResult(0)); //modulus - discard in favor of blob
		tempArray = TpmUtils.hexStringToByteArray(result.getResult(1)); //modulus - discard in favor of blob
		TpmKey toReturn = new TpmKey(tempArray);
		return toReturn;
	}
	/**
	 * 
	 * @param ownerAuth
	 * @param nonce
	 * @return
	 * @throws IOException
	 * @throws TpmModuleException
	 */
	public static byte [] getEndorsementKeyModulus(byte [] ownerAuth, byte [] nonce)
			throws IOException, 
			TpmModuleException {
		/*
		 * Get Key (EK) *
		 * NIARL_TPM_Module -mode 10 -key_type EK -owner_auth <40 char hex blob> -nonce <40 char hex blob>
		 * return: <modulus>
		 */
		String argument = "-key_type ek -owner_auth " + TpmUtils.byteArrayToHexString(ownerAuth) + " -nonce " + TpmUtils.byteArrayToHexString(nonce);
		commandLineResult result = executeVer2Command(10, argument, 1, false);
		if (result.getReturnCode() != 0) throw new TpmModuleException("TpmModule.getPublicEndorsementKey returned nonzero error: " + result.getReturnCode() + "(" + ")");
		return TpmUtils.hexStringToByteArray(result.getResult(0));
	}
	/**
	 * 
	 * @param keyType
	 * @param keyAuth
	 * @param keyIndex
	 * @throws IOException
	 * @throws TpmModuleException
	 */
	public static void clearKey(String keyType, byte [] keyAuth, int keyIndex)
			throws IOException, 
			TpmModuleException {
		/*
		 * Clear Key (sign, bind, or identity)
		 * NIARL_TPM_Module -mode 11 -key_type <"sign" | "bind" | "identity"> -key_auth <40 char hex blob> -key_index <integer index>
		 * return: no return ***
		 */
		if (!(keyType.equals("sign") || keyType.equals("bind") || keyType.equals("identity"))) throw new TpmModuleException("TpmModule.clearKey: key type parameter must be \"sign\", \"bind\", or \"identity\".");
		String argument = "-key_type " + keyType + " -key_auth " + TpmUtils.byteArrayToHexString(keyAuth) + " -key_index " + keyIndex;
		commandLineResult result = executeVer2Command(11, argument, 0, true);
		if (result.getReturnCode() != 0) throw new TpmModuleException("TpmModule.clearKey returned nonzero error: " + result.getReturnCode() + "(" + ")");
		return;
	}
	/**
	 * 
	 * @param ownerAuth
	 * @param credType
	 * @param credBlob
	 * @throws IOException
	 * @throws TpmModuleException
	 */
	public static void setCredential(byte [] ownerAuth, String credType, byte [] credBlob)
			throws IOException, 
			TpmModuleException {
		/*
		 * Set Credential (EC, PC, CC, and PCC)
		 * NIARL_TPM_Module -mode 12 -owner_auth <40 char hex blob> -cred_type <"EC" | "CC" | "PC" | "PCC"> -blob <>[-trousers]
		 * return: no return ***
		 */
		if (!(credType.equals("EC") || credType.equals("CC") || credType.equals("PC")|| credType.equals("PCC"))) throw new TpmModuleException("TpmModule.setCredential: credential type parameter must be \"EC\", \"CC\", \"PC\", or \"PCC\".");
		String argument = "-owner_auth " + TpmUtils.byteArrayToHexString(ownerAuth) + " -cred_type " + credType + " -blob " + TpmUtils.byteArrayToHexString(credBlob);
		// TROUSERS MODE OPTIONAL
		commandLineResult result = executeVer2Command(12, argument, 0, true);
		if (result.getReturnCode() != 0) throw new TpmModuleException("TpmModule.setCredential returned nonzero error: " + result.getReturnCode() + "(" + ")");
		return;
	}
	/**
	 * 
	 * @param ownerAuth
	 * @param credType
	 * @return
	 * @throws IOException
	 * @throws TpmModuleException
	 */
	public static byte [] getCredential(byte [] ownerAuth, String credType)
			throws IOException, 
			TpmModuleException {
		/*
		 * Get Credential (EC, PC, CC, and PCC)
		 * NIARL_TPM_Module -mode 13 -owner_auth <40 char hex blob> -cred_type <"EC" | "CC" | "PC" | "PCC"> [-trousers]
		 * return: <cred blob>
		 */
        // TODO INSECURE the owner secret is sent on the command line here... need to use sh -c  with Runtime.getRuntime().exec  and environment variables in order to protect it
 		if (!(credType.equals("EC") || credType.equals("CC") || credType.equals("PC")|| credType.equals("PCC"))) throw new TpmModuleException("TpmModule.getCredential: credential type parameter must be \"EC\", \"CC\", \"PC\", or \"PCC\".");
		String argument = "-owner_auth " + TpmUtils.byteArrayToHexString(ownerAuth) + " -cred_type " + credType;
		// TROUSERS MODE OPTIONAL
		commandLineResult result = executeVer2Command(13, argument, 1, true);
		if (result.getReturnCode() != 0) throw new TpmModuleException("TpmModule.getCredential returned nonzero error: " + result.getReturnCode() + "(" + ")");
		return TpmUtils.hexStringToByteArray(result.getResult(0));
	}
	/**
	 * 
	 * @param ownerAuth
	 * @param credType
	 * @throws IOException
	 * @throws TpmModuleException
	 */
	public static void clearCredential(byte [] ownerAuth, String credType)
			throws IOException, 
			TpmModuleException {
		/*
		 * Clear Credential (EC, PC, CC, and PCC)
		 * NIARL_TPM_Module -mode 14 -owner_auth <40 char hex blob> -cred_type <"EC" | "CC" | "PC" | "PCC">
		 * return: no return ***
		 */
		if (!(credType.equals("EC") || credType.equals("CC") || credType.equals("PC")|| credType.equals("PCC"))) throw new TpmModuleException("TpmModule.clearCredential: credential type parameter must be \"EC\", \"CC\", \"PC\", or \"PCC\".");
		String argument = "-owner_auth " + TpmUtils.byteArrayToHexString(ownerAuth) + " -cred_type " + credType;
		commandLineResult result = executeVer2Command(14, argument, 0, false);
		if (result.getReturnCode() != 0) throw new TpmModuleException("TpmModule.clearCredential returned nonzero error: " + result.getReturnCode() + "(" + ")");
		return;
	}
	/**
	 * 
	 * @param blob
	 * @param blobAuth
	 * @param mask
	 * @return
	 * @throws IOException
	 * @throws TpmModuleException
	 */
	public static byte [] seal(byte [] blob, byte [] blobAuth, byte [] mask)
			throws IOException, 
			TpmModuleException {
		/*
		 * Seal
		 * NIARL_TPM_Module -mode 15 -blob <> -blob_auth <40 char hex blob> -mask <>
		 * return: <sealed data blob>
		 */
		if ((mask.length > 3)||(mask.length == 0)) {
			throw new IllegalArgumentException ("TpmModule.seal: Mask must be between 1 and 3 bytes in length.");
		}
		String argument = "-blob " + TpmUtils.byteArrayToHexString(blob) + " -blob_auth " + TpmUtils.byteArrayToHexString(blobAuth) + " -mask " + TpmUtils.byteArrayToHexString(mask);
		commandLineResult result = executeVer2Command(15, argument, 1, false);
		if (result.getReturnCode() != 0) throw new TpmModuleException("TpmModule.seal returned nonzero error: " + result.getReturnCode() + "(" + ")");
		return TpmUtils.hexStringToByteArray(result.getResult(0));
	}
	/**
	 * 
	 * @param blob
	 * @param blobAuth
	 * @return
	 * @throws IOException
	 * @throws TpmModuleException
	 */
	public static byte [] unseal(byte [] blob, byte [] blobAuth)
			throws IOException, 
			TpmModuleException {
		/*
		 * Unseal
		 * NIARL_TPM_Module -mode 16 -blob <> -blob_auth <40 char hex blob>
		 * return: <unsealed data>
		 */
		String argument = "-blob " + TpmUtils.byteArrayToHexString(blob) + " -blob_auth " + TpmUtils.byteArrayToHexString(blobAuth);
		commandLineResult result = executeVer2Command(16, argument, 1, false);
		if (result.getReturnCode() != 0) throw new TpmModuleException("TpmModule.unseal returned nonzero error: " + result.getReturnCode() + "(" + ")");
		return TpmUtils.hexStringToByteArray(result.getResult(0));
	}
	/**
	 * 
	 * @param blob
	 * @param blobAuth
	 * @param keyAuth
	 * @param keyIndex
	 * @return
	 * @throws IOException
	 * @throws TpmModuleException
	 */
	public static byte [] bind(byte [] blob, byte [] blobAuth, byte [] keyAuth, int keyIndex)
			throws IOException, 
			TpmModuleException {
		/*
		 * Bind
		 * NIARL_TPM_Module -mode 17 -blob <> -blob_auth <40 char hex blob> -key_auth <40 char hex blob> -key_index <integer index>
		 * return: <bound data blob>
		 */
		String argument = "-blob " + TpmUtils.byteArrayToHexString(blob) + " -blob_auth " + TpmUtils.byteArrayToHexString(blobAuth) + " -key_auth " + TpmUtils.byteArrayToHexString(keyAuth) + " -key_index " + keyIndex;
		commandLineResult result = executeVer2Command(17, argument, 1, false);
		if (result.getReturnCode() != 0) throw new TpmModuleException("TpmModule.bind returned nonzero error: " + result.getReturnCode() + "(" + ")");
		return TpmUtils.hexStringToByteArray(result.getResult(0));
	}
	/**
	 * 
	 * @param blob
	 * @param blobAuth
	 * @param keyAuth
	 * @param keyIndex
	 * @return
	 * @throws IOException
	 * @throws TpmModuleException
	 */
	public static byte [] unbind(byte [] blob, byte [] blobAuth, byte [] keyAuth, int keyIndex)
			throws IOException, 
			TpmModuleException {
		/*
		 * Unbind
		 * NIARL_TPM_Module -mode 18 -blob <> -blob_auth <40 char hex blob> -key_auth <40 char hex blob> -key_index <integer index>
		 * return: <unbound data>
		 */
		String argument = "-blob " + TpmUtils.byteArrayToHexString(blob) + " -blob_auth " + TpmUtils.byteArrayToHexString(blobAuth) + " -key_auth " + TpmUtils.byteArrayToHexString(keyAuth) + " -key_index " + keyIndex;
		commandLineResult result = executeVer2Command(18, argument, 1, false);
		if (result.getReturnCode() != 0) throw new TpmModuleException("TpmModule.unbind returned nonzero error: " + result.getReturnCode() + "(" + ")");
		return TpmUtils.hexStringToByteArray(result.getResult(0));
	}
	/**
	 * 
	 * @param blob
	 * @param blobAuth
	 * @param keyAuth
	 * @param keyIndex
	 * @param mask
	 * @return
	 * @throws IOException
	 * @throws TpmModuleException
	 */
	public static byte [] sealBind(byte [] blob, byte [] blobAuth, byte [] keyAuth, int keyIndex, byte [] mask)
			throws IOException, 
			TpmModuleException {
		/*
		 * Seal Bind
		 * NIARL_TPM_Module -mode 19 -blob <> -blob_auth <40 char hex blob> -key_auth <40 char hex blob> -key_index <integer index> -mask <>
		 * return: <sealed bound data blob>
		 */
		//check the mask
		if ((mask.length > 3)||(mask.length == 0)) {
			throw new IllegalArgumentException ("TpmModule.sealBind: Mask must be between 1 and 3 bytes in length.");
		}
		String argument = "-blob " + TpmUtils.byteArrayToHexString(blob) + " -blob_auth " + TpmUtils.byteArrayToHexString(blobAuth) + " -key_auth " + TpmUtils.byteArrayToHexString(keyAuth) + " -key_index " + keyIndex + " -mask " + TpmUtils.byteArrayToHexString(mask);
		commandLineResult result = executeVer2Command(19, argument, 1, false);
		if (result.getReturnCode() != 0) throw new TpmModuleException("TpmModule.sealBind returned nonzero error: " + result.getReturnCode() + "(" + ")");
		return TpmUtils.hexStringToByteArray(result.getResult(0));
	}
	/**
	 * 
	 * @param blob
	 * @param blobAuth
	 * @param keyAuth
	 * @param keyIndex
	 * @return
	 * @throws IOException
	 * @throws TpmModuleException
	 */
	public static byte [] unsealUnbind(byte [] blob, byte [] blobAuth, byte [] keyAuth, int keyIndex)
			throws IOException, 
			TpmModuleException {
		/*
		 * Unseal Unbind
		 * NIARL_TPM_Module -mode 20 -blob <> -blob_auth <40 char hex blob> -key_auth <40 char hex blob> -key_index <integer index>
		 * return: <unsealed unbound data>
		 */
		String argument = "-blob " + TpmUtils.byteArrayToHexString(blob) + " -blob_auth " + TpmUtils.byteArrayToHexString(blobAuth) + " -key_auth " + TpmUtils.byteArrayToHexString(keyAuth) + " -key_index " + keyIndex;
		commandLineResult result = executeVer2Command(20, argument, 1, false);
		if (result.getReturnCode() != 0) throw new TpmModuleException("TpmModule.unsealUnbind returned nonzero error: " + result.getReturnCode() + "(" + ")");
		return TpmUtils.hexStringToByteArray(result.getResult(0));
	}
	/**
	 * 
	 * @param numBytes
	 * @return
	 * @throws IOException
	 * @throws TpmModuleException
	 */
	public static byte [] getRandomInteger(int numBytes)
			throws IOException, 
			TpmModuleException {
		/*
		 * Get Random Integer
		 * NIARL_TPM_Module -mode 21 -bytes <positive integer>
		 * return: <random integer>
		 */
		if (numBytes < 0) {
			throw new IllegalArgumentException ("TpmModule.getRandomInteger: number of bytes requested must be a positive number.");
		}
		String argument = "-bytes " + numBytes;
		commandLineResult result = executeVer2Command(21, argument, 1, false);
		if (result.getReturnCode() != 0) throw new TpmModuleException("TpmModule.getRandomInteger returned nonzero error: " + result.getReturnCode() + "(" + ")");
		return TpmUtils.hexStringToByteArray(result.getResult(0));
	}
	/**
	 * 
	 * @param blob
	 * @param keyAuth
	 * @param keyIndex
	 * @return
	 * @throws IOException
	 * @throws TpmModuleException
	 */
	public static byte [] sign(byte [] blob, byte [] keyAuth, int keyIndex)
			throws IOException, 
			TpmModuleException {
		/*
		 * Sign
		 * NIARL_TPM_Module -mode 22 -blob <> -key_auth <40 char hex blob> -key_index <integer index>
		 * return: <signature>
		 */
		String argument = "-blob " + TpmUtils.byteArrayToHexString(blob) + " -key_auth " + TpmUtils.byteArrayToHexString(keyAuth) + " -key_index " + keyIndex;
		commandLineResult result = executeVer2Command(22, argument, 1, false);
		if (result.getReturnCode() != 0) throw new TpmModuleException("TpmModule.sign returned nonzero error: " + result.getReturnCode() + "(" + ")");
		return TpmUtils.hexStringToByteArray(result.getResult(0));
	}
}
