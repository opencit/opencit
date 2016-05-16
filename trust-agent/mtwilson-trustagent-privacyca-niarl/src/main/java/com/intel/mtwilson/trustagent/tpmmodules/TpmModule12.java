/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.trustagent.tpmmodules;

import gov.niarl.his.privacyca.TpmIdentity;
import gov.niarl.his.privacyca.TpmModule;
import java.io.IOException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.HashMap;

/**
 *
 * @author hxia5
 */

/* this is the module for TPM 1.2 */
public class TpmModule12 implements TpmModuleProvider {

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
    
}
