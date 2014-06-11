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
public class RepositoryUpdateException extends RepositoryException {

    public RepositoryUpdateException() {
        super();
    }

    public RepositoryUpdateException(ErrorCode errorCode) {
        super(errorCode);
    }        
    
}
