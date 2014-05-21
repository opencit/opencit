/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mountwilson.as.common;
import com.intel.mtwilson.i18n.ErrorCode;

/**
 *
 * @author jbuhacoff
 */
public class ValidationException extends ASException {
    public ValidationException(String missingInput) {
        super(ErrorCode.AS_MISSING_INPUT, missingInput);
    }
}
