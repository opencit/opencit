/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup.ui.console;

import com.intel.mtwilson.validation.InputModel;
import com.intel.mtwilson.datatypes.Hostname;
import com.intel.mtwilson.datatypes.InternetAddress;
import com.intel.mtwilson.setup.*;
import com.intel.mtwilson.setup.cmd.*;
import com.intel.mtwilson.setup.model.*;
import com.intel.mtwilson.validation.Fault;
import java.io.Console;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple console program to obtain user preferences for either a local or
 * remote Mt Wilson configuration. 
 * 
 * The purpose of this program is to obtain and validate user input only - 
 * network access is allowed for input validation (server addresses, database
 * connections, etc) but not to effect any changes. Also this program should not
 * effect any changes on the local host.
 * 
 * The output of this program is a complete "mtwilson.properties" file in
 * the current directory. It can then be used to configure the local instance
 * (using another command) or copied to another server and used there.
 * 
 * How to run it:
 * java -cp setup-console-0.5.4-SNAPSHOT-with-dependencies.jar com.intel.mtwilson.setup.ui.console.Main
 * 
 * @author jbuhacoff
 */
public class Main {
    public static final Console console = System.console();
    public static final SetupContext ctx = new SetupContext();

    private static final InternetAddressInput INTERNET_ADDRESS_INPUT = new InternetAddressInput();
    private static final URLInput URL_INPUT = new URLInput();
    private static final YesNoInput YES_NO_INPUT = new YesNoInput();
    private static final IntegerInput INTEGER_INPUT = new IntegerInput();
    private static final StringInput STRING_INPUT = new StringInput();
    
    
    /**
     * Argument 1:  "local" or "remote"  to indicate if we are setting up the local host or if we are setting up a cluster remotely;  case-insensitive
     * @param args 
     */
    public static void main(String[] args) {
        if (console == null) {
            System.err.println("No console.");
            System.exit(1);
        }
        try {
            if( args.length > 0 ) {
                ctx.target = SetupTarget.valueOf(args[0].toUpperCase()); 
            }
            else {
                ctx.target = SetupTarget.LOCAL;
            }
            collectUserInput();            
            displaySummary();
        }
        catch(Exception e) {
            e.printStackTrace(System.err);
        }
    }
    
    public static void displaySummary() {
        System.out.println("Mt Wilson URL: "+ctx.serverUrl.toExternalForm());
        System.out.println("Mt Wilson Database: "+ctx.databaseServer.type.displayName());
        System.out.println("          hostname: "+ctx.databaseServer.hostname.toString());
        System.out.println("              port: "+ctx.databaseServer.port);
        System.out.println("          username: "+ctx.databaseServer.username);
        System.out.println("          password: "+ctx.databaseServer.password);
    }
    
    public static void collectUserInput() throws SocketException { // XXX TODO: probably should catch the exception, and ask the user if they want to continue setup or abort.
        inputMtWilsonURL();
        inputMtWilsonDatabase();
        inputManagementServiceAdminCredentials();
        inputEkSigningKeyCredentials();

        if( ctx.target.equals(SetupTarget.LOCAL)) {
            inputLocalCAPassword();
            generateSamlSigningKey();
        }
        else {
            inputRemoteCAInfo();
        }        
        
        
    }
    
    
    private static String getConfirmedPasswordWithPrompt(String prompt) {
        while(true) {
            System.out.println(prompt);
            char[] password = console.readPassword("Password: ");
            char[] passwordAgain = console.readPassword("Password (again):");
            if( password.length == passwordAgain.length && String.valueOf(password).equals(String.valueOf(passwordAgain)) ) {
                return String.valueOf(password);
            }
        }
    }

    private static String getRequiredPasswordWithPrompt(String prompt) {
        while(true) {
            System.out.println(prompt);
            char[] password = console.readPassword("Password: ");
            if( password.length > 0 ) {
                return String.valueOf(password);
            }
        }
    }

