/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.audit.helper;

import com.intel.mtwilson.audit.data.AuditContext;

/**
 *
 * @author dsmagadx
 */
public class MtWilsonThreadLocal {

    private static InheritableThreadLocal contextThreadLocal = new InheritableThreadLocal();

    public static void set(AuditContext context) {
        contextThreadLocal.set(context);
    }

    public static void unset() {
        contextThreadLocal.remove();
    }

    public static AuditContext get() {
        return (AuditContext) contextThreadLocal.get();
    }
}
