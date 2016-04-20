/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package test.i18n;

import com.intel.mtwilson.datatypes.AuthResponse;
import com.intel.mtwilson.i18n.ErrorCode;
import com.intel.dcsg.cpg.i18n.Localizable;
import com.intel.dcsg.cpg.i18n.Message;
import java.util.Locale;
import org.junit.Test;
import static org.junit.Assert.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
public class TestLocalizationFilter {
    private static Logger log = LoggerFactory.getLogger(TestLocalizationFilter.class);
    @Test
    public void testAuthResponse() {
        AuthResponse response = new AuthResponse(ErrorCode.OK);
        assertTrue( response instanceof Localizable );
    }
    
    public static class AttestationMessage extends Message {
        public AttestationMessage(String name, Object... args) { super(name,args); }
        public String getBundleName() { return "MtWilsonStrings"; }
    }
    
    @Test
    public void testMessage() {
        Message m = new AttestationMessage(ErrorCode.OK.name());
        log.debug("Message with default locale: {}", m.toString());
        log.debug("Message with en: {}", m.toString(new Locale("en"))); // works, loads "en_US"
        log.debug("Message with es: {}", m.toString(new Locale("es"))); // works, loads "es"
        log.debug("Message with es,MX: {}", m.toString(new Locale("es","MX"))); // works, loads "es_MX"
        log.debug("Message with es_MX: {}", m.toString(new Locale("es_MX")));
        log.debug("Message with es,CA: {}", m.toString(new Locale("es","CA"))); // works, loads "es" since there is no "es_CA"
        log.debug("Message with es_CA: {}", m.toString(new Locale("es_CA"))); // does not work, loads "en_US" since it interprets es_CA as a language and it's not there
        log.debug("Message with fr: {}", m.toString(new Locale("fr")));
        log.debug("Message with fr,FR: {}", m.toString(new Locale("fr","FR"))); // works, loads "fr"
        log.debug("Message with fr_FR: {}", m.toString(new Locale("fr_FR"))); // does not work, loads "en_US" ... must separate language & country in Locale constructor
        m.setLocale(new Locale("es_MX"));
        log.debug("Message with default locale: {}", m.toString());
        m.setLocale(new Locale("es","MX"));
        log.debug("Message with default locale: {}", m.toString());
    }
    
    @Test
    public void testMyMessage() {
        Message m = new AttestationMessage("myprpoertyname");
//        m.toString(m.toString(new Locale("fr"));
    }
}
