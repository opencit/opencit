/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.trustagent.tpmmodules;

import com.intel.dcsg.cpg.crypto.RandomUtil;
import com.intel.mtwilson.util.exec.EscapeUtil;
import com.intel.mtwilson.util.exec.ExecUtil;
import com.intel.mtwilson.util.exec.Result;
import gov.niarl.his.privacyca.TpmIdentity;
import gov.niarl.his.privacyca.TpmModule;
import gov.niarl.his.privacyca.TpmUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author hxia5
 */

/* this is the module for TPM 1.2 */
public class TpmModule12 implements TpmModuleProvider {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TpmModule12.class);
    
    @Override
    public byte[] getCredential(byte[] ownerAuth, String credType) throws IOException, TpmModule.TpmModuleException {
        return TpmModule.getCredential(ownerAuth, credType);
    }

    @Override
    public void takeOwnership(byte[] ownerAuth, byte[] nonce) throws IOException, TpmModule.TpmModuleException {
        TpmModule.takeOwnership(ownerAuth, nonce);
    }

    @Override
    public byte[] getEndorsementKeyModulus(byte[] ownerAuth, byte[] nonce) throws IOException, TpmModule.TpmModuleException {
        return TpmModule.getEndorsementKeyModulus(ownerAuth, nonce);
    }

    @Override
    public void setCredential(byte[] ownerAuth, String credType, byte[] credBlob) throws IOException, TpmModule.TpmModuleException {
        TpmModule.setCredential(ownerAuth, credType, credBlob);
    }

    @Override
    public TpmIdentity collateIdentityRequest(byte[] ownerAuth, byte[] keyAuth, String keyLabel, byte[] pcaPubKeyBlob, int keyIndex, X509Certificate endorsmentCredential, boolean useECinNvram) throws IOException, TpmModule.TpmModuleException, CertificateEncodingException {
        return TpmModule.collateIdentityRequest(ownerAuth, keyAuth, keyLabel, pcaPubKeyBlob, keyIndex, endorsmentCredential, useECinNvram);
    }

    @Override
    public HashMap<String, byte[]> activateIdentity2(byte[] ownerAuth, byte[] keyAuth, byte[] asymCaContents, byte[] symCaAttestation, int keyIndex) throws IOException, TpmModule.TpmModuleException {
        return TpmModule.activateIdentity2(ownerAuth, keyAuth, asymCaContents, symCaAttestation, keyIndex);
    }

    @Override
    public byte[] activateIdentity(byte[] ownerAuth, byte[] keyAuth, byte[] asymCaContents, byte[] symCaAttestation, int keyIndex) throws IOException, TpmModule.TpmModuleException {
        return TpmModule.activateIdentity(ownerAuth, keyAuth, asymCaContents, symCaAttestation, keyIndex);
    }

    @Override
    public void setAssetTag(byte[] ownerAuth, byte[] assetTagHash) throws IOException, TpmModule.TpmModuleException {
        String index = getAssetTagIndex();
        byte[] randPasswd = RandomUtil.randomByteArray(20);
        boolean indexExists = nvIndexExists(getAssetTagIndex());
        if(indexExists) {
            log.debug("Index exists. Releasing index...");
            nvRelease(ownerAuth, index);
            log.debug("Creating new index...");
            nvDefine(ownerAuth, randPasswd, index, 20, "AUTHWRITE");
        } else {
            log.debug("Index does not exist. Creating it...");
            nvDefine(ownerAuth, randPasswd, index, 20, "AUTHWRITE");
        }        
        nvWrite(ownerAuth, randPasswd, index, assetTagHash);
        log.debug("Provisioned asset tag");
    }

    @Override
    public byte[] readAssetTag(byte[] ownerAuth) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getAssetTagIndex() {
        return "0x40000010";
    }

    @Override
    public void nvDefine(byte[] ownerAuth, byte[] indexPassword, String index, int size, String attributes) throws TpmModule.TpmModuleException, IOException {
        log.debug("running command tpm_nvdefine -i " + index + " -s 0x" + Integer.toHexString(size) + " -x -aXXXX -oXXXX --permissions=" + attributes);
        Map<String, String> variables = new HashMap<>();
        variables.put("tpmOwnerPass", TpmUtils.byteArrayToHexString(ownerAuth));
        variables.put("NvramPassword", TpmUtils.byteArrayToHexString(indexPassword));
        CommandLine command = new CommandLine("/opt/trustagent/bin/tpm_nvdefine");
        command.addArgument("-x");
        command.addArgument("-t");
        command.addArgument("-aNvramPassword");
        command.addArgument("-otpmOwnerPass");
        command.addArgument("--permissions=" + attributes);
        command.addArgument("-s 0x14", false);
        command.addArgument(String.format("-i %s", index), false);
        Result result = ExecUtil.execute(command, variables);
        if (result.getExitCode() != 0) {
            log.error("Error running command [{}]: {}", command.getExecutable(), result.getStderr());
            throw new TpmModule.TpmModuleException(result.getStderr());
        }
        log.debug("command stdout: {}", result.getStdout());       

    }

    @Override
    public void nvRelease(byte[] ownerAuth, String index) throws IOException, TpmModule.TpmModuleException {
        log.debug("running command tpm_nvrelease -x -t -i " + index + " -oXXXX");
        Map<String, String> variables = new HashMap<>();
        variables.put("tpmOwnerPass", TpmUtils.byteArrayToHexString(ownerAuth));
        CommandLine command = new CommandLine("/opt/trustagent/bin/tpm_nvrelease");
        command.addArgument("-x");
        command.addArgument("-t");
        command.addArgument("-otpmOwnerPass");
        command.addArgument(String.format("-i %s", index), false);
        Result result = ExecUtil.execute(command, variables);
        if (result.getExitCode() != 0) {
            log.error("Error running command [{}]: {}", command.getExecutable(), result.getStderr());
            throw new TpmModule.TpmModuleException(result.getStderr());
        }
        log.debug("command stdout: {}", result.getStdout());
    }

    @Override
    public void nvWrite(byte[] ownerAuth, byte[] indexPassword, String index, byte[] data) throws IOException, TpmModule.TpmModuleException {
        String filename = "/tmp/" + UUID.randomUUID().toString();
        File tmpFile = new File(filename);
        
        FileOutputStream output = new FileOutputStream(tmpFile);
        IOUtils.write(data, output);
        
        log.debug("running command tpm_nvwrite -x -i " + index + " -pXXXX -f " + filename);
        Map<String, String> variables = new HashMap<>();
        variables.put("NvramPassword", TpmUtils.byteArrayToHexString(indexPassword));
        CommandLine command = new CommandLine("/opt/trustagent/bin/tpm_nvwrite");
        command.addArgument("-x");
        command.addArgument("-t");
        command.addArgument("-pNvramPassword");
        command.addArgument(String.format("-i %s", index), false);
        command.addArgument("-f");
        command.addArgument(EscapeUtil.doubleQuoteEscapeShellArgument(filename));
        Result result = ExecUtil.execute(command, variables);
        if (result.getExitCode() != 0) {
            log.error("Error running command [{}]: {}", command.getExecutable(), result.getStderr());
            throw new TpmModule.TpmModuleException(result.getStderr());
        }
        log.debug("command stdout: {}", result.getStdout());

        // now delete the temporary hash file
        tmpFile.delete();
    }
    
    @Override
    public boolean nvIndexExists(String index) throws IOException, TpmModule.TpmModuleException {
        CommandLine command = new CommandLine("/opt/trustagent/bin/tpm_nvinfo");
        command.addArgument(String.format("-i %s", index), false);
        Result result = ExecUtil.execute(command);
        if (result.getExitCode() != 0) {
            log.error("Error running command [{}]: {}", command.getExecutable(), result.getStderr());
            throw new TpmModule.TpmModuleException(result.getStderr());
        }
        log.debug("command stdout: {}", result.getStdout());

        if (result.getStdout() != null && result.getStdout().contains("NVRAM index")) {
            return true;
        }
        return false;
    }

    @Override
    public byte[] nvRead(byte[] ownerAuth, String index) throws IOException, TpmModule.TpmModuleException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public String getPcrBanks() throws IOException, TpmModule.TpmModuleException {
        return "SHA1";
    }

}
