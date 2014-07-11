/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.privacyca.v2.rpc;

import com.intel.mtwilson.launcher.ws.ext.RPC;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.util.concurrent.Callable;
import com.intel.mtwilson.My;
import gov.niarl.his.privacyca.TpmUtils;
import java.io.File;
import org.apache.shiro.authz.annotation.RequiresPermissions;

/**
 *
 * @author jbuhacoff
 */
@RPC("endorse_tpm")
@RequiresPermissions("tpms:endorse")
public class EndorseTpm implements Callable<X509Certificate> {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EndorseTpm.class);

    private byte[] ekModulus;

    public void setEkModulus(byte[] ekModulus) {
        this.ekModulus = ekModulus;
    }

    public byte[] getEkModulus() {
        return ekModulus;
    }
    
    @Override
    @RequiresPermissions("tpms:endorse")    
    public X509Certificate call() throws Exception {
        // load privacy ca ek signing key (cakey) and corresponding certificate (cacert)
        String EndorsementP12Pass = My.configuration().getPrivacyCaEndorsementPassword();
        File TpmEndorsmentP12 = My.configuration().getPrivacyCaEndorsementP12();
        Integer validityDays = My.configuration().getPrivacyCaEndorsementValidityDays();
        RSAPrivateKey cakey = TpmUtils.privKeyFromP12(TpmEndorsmentP12.getAbsolutePath(), EndorsementP12Pass);
        X509Certificate cacert = TpmUtils.certFromP12(TpmEndorsmentP12.getAbsolutePath(), EndorsementP12Pass);
        X509Certificate ekcert = TpmUtils.makeEkCert(ekModulus, cakey, cacert, validityDays);
        log.info("Endorsed EK sha1 {} serial {}", TpmUtils.sha1hash(ekcert.getEncoded()), ekcert.getSerialNumber());
        return ekcert;
    }
    
}
