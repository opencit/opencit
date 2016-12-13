/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.privacyca.v2.rpc;

import com.intel.dcsg.cpg.x509.X509Util;
import com.intel.mtwilson.My;
import com.intel.mtwilson.launcher.ws.ext.RPC;
import com.intel.mtwilson.privacyca.v2.model.IdentityBlob;
import gov.niarl.his.privacyca.Tpm2Algorithm;
import gov.niarl.his.privacyca.Tpm2Credential;
import gov.niarl.his.privacyca.Tpm2Utils;
import gov.niarl.his.privacyca.TpmIdentityProof;
import gov.niarl.his.privacyca.TpmIdentityRequest;
import gov.niarl.his.privacyca.TpmKeyParams;
import gov.niarl.his.privacyca.TpmPubKey;
import gov.niarl.his.privacyca.TpmSymmetricKey;
import gov.niarl.his.privacyca.TpmUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.concurrent.Callable;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import org.apache.commons.io.IOUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;

/**
 *
 * @author jbuhacoff
 */
@RPC("aik_request_submit_response")
public class IdentityRequestSubmitResponse implements Callable<IdentityBlob> {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(IdentityRequestSubmitResponse.class);
    private byte[] identityRequestResponseToChallenge;
    private String tpmVersion;
    private byte[] aikName;

    public byte[] getAikName() {
        return aikName;
    }

    public void setAikName(byte[] aikName) {
        this.aikName = aikName;
    }

    public String getTpmVersion() {
        return tpmVersion;
    }

    public void setTpmVersion(String tpmVersion) {
        this.tpmVersion = tpmVersion;
    }

    public void setChallengeResponse(byte[] identityRequestResponseToChallenge) {
        this.identityRequestResponseToChallenge = identityRequestResponseToChallenge;
    }

    public byte[] getChallengeResponse() {
        return identityRequestResponseToChallenge;
    }

    @Override
    @RequiresPermissions("host_aiks:certify")
    public IdentityBlob call() throws Exception {
        RSAPrivateKey caPrivKey = TpmUtils.privKeyFromP12(My.configuration().getPrivacyCaIdentityP12().getAbsolutePath(), My.configuration().getPrivacyCaIdentityPassword());
        X509Certificate caPubCert = TpmUtils.certFromP12(My.configuration().getPrivacyCaIdentityP12().getAbsolutePath(), My.configuration().getPrivacyCaIdentityPassword());
        int validityDays = My.configuration().getPrivacyCaIdentityValidityDays();

        //decrypt response
        TpmIdentityRequest returnedIR = new TpmIdentityRequest(identityRequestResponseToChallenge);
        byte[] decryptedIdentityRequestChallenge = returnedIR.decryptRaw(caPrivKey); // should be the same 32 bytes that we sent as the encrypted challenge

        TpmIdentityProof idProof;
        X509Certificate ekCert;
        // find the existing challenge and idproof
        // save the challenge and idproof for use in identity request submit response if the client successfully answers the challenge
        // the filename is the challenge (in hex) and the content is the idproof
        File datadir = My.repository().getDirectory("privacyca-aik-requests"); //new File(My.filesystem().getBootstrapFilesystem().getVarPath() + File.separator + "privacyca-aik-requests"); 
        if (!datadir.exists()) {
            datadir.mkdirs();
        }
        String filename = TpmUtils.byteArrayToHexString(decryptedIdentityRequestChallenge); //Hex.encodeHexString(identityRequestChallenge)
        log.debug("Filename: {}", filename);
        File challengeFile = datadir.toPath().resolve(filename).toFile();
        if (!challengeFile.exists()) {
            throw new RuntimeException("Invalid challenge response");
        }
        
        String ekcertFilename = filename + ".ekcert";
        File ekcertFile = datadir.toPath().resolve(ekcertFilename).toFile();
        try (FileInputStream in = new FileInputStream(ekcertFile)) {
            byte[] ekcertBytes = IOUtils.toByteArray(in);
            ekCert = X509Util.decodeDerCertificate(ekcertBytes);
        }       
        
        String optionsFilename = filename + ".opt";
        
        if ("2.0".equals(tpmVersion)) {
            try(FileInputStream aikIn = new FileInputStream(challengeFile)) {
                byte[] aikBlob = IOUtils.toByteArray(aikIn);
                PublicKey key = Tpm2Utils.getPubKeyFromAikBlob(aikBlob);
                TpmPubKey k = new TpmPubKey((RSAPublicKey)key, 0x1, 0x4);                
                byte[] certBytes = TpmUtils.makeCert(k,"His_Identity_Key", caPrivKey, caPubCert, validityDays, 0).getEncoded();
                return createReturn(k, (RSAPublicKey)ekCert.getPublicKey(), certBytes);
            }
        } else {
            try (FileInputStream in = new FileInputStream(challengeFile)) {
                byte[] idProofBytes = IOUtils.toByteArray(in);
  
                try (FileInputStream optionsIn = new FileInputStream(datadir.toPath().resolve(optionsFilename).toFile())) {
                    String hexOptions = IOUtils.toString(optionsIn);
                    Util.TpmIdentityProofOptions options = Util.decodeTpmIdentityProofOptionsFromHex(hexOptions);
                    idProof = new TpmIdentityProof(idProofBytes, options.TrousersModeIV, options.TrousersModeSymkeyEncscheme, options.TrousersModeBlankOeap);
                }
            }
            //compare decrypted response to challenge
            //if match, create AIC; else create failure code
            byte[] certBytes = TpmUtils.makeCert(idProof, caPrivKey, caPubCert, validityDays, 0).getEncoded();
            //encrypt response and return
            return createReturn(idProof.getAik(), (RSAPublicKey) ekCert.getPublicKey(), certBytes);
        }     
    }

