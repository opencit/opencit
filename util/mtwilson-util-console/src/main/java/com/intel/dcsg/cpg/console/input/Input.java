/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.console.input;

import com.intel.dcsg.cpg.net.InternetAddress;
import com.intel.dcsg.cpg.validation.Fault;
import com.intel.dcsg.cpg.validation.InputModel;
import com.intel.dcsg.cpg.validation.Model;
import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jbuhacoff
 */
public class Input {
    private static final Console console = System.console();
    private static final InternetAddressInput INTERNET_ADDRESS_INPUT = new InternetAddressInput();
    private static final URLInput URL_INPUT = new URLInput();
    private static final YesNoInput YES_NO_INPUT = new YesNoInput();
    private static final IntegerInput INTEGER_INPUT = new IntegerInput();
    private static final StringInput STRING_INPUT = new StringInput();
    
    public static char[] readPassword(String format, Object... args) throws IOException {
        if( console != null ) {
            return console.readPassword(format, args);
        }
        // no console, so use system.out
        System.out.println("Warning: your password will be displayed when you type it");
        System.out.println(String.format(format, args));
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String password = reader.readLine();
        if( password == null ) { return new char[0]; }
        return password.toCharArray();
    }

    public static String readLine(String format, Object... args) throws IOException {
        if( console != null ) {
            return console.readLine(format, args);
        }
        // no console, so use system.out
        System.out.println(String.format(format, args));
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String input = reader.readLine();
        return input;
    }
    
    public static String getConfirmedPasswordWithPrompt(String prompt) throws IOException {
        while(true) {
            if( prompt != null && !prompt.isEmpty() ) { System.out.println(prompt); }
            char[] password = readPassword("Password: ");
            char[] passwordAgain = readPassword("Password (again):");
            if( password.length == passwordAgain.length && String.valueOf(password).equals(String.valueOf(passwordAgain)) ) {
                return String.valueOf(password);
            }
            System.out.println("Passwords must match.");
        }
    }

    public static String getRequiredPasswordWithPrompt(String prompt) throws IOException {
        while(true) {
            if( prompt != null && !prompt.isEmpty() ) { System.out.println(prompt); }
            char[] password = readPassword("Password: ");
            if( password.length > 0 ) {
                return String.valueOf(password);
            }
        }
    }

    public static int getSelectionFromListWithPrompt(List<String> list, String prompt) throws IOException {
        while(true) {
            if( prompt != null && !prompt.isEmpty() ) { System.out.println(prompt); }
            for(int i=0; i<list.size(); i++) {
                System.out.println(String.format("[%2d] %s", i+1, list.get(i)));
            }
            String selection = readLine("Choose 1-%d: ", list.size());
            try {
                Integer value = Integer.valueOf(selection);
                if( value >=1 && value <= list.size() ) {
                    return value-1;
                }
            }
            catch(java.lang.NumberFormatException e) {
                System.err.println("Press Ctrl+C to exit");
            }
        }
    }

    public static <T> T getRequiredEnumWithPrompt(Class<T> clazz, String prompt) throws IOException {
        T[] list = clazz.getEnumConstants();
        if( list == null ) { throw new IllegalArgumentException(clazz.getName()+" is not an enum type"); }
        ArrayList<String> strings = new ArrayList<String>();
        for( T item : list ) {
            strings.add(item.toString());
        }
        int selected = getSelectionFromListWithPrompt(strings, prompt);
        return list[selected];
    }
    
    public static String getRequiredStringWithPrompt(String prompt) throws IOException {
        return getRequiredInputWithPrompt(STRING_INPUT, prompt, "String:");
    }

    
    public static Integer getRequiredIntegerWithPrompt(String prompt) throws IOException {
        return getRequiredInputWithPrompt(INTEGER_INPUT, prompt, "Integer:");
    }
    

    public static Integer getRequiredIntegerInRangeWithPrompt(int min, int max, String prompt) throws IOException {
        return getRequiredInputWithPrompt(new IntegerInput(min,max), prompt, String.format("Integer [%d-%d]:", min, max));
    }
    
    /**
     * 
     * @param prompt
     * @return true for Yes, false for No
     */
    public static boolean getRequiredYesNoWithPrompt(String prompt) throws IOException {
        return getRequiredInputWithPrompt(YES_NO_INPUT, prompt, "[Y]es or [N]o:").booleanValue();
    }

