/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.trustagent.shell;

import java.io.IOException;

/**
 *
 * @author dczech
 */
public interface ShellExecutor {
    CommandLineResult executeTpmCommand(String command, String[] args, int returnCount) throws IOException;
    
}
