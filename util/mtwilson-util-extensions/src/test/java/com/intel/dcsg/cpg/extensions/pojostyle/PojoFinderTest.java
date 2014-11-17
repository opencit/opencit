/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.extensions.pojostyle;
import com.intel.dcsg.cpg.extensions.ImplementationRegistrar;
import com.intel.dcsg.cpg.extensions.Extensions;
import com.intel.dcsg.cpg.extensions.ExtensionUtil;
import com.intel.dcsg.cpg.extensions.WhiteboardExtensionProvider;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jbuhacoff
 */
public class PojoFinderTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PojoFinderTest.class);

    @Test
    public void testEasyScanWithInterface() {
        log.debug("Scanning");
        ExtensionUtil.scan(new ImplementationRegistrar(), Apple.class, Banana.class, Fruit.class); // should ignore Fruit interface and register only the implementations
    }
    
    private void eatFruit(String inputFruitName) {
        log.debug("Finding all {} fruits", inputFruitName);
        List<Fruit> fruits = Extensions.findAll(Fruit.class);
        for(Fruit fruit : fruits) {
            log.debug("Looking at fruit name: {} with color: {}", 
                    fruit.getName(), fruit.getColor());
            if( inputFruitName.equals(fruit.getName())) {
                log.debug("Found {} {}", fruit.getColor(), inputFruitName);
            }
        }        
    }
    
    @Test
    public void testUsePlugins() {
        WhiteboardExtensionProvider.clearAll();
        // initialize whiteboard
        testEasyScanWithInterface();
        // now pretend to do something useful that requires fruit plugins
        eatFruit("apple");
        // runtime addition of new plugin
        WhiteboardExtensionProvider.register(Fruit.class, Carrot.class);
        // now try to use it
        eatFruit("carrot");
        // runtime clearing of available plugins
        WhiteboardExtensionProvider.clear(Fruit.class);
        eatFruit("banana"); // output: No registered implementations for com.intel.dcsg.cpg.whiteboard.Fruit
    }
}
