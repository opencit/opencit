/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.intel.mtwilson.trustagent.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

/**
 *
 * @author hxia5
 */
@JacksonXmlRootElement(localName="vm_attestation_request")
public class VMAttestationRequest {

    private String vm_instance_id;
    private String nonce;
    
    public VMAttestationRequest() {
        this.vm_instance_id = null;
    }
    
    public VMAttestationRequest(String vm_instance_id) {
        this.vm_instance_id = vm_instance_id;
    }

    public void setVmInstanceId(String vm_instance_id) {
        this.vm_instance_id = vm_instance_id;
    }

    public String getVmInstanceId() {
        return vm_instance_id;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    
}