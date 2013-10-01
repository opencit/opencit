/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mountwilson.datamodel;

/**
 *
 * @author ssbangal
 */
public enum ApiClientListType {
    
    ALL(1),
    PENDING(2),
    EXPIRING(3),
    DELETE(4);
    
    private int typeCode;

    public int getTypeCode() {
        return typeCode;
    }

    private ApiClientListType(int typeCode) {
        this.typeCode = typeCode;
    }
}
