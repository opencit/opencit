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
public class RepositoryResourceNotFoundException extends RepositoryException {

    public RepositoryResourceNotFoundException() {
        super();
    }

    public RepositoryResourceNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }        
    
}
