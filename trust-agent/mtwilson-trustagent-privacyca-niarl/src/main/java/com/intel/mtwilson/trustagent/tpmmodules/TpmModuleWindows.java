/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.trustagent.tpmmodules;

import com.intel.dcsg.cpg.crypto.RandomUtil;
import com.intel.mtwilson.Folders;
import com.intel.mtwilson.common.CommandResult;
import com.intel.mtwilson.common.CommandUtil;
import com.intel.mtwilson.common.TAException;
import com.intel.mtwilson.trustagent.TrustagentConfiguration;
import com.intel.mtwilson.trustagent.shell.CommandLineResult;
import com.intel.mtwilson.trustagent.shell.ShellExecutor;
import com.intel.mtwilson.trustagent.shell.ShellExecutorFactory;
import gov.niarl.his.privacyca.TpmIdentity;
import gov.niarl.his.privacyca.TpmIdentityProof;
import gov.niarl.his.privacyca.TpmIdentityRequest;
import gov.niarl.his.privacyca.TpmKeyParams;
import gov.niarl.his.privacyca.TpmModule;
import gov.niarl.his.privacyca.TpmModule.TpmModuleException;
import gov.niarl.his.privacyca.TpmPubKey;
import gov.niarl.his.privacyca.TpmSymmetricKey;
import gov.niarl.his.privacyca.TpmUtils;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author hxia5
 */

/* this is for Windows platform */

