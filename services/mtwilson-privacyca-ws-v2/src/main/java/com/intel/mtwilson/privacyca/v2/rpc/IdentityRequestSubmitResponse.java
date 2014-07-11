/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.privacyca.v2.rpc;

import com.intel.dcsg.cpg.x509.X509Util;
import com.intel.mtwilson.My;
import com.intel.mtwilson.launcher.ws.ext.RPC;
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
import java.security.SignatureException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.concurrent.Callable;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import org.apache.commons.io.IOUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;

/**
 *
 * @author jbuhacoff
 */
@RPC("aik_request_submit_response")
public class IdentityRequestSubmitResponse implements Callable<byte[]> {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(IdentityRequestSubmitResponse.class);
    private byte[] identityRequestResponseToChallenge;

    public void setChallengeResponse(byte[] identityRequestResponseToChallenge) {
        this.identityRequestResponseToChallenge = identityRequestResponseToChallenge;
    }

    public byte[] getChallengeResponse() {
        return identityRequestResponseToChallenge;
    }

    
    @Override
    @RequiresPermissions("host_aiks:certify")    
    public byte[] call() throws Exception {
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
            File datadir = new File(My.filesystem().getBootstrapFilesystem().getVarPath() + File.separator + "privacyca-aik-requests"); 
            if( !datadir.exists() ) { datadir.mkdirs(); }
            String filename = TpmUtils.byteArrayToHexString(decryptedIdentityRequestChallenge); //Hex.encodeHexString(identityRequestChallenge)
            log.debug("Filename: {}", filename);
            File challengeFile = datadir.toPath().resolve(filename).toFile();
            if( !challengeFile.exists() ) {
                throw new RuntimeException("Invalid challenge response");
            }
            try(FileInputStream in = new FileInputStream(challengeFile)) {
                byte[] idProofBytes = IOUtils.toByteArray(in);
                String optionsFilename = filename + ".opt";
                try(FileInputStream optionsIn = new FileInputStream(datadir.toPath().resolve(optionsFilename).toFile())) {
                    String hexOptions = IOUtils.toString(optionsIn);
                    Util.TpmIdentityProofOptions options = Util.decodeTpmIdentityProofOptionsFromHex(hexOptions);
                    idProof = new TpmIdentityProof(idProofBytes, options.TrousersModeIV, options.TrousersModeSymkeyEncscheme, options.TrousersModeBlankOeap);
                }
            }
            String ekcertFilename = filename + ".ekcert";
            File ekcertFile = datadir.toPath().resolve(ekcertFilename).toFile();
            try(FileInputStream in = new FileInputStream(ekcertFile)) {
                byte[] ekcertBytes = IOUtils.toByteArray(in);
                ekCert = X509Util.decodeDerCertificate(ekcertBytes);
            }
            
			//compare decrypted response to challenge
			//if match, create AIC; else create failure code
			byte[] certBytes = TpmUtils.makeCert(idProof, caPrivKey, caPubCert, validityDays, 0).getEncoded();
			//encrypt response and return
			return createReturn(idProof.getAik(), (RSAPublicKey)ekCert.getPublicKey(), certBytes);
    }
    
	private static byte[] createReturn(TpmPubKey aik, RSAPublicKey pubEk, byte[] challengeRaw) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, TpmUtils.TpmUnsignedConversionException, IOException{
		byte [] key = TpmUtils.createRandomBytes(16);
		byte [] iv = TpmUtils.createRandomBytes(16);
		byte [] encryptedBlob = TpmUtils.concat(iv, TpmUtils.TCGSymEncrypt(challengeRaw, key, iv));
		byte [] credSize = TpmUtils.intToByteArray(encryptedBlob.length);

		TpmSymmetricKey symKey = new TpmSymmetricKey();
		symKey.setKeyBlob(key);
		symKey.setAlgorithmId(TpmKeyParams.TPM_ALG_AES);
		symKey.setEncScheme(TpmKeyParams.TPM_ES_SYM_CBC_PKCS5PAD);
		TpmKeyParams keyParms = new TpmKeyParams();
		keyParms.setAlgorithmId(TpmKeyParams.TPM_ALG_AES);
		keyParms.setEncScheme(TpmKeyParams.TPM_ES_NONE);
		keyParms.setSigScheme((short)0);
		keyParms.setSubParams(null);
		keyParms.setTrouSerSmode(true);

		byte [] asymBlob = TpmUtils.TCGAsymEncrypt(TpmUtils.concat(symKey.toByteArray(), TpmUtils.sha1hash(aik.toByteArray())), pubEk);
		byte [] symBlob = TpmUtils.concat(TpmUtils.concat(credSize, keyParms.toByteArray()), encryptedBlob);
		return TpmUtils.concat(asymBlob, symBlob);
	}
    
}
