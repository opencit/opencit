/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.privacyca.v2.rpc;

import com.intel.dcsg.cpg.x509.X509Util;
import com.intel.mtwilson.My;
import com.intel.mtwilson.launcher.ws.ext.RPC;
import com.intel.mtwilson.privacyca.v2.model.IdentityChallenge;
import com.intel.mtwilson.tpm.endorsement.jdbi.TpmEndorsementDAO;
import com.intel.mtwilson.tpm.endorsement.jdbi.TpmEndorsementJdbiFactory;
import com.intel.mtwilson.tpm.endorsement.model.TpmEndorsement;
import gov.niarl.his.privacyca.Tpm2Algorithm;
import gov.niarl.his.privacyca.Tpm2Credential;
import gov.niarl.his.privacyca.Tpm2Utils;
import gov.niarl.his.privacyca.TpmIdentityProof;
import gov.niarl.his.privacyca.TpmIdentityRequest;
import gov.niarl.his.privacyca.TpmKeyParams;
import gov.niarl.his.privacyca.TpmPubKey;
import gov.niarl.his.privacyca.TpmSymmetricKey;
import gov.niarl.his.privacyca.TpmUtils;
import gov.niarl.his.privacyca.TpmUtils.TpmUnsignedConversionException;
import java.io.File;
import java.util.List;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;

/**
 *
 * @author jbuhacoff
 */
@RPC("aik_request_get_challenge")
public class IdentityRequestGetChallenge implements Callable<IdentityChallenge> {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(IdentityRequestGetChallenge.class);

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
    private byte[] identityRequest;
    private byte[] endorsementCertificate;

    public void setIdentityRequest(byte[] identityRequest) {
        this.identityRequest = identityRequest;
    }

    public void setEndorsementCertificate(byte[] endorsementCertificate) {
        this.endorsementCertificate = endorsementCertificate;
    }

    public byte[] getIdentityRequest() {
        return identityRequest;
    }

    public byte[] getEndorsementCertificate() {
        return endorsementCertificate;
    }

    private Map<String, X509Certificate> getEndorsementCertificates() throws IOException, CertificateException {
        Map<String, X509Certificate> endorsementCerts = new HashMap<>();
        File ekCacertsPemFile = My.configuration().getPrivacyCaEndorsementCacertsFile();
        try (FileInputStream in = new FileInputStream(ekCacertsPemFile)) {
            String ekCacertsPem = IOUtils.toString(in); // throws IOException
            List<X509Certificate> ekCacerts = X509Util.decodePemCertificates(ekCacertsPem); // throws CertificateException
            for (X509Certificate ekCacert : ekCacerts) {
                log.debug("Adding issuer {}", ekCacert.getSubjectX500Principal().getName());
                endorsementCerts.put(ekCacert.getSubjectDN().getName(), ekCacert);
            }
        }
        return endorsementCerts;
    }

