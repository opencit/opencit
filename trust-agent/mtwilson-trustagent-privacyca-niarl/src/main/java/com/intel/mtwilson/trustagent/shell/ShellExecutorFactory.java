/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.trustagent.shell;

/**
 *
 * @author dczech
 */
public class ShellExecutorFactory {
    public enum OS {
        Unix,
        Windows
    }
    
    private ShellExecutorFactory() { }
    
    static final ShellExecutor WINDOWSSHELL = new WindowsShellExecutor(), UNIXSHELL = new UnixShellExecutor();    
    
    public static ShellExecutor getInstance(OS operatingSystem) {
        switch(operatingSystem) {
            case Windows:
                return WINDOWSSHELL;
            default:
                return UNIXSHELL;                
        }
    }
}