    public static URL getRequiredURLWithPrompt(String prompt) throws IOException {
        return getRequiredInputWithPrompt(URL_INPUT, prompt, "URL:");
    }
    public static URL getRequiredURLWithDefaultPrompt(String prompt, String defaultValue) throws IOException {
        return getRequiredInputWithDefaultPrompt(URL_INPUT, prompt, "URL:", defaultValue);
    }

    public static InternetAddress getRequiredInternetAddressWithPrompt(String prompt) throws IOException {
        return getRequiredInputWithPrompt(INTERNET_ADDRESS_INPUT, prompt, "Hostname or IP Address:");
    }
    public static InternetAddress getRequiredInternetAddressWithDefaultPrompt(String prompt, String defaultValue) throws IOException {
        return getRequiredInputWithDefaultPrompt(INTERNET_ADDRESS_INPUT, prompt, "Hostname or IP Address:", defaultValue);
    }
    
    public static <T> T getRequiredInputWithPrompt(InputModel<T> model, String caption, String prompt) throws IOException {
        while(true) {
            if( caption != null && !caption.isEmpty() ) { System.out.println(caption); }
            String input = readLine(prompt+" ");
            model.setInput(input);
            if( model.isValid() ) {
                return model.value();
            }
            else {
                printFaults(model);
            }            
            // TODO: allow user to break by typing 'exit', 'cancel', 'abort', etc, and we can throw an exception like UserAbortException (must create it) so the main program can have a chance to save what has already been validated and exit, or skip to the next step, or something.
        }
    }
    
    private static void printFaults(Model model) {
        System.err.println("--- Errors ---");
        for(Fault f : model.getFaults()) {
            printFault(f, 0); // level 0 means no indentation
        }
    }
    
    /**
     * 
     * @param f
     * @param level of indentation;  use 0 for top-level faults, and increment once for each level of logical nesting
     */
    private static void printFault(Fault f, int level) {
        StringBuilder spaces = new StringBuilder(level*2); 
        for(int i=0; i<level; i++) { spaces.append("  "); } // each level is indented two spaces from the previous level
        String indentation = spaces.toString(); 
        System.err.println(String.format("%s- %s", indentation, f.toString()));
//        if( f.getCause() != null ) {
//            System.err.println(String.format("%s  Caused by: %s", indentation, f.getCause().toString()));
//        }
        if( !f.getFaults().isEmpty() ) {
            System.err.println(String.format("%s  Related errors:", indentation));
            for(Fault related : f.getFaults()) {
                printFault(related, level+1);
            }
        }
    }

    public static <T> T getRequiredInputWithDefaultPrompt(InputModel<T> model, String caption, String prompt, String defaultValue) throws IOException {
        while(true) {
            if( caption != null && !caption.isEmpty() ) { System.out.println(caption); }
            String input = readLine(prompt+" ["+defaultValue+"] ");
            if( input == null || input.isEmpty() ) { input = defaultValue; }
            model.setInput(input);
            if( model.isValid() ) {
                return model.value();
            }
            else {
                // TODO: print faults
                for(Fault f : model.getFaults()) {
                    System.err.println(f.toString());
                }
            }            
            // TODO: allow user to break by typing 'exit', 'cancel', 'abort', etc, and we can throw an exception like UserAbortException (must create it) so the main program can have a chance to save what has already been validated and exit, or skip to the next step, or something.
        }
    }
    
    /*
    private InternetAddress getRequiredInternetAddressWithMenuPrompt(String prompt) throws SocketException, IOException {
        SetMtWilsonURL cmd = new SetMtWilsonURL();
        List<String> options = cmd.getLocalAddresses();
        if( ctx.serverAddress != null && !options.contains(ctx.serverAddress.toString())) { 
            options.add(ctx.serverAddress.toString());
        }
        options.add("Other");
        int selected = getSelectionFromListWithPrompt(options, prompt);
        InternetAddress address; 
        if( selected == options.size() - 1 ) { // "Other"
            address = getRequiredInternetAddressWithPrompt("Other "+prompt);
        }
        else {
            address = new InternetAddress(options.get(selected));
        }
        return address;
    }
    */
        
}
