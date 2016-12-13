/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.model;

import com.intel.dcsg.cpg.crypto.AbstractDigest;
import com.intel.dcsg.cpg.crypto.DigestAlgorithm;
import com.intel.dcsg.cpg.crypto.Sha1Digest;
import com.intel.dcsg.cpg.crypto.Sha256Digest;
import java.util.Map;

/**
 *
 * @author dczech
 */
public class MeasurementFactory {
    protected MeasurementFactory() {
        
    }
    
    public static Measurement newInstance(String bank, String digest, String label, Map<String,String> info) {
        return newInstance(DigestAlgorithm.valueOf(bank), digest, label, info);
    }
    
    public static Measurement newInstance(DigestAlgorithm bank, String digest, String label, Map<String,String> info) {
        switch(bank) {
            case SHA1: {
                Sha1Digest sha1;
                if(digest == null || digest.isEmpty()) {
                    sha1 = Sha1Digest.ZERO;
                } else {
                    sha1 = new Sha1Digest(digest);                    
                }
                return new MeasurementSha1(sha1, label, info);
            }
            case SHA256: {
                Sha256Digest sha2;
                if(digest == null || digest.isEmpty()) {
                    sha2 = new Sha256Digest(new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0});
                } else {
                    sha2 = new Sha256Digest(digest);
                }
                return new MeasurementSha256(sha2, label, info);
            }
            default: 
                throw new UnsupportedOperationException("PCRBank: " + bank + " not currently supported");
        }
    }
    
    public static Measurement newInstance(String bank, AbstractDigest digest, String label, Map<String,String> info) {
        return newInstance(DigestAlgorithm.valueOf(bank), digest, label, info);
    }
    
    public static Measurement newInstance(DigestAlgorithm bank, AbstractDigest digest, String label, Map<String,String> info) {
        switch(bank) {
            case SHA1: {
                if (digest == null) {
                    digest = Sha1Digest.ZERO;
                }
                if(!(digest instanceof Sha1Digest)) {
                    throw new IllegalArgumentException("DigestAlgorithm and supplied digest don't match");
                }
                return new MeasurementSha1((Sha1Digest)digest, label, info);
            }
            case SHA256:{
                if (digest == null) {
                    //digest = Sha256Digest.ZERO;
                    digest = new Sha256Digest(new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0});
                }
                if(!(digest instanceof Sha256Digest)) {
                    throw new IllegalArgumentException("DigestAlgorithm and supplied digest don't match");
                }
                return new MeasurementSha256((Sha256Digest)digest, label, info);
            }                
            default:
                throw new UnsupportedOperationException("PCRBank: " + bank + " not currently supported");
        }
    }
}