public class TpmModuleWindows implements TpmModuleProvider {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TpmModuleWindows.class);

    @Override
    public void setAssetTag(byte[] ownerAuth, byte[] assetTagHash) throws IOException, TpmModuleException {
        byte[] randPasswd = RandomUtil.randomByteArray(20);
        String index = getAssetTagIndex();    
        if(nvIndexExists(index)) {
            log.debug("Index exists. Releasing index...");
            nvRelease(ownerAuth, index);
            log.debug("Creating new index...");
            nvDefine(ownerAuth, randPasswd, index, 20, "AUTHWRITE");
        } else {
            log.debug("Index does not exist. Creating it...");
            nvDefine(ownerAuth, randPasswd, index, 20, "AUTHWRITE");
        }
        
        nvWrite(randPasswd, index, assetTagHash);
        log.debug("Provisioned asset tag");
    }

    @Override
    public byte[] readAssetTag(byte[] ownerAuth) throws IOException, TpmModuleException {
        String index = getAssetTagIndex();
        log.debug("Reading asset tag for Windows...");
        if(nvIndexExists(index)) {
            log.debug("Asset Tag Index {} exists", index);
            return nvRead(ownerAuth, index, 20);
        } else {
            throw new TpmModule.TpmModuleException("Asset Tag has not been provisioned on this TPM");
        }
    }

    @Override
    public String getAssetTagIndex() throws IOException, TpmModuleException {
        return "2.0".equals(Tpm.getTpmVersion()) ? "0x01c10110" : "0x40000010";
    }

    @Override
    public void nvDefine(byte[] ownerAuth, byte[] indexPassword, String index, int size, String attribute) throws IOException, TpmModuleException {
        try {
            String cmd = "tpmtool.exe nvdefine " + index + " 0x" + Integer.toHexString(size) + " " + TpmUtils.byteArrayToHexString(indexPassword) + " " + attribute;
            log.debug("running command: " + cmd);
            CommandUtil.runCommand(cmd);
        } catch (TAException ex) {
            log.error("error writing to nvram, " + ex.getMessage());
            throw new TpmModule.TpmModuleException(ex);
        }
    }

    @Override
    public void nvRelease(byte[] ownerAuth, String index) throws IOException, TpmModuleException {
        try {
            String cmd = "tpmtool.exe nvrelease " + index;
            log.debug("running command: " + cmd);
            CommandUtil.runCommand(cmd);
        } catch (TAException ex) {
            log.error("error releasing nvram index, " + ex.getMessage()); 
            throw new TpmModule.TpmModuleException(ex);
        }
    }

    @Override
    public void nvWrite(byte[] authPassword, String index, byte[] data) throws IOException, TpmModuleException {
        try {
            String cmd = "tpmtool.exe nvwrite " + index + " " + TpmUtils.byteArrayToHexString(authPassword) + " " + TpmUtils.byteArrayToHexString(data);
            log.debug("running command: " + cmd);
            CommandUtil.runCommand(cmd);
        } catch (TAException ex) {
            log.error("error writing to nvram, " + ex.getMessage());
            throw new TpmModule.TpmModuleException(ex);
        }
    }

    @Override
    public boolean nvIndexExists(String index) throws IOException, TpmModuleException {
        try {
            CommandResult result = CommandUtil.runCommand("tpmtool.exe nvinfo " + index);
            if (result != null && result.getStdout() != null) {
                if (result.getStdout().contains("NVRAM index")) {
                    return true;
                }
            }
        } catch (TAException ex) {
            log.error("error getting nvram info, " + ex.getMessage());
            throw new TpmModule.TpmModuleException(ex);
        }
        return false;
    }

    @Override
    public byte[] nvRead(byte[] ownerAuth, String index, int size) throws IOException, TpmModuleException {
        try {
            String cmd = "tpmtool.exe nvread " + index;
            log.debug("Running command: " + cmd);
            CommandResult cmdResult = CommandUtil.runCommand(cmd);
            
            // check if the cmd returns successfully. if so, set the assettag
            if (cmdResult.getExitcode() == 0) {
                log.debug("Provisioned Asset tag hash: {}", cmdResult.getStdout());
                return TpmUtils.hexStringToByteArray(cmdResult.getStdout());
            } else {
                log.debug("Error reading Asset tag");
                throw new TpmModule.TpmModuleException("nvread returned non zero error");
            }
        } catch (TAException ex) {
            log.error("error reading assetTag from nvram 0x40000010, " + ex.getMessage() );
            throw new TpmModule.TpmModuleException(ex);
        }
    }

    @Override
    public byte[] getCredential(byte[] ownerAuth, String credType) throws IOException, TpmModule.TpmModuleException {
        if (!(credType.equals("EC") || credType.equals("CC") || credType.equals("PC")|| credType.equals("PCC"))) 
            throw new TpmModuleException("TpmModule.getCredential: credential type parameter must be \"EC\", \"CC\", \"PC\", or \"PCC\".");
        
        //String argument = "-owner_auth " + TpmUtils.byteArrayToHexString(ownerAuth) + " -cred_type " + credType;
        // TROUSERS MODE OPTIONAL
        CommandLineResult result;
        if (credType.equals("EC")) {
            String[] cmdArgs = {}; 
            result = getShellExecutor().executeTpmCommand("GetEkCert", cmdArgs, 1);
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
        try {
            /*
            * Collate Identity Request
            * NIARL_TPM_Module -mode 3 -owner_auth <40 char hex blob> -key_auth <40 char hex blob> -key_label <hex string in ASCII> -pcak <public key blob for Privacy CA> -key_index <integer index> [-ec_blob <hex blob of endorsement credential> -ec_nvram -trousers]
            * return: <identity request> <aik modulus> <aik complete key blob>
            */
            
            /*
            String argument = "-owner_auth " + TpmUtils.byteArrayToHexString(ownerAuth)
            + " -key_auth " + TpmUtils.byteArrayToHexString(keyAuth)
            + " -key_label " + TpmUtils.byteArrayToHexString(keyLabel.getBytes())
            + " -pcak " + TpmUtils.byteArrayToHexString(pcaPubKeyBlob)
            + " -key_index " + keyIndex;
            if (endorsmentCredential != null)
            argument += " -ec_blob " + TpmUtils.byteArrayToHexString(endorsmentCredential.getEncoded());
            if (useECinNvram)
            argument += " -ec_nvram";
            */
            
            /* argument should be "tpmtool collateidentityrequest keyname keyIdBindingChoosenHash keyAuth
             * KeyIdbindingChooseHash should be the hash of keyname (keylabel) + PCA's public key
             */
		MessageDigest md = MessageDigest.getInstance("SHA1");
                byte [] idLabelBytes = keyLabel.getBytes();
		byte [] chosenId = new byte[idLabelBytes.length + pcaPubKeyBlob.length];
		System.arraycopy(idLabelBytes, 0, chosenId, 0, idLabelBytes.length);
		System.arraycopy(pcaPubKeyBlob, 0, chosenId, idLabelBytes.length, pcaPubKeyBlob.length);
		md.update(chosenId);
		byte [] chosenIdHash = md.digest();
                
            String[] cmdArgs = { 
                TpmUtils.byteArrayToHexString(keyLabel.getBytes()),
                TpmUtils.byteArrayToHexString(chosenIdHash),
                TpmUtils.byteArrayToHexString(keyAuth)
                };
            /*
            argument.add(TpmUtils.byteArrayToHexString(pcaPubKeyBlob));
            argument.add(TpmUtils.byteArrayToHexString(keyAuth));
            */
            // executeTpmCommand returns idbinding (IDENTITY_CONTENTS), AIK Pub key modulus, and AIK key blob
            CommandLineResult result = getShellExecutor().executeTpmCommand("CollateIdentityRequest", cmdArgs, 3);
            if (result.getReturnCode() != 0) throw new TpmModuleException("TpmModuleWindows.collateIdentityRequest returned nonzero error", result.getReturnCode());
            
            /* executeTpmCommand returns 
            * >> result.getResult(0) - 
            *      tpm1.2: 
            *        idbinding (IDENTITY_CONTENTS) -- Note the TpmModule for Linux based on NIAL_TPM_MODULE returns TPM_IDENTITY_REQUEST strcuture, 
            *       which is encrypted form of TPM_IDENTITY_PROOF. so we need to do some conversion.
            *      tpm2.0:
            *        aikname in the format of [nameAlg -2 bytes][hash of the AIK pub key - 32 byte in case of SHA256
            * >> result.getResult(1) - AIK Pub key Modulus
            * >> result.getResult(2) - AIK key blob
            * Windows PCP does not create identityreqeust raw bytes. It only created an idbinding, but it is just the structure of TPM_IDENTITY_CONTENTS
            * So we need to do some of the work specified in TSS spec
            * 1. Create a TPM_IDENTITY_PROOF structure and assign its field with identitybinding, AIK pub, AIK blob, and EK cert, etc from tpmtool
            * 2. Create TPM_IDENTITY_REQUEST structure based on TPM_IDENTITY_PROOF
            */
            //TpmIdentityProof(byte [] idLabel, byte [] idBinding, TpmPubKey AIK, byte [] ekCertBytes, byte [] platformCertBytes, byte [] conformanceCertBytes, boolean IV, boolean symKey, boolean oaep)
            if (Tpm.getTpmVersion().equals("1.2")) {
                byte[] endCreBytes = null;
                if (endorsmentCredential != null) endCreBytes = endorsmentCredential.getEncoded();
                TpmIdentityProof idProof = new TpmIdentityProof(keyLabel.getBytes(),
                        TpmUtils.hexStringToByteArray(result.getResult(0)),
                        new TpmPubKey(TpmUtils.hexStringToByteArray(result.getResult(1))),
                        endCreBytes, endCreBytes, endCreBytes, false, false, false);
                TpmPubKey caPubKey = new TpmPubKey(new ByteArrayInputStream(pcaPubKeyBlob));
                //TpmIdentityRequest(TpmIdentityProof newIdProof, RSAPublicKey caKey)
                TpmIdentityRequest idReq = new TpmIdentityRequest(idProof, caPubKey.getKey()); // this does the encryption of idProof by using caPubKey
                byte [] identityRequest = idReq.toByteArray();

                //log.debug("identity request: {}", idReq.toString());
                log.debug("identity request asym size: {}", idReq.getAsymBlob().length);

                byte [] aikModulus = TpmUtils.hexStringToByteArray(result.getResult(1));
                byte [] aikKeyBlob = TpmUtils.hexStringToByteArray(result.getResult(2));
                TpmIdentity toReturn = new TpmIdentity(identityRequest, aikModulus, aikKeyBlob);
                return toReturn;
            } 
            else {
                //result(0): AikName, result(1): aikPubModulus, result(2): aikKeyBlob 
                log.debug("AIK name: {}", result.getResult(0));  
                log.debug("AIK pub key: {}", result.getResult(1));
                log.debug("Aik Key Blob: {}", result.getResult(2));

                TrustagentConfiguration TAconfig = TrustagentConfiguration.loadConfiguration();
                TAconfig.setAikName(result.getResult(0));
        
                byte[] aikName = TpmUtils.hexStringToByteArray(result.getResult(0));
                byte [] aikPubModulus = TpmUtils.hexStringToByteArray(result.getResult(1));
                byte [] aikKeyBlob = TpmUtils.hexStringToByteArray(result.getResult(2));
                TpmIdentity newId = new TpmIdentity(aikPubModulus, aikPubModulus, aikKeyBlob, aikName);
                return newId;
            }
        } catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | NoSuchAlgorithmException | NoSuchPaddingException | TpmUtils.TpmUnsignedConversionException | InvalidKeySpecException ex) {
            Logger.getLogger(TpmModuleWindows.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TpmUtils.TpmBytestreamResouceException ex) {
            Logger.getLogger(TpmModuleWindows.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public HashMap<String, byte[]> activateIdentity2(byte[] ownerAuth, byte[] keyAuth, byte[] asymCaContents, byte[] symCaAttestation, int keyIndex) throws IOException, TpmModule.TpmModuleException {
        try {
            //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            
            /*
            * Activate Identity
            * NIARL_TPM_Module -mode 4 -owner_auth <40 char hex blob> -key_auth <40 char hex blob> -asym <> -sym <> -key_index <integer index>
            * return: <aik certificate>
            */
            /*
            String argument = "-owner_auth " + TpmUtils.byteArrayToHexString(ownerAuth)
            + " -key_auth " + TpmUtils.byteArrayToHexString(keyAuth)
            + " -asym " + TpmUtils.byteArrayToHexString(asymCaContents)
            + " -sym " + TpmUtils.byteArrayToHexString(symCaAttestation)
            + " -key_index " + keyIndex;
            */
            String HisIdentityLabel = "HIS_Identity_Key";
            
            // form the command arguments. This commands only returns the secrect encrypted inside the asymCaContents.
            String[] cmdArgs = {
                TpmUtils.byteArrayToHexString(HisIdentityLabel.getBytes()),
                TpmUtils.byteArrayToHexString(keyAuth),
                TpmUtils.byteArrayToHexString(asymCaContents),
            };
            CommandLineResult result = getShellExecutor().executeTpmCommand("ActivateIdentity", cmdArgs, 2);
            if (result.getReturnCode() != 0) throw new TpmModuleException("TpmModuleWindows.activateIdentity returned nonzero error", result.getReturnCode());
                           
            byte[] aikcert = TpmUtils.decryptSymCaAttestation(TpmUtils.hexStringToByteArray(result.getResult(0)), symCaAttestation);
            
            HashMap<String,byte[]> results = new HashMap<String, byte[]>();
            results.put("aikcert", aikcert);
            results.put("aikblob", TpmUtils.hexStringToByteArray(result.getResult(1)));
            return results;
        } catch (TpmUtils.TpmUnsignedConversionException ex) {
            Logger.getLogger(TpmModuleWindows.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TpmUtils.TpmBytestreamResouceException ex) {
            Logger.getLogger(TpmModuleWindows.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(TpmModuleWindows.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchPaddingException ex) {
            Logger.getLogger(TpmModuleWindows.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidKeyException ex) {
            Logger.getLogger(TpmModuleWindows.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidAlgorithmParameterException ex) {
            Logger.getLogger(TpmModuleWindows.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalBlockSizeException ex) {
            Logger.getLogger(TpmModuleWindows.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BadPaddingException ex) {
            Logger.getLogger(TpmModuleWindows.class.getName()).log(Level.SEVERE, null, ex);
        }       
        return null;
    }

    @Override
    public byte[] activateIdentity(byte[] ownerAuth, byte[] keyAuth, byte[] asymCaContents, byte[] symCaAttestation, int keyIndex) throws IOException, TpmModule.TpmModuleException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ShellExecutor getShellExecutor() {
        return ShellExecutorFactory.getInstance(ShellExecutorFactory.OS.Windows);
    }
    
    @Override
    public String getPcrBanks() throws IOException, TpmModule.TpmModuleException {
        return "SHA1";
    }
}