    @Override
    @RequiresPermissions("host_aiks:certify")
    public IdentityChallenge call() throws Exception {
        log.debug("PrivacyCA.p12: {}", My.configuration().getPrivacyCaIdentityP12().getAbsolutePath());
        RSAPrivateKey caPrivKey = TpmUtils.privKeyFromP12(My.configuration().getPrivacyCaIdentityP12().getAbsolutePath(), My.configuration().getPrivacyCaIdentityPassword());
        X509Certificate caPubCert = TpmUtils.certFromP12(My.configuration().getPrivacyCaIdentityP12().getAbsolutePath(), My.configuration().getPrivacyCaIdentityPassword());

        // load the trusted ek cacerts
        Map<String, X509Certificate> endorsementCerts = getEndorsementCertificates();

        TpmIdentityRequest tempEC = new TpmIdentityRequest(endorsementCertificate);
        X509Certificate ekCert = TpmUtils.certFromBytes(tempEC.decryptRaw(caPrivKey));
        log.debug("Validating endorsement certificate");
        if (!isEkCertificateVerifiedByAuthority(ekCert, endorsementCerts.get(ekCert.getIssuerDN().getName().replaceAll("\\x00", "")))
                && !isEkCertificateVerifiedByAnyAuthority(ekCert, endorsementCerts.values())
                && !isEkCertificateRegistered(ekCert)) {
            // cannot trust the EC because it's not signed by any of our trusted EC CAs and is not in the mw_tpm_ec table
            log.debug("EC is not trusted");
            throw new RuntimeException("Invalid identity request");
        }
        //check out the endorsement certificate
        //if the cert is good, issue challenge
        byte[] identityRequestChallenge = TpmUtils.createRandomBytes(32);
        // save the challenge and idproof for use in identity request submit response if the client successfully answers the challenge
        // the filename is the challenge (in hex) and the content is the idproof
        File datadir = My.repository().getDirectory("privacyca-aik-requests"); //new File(My.filesystem().getBootstrapFilesystem().getVarPath() + File.separator + "privacyca-aik-requests"); 
        if (!datadir.exists()) {
            datadir.mkdirs();
        }
        String filename = TpmUtils.byteArrayToHexString(identityRequestChallenge); //Hex.encodeHexString(identityRequestChallenge)
        log.debug("Filename: {}", filename);
        String optionsFilename = filename + ".opt";

        IdentityChallenge toReturn;
        if (tpmVersion.equals("2.0")) {
            try (FileOutputStream out = new FileOutputStream(datadir.toPath().resolve(filename).toFile())) {
                IOUtils.write(identityRequest, out);
            }

            try (FileOutputStream out = new FileOutputStream(datadir.toPath().resolve(optionsFilename).toFile())) {
                IOUtils.write(aikName, out);
            }

            TpmPubKey tpk = new TpmPubKey((RSAPublicKey)Tpm2Utils.getPubKeyFromAikBlob(identityRequest), 0x1, 0x4);
            toReturn = createReturn(tpk, (RSAPublicKey) ekCert.getPublicKey(), identityRequestChallenge);            
        } else {
            //decrypt identityRequest and endorsementCertificate
            TpmIdentityRequest idReq = new TpmIdentityRequest(identityRequest);
            TpmIdentityProof idProof = idReq.decrypt(caPrivKey);
            //check the rest of the identity proof
            if (!idProof.checkValidity((RSAPublicKey) caPubCert.getPublicKey())) {
                log.error("TPM IDPROOF failed validity check");
                throw new RuntimeException("Invalid identity request");
            }

            try (FileOutputStream out = new FileOutputStream(datadir.toPath().resolve(filename).toFile())) {
                IOUtils.write(idProof.toByteArray(), out);
            }

            try (FileOutputStream out = new FileOutputStream(datadir.toPath().resolve(optionsFilename).toFile())) {
                // and save the 3 trousers mode options into a second file because they are not included 
                Util.TpmIdentityProofOptions options = new Util.TpmIdentityProofOptions();
                options.TrousersModeIV = idProof.getIVmode();
                options.TrousersModeSymkeyEncscheme = idProof.getSymkeyEncscheme();
                options.TrousersModeBlankOeap = idProof.getOeapMode();
                String hexOptions = Util.encodeTpmIdentityProofOptionsToHex(options);
                IOUtils.write(hexOptions, out);
            }

            //encrypt the challenge and return
            log.debug("Phase 1 details:");
            log.debug(" AIK blob: " + TpmUtils.byteArrayToHexString(idProof.getAik().toByteArray()));
            log.debug(" challenge: " + TpmUtils.byteArrayToHexString(identityRequestChallenge));
            toReturn = createReturn(idProof.getAik(), (RSAPublicKey) ekCert.getPublicKey(), identityRequestChallenge);
            log.debug(" toReturn: " + TpmUtils.byteArrayToHexString(toReturn.getIdentityChallenge()));
            //return toReturn;

        }
        // also save the ekcert for the identity request submit response 
        String ekcertFilename = filename + ".ekcert";
        try (FileOutputStream out = new FileOutputStream(datadir.toPath().resolve(ekcertFilename).toFile())) {
            IOUtils.write(ekCert.getEncoded(), out);
        }
        
        return toReturn;
    }

