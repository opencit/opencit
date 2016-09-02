/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.trustagent.tpmmodules;

import com.intel.dcsg.cpg.configuration.Configuration;
import com.intel.dcsg.cpg.crypto.RandomUtil;
import com.intel.dcsg.cpg.x509.X509Util;
import com.intel.mtwilson.Folders;
import com.intel.mtwilson.configuration.ConfigurationFactory;
import com.intel.mtwilson.trustagent.TrustagentConfiguration;
import com.intel.mtwilson.trustagent.shell.CommandLineResult;
import com.intel.mtwilson.trustagent.shell.ShellExecutor;
import com.intel.mtwilson.trustagent.shell.ShellExecutorFactory;
import com.intel.mtwilson.util.exec.EscapeUtil;
import gov.niarl.his.privacyca.TpmIdentity;
import gov.niarl.his.privacyca.TpmKeyParams;
import gov.niarl.his.privacyca.TpmModule;
import gov.niarl.his.privacyca.TpmSymmetricKey;
import gov.niarl.his.privacyca.TpmUtils;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author hxia5
 */
public class TpmModule20 implements TpmModuleProvider {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TpmModule20.class);

    @Override
    public void setAssetTag(byte[] ownerAuth, byte[] assetTagHash) throws IOException, TpmModule.TpmModuleException {
        String index = getAssetTagIndex();
        //byte[] randPasswd = ownerAuth; //RandomUtil.randomByteArray(20);
        
        // Use ownerAuth for indexPassword
        
        if(nvIndexExists(index)) {
            log.debug("Index exists. Releasing index...");
            nvRelease(ownerAuth, index);
            log.debug("Creating new index...");
            nvDefine(ownerAuth, ownerAuth, index, 20, "0x02040002");
        } else {
            log.debug("Index does not exist. Creating it...");
            nvDefine(ownerAuth, ownerAuth, index, 20, "0x02040002");
        }
        
        nvWrite(ownerAuth, index, assetTagHash);
        log.debug("Provisioned asset tag");
    }

    @Override
    public byte[] readAssetTag(byte[] ownerAuth) throws IOException, TpmModule.TpmModuleException {
        String index = getAssetTagIndex();
        log.debug("Reading asset tag for Linux TPM 2.0...");
        if(nvIndexExists(index)) {
            log.debug("Asset Tag Index {} exists", index);
            return nvRead(ownerAuth, index, 20);
        } else {
            throw new TpmModule.TpmModuleException("Asset Tag has not been provisioned on this TPM");
        }
    }

    @Override
    public String getAssetTagIndex() throws IOException, TpmModule.TpmModuleException {
        return "0x1c10110";
    }

    @Override
    public void nvDefine(byte[] ownerAuth, byte[] indexPassword, String index, int size, String attributes) throws IOException, TpmModule.TpmModuleException {
        String[] args = {
            TpmUtils.byteArrayToHexString(ownerAuth),
            TpmUtils.byteArrayToHexString(indexPassword),
            index,
            Integer.toString(size),
            attributes
        };
        
        CommandLineResult result = getShellExecutor().executeTpmCommand("tpm2-nvdefine.sh", args, 0);
        
        if (result.getReturnCode() != 0) {
            throw new TpmModule.TpmModuleException("TpmModule20.nvDefine returned nonzero error", result.getReturnCode());
        }
    }

    @Override
    public void nvRelease(byte[] ownerAuth, String index) throws IOException, TpmModule.TpmModuleException {
        String[] args = {
            TpmUtils.byteArrayToHexString(ownerAuth),
            index
        };
        
        CommandLineResult result = getShellExecutor().executeTpmCommand("tpm2-nvrelease.sh", args, 0);

        if (result.getReturnCode() != 0) {
            throw new TpmModule.TpmModuleException("TpmModule20.nvRelease returned nonzero error", result.getReturnCode());
        }
    }

    @Override
    public byte[] nvRead(byte[] ownerAuth, String index, int size) throws IOException, TpmModule.TpmModuleException {
        String[] args = {
            TpmUtils.byteArrayToHexString(ownerAuth),
            index,
            Integer.toString(size)
        };
        
        CommandLineResult result = getShellExecutor().executeTpmCommand("tpm2-nvread.sh", args, 1);
        if(result.getReturnCode() != 0 ) {
            throw new TpmModule.TpmModuleException("TpmModule20.nvWrite returned nonzero error", result.getReturnCode());
        }
        return TpmUtils.hexStringToByteArray(result.getResult(0));
    }

    @Override
    public void nvWrite(byte[] authPassword, String index, byte[] data) throws IOException, TpmModule.TpmModuleException {        
        File file = File.createTempFile("nvwrite", "data");
        try (FileOutputStream output = new FileOutputStream(file)) {
            
            IOUtils.write(data, output);        

            String[] args  = {
                TpmUtils.byteArrayToHexString(authPassword),
                index,
                EscapeUtil.doubleQuoteEscapeShellArgument(file.getPath())
            };

            CommandLineResult result = getShellExecutor().executeTpmCommand("tpm2-nvwrite.sh", args, 0);

            if (result.getReturnCode() != 0) {
                throw new TpmModule.TpmModuleException("TpmModule20.nvWrite returned nonzero error", result.getReturnCode());
            }        
        } finally {
            file.delete();
        }
    }

    @Override
    public boolean nvIndexExists(String index) throws IOException, TpmModule.TpmModuleException {                        
        String[] args = {
            index
        };
        CommandLineResult result = getShellExecutor().executeTpmCommand("tpm2-nvindex-exists.sh", args, 1);

        if (result.getReturnCode() != 0) {
            throw new TpmModule.TpmModuleException("TpmModule20.nvRelease returned nonzero error", result.getReturnCode());
        }
        
        return "1".equals(result.getResult(0));
    }

    @Override
    public ShellExecutor getShellExecutor() {
        return ShellExecutorFactory.getInstance(ShellExecutorFactory.OS.Unix);
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
        TrustagentConfiguration config = TrustagentConfiguration.loadConfiguration();
        File ecCertificateFile = config.getEcCertificateFile();
        if( ecCertificateFile.exists() )
           return FileUtils.readFileToByteArray(ecCertificateFile);
        else
           throw new TpmModule.TpmModuleException("TpmModule20.getCredential returned nonzero error", 2);
    }

    @Override
    public void takeOwnership(byte[] ownerAuth, byte[] nonce) throws IOException, TpmModule.TpmModuleException {                        
        String[] args = {
            TpmUtils.byteArrayToHexString(ownerAuth)
        };
        
        CommandLineResult result = getShellExecutor().executeTpmCommand("tpm2-takeownership.sh", args, 0);
  
        if (result.getReturnCode() != 0) throw new TpmModule.TpmModuleException("TpmModule20.takeOwnership returned nonzero error", result.getReturnCode());        
    }

    @Override
    public byte[] getEndorsementKeyModulus(byte[] ownerAuth, byte[] nonce) throws IOException, TpmModule.TpmModuleException {                
        String args[] = {
            TpmUtils.byteArrayToHexString(ownerAuth),
            "RSA"
        };
        CommandLineResult result = getShellExecutor().executeTpmCommand("tpm2-readek", args, 2);
  
        if (result.getReturnCode() != 0) throw new TpmModule.TpmModuleException("TpmModule20.getEndorsementKeyModulus returned nonzero error", result.getReturnCode());
        
        log.debug("EK handle: {}", result.getResult(0)); 
        // save the EK handle to configuration
        //SetupConfigurationProvider provider = new SetupConfigurationProvider(ConfigurationFactory.getConfigurationProvider());
        //Configuration configuration = provider.load();
        //configuration.set(TrustagentConfiguration.EK_HANDLE, ekHandle);  
        //provider.save(configuration);
        //log.debug("Set EK handle is {}", TAconfig.getEkHandleHex());

        TrustagentConfiguration TAconfig = TrustagentConfiguration.loadConfiguration();
        // the output from above command return EK handle starting with 0x. this needs to be removed so the processing of this field later will succeed
        TAconfig.setEkHandle(result.getResult(0).substring(2));

        
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
        TrustagentConfiguration TAconfig = TrustagentConfiguration.loadConfiguration();
        String ekHandle = TAconfig.getEkHandle();
        log.debug("collateIdentity Request get EkHandle : {}", ekHandle);
        //byte[] ekHandle = TAconfig.getEkHandle();
        
        /* tpm2-createak.sh <ownerpasswd> <akpasswd> <ekhandle> <aktype>
        output: <akhandle> <akpubkey> <akname>
        */
       
        String[] args = {
            TpmUtils.byteArrayToHexString(ownerAuth),
            TpmUtils.byteArrayToHexString(keyAuth),
            ekHandle,
            //"0x81010000",
            "RSA"
        };
        
        CommandLineResult result = getShellExecutor().executeTpmCommand("tpm2-createak.sh", args, 3);
  
        if (result.getReturnCode() != 0) throw new TpmModule.TpmModuleException("TpmModule20.collateIdentityRequest returned nonzero error", result.getReturnCode());
        
        log.debug("AIK handle: {}", result.getResult(0));
        // the output from above command return key handle starting with 0x. this needs to be removed so the processing of this field later will succeed
        TAconfig.setAikHandle(result.getResult(0).substring(2));
        //TAconfig.getConf().set(TrustagentConfiguration.AIK_HANDLE, result.getResult(0));
        
        log.debug("AIK pub key: {}", result.getResult(1));
        log.debug("AIK name: {}", result.getResult(2));  
        TAconfig.setAikName(result.getResult(2));
        
        byte[] credRequest = TpmUtils.hexStringToByteArray(result.getResult(1));
        TpmIdentity newId = new TpmIdentity(credRequest, null, null, TpmUtils.hexStringToByteArray(result.getResult(2)));
        return newId;
    }

    @Override
    public HashMap<String, byte[]> activateIdentity2(byte[] ownerAuth, byte[] keyAuth, byte[] asymCaContents, byte[] symCaAttestation, int keyIndex) throws IOException, TpmModule.TpmModuleException { 
        HashMap<String, byte[]> toReturn = new HashMap<>();
        TrustagentConfiguration TAconfig = TrustagentConfiguration.loadConfiguration();
        String ekHandle = TAconfig.getEkHandle();
        String akHandle = TAconfig.getAikHandle();
        
        log.debug("activateIdentity2 AIK Handle : {}", akHandle);
       
        String[] args = {
            TpmUtils.byteArrayToHexString(ownerAuth),
            TpmUtils.byteArrayToHexString(keyAuth),
            akHandle,
            ekHandle,
            TpmUtils.byteArrayToHexString(asymCaContents)
        };
        
        CommandLineResult result = getShellExecutor().executeTpmCommand("tpm2-activatecredential.sh", args, 1);
        
        if(result.getReturnCode() != 0) {
            throw new TpmModule.TpmModuleException("Tpm2 activatecredential returned non zero error");
        }
        
        String decrypted = result.getResult(0);
        
        // TODO: put decrypted into HashMap        
        try {
            byte[] decrypted2 = TpmUtils.decryptSymCaAttestation(TpmUtils.hexStringToByteArray(decrypted), symCaAttestation);
            toReturn.put("aikcert", decrypted2);
            toReturn.put("aikblob", new byte[0]);
            return toReturn;
        } catch (TpmUtils.TpmUnsignedConversionException | TpmUtils.TpmBytestreamResouceException | NoSuchAlgorithmException |
                NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException ex) {
            Logger.getLogger(TpmModule20.class.getName()).log(Level.SEVERE, null, ex);
            throw new TpmModule.TpmModuleException(ex);
        }       
    }

    @Override
    public byte[] activateIdentity(byte[] ownerAuth, byte[] keyAuth, byte[] asymCaContents, byte[] symCaAttestation, int keyIndex) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public String getPcrBanks() throws IOException, TpmModule.TpmModuleException {
        String[] args = {};
        CommandLineResult result = getShellExecutor().executeTpmCommand("tpm2-getpcrbanks", args, 1);
  
        if (result.getReturnCode() != 0) throw new TpmModule.TpmModuleException("TpmModule20.getPcrBanks returned nonzero error", result.getReturnCode());
        log.debug("returned pcr banks trimmed: {}", result.getReturnOutput().trim());
        return result.getReturnOutput().trim();
    }      
}
