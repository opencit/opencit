/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package test.extensions;

import com.intel.dcsg.cpg.extensions.Extensions;
import com.intel.dcsg.cpg.extensions.Plugins;
import com.intel.dcsg.cpg.extensions.WhiteboardExtensionProvider;
import java.util.List;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class PluginExtensionTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PluginExtensionTest.class);

    @BeforeClass
    public void registerPlugins() {
        WhiteboardExtensionProvider.register(Fruit.class, Apple.class);
        WhiteboardExtensionProvider.register(Fruit.class, Orange.class);
    }
    
    public static interface Fruit {
        String getName();
        String getColor();
    }
    
    public static class Apple implements Fruit {

        @Override
        public String getName() {
            return "Apple";
        }

        @Override
        public String getColor() {
            return "red";
        }
        
    }
    
    public static class Orange implements Fruit {
        

        @Override
        public String getName() {
            return "Orange";
        }

        @Override
        public String getColor() {
            return "orange";
        }

        
    }
    
    @Test
    public void testFindPlugins() {
        
        List<Fruit> fruits = Extensions.findAll(Fruit.class);
        for(Fruit fruit : fruits) {
            log.debug("Fruit: {}", fruit.getName());  //  apple, orange
        }    
        
        Fruit apple = Plugins.findByAttribute(Fruit.class, "name", "apple");
        log.debug("Apple: {}",  apple.getColor());  // red
    }
}