    private static int getSelectionFromListWithPrompt(List<String> list, String prompt) {
        while(true) {
            System.out.println(prompt);
            for(int i=0; i<list.size(); i++) {
                System.out.println(String.format("[%2d] %s", i+1, list.get(i)));
            }
            String selection = console.readLine("Choose 1-%d: ", list.size());
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

    private static <T> T getRequiredEnumWithPrompt(Class<T> clazz, String prompt) {
        T[] list = clazz.getEnumConstants();
        if( list == null ) { throw new IllegalArgumentException(clazz.getName()+" is not an enum type"); }
        ArrayList<String> strings = new ArrayList<String>();
        for( T item : list ) {
            strings.add(item.toString());
        }
        int selected = getSelectionFromListWithPrompt(strings, prompt);
        return list[selected];
    }
    
    private static String getRequiredStringWithPrompt(String prompt) {
        return getRequiredInputWithPrompt(STRING_INPUT, prompt, "String:");
    }

    
    private static Integer getRequiredIntegerWithPrompt(String prompt) {
        return getRequiredInputWithPrompt(INTEGER_INPUT, prompt, "Integer:");
    }
    

    private static Integer getRequiredIntegerInRangeWithPrompt(int min, int max, String prompt) {
        return getRequiredInputWithPrompt(new IntegerInput(min,max), prompt, String.format("Integer [%d-%d]:", min, max));
    }
    
    /**
     * 
     * @param prompt
     * @return true for Yes, false for No
     */
    private static boolean getRequiredYesNoWithPrompt(String prompt) {
        return getRequiredInputWithPrompt(YES_NO_INPUT, prompt, "[Y]es or [N]o:").booleanValue();
    }

    private static URL getRequiredURLWithPrompt(String prompt) {
        return getRequiredInputWithPrompt(URL_INPUT, prompt, "URL:");
    }

    private static InternetAddress getRequiredNetworkAddressWithPrompt(String prompt) {
        return getRequiredInputWithPrompt(INTERNET_ADDRESS_INPUT, prompt, "Hostname or IP Address:");
    }
    
    private static <T> T getRequiredInputWithPrompt(InputModel<T> model, String caption, String prompt) {
        while(true) {
            System.out.println(caption);
            String input = console.readLine(prompt+" ");
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
    
    private static InternetAddress getRequiredInternetAddressWithMenuPrompt(String prompt) throws SocketException {
        SetMtWilsonURL cmd = new SetMtWilsonURL();
        List<String> options = cmd.getLocalAddresses();
        options.add("Other");
        int selected = getSelectionFromListWithPrompt(options, prompt);
        InternetAddress address; 
        if( selected == options.size() - 1 ) { // "Other"
            address = getRequiredNetworkAddressWithPrompt("Other "+prompt);
        }
        else {
            address = new InternetAddress(options.get(selected));
        }
        return address;
    }
    
    public static void inputMtWilsonURL() throws SocketException {
        if( ctx.target.equals(SetupTarget.LOCAL) ) {
            InternetAddress address = getRequiredInternetAddressWithMenuPrompt("Local Mt Wilson Hostname or IP Address");
            System.out.println("selected: "+address.toString());
            ctx.serverAddress = address;
        }
        else {
            InternetAddress address = getRequiredNetworkAddressWithPrompt("Remote Mt Wilson Hostname or IP Address");
            System.out.println("selected: "+address.toString());
            ctx.serverAddress = address;
        }
        
        WebContainerType webContainerType = getRequiredEnumWithPrompt(WebContainerType.class, "Web application container");
        String defaultUrl = String.format("https://%s:%d", ctx.serverAddress, webContainerType.defaultHttpsPort());
        boolean urlOk = getRequiredYesNoWithPrompt(String.format("Default Mt Wilson URL: %s\nIs this ok?", defaultUrl));
        if( urlOk ) {
            try {
                ctx.serverUrl = new URL(defaultUrl);
            }
            catch(MalformedURLException e) {
                System.err.println("There is a problem with this URL: "+defaultUrl);
                ctx.serverUrl = getRequiredURLWithPrompt("Please enter the Mt Wilson URL");
            }
        }
        else {
            ctx.serverUrl = getRequiredURLWithPrompt("Please enter the Mt Wilson URL");
        }
        ctx.serverPort = ctx.serverUrl.getPort();                
        
    }
    
    public static void inputMtWilsonDatabase() throws SocketException {
        Database db = new Database();
        db.type = getRequiredEnumWithPrompt(DatabaseType.class, "Database system");
        db.driver = db.type.defaultJdbcDriver();
        db.hostname = getRequiredInternetAddressWithMenuPrompt("Database server Hostname or IP Address");
        boolean useNonDefaultPort = getRequiredYesNoWithPrompt(String.format("Default port is %d. Do you want to change it?", db.type.defaultPort()));
        if( useNonDefaultPort ) {
            db.port = getRequiredIntegerInRangeWithPrompt(0,65535,"Database port");
        }
        else {
            db.port = db.type.defaultPort();
        }
        db.username = getRequiredStringWithPrompt("Database username");
        db.password = getRequiredPasswordWithPrompt("Database password");
        
        ctx.databaseServer = db;
        // TODO: verify the connection & login;  maybe do it outside this function so the entire thing can be repeated as necessary.
    }
    
    public static void inputEkSigningKeyCredentials() {
        System.out.println("In order to authorize Linux hosts using Trust Agent, an EK Signing Key is downloaded from Mt Wilson.");
        System.out.println("You must set a username and password to authenticate administrators who are downloading the key during a Trust Agent install.");
        PrivacyCA pca = new PrivacyCA();
        pca.ekSigningKeyDownloadUsername = getRequiredStringWithPrompt("EK Signing Key Download Username");
        pca.ekSigningKeyDownloadPassword = getConfirmedPasswordWithPrompt("EK Signing Key Download Password");
        
        ctx.privacyCA = pca;
    }


    public static void inputManagementServiceAdminCredentials() {
        System.out.println("You must set a username and password for the first Mt Wilson administrator account.");
        AdminUser admin = new AdminUser();
        admin.username = getRequiredStringWithPrompt("Administrator Username");
        admin.password = getConfirmedPasswordWithPrompt("Administrator Password");
        
        ctx.admin = admin;
    }

    private static void inputLocalCAPassword() {
        throw new UnsupportedOperationException("not implemented yet: inputLocalCAPassword");
    }
    private static void inputRemoteCAInfo() {
        throw new UnsupportedOperationException("not implemented yet: inputRemoteCAInfo");
    }
    
    private static void generateSamlSigningKey() {
        throw new UnsupportedOperationException("not implemented yet: generateSamlSigningKey");
    }
}
