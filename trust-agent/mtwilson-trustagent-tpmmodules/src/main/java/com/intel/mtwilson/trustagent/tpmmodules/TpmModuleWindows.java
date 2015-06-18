/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.trustagent.tpmmodules;

import com.intel.mtwilson.Folders;
import gov.niarl.his.privacyca.TpmIdentity;
import gov.niarl.his.privacyca.TpmModule;
import gov.niarl.his.privacyca.TpmModule.TpmModuleException;
import gov.niarl.his.privacyca.TpmUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

/**
 *
 * @author hxia5
 */

/* this is for Windows platform */

public class TpmModuleWindows implements TpmModuleProvider {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TpmModuleWindows.class);

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
    public byte[] getCredential(byte[] ownerAuth, String credType) throws IOException, TpmModule.TpmModuleException {
        if (!(credType.equals("EC") || credType.equals("CC") || credType.equals("PC")|| credType.equals("PCC"))) 
            throw new TpmModuleException("TpmModule.getCredential: credential type parameter must be \"EC\", \"CC\", \"PC\", or \"PCC\".");
        
        //String argument = "-owner_auth " + TpmUtils.byteArrayToHexString(ownerAuth) + " -cred_type " + credType;
        // TROUSERS MODE OPTIONAL
        String argument = "";
        commandLineResult result;
        if (credType.equals("EC")) {
            argument = "GetEkCert";
            result = executeTpmCommand(argument, 1);
            if (result.getReturnCode() != 0) throw new TpmModuleException("TpmModule.getCredential returned nonzero error", result.getReturnCode());
            
            byte[] eccert = TpmUtils.hexStringToByteArray(result.getResult(0));
            log.debug("TpmModuleWindows getCredential eccert length: {}", eccert.length);
            return eccert;
        }
        log.debug("TpmModuleWindows getCredential Not supported CerdType");

        return null;
    }

    @Override
    public void takeOwnership(byte[] ownerAuth, byte[] nonce) throws IOException, TpmModule.TpmModuleException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public byte[] getEndorsementKeyModulus(byte[] ownerAuth, byte[] nonce) throws IOException, TpmModule.TpmModuleException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setCredential(byte[] ownerAuth, String credType, byte[] credBlob) throws IOException, TpmModule.TpmModuleException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public TpmIdentity collateIdentityRequest(byte[] ownerAuth, byte[] keyAuth, String keyLabel, byte[] pcaPubKeyBlob, int keyIndex, X509Certificate endorsmentCredential, boolean useECinNvram) throws IOException, TpmModule.TpmModuleException, CertificateEncodingException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public HashMap<String, byte[]> activateIdentity2(byte[] ownerAuth, byte[] keyAuth, byte[] asymCaContents, byte[] symCaAttestation, int keyIndex) throws IOException, TpmModule.TpmModuleException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public byte[] activateIdentity(byte[] ownerAuth, byte[] keyAuth, byte[] asymCaContents, byte[] symCaAttestation, int keyIndex) throws IOException, TpmModule.TpmModuleException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    
    private static commandLineResult executeTpmCommand(String args, int returnCount)
                    throws IOException {

        int returnCode;
        final String newTpmModuleExePath = Folders.application() + File.separator + "bin" ; // "./exe";
        final String newExeName = "PCPTool.exe";

        // Parse the args parameter to populate the environment variables array
        String[] params = args.split(" ");
        HashMap<String,String> environmentVars = new HashMap<String, String>();
        List<String> cmd = new ArrayList<String>();
        
        /* the command to be run should be
         * "cmd.exe /c path_to_pcptool.exe arguments"
        */
        cmd.add("cmd.exe");
        cmd.add("/c");
        cmd.add(newTpmModuleExePath + File.separator + newExeName);

        for(int loop = 0; loop < params.length; loop++) {
            String param = params[loop];
            cmd.add(param);
            /* Disable for now, the params of PCPTool is different from NIAL_TPM_MODULE on Linux
            if(param.equals("-owner_auth") || param.equals("-nonce") || param.equals("-key_auth") ||
                            param.equals("-pcak") ||  param.equals("-blob_auth")) {
                    String envVarName = param.substring(1).toUpperCase();
                    // Populate the environment variable hash map
                    environmentVars.put(envVarName, params[++loop]);
                    // update the params array with the env variable name
                    //params[loop] = envVarName;
                    cmd.add(envVarName);
            }
            */
        }

        /* create the process to run */
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.environment().putAll(environmentVars);
        Process p = pb.start();
        //Process p = Runtime.getRuntime().exec(commandLine);
        String line = "";
        if (returnCount != 0){
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String newLine;
            try {
                    while ((newLine = input.readLine()) != null) {
                            line = newLine;
                    }
                    log.debug("executeTPM output: {}", line);

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