    private IdentityBlob createReturn(TpmPubKey aik, RSAPublicKey pubEk, byte[] aicBytes) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, TpmUtils.TpmUnsignedConversionException, IOException, ShortBufferException {
        byte[] key = TpmUtils.createRandomBytes(16);
        byte[] iv = TpmUtils.createRandomBytes(16);
        byte[] encryptedBlob = TpmUtils.concat(iv, TpmUtils.tcgSymEncrypt(aicBytes, key, iv));
        byte[] credSize = TpmUtils.intToByteArray(encryptedBlob.length);

        TpmSymmetricKey symKey = new TpmSymmetricKey();
        symKey.setKeyBlob(key);
        symKey.setAlgorithmId(TpmKeyParams.TPM_ALG_AES);
        symKey.setEncScheme(TpmKeyParams.TPM_ES_SYM_CBC_PKCS5PAD);
        TpmKeyParams keyParms = new TpmKeyParams();
        keyParms.setAlgorithmId(TpmKeyParams.TPM_ALG_AES);
        keyParms.setEncScheme(TpmKeyParams.TPM_ES_NONE);
        keyParms.setSigScheme((short) 0);
        keyParms.setSubParams(null);
        keyParms.setTrouSerSmode(true);

        IdentityBlob ret = new IdentityBlob();

        byte[] asymBlob;
        
        if("2.0".equals(tpmVersion)) {
            Tpm2Credential outCred = Tpm2Utils.makeCredential(pubEk, Tpm2Algorithm.Symmetric.AES, 128, Tpm2Algorithm.Hash.SHA256, key, aikName);
            asymBlob = TpmUtils.concat(outCred.getCredential(), outCred.getSecret());
        } else {
            // this can't be SHA256 because this is for TPM 1.2
            asymBlob = TpmUtils.tcgAsymEncrypt(TpmUtils.concat(symKey.toByteArray(), TpmUtils.sha1hash(aik.toByteArray())), pubEk);
        }
            
        byte[] symBlob = TpmUtils.concat(TpmUtils.concat(credSize, keyParms.toByteArray()), encryptedBlob);
        //return TpmUtils.concat(asymBlob, symBlob);                        
        //for windows to return EK_BLOB. append the encrypted EK_BLOB to the existing byte stream
        
        // another 1.2 specific structure
        byte[] ekBlob = returnEKBlob(key, TpmUtils.sha1hash(aik.toByteArray()));
        if (ekBlob == null) {
            log.debug("ekBlob is null :(");
            ret.setAsymSize(asymBlob.length);
            ret.setSymSize(symBlob.length);
            ret.setIdentityBlob(TpmUtils.concat(asymBlob, symBlob));
        } else {
            byte[] asymEkBlob = TpmUtils.tcgAsymEncrypt(ekBlob, pubEk);
            log.debug(" asymEkBlob: " + TpmUtils.byteArrayToHexString(asymEkBlob));
            ret.setAsymSize(asymBlob.length);
            ret.setSymSize(symBlob.length);
            ret.setIdentityBlob(TpmUtils.concat(TpmUtils.concat(asymBlob, symBlob), asymEkBlob));
        }
        return ret;
    }

