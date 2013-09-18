/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package test.i18n;

import com.intel.mtwilson.datatypes.ErrorCode;
import com.intel.mtwilson.i18n.BundleName;
import java.text.ChoiceFormat;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Issue #956
 * 
 * Cannot use "My" here for testing because it would create a circular dependency
 * 
 * @author jbuhacoff
 */
public class I18nTest {
    private final Logger log = LoggerFactory.getLogger(getClass());
    @Test
    public void testServerErrorInternationalization() throws Exception {
        ErrorCode e = ErrorCode.AS_HOST_EXISTS;
        log.debug("Error toString: {}", e.toString());
    }
    
    /**
     * Reference:
     * http://docs.oracle.com/javase/tutorial/i18n/resbundle/concept.html
     * @throws Exception 
     */
    @Test
    public void testLoadMtWilsonStrings() throws Exception {
        Locale currentLocale = new Locale("en", "US" /*, "UNIX"*/);
        ResourceBundle labels = ResourceBundle.getBundle(BundleName.MTWILSON_STRINGS.bundle(), currentLocale);
        String test = labels.getString("TLS_COMMMUNICATION_ERROR");
        log.debug("Got string: {}", test);
        Object[] args = new Object[] { "myhost123", "no route to host" };
        MessageFormat formatter = new MessageFormat("");
        formatter.setLocale(currentLocale);
        formatter.applyPattern(test);
        String output = formatter.format(args);
        log.debug("After l10n: {}", output);
    }
    
    /**
     * Sample output:
Got string: Asynchronous operation timed out {0,choice,0#immediately|1#after 1 second|1<after {0,number,integer} seconds}
After l10n: Asynchronous operation timed out immediately
After l10n: Asynchronous operation timed out after 1 second
After l10n: Asynchronous operation timed out after 2 seconds
     * 
     * @throws Exception 
     */
    @Test
    public void testPluralStrings() throws Exception {
        //AS_ASYNC_TIMEOUT=Asynchronous operation timed out after %d seconds        
        Locale currentLocale = new Locale("en", "US" /*, "UNIX"*/);
        ResourceBundle labels = ResourceBundle.getBundle(BundleName.MTWILSON_STRINGS.bundle(), currentLocale);
        String test = labels.getString("AS_ASYNC_TIMEOUT");
        log.debug("Got string: {}", test);
        for(int i=0; i<=2; i++) {
            Object[] args = new Object[] { Integer.valueOf(i) };  // 0, 1, or 2 seconds            
            MessageFormat formatter = new MessageFormat("");
            formatter.setLocale(currentLocale);
            formatter.applyPattern(test);
            String output = formatter.format(args);
            log.debug("After l10n: {}", output);
        }
        
    }
    
    @Test
    public void testClientErrorInternationalization() throws Exception {
    }
}
