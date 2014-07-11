package com.intel.mountwilson.as.helper;

import com.intel.mountwilson.as.common.ASException;
import com.intel.mtwilson.i18n.ErrorCode;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang.StringUtils;

/**
 * @author dsmagadX
 */
public class CommandUtil {

    private static final Logger log = LoggerFactory.getLogger(CommandUtil.class);
    private static final Pattern singleQuoteShellSpecialCharacters = Pattern.compile("[*?#~=%\\[]");
    private static final Pattern anySingleQuoteShellSpecialCharacters = Pattern.compile("(.*?)" + singleQuoteShellSpecialCharacters.pattern() + "(.*?)");
    private static final Pattern doubleQuoteShellSpecialCharacters = Pattern.compile("[$`*@\\\\]");
    private static final Pattern anyDoubleQuoteShellSpecialCharacters = Pattern.compile("(.*?)" + doubleQuoteShellSpecialCharacters.pattern() + "(.*?)");

    public static List<String> runCommand(String commandLine, boolean readResult, String commandAlias) {
        List<String> result = new ArrayList<String> ();

        try {
            int returnCode;

            log.trace("Running command {}", commandLine);

            Process p = Runtime.getRuntime().exec(commandLine);

            if (readResult) {
            	try(InputStream in = p.getInputStream()) {
                BufferedReader input = new BufferedReader(new InputStreamReader(in));



                String newLine;

                while ((newLine = input.readLine()) != null) {
                    result.add(newLine);
                }

                input.close();
            	}
            }
            String resultForLog = result.size()+" items:\n"+StringUtils.join(result, "\n");
            log.trace("Result Output \n{}", resultForLog);
            //do a loop to wait for an exit value
            boolean isRunning;
            int timeout = 5000;
            int countToTimeout = 0;

            do {
                countToTimeout++;
                isRunning = false;
                try {
                    /*returnCode = */ p.exitValue();
                } catch (IllegalThreadStateException e1) {
                    isRunning = true;
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e2) {
                        isRunning = false;
                    }
                }
            } while (isRunning
                    && (countToTimeout < timeout));


            if (countToTimeout
                    == timeout) {
                log.trace("Command is not responding.");
                p.destroy();

            }

            returnCode = p.exitValue();


            log.trace("Return code {}", String.valueOf(returnCode));

            if (returnCode != 0) {
                throw new ASException(ErrorCode.AS_QUOTE_VERIFY_COMMAND_FAILED, returnCode);
            }

        } catch (ASException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ASException(ex);
        }
        return result;
    }

    // not being uesd.  also, use IOUtils to read contents of entire file into string 
    /*
    public static byte[] readfile(String fileName) throws Exception {

        byte[] fileContents = null;

        try {
            InputStream fStream = new FileInputStream(fileName);
            fileContents = new byte[fStream.available()];
            fStream.read(fileContents);

            fStream.close();
        } catch (Exception ex) {
            log.log(Level.SEVERE, null, ex);
            throw ex;
        }
        return fileContents;
    }
    */
    
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
