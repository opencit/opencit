/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.trustagent.shell;

import com.intel.mtwilson.Folders;
import java.io.File;
import java.util.List;

/**
 *
 * @author dczech
 */
class WindowsShellExecutor extends GenericShellExecutor {

    @Override
    void prepareCommandOverride(List<String> cmd) {        
        // add each to front, which means it will be cmd.exe /c TPMTool.exe
        cmd.add(0, Folders.application() + File.separator + "bin" + File.separator + "TPMTool.exe");
        cmd.add(0, "/c");
        cmd.add(0, "cmd.exe");
    }
    
}
