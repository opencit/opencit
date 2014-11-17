/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.extensions.factorystyle;

import com.intel.dcsg.cpg.extensions.ExtensionNotFoundException;
import com.intel.dcsg.cpg.extensions.Extensions;
import com.intel.dcsg.cpg.extensions.WhiteboardExtensionProvider;
import com.intel.dcsg.cpg.extensions.factorystyle.Telephone;
import com.intel.dcsg.cpg.extensions.factorystyle.InternetTelephoneFactory;
import com.intel.dcsg.cpg.extensions.factorystyle.TraditionalTelephoneFactory;
import com.intel.dcsg.cpg.extensions.factorystyle.AcmeTelephoneFactory;
import com.intel.dcsg.cpg.extensions.factorystyle.VoipFactory;
import com.intel.dcsg.cpg.extensions.factorystyle.Voip;
import com.intel.dcsg.cpg.extensions.factorystyle.TelephoneFactory;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jbuhacoff
 */
public class FactoryWhiteboardTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FactoryWhiteboardTest.class);

    @Test(expected=IllegalArgumentException.class)
    public void testRegisterWrongClass() {
        WhiteboardExtensionProvider.register(Telephone.class, VoipFactory.class);
    }

    @Test
    public void testRegisterAppropriateClass() {
        WhiteboardExtensionProvider.register(Telephone.class, Voip.class);
    }

    @Test
    public void testCreateLandline() {
        WhiteboardExtensionProvider.clearAll(); // must clear or else VoipFactory or AcmeTelephoneFactory from other tests might appear and w/o the context we might try to accidentally get a ladnline from them which won't work!
        WhiteboardExtensionProvider.register(TelephoneFactory.class, TraditionalTelephoneFactory.class);
        TelephoneFactory factory = Extensions.require(TelephoneFactory.class);
        log.debug("Got factory: {}", factory.getClass().getName());
        Telephone landline = factory.create();
        log.debug("Got telephone: {}", landline.getClass().getName());
        log.debug("Calling: {}", landline.call("111-222-3333"));
    }
    
    @Test
    public void testCreateLandlineWithPreference() {
        WhiteboardExtensionProvider.register(TelephoneFactory.class, TraditionalTelephoneFactory.class);
        WhiteboardExtensionProvider.register(TelephoneFactory.class, InternetTelephoneFactory.class);
        WhiteboardExtensionProvider.prefer(TelephoneFactory.class, new String[] { "com.intel.dcsg.cpg.whiteboard.InternetTelephoneFactory", "com.intel.dcsg.cpg.whiteboard.TraditionalTelephoneFactory" });
        TelephoneFactory factory = Extensions.require(TelephoneFactory.class); // we are asking for ANY telephone factory...
        Telephone landline = factory.create(); // but then requesting a landline... so this will throw IllegalArgumentException("Unrecognized context") beacuse the factory we get is InternettelephoneFactory (because of the prefernce) which doesn't implement "landline"
        log.debug("Got telephone: {}", landline.getClass().getName());
        log.debug("Calling: {}", landline.call("111-222-3333"));
    }

    @Test
    public void testCreateLandlineWithPreferenceAndContext() {
        WhiteboardExtensionProvider.register(TelephoneFactory.class, TraditionalTelephoneFactory.class);
        WhiteboardExtensionProvider.register(TelephoneFactory.class, InternetTelephoneFactory.class);
        WhiteboardExtensionProvider.prefer(TelephoneFactory.class, new String[] { "com.intel.dcsg.cpg.whiteboard.InternetTelephoneFactory", "com.intel.dcsg.cpg.whiteboard.TraditionalTelephoneFactory" });
        TelephoneFactory factory = Extensions.require(TelephoneFactory.class, "landline"); // we are asking for a telephone factory that can give us a landline...
        Telephone landline = factory.create(); // so even though InternetTelephoneFactory is listed first in the preferences, because it cannot gie a landline it is skipped and then we get the landline from TraditionalTelephoneFactory
        log.debug("Got telephone: {}", landline.getClass().getName());
        log.debug("Calling: {}", landline.call("111-222-3333"));
    }
    
    @Test
    public void testCreateCellphoneWithPreferenceAndContext() {
        WhiteboardExtensionProvider.register(TelephoneFactory.class, AcmeTelephoneFactory.class);
        WhiteboardExtensionProvider.register(TelephoneFactory.class, TraditionalTelephoneFactory.class);
        WhiteboardExtensionProvider.register(TelephoneFactory.class, InternetTelephoneFactory.class);
        WhiteboardExtensionProvider.prefer(TelephoneFactory.class, new String[] { "com.intel.dcsg.cpg.whiteboard.InternetTelephoneFactory", "com.intel.dcsg.cpg.whiteboard.AcmeTelephoneFactory", "com.intel.dcsg.cpg.whiteboard.TraditionalTelephoneFactory" });
        TelephoneFactory factory = Extensions.require(TelephoneFactory.class, "cell"); 
        Telephone landline = factory.create();
        log.debug("Got telephone: {}", landline.getClass().getName()); // should be acme  because it's first in priority order
        log.debug("Calling: {}", landline.call("111-222-3333"));
    }
    
    
    @Test
    public void testCreateLandlineWithPreferenceForNonexistentImplementation() {
        WhiteboardExtensionProvider.register(TelephoneFactory.class, TraditionalTelephoneFactory.class);
        WhiteboardExtensionProvider.register(TelephoneFactory.class, InternetTelephoneFactory.class);
        WhiteboardExtensionProvider.prefer(TelephoneFactory.class, new String[] { "com.intel.dcsg.cpg.whiteboard.DoesNotExistTelephoneFactory" });
        TelephoneFactory factory = Extensions.require(TelephoneFactory.class, "landline"); // if you don't specify the context here then you MIGHT get InternetTelephoneFactory which will throw IllegalAgumentException when you try to create("landline")
        log.debug("Got factory implementation {}", factory.getClass().getName());
        Telephone landline = factory.create();
        log.debug("Got telephone: {}", landline.getClass().getName());
        log.debug("Calling: {}", landline.call("111-222-3333"));
    }

    
    @Test
    public void testCreateLandlineWithContext() {
        WhiteboardExtensionProvider.clearAll();
        WhiteboardExtensionProvider.register(TelephoneFactory.class, TraditionalTelephoneFactory.class);
        connectByTelephone();
        TelephoneFactory factory = Extensions.require(TelephoneFactory.class, "landline");
        Telephone landline = factory.create();
        log.debug("Got telephone: {}", landline.getClass().getName());
        log.debug("Calling: {}", landline.call("111-222-3333"));
    }

    
    private void connectByTelephone() {
        TelephoneFactory factory = // extension point:
                Extensions.require(TelephoneFactory.class, "landline");
        // now we have a new "landline" TelephoneFactory instance
        Telephone landline = factory.create();
        log.debug("Got telephone: {}", landline.getClass().getName());
        log.debug("Calling: {}", landline.call("111-222-3333"));
    }
    
    
    @Test
    public void testCreateCellphoneWithContext() {
        WhiteboardExtensionProvider.register(TelephoneFactory.class, TraditionalTelephoneFactory.class);
        TelephoneFactory factory = Extensions.require(TelephoneFactory.class, "cell");
        Telephone landline = factory.create();
        log.debug("Got telephone: {}", landline.getClass().getName());
        log.debug("Calling: {}", landline.call("111-222-3333"));
    }
    @Test(expected=ExtensionNotFoundException.class)
    public void testCreateCellphoneWithUnsupportedContext() {
        WhiteboardExtensionProvider.register(TelephoneFactory.class, InternetTelephoneFactory.class);
        WhiteboardExtensionProvider.register(TelephoneFactory.class, TraditionalTelephoneFactory.class);
        TelephoneFactory factory = Extensions.require(TelephoneFactory.class, "xyzphone"); // throws ServiceNotFoundException because none of the telephone factories know how to create an "xyzphone"
        Telephone landline = factory.create();
        log.debug("Got telephone: {}", landline.getClass().getName());
        log.debug("Calling: {}", landline.call("111-222-3333"));
    }
    @Test(expected=ExtensionNotFoundException.class)
    public void testCreateCellphoneWithWrongContextType() {
        WhiteboardExtensionProvider.register(TelephoneFactory.class, InternetTelephoneFactory.class);
        WhiteboardExtensionProvider.register(TelephoneFactory.class, TraditionalTelephoneFactory.class);
        TelephoneFactory factory = Extensions.require(TelephoneFactory.class, Integer.valueOf(5)); // throws ServiceNotFoundException because the context for telephone factory is string not integer
//        Telephone landline = factory.create(Integer.valueOf(5)); // cannot actually do this because of compile-time type checking
//        log.debug("Got telephone: {}", landline.getClass().getName());
//        log.debug("Calling: {}", landline.call("111-222-3333"));
        log.debug("Got factory class {}", factory.getClass().getName()); // shouldn't get here
        fail("Should have thrown exception ServiceNotFoundException: Wrong context class java.lang.Integer for implementation com.intel.dcsg.cpg.whiteboard.TraditionalTelephoneFactory");
    }
    
}
