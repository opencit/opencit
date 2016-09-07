/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.trustagent.shell;

import com.intel.mtwilson.Folders;
import com.intel.mtwilson.util.exec.ExecUtil;
import com.intel.mtwilson.util.exec.Result;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 *
 * @author dczech
 */
abstract class GenericShellExecutor implements ShellExecutor {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ShellExecutor.class);
    
    @Override
    public CommandLineResult executeTpmCommand(String commandName, String[] commandArgs, int returnCount) throws IOException {
        int returnCode;
        
                       
        List<String> cmd = new ArrayList<>();
        
        cmd.add(commandName);
        for(String param : commandArgs) {
            cmd.add(param);
        }                
        
        // call into subclasses
        prepareCommandOverride(cmd);
        
        for(String tmp : cmd) {
            log.debug(tmp);
        }
        
        ProcessBuilder pb = new ProcessBuilder(cmd);                
        Process p = pb.start();
        
        String line = "";
        if (returnCount != 0) {
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String newLine;
            try {
                while ((newLine = input.readLine()) != null) {
                    line = newLine;
                    log.debug("executeTPM output line: {}", line);
                }
                log.debug("executeTPM last line: {}", line);

                input.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (input != null) {
                    input.close();
                }
            }
        }

        //do a loop to wait for an exit value
        try {
            returnCode = p.waitFor();            
        } catch (InterruptedException e) {
            log.error("Interrupted while waiting for return value");
            log.debug("Interrupted while waiting for return value", e);
            returnCode = -1;
        }

        log.debug("Return code: " + returnCode);
        
        CommandLineResult toReturn = new CommandLineResult(returnCode, returnCount);
        toReturn.setReturnOutput(line);
        if ((returnCode == 0) && (returnCount != 0)) {
            StringTokenizer st = new StringTokenizer(line);
            if (st.countTokens() < returnCount) {
                log.debug("executeTPMCommand with return count {} but only {} tokens are available; expect java.util.NoSuchElementException", returnCount, st.countTokens());
            }
            for (int i = 0; i < returnCount; i++) {
                toReturn.setResult(i, st.nextToken());
            }
        }
        return toReturn;
    }    
    
    
    abstract void prepareCommandOverride(List<String> cmd);
}
