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
public class Term {
    private boolean enabled = true;
    
    private final static byte ESC = 0x1B;
    private final static int FG = 30; // add to color name;  for example 31 is red foreground
    private final static int BG = 40; // add to color name;  for example 41 is red background
    
    public static enum Attr {
        RESET(0),
        BRIGHT(1),
        DIM(2),
        UNDERLINE(3),
        BLINK(4),
        REVERSE(7),
        HIDDEN(8);
        
        private final int code;
        Attr(int code) {
            this.code = code;
        }
        public int intValue() { return code; }
    }
   
    
    public static enum Color {
        BLACK(0),
        RED(1),
        GREEN(2),
        YELLOW(3),
        BLUE(4),
        MAGENTA(5),
        CYAN(6),
        WHITE(7),
        NORMAL(9);
        
        private final int code;
        Color(int code) {
            this.code = code;
        }
        public int intValue() { return code; }
    }
    
    public void enable() { enabled = true; }
    public void disable() { enabled = false; }
    
    public String code(Attr attr, Color fg, Color bg) {
        return enabled ? String.format("%c[%d;%d;%dm", ESC, attr.intValue(), FG+fg.intValue(), BG+bg.intValue()) : "";
    }

    public String code(Attr attr, Color fg) {
        return enabled ? String.format("%c[%d;%dm", ESC, attr.intValue(), FG+fg.intValue()) : "";
    }

    public String code(Attr attr) {
        return enabled ? String.format("%c[%dm", ESC, attr.intValue()) : "";
    }
    
    public String clearLine() { return enabled ? String.format("\r%c[K", ESC) : ""; } 
    
    /*
    public void set(Attr attr, Color fg, Color bg) {
        System.out.print(code(attr,fg,bg));
    }

    public void set(Attr attr, Color fg) {
        System.out.print(code(attr,fg));
    }

    public void set(Attr attr) {
        System.out.print(code(attr));
    }
    */
    
    public String wrap(Attr attr, Color fg, Color bg, String format, Object... args) {
        return code(attr,fg,bg) + String.format(format,args) + reset();
    }

    public String wrap(Attr attr, Color fg, String format, Object... args) {
        return code(attr,fg) + String.format(format,args) + reset();
    }
    
    public String wrap(Attr attr, String format, Object... args) {
        return code(attr) + String.format(format,args) + reset();
    }
    
    public String reset() { return code(Attr.RESET); }
    public String bright() { return code(Attr.BRIGHT); }
    public String dim() { return code(Attr.DIM); }
    public String underline() { return code(Attr.UNDERLINE); }
    public String blink() { return code(Attr.BLINK); }
    public String reverse() { return code(Attr.REVERSE); }
    public String hidden() { return code(Attr.HIDDEN); }

    
    public String normal() { return code(Attr.RESET,Color.NORMAL); }
    
    public String red() { return code(Attr.DIM,Color.RED); }
    public String green() { return code(Attr.DIM,Color.GREEN); }
    public String yellow() { return code(Attr.DIM,Color.YELLOW); }
    public String blue() { return code(Attr.DIM,Color.BLUE); }
    public String magenta() { return code(Attr.DIM,Color.MAGENTA); }
    public String cyan() { return code(Attr.DIM,Color.CYAN); }
    public String white() { return code(Attr.DIM,Color.WHITE); }

    public String brightRed() { return code(Attr.BRIGHT,Color.RED); }
    public String brightGreen() { return code(Attr.BRIGHT,Color.GREEN); }
    public String brightYellow() { return code(Attr.BRIGHT,Color.YELLOW); }
    public String brightBlue() { return code(Attr.BRIGHT,Color.BLUE); }
    public String brightMagenta() { return code(Attr.BRIGHT,Color.MAGENTA); }
    public String brightCyan() { return code(Attr.BRIGHT,Color.CYAN); }
    public String brightWhite() { return code(Attr.BRIGHT,Color.WHITE); }
    
    public String redBackground() { return code(Attr.BRIGHT,Color.NORMAL,Color.RED); }
    public String greenBackground() { return code(Attr.BRIGHT,Color.NORMAL,Color.GREEN); }
    public String yellowBackground() { return code(Attr.BRIGHT,Color.NORMAL,Color.YELLOW); }
    public String blueBackground() { return code(Attr.BRIGHT,Color.NORMAL,Color.BLUE); }
    public String magentaBackground() { return code(Attr.BRIGHT,Color.NORMAL,Color.MAGENTA); }
    public String cyanBackground() { return code(Attr.BRIGHT,Color.NORMAL,Color.CYAN); }
    public String whiteBackground() { return code(Attr.BRIGHT,Color.NORMAL,Color.WHITE); }
    public String normalBackground() { return code(Attr.BRIGHT,Color.NORMAL,Color.NORMAL); }
    
//    public String red(String text) { return red() + text + normal(); }
//    public String yellow(String text) { return yellow() + text + normal(); }
//    public String green(String text) { return green() + text + normal(); }

    public String red(String text) { return wrap(Attr.BRIGHT,Color.RED,text); }
    public String yellow(String text) { return wrap(Attr.BRIGHT,Color.YELLOW,text); }
    public String green(String text) { return wrap(Attr.BRIGHT,Color.GREEN,text); }
    
    public void printlnError(String format, Object... args) {
        System.out.println(red(String.format(format, args)));
    }

    public void printlnWarning(String format, Object... args) {
        System.out.println(yellow(String.format(format, args)));
    }

    public void printlnSuccess(String format, Object... args) {
        System.out.println(green(String.format(format, args)));
    }
    
    public void printClearLine() {
        System.out.print(clearLine());
    }
}
