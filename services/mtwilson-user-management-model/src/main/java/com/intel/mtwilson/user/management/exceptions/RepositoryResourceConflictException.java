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
public class RepositoryResourceConflictException extends RepositoryException {

    public RepositoryResourceConflictException() {
        super();
    }

    public RepositoryResourceConflictException(ErrorCode errorCode) {
        super(errorCode);
    }        
    
}
