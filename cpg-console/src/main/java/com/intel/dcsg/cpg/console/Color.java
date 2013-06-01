/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.console;

/**
 * Assumes the terminal is color compatible. If it's not, just disable the Color to avoid
 * printing the escape codes.
 * 
 * @author jbuhacoff
 */
public class Color {
    private boolean enabled = true;
    
    public void enable() { enabled = true; }
    public void disable() { enabled = false; }
    
    public String red() { return enabled ? "\\033[1;31m" : ""; }
    public String green() { return enabled ? "\\033[1;32m" : ""; }
    public String yellow() { return enabled ? "\\033[1;33m" : ""; }
    public String blue() { return enabled ? "\\033[1;34m" : ""; }
    public String magenta() { return enabled ? "\\033[1;35m" : ""; }
    public String cyan() { return enabled ? "\\033[1;36m" : ""; }
    public String white() { return enabled ? "\\033[1;37m" : ""; }
    public String normal() { return enabled ? "\\033[0;39m" : ""; }

    public String redBackground() { return enabled ? "\\033[1;31m" : ""; }
    public String greenBackground() { return enabled ? "\\033[1;32m" : ""; }
    public String yellowBackground() { return enabled ? "\\033[1;33m" : ""; }
    public String blueBackground() { return enabled ? "\\033[1;34m" : ""; }
    public String magentaBackground() { return enabled ? "\\033[1;35m" : ""; }
    public String cyanBackground() { return enabled ? "\\033[1;36m" : ""; }
    public String whiteBackground() { return enabled ? "\\033[1;37m" : ""; }
    public String normalBackground() { return enabled ? "\\033[0;39m" : ""; }
    
    public String red(String text) { return red() + text + normal(); }
    public String yellow(String text) { return yellow() + text + normal(); }
    public String green(String text) { return green() + text + normal(); }
    
    public void error(String format, Object... args) {
        System.out.println(red(String.format(format, args)));
    }

    public void warning(String format, Object... args) {
        System.out.println(yellow(String.format(format, args)));
    }

    public void success(String format, Object... args) {
        System.out.println(green(String.format(format, args)));
    }
    
}
