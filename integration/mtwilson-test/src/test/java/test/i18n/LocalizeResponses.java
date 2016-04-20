/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package test.i18n;

import com.intel.mtwilson.ApacheHttpClient;
import com.intel.mtwilson.ApiClient;
import test.api.*;
import com.intel.mtwilson.api.*;
import com.intel.mtwilson.My;
import com.intel.dcsg.cpg.crypto.SimpleKeystore;
import com.intel.mtwilson.datatypes.ConnectionString;
import com.intel.mtwilson.i18n.ErrorCode;
import com.intel.mtwilson.datatypes.HostConfigData;
import com.intel.mtwilson.datatypes.TxtHostRecord;
import com.intel.mtwilson.datatypes.Vendor;
import com.intel.mtwilson.security.http.apache.ApacheHttpAuthorization;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.SignatureException;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Properties;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.MapConfiguration;
import org.junit.Test;
import static org.junit.Assert.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
public class LocalizeResponses {
    private final static Logger log = LoggerFactory.getLogger(LocalizeResponses.class);
    
    @Test
    public void testMessageNoLocale() throws Exception {
        Properties p = new Properties();
//        p.setProperty("mtwilson.url", "http://127.0.0.1:8080");
        p.setProperty("mtwilson.api.ssl.policy", "INSECURE");
        MapConfiguration config = new MapConfiguration(p);
        ApacheHttpClient client = new ApacheHttpClient(new URL("http://127.0.0.1:8080/AttestationService/resources/test/i18n"), null /* credentials */, null /* sslKeystore */, config);
        // not setting any locale at all means the client keeps its platform-default locale  (for jonathan's computer that's en-US)
        ApiResponse response = client.get("http://127.0.0.1:8080/AttestationService/resources/test/i18n/message.ok");        
        log.debug("message.ok default: {}",  new String(response.content) ); // getting "okie dokie"
    }

    @Test
    public void testMessageEmptyLocale() throws Exception {
        Properties p = new Properties();
//        p.setProperty("mtwilson.url", "http://127.0.0.1:8080");
        p.setProperty("mtwilson.api.ssl.policy", "INSECURE");
//        p.setProperty("mtwilson.locale", "");
        MapConfiguration config = new MapConfiguration(p);
        ApacheHttpClient client = new ApacheHttpClient(new URL("http://127.0.0.1:8080/AttestationService/resources/test/i18n"), null /* credentials */, null /* sslKeystore */, config);
        client.setLocale(new Locale("")); // results in an accept-language of "*" which means any locale
        ApiResponse response = client.get("http://127.0.0.1:8080/AttestationService/resources/test/i18n/message.ok");        
        log.debug("message.ok empty: {}",  new String(response.content) ); // getting "okie dokie"
    }
    
    @Test
    public void testMessageSpanishLocale() throws Exception {
        Properties p = new Properties();
//        p.setProperty("mtwilson.url", "http://127.0.0.1:8080");
        p.setProperty("mtwilson.api.ssl.policy", "INSECURE");
//        p.setProperty("mtwilson.locale", "es-CA");
        MapConfiguration config = new MapConfiguration(p);
        ApacheHttpClient client = new ApacheHttpClient(new URL("http://127.0.0.1:8080/AttestationService/resources/test/i18n"), null /* credentials */, null /* sslKeystore */, config);
        client.setLocale(new Locale("es-CA"));
        ApiResponse response = client.get("http://127.0.0.1:8080/AttestationService/resources/test/i18n/message.ok");        
        log.debug("message.ok es: {}",  new String(response.content) ); // getting "okie dokie"
    }
    
    @Test
    public void testMessageSpanishLocale2() throws Exception {
        Properties p = new Properties();
//        p.setProperty("mtwilson.url", "http://127.0.0.1:8080");
        p.setProperty("mtwilson.api.ssl.policy", "INSECURE");
//        p.setProperty("mtwilson.locale", "es-CA");
        MapConfiguration config = new MapConfiguration(p);
        ApacheHttpClient client = new ApacheHttpClient(new URL("http://127.0.0.1:8080/AttestationService/resources/test/i18n"), null /* credentials */, null /* sslKeystore */, config);
        client.setLocale(new Locale("es"));
        ApiResponse response = client.get("http://127.0.0.1:8080/AttestationService/resources/test/i18n/message.ok");        
        log.debug("message.ok es: {}",  new String(response.content) ); // getting "okie dokie"
    }

    @Test
    public void testMessageSpanishLocale3a() throws Exception {
        Properties p = new Properties();
//        p.setProperty("mtwilson.url", "http://127.0.0.1:8080");
        p.setProperty("mtwilson.api.ssl.policy", "INSECURE");
//        p.setProperty("mtwilson.locale", "es-CA");
        MapConfiguration config = new MapConfiguration(p);
        ApacheHttpClient client = new ApacheHttpClient(new URL("http://127.0.0.1:8080/AttestationService/resources/test/i18n"), null /* credentials */, null /* sslKeystore */, config);
        client.setLocale(new Locale("es-MX"));
        ApiResponse response = client.get("http://127.0.0.1:8080/AttestationService/resources/test/i18n/message.ok");        
        log.debug("message.ok es: {}",  new String(response.content) ); // getting "okie dokie"
    }

    @Test
    public void testMessageSpanishLocale3b() throws Exception {
        Properties p = new Properties();
//        p.setProperty("mtwilson.url", "http://127.0.0.1:8080");
        p.setProperty("mtwilson.api.ssl.policy", "INSECURE");
//        p.setProperty("mtwilson.locale", "es-CA");
        MapConfiguration config = new MapConfiguration(p);
        ApacheHttpClient client = new ApacheHttpClient(new URL("http://127.0.0.1:8080/AttestationService/resources/test/i18n"), null /* credentials */, null /* sslKeystore */, config);
        client.setLocale(new Locale("es_MX"));
        ApiResponse response = client.get("http://127.0.0.1:8080/AttestationService/resources/test/i18n/message.ok");        
        log.debug("message.ok es: {}",  new String(response.content) ); // getting "okie dokie"
    }
    
    @Test
    public void testMessageFrenchLocale() throws Exception {
        Properties p = new Properties();
        p.setProperty("mtwilson.api.ssl.policy", "INSECURE");
//        p.setProperty("mtwilson.locale", "fr");
        MapConfiguration config = new MapConfiguration(p);
        ApacheHttpClient client = new ApacheHttpClient(new URL("http://127.0.0.1:8080/AttestationService/resources/test/i18n"), null /* credentials */, null /* sslKeystore */, config);
        client.setLocale(new Locale("fr"));
        ApiResponse response = client.get("http://127.0.0.1:8080/AttestationService/resources/test/i18n/message.ok");        
        log.debug("message.ok fr: {}",  new String(response.content) );
    }

}
