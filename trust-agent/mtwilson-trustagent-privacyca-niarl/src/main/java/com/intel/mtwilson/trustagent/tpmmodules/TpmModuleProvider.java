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

/* This interface defines all the methods needed by trustagent communicating with TPM modules */
public interface TpmModuleProvider {
    public byte [] getCredential(byte [] ownerAuth, String credType)throws IOException, TpmModule.TpmModuleException; /* get credentials */
    public void takeOwnership(byte [] ownerAuth, byte [] nonce) throws IOException, TpmModule.TpmModuleException; /* take TPM the ownership */
    public byte [] getEndorsementKeyModulus(byte [] ownerAuth, byte [] nonce)throws IOException, TpmModule.TpmModuleException;
    public void setCredential(byte [] ownerAuth, String credType, byte [] credBlob) throws IOException, TpmModule.TpmModuleException;
    public TpmIdentity collateIdentityRequest(byte [] ownerAuth, byte [] keyAuth, String keyLabel, byte [] pcaPubKeyBlob, int keyIndex, X509Certificate endorsmentCredential, boolean useECinNvram) throws IOException, TpmModule.TpmModuleException, CertificateEncodingException;
    public HashMap<String,byte[]> activateIdentity2(byte [] ownerAuth, byte [] keyAuth, byte [] asymCaContents, byte [] symCaAttestation, int keyIndex) throws IOException, TpmModule.TpmModuleException;
    public byte [] activateIdentity(byte [] ownerAuth, byte [] keyAuth, byte [] asymCaContents, byte [] symCaAttestation, int keyIndex) throws IOException, TpmModule.TpmModuleException;
}