    private static byte[] returnEKBlob(byte[] key, byte[] aikDigest) {
        try {
            /* it seems Windows uses TPM_EK_BLOB for TPM_ActivateIdentity. so we have to form that for Windows
            typedef struct tdTPM_EK_BLOB{
            TPM_STRUCTURE_TAG tag;
            TPM_EK_TYPE ekType;
            UINT32 blobSize;
            [size_is(blobSize)] BYTE* blob;
            } TPM_EK_BLOB;
            typedef struct tdTPM_EK_BLOB_ACTIVATE{
            TPM_STRUCTURE_TAG tag;
            TPM_SYMMETRIC_KEY sessionKey;
            TPM_DIGEST idDigest;
            TPM_PCR_INFO_SHORT pcrInfo;
            } TPM_EK_BLOB_ACTIVATE;
             */
            int cbActivation = 2
                    + //TPM_STRUCTURE_TAG tag = TPM_TAG_EK_BLOB
                    2
                    + //TPM_EK_TYPE ekType = TPM_EK_TYPE_ACTIVATE
                    4
                    + //UINT32 blobSize = cbActivation - (2 * sizeof(UINT16) + sizeof(UINT32))
                    2
                    + //TPM_STRUCTURE_TAG tag = TPM_TAG_EK_BLOB_ACTIVATE
                    4
                    + //TPM_ALGORITHM_ID algId = TPM_ALG_XOR
                    2
                    + //TPM_ENC_SCHEME encScheme = TPM_ES_NONE
                    2
                    + //UINT16 size
                    key.length
                    + aikDigest.length
                    + //cbAikDigest +
                    2
                    + // UINT16 sizeOfSelect = 3
                    3
                    + // PcrSelect
                    1
                    + //TPM_LOCALITY_SELECTION localityAtRelease = TPM_LOC_ZERO
                    20; //TPM_COMPOSITE_HASH digestAtRelease = 0
            byte[] activationBlob = new byte[cbActivation];
            short sVal;
            int intVal; // parameters to contrusct the activationBlob
            int index = 0;
            sVal = 0x000c; //TPM_STRUCTURE_TAG
            System.arraycopy(TpmUtils.shortToByteArray(sVal), 0, activationBlob, index, 2);
            index = index + 2;
            sVal = 0x0001; //TPM_EK_TYPE
            System.arraycopy(TpmUtils.shortToByteArray(sVal), 0, activationBlob, index, 2);
            index = index + 2;
            intVal = cbActivation - 8; //blobSize
            System.arraycopy(TpmUtils.intToByteArray(intVal), 0, activationBlob, index, 4);
            index = index + 4;
            sVal = 0x002b; // TPM_TAG_EK_BLOB_ACTIVATE
            System.arraycopy(TpmUtils.shortToByteArray(sVal), 0, activationBlob, index, 2);
            index = index + 2;
            //intVal = TpmKeyParams.TPM_ALG_AES; //not TPM_ALG_XOR
            intVal = 0x0000000a; //not TPM_ALG_XOR
            System.arraycopy(TpmUtils.intToByteArray(intVal), 0, activationBlob, index, 4);
            index = index + 4;
            //sVal = TpmKeyParams.TPM_ES_SYM_CBC_PKCS5PAD; //TPM_ES_NONE
            sVal = 0x0001; //TPM_ES_NONE
            System.arraycopy(TpmUtils.shortToByteArray(sVal), 0, activationBlob, index, 2);
            index = index + 2;
            sVal = (short) key.length;
            System.arraycopy(TpmUtils.shortToByteArray(sVal), 0, activationBlob, index, 2);
            index = index + 2;
            System.arraycopy(key, 0, activationBlob, index, key.length);
            index = index + key.length;
            System.arraycopy(aikDigest, 0, activationBlob, index, aikDigest.length);
            index = index + aikDigest.length;
            sVal = 0x0003; // UINT16 sizeOfSelect = 3
            System.arraycopy(TpmUtils.shortToByteArray(sVal), 0, activationBlob, index, 2);
            index = index + 2;
            index = index + 3; // 3 bytes of 0 PcrSelect
            byte[] loczero = new byte[1];
            loczero[0] = (byte) 0x01; //TPM_LOC_ZERO
            System.arraycopy(loczero, 0, activationBlob, index, 1);
            //#5830: Variable 'index' was never read after being assigned.
            //index = index + 1;
            // the digest is 0, so no need to copy
            //index = index + 20;
            log.debug("Activation blob size: " + cbActivation);
            log.debug("Activatoin blob: " + TpmUtils.byteArrayToHexString(activationBlob));
            return activationBlob;
        } catch (TpmUtils.TpmUnsignedConversionException ex) {
            log.debug("ReturnEK blob Error");
        }
        return null;
    }

}
