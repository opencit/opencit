/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.trustagent.tpmmodules;

import com.intel.dcsg.cpg.x509.X509Util;
import com.intel.mtwilson.Folders;
import com.intel.mtwilson.trustagent.TrustagentConfiguration;
import gov.niarl.his.privacyca.TpmIdentity;
import gov.niarl.his.privacyca.TpmModule;
import gov.niarl.his.privacyca.TpmUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author hxia5
 */
public class TpmModule20 implements TpmModuleProvider {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TpmModule20.class);

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
    
    @Override
    public byte[] getCredential(byte[] ownerAuth, String credType) throws TpmModule.TpmModuleException, IOException {
        /*
         * Get Credential (EC, PC, CC, and PCC)
         * return: <cred blob>
         */
        /* if (!(credType.equals("EC") || credType.equals("CC") || credType.equals("PC")|| credType.equals("PCC"))) 
            throw new TpmModule.TpmModuleException("TpmModule.getCredential: credential type parameter must be \"EC\", \"CC\", \"PC\", or \"PCC\".");

        commandLineResult result;
        if (credType.equals("EC")) {
            final String cmdPath = Folders.application() + File.separator + "bin";
            String cmdToexecute = cmdPath + File.separator + "tpm2-getec" + " RSA";
            result = executeTpmCommand(cmdToexecute, 1);
            if (result.getReturnCode() != 0) 
                throw new TpmModule.TpmModuleException("TpmModule20.getCredential returned nonzero error", result.getReturnCode()); 
            byte[] eccert = TpmUtils.hexStringToByteArray(result.getResult(0));
            log.debug("TpmModule20 getCredential eccert length: {}", eccert.length);
            return eccert;
        }
        log.debug("TpmModule20 getCredential Not supported CerdType: {}", credType);
        return null;
        */
        
        /* EC cert is save to file ekcert.cer now */
        byte[] ecCertByte = null;
        TrustagentConfiguration config = TrustagentConfiguration.loadConfiguration();
        File ecCertificateFile = config.getEcCertificateFile();
        if( ecCertificateFile.exists() )
           ecCertByte = FileUtils.readFileToByteArray(ecCertificateFile);
        else
           throw new TpmModule.TpmModuleException("TpmModule20.getCredential returned nonzero error", 2);

        return ecCertByte;
    }

    @Override
    public void takeOwnership(byte[] ownerAuth, byte[] nonce) throws IOException, TpmModule.TpmModuleException {
        final String cmdPath = Folders.application() + File.separator + "bin";
        String cmdToexecute = cmdPath + File.separator + "cit_tpm2_takeownership" + " " + TpmUtils.byteArrayToHexString(ownerAuth);
        commandLineResult result = executeTpmCommand(cmdToexecute, 0);
  
        if (result.getReturnCode() != 0) throw new TpmModule.TpmModuleException("TpmModule20.takeOwnership returned nonzero error", result.getReturnCode());
        return;
    }

    @Override
    public byte[] getEndorsementKeyModulus(byte[] ownerAuth, byte[] nonce) throws IOException, TpmModule.TpmModuleException {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        final String cmdPath = Folders.application() + File.separator + "bin";
        String cmdToexecute = cmdPath + File.separator + "tpm2-readek" + " " + TpmUtils.byteArrayToHexString(ownerAuth) + " " + "RSA";
        commandLineResult result = executeTpmCommand(cmdToexecute, 2);
  
        if (result.getReturnCode() != 0) throw new TpmModule.TpmModuleException("TpmModule20.getEndorsementKeyModulus returned nonzero error", result.getReturnCode());
        log.debug("EK handle: {}", result.getResult(0));
        return TpmUtils.hexStringToByteArray(result.getResult(1));
    }

    @Override
    public void setCredential(byte[] ownerAuth, String credType, byte[] credBlob) throws IOException {
        try{
            TrustagentConfiguration config = TrustagentConfiguration.loadConfiguration();
            String ekcertificatepath = config.getEcCertificateFile().getAbsolutePath();
            File file = new File(ekcertificatepath);
            mkdir(file);
            
            X509Certificate certificate = X509Util.decodeDerCertificate(credBlob); // throws CertificateException
            String certificatePem = X509Util.encodePemCertificate(certificate);          
            //String certificatestr = new String(credBlob);
            try(FileOutputStream out = new FileOutputStream(file)) { // throws FileNotFoundException
                IOUtils.write(certificatePem, out); // throws IOException
            }
        }   catch (CertificateException ex) {
            Logger.getLogger(TpmModule20.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void mkdir(File file) throws IOException {
        if (!file.getParentFile().isDirectory()) {
            if (!file.getParentFile().mkdirs()) {
                log.warn("Failed to create client installation path!");
                throw new IOException("Failed to create client installation path!");
            }
        }
    }

    @Override
    public TpmIdentity collateIdentityRequest(byte[] ownerAuth, byte[] keyAuth, String keyLabel, byte[] pcaPubKeyBlob, int keyIndex, X509Certificate endorsmentCredential, boolean useECinNvram) throws IOException, TpmModule.TpmModuleException {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        final String cmdPath = Folders.application() + File.separator + "bin";
        String cmdToexecute = cmdPath + File.separator + "tpm2-createak" + " " + TpmUtils.byteArrayToHexString(ownerAuth) + " " + "RSA";
        commandLineResult result = executeTpmCommand(cmdToexecute, 2);
  
        if (result.getReturnCode() != 0) throw new TpmModule.TpmModuleException("TpmModule20.collateIdentityRequest returned nonzero error", result.getReturnCode());
        log.debug("AIK pub key in hex: {}", result.getResult(0));
        byte[] credRequest = TpmUtils.hexStringToByteArray(result.getResult(0));
        TpmIdentity newId = new TpmIdentity(credRequest, null, null);
        return newId;
    }

    @Override
    public HashMap<String, byte[]> activateIdentity2(byte[] ownerAuth, byte[] keyAuth, byte[] asymCaContents, byte[] symCaAttestation, int keyIndex) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public byte[] activateIdentity(byte[] ownerAuth, byte[] keyAuth, byte[] asymCaContents, byte[] symCaAttestation, int keyIndex) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    private static commandLineResult executeTpmCommand(String cmdArgs, int returnCount)
                    throws IOException {

        int returnCode;
        final String newTpmModuleExePath = Folders.application() + File.separator + "bin" ;
        final String newExeName = "";

        // Parse the args parameter to populate the environment variables array
        //HashMap<String,String> environmentVars = new HashMap<String, String>();
        
        String[] params = cmdArgs.split(" ");
        //HashMap<String,String> environmentVars = new HashMap<String, String>();
        List<String> cmd = new ArrayList<String>();
        //cmd.add(newTpmModuleExePath + File.separator + newExeName);

        for(int loop = 0; loop < params.length; loop++) {
            String param = params[loop];
            cmd.add(param);
        }   
        // check the parameters
        for (String temp: cmd) {
            log.debug(temp);
        }
      
        /* create the process to run */
        ProcessBuilder pb = new ProcessBuilder(cmd);
        //pb.environment().putAll(environmentVars);
        Process p = pb.start();
        //Process p = Runtime.getRuntime().exec(commandLine);
        String line = "";
        if (returnCount != 0){
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String newLine;
            try {
                    while ((newLine = input.readLine()) != null) {
                            line = newLine;
                            log.debug("executeTPM output line: {}", line);
                    }
                    log.debug("executeTPM last line: {}", line);

                    input.close();
            } catch (Exception e) {
                    e.printStackTrace();
            }
            finally{
                    if (input != null)
                            input.close();
            }
        }
        
        //do a loop to wait for an exit value
        try {
            returnCode = p.waitFor();
        }
        catch(InterruptedException e) {
            log.error("Interrupted while waiting for return value");
            log.debug("Interrupted while waiting for return value", e);
            returnCode = -1;
        }

        log.debug("Return code: " + returnCode);

        commandLineResult toReturn = new commandLineResult(returnCode, returnCount);
        if ((returnCode == 0)&&(returnCount != 0)) {
                StringTokenizer st = new StringTokenizer(line);
            if( st.countTokens() < returnCount ) {
                log.debug("executeTPMCommand with return count {} but only {} tokens are available; expect java.util.NoSuchElementException", returnCount, st.countTokens());
            }
            for (int i = 0; i < returnCount; i++) {
                toReturn.setResult(i, st.nextToken());
            }
        }
        return toReturn;
    }
}
