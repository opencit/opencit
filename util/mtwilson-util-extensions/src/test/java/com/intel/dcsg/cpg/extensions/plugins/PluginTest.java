/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.extensions.plugins;

import com.intel.dcsg.cpg.extensions.Plugins;
import com.intel.dcsg.cpg.extensions.WhiteboardExtensionProvider;
import com.intel.mtwilson.pipe.Filter;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jbuhacoff
 */
public class PluginTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PluginTest.class);

    @BeforeClass
    public static void registerPlugins() {
        WhiteboardExtensionProvider.register(Widget.class, WidgetFoo.class);
        WhiteboardExtensionProvider.register(Widget.class, WidgetBar.class);
    }
    
    private void useWidget(Widget widget) {
        assertNotNull(widget);
        log.debug("Widget class {} type {}", widget.getClass().getName(), widget.getType());
        widget.run();
    }
    
    @Test
    public void testPlugin() {
        Widget foo = Plugins.findByAttribute(Widget.class, "type", "foo");
        useWidget(foo);
        Widget bar = Plugins.findByAttribute(Widget.class, "type", "bar");
        useWidget(bar);
        Widget nonexistent = Plugins.findByAttribute(Widget.class, "type", "nonexistent");
        assertNull(nonexistent);
    }
    
    @Test
    public void testPluginWithCustomFilter() {
        Widget foo = Plugins.find(Widget.class, new WidgetClassNameFilter("foo"));
        useWidget(foo);
        Widget bar = Plugins.find(Widget.class, new WidgetClassNameFilter("bar"));
        useWidget(bar);
        Widget nonexistent = Plugins.find(Widget.class, new WidgetClassNameFilter("nonexistent"));
        assertNull(nonexistent);
    }
    
    public static class WidgetClassNameFilter<T> implements Filter<T> {
        private String abbrev;

        public WidgetClassNameFilter(String abbrev) {
            this.abbrev = abbrev;
        }
        
        @Override
        public boolean accept(T item) {
            return item.getClass().getName().toLowerCase().endsWith(abbrev);
        }
    
}
    
}
