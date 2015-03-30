package com.intel.mountwilson.as.helper;

import com.intel.mountwilson.as.common.ASException;
import com.intel.mtwilson.i18n.ErrorCode;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang.StringUtils;

/**
 * @author dsmagadX
 */
public class CommandUtil {

    private static final Logger log = LoggerFactory.getLogger(CommandUtil.class);
    
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
}
