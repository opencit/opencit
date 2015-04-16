/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.common;

import com.intel.mtwilson.datatype.IPAddress;
import java.io.*;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Date;
import java.util.Enumeration;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.io.IOUtils;
/**
 *
 * @author dsmagadX
 */
public class CommandUtil {
    
    private static final Logger log = LoggerFactory.getLogger(CommandUtil.class.getName());

    public static CommandResult runCommand(String commandLine) throws TAException, IOException {
        return runCommand(commandLine, null);
    }
    
    public static CommandResult runCommand(String commandLine, String[] envp) throws TAException, IOException {
        
        if(StringUtils.isBlank(commandLine))
            throw new TAException(ErrorCode.ERROR,"Command cannot be empty.");
        
        String[] command = commandLine.split(" ");
        
        if(new File(Config.getBinPath() + File.separator + command[0]).exists())
            commandLine = Config.getBinPath() + File.separator + commandLine;

        if(new File(Config.getBinPath() + File.separator + commandLine).exists())
            commandLine = Config.getBinPath() + File.separator + commandLine;
        
        log.debug("Command to be executed is :" + commandLine);

        Process p;
        if( envp == null ) {
            p = Runtime.getRuntime().exec(commandLine);
        }
        else {
            p = Runtime.getRuntime().exec(commandLine, envp);
        }
        // read stdout
        InputReader stdout = new InputReader(p.getInputStream());
        Thread stdoutThread = new Thread(stdout);
        stdoutThread.start();
        // read stderr
        InputReader stderr = new InputReader(p.getErrorStream());
        Thread stderrThread = new Thread(stderr);
        stderrThread.start();
        CommandResult result = new CommandResult();
        try {
        // wait until the process exits
        result.exitcode = p.waitFor();
        // after the process exits the stdout and stderr threads will terminate
        stdoutThread.join(); // throws InterruptedException
        stderrThread.join(); // throws InterruptedException
        }
        catch(InterruptedException e) {
            log.error("Interrupted", e);
        }
        
        log.debug("stdout:\n{}", stdout.getResult());
        log.debug("stderr:\n{}", stderr.getResult());

        result.command = commandLine;
        result.stdout = stdout.getResult();
        result.stderr = stderr.getResult();

        if( result.exitcode != 0 ) {
            throw new TAException(ErrorCode.FATAL_ERROR, result.exitcode + ": Error while running command: " + commandLine);            
        }

        return result;
    }
        
    public static byte[] readfile(String fileName) throws TAException {
        
        try (InputStream fStream = new FileInputStream(fileName)) {
            int fileLength = (int) new File(fileName).length();
            byte[] fileContents = new byte[fileLength];
            int read = fStream.read(fileContents);
            if (read != fileLength) {
                log.warn("Length of file read is not same as file length");
            }
            return fileContents;
        } catch (Exception ex) {
            throw new TAException(ErrorCode.ERROR, "Error while reading cert", ex);
        } 
    }

    public static String readCertificate(String fileName) throws TAException {
        try {
            String pem;
            try (FileInputStream in = new FileInputStream(new File(fileName))) {
                pem = IOUtils.toString(in);
            }
//            X509Certificate certificate = X509Util.decodePemCertificate(pem);
            return pem;
        } catch (Exception e) {
            throw new TAException(ErrorCode.ERROR, "Error while reading AIK Cert", e);
        }
    }

    public static String getHostIpAddress() {
        String localIpAddress = "127.0.0.1";
        Enumeration<NetworkInterface> networkInterface;
        try {

            networkInterface = NetworkInterface.getNetworkInterfaces();
            for (; networkInterface.hasMoreElements();) {
                NetworkInterface e = networkInterface.nextElement();
                log.debug( "Interface: {}", new Object[]{e.getName()});
                Enumeration<InetAddress> ad = e.getInetAddresses();
                for (; ad.hasMoreElements();) {
                    InetAddress addr = ad.nextElement();
                    String returnIpAddress = addr.getHostAddress();
                    if (!returnIpAddress.equals(localIpAddress) && IPAddress.isValid(returnIpAddress)) {
                        return returnIpAddress;
                    } else {
                        log.debug("{} == {} or ip validation failed.", new Object[]{returnIpAddress, localIpAddress});
                    }
                }
            }

        } catch (Exception ex) {
            log.error("Error while getting the network interfaces returning 127.0.0.1", ex);
        }
        return localIpAddress;
    }

    public static String generateErrorResponse(ErrorCode errorCode) {
        return generateErrorResponse(errorCode, null);
    }
    
    public static String generateErrorResponse(ErrorCode errorCode, String optionalDescription) {
        String extra = "";
        if( optionalDescription != null ) {
            extra = ": "+optionalDescription;
        }
        String responseXML =
                "<client_request> "
                + "<timestamp>" + new Date(System.currentTimeMillis()).toString() + "</timestamp>"
                + "<clientIp>" + StringEscapeUtils.escapeXml(CommandUtil.getHostIpAddress()) + "</clientIp>"
                + "<error_code>" + errorCode.getErrorCode() + "</error_code>"
                + "<error_message>" + StringEscapeUtils.escapeXml(errorCode.getMessage() + extra) + "</error_message>"
                + "</client_request>";
        return responseXML;
    }
}
