/*
 * Copyright (C) 2015 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.util.exec;

import java.util.regex.Pattern;

/**
 *
 * @author rksavino
 */
public class EscapeUtil {
    private static final Pattern singleQuoteShellSpecialCharacters = Pattern.compile("[*?#~=%\\[]");
    private static final Pattern anySingleQuoteShellSpecialCharacters = Pattern.compile("(.*?)" + singleQuoteShellSpecialCharacters.pattern() + "(.*?)");
    private static final Pattern doubleQuoteShellSpecialCharacters = Pattern.compile("[$`*@\\\\]");
    private static final Pattern anyDoubleQuoteShellSpecialCharacters = Pattern.compile("(.*?)" + doubleQuoteShellSpecialCharacters.pattern() + "(.*?)");
    
    // This function returns true if the string input contains bash/shell single quote special characters
    public static boolean containsSingleQuoteShellSpecialCharacters(String input) {
        return anySingleQuoteShellSpecialCharacters.matcher(input).matches();
    }
    
    // This function returns true if the string input contains bash/shell double quote special characters
    public static boolean containsDoubleQuoteShellSpecialCharacters(String input) {
        return anyDoubleQuoteShellSpecialCharacters.matcher(input).matches();
    }
    
    // This function will escape special characters in an argument being passed to the bash/shell command line
    public static String singleQuoteEscapeShellArgument(String input) {
        return "\'" + input.replaceAll(singleQuoteShellSpecialCharacters.pattern(), "\\\\$0") + "\'";
    }
    
    // This function will escape special characters in an option being passed to the bash/shell command line
    public static String singleQuoteEscapeShellOption(String input) {
        if (input.contains("=")) {
            String[] option = input.split("=", 2);
            String parameter = option[0];
            String value = option[1];
            return parameter + "=\'" + value.replaceAll(singleQuoteShellSpecialCharacters.pattern(), "\\\\$0") + "\'";
        } else {
            return singleQuoteEscapeShellArgument(input);
        }
    }
    
    // Overload for supplying both the parameter and argument value
    public static String singleQuoteEscapeShellOption(String parameterName, String argumentValue) {
        return String.format("%s=%s", singleQuoteEscapeShellArgument(parameterName), singleQuoteEscapeShellArgument(argumentValue));
    }
    
    // This function will escape special characters in an argument being passed to the bash/shell command line
    public static String doubleQuoteEscapeShellArgument(String input) {
        return input.replaceAll(doubleQuoteShellSpecialCharacters.pattern(), "\\\\$0");
    }
    
    // This function will escape special characters in an option being passed to the bash/shell command line
    public static String doubleQuoteEscapeShellOption(String input) {
        if (input.contains("=")) {
            String[] option = input.split("=", 2);
            String parameter = option[0];
            String value = option[1];
            return parameter + "=\'" + value.replaceAll(doubleQuoteShellSpecialCharacters.pattern(), "\\\\$0") + "\'";
        } else {
            return doubleQuoteEscapeShellArgument(input);
        }
    }
    
    // Overload for supplying both the parameter and argument value
    public static String doubleQuoteEscapeShellOption(String parameterName, String argumentValue) {
        return String.format("%s=%s", doubleQuoteEscapeShellArgument(parameterName), doubleQuoteEscapeShellArgument(argumentValue));
    }
}
