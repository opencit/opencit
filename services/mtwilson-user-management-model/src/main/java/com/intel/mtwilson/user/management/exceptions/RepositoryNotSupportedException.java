/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.user.management.exceptions;

import com.intel.mtwilson.i18n.ErrorCode;

/**
 *
 * @author ssbangal
 */
public class RepositoryNotSupportedException extends RepositoryException {

    public RepositoryNotSupportedException() {
        super();
    }

    public RepositoryNotSupportedException(ErrorCode errorCode) {
        super(errorCode);
    }        
    
}