    private IdentityChallenge createReturn(TpmPubKey aik, RSAPublicKey pubEk, byte[] challengeRaw) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, TpmUnsignedConversionException, IOException, ShortBufferException {
        byte[] key = TpmUtils.createRandomBytes(16);
        //String keyfixed  = "1234567890123456";
        //String keyfixed = "1234567890123456";
        //byte[] key = keyfixed.getBytes();
        byte[] iv = TpmUtils.createRandomBytes(16);
        byte[] encryptedBlob = TpmUtils.concat(iv, TpmUtils.tcgSymEncrypt(challengeRaw, key, iv));
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

        IdentityChallenge ret = new IdentityChallenge();
        
        byte[] asymBlob;
        if ("2.0".equals(tpmVersion)) {
            Tpm2Credential outCred = Tpm2Utils.makeCredential(pubEk, Tpm2Algorithm.Symmetric.AES, 128, Tpm2Algorithm.Hash.SHA256, key, aikName);
            asymBlob = TpmUtils.concat(outCred.getCredential(), outCred.getSecret());
        } else {
            asymBlob = TpmUtils.tcgAsymEncrypt(TpmUtils.concat(symKey.toByteArray(), TpmUtils.sha1hash(aik.toByteArray())), pubEk);
        }
        byte[] symBlob = TpmUtils.concat(TpmUtils.concat(credSize, keyParms.toByteArray()), encryptedBlob);

        //for windows to return EK_BLOB. append the encrypted EK_BLOB to the existing byte stream
        byte[] ekBlob = returnEKBlob(key, TpmUtils.sha1hash(aik.toByteArray()));
        if (ekBlob == null) {
            log.debug("ekBlob is null :(");
            ret.setAsymSize(asymBlob.length);
            ret.setSymSize(symBlob.length);
            ret.setIdentityChallenge(TpmUtils.concat(asymBlob, symBlob));
        } else {
            byte[] asymEkBlob = TpmUtils.tcgAsymEncrypt(ekBlob, pubEk);
            log.debug(" asymEkBlob: " + TpmUtils.byteArrayToHexString(asymEkBlob));
            ret.setAsymSize(asymBlob.length);
            ret.setSymSize(symBlob.length);
            //we can conclude asymBlob from asymSize + symSize
            ret.setIdentityChallenge(TpmUtils.concat(TpmUtils.concat(asymBlob, symBlob), asymEkBlob));
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
            //#5834: Variable 'index' was never read after being assigned.
            //index = index + 1;
            // the digest is 0, so no need to copy
            //index = index + 20;
            log.debug("Activation blob size: " + cbActivation);
            log.debug("Activatoin blob: " + TpmUtils.byteArrayToHexString(activationBlob));
            return activationBlob;
        } catch (TpmUnsignedConversionException ex) {
            log.debug("ReturnEK blob Error");
        }
        return null;
    }

    private boolean isEkCertificateVerifiedByAuthority(X509Certificate ekCert, X509Certificate authority) {
        if (authority != null) {
            try {
                ekCert.verify(authority.getPublicKey()); // throws SignatureException
                return true;
            } catch (Exception e) {
                log.debug("Failed to verify EC using CA {}: {}", ekCert.getIssuerDN().getName().replaceAll("\\x00", ""), e.getMessage());
            }
        }
        return false;
    }

    private boolean isEkCertificateVerifiedByAnyAuthority(X509Certificate ekCert, Collection<X509Certificate> authorities) {
        for (X509Certificate authority : authorities) {
            try {
                ekCert.verify(authority.getPublicKey()); // throws SignatureException
                log.debug("Verified EC with authority: {}", authority.getSubjectX500Principal().getName());
                return true;
            } catch (Exception e) {
                log.debug("Failed to verify EC with authority: {}", authority.getSubjectX500Principal().getName());
            }
        }
        return false;
    }

    private boolean isEkCertificateRegistered(X509Certificate ekCert) {
        try (TpmEndorsementDAO dao = TpmEndorsementJdbiFactory.tpmEndorsementDAO()) {
            TpmEndorsement tpmEndorsement = dao.findTpmEndorsementByIssuerEqualTo(ekCert.getIssuerDN().getName().replaceAll("\\x00", "")); // SHOULD REALLY BE BY CERT SHA256
            if (tpmEndorsement == null) {
                return false;
            }
            log.debug("EC is registered: {}", tpmEndorsement.getId().toString());
            return true;
        } catch (IOException e) {
            log.debug("Cannot check if EC is registered", e);
            return false;
        }
    }

}
