/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.model;

import com.intel.dcsg.cpg.crypto.DigestAlgorithm;
import java.util.List;

/**
 *
 * @author dczech
 */
public class PcrEventLogFactory {
    protected PcrEventLogFactory() {
        
    }
    
    public static PcrEventLog newInstance(DigestAlgorithm bank, PcrIndex index, List modules) {
        switch(bank) {
            case SHA1:
                return new PcrEventLogSha1(index, modules);
            case SHA256:
                return new PcrEventLogSha256(index, modules);
            default:
                throw new UnsupportedOperationException("PCRBank: " + bank + " not currently supported");
        }
    }
    
    public static PcrEventLog newInstance(String bank, PcrIndex index, List modules) {
        return newInstance(DigestAlgorithm.valueOf(bank), index, modules);
    }
}
